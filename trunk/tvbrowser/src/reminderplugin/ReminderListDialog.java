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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */



package reminderplugin;


import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import java.util.Iterator;
import devplugin.Program;

public class ReminderListDialog extends JDialog {

  private static String[] listItems={
    "remind me when the program begins",
   "remind me one minute before",
   "reminde me two minutes before",
   "remind me three minutes before",
   "remind me five minutes before",
   "remind me ten minutes before",
   "remind me 15 minutes before",
   "remind me 30 minutes before",
    "remind me one hour before",
   "don't remind me"};



  private static final int DONT_REMEMBER=9;

  private ReminderList reminderList;

    public ReminderListDialog(Frame parent, ReminderList list) {
        super(parent,true);
        reminderList=list;
        setTitle("Erinnerungen");

        JPanel contentpane=(JPanel)getContentPane();
        contentpane.setLayout(new BorderLayout(0,12));
        contentpane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JLabel label=new JLabel("Sie werden an folgende Sendungen erinnert:");
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

       contentpane.add(new JScrollPane(listPanel),BorderLayout.CENTER);

        JPanel btnPanel=new JPanel(new BorderLayout());
        JButton closeBtn=new JButton("Schlieﬂen");

        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        }
         );

        btnPanel.add(closeBtn,BorderLayout.EAST);
        contentpane.add(btnPanel,BorderLayout.SOUTH);
    }


    private JPanel createListItemPanel(final ReminderListItem item) {

      Program prog=item.getProgram();

        JPanel result=new JPanel(new BorderLayout());
        result.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        JPanel panel1=new JPanel(new BorderLayout());
        JPanel panel2=new JPanel(new BorderLayout());
        final JComboBox box1=new JComboBox(listItems);

        box1.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            int inx=box1.getSelectedIndex();
            if (inx==DONT_REMEMBER) {
              reminderList.remove(item);
            }else{
              item.setReminderSelection(inx);
            }
          }
        });

        box1.setSelectedIndex(item.getReminderSelection());

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