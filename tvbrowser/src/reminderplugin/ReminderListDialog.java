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


import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import java.util.Iterator;
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
    
    JPanel listPanel=new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel,BoxLayout.Y_AXIS));
    
    if (list!=null) {
      Iterator it=list.getReminderItems();
      while (it.hasNext()) {
        ReminderListItem item=(ReminderListItem)it.next();
        listPanel.add(createListItemPanel(item));
      }
    }
    
    JScrollPane scrollPane = new JScrollPane(listPanel);
    scrollPane.getVerticalScrollBar().setUnitIncrement(30);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(30);
    contentpane.add(scrollPane, BorderLayout.CENTER);
    
    JPanel btnPanel=new JPanel(new BorderLayout());
    JButton closeBtn = new JButton(mLocalizer.msg("close", "Close"));
    getRootPane().setDefaultButton(closeBtn);
    
    closeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hide();
      }
    });
    
    btnPanel.add(closeBtn,BorderLayout.EAST);
    contentpane.add(btnPanel,BorderLayout.SOUTH);
  }
  
  
  
  private JPanel createListItemPanel(final ReminderListItem item) {
    
    Program prog=item.getProgram();
    
    JPanel result=new JPanel(new BorderLayout());
    result.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    JPanel panel1=new JPanel(new BorderLayout());
    JPanel panel2=new JPanel(new BorderLayout());
    final JComboBox box1=new JComboBox(REMIND_MSG_ARR);
    
    box1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        int inx = box1.getSelectedIndex();
        if (inx == DONT_REMEMBER) {
          reminderList.remove(item);
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
    panel2.add(box1,BorderLayout.NORTH);
    
    result.add(panel1,BorderLayout.WEST);
    result.add(panel2,BorderLayout.EAST);
    result.add(devplugin.Plugin.getPluginManager().createProgramPanel(prog),BorderLayout.CENTER);
    
    return result;
  }

}