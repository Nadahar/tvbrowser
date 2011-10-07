/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *     $Date: 2009-09-04 11:15:55 +0200 (Fr, 04 Sep 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5953 $
 */

package captureplugin;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import util.ui.Localizer;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.Sizes;

import devplugin.SettingsTab;

/**
 *	The Settings-Tab for the CapturePlugin,
 *	Actually this is a wrapper for a CapturePluginPanel
 */
public class CapturePluginSettingsTab implements SettingsTab {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CapturePluginSettingsTab.class);
  
    /** Cloned Data */
    private CapturePluginData mCloneData;
    /** Plugin */
    private CapturePlugin mPlugin;
    /** Frame-Owner */
    private JFrame mOwner;
    
    private CapturePluginPanel mPanel;
    
    private int mCurrentPanel = CapturePluginPanel.TAB_PROGRAMLIST;
    
    /**
     * Creates the SettingsTab
     * @param owner Frame-Owner
     * @param plugin the current instance of the plugin
     */
    public CapturePluginSettingsTab(JFrame owner, CapturePlugin plugin) {
        mPlugin = plugin;
        mOwner = owner;
        mCloneData = (CapturePluginData)plugin.getCapturePluginData().clone();
    }

    /**
     * Returns the PluginPanel
     * @return Panel
     */
    public JPanel createSettingsPanel() {
      mPanel = new CapturePluginPanel(mOwner, mCloneData);
      mPanel.setBorder(Borders.createEmptyBorder(Sizes.DLUY5,Sizes.DLUX5,Sizes.DLUY5,Sizes.DLUX5));
      mPanel.setSelectedTab(mCurrentPanel);
      return mPanel;
    }

    /**
     * Get the Icon
     * @return Icon
     */
    public Icon getIcon() {
        return CapturePlugin.getInstance().createImageIcon("mimetypes", "video-x-generic", 16);
    }

    /**
     * Returns the Title
     * @return Title
     */
    public String getTitle() {
        return mLocalizer.msg("Title", "Settings for the CapturePlugin");
    }

    /**
     * Save the Settings
     */
    public void saveSettings() {
        mCurrentPanel = mPanel.getSelectedTabIndex();
        mPlugin.setCapturePluginData(mCloneData);
        mPlugin.updateMarkedPrograms();
        mPanel.saveMarkingSettings();
    }

}