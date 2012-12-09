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

import static junit.framework.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import net.efaber.irekia.I18n;
import net.efaber.irekia.Irekia;
import net.efaber.irekia.JSON;
import net.efaber.irekia.R;
import net.efaber.irekia.zerg.Overlord;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

/** Takes care of the full application.
 * This activity is in charge of creating and managing the active tabs. Usually
 * you will first call the static method to validate a JSON which creates the
 * tabs, to later create the true activity.
 */
public class Tab_activity extends TabActivity
	implements OnTabChangeListener
{
private static final String TAG = "Irekia.Tab_activity";

public static String PARAM_KEY = "Tab_input";
private ArrayList<Overlord> overlords;

/** Called when the activity is first created. */
@Override
public void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	requestWindowFeature(Window.FEATURE_PROGRESS);
	setContentView(R.layout.tab);

	setProgressBarIndeterminateVisibility(true);
	setProgressBarVisibility(true);

	TabHost tabHost = getTabHost();  // The activity TabHost
	Intent intent;
	TabHost.TabSpec spec;

	// Create a dummy tab to avoid performance issues during device rotation.
	// See http://stackoverflow.com/questions/6291697/why-does-the-first-tab-activity-in-android-live-forever
	// for the gory details.
	intent = new Intent().setClass(this, Dummy_activity.class);
	spec = tabHost.newTabSpec("dummy").setIndicator("dummy").setContent(intent);
	tabHost.addTab(spec);
	tabHost.getTabWidget().getChildAt(0).setVisibility(View.GONE);

	// Construct tabs from the overlords.
	overlords = Irekia.overlords;
	if (null == overlords || overlords.size() < 1)
		return;

	// Initialize a TabSpec for each tab and add it to the TabHost
	int num_tabs = 0;
	for (Overlord overlord : overlords) {
		String short_title = I18n.p(overlord.short_title_id);
		assertTrue("Can't get null string!", null != short_title);

		intent = new Intent().setClass(this, overlord.race_class);
		intent.putExtra(Irekia.OVERLORD_ID_PARAM, overlord.id);
		View tabview = create_tab_view(tabHost.getContext(), short_title,
			overlord.tab_image);
		spec = tabHost.newTabSpec(short_title).
			setIndicator(tabview).setContent(intent);
		tabHost.addTab(spec);
		num_tabs++;
	}

	// Don't let the hidden tab be the active one.
	SharedPreferences prefs =
		PreferenceManager.getDefaultSharedPreferences(this);
	final int last_tab = prefs.getInt(Irekia.PREFKEY_LAST_TAB, -1);
	if (last_tab >= 1 && last_tab <= num_tabs)
		tabHost.setCurrentTab(last_tab);
	else
		tabHost.setCurrentTab(1);

	// Detect further tab changes to be able to save them to the preferences.
	tabHost.setOnTabChangedListener(this);
}

/** Check if we have to die.
 */
@Override
public void onResume()
{
	super.onResume();

	final boolean lang_changed = !I18n.get_effective_language()
		.equals(Irekia.last_lang_preference);
	if (Irekia.needs_restarting || null == overlords ||
			overlords.size() < 1 || lang_changed) {
		if (null != Irekia.overlords)
			Irekia.overlords.clear();
		Intent intent = new Intent(this, Startup_activity.class);
		startActivityForResult(intent, Startup_activity.STARTUP_REQUEST_CODE);
	}
}

/** Method that gets called when the startup intent has finished.
 * We simply force a restart.
 */
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data)
{
	super.onActivityResult(requestCode, resultCode, data);
	if (Startup_activity.STARTUP_REQUEST_CODE == requestCode) {
		// Did the startup activity succeed in downloading stuff?
		if (null == Irekia.overlords || Irekia.overlords.size() < 1)
			finish();
		else
			restart_app();
	}
}

/** Creates the view for the tab interface.
 * Accepts the text for the tab and an optional bitmap image, which can be null.
 * @return Returns the view that can be used as the indicator of a
 * TabHost.TabSpec.
 */
private static View create_tab_view(final Context context, final String text,
	final Bitmap bitmap)
{
	View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
	TextView tv = (TextView) view.findViewById(R.id.tabsText);
	tv.setText(text);
	if (null != bitmap) {
		ImageView iv = (ImageView)view.findViewById(R.id.icon_image);
		if (null != iv)
			iv.setImageBitmap(bitmap);
	}
	return view;
}

/** Parses the tab json and creates the controllers.
 * @return list of tabs that can be used to create the tab activity, or null if
 * there were problems with the parsing.
 */
static public ArrayList<Overlord> parse_app_data(JSON json)
{
	ArrayList<Object> tabs = json.get_array("tabs", null, HashMap.class);

	if (null == tabs || tabs.size() < 1) {
		Log.w(TAG, "No tabs in appdata?");
		return null;
	}

	ArrayList<Overlord> valid_tabs = new ArrayList<Overlord>();

	for (Object tab_stub : tabs) {
		JSON tab_json = JSON.map(tab_stub);
		try {
			valid_tabs.add(new Overlord(tab_json));
		} catch (ParseException e) {
			Log.w(TAG, "Couldn't parse tab. " + e.getLocalizedMessage());
		}
	}

	return valid_tabs;
}

/** Force finishing this app while also launching the restart handler.
 */
public void restart_app()
{
	Intent intent = new Intent(this, Restarter_activity.class);
	startActivity(intent);
	finish();
}

/** The user changed the tab.
 * Record the new tab and save it to the preferences as the last used one.
 */
public void onTabChanged(String tabId)
{
	final int tab = getTabHost().getCurrentTab();
	Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
	prefs.putInt(Irekia.PREFKEY_LAST_TAB, tab);
	prefs.commit();

	// Check if somebody else requested a restert.
	if (Irekia.needs_restarting) {
		if (null != Irekia.overlords)
			Irekia.overlords.clear();
		restart_app();
	}
}

}
