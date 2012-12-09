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

import com.github.droidfu.activities.BetterPreferenceActivity;

/** Wrapper to keep track of activity and share code.
 */
public class Irekia_preference_activity extends BetterPreferenceActivity
{

/** Check if we have to die.
 * If we don't have to, increment the activity counter.
 */
@Override
public void onResume()
{
	super.onResume();

	if (Irekia.needs_restarting)
		finish();
	else
		Irekia.one_living(this);
}

/** Tells the global class we are dying.
 */
@Override
public void onPause()
{
	super.onPause();
	Irekia.one_dying();
}

}
