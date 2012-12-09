//
// OpenIrekia v2.0 Cliente Android
//
// Copyright 2009-2011 eFaber, S.L.
// Copyright 2009-2011 Ejie, S.A.
// Copyrigth 2009-2011 Dirección de Gobierno Abierto y Comunicación en Internet; 
//    Gobernu Irekirako eta Interneteko Komunikaziorako Zuzendaritza; Lehendakaritza.
//    Gobierno Vasco – Eusko Jaurlaritza 
// Licencia con arreglo a la EUPL, Versión 1.1 o –en cuanto sean aprobadas 
// por la Comisión Europea– versiones posteriores de la EUPL (la Licencia);
// Solo podrá usarse esta obra si se respeta la Licencia. Puede obtenerse una 
// copia de la Licencia en: http://ec.europa.eu/idabc/eupl 
// Salvo cuando lo exija la legislación aplicable o se acuerde por escrito, 
// el programa distribuido con arreglo a la Licencia se distribuye TAL CUAL,
// SIN GARANTÍAS NI CONDICIONES DE NINGÚN TIPO, ni expresas ni implícitas.
// Véase la Licencia en el idioma concreto que rige los permisos y limitaciones 
// que establece la Licencia
//
//  http://open.irekia.net, openirekia@efaber.net

package net.efaber.irekia.zerg;

import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import net.efaber.irekia.JSON;
import net.efaber.irekia.models.Content_item;
import net.efaber.irekia.models.Content_item_comparator;
import net.efaber.irekia.models.Section_comparator;
import net.efaber.irekia.models.Section_state;
import android.os.AsyncTask;
import android.util.Log;

import com.github.droidfu.http.BetterHttp;
import com.github.droidfu.http.BetterHttpResponse;

/** Class in charge of fetching the new JSON data from the server.
 * Broodlings are usually spawned by Brood lords. We are taking here some
 * creative license and presume they would fetch JSON to the overlords too.
 */
public class Broodling extends AsyncTask<String, Integer, Boolean>
{
private static final String TAG = "Irekia.Broodling";

ArrayList<Content_item> new_items;
ArrayList<Integer> to_delete;
ArrayList<Section_state> new_sections;
Overlord overlord;

public Broodling(Overlord overlord)
{
	super();
	this.overlord = overlord;
}

/** Call the parent, in a UI safe way.
 */
protected void onProgressUpdate(Integer... progress)
{
	overlord.onBroodlingUpdate(progress[0]);
}

/** Downloads and processes the retrieved JSON.
 */
protected Boolean doInBackground(String... urls)
{
	assertTrue("This is meant to use one url", 1 == urls.length);

	try {
		publishProgress(0);
		if (overlord.items.countObservers() < 1)
			return false;

		//log.i("Downloading " + urls[0]);
		BetterHttpResponse response = BetterHttp.get(urls[0]).send();
		publishProgress(4000);
		JSON json = JSON.parse(response.getResponseBodyAsBytes());
		publishProgress(5000);
		if (null != json) {
			return process_json(json);
		} else {
			Log.w(TAG, "Couldn't parse json!");
		}
	} catch (ConnectException e) {
		e.printStackTrace();
		Log.w(TAG, "ConnectException error: " + e.getLocalizedMessage());
	} catch (IOException e) {
		e.printStackTrace();
		Log.w(TAG, "IOException error: " + e.getLocalizedMessage());
	}
	return Boolean.valueOf(false);
}

/** If new_items is valid, it is assigned to the overlord.
 */
protected void onPostExecute(Boolean result)
{
	overlord.replace(new_items, new_sections);
	publishProgress(10000);
}

/** Parses the downloaded json for items in the protocol.
 * This will use race_tag to find the correct entries.
 */
protected Boolean process_json(JSON json)
{
	ArrayList<Object> items = json.get_array(overlord.race_tag,
		null, HashMap.class);
	if (null == items || items.size() < 1) {
		//log.i("No items in result?");
		return Boolean.valueOf(false);
	}

	to_delete = json.get_array("to_delete", new ArrayList<Integer>(),
		Integer.class);

	new_sections = process_sections(
		json.get_array("sections", null, HashMap.class), overlord.sections);

	publishProgress(6000);
	// Convert the item JSON into objects.
	new_items = new ArrayList<Content_item>();
	for (Object item_stub : items) {
		try {
			new_items.add(overlord.race_item_class.getConstructor(JSON.class)
				.newInstance(JSON.map(item_stub)));
		} catch (IllegalArgumentException e) {
			Log.w(TAG, "Ignoring item 1");
		} catch (SecurityException e) {
			Log.w(TAG, "Ignoring item 2");
		} catch (InstantiationException e) {
			Log.w(TAG, "Ignoring item 3");
		} catch (IllegalAccessException e) {
			Log.w(TAG, "Ignoring item 4");
		} catch (InvocationTargetException e) {
			Log.w(TAG, "Ignoring item 5");
		} catch (NoSuchMethodException e) {
			Log.w(TAG, "Ignoring item 6");
		}
	}

	if (new_items.size() < 1) {
		new_items = null;
		return Boolean.valueOf(false);
	} else {
		publishProgress(7000);
		sort_and_purge();
		publishProgress(8000);
		associate_items_to_sections(new_sections, new_items);
		publishProgress(9000);
		return Boolean.valueOf(true);
	}
}

/** Goes through the new_items array sorting and purging elements.
 * - Sort items according to sort_id + id
 * - Remove items found in to_delete.
 * - Remove items out of the maximum overlord limit.
 * - Remove items past their expiration date.
 */
protected void sort_and_purge()
{
	// Remove items as requested by the server.
	for (Integer number : to_delete) {
		for (int f = 0; f < new_items.size(); f++) {
			if (number == new_items.get(f).id) {
				new_items.remove(f);
				break;
			}
		}
	}

	// Remove elements whose expiration date was reached.
	final int now = (int)(System.currentTimeMillis() / 1000);
	for (int f = 0; f < new_items.size(); f++) {
		Content_item item = new_items.get(f);
		if (item.expiration_date > 0 && item.expiration_date < now)
			new_items.remove(f--);
	}

	// Sort items by sorting identifier.
	Collections.sort(new_items, new Content_item_comparator());

	// Remove superfluous items according to maximum number of allowed items.
	if (new_items.size() > overlord.cache_size)
		new_items.subList(overlord.cache_size, new_items.size()).clear();
}

/** Processes the section list JSON.
 * This will convert the JSONs, filter duplicate entries, copy the state of the
 * sections currently in use. Any of the parameters can be null.
 *
 * @return The list of sections you have to assign to the overlord. Note that
 * these still have not filled the appropriate items, call
 * associate_items_to_sections() for that. This function will not return null
 * but an empty list.
 */
protected ArrayList<Section_state> process_sections(
		ArrayList<Object> section_stubs, ArrayList<Section_state> old_sections)
{
	ArrayList<Section_state> valid_sections = new ArrayList<Section_state>();
	if (null == section_stubs)
		return valid_sections;

	// Convert the sections without duplicates, use a hash set to check.
	HashSet<Integer> visited_sections = new HashSet<Integer>();
	for (Object section_stub : section_stubs) {
		try {
			Section_state s = new Section_state(JSON.map(section_stub));
			if (visited_sections.contains(s.id))
				continue;
			valid_sections.add(s);
			visited_sections.add(s.id);
		} catch (ParseException e) {
			Log.w(TAG, "Ignoring section 1 " + section_stub);
		}
	}

	// Try to copy the previous collapsed state in case something changes.
	for (Section_state valid_section : valid_sections) {
		Section_state previous = overlord.section_by_id(valid_section.id);
		if (null != previous)
			valid_section.collapsed = previous.collapsed;
	}

	if (valid_sections.size() > 0 && null != old_sections) {
		// Preserve old sections by copying them to the new ones.
		for (Section_state old_section : old_sections) {
			if (!(visited_sections.contains(old_section.id))) {
				valid_sections.add(new Section_state(old_section));
			}
		}
	}

	// Sort sections by identifier.
	Collections.sort(valid_sections, new Section_comparator());

	return valid_sections;
}

/** Goes through the list of items putting them into any of the sections.
 * Additionally this method will also purge any empty sections.
 */
protected void associate_items_to_sections(ArrayList<Section_state> sections,
		ArrayList<Content_item> items)
{
	assertTrue("Sections can't be null, only empty", null != sections);
	assertTrue("Items can't be null, only empty", null != items);

	if (sections.size() > 0 && items.size() > 0) {
		// First build a HashMap of the sections for easier finding.
		HashMap<Integer,Section_state> d = new HashMap<Integer,Section_state>();
		for (Section_state section : sections)
			d.put(section.id, section);

		for (Content_item item : items) {
			if (null == item.section_ids)
				continue;
			for (Integer section_id : item.section_ids) {
				Section_state dest = d.get(section_id);
				if (null != dest)
					dest.items.add(item);
			}
		}
	}

	// Purge empty sections.
	for (int f = 0; f < sections.size(); f++)
		if (sections.get(f).items.size() < 1)
			sections.remove(f--);
}

}
