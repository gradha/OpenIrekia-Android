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

package net.efaber.irekia.gallery;

import java.util.ArrayList;
import java.util.List;

import net.efaber.irekia.Irekia;
import net.efaber.irekia.R;
import net.efaber.irekia.models.Content_item;
import smartlistadapter.SmartList;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.github.droidfu.adapters.WebGalleryAdapter;
import com.github.droidfu.widgets.WebImageView;

/** Modification of a web gallery adapter.
 * Our version simply deals with converting Overlord's items into lists of url
 * strings and configuring the view before returning it to the grid, setting
 * our custom sizes as specified in the Gallery_data.
 */
public class Gallery_adapter extends WebGalleryAdapter
{
/// Parameters for the look of the images.
private Gallery_data data;
/// Set to true if you are using the adapter inside a gallery, not a grid.
public boolean fullscreen;

// Required explicit constructor.
public Gallery_adapter(Context context, Gallery_data data)
{
	super(context, null, R.drawable.loading, R.drawable.broken_icon);
	this.data = data;
}

/** Sets the image urls for the adapter.
 * After calling this you are required to call notifyDataSetChanged() yourself.
 */
public void set_items(SmartList<Content_item> items)
{
	List<String> urls = new ArrayList<String>();
	if (fullscreen)
		for (Content_item item : items)
			urls.add(item.url);
	else
		for (Content_item item : items)
			urls.add(item.image);

	setImageUrls(urls);
}

/** Set our size settings before letting the view loose.
 */
protected void onGetView(int position, View convertView, ViewGroup parent)
{
	FrameLayout layout = (FrameLayout)convertView;
	if (!fullscreen)
		layout.setLayoutParams(new GridView.LayoutParams(
			(int)(data.image_w * Irekia.density),
			(int)(data.image_h * Irekia.density)));
	WebImageView w = (WebImageView)layout.getChildAt(0);
	ImageView i = (ImageView)w.getChildAt(1);
	if (data.stretch_images || fullscreen)
		i.setScaleType(ImageView.ScaleType.FIT_XY);
	else
		i.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	i.setPadding(0, 0, 0, 0);
}

}
