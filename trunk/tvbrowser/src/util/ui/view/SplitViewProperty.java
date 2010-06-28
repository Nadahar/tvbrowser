/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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


package util.ui.view;

import util.settings.PropertyManager;


public class SplitViewProperty extends ViewProperty {

    private Property mDefaultValue;
    private Property mCachedValue;
    private PropertyManager mManager;
    private String mKey;
    
    
    
    public SplitViewProperty(PropertyManager manager, String key, boolean verticalSplit, boolean leftComponentIsFixed, int fixedComponentWidth) {
      super(manager, key);
      mManager = manager;
      mKey = key;
      mDefaultValue = new Property(verticalSplit, leftComponentIsFixed, fixedComponentWidth);
      mCachedValue = null;
    }
    
    private SplitViewProperty(PropertyManager manager, String key, Property prop) {
      this(manager, key, prop.isVerticalSplit(), prop.isLeftComponentFix(), prop.getFixComponentWidth());
    }

    public SplitViewProperty getDefault() {
      SplitViewProperty prop = new SplitViewProperty(mManager, mKey, mDefaultValue);
      prop.mCachedValue = new Property(mDefaultValue);
      return prop;
    }
    
    private String createPropertyString(Property prop) {
      return prop.isVerticalSplit()+";"+prop.isLeftComponentFix()+";"+prop.getFixComponentWidth();
    }
    
    private Property getPropertyFromString(String str) {
      if (str == null) {
        return mDefaultValue;
      }
      String[] s = str.split(";");
      if (s.length ==3) {
        try {
          boolean verticalSplit = "true".equals(s[0]);
          boolean leftComponent = "true".equals(s[1]);
          int width = Integer.parseInt(s[2]);
          return new Property(verticalSplit, leftComponent, width);
        }catch(NumberFormatException e) {
          return mDefaultValue;
        }
      }
      return mDefaultValue;
    }
   
    public void setVerticalSplit(boolean verticalSplit) {
      if (mCachedValue == null) {
        mCachedValue = new Property(mDefaultValue);
      }
      mCachedValue.setVerticalSplit(verticalSplit);
      setProperty(createPropertyString(mCachedValue));
    }
    
    public void setLeftComponentFixed(boolean fixed) {
      if (mCachedValue == null) {
        mCachedValue = new Property(mDefaultValue);
      }
      mCachedValue.setLeftComponentFix(fixed);
      setProperty(createPropertyString(mCachedValue));
    }
 
    public void setFixedComponentWidth(int width) {
      if (mCachedValue == null) {
        mCachedValue = new Property(mDefaultValue);
      }
      mCachedValue.setFixComponentWidth(width);
      setProperty(createPropertyString(mCachedValue));
    }
    
    public boolean getVerticalSplit() {
      if (mCachedValue == null) {
        mCachedValue = getPropertyFromString(getProperty());
      }
      return mCachedValue.isVerticalSplit();
    }
    
    public boolean getLeftComponentFixed() {
      if (mCachedValue == null) {
        mCachedValue = getPropertyFromString(getProperty());
      }
      return mCachedValue.isLeftComponentFix();
    }
    
    
    public int getFixedComponentWidth() {
      if (mCachedValue == null) {
        mCachedValue = getPropertyFromString(getProperty());
      }
      return mCachedValue.getFixComponentWidth();
    }
    
    protected void clearCache() {
      mCachedValue = null;
        
    }
    
    public String toString() {
      if (mCachedValue == null) {
        return "not initialized";
      }
      return createPropertyString(mCachedValue);
    }
    
    private static class Property {
      private boolean mVerticalSplit;
      private boolean mLeftComponentIsFixed;
      private int mFixedComponentWidth;
      
      public Property(boolean verticalSplit, boolean leftComponentIsFixed, int width) {
        mVerticalSplit = verticalSplit;
        mLeftComponentIsFixed = leftComponentIsFixed;
        mFixedComponentWidth = width;
      }
      
      public Property(Property prop) {
        this(prop.mVerticalSplit, prop.mLeftComponentIsFixed, prop.mFixedComponentWidth);
      }
      
      public void setVerticalSplit(boolean v) {
        mVerticalSplit = v;
      }
      
      public boolean isVerticalSplit() {
        return mVerticalSplit;
      }
      
      public void setLeftComponentFix(boolean b) {
        mLeftComponentIsFixed = b;
      }
      
      public boolean isLeftComponentFix() {
        return mLeftComponentIsFixed;
      }
      
      public void setFixComponentWidth(int w) {
        mFixedComponentWidth = w;
      }
      
      public int getFixComponentWidth() {
        return mFixedComponentWidth;
      }
    }
    
}