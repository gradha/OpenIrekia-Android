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

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/** Code from
 * http://stackoverflow.com/questions/4434027/android-videoview-orientation-change-with-buffered-video
 */

public class Custom_video_view extends VideoView
{
	private int mForceHeight = 0;
	private int mForceWidth = 0;

	public Custom_video_view(Context context)
	{
		super(context);
	}

	public Custom_video_view(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public Custom_video_view(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public void setDimensions(int w, int h)
	{
		this.mForceHeight = h;
		this.mForceWidth = w;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(mForceWidth, mForceHeight);
	}
}
