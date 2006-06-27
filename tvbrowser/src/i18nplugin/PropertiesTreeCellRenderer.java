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
 *     $Date: 2006-06-05 21:02:43 +0200 (Mo, 05 Jun 2006) $
 *   $Author: darras $
 * $Revision: 2466 $
 */
package i18nplugin;

import java.awt.Color;
import java.awt.Component;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * This is a CellRenderer that highlights TreeNodes that have a incomplete translation
 * 
 * @author bodum
 */
public class PropertiesTreeCellRenderer extends DefaultTreeCellRenderer {

  private Locale mLocale;
  
  public PropertiesTreeCellRenderer(Locale locale) {
    mLocale = locale;
  }
  
  public void setCurrentLocale(Locale locale) {
    mLocale = locale;
  }
  
  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus); 

    if (value instanceof LanguageNodeIf) {
      LanguageNodeIf entry = (LanguageNodeIf) value;
      if (!entry.allTranslationsAvailableFor(mLocale) && !sel) {
        label.setForeground(Color.RED);
      }
    }
    
    return label;
  }
  
}