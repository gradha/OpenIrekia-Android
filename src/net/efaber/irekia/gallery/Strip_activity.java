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

import net.efaber.irekia.I18n;
import net.efaber.irekia.I18n.Embedded_string;
import net.efaber.irekia.Irekia;
import net.efaber.irekia.Irekia_default_activity;
import net.efaber.irekia.Navigation_buttons;
import net.efaber.irekia.R;
import net.efaber.irekia.Sharekit;
import net.efaber.irekia.models.Content_item;
import net.efaber.irekia.models.Gallery_item;
import net.efaber.irekia.zerg.Overlord;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.Toast;

/** Shows a list of items.
 */
public class Strip_activity extends Irekia_default_activity
	implements OnItemClickListener, View.OnClickListener
{
/// Points to the object in charge of the items to show.
private Overlord overlord;
/// The adapter, reused from the Gallery_activity but using fullscreen mode.
private Gallery_adapter adapter;
/// The buttons used to navigate news. Might be hidden.
private Navigation_buttons buttons;
/// Points to the gallery, we need to interact with it frequently.
private Gallery gallery;

/// Points to the previous limit warning toast, to avoid spamming the user.
private Toast last_limit_toast;
/// Points to the previous description toast, to avoid spamming the user.
private Toast last_desc_toast;

public void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	setContentView(R.layout.strip);

	buttons = new Navigation_buttons(this, this);

	// Hide the buttons if the user enabled hardware key navigation.
	if (Irekia.volume_key_navigation) {
		LinearLayout button_layout =
			(LinearLayout)findViewById(R.id.button_layout);
		if (null != button_layout)
			button_layout.setVisibility(View.GONE);
	}

	gallery = (Gallery)findViewById(R.id.strip);

	Bundle extras = getIntent().getExtras();
	if (null != extras)
		overlord = Irekia.get_overlord(extras.getInt(Irekia.OVERLORD_ID_PARAM));

	if (null == overlord || null == overlord.parsed_data)
		return;

	Gallery_data gallery_data = (Gallery_data)overlord.parsed_data;
	adapter = new Gallery_adapter(this, gallery_data);
	adapter.fullscreen = true;
	adapter.set_items(overlord.items);

	gallery.setId(14);
	gallery.setAdapter(adapter);

	// Find the correct identifier and select it.
	final int id = extras.getInt(Irekia.CONTENT_ID_PARAM, -1);
	int f = 0;
	for (Content_item item : overlord.items) {
		if (id == item.id) {
			gallery.setSelection(f, false);
			break;
		}
		f++;
	}

	gallery.setOnItemClickListener(this);
	buttons.update(overlord, get_current_item());
}

/** Returns the currently viewed item.
 */
Gallery_item get_current_item()
{
	final int pos = gallery.getSelectedItemPosition();
	if (Gallery.INVALID_POSITION != pos && pos >= 0 &&
			pos < overlord.items.size())
		return (Gallery_item)overlord.items.get(pos);

	return null;
}

/** Override the key down and route it to web back navigation.
 * @return returns true if we did intercept the web view navigation otherwise
 * what super does.
 */
@Override
public boolean onKeyDown(final int keyCode, final KeyEvent event)
{
	final int direction = Irekia.handle_volume_keys(keyCode);
	if (0 != direction) {
		switch_item(direction);
		return true;
	}
	return super.onKeyDown(keyCode, event);
}

/** Handles next/prev button clicks to change the current item.
 */
public void onClick(View v)
{
	if (v == buttons.prev)
		switch_item(-1);
	else if (v == buttons.next)
		switch_item(+1);
}

/** Changes the current item to the previous or next one.
 * @param direction A positive or negative value, the function won't do
 * anything if it is zero. The direction will move the item forward or backward
 * through the overlord's list and animate a view flip.
 *
 * Even if no changes are done, this function calls buttons.update().
 */
private void switch_item(int direction)
{
	if (null != overlord && 0 != direction) {
		Content_item new_item = null;
		if (direction < 0)
			new_item = overlord.get_prev(get_current_item());
		else if (direction > 0)
			new_item = overlord.get_next(get_current_item());

		// Ok, if the next item is ok, flip to it.
		if (null != new_item && null != new_item.url) {
			gallery.setSelection(gallery.getSelectedItemPosition() + direction);
		} else if (0 != direction && Irekia.volume_key_navigation) {
			// Looks like we reached a limit, tell the user.
			if (null == last_limit_toast)
				last_limit_toast = Toast.makeText(this,
					I18n.e(Embedded_string.VOLUME_LIST_LIMIT),
					Toast.LENGTH_SHORT);

			if (null != last_limit_toast) {
				last_limit_toast.cancel();
				last_limit_toast.show();
			}
		}
	}
	buttons.update(overlord, get_current_item());

	if (null != last_desc_toast)
		last_desc_toast.cancel();
}

/** Show the description of the image if the user touches it.
 */
@Override
public void onItemClick(AdapterView<?> parent, View v, int position, long id)
{
	if (null != last_desc_toast)
		last_desc_toast.cancel();

	Gallery_item item = get_current_item();

	last_desc_toast = Toast.makeText(this, item.title, Toast.LENGTH_SHORT);
	last_desc_toast.show();
}

/********************************** Sharekit ********************************/

/** The user pressed the menu button, create the share menu.
 * @return returns always true, because we always intercept the action.
 */
@Override
public boolean onCreateOptionsMenu(Menu menu)
{
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.share_menu, menu);
	I18n.translate(menu);
	return true;
}

/** The user selected a menu option, what should we do?
 * @return returns always true, because we always intercept the action.
 */
@Override
public boolean onOptionsItemSelected(MenuItem menuitem)
{
	Content_item item = get_current_item();
	if (null != item)
		Sharekit.share_item(this, menuitem.getItemId(), item);
	return true;
}

}
