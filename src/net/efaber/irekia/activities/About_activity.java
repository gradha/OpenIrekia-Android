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
import net.efaber.irekia.Irekia_default_activity;
import net.efaber.irekia.R;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/** Simple "hi there" about screen.
 */
public class About_activity extends Irekia_default_activity
{
/** Called when the activity is first created. */
@Override
public void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	setContentView(R.layout.about);

	final Button button = (Button)findViewById(R.id.close_button);
	button.setText(I18n.e(Embedded_string.CLOSE));
	button.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) { finish(); } });

	setTitle(I18n.e(Integer.decode(getTitle().toString())));

	TextView text = (TextView)findViewById(R.id.version_number);
	try {
		text.setText(getPackageManager().getPackageInfo(getPackageName(),
			0).versionName);
	} catch (NameNotFoundException e) {
		text.setText("");
	}
}

}
