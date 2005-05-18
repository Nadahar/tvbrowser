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

package printplugin.dlgs.printfromqueuedialog;

import devplugin.Program;
import devplugin.Channel;
import devplugin.PluginTreeNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import util.ui.ProgramPanel;
import util.ui.ImageUtilities;
import util.ui.UiUtilities;
import printplugin.PrintPlugin;

public class GeneralTab extends JPanel {

  private JCheckBox mEmptyQueueCb;
  private PluginTreeNode mRootNode;

  public GeneralTab(PluginTreeNode rootNode) {

    super();
    mRootNode = rootNode;
    setLayout(new BorderLayout());

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    JPanel programList = createProgramListPanel();

    add(new JScrollPane(programList), BorderLayout.CENTER);
    add(content, BorderLayout.NORTH);
    add(mEmptyQueueCb = new JCheckBox("Queue nach dem Drucken leeren"), BorderLayout.SOUTH);
  }


  private JPanel createProgramListPanel() {

    final JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    Program[] progs = mRootNode.getPrograms();

        
    for (int i=0; i<progs.length; i++) {
      final Program program = progs[i];
      final JPanel programPanel = new JPanel(new BorderLayout());
      Channel ch = program.getChannel();
      JLabel channelLb;
      if (ch.hasIcon()) {
        channelLb = new JLabel(ch.getIcon());
      }
      else {
        channelLb = new JLabel(ch.getName());
      }

      channelLb.setHorizontalAlignment(SwingConstants.RIGHT);
      channelLb.setBorder(BorderFactory.createLineBorder(Color.black));
      JPanel channelPn = new JPanel(new BorderLayout());
      channelPn.add(channelLb, BorderLayout.NORTH);


      JPanel removePn = new JPanel(new BorderLayout());
      Icon icon = ImageUtilities.createImageIconFromJar("printplugin/imgs/Delete16.gif", getClass());
      JButton removeBtn = UiUtilities.createToolBarButton("Remove from queue", icon);
      removeBtn.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent event) {
          program.unmark(PrintPlugin.getInstance());
          mRootNode.removeProgram(program);
          mRootNode.update();
          content.remove(programPanel);
          content.updateUI();
        }
      });
      removePn.add(removeBtn, BorderLayout.NORTH);

      channelPn.setPreferredSize(new Dimension(60,10));

      programPanel.add(channelPn, BorderLayout.WEST);
      programPanel.add(new ProgramPanel(progs[i]), BorderLayout.CENTER);
      programPanel.add(removePn, BorderLayout.EAST);

      content.add(programPanel);
    }

    return content;
  }

  public void setEmptyQueueAfterPrinting(boolean b) {
    mEmptyQueueCb.setSelected(b);
  }

  public boolean emptyQueueAfterPrinting() {
    return mEmptyQueueCb.isSelected();
  }
}
