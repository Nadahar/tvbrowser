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

import devplugin.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ReminderDialog extends /*PluginDialog*/JDialog {


  private static String[] listItems={"remind me when the program begins",
    "remind me one minute before",
    "reminde me two minutes before",
    "remind me three minutes before",
    "remind me five minutes before",
    "remind me ten minutes before",
    "remind me 15 minutes before",
    "remind me 30 minutes before",
    "remind me one hour before"};



  private boolean ok=false;

  private JComboBox list;

  public ReminderDialog(Frame parent, devplugin.Program program) {
    super(parent,true);

devplugin.Profiler.getDefault().show("2.1");
    setTitle("New reminder item");

    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    JLabel titleLabel=new JLabel(program.getChannel().getName()+": "+program.getTitle());
    JPanel infoPanel=new JPanel(new GridLayout(2,1));

devplugin.Profiler.getDefault().show("2.2");
    Font font=titleLabel.getFont();

    titleLabel.setFont(new Font(font.getName(),Font.BOLD,font.getSize()+4));

    infoPanel.add(new JLabel(program.getDateString()));
    infoPanel.add(new JLabel(program.getTimeString()));

    JPanel panel5=new JPanel(new BorderLayout(20,0));
    panel5.add(titleLabel,BorderLayout.WEST);
    panel5.add(infoPanel,BorderLayout.EAST);

devplugin.Profiler.getDefault().show("2.3");
    list=new JComboBox(listItems);
    list.setSelectedIndex(5);

    JPanel panel1=new JPanel(new BorderLayout(10,0));
    panel1.setBorder(BorderFactory.createEmptyBorder(10,0,30,0));
    panel1.add(list,BorderLayout.CENTER);

devplugin.Profiler.getDefault().show("2.4");

    JPanel panel2=new JPanel(new BorderLayout());
    panel2.add(panel5,BorderLayout.NORTH);

    JPanel panel6=new JPanel(new BorderLayout());
    panel6.add(panel1,BorderLayout.NORTH);
    panel2.add(panel6,BorderLayout.SOUTH);


devplugin.Profiler.getDefault().show("2.5");
    JPanel panel3=new JPanel(new GridLayout(1,0,10,0));
    JButton cancelBtn=new JButton("Cancel");
    cancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hide();
      }
    }
    );
devplugin.Profiler.getDefault().show("2.6");
    JButton okBtn=new JButton("OK");
    okBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ok=true;
        hide();
      }
    }
    );
    panel3.add(cancelBtn);
    panel3.add(okBtn);
devplugin.Profiler.getDefault().show("2.7");

    JPanel panel4=new JPanel(new BorderLayout());
    panel4.add(panel3,BorderLayout.EAST);

    contentPane.add(panel2,BorderLayout.NORTH);
    contentPane.add(panel4,BorderLayout.SOUTH);

devplugin.Profiler.getDefault().show("2.8");
    pack();
devplugin.Profiler.getDefault().show("2.9");
    setVisible(true);
devplugin.Profiler.getDefault().show("2.10");
  }

  public int getReminderSelection() {
    return list.getSelectedIndex();
  }

  public boolean ok() {
    return ok;
  }

}