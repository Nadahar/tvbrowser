/*
 *******************************************************************
 *              TVBConsole plugin for TVBrowser                    *
 *                                                                 *
 * Copyright (C) 2010 Tomas Schackert.                             *
 * Contact koumori@web.de                                          *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 3 of the License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program, in a file called LICENSE in the top
 directory of the distribution; if not, write to
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 Boston, MA  02111-1307  USA
 
 *******************************************************************/
package aconsole.properties;

import java.awt.Color;
import java.util.Properties;


public class ColorProperty extends BaseProperty<Color>{
	Properties props;
	public ColorProperty(Properties props,String key,Color defaultval){
		super(key,defaultval);
		this.props=props;
	}
	  /**
     * Returns the Color in this Property
     * @return Color
     */
    public Color get() {
        if (mCachedValue==null) {
            String asString = props.getProperty(key);
            if (asString == null) {
                mCachedValue = defaultvalue;

            } else {
                try {
                    if ((asString.length() == 9) && (asString.startsWith("#"))) {
                        int red = Integer.parseInt(asString.substring(1,3),16);
                        int green = Integer.parseInt(asString.substring(3,5),16);
                        int blue = Integer.parseInt(asString.substring(5,7),16);
                        int alpha = Integer.parseInt(asString.substring(7,9),16);
                        
                        Color c = new Color(red, green, blue, alpha);
                        mCachedValue = c;
                    } else {
                        mCachedValue = defaultvalue;
                    }
                    //mCachedValue = Integer.parseInt(asString);
                } catch (Exception exc) {
                    exc.printStackTrace();
                    mCachedValue = defaultvalue;
                }
            }
        }
        return mCachedValue;
    }

    /**
     * Sets the Color in this Property
     * @param color Color
     */
    public void set(Color color) {
    	mCachedValue = color;
        
        String value = "#";

        value += addZero(Integer.toString(color.getRed(), 16),2);
        value += addZero(Integer.toString(color.getGreen(), 16),2);
        value += addZero(Integer.toString(color.getBlue(), 16),2);
        value += addZero(Integer.toString(color.getAlpha(), 16),2);

        props.setProperty(key,value);
        fireChanged();
    }
    private String addZero(String str, int len) {
        while (str.length() < len) {
            str = "0" + str;
        }
        return str;
    }
}
