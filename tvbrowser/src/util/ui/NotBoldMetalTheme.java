/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package util.ui;

import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;

/**
 * For using this theme, yust add the following line to your application:
 * <pre>
 * javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new NotBoldMetalTheme());
 * </pre>
 *
 * @author Til Schneider, www.murfman.de
 */
public class NotBoldMetalTheme extends DefaultMetalTheme {
  
  public String getName() { return "NotBoldMetalTheme"; }

  private final FontUIResource controlFont = new FontUIResource("Dialog", Font.PLAIN, 12);
  private final FontUIResource systemFont = new FontUIResource("Dialog", Font.PLAIN, 12);
  private final FontUIResource userFont = new FontUIResource("Dialog", Font.PLAIN, 12);
  private final FontUIResource smallFont = new FontUIResource("Dialog", Font.PLAIN, 10);

  public FontUIResource getControlTextFont() { return controlFont;}
  public FontUIResource getSystemTextFont() { return systemFont;}
  public FontUIResource getUserTextFont() { return userFont;}
  public FontUIResource getMenuTextFont() { return controlFont;}
  public FontUIResource getWindowTitleFont() { return controlFont;}
  public FontUIResource getSubTextFont() { return smallFont;}

}
