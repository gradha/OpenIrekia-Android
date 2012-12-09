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

package net.efaber.irekia.news;

import net.efaber.irekia.Irekia;
import net.efaber.irekia.models.Section_state;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.View;

/** Custom class to draw our rows without constructing views.
 * Everything is done dynamically inside onDraw().
 */
public class Section_row extends View
{
/// Defined height of a header row.
public static final float ROW_HEIGHT = 30;
/// Defined size of the font in points.
public static final float FONT_SIZE = 18;

/// Cache of the text paint.
private TextPaint paint;
/// Cached size of the font.
private float font_ascent;
/// The section object we are tracking with this header object.
private Section_state section;
/// Pointer to the view configuration information.
private News_data data;

/** Constructor.
 * Pass the element position to differentiate rows. A better constructor would
 * pass the section or something.
 */
public Section_row(Context context, News_data data)
{
	super(context);
	this.data = data;
}

/** Simple section setter.
 * Required to refresh internally cached values for drawing.
 */
public void set(Section_state section)
{
	this.section = section;
	paint = null;
	postInvalidate();
}

/** Indicates if this view is opaque.
 * @return This will always return true, in a hope it gets optimised.
 */
@Override
public boolean isOpaque()
{
	return true;
}

/** Draws the canvas.
 */
@Override
public void onDraw(Canvas canvas)
{
	super.onDraw(canvas);

	if (null == section)
		return;

	if (null == paint) {
		paint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
		paint.setTextSize(FONT_SIZE * Irekia.font_density);
		Paint.FontMetrics size = paint.getFontMetrics();
		font_ascent = (-size.ascent + size.descent) / 3.0f;
	}

	// Invert the color selection if we are being pressed or iterated over.
	if (isSelected() || isPressed()) {
		canvas.drawColor(data.back_highlight_color);
		paint.setColor(data.title_color);
	} else {
		if (section.collapsed) {
			canvas.drawColor(data.section_collapsed_back_color);
			paint.setColor(data.section_collapsed_text_color);
		} else {
			canvas.drawColor(data.section_expanded_back_color);
			paint.setColor(data.section_expanded_text_color);
		}
	}

	// Get the screen's density scale
	final float spadding = data.padding * Irekia.density;

	// Calculate text position for the first title bold line.
	float x = spadding;
	float y = (ROW_HEIGHT * Irekia.density * 0.5f) + font_ascent;

	String text = section.name;
	if (section.show_count)
		text += " (" + section.items.size() + ")";
	canvas.drawText(text, x, y, paint);
}

@Override
public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
{
	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	final int width = MeasureSpec.getSize(widthMeasureSpec);
	if (null == section)
		setMeasuredDimension(width, 0);
	else
		setMeasuredDimension(width, (int)(ROW_HEIGHT * Irekia.density + 0.5f));
}

}
