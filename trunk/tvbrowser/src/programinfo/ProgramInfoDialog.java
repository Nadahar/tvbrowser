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

package programinfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ProgramInfoDialog extends JDialog {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ProgramInfoDialog.class);
  
  
  
  public ProgramInfoDialog(Frame parent, devplugin.Program program) {
    super(parent);
    
    setTitle(mLocalizer.msg("title", "Program information"));
    
    JPanel mainPane=(JPanel)getContentPane();
    mainPane.setPreferredSize(new Dimension(450,250));
    JPanel contentPane=new JPanel(new BorderLayout());
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    JLabel titleLabel=new JLabel(program.getChannel().getName()+": "+program.getTitle());
    JPanel infoPanel=new JPanel(new GridLayout(2,1));

    Font font=titleLabel.getFont();

    titleLabel.setFont(new Font(font.getName(),Font.BOLD,font.getSize()+4));

    infoPanel.add(new JLabel(program.getDateString()));
    infoPanel.add(new JLabel(program.getTimeString()));

    JPanel panel1=new JPanel(new BorderLayout(20,0));
    panel1.add(titleLabel,BorderLayout.WEST);
    panel1.add(infoPanel,BorderLayout.EAST);

    String labelText="";
    int info=program.getInfo();
    if (info==0) {
      labelText="";
    }
    else {
      // TODO: This is TVgenial specific!!
      if ((info&0x1) == 0x1) labelText += "? (" + info + ")  ";
      if ((info&0x2) == 0x2) labelText += mLocalizer.msg("16to9", "16:9") + "  ";
      if ((info&0x4) == 0x4) labelText += mLocalizer.msg("dolby", "Dolby") + "  ";
      if ((info&0x8) == 0x8) labelText += "oo  ";
      if ((info&0x10) == 0x10) labelText += mLocalizer.msg("stereo", "Stereo") + "  ";
      if ((info&0x20) == 0x20) labelText += mLocalizer.msg("subtitle", "Subtitle") + "  ";
      if ((info&0x40) == 0x40) labelText += mLocalizer.msg("live", "Live") + "live  ";
      if ((info&0x80) == 0x80) labelText += mLocalizer.msg("blackAndWhite", "b/w") + "  ";
    }

    JLabel infoLabel=new JLabel();
    font=infoLabel.getFont();

    infoLabel.setFont(new Font(font.getName(),Font.ITALIC,font.getSize()-2));


    JTextArea descArea=new JTextArea();
    descArea.setEditable(false);
    descArea.setOpaque(false);
    descArea.setLineWrap(true);
    descArea.setWrapStyleWord(true);
    descArea.setText(program.getDescription());
    //   descArea.setColumns(20);

    JTextArea shortInfoArea=new JTextArea();
    shortInfoArea.setEditable(false);
    shortInfoArea.setOpaque(false);
    shortInfoArea.setLineWrap(true);
    shortInfoArea.setWrapStyleWord(true);
    shortInfoArea.setText(program.getShortInfo());

    JTextArea actorsArea=new JTextArea();
    actorsArea.setEditable(false);
    actorsArea.setOpaque(false);
    actorsArea.setLineWrap(true);
    actorsArea.setWrapStyleWord(true);
    actorsArea.setText(program.getActors());

    contentPane.add(panel1,BorderLayout.NORTH);
    JPanel panel2=new JPanel(new BorderLayout(0,10));
    contentPane.add(panel2,BorderLayout.CENTER);
    panel2.add(infoLabel,BorderLayout.NORTH);

    JPanel panel3=new JPanel(new BorderLayout(0,10));
    panel2.add(panel3,BorderLayout.CENTER);
    panel3.add(shortInfoArea,BorderLayout.NORTH);
    JPanel panel4=new JPanel(new BorderLayout(0,10));
    panel3.add(panel4,BorderLayout.CENTER);
    panel4.add(descArea,BorderLayout.NORTH);
    JPanel panel5=new JPanel(new BorderLayout(0,10));
    panel4.add(panel5,BorderLayout.CENTER);
    panel5.add(actorsArea,BorderLayout.NORTH);

    JPanel btnPanel=new JPanel(new BorderLayout());

    JButton closeBtn=new JButton(mLocalizer.msg("close", "Close"));
    closeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    }
    );
    btnPanel.add(closeBtn,BorderLayout.EAST);
    btnPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    mainPane.add(btnPanel,BorderLayout.SOUTH);
    final JScrollPane scrollPane = new JScrollPane(contentPane);
    mainPane.add(scrollPane, BorderLayout.CENTER);
    
    // Scroll to the beginning
    Runnable runnable = new Runnable() {
      public void run() {
        scrollPane.getVerticalScrollBar().setValue(0);
      }
    };
    SwingUtilities.invokeLater(runnable);
  }

}