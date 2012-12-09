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
import java.util.HashMap;

import net.efaber.irekia.JSON;

/** Implements the holder of section parameters.
 * Sections come along with News_item or Gallery_item objects. Usually you
 * won't be using one of these but a Section_state object.
 */
public class Section
{
/// Identifier of the item.
public int id;

/// Sorting identifier.
public long sort_id;

/// Says if the section is visible or not.
public boolean visible;

/// Name of the section, can't be null.
public String name;

/// Can the user collapse/expand the section=
public boolean interactive;

/// Default state of the section when it converts into a Section_state object.
public boolean starts_collapsed;

/// Should interaction collapse/expand other sections in the list?
public boolean autocollapse_others;;

/// Shows the number of children item in the title.
public boolean show_count;

/// Colors.
public int collapsed_text_color;
public int collapsed_back_color;
public int expanded_text_color;
public int expanded_back_color;


/** Creates a Section element from a JSON dictionary.
 */
public Section(JSON json) throws ParseException
{
	id = json.get_int("id", -1);
	sort_id = json.get_long("sort_id", id);
	name = json.get_string("name", "");
	visible = json.get_bool("visible", true);
	interactive = json.get_bool("interactive", true);
	starts_collapsed = json.get_bool("starts_collapsed", true);
	autocollapse_others = json.get_bool("autocollapse_others", true);
	show_count = json.get_bool("show_count", false);
	collapsed_text_color = json.get_color("collapsed_text_color", -1);
	collapsed_back_color = json.get_color("collapsed_back_color", -1);
	expanded_text_color = json.get_color("expanded_text_color", -1);
	expanded_back_color = json.get_color("expanded_back_color", -1);

	if (id < 1)
		throw new ParseException("Invalid section id", 0);
}

/** Creates a Section element from another.
 */
public Section(final Section other)
{
	id = other.id;
	sort_id = other.sort_id;
	name = other.name;
	visible = other.visible;
	interactive = other.interactive;
	starts_collapsed = other.starts_collapsed;
	autocollapse_others = other.autocollapse_others;
	show_count = other.show_count;
	collapsed_text_color = other.collapsed_text_color;
	collapsed_back_color = other.collapsed_back_color;
	expanded_text_color = other.expanded_text_color;
	expanded_back_color = other.expanded_back_color;
}

/** Generates the JSON like map ready to be serialized.
 */
public JSON get_json()
{
	HashMap<String, Object> json = new HashMap<String, Object>(18);
	json.put("id", id);
	json.put("sort_id", (long)sort_id);
	json.put("name", name);
	json.put("visible", visible);
	json.put("interactive", interactive);
	json.put("starts_collapsed", starts_collapsed);
	json.put("autocollapse_others", autocollapse_others);
	json.put("show_count", show_count);

	assertTrue("Not finished implementation yet!", false);
	return new JSON(json);
}

/** String that represents the object. Helpful for debugging.
 */
public String toString()
{
	return "Section {id:" + id + ", sort_id:" + sort_id + ", name:'" +
		name + "', ...}";
}

}
