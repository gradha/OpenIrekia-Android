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

import net.efaber.irekia.R;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.droidfu.activities.BetterDefaultActivity;

public class Video_player_activity extends BetterDefaultActivity
	implements OnPreparedListener, OnVideoSizeChangedListener,
		OnErrorListener, OnCompletionListener
{
/// Name of the bundle key to pass the url param.
public static final String IOS_URL_PARAM = "IOS_URL_PARAM";

/// Name of the bundle key to pass the progressive url param.
public static final String PROGRESSIVE_URL_PARAM = "PROGRESSIVE_URL_PARAM";

private ProgressBar progress_bar;
private Custom_video_view video_view;
private int screen_width, screen_height;
private int media_width, media_height;
private boolean landscape;

/// Store the URL of the video we want to play to retry with external activity.
private String url;

/** Called when the activity is first created. */
@Override
public void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	setContentView(R.layout.video_player);

	progress_bar = (ProgressBar)findViewById(R.id.progress_bar);
	getWindow().clearFlags(WindowManager
		.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);

	// Get the size of the device, will be our maximum.
	Display display = getWindowManager().getDefaultDisplay();
	screen_width = display.getWidth();
	screen_height = display.getHeight();

	video_view = (Custom_video_view)findViewById(R.id.surface_view);
	video_view.setOnCompletionListener(this);
	video_view.setOnPreparedListener(this);
	video_view.setOnErrorListener(this);
	resize();

	Bundle extras = getIntent().getExtras();
	url = extras.getString(IOS_URL_PARAM);
	if (null != url)
		url = url.replace("/ts/", "/html5/").replace("m3u8", "m4v");

	final String other_url = extras.getString(PROGRESSIVE_URL_PARAM);
	if (null != other_url)
		url = other_url;

	if (null != url) {
		video_view.setVideoURI(Uri.parse(url));
		video_view.setMediaController(new MediaController(this));
		video_view.requestFocus();
	} else {
		finish();
	}
}

/** The user rotated the screen.
 * Since we specified in the AndroidManifest.xml that we want to handle our
 * own orientation changes, we resize the screen in function of being
 * portrait or landscape.
 */
@Override
public void onConfigurationChanged(Configuration newConfig)
{
	super.onConfigurationChanged(newConfig);

	landscape = (Configuration.ORIENTATION_LANDSCAPE == newConfig.orientation);
	resize();
}

/** Resizes the surfaces to fill the screen.
 * If the media_width and media_height variables are zero, that means we
 * don't know yet the size of the video, so we just resize to fill the
 * whole screen. Otherwise we scale maintaining the aspect ratio to fill
 * it. The gravity of the layout leaves us centered.
 */
public void resize()
{
	int w = landscape ? screen_height : screen_width;
	int h = landscape ? screen_width : screen_height;

	// If we have the media, calculate best scaling inside bounds.
	if (media_width > 0 && media_height > 0) {
		final float max_w = w;
		final float max_h = h;
		float temp_w = media_width;
		float temp_h = media_height;
		float factor = max_w / temp_w;
		temp_w *= factor;
		temp_h *= factor;

		// If we went above the height limit, scale down.
		if (temp_h > max_h) {
			factor = max_h / temp_h;
			temp_w *= factor;
			temp_h *= factor;
		}

		w = (int)temp_w;
		h = (int)temp_h;
	}
	video_view.setDimensions(w, h);
	video_view.getHolder().setFixedSize(w, h);
}

/** The media player is prepared.
 * Sometimes the media player doesn't have yet the sizes of the video. If
 * that is the case, register a video size change listener, otherwise
 * resize ourselves and start the video.
 */
public void onPrepared(MediaPlayer mp)
{
	media_width = mp.getVideoWidth();
	media_height = mp.getVideoHeight();
	//log.i("onPrepared " + media_width + "x" + media_height);

	if (media_width > 0 && media_height > 0) {
		// We got a size, resize and start the video.
		resize();
		video_view.start();
		progress_bar.setVisibility(View.GONE);
	} else {
		// Yuck, no sizes yet? Register a callback.
		mp.setOnVideoSizeChangedListener(this);
	}
}

/** Called when the media player knows the video size.
 * We use this to store the size of the media and start the video, then
 * remove the listener since the media won't change size.
 */
@Override
public void onVideoSizeChanged(MediaPlayer mp, int width, int height)
{
	media_width = width;
	media_height = height;
	//log.i("onVideoSizeChanged " + media_width + "x" + media_height);

	if (media_width > 0 && media_height > 0) {
		resize();
		video_view.start();
		progress_bar.setVisibility(View.GONE);
		mp.setOnVideoSizeChangedListener(null);
	}
}

/** We got an awful error message. Process it.
 * Since some terminals have bad embedded video implementations, we will try to
 * show the video using an external player before really giving up.
 */
@Override
public boolean onError(MediaPlayer mp, int what, int extra)
{
	try {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse(url), "video/mp4");
		startActivity(intent);
		finish();
		return true;
	} catch (ActivityNotFoundException e) {
		String message = "Video error";
		switch (what) {
			case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
				message = "Video error: not progressive (" + extra + ")";
				break;
			case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
				message = "Video error: media player died (" + extra + "). " +
					"Please restart the application";
				break;
			case MediaPlayer.MEDIA_ERROR_UNKNOWN:
				message = "Video error: unknown (" + extra + ")";
				break;
			default:
				message = "Video error " + what + " (" + extra + ")";
				break;
		}
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		finish();
		return false;
	}
}

/** Playback finished, quit activity.
 */
public void onCompletion(MediaPlayer mp)
{
	finish();
}

}
