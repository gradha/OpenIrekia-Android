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

import net.efaber.irekia.models.Content_item;
import net.efaber.irekia.zerg.Overlord;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class Navigation_buttons
{
/// The buttons used to navigate news. Might be hidden.
public final Button prev, next;

/** Called when the activity is first created. */
public Navigation_buttons(Activity activity, View.OnClickListener listener)
{
	prev = (Button)activity.findViewById(R.id.prev_button);
	prev.setOnClickListener(listener);
	next = (Button)activity.findViewById(R.id.next_button);
	next.setOnClickListener(listener);
}

/** Updates the state of the navigation buttons.
 * This checks the current overlord/item and enables/disables the buttons and
 * makes them visible/invisible.
 */
public void update(final Overlord overlord, final Content_item item)
{
	if (null == item || null == overlord) {
		prev.setClickable(false);
		next.setClickable(false);
	} else {
		prev.setClickable(null != overlord.get_prev(item));
		next.setClickable(null != overlord.get_next(item));
	}

	prev.setVisibility(prev.isClickable() ?  View.VISIBLE : View.INVISIBLE);
	next.setVisibility(next.isClickable() ?  View.VISIBLE : View.INVISIBLE);
}

}
