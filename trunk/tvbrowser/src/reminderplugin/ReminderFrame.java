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

import util.ui.UiUtilities;
import util.ui.ProgramPanel;
import util.io.IOUtilities;

public class ReminderFrame extends JFrame {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ReminderFrame.class);

  public static final String[] REMIND_MSG_ARR = {
    mLocalizer.msg("remind.-1", "Don't remind me"),
    mLocalizer.msg("remind.0", "Remind me when the program begins"),
    mLocalizer.msg("remind.1", "Remind me one minute before"),
    mLocalizer.msg("remind.2", "Remind me 2 minutes before"),
    mLocalizer.msg("remind.3", "Remind me 3 minutes before"),
    mLocalizer.msg("remind.5", "Remind me 5 minutes before"),
    mLocalizer.msg("remind.10", "Remind me 10 minutes before"),
    mLocalizer.msg("remind.15", "Remind me 15 minutes before"),
    mLocalizer.msg("remind.30", "Remind me 30 minutes before"),
    mLocalizer.msg("remind.60", "Remind me one hour before")
  };

  public static final int[] REMIND_VALUE_ARR = {-1, 0, 1, 2, 3, 5, 10, 15, 30, 60};
  
  
  
  public ReminderFrame(final ReminderList list, ReminderListItem item) {
    super(mLocalizer.msg("title", "Erinnerung"));
    
    list.remove(item);
    final Program prog=item.getProgram();
    JPanel jcontentPane=(JPanel)getContentPane();
    jcontentPane.setLayout(new BorderLayout(0,10));
    jcontentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    JPanel progPanel=new JPanel(new BorderLayout(5, 10));

    // text label
    String msg;
    int progMinutesAfterMidnight = prog.getHours() * 60 + prog.getMinutes();
    if (IOUtilities.getMinutesAfterMidnight() <= progMinutesAfterMidnight) {
      msg = mLocalizer.msg("soonStarts", "Soon starts");
    } else {
      msg = mLocalizer.msg("alreadyRunning", "Already running");
    }
    progPanel.add(new JLabel(msg), BorderLayout.NORTH);
    
    JLabel channelLabel=new JLabel(prog.getChannel().getName());
    progPanel.add(channelLabel,BorderLayout.EAST);
    
    progPanel.add(new ProgramPanel(prog), BorderLayout.CENTER);
    
    JPanel btnPanel=new JPanel(new BorderLayout(10,0));
    JButton closeBtn=new JButton(mLocalizer.msg("close", "Close"));
    getRootPane().setDefaultButton(closeBtn);
    
    final JComboBox comboBox=new JComboBox();
    int i=0;
    while(i<REMIND_VALUE_ARR.length && REMIND_VALUE_ARR[i]<item.getReminderMinutes()) {
      comboBox.addItem(REMIND_MSG_ARR[i]);
      i++;
    }
    
    btnPanel.add(comboBox,BorderLayout.WEST);
    btnPanel.add(closeBtn,BorderLayout.EAST);
    
    jcontentPane.add(progPanel,BorderLayout.NORTH);
    jcontentPane.add(btnPanel,BorderLayout.SOUTH);
    
    closeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event){
        int inx = comboBox.getSelectedIndex();
        int minutes = REMIND_VALUE_ARR[inx];
        if (minutes != -1) {
          list.add(prog, minutes);
        }

        dispose();
      }
    }
    );
    
    this.pack();
    UiUtilities.centerAndShow(this);
  }

}