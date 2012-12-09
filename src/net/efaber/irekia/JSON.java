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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

/** Simple free style JSON reader.
 * This class simplifies error checking and default behaviours when reading
 * from a random JSON string. You want to use it when you don't want to create
 * POJOs, like when you have minimal strings.
 */
public class JSON
{
private static final String TAG = "Irekia.JSON";

/// Holds the map object, you can read it from outside too.
public Map<String, Object> data;

/** Make printf debugging simpler on us.
 */
public String toString()
{
	if (null == data)
		return "{}";
	else
		return data.toString();
}

/** Tries to convert an object to a JSON map.
 * @return Returns a JSON map object or null if the thing could not be
 * converted properly.
 */
static public JSON map(Object thing)
{
	if (Map.class.isInstance(thing))
		return new JSON(thing);
	else
		return null;
}

/** Tries to parse a chunk of bytes as JSON.
 *
 * @param bytes, the stuff you want to parse.
 * @return a JSON object or null if it was not possible.
 */
static public JSON parse(final byte[] bytes)
{
	try {
		return new JSON(bytes);
	} catch (JsonParseException e) {
		Log.w(TAG, "JsonParseException: " + e.getLocalizedMessage());
		e.printStackTrace();
	} catch (JsonMappingException e) {
		Log.w(TAG, "JsonMappingException: " + e.getLocalizedMessage());
		e.printStackTrace();
	} catch (IOException e) {
		Log.w(TAG, "IOException: " + e.getLocalizedMessage());
		e.printStackTrace();
	}
	return null;
}

/** Tries to parse a chunk of bytes as JSON.
 *
 * @param bytes, the stuff you want to parse.
 * @return a JSON object or null if it was not possible.
 */
static public JSON parse(final String data)
{
	try {
		return new JSON(data);
	} catch (JsonParseException e) {
		Log.w(TAG, "JsonParseException: " + e.getLocalizedMessage());
		e.printStackTrace();
	} catch (JsonMappingException e) {
		Log.w(TAG, "JsonMappingException: " + e.getLocalizedMessage());
		e.printStackTrace();
	} catch (IOException e) {
		Log.w(TAG, "IOException: " + e.getLocalizedMessage());
		e.printStackTrace();
	}
	return null;
}

/** Creates a reader object with the specified bytes.
 * @throws IOException
 * @throws JsonMappingException
 * @throws JsonParseException
 */
@SuppressWarnings("unchecked")
public JSON(byte[] bytes)
	throws JsonParseException, JsonMappingException, IOException
{
	data = Irekia.mapper.readValue(bytes, Map.class);
}

/** Creates a reader object with the file input stream.
 * @throws IOException
 * @throws JsonMappingException
 * @throws JsonParseException
 */
@SuppressWarnings("unchecked")
public JSON(FileInputStream input)
	throws JsonParseException, JsonMappingException, IOException
{
	data = Irekia.mapper.readValue(input, Map.class);
}

/** Creates a reader object with the input stream.
 * @throws IOException
 * @throws JsonMappingException
 * @throws JsonParseException
 */
@SuppressWarnings("unchecked")
public JSON(InputStream input)
	throws JsonParseException, JsonMappingException, IOException
{
	data = Irekia.mapper.readValue(input, Map.class);
}

/** Creates a reader object with the input String.
 * @throws IOException
 * @throws JsonMappingException
 * @throws JsonParseException
 */
@SuppressWarnings("unchecked")
public JSON(final String input)
	throws JsonParseException, JsonMappingException, IOException
{
	data = Irekia.mapper.readValue(input, Map.class);
}

/** Creates a reader object from a map object.
 */
@SuppressWarnings("unchecked")
public JSON(Object object)
{
	data = (Map<String, Object>) object;
}

/** Retrieves an integer from the JSON.
 * @return Returns the value of the integer, or def if the value was not
 * present or malformed.
 */
public int get_int(String name, int def)
{
	Object object = data.get(name);
	if (null == object)
		return def;
	if (Integer.class == object.getClass())
		return ((Integer)object).intValue();
	else
		return def;
}

/** Retrieves a long from the JSON.
 * @return Returns the value of the long, or def if the value was not
 * present or malformed.
 */
public long get_long(String name, long def)
{
	Object object = data.get(name);
	if (null == object)
		return def;
	if (Integer.class == object.getClass())
		return ((Integer)object).intValue();
	else if (Long.class == object.getClass())
		return ((Long)object).longValue();
	else if (Double.class == object.getClass())
		return ((Double)object).longValue();
	else
		return def;
}

/** Retrieves a float from the JSON.
 * @return Returns the value of the float, or def if the value was not
 * present or malformed.
 */
public float get_float(String name, float def)
{
	Object object = data.get(name);
	if (null == object)
		return def;
	if (Float.class == object.getClass())
		return ((Float)object).floatValue();
	else
		return def;
}

/** Retrieves a boolean from the JSON.
 * @return Returns the value of the boolean, or def if the value was not
 * present or malformed.
 */
public boolean get_bool(String name, boolean def)
{
	Object object = data.get(name);
	if (null == object)
		return def;
	if (Boolean.class == object.getClass())
		return ((Boolean)object).booleanValue();
	else
		return def;
}

/** Retrieves a string from the JSON.
 * @return Returns the value of the string, or def if the value was not
 * present or malformed.
 */
public String get_string(String name, String def)
{
	Object object = data.get(name);
	if (null == object)
		return def;
	if (String.class == object.getClass())
		return ((String)object).replace("\r", "");
	else
		return def;
}

/** Wrapper over get_string which tries to clean up spaces and stuff.
 * Note that this does NOT a full url encoding, because doing so we would then
 * translate also the path separators, colons, etc. This is pretty much a "try
 * to parse a few bad characters without breaking the rest", since url encoding
 * is not "reentrant".
 *
 * @return Returns the value of the string, as much clean and URL valid as
 * possible.
 */
public String get_url(String name, String def)
{
	String raw_url = get_string(name, def);
	if (null == raw_url)
		return def;

	String url = raw_url.trim();
	int pos = 0;
	while (pos < url.length()) {
		final char letter = url.charAt(pos++);
		// These are the valid characters we want to preserved unfiltered.
		switch (letter) {
			case ';':
			case '/':
			case '?':
			case ':':
			case '@':
			case '&':
			case '=':
			case '_':
			case '+':
			case '.':
			case ',':
			case '%':
				break;
			default:
				// Still, don't filter all chars.
				if ((letter >= '0' && letter <= '9') ||
						(letter >= 'A' && letter <= 'Z') ||
						(letter >= 'a' && letter <= 'z')) {
					break;
				} else {
					url = url.replace(String.valueOf(letter),
						"%" + Integer.toHexString(letter));
				}
		}
	}
	return url;
}

/** Retrieves a JSON map from the JSON.
 * @return Returns the value of the JSON, or def if the value was not
 * present or malformed.
 */
public JSON get_map(String name, JSON def)
{
	Object object = data.get(name);
	if (null == object)
		return def;

	JSON ret = JSON.map(object);
	if (null == ret)
		return def;
	else
		return ret;
}

/** Retrieves an array from the JSON.
 * @return Returns the value of the array, or def if the value was not
 * present or malformed. Arrays are forced to contain objects of the same
 * type, so you have to pass the class of the object you expect to be
 * contained in the array.
 */
public <T> ArrayList<T> get_array(String name, ArrayList<T> def,
		Class<?> cls)
{
	Object object = data.get(name);
	if (null == object)
		return def;
	if (ArrayList.class != object.getClass())
		return def;
	// Now test all instances.
	@SuppressWarnings("unchecked")
	ArrayList<T> ret = (ArrayList<T>)object;
	for (Object tester : ret)
		if (!cls.isInstance(tester))
			return def;

	return ret;
}

/** Retrieves a base64 image from the JSON.
 * @return Returns the value of the image already decoded, or def if the value
 * was not present or malformed.
 */
public Bitmap get_image(String name, Bitmap def)
{
	String base64 = get_string(name, null);
	if (null == base64)
		return def;

	byte buf[] = Base64_2_2.Base64.decodeFast(base64);
	if (buf.length < 10)
		return def;

	Bitmap ret = BitmapFactory.decodeByteArray(buf, 0, buf.length);
	if (null == ret)
		return def;
	else
		return ret;
}

/** Retrieves a size pair from the JSON.
 * @return Returns a two element array with the value of the JSON, or a two
 * element array with the provided default values.
 */
public int[] get_size(String name, int default_w, int default_h)
{
	ArrayList<Integer> values = get_array(name, null, Integer.class);
	int ret[] = { default_w, default_h };
	if (null == values || 2 != values.size())
		return ret;

	ret[0] = values.get(0);
	ret[1] = values.get(1);
	return ret;
}

public int get_color(String name, int def)
{
	ArrayList<Integer> values = get_array(name, null, Integer.class);
	if (null == values || 3 != values.size())
		return def;

	final int rgb[] = { values.get(0), values.get(1), values.get(2) };

	if (rgb[0] < 0 || rgb[0] > 255 ||
			rgb[1] < 0 || rgb[1] > 255 || rgb[2] < 0 || rgb[2] > 255)
		return def;
	else
		return Color.rgb(rgb[0], rgb[1], rgb[2]);
}

}
