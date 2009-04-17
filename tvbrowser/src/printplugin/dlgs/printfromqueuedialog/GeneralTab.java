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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import printplugin.EmptyQueueAction;
import printplugin.PrintPlugin;
import util.program.ProgramUtilities;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.Date;
import devplugin.PluginTreeNode;
import devplugin.Program;

public class GeneralTab extends JPanel {

  private static final util.ui.Localizer mLocalizer
        = util.ui.Localizer.getLocalizerFor(GeneralTab.class);


  private JCheckBox mEmptyQueueCb;
  private PluginTreeNode mRootNode;
  private JButton mEmptyQueueBt;
  private JPanel mProgramListPanel;

  public GeneralTab(PluginTreeNode rootNode) {
    mRootNode = rootNode;
    
    CellConstraints cc = new CellConstraints();
    
    PanelBuilder pb = new PanelBuilder(new FormLayout("pref:grow",
        "fill:default:grow,2dlu,pref,2dlu,pref,10dlu"), this);
    pb.setDefaultDialogBorder();
    
    JPanel pn1 = new JPanel(new BorderLayout());
    pn1.add(mProgramListPanel = createProgramListPanel(), BorderLayout.NORTH);
    JScrollPane scrollPane = new JScrollPane(pn1);
    scrollPane.getVerticalScrollBar().setUnitIncrement(30);
    scrollPane.getVerticalScrollBar().setBlockIncrement(80);
    
    pb.add(scrollPane, cc.xy(1,1));
    pb.add(mEmptyQueueBt = new JButton(new EmptyQueueAction()), cc.xy(1,3));
    pb.add(mEmptyQueueCb = new JCheckBox(mLocalizer.msg("emptyQueue","empty queue after pringing")), cc.xy(1,5));

    mEmptyQueueBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mProgramListPanel.removeAll();
        mProgramListPanel.invalidate();
        mProgramListPanel.repaint();
      }
    });
  }


  private JPanel createProgramListPanel() {

    final JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    Program[] progs = mRootNode.getPrograms();

    Arrays.sort(progs, ProgramUtilities.getProgramComparator());
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

    Icon icon = TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL);
    JButton removeBtn = UiUtilities.createToolBarButton("Remove from queue", icon);
    removeBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        program.unmark(PrintPlugin.getInstance());
        mRootNode.removeProgram(program);
        mRootNode.update();
        content.remove(progPn);
        content.repaint();
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
