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

package net.efaber.irekia.activities;

import net.efaber.irekia.Irekia;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.github.droidfu.activities.BetterDefaultActivity;


/** Splash screen class.
 * In charge of checking the stored data, and if it is bad, retrieve new
 * version from the net, then let the real application run.
 */
public class Restarter_activity extends BetterDefaultActivity
	implements Runnable
{

/** Called when the activity is first created. */
@Override
public void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	View view = new View(this);
	setContentView(view);
	view.postDelayed(this, 500);
}

/** Method implementing the Runnable interface for the pending launch.
 * This simply kills ourselves while creating the Startup intent as if it was
 * being launched from scratch. It also clears up the purge setting.
 */
public void run()
{
	// Just to be sure
	Irekia.purge_cache = false;
	Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
	prefs.putBoolean(Irekia.PREFKEY_PURGE, false);
	prefs.commit();

	Intent intent = new Intent(this, Tab_activity.class);
	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	startActivity(intent);
	finish();
}

}
