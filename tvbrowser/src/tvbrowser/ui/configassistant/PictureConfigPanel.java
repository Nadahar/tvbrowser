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
 */
package tvbrowser.ui.configassistant;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvdataservice.SettingsPanel;
import util.ui.Localizer;
import util.ui.UiUtilities;

/**
 * A panel with the settings for the picture
 * download of the TvBrowserDataService.
 * 
 * @author René Mach
 * @since 2.2.2
 */
public class PictureConfigPanel extends JPanel {
  private static final long serialVersionUID = 1L;

  protected static Localizer mLocalizer = Localizer.getLocalizerFor(PictureConfigPanel.class);
  
  private JRadioButton mDownloadAll, mDownloadNoPictures, mDownloadEvening, mDownloadMorning;
  private SettingsPanel mTvBrowserDataServiceSettingsPanel;
  private JCheckBox mMorningPictureCheckBox, mEveningPictureCheckBox;

  /**
   * Creates this panel.
   *  
   * @param update If this panel is for an update of TV-Browser.
   */
  public PictureConfigPanel(boolean update) {
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(new FormLayout(
        "fill:pref:grow, 10dlu, fill:300dlu:grow, fill:pref:grow",
        "fill:pref:grow, pref, 15dlu, pref, pref, pref, pref 15dlu, pref, fill:pref:grow"), this);    
    
    mDownloadAll = new JRadioButton(mLocalizer.msg("allPictures","Download pictures for all programs"));
    mDownloadNoPictures = new JRadioButton(mLocalizer.msg("noPictures","Don't download pictures"));
    mDownloadEvening = new JRadioButton(mLocalizer.msg("eveningPictures","Download only pictures for the evening programs"));
    mDownloadMorning = new JRadioButton(mLocalizer.msg("morningPictures","Download only pictures for the night and day programs"));

    pb.add(UiUtilities.createHtmlHelpTextArea((update ? mLocalizer.msg("preambelUpdate", "Preambel") : "") + mLocalizer.msg("preambel", "Preambel")), cc.xyw(2,2,2));
    
    pb.add(mDownloadAll, cc.xy(3,4));
    pb.add(mDownloadNoPictures, cc.xy(3,5));
    pb.add(mDownloadEvening, cc.xy(3,6));
    pb.add(mDownloadMorning, cc.xy(3,7));
    
    pb.add(UiUtilities.createHtmlHelpTextArea(mLocalizer.msg(update ? "closingUpdate" : "closing", "Closing")), cc.xyw(2,9,2));
    
    ButtonGroup bg = new ButtonGroup();
    
    bg.add(mDownloadAll);
    bg.add(mDownloadNoPictures);
    bg.add(mDownloadEvening);
    bg.add(mDownloadMorning);
    
    TvDataServiceProxy[] services = TvDataServiceProxyManager.getInstance().getTvDataServices(new String[] {"tvbrowserdataservice.TvBrowserDataService"});
    
    if(services != null && services.length == 1) {
      mTvBrowserDataServiceSettingsPanel = services[0].getSettingsPanel();
      
      if(mTvBrowserDataServiceSettingsPanel instanceof tvbrowserdataservice.TvBrowserDataServiceSettingsPanel) {
        mMorningPictureCheckBox = ((tvbrowserdataservice.TvBrowserDataServiceSettingsPanel)mTvBrowserDataServiceSettingsPanel).getMorningPictureCheckBox();
        mEveningPictureCheckBox = ((tvbrowserdataservice.TvBrowserDataServiceSettingsPanel)mTvBrowserDataServiceSettingsPanel).getEveningPictureCheckBox();
        
        if(mMorningPictureCheckBox.isSelected() && mEveningPictureCheckBox.isSelected())
          mDownloadAll.setSelected(true);
        else if(mMorningPictureCheckBox.isSelected())
          mDownloadMorning.setSelected(true);
        else if(mEveningPictureCheckBox.isSelected())
          mDownloadEvening.setSelected(true);
        else
          mDownloadNoPictures.setSelected(true);
      }
    }
  }
  
  /**
   * Saves the picture settings for TvBrowserDataService.
   */
  public void saveSettings() {
    if(mMorningPictureCheckBox != null) {      
      mMorningPictureCheckBox.setSelected(mDownloadAll.isSelected() || mDownloadMorning.isSelected());
      mEveningPictureCheckBox.setSelected(mDownloadAll.isSelected() || mDownloadEvening.isSelected());
      
      mTvBrowserDataServiceSettingsPanel.ok();
    }
  }
}
