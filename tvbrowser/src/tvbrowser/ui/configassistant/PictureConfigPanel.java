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
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvdataservice.PictureSettingsIf;
import tvdataservice.SettingsPanel;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A panel with the settings for the picture
 * download of the TvBrowserDataService.
 * 
 * @author René Mach
 * @since 2.2.2
 */
public class PictureConfigPanel extends JPanel {
  private static final long serialVersionUID = 1L;

  protected static final Localizer mLocalizer = Localizer.getLocalizerFor(PictureConfigPanel.class);
  
  private JRadioButton mDownloadAll, mDownloadNoPictures, mDownloadEvening, mDownloadMorning;
  private SettingsPanel mTvBrowserDataServiceSettingsPanel;

  /**
   * Creates this panel.
   * 
   * @param update If this panel is for an update of TV-Browser.
   */
  public PictureConfigPanel(boolean update) {
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(new FormLayout(
        "fill:pref:grow, 10dlu, fill:300dlu:grow, fill:pref:grow",
        "fill:0dlu:grow, pref, 15dlu, pref, pref, pref, pref, 15dlu, pref, fill:0dlu:grow"), this);
    
    mDownloadAll = new JRadioButton(mLocalizer.msg("allPictures","Download pictures for all programs"));
    mDownloadNoPictures = new JRadioButton(mLocalizer.msg("noPictures","Don't download pictures"));
    mDownloadEvening = new JRadioButton(mLocalizer.msg("eveningPictures",
        "Download only pictures for the evening programs (4 PM to midnight)"));
    mDownloadMorning = new JRadioButton(mLocalizer.msg("morningPictures",
        "Download only pictures for the day programs (midnight to 4 PM)"));

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
      
      if(mTvBrowserDataServiceSettingsPanel instanceof PictureSettingsIf) {
        int i = ((PictureSettingsIf)mTvBrowserDataServiceSettingsPanel).getPictureState();
        
        mDownloadNoPictures.setSelected(i == PictureSettingsIf.NO_PICTURES);
        mDownloadAll.setSelected(i == PictureSettingsIf.ALL_PICTURES);
        mDownloadMorning.setSelected(i == PictureSettingsIf.MORNING_PICTURES);
        mDownloadEvening.setSelected(i == PictureSettingsIf.EVENING_PICTURES);
      }
    }
  }
  
  /**
   * Saves the picture settings for TvBrowserDataService.
   */
  public void saveSettings() {
    if(mTvBrowserDataServiceSettingsPanel instanceof PictureSettingsIf) {
      PictureSettingsIf pictureIf = ((PictureSettingsIf)mTvBrowserDataServiceSettingsPanel);
      
      if(mDownloadNoPictures.isSelected()) {
        pictureIf.setPictureState(PictureSettingsIf.NO_PICTURES);
      } else if(mDownloadMorning.isSelected()) {
        pictureIf.setPictureState(PictureSettingsIf.MORNING_PICTURES);
      } else if(mDownloadEvening.isSelected()) {
        pictureIf.setPictureState(PictureSettingsIf.EVENING_PICTURES);
      } else if(mDownloadAll.isSelected()) {
        pictureIf.setPictureState(PictureSettingsIf.ALL_PICTURES);
      }

      mTvBrowserDataServiceSettingsPanel.ok();
    }
  }
  
  /**
   * Gets if the picture downloading is activated.
   * 
   * @return If the picture downloading is activated.
   */
  public boolean isActivated() {
    return !mDownloadNoPictures.isSelected();
  }
}
