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

package tvbrowser.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UpdateDlg extends JDialog implements ActionListener {

  public static final int CANCEL=-1;

  private JButton cancelBtn, updateBtn;
  private int result=0;
  private JComboBox comboBox;
  private static String[] comboBoxEntries={"today", "up to tomorrow", "next 2 days", "next 3 days","next 4 days","next 5 days", "next 6 days","1 week","get all"};
  private JCheckBox checkBox;

  public UpdateDlg(JFrame parent, boolean modal) {
    super(parent,modal);
    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    this.setTitle("Update");
    JPanel buttonPanel=new JPanel();
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
    cancelBtn=new JButton("cancel");
    updateBtn=new JButton("update now");

    cancelBtn.addActionListener(this);
    updateBtn.addActionListener(this);

    buttonPanel.add(cancelBtn);
    buttonPanel.add(updateBtn);

    contentPane.add(buttonPanel,BorderLayout.SOUTH);

    JPanel northPanel=new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.Y_AXIS));


    JPanel panel1=new JPanel(new BorderLayout(7,0));
    panel1.add(new JLabel("update program for"),BorderLayout.WEST);
    comboBox=new JComboBox();
    for (int i=0;i<comboBoxEntries.length;i++) {
      comboBox.addItem(comboBoxEntries[i]);
    }
    comboBox.setSelectedIndex(0);
    panel1.add(comboBox,BorderLayout.EAST);
    northPanel.add(panel1);
    checkBox=new JCheckBox("remember settings");
    JPanel panel2=new JPanel(new BorderLayout());
    panel2.add(checkBox,BorderLayout.WEST);

    northPanel.add(panel2);

    contentPane.add(northPanel,BorderLayout.NORTH);
  }


  public int getResult() { return result; }

  public boolean rememberSettings() {
    return true;
  }

  public void actionPerformed(ActionEvent event) {
    Object source=event.getSource();
    if (source==cancelBtn) {
      result=CANCEL;
      setVisible(false);
    }
    else if (source==updateBtn) {


      result=comboBox.getSelectedIndex();
      if (checkBox.isSelected()) {
        // TO DO: enter code here :-)
      }

      setVisible(false);
    }
  }
}