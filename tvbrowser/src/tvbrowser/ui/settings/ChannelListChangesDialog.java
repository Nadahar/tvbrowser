/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourcceforge.net)
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
 *     $Date: 2007-07-02 21:35:23 +0200 (Mo, 02 Jul 2007) $
 *   $Author: bananeweizen $
 * $Revision: 3507 $
 */
package tvbrowser.ui.settings;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tvbrowser.ui.settings.channel.ChannelJList;
import util.ui.ChannelListCellRenderer;
import util.ui.Localizer;
import devplugin.Channel;

public class ChannelListChangesDialog extends JDialog {
  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ChannelListChangesDialog.class);

  private ArrayList<Channel> mAddedList;

  private ArrayList<Channel> mDeletedList;

  public ChannelListChangesDialog(JDialog dialog,
      ArrayList<Channel> channelsBefore, List<Channel> channelsAfter) {
    super(dialog, true);
    mAddedList = new ArrayList<Channel>();
    mDeletedList = new ArrayList<Channel>();
    for (int i = 0; i < channelsAfter.size(); i++) {
      if (!channelsBefore.contains(channelsAfter.get(i))) {
        mAddedList.add(channelsAfter.get(i));
      }
    }
    Collections.sort(mAddedList);
    for (int i = 0; i < channelsBefore.size(); i++) {
      if (!channelsAfter.contains(channelsBefore.get(i))) {
        mDeletedList.add(channelsBefore.get(i));
      }
    }
    Collections.sort(mDeletedList);
    createGui();
  }

  /**
   * Creates the GUI
   */
  private void createGui() {
    setTitle(mLocalizer.msg("title", "Channel changes"));

    setLocationRelativeTo(getParent());

    JPanel contentPanel = (JPanel) getContentPane();
    contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    contentPanel.setLayout(new BorderLayout());

    JPanel panelAdded = new JPanel(new BorderLayout());
    panelAdded.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("added", "New channels")));

    DefaultListModel listModel = new DefaultListModel();
    for (int i = 0; i < mAddedList.size(); i++) {
      listModel.addElement(mAddedList.get(i));
    }
    ChannelJList list = new ChannelJList(listModel);
    list.setCellRenderer(new ChannelListCellRenderer(true, true));

    panelAdded.add(new JScrollPane(list), BorderLayout.CENTER);

    JPanel panelDeleted = new JPanel(new BorderLayout());
    panelDeleted.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("deleted", "Removed channels")));

    listModel = new DefaultListModel();
    for (int i = 0; i < mDeletedList.size(); i++) {
      listModel.addElement(mDeletedList.get(i));
    }
    list = new ChannelJList(listModel);
    list.setCellRenderer(new ChannelListCellRenderer(true, true));

    panelDeleted.add(new JScrollPane(list), BorderLayout.CENTER);

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }

    });
    btnPanel.add(ok);

    contentPanel.add(panelAdded, BorderLayout.NORTH);
    contentPanel.add(panelDeleted, BorderLayout.CENTER);
    contentPanel.add(btnPanel, BorderLayout.SOUTH);

    pack();

  }
}
