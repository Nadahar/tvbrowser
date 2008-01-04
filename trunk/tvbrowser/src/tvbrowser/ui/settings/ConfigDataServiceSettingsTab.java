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


package tvbrowser.ui.settings;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvdataservice.SettingsPanel;

import com.jgoodies.forms.factories.Borders;

public class ConfigDataServiceSettingsTab implements devplugin.CancelableSettingsTab {

  private TvDataServiceProxy mDataService;
  private SettingsPanel mSettingsPanel;

  public ConfigDataServiceSettingsTab(TvDataServiceProxy dataService) {
    mDataService=dataService;
    mSettingsPanel=mDataService.getSettingsPanel();
  }

  public JPanel createSettingsPanel() {

    JPanel mainPn=new JPanel(new BorderLayout());
    mainPn.setBorder(Borders.DIALOG_BORDER);
    PluginInfoPanel infoPn=new PluginInfoPanel(mSettingsPanel != null);
    infoPn.setDefaultBorder(false);
    infoPn.setPluginInfo(mDataService.getInfo());
    mainPn.add(infoPn,BorderLayout.NORTH);

    if (mSettingsPanel!=null) {
      mainPn.add(mSettingsPanel,BorderLayout.CENTER);
    }
    return mainPn;
  }


    /**
     * Called by the host-application, if the user wants to save the settings.
     */
    public void saveSettings() {
      if (mSettingsPanel!=null) {
        mSettingsPanel.ok();
      }
    }


    /**
     * Returns the name of the tab-sheet.
     */
    public Icon getIcon() {
      return new ImageIcon("imgs/Jar16.gif");
    }


    /**
     * Returns the title of the tab-sheet.
     */
    public String getTitle() {
      return mDataService.getInfo().getName();
    }

    /**
     * Cancel was pressed
     */
    public void cancel() {
        mSettingsPanel.cancel();
    }
}