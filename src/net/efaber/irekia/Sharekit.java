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

import java.util.ArrayList;
import java.util.List;

import net.efaber.irekia.I18n.Embedded_string;
import net.efaber.irekia.I18n.System_string;
import net.efaber.irekia.models.Sharing_tags;
import net.efaber.irekia.zerg.Overlord;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.Toast;

/** Class containing sharing code for the app.
 * This code is inspired by the iOS ShareKit project (http://getsharekit.com/)
 * and the lack of a simple way in Android to share something on Twitter or
 * Facebook without writting too much hack y code.
 *
 * The class works as a Mixin class, you add it to your activity passing the
 * activity itself in the constructor.
 */
public class Sharekit
{
private static final String TAG = "Irekia.Sharekit";

/** Mixin to handle user helding a listview item for some time, show actions.
 * The share actions for the item.
 */
public static void onCreateContextMenu(Activity activity, ContextMenu menu,
	View v, ContextMenuInfo menuInfo)
{
	// Detects if the user is trying to share on an expandable list section and
	// ignore that.
	if (ExpandableListContextMenuInfo.class.isInstance(menuInfo)) {
		ExpandableListContextMenuInfo info =
			(ExpandableListContextMenuInfo)menuInfo;
		if (ExpandableListView.PACKED_POSITION_TYPE_CHILD !=
				ExpandableListView.getPackedPositionType(info.packedPosition)) {
			Log.w(TAG, "Not a valid item to show context menu about");
			return;
		}
	}

	MenuInflater inflater = activity.getMenuInflater();
	inflater.inflate(R.menu.share_menu, menu);
	I18n.translate(menu);
}

/** The user selected a context menu option, what should we do?
 * @return Returns always true.
 */
public static boolean onContextItemSelected(Activity activity,
	MenuItem item, Overlord overlord)
{
	ContextMenuInfo info_object = item.getMenuInfo();
	// Depending on the type of view, we need one or another way or getting ids.
	if (ExpandableListContextMenuInfo.class.isInstance(info_object)) {
		ExpandableListContextMenuInfo info =
			(ExpandableListContextMenuInfo)item.getMenuInfo();
		Sharekit.share_item(activity, item.getItemId(),
			overlord.get_item((int)info.id));
	} else if (AdapterContextMenuInfo.class.isInstance(info_object)) {
		AdapterContextMenuInfo info =
			(AdapterContextMenuInfo)item.getMenuInfo();
		Sharekit.share_item(activity, item.getItemId(),
			overlord.items.get(info.position));
	} else {
		Toast.makeText(activity, "Internal error processing item id!",
			Toast.LENGTH_SHORT).show();
	}
	return true;
}

/** Public helper to process sharing an item.
 * Pass the menu identifier that was used to trigger the sharing action and an
 * object supporting the Sharing_tags interface so that the appropriate
 * information can be extracted from it.
 */
public static void share_item(Context context, int menu_id,
	Sharing_tags item)
{
	if (null == item) {
		Toast.makeText(context, "Can't share empty item!",
			Toast.LENGTH_SHORT).show();
		return;
	}

	if (R.id.send_by_mail == menu_id) {
		Sharekit.mail(context, I18n.p(System_string.MAIL_NEWS_SUBJECT),
			I18n.p(System_string.MAIL_NEWS_BODY), item);
	} else if (R.id.twitter == menu_id) {
		Sharekit.tweet(context, item.get_title() + " " + item.get_url());
	} else if (R.id.facebook == menu_id) {
		Sharekit.facebook(context, item.get_title(), item.get_url());
	} else {
		Toast.makeText(context, "Not implemented yet!",
			Toast.LENGTH_SHORT).show();
	}
}

/** Sends an HTML mail.
 * This will look for the intents answering for the ACTION_SEND and text/html
 * type.
 */
static public void mail(Context context, final String subject,
	final String html_body, Sharing_tags item)
{
	Intent intent = new Intent(android.content.Intent.ACTION_SEND);
	intent.setType("text/html");
	final String parsed_subject = replace_tags(subject, item);
	final String parsed_body = replace_tags(html_body, item);
	intent.putExtra(android.content.Intent.EXTRA_SUBJECT, parsed_subject);
	intent.putExtra(android.content.Intent.EXTRA_TEXT,
		Html.fromHtml(parsed_body));

	try {
		context.startActivity(Intent.createChooser(intent,
			I18n.e(Embedded_string.SEND_TO_A_FRIEND)));
	} catch (ActivityNotFoundException e) {
		Toast.makeText(context, I18n.e(Embedded_string.INSTALL_EMAIL),
			Toast.LENGTH_SHORT).show();
	}
}

/** Share a text through an external twitter application.
 * This will query all the programs available on the device with twitter like
 * names and offer a chooser for them. If none are found, the application
 * offers the option of searching the android market to install one.
 *
 * Note that this will fail for applications which don't contain the partial
 * word twitt or tweet in their class/activity names.
 *
 * References:
 *
 * Experimental twitter code, from
 * http://stackoverflow.com/questions/2077008/android-intent-for-twitter-application
 *
 * For class filtering see also
 * http://regis.decamps.info/blog/2011/06/intent-to-open-twitter-client-on-android/
 */
static public void tweet(final Context context, final String message)
{
	assertTrue("Invalid null message to tweet", message != null);
	// Prepare the intents we want to tweak.
	Intent twitter_intent = new Intent(Intent.ACTION_SEND);
	twitter_intent.putExtra(Intent.EXTRA_TEXT, message);
	twitter_intent.setType("application/twitter");

	Intent plain_intent = new Intent(Intent.ACTION_SEND);
	plain_intent.putExtra(Intent.EXTRA_TEXT, message);
	plain_intent.setType("text/plain");

	// Scan list of programs answering to the plain intent.
	PackageManager pm = context.getPackageManager();
	List<ResolveInfo> activities = pm.queryIntentActivities(plain_intent,
		PackageManager.MATCH_DEFAULT_ONLY);
	List<ResolveInfo> candidates = new ArrayList<ResolveInfo>();

	Intent chooser = null;

	// Pick from all the activities those that contain twitter like names.
	for (ResolveInfo candidate : activities) {
		final String name = candidate.activityInfo.name.toLowerCase();
		if (name.indexOf("twitt") >= 0 || name.indexOf("tweet") >= 0) {
			if (name.equals("com.twitter.android.postactivity"))
				candidates.add(0, candidate);
			else
				candidates.add(candidate);
		}
	}

	// If there are candidates, show them first in the list of plain/text.
	if (candidates.size() > 0) {
		chooser = Intent.createChooser(twitter_intent,
			I18n.p(System_string.TWITTER_BUTTON));

		Intent[] intents = new Intent[candidates.size()];
		int f = 0;

		for (ResolveInfo candidate : candidates) {
			intents[f] = new Intent(Intent.ACTION_SEND);
			intents[f].setClassName(candidate.activityInfo.packageName,
				candidate.activityInfo.name);

			intents[f].setType("text/plain");
			intents[f].putExtra(Intent.EXTRA_TEXT, message);
			f++;
		}

		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
		context.startActivity(chooser);
	} else {
		install_alerts(context, "http://market.android.com/search?q=twitter",
			I18n.e(Embedded_string.MISSING_TWITTER_TITLE),
			I18n.e(Embedded_string.MISSING_TWITTER_BODY),
			I18n.e(Embedded_string.INSTALL_FAIL));
	}
}

/** Share a text through the official facebook application.
 *
 * References:
 *
 * Experimental twitter code, from
 * http://stackoverflow.com/questions/3515198/share-text-on-facebook-from-android-app-via-action-send
 */
static public void facebook(final Context context, final String message,
	final String url)
{
	assertTrue("Invalid null message to share", message != null);
	Intent intent = new Intent(Intent.ACTION_SEND);
	intent.putExtra(Intent.EXTRA_SUBJECT, message);
	intent.putExtra(Intent.EXTRA_TEXT, url);
	intent.setType("text/plain");

	// Scan list of programs answering to the plain intent.
	PackageManager pm = context.getPackageManager();
	List<ResolveInfo> activities = pm.queryIntentActivities(intent,
		PackageManager.MATCH_DEFAULT_ONLY);
	List<ResolveInfo> candidates = new ArrayList<ResolveInfo>();

	Intent chooser = null;

	// Pick from all the activities those that contain twitter like names.
	for (ResolveInfo candidate : activities) {
		final String name = candidate.activityInfo.name.toLowerCase();
		if (name.indexOf("facebook") >= 0) {
			if (name.equals("com.facebook.katana.sharelinkactivity"))
				candidates.add(0, candidate);
			else
				candidates.add(candidate);
		}
	}

	// If there are candidates, show them first in the list of plain/text.
	if (candidates.size() > 0) {
		chooser = Intent.createChooser(intent,
			I18n.p(System_string.FACEBOOK_BUTTON));

		Intent[] intents = new Intent[candidates.size()];
		int f = 0;

		for (ResolveInfo candidate : candidates) {
			intents[f] = new Intent(Intent.ACTION_SEND);
			intents[f].setClassName(candidate.activityInfo.packageName,
				candidate.activityInfo.name);

			intents[f].setType("text/plain");
			intents[f].putExtra(Intent.EXTRA_SUBJECT, message);
			intents[f].putExtra(Intent.EXTRA_TEXT, url);
			f++;
		}

		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
		context.startActivity(chooser);
	} else {
		install_alerts(context, "http://market.android.com/search?q=facebook",
			I18n.e(Embedded_string.MISSING_FACEBOOK_TITLE),
			I18n.e(Embedded_string.MISSING_FACEBOOK_BODY),
			I18n.e(Embedded_string.INSTALL_FAIL));
	}
}

/** Handles the failure dialog sequence if an application was not installed.
 * First a dialog will ask the user if she wants to install an application,
 * which simply opens the passed market search url. And if that fails too, a
 * simple facepalm message is displayed.
 */
static private void install_alerts(final Context context, final String url,
	final String title, final String body, final String fail_body)
{
	// Yuck, no candidates, try to install one from the android market.
	DialogInterface.OnClickListener listener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			if (DialogInterface.BUTTON_POSITIVE != which)
				return;
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				context.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				// What, no web browser? Ridiculous, bail out.
				AlertDialog.Builder builder = new AlertDialog.Builder(
					context);
				builder.setCancelable(true);
				builder.setTitle(title);
				builder.setMessage(fail_body);
				builder.setPositiveButton(android.R.string.yes, null);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.create().show();
			}
		}};

	AlertDialog.Builder builder = new AlertDialog.Builder(context);
	builder.setCancelable(true);
	builder.setPositiveButton(android.R.string.yes, listener);
	builder.setNegativeButton(android.R.string.no, listener);
	builder.setTitle(title);
	builder.setMessage(body);
	builder.setIcon(android.R.drawable.ic_dialog_alert);
	builder.create().show();
}

/** Replaces in text the tags extracted from the content item.
 * nil attributes in FLContent_item will be replaced by empty strings.
 */
private static String replace_tags(final String input_text,
	final Sharing_tags item)
{
	String text = input_text.replace("<PHOTO_DESC>", item.get_photo_desc());
	text = text.replace("<TITLE>", item.get_title());
	text = text.replace("<URL>", item.get_url());
	return text;
}

}
