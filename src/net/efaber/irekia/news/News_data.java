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

package net.efaber.irekia.news;

import static junit.framework.Assert.assertTrue;

import java.text.ParseException;

import net.efaber.irekia.Colors;
import net.efaber.irekia.JSON;
import android.graphics.Bitmap;

/** Simple data container class for News_activity.
 */
public class News_data
{
/// Height in pixels of each row.
public int row_height;
/// Amount of padding for each row.
public int padding;
/// True if the thumbnail image is right aligned.
public boolean image_right;
/// Thumbnail image sizes.
public int image_w, image_h;
/// Number of lines used in the cell for the title.
public int title_lines;
/// Font size for the cell in points.
public int title_size;
/// Color for the title text.
public int title_color;
/// Body font size in points.
public int text_size;
/// Color for the body text.
public int text_color;
/// Size of the footer font in points.
public int footer_size;
/// Color for the footer text.
public int footer_color;
/// Footer alignment, can be negative (left), zero (center) or positive (right).
public int footer_alignment;
/// Background color of the cells.
public int back_normal_color;
/// Background color of the cells when highlighted.
public int back_highlight_color;
/// Image used for item disclosure. Might be null.
public Bitmap item_disclosure;
/// True if the user can switch between sections.
public boolean navigation_changes_section;
/// Amount of padding applied to the left and right sides of a section's text.
public int section_title_padding;
public int section_collapsed_text_color;
public int section_expanded_text_color;
public int section_collapsed_back_color;
public int section_expanded_back_color;

/** Parses the attributes from an Overlord to see if everything is right.
 * @return If all the parsing went fine, returns an object with the static data
 * state required for the List_activity to work. Otherwise returns null.
 */
public News_data(JSON data)
	throws ParseException
{
	assertTrue("Null param", null != data);

	padding = data.get_int("padding", 3);
	row_height = data.get_int("row_height", 44);
	title_lines = Math.max(1, data.get_int("title_lines", 1));
	title_size = data.get_int("title_size", 16);
	text_size = data.get_int("text_size", 13);
	footer_size = data.get_int("footer_size", 11);
	footer_alignment = data.get_int("footer_alignment", 0);
	image_right = (1 == data.get_int("image_alignment", 0));
	final int image_size[] = data.get_size("image_size",
		30, row_height - padding * 2);
	image_w = image_size[0];
	image_h = image_size[1];
	title_color = data.get_color("title_color", Colors.blue);
	text_color = data.get_color("text_color", Colors.black);
	footer_color = data.get_color("footer_color", Colors.gray);
	back_normal_color = data.get_color("back_normal_color", Colors.white);
	back_highlight_color = data.get_color("back_highlight_color", Colors.blue);

	navigation_changes_section = data.get_bool("navigation_changes_section",
	  false);
	section_title_padding = data.get_int("section_title_padding", 10);

	section_expanded_text_color = data.get_color(
	  "section_expanded_text_color", Colors.white);
	section_expanded_back_color = data.get_color(
	  "section_expanded_back_color", Colors.expanded_back_color);
	section_collapsed_text_color = data.get_color(
	  "section_collapsed_text_color", Colors.black);
	section_collapsed_back_color = data.get_color(
		"section_collapsed_back_color", Colors.collapsed_back_color);

	// Make sure the disclosure image is set correctly. Use defaults.
	item_disclosure = data.get_image("item_disclosure", null);

	if (padding < 1)
		throw new ParseException("Padding has to be a positive integer", 0);
	if (row_height < 1)
		throw new ParseException("Row height has to be a positive integer", 1);
	if (title_size < 1)
		throw new ParseException("Title size has to be a positive integer", 2);
	if (text_size < 1)
		throw new ParseException("Text size has to be a positive integer", 3);
	if (footer_size < 1)
		throw new ParseException("Footer size has to be a positive integer", 4);
	if (section_title_padding < 1 || section_title_padding >= 320)
		throw new ParseException("Invalid section title padding size", 5);
}

}
