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

import net.efaber.irekia.JSON;

/** Implements the holder of section parameters.
 * Sections come along with News_item or Gallery_item objects. Usually you
 * won't be using one of these but a Section_state object.
 */
public class Section_state extends Section
{
/// State of the collapsation state.
public boolean collapsed;

/// List of child identifiers, always valid.
public ArrayList<Content_item> items;

/** Creates a Section element from a JSON dictionary.
 * @throws ParseException
 */
public Section_state(JSON json) throws ParseException
{
	super(json);
	collapsed = starts_collapsed;
	items = new ArrayList<Content_item>();
}

/** Clones a Section_state element from another, but without preserving items.
 */
public Section_state(final Section_state other)
{
	super(other);
	collapsed = starts_collapsed;
	items = new ArrayList<Content_item>();
}

/** String that represents the object. Helpful for debugging.
 */
public String toString()
{
	return "Section_state {collapsed: " + collapsed + ", " + super.toString() +
		", items: " + items + "}";
}

/** Returns the item at the specified numeric position.
 * @return Null if the index is out of bounds.
 */
public Content_item item_by_pos(final int position)
{
	assertTrue("No items?", null != items);
	if (position < 0 || position >= items.size())
		return null;
	return
		items.get(position);
}

}
