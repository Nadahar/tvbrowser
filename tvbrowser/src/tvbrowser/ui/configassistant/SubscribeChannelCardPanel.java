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

package tvbrowser.ui.configassistant;

import java.awt.BorderLayout;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import tvbrowser.core.ChannelList;
import tvbrowser.ui.settings.ChannelsSettingsTab;

class SubscribeChannelCardPanel extends AbstractCardPanel {

  private ChannelsSettingsTab mChannelsSettingsTab;

  private JPanel mContent;

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(SubscribeChannelCardPanel.class);

  public SubscribeChannelCardPanel(PrevNextButtons btns) {
    super(btns);
    mContent = new JPanel(new BorderLayout());
  }

  public JPanel getPanel() {
    return mContent;
  }

  public boolean onNext() {
    mChannelsSettingsTab.saveSettingsWithoutDataUpdate();
    if (ChannelList.getNumberOfSubscribedChannels() == 0) {
      JOptionPane.showMessageDialog(mContent,
          mLocalizer.msg("noChannelsSelected", "There are no channels selected..."), mLocalizer.msg(
              "noChannelsSelected.Title", "no selected channels"), JOptionPane.INFORMATION_MESSAGE);
      return false;
    }

    return true;
  }

  public boolean onPrev() {
    mChannelsSettingsTab.saveSettings();
    return true;
  }

  public void onShow() {
    super.onShow();
    mChannelsSettingsTab = new ChannelsSettingsTab(true);
    mContent.removeAll();
    mContent.add(new StatusPanel(StatusPanel.CHANNELS), BorderLayout.NORTH);
    mContent.add(mChannelsSettingsTab.createSettingsPanel(), BorderLayout.CENTER);
  }

}
