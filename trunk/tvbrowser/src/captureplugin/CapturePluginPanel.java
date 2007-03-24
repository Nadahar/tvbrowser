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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package captureplugin;

import captureplugin.tabs.DevicePanel;
import captureplugin.tabs.ProgramListPanel;
import util.ui.Localizer;
import util.ui.PictureSettingsPanel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Window;

/**
 * This is a Panel for changing the Settings in the Plugin
 */
public class CapturePluginPanel extends JPanel {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CapturePluginPanel.class);

    /** Tab for Programlist */
    public static final int TAB_PROGRAMLIST = 0;
    /** Tab for PictureSettings */
    public static final int TAB_PICTURE_SETTINGS = 1;
    /** Tab for DeviceList */
    public static final int TAB_DEVICELIST = 2;

    /** GUI */
    private JTabbedPane mTabPane;

    private PictureSettingsPanel mPictureSettings;
    
    /**
     * Creates the Panel
     * 
     * @param data Data to use
     */
    public CapturePluginPanel(Window owner, CapturePluginData data) {
        this.setLayout(new BorderLayout());

        mTabPane = new JTabbedPane();

        mTabPane.addTab(mLocalizer.msg("ProgramList", "Programlist"), new ProgramListPanel(owner, data));
        
        mTabPane.addTab(mLocalizer.msg("Devices", "Devices"), new DevicePanel(owner, data));

        mPictureSettings = new PictureSettingsPanel(CapturePlugin.getInstance().getProgramPanelSettings(),true,true);
        
        final JScrollPane scrollPane = new JScrollPane(mPictureSettings);
        scrollPane.setBorder(null);
        
        mTabPane.addTab(Localizer.getLocalization(Localizer.I18N_PICTURES), scrollPane);
        
        // Tabbed - Pane
        //	this.add(tabPane,BorderLayout.CENTER);
        this.add(mTabPane, BorderLayout.CENTER);
        
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            scrollPane.getVerticalScrollBar().setValue(0);
          }
        });
    }


    /**
     * Sets the selected Tab
     * @param num Number of Tab (use TAB_* )
     */
    public void setSelectedTab(int num) {
        mTabPane.setSelectedIndex(num);
    }
    
    /**
     * Gets the index of the selected tab.
     * 
     * @return The index of the selected tab.
     */
    public int getSelectedTabIndex() {
      return mTabPane.getSelectedIndex();
    }
    
    /**
     * Saves the picture settings for the program list
     */
    public void savePictureSettings() {
      CapturePlugin.getInstance().storeSettings().setProperty("pictureType", String.valueOf(mPictureSettings.getPictureShowingType()));
      CapturePlugin.getInstance().storeSettings().setProperty("pictureTimeRangeStart", String.valueOf(mPictureSettings.getPictureTimeRangeStart()));
      CapturePlugin.getInstance().storeSettings().setProperty("pictureTimeRangeEnd", String.valueOf(mPictureSettings.getPictureTimeRangeEnd()));
      CapturePlugin.getInstance().storeSettings().setProperty("pictureShowsDescription", String.valueOf(mPictureSettings.getPictureIsShowingDescription()));
      CapturePlugin.getInstance().storeSettings().setProperty("pictureDuration", String.valueOf(mPictureSettings.getPictureDurationTime()));
      
      if(PictureSettingsPanel.typeContainsType(mPictureSettings.getPictureShowingType(),PictureSettingsPanel.SHOW_FOR_PLUGINS)) {
        StringBuffer temp = new StringBuffer();
        
        String[] plugins = mPictureSettings.getClientPluginIds();
        
        for(int i = 0; i < plugins.length; i++)
          temp.append(plugins[i]).append(";;");
        
        if(temp.toString().endsWith(";;"))
          temp.delete(temp.length()-2,temp.length());
        
        CapturePlugin.getInstance().storeSettings().setProperty("picturePlugins", temp.toString());
      }
    }
}