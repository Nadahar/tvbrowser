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
package util.tvdataservice;

import java.util.Properties;

import tvdataservice.SettingsPanel;
import tvdataservice.TvDataService;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 * @deprecated Please use <code>util.AbstractTvDataService</code>
 */
public abstract class AbstractTvDataService implements TvDataService {

  private Properties mSettings;



  public AbstractTvDataService() {
  }


  /**
   * Gets the settings.
   */
  protected Properties getSettings() {
    if (mSettings == null) {
      mSettings = new Properties();
    }
    return mSettings;
  }


  /**
   * Called by the host-application during start-up. Implements this method to
   * load your dataservices settings from the file system.
   */
  public void loadSettings(Properties settings) {
    mSettings = settings;
  }


  /**
   * Called by the host-application during shut-down. Implements this method to
   * store your dataservices settings to the file system.
   */
  public Properties storeSettings() {
    return mSettings;
  }


  public boolean hasSettingsPanel() {
    return false;
  }


  public SettingsPanel getSettingsPanel() {
    return null;
  }
  
  

}
