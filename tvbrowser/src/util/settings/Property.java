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
package util.settings;

import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public abstract class Property {
  
  private PropertyManager mManager;
  private String mKey;
  private Vector mChangeList;
  
  public Property(PropertyManager manager, String key) {
    mManager = manager;
    mKey = key;
    mChangeList = new Vector();
    // Register this property
    mManager.addProperty(this);
  }


  protected void setProperty(String value) {
    mManager.setProperty(mKey, value);
    fireChangeEvent();
  }
  
  
  protected String getProperty() {
    return mManager.getProperty(mKey);
  }
  
  
  public String getKey() {
    return mKey;
  }
  
  
  public void addChangeListener(ChangeListener l) {
      mChangeList.add(l);
  }
  
  public void removeChangeListener(ChangeListener l) {
      mChangeList.remove(l);
  }

  public void fireChangeEvent() {
      for (int i = 0; i < mChangeList.size(); i++) {
          ChangeListener l = (ChangeListener) mChangeList.get(i);
          l.stateChanged(new ChangeEvent(this));
      }
  }
  
  protected abstract void clearCache();

}