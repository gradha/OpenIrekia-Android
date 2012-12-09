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

package net.efaber.irekia.gallery;

import static junit.framework.Assert.assertTrue;

import java.text.ParseException;

import net.efaber.irekia.Colors;
import net.efaber.irekia.JSON;

/** Simple data container class for Gallery_adapter.
 */
public class Gallery_data
{
/// Height in pixels of each row.
public int row_height;
/// Amount of padding for each row.
public int padding;
/// Normal color of the cells.
public int cell_normal_color;
/// Normal color of the cells when highlighted.
public int cell_highlight_color;
/// Number of cells per row (with a base width of 320 dip).
public int cells_per_row;
/// Specifies if images should stretch to fill or be cropped.
public boolean stretch_images;
/// Width and height of each gallery thumbnail, not including padding.
public int image_w, image_h;

/** Parses the attributes from an Overlord to see if everything is right.
 * @return If all the parsing went fine, returns an object with the static data
 * state required for the List_activity to work. Otherwise returns null.
 */
public Gallery_data(JSON data)
	throws ParseException
{
	assertTrue("Null param", null != data);

	padding = data.get_int("padding", 2);
	row_height = data.get_int("row_height", 79);
	cells_per_row = data.get_int("cells_per_row", 4);
	cell_normal_color = data.get_color("cell_normal_color", Colors.white);
	cell_highlight_color = data.get_color("cell_highlight_color", Colors.blue);
	stretch_images = data.get_bool("stretch_images", true);

	calculate_sizes();

	if (padding < 1)
		throw new ParseException("Padding has to be a positive integer", 0);
	if (row_height < 1)
		throw new ParseException("Row height has to be a positive integer", 1);
	if (padding * 2 >= row_height)
		throw new ParseException("Padding multiplied by 2 has to be " +
			"less than row height", 2);
	if (cells_per_row < 1)
		throw new ParseException("You need at least one cell per row", 3);
}

/** Calculates the sizes for the gallery based on input parameters.
 * This function recalculates the image_size variables based on other input
 * parameters like the cell padding or the row width/height. All other
 * attributes will remain constant.
 *
 * The calculations are performed always thinking of a base width of 320 dip
 * pixels. You have to call this shortly after initialisation, though you can
 * call this at any time at runtime if the input parameters change.
 *
 * @throws ParseException if there problems with the inputs, meaning you should
 * abort execution of the program, or at least prevent the controller from
 * being pushed.
 */
private void calculate_sizes()
	throws ParseException
{
	/* Calculate the size of the cells, always with a base idea of 320. */
	image_h = row_height - 2 * padding;
	image_w = (int)((320.0f - 2 * padding) /
		(float)cells_per_row - 2 * padding);

	final int row_width = 320;
	final int start_x = (row_width - (cells_per_row *
		(image_w + 2 * padding))) / 2;

	if (image_w < 1 || image_h < 1 || start_x < padding)
		throw new ParseException("Image size width or height less than 1, " +
			"or start less than padding", 10);
}

}
