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

import java.awt.Color;

/**
 * This Property stores a Color
 */
public final class ColorProperty extends Property {

    /** The Default Color */
    private Color mDefaultColor;

    /** Is the Cache filled ? */
    private boolean mIsCacheFilled;

    /** The Color of this Property */
    private Color mCachedValue;

    /**
     * Creates the Color
     * @param manager the Manager for this Property
     * @param key the Key
     * @param defaultvalue the Default Color
     */
    public ColorProperty(PropertyManager manager, String key, Color defaultvalue) {
        super(manager, key);
        mDefaultColor = defaultvalue;
    }

    /**
     * Returns the Color in this Property
     * @return Color
     */
    public Color getColor() {
        
        if (!mIsCacheFilled) {
            String asString = getProperty();
            if (asString == null) {
                mCachedValue = mDefaultColor;

            } else {
                try {
                    
                    if ((asString.length() == 9) && (asString.startsWith("#"))) {
                        
                        int red = Integer.parseInt(asString.substring(1,2));
                        int green = Integer.parseInt(asString.substring(3,4));
                        int blue = Integer.parseInt(asString.substring(5,6));
                        int alpha = Integer.parseInt(asString.substring(7,8));
                        
                        Color c = new Color(red, green, blue, alpha);
                    } else {
                        mCachedValue = mDefaultColor;
                    }
                    //mCachedValue = Integer.parseInt(asString);
                } catch (Exception exc) {
                    mCachedValue = mDefaultColor;
                }
            }

            mIsCacheFilled = true;
        }

        return mCachedValue;
    }

    /**
     * Sets the Color in this Property
     * @param color Color
     */
    public void setColor(Color color) {
        if (color == mDefaultColor) {
            setProperty(null);
        } else {

            String value = "#";

            value += Integer.toString(color.getRed(), 16);
            value += Integer.toString(color.getGreen(), 16);
            value += Integer.toString(color.getBlue(), 16);
            value += Integer.toString(color.getAlpha(), 16);

            setProperty(value);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see util.settings.Property#clearCache()
     */
    protected void clearCache() {
        mIsCacheFilled = false;
    }

}