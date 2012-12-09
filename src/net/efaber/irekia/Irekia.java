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

import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.efaber.irekia.I18n.Embedded_string;
import net.efaber.irekia.zerg.Overlord;

import org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.github.droidfu.DroidFuApplication;
import com.github.droidfu.cachefu.HttpResponseCache;
import com.github.droidfu.http.BetterHttp;
import com.github.droidfu.imageloader.ImageLoader;

import org.acra.*;
import org.acra.annotation.*;

/** Global app class. The glue that holds everything together.
 */
@ReportsCrashes(formKey = "dHBuODdXVkUzMUd6Wk9QbjBiOHEtLUE6MQ")
public class Irekia extends DroidFuApplication
{
private static final String TAG = "Irekia";

/// Check this in all activities resume, tells if you have to die/restart.
public static boolean needs_restarting = false;

/// Reused JSON object mapper.
public static ObjectMapper mapper;

/// Horrible global passing of parameters between activities.
public static HashMap<String,Object> params;

/// All the active overlords.
public static ArrayList<Overlord> overlords;

/// Specific string name when passing the title parameter to another activity.
public static final String TITLE_PARAM = "TITLE_PARAM";
/// Name of the bundle key to pass the overlord identifier.
public static final String OVERLORD_ID_PARAM = "OVERLORD_ID_PARAM";
/// Name of the bundle key to pass the content identifier.
public static final String CONTENT_ID_PARAM = "CONTENT_ID_PARAM";

/// Pseudo constant, contains the amount of pixels the scrollbar takes space.
public static float SCROLLBAR_SPACE = 5;

/// Current device density.
public static float density = 1;

/// Current device font density.
public static float font_density = 1;

/// Current pixels per inch.
public static int density_dpi = 160;

/// Global app preference for the application language.
public static String lang_preference, last_lang_preference;

/// Global app preference for volume navigation vs screen touch navigation.
public static boolean volume_key_navigation;

/// Global app preference for wifi prefetching.
public static boolean prefetch_on_wifi;

/// Global app preference for offline cache purging.
public static boolean purge_cache;

/// Holds the directory of the cache directory.
public static String cache_dir = "";

/// Key used to store the last valid setting of the used language.
public static final String PREFKEY_LAST_LANG = "last_lang_preference";
/// Key for the hardware volume key navigation setting.
public static final String PREFKEY_VOLUME_NAVIGATION = "volume_key_navigation";
/// Key for the prefetch on wifi setting.
public static final String PREFKEY_PREFETCH = "prefetch_on_wifi";
/// Key for the language preference setting.
public static final String PREFKEY_LANGCODE = "lang_preference";
/// Key for the boolean user setting to force a cache purge.
public static final String PREFKEY_PURGE = "purge_cache";
/// Key for the integer user setting storing the last visited tab.
public static final String PREFKEY_LAST_TAB = "last_tab_preference";


/// Internal static caches for bitmaps, to avoid reloading from disk.
private static Bitmap loading_icon;
private static Bitmap scaled_loading_icon;
private static Bitmap disclosure_icon;

/// Holds the thread executor used to refresh the news.
private static ScheduledThreadPoolExecutor refresh_queue;

/// Counter to track amount of living activities and know if we are in use.
private static int active_activities = 0;


public void onCreate()
{
	ACRA.init(this);
	super.onCreate();

	mapper = new ObjectMapper();
	params = new HashMap<String,Object>();
	refresh_queue = new ScheduledThreadPoolExecutor(2);

	Context context = getApplicationContext();
	ImageLoader.initialize(context);
	try {
		cache_dir = context.getCacheDir().getCanonicalPath();
	} catch (IOException e) {
		e.printStackTrace();
		Log.w(TAG, "Couldn't get cache dir, presuming empty");
	}
	PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

	SharedPreferences prefs =
		PreferenceManager.getDefaultSharedPreferences(this);
	lang_preference = prefs.getString(PREFKEY_LANGCODE, "auto");
	last_lang_preference = prefs.getString(PREFKEY_LAST_LANG, "");
	volume_key_navigation = prefs.getBoolean(PREFKEY_VOLUME_NAVIGATION, false);
	prefetch_on_wifi = prefs.getBoolean(PREFKEY_PREFETCH, false);
	purge_cache = prefs.getBoolean(PREFKEY_PURGE, false);

	I18n.set_embedded_values(context, I18n.get_effective_language());

	BetterHttp.setupHttpClient();
	BetterHttp.enableResponseCache(10, 10, 1);
	BetterHttp.enableGZIPEncoding();

	SCROLLBAR_SPACE = ViewConfiguration.get(context)
		.getScaledScrollBarSize();

	density = getResources().getDisplayMetrics().density;
	font_density = getResources().getDisplayMetrics().scaledDensity;
	density_dpi = getResources().getDisplayMetrics().densityDpi;

	//activate_developer_mode();
}

public void onLowMemory()
{
	super.onLowMemory();
	Log.w(TAG, "onLowMemory!");
}

public void onConfigurationChanged(Configuration newConfig)
{
	super.onConfigurationChanged(newConfig);

	Context context = getApplicationContext();
	if (update_shared_preferences(context))
		needs_restarting = true;
}

public void onTerminate()
{
	super.onTerminate();
}

/** Retrieves a specific overlord.
 * @return the overlord or null if it could not be found.
 */
public static Overlord get_overlord(final int id)
{
	for (Overlord overlord : overlords)
		if (id == overlord.id)
			return overlord;

	return null;
}

/** Retrieves the icon used for loading pictures.
 * @return may return null if there is a really serious problem.
 */
public static Bitmap get_loading_icon(Resources res)
{
	if (null != loading_icon)
		return loading_icon;

	loading_icon = BitmapFactory.decodeResource(res, R.drawable.loading);
	assertTrue("No loading icon resource?", null != loading_icon);
	return loading_icon;
}

/** Retrieves a scaled version of the icon used for loading pictures.
 * @return may return null if there is a really serious problem.
 */
public static Bitmap get_loading_icon(Resources res, RectF target_size)
{
	final int w = (int)(target_size.right - target_size.left + 0.5f);
	final int h = (int)(target_size.bottom - target_size.top + 0.5f);
	if (null != scaled_loading_icon) {
		if (scaled_loading_icon.getWidth() == w &&
				scaled_loading_icon.getHeight() == h) {
			return scaled_loading_icon;
		}
	}

	Bitmap icon = get_loading_icon(res);
	if (icon.getWidth() == w && icon.getHeight() == h) {
		scaled_loading_icon = icon;
		return icon;
	}

	scaled_loading_icon = Bitmap.createScaledBitmap(icon, w, h, true);
	assertTrue("No scaled icon?", null != scaled_loading_icon);

	scaled_loading_icon.setDensity(Irekia.density_dpi);
	return scaled_loading_icon;
}

/** Retrieves the icon used for item disclosures.
 * @return may return null if there is a really serious problem.
 */
public static Bitmap get_disclosure_icon(Resources res)
{
	if (null != disclosure_icon)
		return disclosure_icon;

	disclosure_icon = BitmapFactory.decodeResource(res, R.drawable.disclosure);
	assertTrue("No disclosure icon resource?", null != disclosure_icon);
	return disclosure_icon;
}

/** Call when you think the preferences might have changed.
 * The function checks the status of the preferences and compares against the
 * previously known value. This avoids performing multiple purges or interface
 * changes while the user is tinkering with the settings. This function may
 * purge manually cached files.
 *
 * @return The function returns true if you are meant to restart the
 * application due to changes being too severe to handle at runtime.
 */
public static boolean update_shared_preferences(Context context)
{
	SharedPreferences prefs =
		PreferenceManager.getDefaultSharedPreferences(context);
	String new_lang_preference = prefs.getString("lang_preference", "auto");
	boolean new_volume_key_navigation =
		prefs.getBoolean("volume_key_navigation", false);
	boolean new_prefetch_on_wifi = prefs.getBoolean("prefetch_on_wifi", false);
	boolean new_purge_cache = prefs.getBoolean("purge_cache", false);

	if (new_volume_key_navigation != volume_key_navigation)
		volume_key_navigation = new_volume_key_navigation;

	if (new_prefetch_on_wifi != prefetch_on_wifi)
		prefetch_on_wifi = new_prefetch_on_wifi;

	if (new_purge_cache != purge_cache)
		purge_cache = new_purge_cache;

	if (!new_lang_preference.equals(lang_preference)) {
		lang_preference = new_lang_preference;
		purge_cache = true;
	}

	if (!I18n.get_effective_language().equals(I18n.current_langcode))
		purge_cache = true;

	if (purge_cache) {
		I18n.set_embedded_values(context, I18n.get_effective_language());
		HttpResponseCache cache = BetterHttp.getResponseCache();
		if (null != cache)
			cache.clear();
		ImageLoader.clearCache();
		Beacon_downloader.purge_cache();
	}

	return purge_cache;
}

/** Call to handle hardware volume keys.
 * The volume keys can be optionally used by the navigation. This method first
 * checks if the user has enabled the appropriate setting, and if so, handles
 * the detection of the volume keys.
 *
 * @return Returns the direction of the next navigation item, or zero if the
 * preference is disabled or the user did not press the volume keys.
 */
public static int handle_volume_keys(final int keyCode)
{
	if (!volume_key_navigation)
		return 0;

	if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
		return 1;
	else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
		return -1;
	else
		return 0;
}

/** Sets the empty view of a list or grid to the default Irekia shield.
 * The empty layout will be inflated from the XML and added to the adapter as
 * the empty view after changing the text title to a correctly translated
 * string.
 */
public static void set_empty_view(AdapterView<?> adapter, Activity activity)
{
	ViewGroup group = (ViewGroup)adapter.getParent();
	View empty = activity.getLayoutInflater().inflate(R.layout.empty,
		group, false);
	TextView text = (TextView)empty.findViewById(R.id.connecting_text);
	if (null != text)
		text.setText(I18n.e(Embedded_string.CONNECTING_TO_SERVER));
	group.addView(empty);
	adapter.setEmptyView(empty);
}

/** Queues a download/refresh for overlords.
 * Pass as first parameter the overlord itself, which conforms to the Runnable
 * interface, and how many seconds are to be elapsed before it is run. The task
 * will be queued as a one off shot.
 */
public static void queue_refresh(Runnable command, int delay_seconds)
{
	refresh_queue.schedule(command, delay_seconds, TimeUnit.SECONDS);
}

/** Method called by onResume to notify globally somebody is living.
 */
public static void one_living(Context context)
{
	active_activities++;
	//Log.i(TAG, "Living up, count " + active_activities);
	Beacon_downloader.update_if_needed(context);
}

/** Method called by onPause to notify globally somebody is dying.
 */
public static void one_dying()
{
	active_activities--;
	//Log.i(TAG, "Dying off, count " + active_activities);
}

/** Activates the developer mode.
 * Starts spewing lots of useless junk to the log about hypothetical
 * performance problems. The method will silently fail with a log on older API
 * levels.
 */
//private static void activate_developer_mode()
//{
//	if (android.os.Build.VERSION.SDK_INT >= 9) {
//		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//			.detectDiskReads().detectDiskWrites().detectNetwork()
//			.penaltyLog().build());
//	}
//
//	//StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//		//.detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
//		//.penaltyLog().penaltyDeath().build());
//}

}
