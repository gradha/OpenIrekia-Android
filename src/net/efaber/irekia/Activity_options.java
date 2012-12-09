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

package net.efaber.irekia;

import net.efaber.irekia.activities.About_activity;
import net.efaber.irekia.activities.Settings_activity;
import net.efaber.irekia.activities.Tab_activity;
import net.efaber.irekia.zerg.Overlord;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/** Contains the common code used in most activities to show global menus.
 */
public class Activity_options
{
private static final int SETTINGS_REQUEST_CODE = 11235;

/** The user pressed the menu button, creates the global common menu.
 * Pass whether we allow the * refresh option or hide it.
 * @return Always returns true.
 */
public static boolean onCreateOptionsMenu(Activity activity, Menu menu,
	Overlord overlord)
{
	MenuInflater inflater = activity.getMenuInflater();
	inflater.inflate(R.menu.activity_menu, menu);
	MenuItem refresh = menu.findItem(R.id.refresh);
	if (null != refresh)
		refresh.setVisible(overlord.allow_manual_reload);
	I18n.translate(menu);
	return true;
}

/** Handles the selection of a menu option, which usually opens an activity.
 * Pass the overlord to let the menu actions force a data refresh.
 * @return Always returns true.
 */
public static boolean onOptionsItemSelected(Activity activity, MenuItem item,
	Overlord overlord)
{
	Intent intent;
	switch (item.getItemId()) {
		case R.id.refresh:
			if (null != overlord)
				overlord.fetch_data();
			break;
		case R.id.about:
			intent = new Intent(activity, About_activity.class);
			activity.startActivity(intent);
			break;
		case R.id.settings:
			intent = new Intent(activity, Settings_activity.class);
			activity.startActivityForResult(intent, SETTINGS_REQUEST_CODE);
			break;
		default:
			Toast.makeText(activity, "Selected " + item.getItemId(),
				Toast.LENGTH_SHORT).show();
	}
	return true;
}

/** Method that gets called when the settings activity has finished.
 * Now we can pick up the list of changes and see if we need to refresh
 * ourselves.
 */
public static void onActivityResult (Activity activity, int requestCode)
{
	if (SETTINGS_REQUEST_CODE == requestCode) {
		if (Irekia.update_shared_preferences(activity)) {
			// Force clearing the overlords to make Tab_activity reload all.
			Irekia.overlords.clear();
			Tab_activity tab = (Tab_activity)activity.getParent();
			tab.restart_app();
		}
	}
}

}
