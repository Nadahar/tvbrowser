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


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ReminderDialog extends /*PluginDialog*/JDialog {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ReminderDialog.class);

  public static final String[] SMALL_REMIND_MSG_ARR
    = new String[ReminderFrame.REMIND_MSG_ARR.length - 1];

  private static final int[] SMALL_REMIND_VALUE_ARR
    = new int[ReminderFrame.REMIND_VALUE_ARR.length - 1];
  
  static {
    // use the same entries as the ReminderFrame but without "don't remind me"
    System.arraycopy(ReminderFrame.REMIND_MSG_ARR, 1, SMALL_REMIND_MSG_ARR, 0,
      SMALL_REMIND_MSG_ARR.length);
    System.arraycopy(ReminderFrame.REMIND_VALUE_ARR, 1, SMALL_REMIND_VALUE_ARR, 0,
      SMALL_REMIND_VALUE_ARR.length);
  }

  private boolean mOkPressed=false;

  private JComboBox list;

  
  
  public ReminderDialog(Frame parent, devplugin.Program program) {
    super(parent,true);

    setTitle(mLocalizer.msg("title", "New reminder"));

    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    JLabel titleLabel=new JLabel(program.getChannel().getName()+": "+program.getTitle());
    JPanel infoPanel=new JPanel(new GridLayout(2,1));

    Font font=titleLabel.getFont();

    titleLabel.setFont(new Font(font.getName(),Font.BOLD,font.getSize()+4));

    infoPanel.add(new JLabel(program.getDateString()));
    infoPanel.add(new JLabel(program.getTimeString()));

    JPanel panel5=new JPanel(new BorderLayout(20,0));
    panel5.add(titleLabel,BorderLayout.WEST);
    panel5.add(infoPanel,BorderLayout.EAST);

    list=new JComboBox(SMALL_REMIND_MSG_ARR);
    list.setSelectedIndex(5);

    JPanel panel1=new JPanel(new BorderLayout(10,0));
    panel1.setBorder(BorderFactory.createEmptyBorder(10,0,30,0));
    panel1.add(list,BorderLayout.CENTER);

    JPanel panel2=new JPanel(new BorderLayout());
    panel2.add(panel5,BorderLayout.NORTH);

    JPanel panel6=new JPanel(new BorderLayout());
    panel6.add(panel1,BorderLayout.NORTH);
    panel2.add(panel6,BorderLayout.SOUTH);

    JPanel panel3=new JPanel(new FlowLayout(FlowLayout.TRAILING));
    
    JButton okBtn=new JButton(mLocalizer.msg("ok", "OK"));
    okBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mOkPressed=true;
        hide();
      }
    });
    panel3.add(okBtn);
    getRootPane().setDefaultButton(okBtn);

    JButton cancelBtn=new JButton(mLocalizer.msg("cancel", "Cancel"));
    cancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hide();
      }
    });
    panel3.add(cancelBtn);
    
    JPanel panel4=new JPanel(new BorderLayout());
    panel4.add(panel3,BorderLayout.EAST);

    contentPane.add(panel2,BorderLayout.NORTH);
    contentPane.add(panel4,BorderLayout.SOUTH);

    pack();
  }

  
  
  public int getReminderMinutes() {
    int idx = list.getSelectedIndex();
    return SMALL_REMIND_VALUE_ARR[idx];
  }

  
  
  public boolean getOkPressed() {
    return mOkPressed;
  }

}