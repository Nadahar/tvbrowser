/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package reminderplugin;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.ui.ImageUtilities;
import util.ui.ProgramPanel;
import util.ui.SendToPluginDialog;
import util.ui.UiUtilities;
import devplugin.Program;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ReminderListDialog extends JDialog {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ReminderListDialog.class);
  
  private static final String[] REMIND_MSG_ARR = ReminderFrame.REMIND_MSG_ARR;
  private static final int[] REMIND_VALUE_ARR = ReminderFrame.REMIND_VALUE_ARR;

  private static final int DONT_REMEMBER = 0;

  private ReminderList reminderList;
  
  private JPanel mListPanel;
  private JScrollPane mScrollPane;
  
  public ReminderListDialog(Frame parent, ReminderList list) {
    super(parent,true);
    
    String msg;
    
    reminderList=list;
    setTitle(mLocalizer.msg("title", "Reminder"));
    
    JPanel contentpane=(JPanel)getContentPane();
    contentpane.setLayout(new BorderLayout(0,12));
    contentpane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    msg = mLocalizer.msg("labelText", "You will be reminded for the following programs:");
    JLabel label = new JLabel(msg);
    contentpane.add(label,BorderLayout.NORTH);
    
    mListPanel=new JPanel();
    mListPanel.setLayout(new BoxLayout(mListPanel,BoxLayout.Y_AXIS));
    
    if (list!=null) {
      Iterator it=list.getReminderItems();
      while (it.hasNext()) {
        ReminderListItem item=(ReminderListItem)it.next();
        mListPanel.add(createListItemPanel(item));
      }
    }
    
    mScrollPane = new JScrollPane(mListPanel);
    mScrollPane.getVerticalScrollBar().setUnitIncrement(30);
    mScrollPane.getHorizontalScrollBar().setUnitIncrement(30);
    contentpane.add(mScrollPane, BorderLayout.CENTER);
    
    JPanel btnPanel=new JPanel(new BorderLayout());
    
    JButton closeBtn = new JButton(mLocalizer.msg("close", "Close"));
    getRootPane().setDefaultButton(closeBtn);
    
    closeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hide();
      }
    });

    
    btnPanel.add(closeBtn,BorderLayout.EAST);

    JButton sendBtn = new JButton(new ImageIcon("imgs/SendToPlugin.png"));

    sendBtn.setToolTipText(mLocalizer.msg("send", "Send Programs to another Plugin"));
    sendBtn.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
           showSendDialog();            
        }
        
    });

    btnPanel.add(sendBtn, BorderLayout.WEST);   
    
    contentpane.add(btnPanel,BorderLayout.SOUTH);
  }
  
  private void showSendDialog() {
      Program[] programArr = new Program[reminderList.size()];

      int i = 0;
      Iterator it = reminderList.getReminderItems();      
      while (it.hasNext()) {
          ReminderListItem item = (ReminderListItem)it.next();
          programArr[i] = item.getProgram();
          i++;
      }
      
      SendToPluginDialog send = new SendToPluginDialog(this, programArr);

      send.show();
  }  
  
  private void removeReminderListItem(ReminderListItem item, JPanel panel) {
    reminderList.remove(item);    
    mListPanel.remove(panel);
    mScrollPane.updateUI();
  }
  
  private JPanel createListItemPanel(final ReminderListItem item) {
    
    final Program prog=item.getProgram();
    
    final JPanel result=new JPanel(new BorderLayout());
    result.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    JPanel panel1=new JPanel(new BorderLayout());
    JPanel panel2=new JPanel(new BorderLayout());
    
    
    final JComboBox box1=new JComboBox(REMIND_MSG_ARR);
    
    box1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        int inx = box1.getSelectedIndex();
        if (inx == DONT_REMEMBER) {
          removeReminderListItem(item,result);
        } else {
          item.setReminderMinutes(REMIND_VALUE_ARR[inx]);
        }
      }
    });

    // select the right item
    int minutes = item.getReminderMinutes();
    int idx = 0;
    for (int i = 0; i < REMIND_VALUE_ARR.length; i++) {
      if (minutes == REMIND_VALUE_ARR[i]) {
        idx = i;
        break;
      }
    }
    box1.setSelectedIndex(idx);
    
    JLabel dateLabel=new JLabel(prog.getDate().toString());
    JLabel channelLabel=new JLabel(prog.getChannel().getName());
    dateLabel.setPreferredSize(new java.awt.Dimension(100,(int)dateLabel.getPreferredSize().getHeight()));
    
    JPanel panel3=new JPanel(new BorderLayout());
    panel3.add(channelLabel,BorderLayout.NORTH);
    panel1.add(dateLabel,BorderLayout.NORTH);
    panel1.add(panel3,BorderLayout.CENTER);
    
    JPanel panel4=new JPanel();
    panel4.setLayout(new BoxLayout(panel4,BoxLayout.Y_AXIS));
    panel2.add(panel4,BorderLayout.NORTH);
    
    
    JPanel panel5=new JPanel(new BorderLayout());
    panel4.add(box1);
    panel4.add(panel5);
    Icon icon = ImageUtilities.createImageIconFromJar("reminderplugin/Delete24.gif", getClass());
    String msg = mLocalizer.msg("delete", "Delete this program from reminder list");
    JButton deleteBtn = UiUtilities.createToolBarButton(msg, icon);
    deleteBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        removeReminderListItem(item,result);
      }
    });
    panel5.add(deleteBtn,BorderLayout.EAST);
    
    result.add(panel1,BorderLayout.WEST);
    result.add(panel2,BorderLayout.EAST);
    
    ProgramPanel panel = new ProgramPanel(prog);
    panel.addPluginContextMenuMouseListener(ReminderPlugin.getInstance());
    result.add(panel, BorderLayout.CENTER);
    
    return result;
  }

}