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
import net.efaber.irekia.models.Content_item;
import net.efaber.irekia.models.News_item;
import net.efaber.irekia.models.Section_state;
import net.efaber.irekia.zerg.Overlord;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

/** Custom adapter to force specification of custom News_row views.
 * The adapter actually wraps around Overlord objects providing enough
 * abstraction to deal with those having sections and those without.
 */
public class News_adapter extends BaseExpandableListAdapter
	implements OnGroupExpandListener, OnGroupCollapseListener
{
private final Context context;
private final News_data data;
private Object cached_data;
private Overlord overlord;

public News_adapter(Context context, News_data data)
{
	super();
	this.data = data;
	this.context = context;
}

/** Gets some data from the overlord for the list.
 * Later calls notifyDataSetChanged().
 */
public void set(Overlord overlord)
{
	assertTrue("Setting empty overlord?", null != overlord);
	assertTrue("Setting empty overlord items?", null != overlord.items);
	this.overlord = overlord;
	notifyDataSetChanged();
}

public Content_item getItem(int position)
{
	assertTrue("No overlord?", null != overlord);
	if (position < 0 || position >= overlord.items.size())
		return null;
	else
		return overlord.items.get(position);
}

@Override
public Content_item getChild(int groupPosition, int childPosition)
{
	Section_state section = overlord.section_by_pos(groupPosition);
	if (null != section)
		return section.item_by_pos(childPosition);
	else
		return getItem(childPosition);
}

@Override
public long getChildId(int groupPosition, int childPosition)
{
	Content_item item = getChild(groupPosition, childPosition);
	if (null != item)
		return item.id;
	else
		return 0;
}

@Override
public View getChildView(int groupPosition, int childPosition,
		boolean isLastChild, View convertView, ViewGroup parent)
{
	assertTrue("Missing adapter look data", null != data);
	if (null == convertView)
		convertView = new News_row(context, data);

	News_row row = (News_row)convertView;
	Object item = getChild(groupPosition, childPosition);
	if (null != item && News_item.class.isInstance(item)) {
		row.set((News_item)item);
		cached_data = row.refresh_cache(cached_data);
	}

	return convertView;
}

@Override
public View getGroupView(int groupPosition, boolean isExpanded,
		View convertView, ViewGroup parent)
{
	assertTrue("Missing adapter look data", null != data);
	if (null == convertView)
		convertView = new Section_row(context, data);

	Section_row row = (Section_row)convertView;
	Section_state section = getGroup(groupPosition);
	row.set(section);

	return convertView;
}

@Override
public int getChildrenCount(int groupPosition)
{
	Section_state section = overlord.section_by_pos(groupPosition);
	if (null != section)
		return section.items.size();
	else
		return overlord.items.size();
}

@Override
public Section_state getGroup(int groupPosition)
{
	assertTrue("No overlord?", null != overlord);
	return overlord.section_by_pos(groupPosition);
}

/** We have to provide at least a fake group if there are no sections.
 * But this method has to provide zero groups if there are no items at all,
 * since otherwise the empty view won't kick in.
 */
@Override
public int getGroupCount()
{
	assertTrue("No overlord?", null != overlord);
	if (overlord.items.size() < 1)
		return 0;
	else
		return Math.max(1, overlord.sections.size());
}

@Override
public long getGroupId(int groupPosition)
{
	if (groupPosition >= 0 && groupPosition < overlord.sections.size())
		return overlord.sections.get(groupPosition).id;
	return 0;
}

@Override
public boolean hasStableIds()
{
	return true;
}

@Override
public boolean isChildSelectable(int groupPosition, int childPosition)
{
	return true;
}

/********************************** Listeners ********************************/

public void onGroupCollapse(int groupPosition)
{
	Section_state section = getGroup(groupPosition);
	if (null != section)
		section.collapsed = true;
}

public void onGroupExpand(int groupPosition)
{
	Section_state section = getGroup(groupPosition);
	if (null != section)
		section.collapsed = false;
}

}
