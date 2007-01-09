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

package tvbrowser.extras.reminderplugin;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import devplugin.Program;

public class RemovedProgramsDialog extends JDialog implements WindowClosingIf{

  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(RemovedProgramsDialog. class );


  public RemovedProgramsDialog(Dialog parent, Program[] programs) {
    super(parent, false);
    init(programs);
  }

  public RemovedProgramsDialog(Frame parent, Program[] programs) {
    super(parent, false);
    init(programs);
  }

  private void init(Program[] programs) {
    setTitle(mLocalizer.msg("dialog.title","Removed Programs"));
    
    UiUtilities.registerForClosing(this);
    
    JPanel contentPane = (JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout(6,6));
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    JLabel lb = new JLabel(mLocalizer.msg("dialog.header","<html>Die folgenden Sendungen, an die sie erinnert werden wollten, sind in der aktualisierten Programmvorschau nicht mehr enthalten:</html>"));

    contentPane.add(lb, BorderLayout.NORTH);
    contentPane.add(new JScrollPane(new ProgramList(programs, true)), BorderLayout.CENTER);
    contentPane.add(createButtonPanel(), BorderLayout.SOUTH);

    setSize(400,200);
  }
  
  private JPanel createButtonPanel() {
    JPanel result = new JPanel(new BorderLayout());
    JButton btn = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    btn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });

    result.add(btn, BorderLayout.EAST);
    return result;
  }

  public void close() {
    setVisible(false);
  }

}
