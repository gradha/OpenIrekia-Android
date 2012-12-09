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

package net.efaber.irekia.video;

import net.efaber.irekia.Colors;
import net.efaber.irekia.I18n;
import net.efaber.irekia.I18n.Embedded_string;
import net.efaber.irekia.Irekia;
import net.efaber.irekia.Irekia_default_activity;
import net.efaber.irekia.Navigation_buttons;
import net.efaber.irekia.R;
import net.efaber.irekia.Sharekit;
import net.efaber.irekia.models.Content_item;
import net.efaber.irekia.models.News_item;
import net.efaber.irekia.zerg.Overlord;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.github.droidfu.widgets.WebImageView;

public class Video_facade_activity extends Irekia_default_activity
	implements View.OnClickListener, AdapterView.OnItemClickListener
{
/// Points to the layout's view flipper.
private ViewFlipper flipper;
/// Holds pointers to both web views inside the view flipper.
private ListView first_list, second_list;
/// Pair of adapters and their respective backing arrays.
private Video_adapter first_adapter, second_adapter;

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
	setContentView(R.layout.video_facade);

	// Calibrate flick gestures according to the device's resolution.
	final ViewConfiguration vc = ViewConfiguration.get(this);
	swipe_threshold = vc.getScaledMinimumFlingVelocity();
	swipe_distance = getResources().getDisplayMetrics().xdpi * 0.70f;

	// Set up GUI and connect handlers.
	flipper = (ViewFlipper)findViewById(R.id.flipper);
	first_list = (ListView)findViewById(R.id.first_list);
	first_list.setOnItemClickListener(this);
	first_adapter = new Video_adapter(this);
	first_list.setAdapter(first_adapter);
	second_list = (ListView)findViewById(R.id.second_list);
	second_list.setOnItemClickListener(this);
	second_adapter = new Video_adapter(this);
	second_list.setAdapter(second_adapter);
	buttons = new Navigation_buttons(this, this);

	// Hide the buttons if the user enabled hardware key navigation.
	if (Irekia.volume_key_navigation) {
		LinearLayout button_layout =
			(LinearLayout)findViewById(R.id.button_layout);
		if (null != button_layout)
			button_layout.setVisibility(View.GONE);
	}

	// Caller dependant setup through parameters.
	load_params();
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

	((Video_adapter)visible_list().getAdapter()).set_item(item);
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
private ListView visible_list()
{
	if (flipper.getCurrentView() == first_list)
		return first_list;
	else
		return second_list;
}

/** Small helper to get the currently hidden web view.
 * @return the WebView object, could be null.
 */
private ListView hidden_list()
{
	if (flipper.getCurrentView() == first_list)
		return second_list;
	else
		return first_list;
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
			item = new_item;
			((Video_adapter)hidden_list().getAdapter()).set_item(item);
			flipper.showNext();
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

/** The user touched a cell.
 * We want to discard touches on the top cell, the bottom cell will play back
 * the video.
 */
public void onItemClick(AdapterView<?> parent, View view, int position, long id)
{
	if (1 != position || null == item)
		return;

	// Ugly hack here to detect video items.
	Intent intent = new Intent(this, Video_player_activity.class);
	intent.putExtra(Video_player_activity.IOS_URL_PARAM, item.url);
	if (null != item.data)
		intent.putExtra(Video_player_activity.PROGRESSIVE_URL_PARAM,
			item.data.get_url("progressive_file", null));

	startActivity(intent);
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

/****************************************************************************/

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

/** Implements a fake video adapter on top of the selected item.
 */
private class Video_adapter extends BaseAdapter
	implements ListAdapter
{
/// The item we have to show.
private News_item item;

/// Extracted configuration data for the video item.
private String preview_url;
private int title_color, text_color;
private float preview_width, preview_height, padding, title_size, text_size;

/// Store pointer to context to be able to create widgets.
private Context context;

public Video_adapter(Context context)
{
	super();
	this.context = context;
}

/// Sets and refreshes the parent list with the contents.
public void set_item(Content_item item)
{
	if (News_item.class.isInstance(item)) {
		this.item = (News_item)item;
		preview_url = item.image;
		// Extract video data from the item.
		if (null != item.data) {
			preview_url = item.data.get_url("preview_url", item.image);
			preview_width = item.data.get_int("preview_width", 285);
			preview_width = Math.max(30, Math.min(285, preview_width));
			preview_height = item.data.get_int("preview_width", 160);
			preview_height = Math.max(30, Math.min(160, preview_height));
			padding = item.data.get_int("padding", 3);
			title_size = item.data.get_int("title_size", 16);
			title_color = item.data.get_color("title_color", Colors.blue);
			text_size = item.data.get_int("text_size", 13);
			text_color = item.data.get_int("text_color", Colors.black);
			// Scale everything to final pixel sizes.
			preview_width *= Irekia.density;
			preview_height *= Irekia.density;
			padding *= Irekia.density;
			title_size *= Irekia.density;
			text_size *= Irekia.density;
		}
	} else {
		this.item = null;
		preview_url = null;
	}

	notifyDataSetChanged();
}

@Override
public int getCount()
{
	return (null == item) ? 0 : 2;
}

@Override
public Object getItem(int position)
{
	return item;
}

@Override
public long getItemId(int position)
{
	return position;
}

@Override
public int getItemViewType(int position)
{
	return position;
}

@Override
public View getView(int position, View convertView, ViewGroup parent)
{
	if (null == item) {
		TextView t = new TextView(context);
		t.setText("Position " + position);
		return t;
	}

	if (position < 1) {
		View cell = LayoutInflater.from(context).inflate(
			R.layout.video_text_cell, null);
		TextView title = (TextView)cell.findViewById(R.id.title);
		TextView subtitle = (TextView)cell.findViewById(R.id.subtitle);
		title.setText(item.title);
		title.setTextColor(title_color);
		title.setTextSize(title_size);
		if (null != item.footer)
			subtitle.setText(item.footer);
		else
			subtitle.setText("");
		subtitle.setTextColor(text_color);
		subtitle.setTextSize(text_size);
		return cell;
	} else {
		View cell = LayoutInflater.from(context).inflate(
			R.layout.video_thumb_cell, null);
		WebImageView w = (WebImageView)cell.findViewById(R.id.image);
		w.setNoImageDrawable(R.drawable.video_loading);
		//ImageView i = (ImageView)w.getChildAt(1);
		//i.setScaleType(ImageView.ScaleType.FIT_XY);
		// This is quite wrong.
		//i.setImageDrawable(
			//getResources().getDrawable(R.drawable.video_loading));

		w.setImageUrl(preview_url);
		w.loadImage();
		return cell;
	}
}

@Override
public int getViewTypeCount()
{
	return 2;
}

@Override
public boolean hasStableIds()
{
	return true;
}

@Override
public boolean isEmpty()
{
	return null == item;
}

/** We don't want all cells in the video facade to be selectable.
 */
@Override
public boolean areAllItemsEnabled()
{
	return false;
}

/** Tells that only the thumbnail cell has interaction.
 */
@Override
public boolean isEnabled(int position)
{
	return position > 0;
}

}

}
