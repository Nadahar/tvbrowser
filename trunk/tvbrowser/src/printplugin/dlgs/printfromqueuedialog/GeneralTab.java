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
import devplugin.Date;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import util.ui.ImageUtilities;
import util.ui.UiUtilities;
import printplugin.PrintPlugin;
import printplugin.util.Util;


public class GeneralTab extends JPanel {

  private static final util.ui.Localizer mLocalizer
        = util.ui.Localizer.getLocalizerFor(GeneralTab.class);


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
    add(mEmptyQueueCb = new JCheckBox(mLocalizer.msg("emptyQueue","empty queue after pringing")), BorderLayout.SOUTH);
  }


  private JPanel createProgramListPanel() {

    final JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    Program[] progs = mRootNode.getPrograms();

    Util.sortProgramsByDateAndChannel(progs);
    Date curDate=null;

    for (int i=0; i<progs.length; i++) {
      if (!progs[i].getDate().equals(curDate)) {
        curDate = progs[i].getDate();
        JPanel datePanel = new JPanel(new BorderLayout());
        JLabel dateLb = new JLabel(curDate.getLongDateString());
        dateLb.setFont(new Font("Dialog", Font.ITALIC, 14));
        datePanel.add(dateLb, BorderLayout.WEST);
        content.add(datePanel);
      }
      addProgramPanel(content, progs[i]);
    }

    return content;
  }




  private void addProgramPanel(final JPanel content, final Program program) {
    final JPanel progPn = new JPanel(new BorderLayout());

    Icon icon = ImageUtilities.createImageIconFromJar("printplugin/imgs/Delete16.gif", getClass());
    JButton removeBtn = UiUtilities.createToolBarButton("Remove from queue", icon);
    removeBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        program.unmark(PrintPlugin.getInstance());
        mRootNode.removeProgram(program);
        mRootNode.update();
        content.remove(progPn);
        content.updateUI();
      }
    });

    progPn.add(removeBtn, BorderLayout.WEST);

    JPanel pn1 = new JPanel(new BorderLayout());
    progPn.add(pn1, BorderLayout.CENTER);
    Channel ch = program.getChannel();
    JLabel channelLb;
    channelLb = new JLabel(ch.getName()+": ");
    channelLb.setFont(new Font("Dialog", Font.BOLD, 12));
    channelLb.setHorizontalAlignment(SwingConstants.RIGHT);
    channelLb.setPreferredSize(new Dimension(60,10));
    pn1.add(channelLb, BorderLayout.WEST);

    JLabel progLb = new JLabel(program.getTimeString()+": "+program.getTitle());
    pn1.add(progLb, BorderLayout.CENTER);
    content.add(progPn);

  }



  public void setEmptyQueueAfterPrinting(boolean b) {
    mEmptyQueueCb.setSelected(b);
  }

  public boolean emptyQueueAfterPrinting() {
    return mEmptyQueueCb.isSelected();
  }
}
