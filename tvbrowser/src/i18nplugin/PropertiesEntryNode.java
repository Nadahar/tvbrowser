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

import java.util.Locale;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Entry for a Property
 * 
 * @author bodum
 */
public class PropertiesEntryNode extends DefaultMutableTreeNode implements LanguageNodeIf {

  /**
   * @param name Name of this Entry
   */
  public PropertiesEntryNode(String name) {
    super(name);
  }

  /**
   * @return Name of this Entry
   */
  public String getPropertyName() {
    return toString();
  }

  /*
   * (non-Javadoc)
   * @see i18nplugin.LanguageNodeIf#allTranslationsAvailableFor(java.util.Locale)
   */
  public boolean allTranslationsAvailableFor(Locale locale) {
    if (getParent() == null)
      return false;
    if (!(getParent() instanceof PropertiesNode))
      return false;
    return ((PropertiesNode) getParent()).containsKey(locale, getPropertyName());
  }

  /*
   * (non-Javadoc)
   * @see i18nplugin.LanguageNodeIf#save()
   */
  public void save() {
  }
  
}