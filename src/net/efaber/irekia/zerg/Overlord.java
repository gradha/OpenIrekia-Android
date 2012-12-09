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

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;

import net.efaber.irekia.I18n;
import net.efaber.irekia.Irekia;
import net.efaber.irekia.JSON;
import net.efaber.irekia.gallery.Gallery_activity;
import net.efaber.irekia.gallery.Gallery_data;
import net.efaber.irekia.models.Activity_progress;
import net.efaber.irekia.models.Content_item;
import net.efaber.irekia.models.Gallery_item;
import net.efaber.irekia.models.News_item;
import net.efaber.irekia.models.Section_state;
import net.efaber.irekia.news.News_activity;
import net.efaber.irekia.news.News_data;
import smartlistadapter.SmartList;
import android.graphics.Bitmap;

public class Overlord
	implements Runnable
{
/// Private pointer to the activity showing progress.
private WeakReference<Activity_progress> progress_reporter;

/// Unique identifier of the overlord. Used to access the database and tabs.
public int id;

/// The feed's URL. Note that this is not used, but rather langcode_url.
String url;

/// And this is url + the current language code if necessary.
String langcode_url;

/// True if we allow manual reloads for the Overlord.
public boolean allow_manual_reload;
/// Time to live of the fetched data in seconds. If zero or less, there
/// will be made no network requests for updates.
int ttl;
/// Witness to detect if we are already in the global refresh queue.
private boolean queued_for_refreshes;

/// Time of the last network update in miliseconds. Stored to disk.
public long last_update;
/// If true, activating the controller always fetches from the network,
/// regardless of ttl and last_update.
boolean download_if_virgin;

/// Stores the tag to search for items in the parsed JSON.
String race_tag;
/// Stores the class used to construct new items.
@SuppressWarnings("rawtypes")
public Class race_class;
public Class<? extends Content_item> race_item_class;

/// Stores the items of the view. Always works, albeit empty.
public SmartList<Content_item> items;
/// Stores the sections of the view. Always works, albeit empty.
public ArrayList<Section_state> sections;
/// Maximum number of items to show in the view. Excedent is purged.
int cache_size;
/// Values automatically set when you modify the list of items.
int max_item_id, min_item_id;

/// Internal witness for remote network activity start.
boolean already_doing_network_fetches;

/// Tells us if the FLOverlord already loaded cached information.
boolean disk_loaded;

/// Ventral sacs hold the data JSON used to spawn new view controllers.
JSON ventral_sacs;
/// The ventral class holds the name of the class used to spawn views.
String ventral_class;

/** The following should not be part of the overlord but of the TabSpec. */

/// Long title of the tab. This should not be a part of the overlod.
public int long_title_id;
/// Short title of the tab. This should not be a part of the overlod.
public int short_title_id;
/// Bitmap containing the base64 of the tab image. Null if there is none.
public Bitmap tab_image;

/// Processed json data contained in the ventral_sacs. Null if there is none.
public Object parsed_data;

/// Remembers if this is downloading something or not.
private boolean is_fetching_data;


public Overlord(JSON json)
	throws ParseException
{
	JSON data = json.get_map("data", null);
	if (null == data)
		throw new ParseException("Overlord data can't be null", 0);

	String class_type = json.get_string("class_type", "");
	if (class_type.length() < 1)
		throw new ParseException("Can't create overlord without class type. " +
			json, 1);

	race_class = News_activity.class;
	race_item_class = News_item.class;
	if (class_type.equals("Gallery_view_controller")) {
		race_class = Gallery_activity.class;
		race_item_class = Gallery_item.class;
		race_tag = "thumbs";
	} else if (class_type.equals("News_view_controller")) {
		race_tag = "items";
	}

	id = json.get_int("unique_id", 0);
	if (id < 1)
		throw new ParseException("Can't create overlord with unique_id < 1" +
			json, 2);

	url = data.get_url("main_url", null);
	if (null == url)
		throw new ParseException("Can't create overlord with null main_url", 3);
	langcode_url = url + I18n.current_langcode;
	allow_manual_reload = data.get_bool("allow_manual_reload", false);
	ttl = data.get_int("ttl", 300);
	if (ttl < 1)
		throw new ParseException("ttl has to be a positive value", 4);
	download_if_virgin = data.get_bool("download_if_virgin", false);
	cache_size = data.get_int("cache_size", 50);
	if (cache_size < 1)
		throw new ParseException("cache_size has to be a positive value", 5);
	long_title_id = json.get_int("long_title", -1);
	short_title_id = json.get_int("short_title", -1);
	tab_image = json.get_image("tab_image", null);

	// TODO: Make this generic, possibly from class type.
	if (Gallery_activity.class == race_class)
		parsed_data = new Gallery_data(data);
	else
		parsed_data = new News_data(data);

	ventral_sacs = data;
	ventral_class = class_type;
	items = new SmartList<Content_item>();
	sections = new ArrayList<Section_state>();
}

/** Starts a network update in a separate thread.
 * If you want to be notified of updates, just add yourself as observer to the
 * smartlist.
 */
public void fetch_data()
{
	synchronized (this) {
		if (is_fetching_data)
			return;

		is_fetching_data = true;
		if (!queued_for_refreshes && ttl > 0) {
			Irekia.queue_refresh(this, ttl);
			queued_for_refreshes = true;
		}
	}

	Broodling broodling = new Broodling(this);
	broodling.execute(langcode_url);
}

/** Checks if fetching is required due to ttl and last_update timestamps.
 * Since overlords don't actually download scheduled refreshes if they are not
 * being observed, this method has to be run by the activities willing to watch
 * the overlords' items on their resume method.
 * The method will calculate against the current time how much has ellapsed and
 * if enough, start immediately a fetch.
 */
public void fetch_if_ttl_expired()
{
	if (last_update + ttl * 1000 < System.currentTimeMillis())
		fetch_data();
}

/** Runnable implementation, starts a fetch and requeues itself.
 */
public void run()
{
	fetch_data();
	Irekia.queue_refresh(this, ttl);
}

/** Retrieves a specific item according to id.
 * @return The item or null if it wasn't found.
 */
public Content_item get_item(final int id)
{
	for (Content_item item : items)
		if (id == item.id)
			return item;
	return null;
}

/** Returns the previous item from the specified one.
 * @param ref The parameter you want to find it's previous, can be null.
 * @return The previous item or null if it wasn't found.
 */
public Content_item get_prev(Content_item ref)
{
	if (null == ref || null == items || items.size() < 2)
		return null;

	Content_item prev = null;
	for (Content_item item : items) {
		if (item.id == ref.id)
			return prev;
		prev = item;
	}
	return null;
}


/** Returns the next item from the specified one.
 * @param ref The parameter you want to find it's next, can be null.
 * @return The next item or null if it wasn't found.
 */
public Content_item get_next(Content_item ref)
{
	if (null == ref || null == items || items.size() < 2)
		return null;

	int f = items.size() - 1;
	Content_item next = items.get(f--);
	while (f >= 0) {
		Content_item item = items.get(f--);
		if (item.id == ref.id)
			return next;
		next = item;
	}
	return null;
}

/** Public entry for Broodling objects to replace the overlord's private vars.
 * Even if you pass null objects this function will reset the is_fetching_data
 * internal variable to false.
 */
public void replace(ArrayList<Content_item> new_items,
	ArrayList<Section_state> new_sections)
{
	synchronized (this) {
		is_fetching_data = false;
		if (null != new_items) {
			assertTrue("Sections can't be null", null != new_sections);
			this.last_update = System.currentTimeMillis();
			this.sections = new_sections;
			this.items.replace(new_items);
		}
	}
}

/** Allows activities to set themselves as reporters of progress.
 */
public void set_progress_reporter(Activity_progress reporter)
{
	if (null != reporter)
		progress_reporter = new WeakReference<Activity_progress>(reporter);
	else
		progress_reporter = null;
}

/** Relayed progress message of the Broodling.
 * This method is supposed to communicate the progress to whatever observers
 * are watching the Overlord. The method is run in the UI thread.
 */
public void onBroodlingUpdate(int progress)
{
	Activity_progress reporter = null;
	if (null != progress_reporter)
		reporter = progress_reporter.get();
	if (null != reporter)
		reporter.set_progress(progress);
}

/*************************** Section specific code *************************/

/** Find an active section by section identifier.
 * @return The section if found, null if it wasn't.
 */
public Section_state section_by_id(final int id)
{
	if (null != sections)
		for (Section_state section : sections)
			if (id == section.id)
				return section;
	return null;
}

/** Finds a section position by index.
 * You can pass any kind of index, this won't throw errors.
 * @return The section if found, null if it wasn't.
 */
public Section_state section_by_pos(final int pos)
{
	if (null != sections && pos >= 0 && pos < sections.size())
		return sections.get(pos);
	else
		return null;
}

}
