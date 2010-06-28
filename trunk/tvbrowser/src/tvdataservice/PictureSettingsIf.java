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
 */
package tvdataservice;

/**
 * An interface which is implemented by the
 * TvBrowserDataService, to let the picture
 * settings be customizable in the setup assistant.
 * 
 * @author René Mach
 */
public interface PictureSettingsIf {
  
  public static final int NO_PICTURES = 0;
  public static final int MORNING_PICTURES = 1;
  public static final int EVENING_PICTURES = 2;
  public static final int ALL_PICTURES = 3;
  
  public int getPictureState();
  
  public void setPictureState(int type);
}
