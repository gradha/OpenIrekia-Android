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

package net.efaber.irekia.models;

/** Special interface required by Sharekit.
 * To be able to pass any kind of objects to the Sharekit class, implement
 * these required methods. These methods will be used to replace the HTML like
 * tags found in protocol appdata. Please see section "Email text substitution"
 * in the protocol specification.
 */
public interface Sharing_tags
{
	/** Gets the photo description of an item.
	 * @return Returns the string with the description or the empty string.
	 * This method should never return null.
	 */
	public String get_photo_desc();

	/** Gets the title of an item.
	 * @return Returns the string with the title or the empty string.  This
	 * method should never return null.
	 */
	public String get_title();

	/** Gets the sharing url of an item.
	 * An item may have different urls, the one this method has to return is
	 * the publicly available internet URL, since Sharekit is going to embed it
	 * into emails, tweets and stuff.
	 *
	 * @return Returns the string with the url or the empty string.
	 * This method should never return null.
	 */
	public String get_url();
}
