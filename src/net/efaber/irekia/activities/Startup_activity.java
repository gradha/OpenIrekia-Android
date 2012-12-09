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

import net.efaber.irekia.Beacon_downloader;
import net.efaber.irekia.I18n;
import net.efaber.irekia.I18n.Embedded_string;
import net.efaber.irekia.Irekia;
import net.efaber.irekia.R;
import android.os.Bundle;
import android.widget.TextView;

import com.github.droidfu.activities.BetterDefaultActivity;


/** Splash screen class.
 * In charge of checking the stored data, and if it is bad, retrieve new
 * version from the net, then let the real application run.
 */
public class Startup_activity extends BetterDefaultActivity
{
/// The parameter used for intent result communication.
public static final int STARTUP_REQUEST_CODE = 51235;

/** Called when the activity is first created. */
@Override
public void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	setContentView(R.layout.startup);

	if (!I18n.get_effective_language().equals(Irekia.last_lang_preference)) {
		//Log.I("Detected language configuration change, forcing cache purge.");
		Beacon_downloader.purge_cache();
	}

	TextView text = (TextView)findViewById(R.id.connecting_text);
	if (null != text)
		text.setText(I18n.e(Embedded_string.CONNECTING_TO_SERVER));
}

protected void onResume()
{
	super.onResume();

	if (isLaunching())
		Beacon_downloader.start(this, this);
}

}
