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

package net.efaber.irekia.models;

import static junit.framework.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import net.efaber.irekia.JSON;
import net.efaber.irekia.zerg.Overlord;
import android.content.Context;

public class Content_item
	implements Sharing_tags
{
/// Identifier of the item.
public int id;

/// Unix timestamp.
public int expiration_date;

/// Sorting identifier.
public long sort_id;

/// Title of the object, can't be null.
public String title;

/// Retrieval URL of the data this item points to if available.
public String url;

/// Public URL for user consumption if available.
public String share_url;

/// URL of the image associated with this item if available.
public String image;

/// If yes, the content is never cached, always grabbed from the net.
public boolean online;

/// Type of the item, can't be null.
public String class_type;

/// Further data for the item if available.
public JSON data;

/// List of section identifiers this item is child of if available.
public ArrayList<Integer> section_ids;

/** Creates a Content_item element from a JSON dictionary.
 */
public Content_item(JSON json) throws ParseException
{
	id = json.get_int("id", -1);
	sort_id = json.get_long("sort_id", id);
	title = json.get_string("title", null);
	url = json.get_url("url", null);
	share_url = json.get_url("share_url", null);
	image = json.get_url("image", null);
	online = json.get_bool("online", false);
	class_type = json.get_string("class_type", null);
	data = json.get_map("data", null);
	section_ids = json.get_array("section_ids", null, Integer.class);
	expiration_date = json.get_int("expiration_date", -1);

	if (null == title || id < 1)
		throw new ParseException("Null title or invalid id", 0);

	// Verify that the type is one of the allowed types. */
	if (null == class_type) {
		class_type = default_controller();
	} else {
		boolean valid = default_controller().equals(class_type);

		if (!valid) {
			// TODO: Perform class instantiation from string name.
		}
	}
}

/** Returns the specific class name for the class.
 * Use this to let the Content_item class know the name of the specific
 * controller for the class inheriting from FLContent_item. This
 * should be the name of the default controller when the application
 * data protocol doesn't specify any.
 */
public String default_controller()
{
	return "No controller";
}

/** Generates the JSON like map ready to be serialized.
 */
public JSON get_json()
{
	HashMap<String, Object> json = new HashMap<String, Object>(18);
	json.put("id", id);
	json.put("sort_id", (long)sort_id);
	json.put("title", title);
	if (null != url) json.put("url", url);
	if (null != share_url) json.put("share_url", share_url);
	if (null != image) json.put("image", image);
	if (online) json.put("online", online);
	if (null != class_type) json.put("class_type", class_type);
	if (null != data) json.put("data", data);
	if (null != section_ids && section_ids.size() > 0)
		json.put("section_ids", section_ids);
	if (expiration_date > 0) json.put("expiration_date", expiration_date);

	return new JSON(json);
}

/** String that represents the object. Helpful for debugging.
 */
public String toString()
{
	return "Content {id:" + id + ", sort_id:" + sort_id + ", title:'" +
		title + "', url:'" + url + "', share_url:'" + share_url + "', image:'" +
		image + "', data:" + data + "}";
}

/** Call this to open an activity with the details of the item.
 * This method doesn't work at the Content_item level, it has to be overriden
 * by subclasses, which should not call this version (just asserts).
 */
public void start_activity(Context context, final Overlord overlord)
{
	assertTrue("Don't call this at Content_item level", false);
}

/**************** Sharing_tags interface implementation **********************/

/** Returns the title of the item. See @Sharing_tags for more info.
 */
public String get_title()
{
	if (null != title)
		return title;
	else
		return "";
}

/** Returns the photo description of the item. See @Sharing_tags for more info.
 * Since Content_item objects don't know anything about photos, this method
 * should be overriden by a child class.
 */
public String get_photo_desc()
{
	return "";
}

/** Returns the public url of the item. See @Sharing_tags for more info.
 */
public String get_url()
{
	if (null != share_url)
		return share_url;
	else
		return "";
}

}
