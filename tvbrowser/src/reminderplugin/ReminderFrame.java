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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
import java.util.Properties;

public class ReminderFrame extends JFrame {


  private static String[] listItems={
    "don't remind me",
    "remind me when the program begins",
   "remind me one minute before",
   "reminde me two minutes before",
   "remind me three minutes before",
   "remind me five minutes before",
   "remind me ten minutes before",
   "remind me 15 minutes before",
   "remind me 30 minutes before",
    "remind me one hour before"};

    private static int[] listValues={-1, 0, 1, 2, 3, 5, 10, 15, 30, 60};

    public ReminderFrame(final ReminderList list, ReminderListItem item) {

      super("Reminder");
      list.remove(item);
      final Program prog=item.getProgram();
      JPanel jcontentPane=(JPanel)getContentPane();
      jcontentPane.setLayout(new BorderLayout(0,10));
      jcontentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      JPanel progPanel=new JPanel(new BorderLayout());

      JLabel channelLabel=new JLabel(prog.getChannel().getName());

      progPanel.add(channelLabel,BorderLayout.EAST);

      progPanel.add(Plugin.getPluginManager().createProgramPanel(prog),BorderLayout.CENTER);

      JPanel btnPanel=new JPanel(new BorderLayout(10,0));
      JButton closeBtn=new JButton("Done");

      final JComboBox comboBox=new JComboBox();
      int i=0;
      while(i<listValues.length && listValues[i]<item.getReminderMinutes()) {
        comboBox.addItem(listItems[i]);
        i++;
      }

      btnPanel.add(comboBox,BorderLayout.WEST);
      btnPanel.add(closeBtn,BorderLayout.EAST);

      jcontentPane.add(progPanel,BorderLayout.NORTH);
      jcontentPane.add(btnPanel,BorderLayout.SOUTH);

      closeBtn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event){
          int inx=comboBox.getSelectedIndex()-1;
          if (inx>0) {
            ReminderListItem item=new ReminderListItem(prog,inx);
            list.add(item);
          }
          hide();
        }
      }
      );


      this.pack();
      this.show();

    }




}