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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/** Contains multiple internationalization helpers.
 */
public class I18n
{
private static final String TAG = "Irekia.I18n";

/// Possible values for the system string runtime identifiers.
public enum System_string
{
	FIRST(9999),
	NEWS_SHARING_TITLE(10000),
	PICTURE_SHARING_TITLE(10001),
	COPY_TO_CLIPBOARD(10002),
	SEND_EMAIL(10003),
	CANCEL_ACTION(10004),
	MAIL_NEWS_SUBJECT(10005),
	MAIL_PICTURE_SUBJECT(10006),
	MAIL_NEWS_BODY(10007),
	MAIL_PICTURE_BODY(10008),
	MAIL_PICTURE_BODY_DESC(10009),
	MOVIE_SHARING_TITLE(10010),
	MAIL_MOVIE_SUBJECT(10011),
	MAIL_MOVIE_BODY(10012),
	TWITTER_BUTTON(10013),
	FACEBOOK_BUTTON(10014),
	BROWSE_IPAD_SECTIONS(10015),
	IPAD_SECTION_BUTTON(10016),
	IPHONE_SHOW_MAP(10017),
	IPHONE_MAP_RETURN(10018),
	IPAD_MAP_PLUS(10019),
	IPAD_MAP_MINUS(10020),
	GPS_END_OF_THE_WORLD(10021),
	GPS_SEARCHING_POSITION(10022),
	GPS_ERROR_IN_DISTANCE(10023),
	GPS_YOU_ARE_THERE(10024),
	GPS_FORMAT_METERS(10025),
	GPS_FORMAT_KMS1(10026),
	GPS_FORMAT_KMS2(10027),
	RELATED_ITEMS(10028),
	DOWNLOAD_IN_PROGRESS(10029),
	DOWNLOAD_RETRY(10030),
	ERROR_PARSING_JSON(10031),
	ERROR_DOWNLOADING(10032),
	DOWNLOAD_TRANSFORM_ERROR(10033),
	ERROR_PROCESSING(10034),
	ERROR_NO_CONTROLLER(10045),
	LAST(10046);

	System_string(int value) { this.value = value; };
	private final int value;
	public int value() { return value; };
}

/// Possible values for the embedded string identifiers.
public enum Embedded_string
{
	IN_PROGRESS(1),
	DOWNLOADING_PLEASE_WAIT(2),
	SEND_TO_A_FRIEND(3),
	INSTALL_EMAIL(4),
	CLOSE(7),
	CONNECTING_TO_SERVER(8),
	MISSING_TWITTER_TITLE(32),
	MISSING_TWITTER_BODY(32),
	INSTALL_FAIL(34),
	MISSING_FACEBOOK_TITLE(35),
	MISSING_FACEBOOK_BODY(36),
	VOLUME_LIST_LIMIT(37);

	Embedded_string(int value) { this.value = value; };
	private final int value;
	public int value() { return value; };
}

/// Holds the protocol strings for the selected language.
private static Map<Integer,String> values;

/// Holds the embedded strings for the selected language.
private static Map<Integer,String> embedded;

/// The default language code of the application content.
public static final String DEFAULT_LANGCODE = "en";

/// The current langcode that is being used.
public static String current_langcode = "en";

/** Returns the specific system language code.
 * You can call this anytime anywhere.
 *
 * @return A two letter string, always, if something fails returns the default
 * language code.
 */
public static String get_system_langcode()
{
	final String langcode = Locale.getDefault().getLanguage();
	if (langcode.length() != 2)
		return DEFAULT_LANGCODE;
	else
		return langcode;
}

/** Parses the i18n dictionary contained in the json.
 * Note that this function doesn't have side effects, if the parsing was
 * successful you still have to call set_values() with the result.
 *
 * @param langs This comes from the protocol's appdata langs array.
 *
 * @return Returns a valid Map<Integer,String> object with the lookups for the
 * current language, or null if there was any problem.
 */
static public Map<Integer,String> parse_json_langs(ArrayList<Object> langs)
{
	if (null == langs || langs.size() < 1)
		return null;

	Map<Integer,String> master_lang = parse_json_lang_strings(langs.get(0));
	if (null == master_lang) {
		Log.w(TAG, "Didn't find valid master language in protocol!");
		return null;
	}

	Map<Integer,String> preferred = scan_for_ui_langs(langs);
	if (null == preferred)
		preferred = master_lang;

	// Use the preferred version with the master_lang as backing default.
	Map<Integer,String> mixed = new HashMap<Integer,String>(master_lang);
	if (null != preferred)
		mixed.putAll(preferred);
	return preferred;
}

/** Converts a single JSON mapping of strings:strings.
 * Since this function would strip the language code, it is stored as the -1
 * key.
 * @return Returns a valid Map<Integer,String> object with the lookups for the
 * current language, or null if there was any problem.
 */
private static Map<Integer,String> parse_json_lang_strings(Object lang_map)
{
	// Get the language codename from the single key of the set.
	JSON lang_json = JSON.map(lang_map);
	if (null == lang_json)
		return null;

	Set<String> lang_keys = lang_json.data.keySet();
	if (1 != lang_keys.size()) {
		Log.w(TAG, "Couldn't parse lang key from i18n map" + lang_map);
		return null;
	}
	final String langcode = lang_keys.toArray(new String[0])[0];
	lang_json = lang_json.get_map(langcode, null);
	if (null == lang_json) {
		Log.w(TAG, "Couldn't extract data for " + langcode);
		return null;
	}
	// Now iterate over the keys/values converting them to integer/string.
	Map<Integer,String> ret = new HashMap<Integer,String>();
	ret.put(-1, langcode);
	for (String key : lang_json.data.keySet().toArray(new String[0])) {
		String value = lang_json.get_string(key, null);
		if (null == value)
			continue;

		try {
			Integer integer = Integer.decode(key);
			ret.put(integer, value);
		} catch (NumberFormatException e) {
			Log.w(TAG, "Bad I18N key number in " + key);
		}
	}
	return ret;
}

/** Scans the available languages and picks the one preferred by the user.
 * @return Returns a valid Map<Integer,String> object with the lookups for the
 * preferred language, or null if there was any problem or wasn't found.
 */
private static Map<Integer,String> scan_for_ui_langs(ArrayList<Object> langs)
{
	final String preferred_langcode = get_effective_language();
	assertTrue("Effective language can't be null", null != preferred_langcode);

	for (Object candidate : langs) {
		Map<Integer,String> lang = parse_json_lang_strings(candidate);
		if (null == lang)
			continue;
		final String test_code = lang.get(-1);
		if (preferred_langcode.equals(test_code))
			return lang;
	}
	return null;
}

/** Returns the currently effective selected user language.
 * This function only looks at the system preferences, the global
 * OS language, and returns the string it thinks should be used by
 * the code. This transforms the auto setting too. The final returned
 * string should be two characters long, or something like that.
 */
public static String get_effective_language()
{
	// Read the user's language preferences.
	String preferred_langcode = Irekia.lang_preference;
	if (null == preferred_langcode || preferred_langcode.equals("auto"))
		preferred_langcode = get_system_langcode();

	// Just in case we get something weird.
	if (preferred_langcode.length() != 2) {
		Log.w(TAG, "We got some weird code '" + preferred_langcode +
			"', setting default.");
		preferred_langcode = DEFAULT_LANGCODE;
	}
	return preferred_langcode;
}

/** Quick protocol string getter.
 * @param string_id The number of the string you want from the protocol.
 * @return Returns the string or a fake "#id" string if it wasn't found. This
 * method will never return null.
 */
public static String p(final int string_id)
{
	return p(Integer.valueOf(string_id));
}

public static String p(final Integer integer)
{
	if (null == values)
		return "#" + integer;

	String value = values.get(integer);
	if (null == value)
		return "#" + integer;
	else
		return value;
}

public static String p(final System_string sys)
{
	return p(sys.value());
}

/** Quick embedded string getter.
 * @param string_id The number of the string you want from the embedded strings.
 * @return Returns the string or a fake "#id" string if it wasn't found. This
 * method will never return null.
 */
public static String e(final int string_id)
{
	if (null == embedded)
		return "#" + string_id;

	String value = embedded.get(Integer.valueOf(string_id));
	if (null == value)
		return "#" + string_id;
	else
		return value;
}

public static String e(final Embedded_string sys)
{
	return e(sys.value());
}

/** Sets the language of the hardcoded language strings of the application.
 * The hardcoded strings are accesible through the e() method and usually show
 * up in places which don't have a protocol customizable string, like the
 * settings screen.
 *
 * Usually you will call this during the initialisation of Irekia (after the
 * preferences are loaded) and when there is a configuration change.
 */
public static void set_embedded_values(Context context, final String langcode)
{
	if (null == embedded)
		embedded = new HashMap<Integer,String>();

	InputStream input = null;
	try { input = context.getAssets().open("str_" + langcode + ".json");
	} catch (IOException e) { }

	if (null == input) {
		try { input = context.getAssets().open("str_en.json");
		} catch (IOException e) { }
	}

	if (null == input) {
		assertTrue("Can't load even default embedded values?", false);
		return;
	}

	try {
		JSON json = new JSON(input);

		for (String key : json.data.keySet().toArray(new String[0])) {
			String value = json.get_string(key, null);
			if (null == value)
				continue;

			try {
				Integer integer = Integer.decode(key);
				embedded.put(integer, value);
			} catch (NumberFormatException e) {
				Log.w(TAG, "Bad I18N key number in " + key);
			}
		}
	} catch (JsonParseException e) {
		e.printStackTrace();
	} catch (JsonMappingException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		try {
			input.close();
		} catch (IOException e) {
		}
	}
}

/** Used by other loading classes to change the current global protocol map.
 * The map you pass has to have a -1 key with the two letter langcode of the
 * new values, which will be set in the global current_langcode variable and
 * also saved in the application's last_lang_preference setting. The code will
 * add the default strings from System_string.FIRST to System_string.LAST using
 * the resources if not present in new_values.
 */
public static void set_values(Context context, Map<Integer,String> new_values)
{
	assertTrue("New values don't contain magic -1 key!",
		null != new_values.get(-1));
	assertTrue("-1 key value requried to be two letter long",
		2 == new_values.get(-1).length());

	values = new_values;
	current_langcode = values.get(-1);

	// Fill in the default strings.
	int counter = 10000;
	final String[] defaults = {
		"Share this news...",
		"Share this picture...",
		"Copy address",
		"Send email",
		"Cancel",
		"<TITLE>",
		"<TITLE>",
		"<A HREF=\"<URL>\"><URL></A>",
		"<A HREF=\"<URL>\"><URL></A>",
		"<A HREF=\"<URL>\"><PHOTO_DESC></A>",
		"Share this movie...",
		"<TITLE>",
		"<A HREF=\"<URL>\"><URL></A>",
		"Twitter",
		"Facebook",
		"Browse the sections and select an item",
		"Sections",
		"Map",
		"Return",
		"Map (+)",
		"Map (-)",
		"End of the world",
		"Searching...",
		"Error in distance",
		"You are there",
		"%d meters",
		"%0.1f kms",
		"%0.0f kms",
		"Related items",
		"Download in progress.\nPlease wait.",
		"Retry download",
		"Couldn't parse JSON",
		"Error downloading data",
		"Data wasn't transformed propertly",
		"Error processing data",
		"No valid controller found to process info.",
	};
	for (String default_string : defaults) {
		if (!values.containsKey(counter))
			values.put(counter, default_string);
		counter++;
	}

	Editor prefs =
		PreferenceManager.getDefaultSharedPreferences(context).edit();
	prefs.putString(Irekia.PREFKEY_LAST_LANG, current_langcode);
	prefs.commit();
	Irekia.last_lang_preference = current_langcode;
}

/** Changes menu item titles' numbers into the embedded strings
 */
public static void translate(Menu menu)
{
	for (int f = 0; f < menu.size(); f++) {
		MenuItem item = menu.getItem(f);
		final String title = item.getTitle().toString();
		try {
			Integer integer = Integer.decode(title);
			item.setTitle(e(integer));
		} catch (NumberFormatException e) {
			Log.w(TAG, "Bad menu key title in " + title);
		}
	}
}

}
