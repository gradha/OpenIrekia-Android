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

import java.text.ParseException;

import net.efaber.irekia.I18n;
import net.efaber.irekia.Irekia;
import net.efaber.irekia.JSON;
import net.efaber.irekia.activities.Web_activity;
import net.efaber.irekia.video.Video_facade_activity;
import net.efaber.irekia.zerg.Overlord;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class News_item extends Content_item
{
private static final String TAG = "Irekia.News_item";

/// String with the contents of the news, can't be null.
public String body;

/// Text for the footer, if available.
public String footer;


/** Creates a News_item element from a JSON dictionary.
 */
public News_item(JSON json) throws ParseException
{
	super(json);
	body = json.get_string("body", null);
	footer = json.get_string("footer", null);

	if (null == body)
		throw new ParseException("Null body for news item", 0);
}

/** Returns the controller class name for a news item.
 * See parent definition for more info.
 */
public String default_controller()
{
	return "Item_view_controller";
}

/** Generates the JSON like map ready to be serialized.
 */
public JSON get_json()
{
	JSON json = super.get_json();
	if (null == json)
		return null;

	json.data.put("body", body);
	if (null != footer) json.data.put("footer", footer);

	return json;
}

/** String that represents the object. Helpful for debugging.
 */
public String toString()
{
	final String micro_body = (null == body) ? null :
		(body.length() < 21 ? body :
			body.substring(0, Math.min(20, body.length())) + "...");
	return "News_item {body:'" + micro_body + "', " + super.toString() + "}";
}

/** Call this to open a Web_activity to read the item.
 */
public void start_activity(Context context, final Overlord overlord)
{
	Intent intent = null;
	if (class_type.equals(default_controller())) {
		intent = new Intent(context, Web_activity.class);
	} else if (null != data &&
			data.get_url("preview_url", "").length() > 0) {
		intent = new Intent(context, Video_facade_activity.class);
	}

	if (null != intent) {
		intent.putExtra(Irekia.OVERLORD_ID_PARAM, overlord.id);
		intent.putExtra(Irekia.CONTENT_ID_PARAM, id);
		intent.putExtra(Irekia.TITLE_PARAM, I18n.p(overlord.long_title_id));
		context.startActivity(intent);
	} else {
		Log.w(TAG, "Don't know how to deal with item (" + overlord.id +
			"." + id + ")");
	}
}

}
