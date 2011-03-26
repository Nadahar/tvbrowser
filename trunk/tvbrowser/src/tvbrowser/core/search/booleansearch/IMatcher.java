/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.core.search.booleansearch;

/**
 * Jedes Element in dem Suchbaum erbt von dieser Klasse.
 * 
 * @author Gilson Laurent, pumpkin@gmx.de
 */
public interface IMatcher {

  /** Testet einen String. Gibt true zurück wenn die Bedinung erfüllt ist */
  public boolean matches(String s);

  /**
   * Initialisiert und optimiert den IMatcher. Da der IMatcher u.U. ausgetauscht
   * werden muss gibt die Methode einen (neuen) IMatcher zurück. Dieser ist zu
   * verwenden.
   */
  public IMatcher optimize();

}