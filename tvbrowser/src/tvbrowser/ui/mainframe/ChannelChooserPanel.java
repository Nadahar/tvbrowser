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


package tvbrowser.ui.mainframe;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tvbrowser.core.Settings;
import util.ui.ChannelListCellRenderer;
import devplugin.Channel;


/**
 * @author bodum
 */
public class ChannelChooserPanel extends JPanel {


  private DefaultListModel mChannelChooserModel;
  private JList mList;
  private MainFrame mParent;

  /**
   * @param frame
   */
  public ChannelChooserPanel(MainFrame frame) {
    mParent =frame;

    mChannelChooserModel = new DefaultListModel();

    mList = new JList(mChannelChooserModel);
    updateChannelChooser();
    setLayout(new BorderLayout());
    add(new JScrollPane(mList));

    mList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        Channel selectedChannel = (Channel)mList.getSelectedValue();
        if (selectedChannel != null) {
          mParent.showChannel(selectedChannel);
        }
      }
    });
  }


  public void updateChannelChooser() {
    mList.setCellRenderer(new ChannelListCellRenderer(Settings.propShowChannelIconsInChannellist.getBoolean()));
    mChannelChooserModel.removeAllElements();
    Channel[] channelList=tvbrowser.core.ChannelList.getSubscribedChannels();
    for (int i=0;i<channelList.length;i++) {
      mChannelChooserModel.addElement(channelList[i]);
    }
  }
}
