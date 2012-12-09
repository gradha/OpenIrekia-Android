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

import java.util.Observable;
import java.util.Observer;

import net.efaber.irekia.Activity_options;
import net.efaber.irekia.Irekia;
import net.efaber.irekia.Irekia_expandable_list_activity;
import net.efaber.irekia.R;
import net.efaber.irekia.Sharekit;
import net.efaber.irekia.activities.Tab_activity;
import net.efaber.irekia.models.Activity_progress;
import net.efaber.irekia.models.News_item;
import net.efaber.irekia.models.Section_state;
import net.efaber.irekia.zerg.Overlord;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;

/** Shows a list of items.
 */
public class News_activity extends Irekia_expandable_list_activity
	implements Observer, OnChildClickListener, Activity_progress
{
private Overlord overlord;

private Post_rotation r;

/// Stores the last known progress.
private int last_progress = 10000;

public void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	requestWindowFeature(Window.FEATURE_PROGRESS);

	setProgressBarIndeterminateVisibility(true);
	setProgressBarVisibility(true);

	// Recover rotation persistant variables.
	r = (Post_rotation)getLastNonConfigurationInstance();
	if (null == r)
		r = new Post_rotation();

	Bundle extras = getIntent().getExtras();
	if (null != extras)
		overlord = Irekia.get_overlord(extras.getInt(Irekia.OVERLORD_ID_PARAM));

	if (null == overlord || null == overlord.parsed_data)
		return;

	overlord.set_progress_reporter(this);

	//setListAdapter(new ArrayAdapter<Content_item>(this,
	//	android.R.layout.simple_list_item_1,
	//	android.R.id.text1, overlord.items));
	News_data news_data = (News_data)overlord.parsed_data;
	News_adapter adapter = new News_adapter(this, news_data);
	adapter.set(overlord);
	setListAdapter(adapter);

	// Set the empty view for the list when there are no items.
	ExpandableListView l = getExpandableListView();
	l.setId(android.R.id.list);
	l.setChildDivider(getResources().getDrawable(R.color.r_light_gray));
	l.setDivider(getResources().getDrawable(R.color.r_light_gray));
	l.setDividerHeight((int)(Irekia.density + 0.5f));
	l.setGroupIndicator(getResources().getDrawable(R.drawable.empty_expandable));
	l.setOnGroupCollapseListener(adapter);
	l.setOnGroupExpandListener(adapter);
	Irekia.set_empty_view(l, this);

	// Configure some looks for the listview.
	l.setBackgroundColor(news_data.back_normal_color);
	l.setDrawingCacheBackgroundColor(news_data.back_normal_color);
	l.setCacheColorHint(news_data.back_normal_color);

	l.setOnChildClickListener(this);
	registerForContextMenu(l);
}

public void onResume()
{
	super.onResume();
	setProgress(last_progress);

	if (null == overlord)
		return;

	overlord.items.addObserver(this);

	if (!r.did_fetch) {
		overlord.fetch_data();
		r.did_fetch = true;
	} else {
		overlord.fetch_if_ttl_expired();
	}

	if (r.last_fetch < overlord.last_update)
		update(null, null);

	update_collapsed_state(getExpandableListView(),
		(News_adapter)getExpandableListAdapter());
}

public void onPause()
{
	super.onPause();
	if (null == overlord)
		return;

	overlord.items.deleteObserver(this);
}

/** Pass some parameters to our future self after rotation.
 * We want to preserve some data like the r.last_fetch timestamp after a
 * rotation, so we communicate it here.
 */
@Override
public Object onRetainNonConfigurationInstance()
{
	return r;
}

/** Gets notifications about updates to the list of Overlord items.
 * The observable will actually be the overlord. The data is always null (?).
 */
@Override
public void update(Observable observable, Object data)
{
	News_adapter adapter = (News_adapter)getExpandableListAdapter();
	r.last_fetch = overlord.last_update;
	adapter.set(overlord);
	update_collapsed_state(getExpandableListView(), adapter);
}

/** Handles the user clicking or selecting an item with.
 * The item's url will be loaded in a new web activity.
 */
@Override
public boolean onChildClick(ExpandableListView parent, View view,
	int groupPosition, int childPosition, long id)
{
	News_item item = (News_item) parent.getExpandableListAdapter().getChild(
		groupPosition, childPosition);
	if (null != item) {
		item.start_activity(this, overlord);
		return true;
	} else {
		Toast.makeText(this, "Internal error processing item id!",
			Toast.LENGTH_SHORT).show();
		return false;
	}
}

/** Modifies the collapsed state of all groups in a listview.
 * The collapsed state comes out of the attribute for the Section_state objects.
 */
protected void update_collapsed_state(ExpandableListView list,
	News_adapter adapter)
{
	for (int f = 0; f < adapter.getGroupCount(); f++) {
		Section_state section = adapter.getGroup(f);
		if (null != section) {
			if (section.collapsed)
				list.collapseGroup(f);
			else
				list.expandGroup(f);
		} else {
			list.expandGroup(f);
		}
	}
}

/** Interface to let the Overlord notify us of download progresses.
 */
public void set_progress(int progress)
{
	last_progress = progress;
	if (this == ((Tab_activity)getParent()).getCurrentActivity())
		setProgress(progress);
}

/*********************** Common activity options menu ***********************/

/** The user pressed the menu button, create the menu.
 */
@Override
public boolean onCreateOptionsMenu(Menu menu)
{
	return Activity_options.onCreateOptionsMenu(this, menu, overlord);
}

/** The user selected a menu option, what should we do?
 */
@Override
public boolean onOptionsItemSelected(MenuItem item)
{
	return Activity_options.onOptionsItemSelected(this, item, overlord);
}

/** Method that gets called when the settings activity has finished.
 * Now we can pick up the list of changes and see if we need to refresh
 * ourselves.
 */
@Override
public void onActivityResult (int requestCode, int resultCode, Intent data)
{
	super.onActivityResult(requestCode, resultCode, data);
	Activity_options.onActivityResult(this, requestCode);
}

/********************************** Sharekit ********************************/

@Override
public void onCreateContextMenu(ContextMenu menu, View v,
	ContextMenuInfo menuInfo)
{
	super.onCreateContextMenu(menu, v, menuInfo);
	Sharekit.onCreateContextMenu(this, menu, v, menuInfo);
}

@Override
public boolean onContextItemSelected(MenuItem item)
{
	return Sharekit.onContextItemSelected(this, item, overlord);
}

/****************************************************************************/

/** Custom private class to preserve data between rotations.
 */
private class Post_rotation
{
public long last_fetch;
public boolean did_fetch;
}

}
