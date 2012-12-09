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

import net.efaber.irekia.Irekia;
import net.efaber.irekia.JSON;
import net.efaber.irekia.gallery.Strip_activity;
import net.efaber.irekia.zerg.Overlord;
import android.content.Context;
import android.content.Intent;

public class Gallery_item extends Content_item
{
/// Width and height of the image being pointed by the item.
public int width, height;

/// The maximum amount of zoom allowed for the item.
public float max_zoom;

/// Text for the footer, always available, may be zero length.
public String caption_text;


/** Creates a Gallery_item element from a JSON dictionary.
 */
public Gallery_item(JSON json) throws ParseException
{
	super(json);
	width = json.get_int("width", -1);
	height = json.get_int("height", -1);
	max_zoom = Math.max(1, json.get_float("max_zoom", 2));
	caption_text = json.get_string("caption_text", "");

	if (null == image)
		throw new ParseException("Gallery item requires image", 0);

	if (width < 1)
		throw new ParseException("Gallery item requires width > 0", 1);

	if (height < 1)
		throw new ParseException("Gallery item requires height > 0", 2);
}

/** Returns the controller class name for a news item.
 * See parent definition for more info.
 */
public String default_controller()
{
	return "Photo_view_controller";
}

/** Generates the JSON like map ready to be serialized.
 */
public JSON get_json()
{
	JSON json = super.get_json();
	if (null == json)
		return null;

	json.data.put("width", width);
	json.data.put("height", height);
	json.data.put("max_zoom", max_zoom);
	if (caption_text.length() > 0)
		json.data.put("caption_text", caption_text);
	return json;
}

/** String that represents the object. Helpful for debugging.
 */
public String toString()
{
	final String micro_caption = (caption_text.length() < 21 ? caption_text :
		caption_text.substring(0, Math.min(20, caption_text.length())) + "...");
	return "Gallery_item {" + width + "x" + height + " + caption_text:'" +
		micro_caption + "', " + super.toString() + "}";
}

/** Call this to open the strip browser with the specified photo.
 */
public void start_activity(Context context, final Overlord overlord)
{
	Intent intent = new Intent(context, Strip_activity.class);
	intent.putExtra(Irekia.OVERLORD_ID_PARAM, overlord.id);
	intent.putExtra(Irekia.CONTENT_ID_PARAM, id);
	context.startActivity(intent);
}

/**************** Sharing_tags interface implementation **********************/

/** Returns the photo description of the item. See @Sharing_tags for more info.
 * Since Content_item objects don't know anything about photos, this method
 * should be overriden by a child class.
 */
public String get_photo_desc()
{
	assertTrue("Bad object construction?", null != caption_text);
	return caption_text;
}

}
