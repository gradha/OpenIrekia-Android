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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.efaber.irekia.activities.Tab_activity;
import net.efaber.irekia.zerg.Overlord;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.droidfu.activities.BetterDefaultActivity;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.http.BetterHttp;
import com.github.droidfu.http.BetterHttpResponse;


/** Class in charge of downloading and parsing server files.
 * The class goes through several states, first it downloads the beacon
 * URL, then retrieves the proper application JSON.
 */
public class Beacon_downloader extends BetterAsyncTask<String, Integer, Boolean>
{
private static final String TAG = "Irekia.Beacon_downloader";

/// Address of the beacon URL, hardcoded.
private static String BEACON_URL =
	"http://www.irekia.euskadi.net/mob_app/2";

/// Name of the file storing the beacon.
private static String FILENAME_BEACON = "beacon.json";

/// Name of the file storing the appdata.
private static String FILENAME_APPDATA = "appdata.json";

/// Key to read/write the last beacon's v value of the json.
private static String KEY_BEACON_V = "last_beacon_v";

/// Key to read/write the beacon time to live of the json.
private static String KEY_BEACON_TTL = "last_beacon_ttl";

/// Stores the maximum last known modification time of files, any of them.
private static long last_modified;

/// Zero, or last known time to live of the beacon. Stored in seconds.
private static long last_ttl;

/// Number of active Beacon_downloader objects.
private static int instances_running;


/** Called only from Startup_activity.
 * This will load the beacons from disk or from the network.
 */
public static void start(Context context, BetterDefaultActivity activity)
{
	instances_running = 0;
	Beacon_downloader task = new Beacon_downloader(context, activity);
	task.execute(BEACON_URL);
}

/** Called periodically by the usage of the activities.
 * This will check the last known time to live of the files, compare it to the
 * current system time, and if needed, start a new download.
 */
public static void update_if_needed(Context context)
{
	if (last_modified < 1)
		return;

	if (instances_running > 0) {
		//Log.w(TAG, "can't update, downloads in progress...");
		return;
	}

	//Log.i(TAG, "last " + last_modified + " current " + System.currentTimeMillis());
	if (Math.abs(last_modified - System.currentTimeMillis()) >
			last_ttl * 1000) {
		//Log.w(TAG, "Ah, should be redownloading stuff");
		Beacon_downloader task = new Beacon_downloader(context, null);
		task.execute(BEACON_URL);
	}
}

/** Forces the deletion of the locally saved JSON files.
 * You can call from anywhere any time, but you will need to restart the
 * application to actually force re-downloading the files. If something fails
 * during deletion, it will be ignored silently.
 */
public static void purge_cache()
{
	final String[] filenames = { FILENAME_BEACON, FILENAME_APPDATA };
	for (final String filename : filenames)
		new File(Irekia.cache_dir + "/" + filename).delete();
}

/****************************************************************************/

/// Counts the current progress. Really zero or once since we only have to
/// perform two sequential downloads.
int state;

/// Stores the next URL to be downloaded once we finish with the current one.
String next_url;

/// Pointer to the activity to show error dialogs. Null for updates.
private BetterDefaultActivity activity;


/** Pass the activity launching the startup.
 * If null, that means we are updating, so we change the behaviour.
 */
public Beacon_downloader(Context context, BetterDefaultActivity activity)
{
	super(context);
	instances_running++;
	this.activity = activity;
	disableDialog();
}

/** Checks the state of connection and constructs the parsed JSON.
 * This function delegate sin one of the process_beacon or
 * process_appdata methods, which really do the hard work.
 */
protected Boolean doCheckedInBackground(final Context context, String... urls)
{
	assertTrue("This is meant to use one url", 1 == urls.length);

	try {
		final String filename = (0 == state) ?
			FILENAME_BEACON : FILENAME_APPDATA;

		boolean ret = false;
		// Don't try to load from this if we are performing an update.
		if (null != activity) {
			if (0 == state)
				ret = process_beacon(context,
					load_json(context, filename, false));
			else
				ret = process_appdata(context,
					load_json(context, filename, true));

			// Return already if we managed to load from the cache.
			if (ret)
				return true;
		}

		// Ok, we have to load the stuff from the server.
		BetterHttpResponse response = BetterHttp.get(urls[0]).send();
		final String response_body = response.getResponseBodyAsString();
		JSON json = JSON.parse(response_body);
		if (null == json) {
			Log.w(TAG, "Couldn't parse json in step " + state + ", aborting!");
			return false;
		}

		// Are we doing updates? If so, compare JSONs before doing stuff.
		if (null == activity) {
			//Log.i(TAG, "Checking json equality");
			final boolean equal = are_json_equal(context,
				filename, response_body);

			if (equal) {
				/*
				if (0 == state)
					Log.i(TAG, "Beacons equal, not checking further");
				else
					Log.i(TAG, "Got appdata, but is equal.");
				*/
				update_timestamps(context);
				return false;
			}
			//Log.i(TAG, "Proceeding with normal update code, state " + state);
		}

		// Do logic as this was the first download, process and save.
		if (0 == state) {
			ret = process_beacon(context, json);
			if (ret)
				save_json(context, response_body, FILENAME_BEACON, false);
		} else {
			ret = process_appdata(context, json);
			if (ret)
				save_json(context, response_body, FILENAME_APPDATA, true);
		}

		if (null == activity && 0 != state) {
			Log.i(TAG, "No activity, second state, setting restart flag");
			Irekia.needs_restarting = true;
		}

		return ret;
	} catch (ConnectException e) {
		e.printStackTrace();
		Log.w(TAG, "ConnectException error: " + e.getLocalizedMessage());
	} catch (IOException e) {
		e.printStackTrace();
		Log.w(TAG, "IOException error: " + e.getLocalizedMessage());
	}
	return false;
}

/** Check the status of the operation.
 * If it was ok, depending on the state of the class we might pass to
 * another state or finish. If there were problems, allow the user to
 * retry.
 */
protected void after(final Context context, Boolean result)
{
	if (instances_running > 0)
		instances_running--;

	if (result) {
		if (0 == state) {
			Beacon_downloader task = new Beacon_downloader(context, activity);
			task.state = 1;
			task.execute(next_url);
		} else {
			if (null != activity)
				activity.finish();
		}
	} else if (null != activity) {
		// Exhausted retries, ask the user to keep trying.
		final String langcode = I18n.get_effective_language();
		int title_id = R.string.download_problems;
		int message_id = R.string.download_problems_retry;

		if (langcode.equals("es")) {
			title_id = R.string.download_problems_es;
			message_id = R.string.download_problems_retry_es;
		} else if (langcode.equals("eu")) {
			title_id = R.string.download_problems_eu;
			message_id = R.string.download_problems_retry_eu;
		}

		activity.newYesNoDialog(title_id, message_id, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which)
				{
					if (which == DialogInterface.BUTTON_NEGATIVE) {
						activity.finish();
					} else {
						Beacon_downloader task =
							new Beacon_downloader(context, activity);
						task.execute(BEACON_URL);
					}
				}}).show();
	}
}

/** Handles errors, in a not very graceful way.
 * Maybe this should raise a dialog for the user?
 */
protected void handleError(Context context, Exception error)
{
	instances_running--;
	Log.w(TAG, "Error trying to download: " + error.getLocalizedMessage());
	if (null != activity)
		activity.finish();
}

/** Handles processing the beacon JSON.
 * The method extracts the appdata URL from the json and stores it in the
 * next_url variable.
 * @return Returns true if the parsing was done correctly and next_url can be
 * used, false otherwise.
 */
protected Boolean process_beacon(Context context, JSON json)
{
	if (null == json)
		return false;

	// Check the version.
	final int new_v = json.get_int("v", -1);
	if (new_v < 0)
		return false;

	final int new_ttl = json.get_int("ttl", -1);
	if (new_ttl < 1)
		return false;

	next_url = json.get_url("url", null);

	if (null != next_url) {
		Editor prefs =
			PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefs.putInt(KEY_BEACON_V, new_v);
		prefs.putInt(KEY_BEACON_TTL, new_ttl);
		last_ttl = new_ttl;
		prefs.commit();
		return true;
	} else {
		return false;
	}
}

/** Handles processing the app data JSON.
 * @return Returns true if the parsing successfully launched the Tab activity.
 */
protected Boolean process_appdata(Context context, JSON json)
{
	if (null == json)
		return false;

	Map<Integer,String> values = I18n.parse_json_langs(
		json.get_array("langs", null, HashMap.class));
	if (null == values)
		return false;

	I18n.set_values(context, values);

	ArrayList<Overlord> overlords = Tab_activity.parse_app_data(json);
	if (null == overlords || overlords.size() < 1)
		return false;

	Irekia.overlords = overlords;

	Irekia.needs_restarting = false;
	return true;
}

/** Opens a filename from the cache using just the basename.
 * \return Returns the FileInputStream or null if there was a problem.
 */
protected FileInputStream open_file(final Context context,
	final String filename)
{
	final String name = context.getCacheDir() + "/" + filename;
	try {
		return new FileInputStream(name);
	} catch (FileNotFoundException e) {
		//Log.w(TAG, "Can't load " + name + ".\n" + e.getLocalizedMessage());
		//e.printStackTrace();
		return null;
	}
}

/** Tries to load a JSON from disk.
 * Returns null if the file did not exist. The last parameter tells the
 * function to update or not the global last_modified variable. Usually you
 * should use this only against the protocol file and not the beacon one.
 */
protected JSON load_json(final Context context, final String filename,
	final boolean update_timestamp)
{
	final String name = context.getCacheDir() + "/" + filename;
	FileInputStream input = open_file(context, filename);
	if (null == input)
		return null;

	JSON json = null;
	try {
		json = new JSON(input);
		if (update_timestamp)
			last_modified = Math.max(last_modified,
				new File(name).lastModified());
	} catch (JsonGenerationException e) {
		Log.w(TAG, "Can't load " + name + ".\n" + e.getLocalizedMessage());
		e.printStackTrace();
	} catch (JsonMappingException e) {
		Log.w(TAG, "Can't load " + name + ".\n" + e.getLocalizedMessage());
		e.printStackTrace();
	} catch (IOException e) {
		Log.w(TAG, "Can't load " + name + ".\n" + e.getLocalizedMessage());
		e.printStackTrace();
	} finally {
		if (input != null) try {
			input.close();
		} catch (IOException e) {
			Log.w(TAG, "Can't close " + name + ".\n" + e.getLocalizedMessage());
		}
	}
	return json;
}

/** Compares the json, one coming from a filename, the other from the bytes.
 * Returns true if the json are equal.
 */
protected boolean are_json_equal(final Context context, final String filename,
	final String json_string)
{
	//final String name = context.getCacheDir() + "/" + filename;
	FileInputStream input = open_file(context, filename);
	if (null == input)
		return false;

	try {
		JsonNode tree1 = Irekia.mapper.readTree(input);
		JsonNode tree2 = Irekia.mapper.readTree(json_string);
		boolean ret = tree1.equals(tree2);
		//Log.w(TAG, "for " + filename + " tree 1 equals 2? " + ret);
		return ret;

	} catch (JsonProcessingException e) {
		Log.w(TAG, "Can't process jsons.\n" + e.getLocalizedMessage());
		e.printStackTrace();
	} catch (IOException e) {
		Log.w(TAG, "Can't compare jsons " + filename + ".\n" +
			e.getLocalizedMessage());
		e.printStackTrace();
	} finally {
		try {
			input.close();
		} catch (IOException e) {
			Log.w(TAG, "Can't close " + filename + ".\n" +
				e.getLocalizedMessage());
		}
	}
	return false;
}

/** Overrides a local file with the JSON in memory.
 * Similar to the load_json method, pass if this function should update the
 * global timestamp of files. Activate it only for the protocol file, not the
 * beacon one.
 */
protected void save_json(final Context context,
	final String data, final String filename, final boolean update_timestamp)
{
	final String name = context.getCacheDir() + "/" + filename;
	FileOutputStream output;
	try {
		output = new FileOutputStream(name);
	} catch (FileNotFoundException e) {
		Log.w(TAG, "Can't save " + name + ".\n" + e.getLocalizedMessage());
		e.printStackTrace();
		return;
	}

	try {
		//Log.w(TAG, "Saving to " + filename);
		output.write(data.getBytes());
		if (update_timestamp)
			last_modified = Math.max(last_modified, System.currentTimeMillis());
	} catch (JsonGenerationException e) {
		Log.w(TAG, "Can't save " + name + ".\n" + e.getLocalizedMessage());
		e.printStackTrace();
	} catch (JsonMappingException e) {
		Log.w(TAG, "Can't save " + name + ".\n" + e.getLocalizedMessage());
		e.printStackTrace();
	} catch (IOException e) {
		Log.w(TAG, "Can't save " + name + ".\n" + e.getLocalizedMessage());
		e.printStackTrace();
	} finally {
		if (output != null) try {
			output.close();
		} catch (IOException e) {
			Log.w(TAG, "Can't close " + name + ".\n" + e.getLocalizedMessage());
		}
	}
}

/** Updates the appdata file timestamp and the global variable.
 */
protected void update_timestamps(final Context context)
{
	final String name = context.getCacheDir() + "/" + FILENAME_APPDATA;
	File file = new File(name);

	final long current_time = System.currentTimeMillis();
	if (file.setLastModified(current_time))
		last_modified = Math.max(last_modified, current_time);
	else
		Log.w(TAG, "Could not update appdata file timestamp");
}

}
