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
import net.efaber.irekia.Irekia;
import net.efaber.irekia.Irekia_default_activity;
import net.efaber.irekia.Navigation_buttons;
import net.efaber.irekia.R;
import net.efaber.irekia.Sharekit;
import net.efaber.irekia.models.Content_item;
import net.efaber.irekia.zerg.Overlord;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class Web_activity extends Irekia_default_activity
	implements View.OnClickListener, Runnable
{
/// Points to the layout's view flipper.
private ViewFlipper flipper;
/// Holds pointers to both web views inside the view flipper.
private WebView first_webview, second_webview;
/// The buttons used to navigate news. Might be hidden.
private Navigation_buttons buttons;

/// Pointer to the overlord controlling the item and navigation.
private Overlord overlord;
/// The item itself, we extract the URL from it and use it to share.
private Content_item item;

/// Points to the previous limit warning toast, to avoid spamming the user.
private Toast last_limit_toast;

/// Minimum speed of the swipe to be accepted as a valid fling gesture.
private float swipe_threshold = 1000;
/// Minimum finger distance traveled to accept flings.
private float swipe_distance = 1000;

/// Our gesture detector.
private final GestureDetector mGestureDetector =
	new GestureDetector(new Custom_fling_detector());

/** Called when the activity is first created. */
@Override
public void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	requestWindowFeature(Window.FEATURE_PROGRESS);
	setContentView(R.layout.webview);

	setProgressBarIndeterminateVisibility(true);
	setProgressBarVisibility(true);

	// Calibrate flick gestures according to the device's resolution.
	final ViewConfiguration vc = ViewConfiguration.get(this);
	swipe_threshold = vc.getScaledMinimumFlingVelocity();
	swipe_distance = getResources().getDisplayMetrics().xdpi * 0.70f;

	// Set up GUI and connect handlers.
	flipper = (ViewFlipper)findViewById(R.id.flipper);
	first_webview = (WebView)findViewById(R.id.first_webview);
	first_webview.setWebViewClient(new Web_view_client());
	first_webview.getSettings().setJavaScriptEnabled(true);
	first_webview.getSettings().setPluginsEnabled(true);
	second_webview = (WebView)findViewById(R.id.second_webview);
	second_webview.setWebViewClient(new Web_view_client());
	second_webview.getSettings().setJavaScriptEnabled(true);
	second_webview.getSettings().setPluginsEnabled(true);
	buttons = new Navigation_buttons(this, this);
	first_webview.setWebChromeClient(new Web_chrome_client());
	second_webview.setWebChromeClient(new Web_chrome_client());

	// Hide the buttons if the user enabled hardware key navigation.
	if (Irekia.volume_key_navigation) {
		LinearLayout button_layout =
			(LinearLayout)findViewById(R.id.button_layout);
		if (null != button_layout)
			button_layout.setVisibility(View.GONE);
	}

	// Caller dependant setup through parameters.
	load_params();

	if (null == savedInstanceState) {
		if (null != item && null != item.url)
			visible_web().loadUrl(item.url);
	} else {
		visible_web().restoreState(savedInstanceState);
	}
}

/** Called before killing the activity. Try to save web contents.
 */
@Override
protected void onSaveInstanceState(Bundle outState)
{
	visible_web().saveState(outState);
}

/** Pass some parameters to our future self after rotation.
 * We actually do this through the intent.
 */
@Override
public Object onRetainNonConfigurationInstance()
{
	save_params();
	return null;
}

/** Loads the parameters for the web view from the bundle.
 * This method will fill the attributes overlord and item or leave them as
 * null.  It also sets the title of the activity, breadcrumb style. Call this
 * method after the interface has been set up, since this updates the state of
 * the navigation buttons.
 */
private void load_params()
{
	overlord = null;
	item = null;

	Bundle extras = getIntent().getExtras();
	String title = extras.getString(Irekia.TITLE_PARAM);
	if (null != title)
		setTitle("Irekia: " + title);

	overlord = Irekia.get_overlord(extras.getInt(Irekia.OVERLORD_ID_PARAM, -1));
	if (null != overlord)
		item = overlord.get_item(extras.getInt(Irekia.CONTENT_ID_PARAM, -1));

	buttons.update(overlord, item);
}

/** Inverse version of load_params(), call it before rotation or so.
 * In fact, you should call this whenever the content changes, since you really
 * don't know when you are going to be killed by the grim reaper to be reborn
 * later.
 */
private void save_params()
{
	Bundle extras = getIntent().getExtras();
	extras.remove(Irekia.OVERLORD_ID_PARAM);
	extras.remove(Irekia.CONTENT_ID_PARAM);
	if (null != overlord)
		extras.putInt(Irekia.OVERLORD_ID_PARAM, overlord.id);

	if (null != item)
		extras.putInt(Irekia.CONTENT_ID_PARAM, item.id);
	getIntent().putExtras(extras);
}

/** Small helper to get the currently viewed web view.
 * @return the WebView object, could be null.
 */
private WebView visible_web()
{
	if (flipper.getCurrentView() == first_webview)
		return first_webview;
	else
		return second_webview;
}

/** Small helper to get the currently hidden web view.
 * @return the WebView object, could be null.
 */
private WebView hidden_web()
{
	if (flipper.getCurrentView() == first_webview)
		return second_webview;
	else
		return first_webview;
}

/** Changes the current item to the previous or next one.
 * @param direction A positive or negative value, the function won't do
 * anything if it is zero. The direction will move the item forward or backward
 * through the overlord's list and animate a view flip.
 *
 * Even if no changes are done, this function calls buttons.update().
 */
private void switch_item(int direction)
{
	if (null != overlord && 0 != direction) {
		Content_item new_item = null;
		if (direction < 0) {
			flipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_in_left));
			flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_out_right));
			//flipper.showPrevious();
			new_item = overlord.get_prev(item);
		} else if (direction > 0) {
			flipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_in_right));
			flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_out_left));
			//flipper.showNext();
			new_item = overlord.get_next(item);
		}

		// Ok, if the next item is ok, flip to it.
		if (null != new_item && null != new_item.url) {
			hidden_web().clearHistory();
			hidden_web().loadUrl(new_item.url);
			item = new_item;
			flipper.showNext();
			// Enqueue removing content from the hidden view.
			flipper.postDelayed(this, 200);
			save_params();
		} else if (0 != direction && Irekia.volume_key_navigation) {
			// Looks like we reached a limit, tell the user.
			if (null == last_limit_toast)
				last_limit_toast = Toast.makeText(this,
					I18n.e(Embedded_string.VOLUME_LIST_LIMIT),
					Toast.LENGTH_SHORT);

			if (null != last_limit_toast) {
				last_limit_toast.cancel();
				last_limit_toast.show();
			}
		}
	}
	buttons.update(overlord, item);
}

/** Method implementing the Runnable interface for the onClick future.
 * This simply resets the hidden web view to empty, so it doesn't show up with
 * stale content when the user flips to a new view.
 */
public void run()
{
	WebView view = hidden_web();
	if (null != view) {
		view.clearHistory();
		view.clearView();
	}
}

/** Override the key down and route it to web back navigation.
 * @return returns true if we did intercept the web view navigation otherwise
 * what super does.
 */
@Override
public boolean onKeyDown(final int keyCode, final KeyEvent event)
{
	final int direction = Irekia.handle_volume_keys(keyCode);
	if (0 != direction) {
		switch_item(direction);
		return true;
	}
	return super.onKeyDown(keyCode, event);
}

/** Handles next/prev button clicks to change the current item.
 */
public void onClick(View v)
{
	if (v == buttons.prev)
		switch_item(-1);
	else if (v == buttons.next)
		switch_item(+1);
}

/** Hook touch events and delegate them to our custom fling class.
 */
@Override
public boolean dispatchTouchEvent(MotionEvent e)
{
	super.dispatchTouchEvent(e);
	return mGestureDetector.onTouchEvent(e);
}

/** Implements fling detection on the webviews.
 */
private class Custom_fling_detector
	extends GestureDetector.SimpleOnGestureListener
{
public boolean onFling (MotionEvent e1, MotionEvent e2, float vX, float vY)
{
	final float abs_v = Math.abs(vX);
	if (abs_v > Math.abs(vY)) {
		// Filtered horizontal movement.
		if (abs_v > swipe_threshold &&
				Math.abs(e1.getX() - e2.getX()) > swipe_distance) {
			final boolean fling_to_right = vX > 0;
			if (fling_to_right)
				switch_item(-1);
			else
				switch_item(+1);
			return true;
		}
	}
	return false;
}

}

/********************************** Sharekit ********************************/

/** The user pressed the menu button, create the share menu.
 * @return returns always true, because we always intercept the action.
 */
@Override
public boolean onCreateOptionsMenu(Menu menu)
{
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.share_menu, menu);
	I18n.translate(menu);
	return true;
}

/** The user selected a menu option, what should we do?
 * @return returns always true, because we always intercept the action.
 */
@Override
public boolean onOptionsItemSelected(MenuItem menuitem)
{
	if (null != item)
		Sharekit.share_item(this, menuitem.getItemId(), item);
	return true;
}

/****************************************************************************/

/** Private class to set/update the progress visibility in the activity.
 */
private class Web_chrome_client extends WebChromeClient
{
	public void onProgressChanged(WebView view, int progress) {
		final boolean visible = progress < 100;
		setProgressBarIndeterminateVisibility(visible);
		setProgressBarVisibility(visible);
		setProgress(progress * 100);
	}
}

/** The actual class extending the web client to implement our custom behaviour.
 * We just open the OS browser if available.
 */
private class Web_view_client extends WebViewClient
{
@Override
public boolean shouldOverrideUrlLoading(WebView view, String url)
{
	try {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	} catch (ActivityNotFoundException e) {
		view.loadUrl(url);
	}
	return true;
}

}

}
