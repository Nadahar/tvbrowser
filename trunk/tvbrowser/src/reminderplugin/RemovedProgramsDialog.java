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

package reminderplugin;

import devplugin.Program;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import util.ui.TabLayout;
import util.ui.ProgramPanel;

public class RemovedProgramsDialog extends JDialog {

  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(RemovedProgramsDialog. class );


  public RemovedProgramsDialog(Frame parent, Program[] programs) {
    super(parent, false);
    setTitle(mLocalizer.msg("dialog.title","Removed Programs"));
    JPanel contentPane = (JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout(6,6));
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    JLabel lb = new JLabel(mLocalizer.msg("dialog.header","<html>Die folgenden Sendungen, an die sie erinnert werden wollten, sind in der aktualisierten Programvorschau nicht mehr enthalten:</html>"));

    contentPane.add(lb, BorderLayout.NORTH);
    contentPane.add(new JScrollPane(createProgramList(programs)), BorderLayout.CENTER);
    contentPane.add(createButtonPanel(), BorderLayout.SOUTH);

    setSize(400,200);
  }

  private JPanel createProgramList(Program[] progArr) {
    JPanel result = new JPanel(new TabLayout(2));
    for (int i=0; i<progArr.length; i++) {
      JPanel infoPn = new JPanel(new BorderLayout());

      infoPn.add(new JLabel(progArr[i].getDate()+", "+progArr[i].getChannel().getName()+":"), BorderLayout.NORTH);
      result.add(infoPn);
      result.add(new ProgramPanel(progArr[i]));
    }
    return result;
  }

  private JPanel createButtonPanel() {
    JPanel result = new JPanel(new BorderLayout());
    JButton btn = new JButton(mLocalizer.msg("close","Close"));
    btn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        hide();
      }
    });

    result.add(btn, BorderLayout.EAST);
    return result;
  }

}
