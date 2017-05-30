/*
 * TV-Browser
 * Copyright (C) 2013 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date: 2017-03-04 23:44:53 +0100 (Sa, 04 Mär 2017) $
 *   $Author: ds10 $
 * $Revision: 8599 $
 */
package util.ui;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import devplugin.TabListener;

/**
 * A JPanel implementing TabListener to support focus
 * for the given default component, when tab is opened.
 * <p>
 * @author René Mach
 * @since 3.4.5
 */
public class TabListenerPanel extends JPanel implements TabListener {
  private Component mFocusOwner;
  private Component mDefaultFocusOwner;
  
  public void setDefaultFocusOwner(Component defaultFocusOwner) {
    mDefaultFocusOwner = defaultFocusOwner;
  }
  
  public void tabShown() {
    if(mFocusOwner == null) {
      if(mDefaultFocusOwner != null) {
        SwingUtilities.invokeLater(() -> {
          mDefaultFocusOwner.requestFocusInWindow();
          
          if(mDefaultFocusOwner instanceof JList && ((JList<?>) mDefaultFocusOwner).getModel().getSize() > 0
              && ((JList<?>) mDefaultFocusOwner).getSelectedIndex() == -1) {
            ((JList<?>)mDefaultFocusOwner).setSelectedIndex(0);
            ((JList<?>)mDefaultFocusOwner).ensureIndexIsVisible(0);
          }
        });
      }
    }
    else {
      SwingUtilities.invokeLater(() -> {
        mFocusOwner.requestFocusInWindow();
        
        if(mFocusOwner instanceof JList && ((JList<?>) mFocusOwner).getModel().getSize() > 0
            && ((JList<?>) mFocusOwner).getSelectedIndex() == -1) {
          ((JList<?>)mFocusOwner).setSelectedIndex(0);
          ((JList<?>)mFocusOwner).ensureIndexIsVisible(0);
        }
      });
    }
  }
  
  public void tabHidden(Component mostRecent) {
    mFocusOwner = null;
    
    for(int i = 0; i < getComponentCount(); i++) {
      if(getComponent(i).equals(mostRecent)) {
        mFocusOwner = mostRecent;
      }
    }
  }
}
