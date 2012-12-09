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

import net.efaber.irekia.I18n;
import net.efaber.irekia.I18n.Embedded_string;
import net.efaber.irekia.Irekia_preference_activity;
import net.efaber.irekia.R;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.Log;


/** Splash screen class.
 * In charge of checking the stored data, and if it is bad, retrieve new
 * version from the net, then let the real application run.
 */
public class Settings_activity extends Irekia_preference_activity
{
private static final String TAG = "Irekia.Settings_activity";

@Override
protected void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.preferences);

	translate_preference("pref_section_ui");
	translate_preference("lang_preference");
	translate_preference_dialog("lang_preference");
	translate_preference("volume_key_navigation");
	translate_preference("pref_section_data");
	//translate_preference("prefetch_on_wifi");
	translate_preference("purge_cache");
	translate_preference("acra.enable");

	setTitle(I18n.e(Integer.decode(getTitle().toString())));
}

/** Translates the text of a preference widget.
 * Pass the key of the preference. The method will find it, ask for its
 * title/summary and if they are numeric, translate them dynamically to one of
 * the available embedded strings.
 */
private void translate_preference(final String widget_key)
{
	Preference widget = findPreference(widget_key);
	if (null == widget) {
		Log.w(TAG, "Couldn't find preference for key " + widget_key);
		return;
	}

	final CharSequence old_title = widget.getTitle();
	try {
		if (null != old_title)
			widget.setTitle(I18n.e(Integer.decode(old_title.toString())));
	} catch (NumberFormatException e) {
		Log.w(TAG, "Couldn't find key number for '" + old_title + "'");
	}

	final CharSequence old_summary = widget.getSummary();
	try {
		if (null != old_summary)
			widget.setSummary(I18n.e(Integer.decode(old_summary.toString())));
	} catch (NumberFormatException e) {
		Log.w(TAG, "Couldn't find key number for '" + old_summary + "'");
	}

	if (CheckBoxPreference.class.isInstance(widget)) {
		CheckBoxPreference checkbox = (CheckBoxPreference)widget;
		final CharSequence old_on = checkbox.getSummaryOn();
		try {
			if (null != old_on)
				checkbox.setSummaryOn(
					I18n.e(Integer.decode(old_on.toString())));
		} catch (NumberFormatException e) {
			Log.w(TAG, "Couldn't find key number for '" + old_on + "'");
		}

		final CharSequence old_off = checkbox.getSummaryOff();
		try {
			if (null != old_off)
				checkbox.setSummaryOff(
					I18n.e(Integer.decode(old_off.toString())));
		} catch (NumberFormatException e) {
			Log.w(TAG, "Couldn't find key number for '" + old_off + "'");
		}
	}
}

/** Translates the text of a preference dialog widget.
 * Pass the key of the preference. Unlike translate_preference(), this requires
 * the preference to be of type DialogPreference. The function changes the
 * popup dialog title.
 */
private void translate_preference_dialog(final String widget_key)
{
	DialogPreference widget = (DialogPreference)findPreference(widget_key);
	if (null == widget) {
		Log.w(TAG, "Couldn't find dialog preference for key " + widget_key);
		return;
	}

	final CharSequence old_title = widget.getDialogTitle();
	try {
		if (null != old_title)
			widget.setDialogTitle(I18n.e(Integer.decode(old_title.toString())));
		widget.setNegativeButtonText(I18n.e(Embedded_string.CLOSE));
	} catch (NumberFormatException e) {
		Log.w(TAG, "Couldn't find key number for '" + old_title + "'");
	}
}

}
