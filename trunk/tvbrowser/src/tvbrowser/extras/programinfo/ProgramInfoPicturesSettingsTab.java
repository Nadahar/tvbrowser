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
 */
package tvbrowser.extras.programinfo;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import util.ui.Localizer;
import util.ui.PictureSettingsPanel;

import devplugin.SettingsTab;

/**
 * The settings tab for the picture showing the the dialog.
 * 
 * @author René Mach
 * @since 2.2.2
 */
public class ProgramInfoPicturesSettingsTab implements SettingsTab {  
  /** Picture settings */
  private PictureSettingsPanel mPictureSettings;
  private JCheckBox mZoomEnabled;
  private JSpinner mZoomValue;
  
  public JPanel createSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,12dlu,pref,2dlu,pref:grow","pref,2dlu,pref"));
    
    pb.add(mZoomEnabled = new JCheckBox(ProgramInfo.mLocalizer.msg("scaleImage","Scale picture:"), ProgramInfo.getInstance().getProperty("zoom","false").compareTo("true") == 0), cc.xyw(2,1,4));
    pb.add(mZoomValue = new JSpinner(new SpinnerNumberModel(Integer.parseInt(ProgramInfo.getInstance().getProperty("zoomValue","100")),50,300,1)), cc.xy(3,3));
    final JLabel label = pb.addLabel("%",cc.xy(5,3));

    mZoomEnabled.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mZoomValue.setEnabled(mZoomEnabled.isSelected());
        label.setEnabled(mZoomEnabled.isSelected());
      }
    });
    
    mZoomValue.setEnabled(mZoomEnabled.isSelected());
    label.setEnabled(mZoomEnabled.isSelected());
    
    mPictureSettings = new PictureSettingsPanel(ProgramInfo.getInstance().getProgramPanelSettings(), true, true , pb.getPanel());
    
    return mPictureSettings;
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return Localizer.getLocalization(Localizer.I18N_PICTURES);
  }

  public void saveSettings() {
    ProgramInfo.getInstance().getSettings().setProperty("pictureType", String.valueOf(mPictureSettings.getPictureShowingType()));
    ProgramInfo.getInstance().getSettings().setProperty("pictureTimeRangeStart", String.valueOf(mPictureSettings.getPictureTimeRangeStart()));
    ProgramInfo.getInstance().getSettings().setProperty("pictureTimeRangeEnd", String.valueOf(mPictureSettings.getPictureTimeRangeEnd()));
    ProgramInfo.getInstance().getSettings().setProperty("pictureShowsDescription", String.valueOf(mPictureSettings.getPictureIsShowingDescription()));
    ProgramInfo.getInstance().getSettings().setProperty("pictureDuration", String.valueOf(mPictureSettings.getPictureDurationTime()));
    ProgramInfo.getInstance().getSettings().setProperty("zoom", String.valueOf(mZoomEnabled.isSelected()));
    ProgramInfo.getInstance().getSettings().setProperty("zoomValue", String.valueOf(mZoomValue.getValue()));
    
    if(mPictureSettings.getPictureShowingType() == PictureSettingsPanel.SHOW_FOR_PLUGINS) {
      StringBuffer temp = new StringBuffer();
      
      String[] plugins = mPictureSettings.getClientPluginIds();
      
      for(int i = 0; i < plugins.length; i++)
        temp.append(plugins[i]).append(";;");
      
      if(temp.toString().endsWith(";;"))
        temp.delete(temp.length()-2,temp.length());
      
      ProgramInfo.getInstance().getSettings().setProperty("clientPlugins", temp.toString());
    }
      
  }

}
