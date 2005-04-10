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

package printplugin.dlgs;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class MainPrintDialog extends JDialog implements ActionListener {

  /** The localizer for this class. */
   private static final util.ui.Localizer mLocalizer
       = util.ui.Localizer.getLocalizerFor(MainPrintDialog.class);


  public static final int PRINT_DAYPROGRAMS = 1;
  public static final int PRINT_QUEUE = 2;
  public static final int CANCEL = 0;

  private JRadioButton mPrintDayProgramsRb, mPrintQueueRb;
  private int mResult = CANCEL;
  private JButton mOkBtn, mCancelBtn;

  public MainPrintDialog(Frame parent) {
    super(parent, true);
    setTitle("Drucken");
    JPanel content = (JPanel)getContentPane();
    content.setLayout(new BorderLayout());

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(new JLabel(mLocalizer.msg("whatDoYouWantToPrint","Was wollen Sie drucken?")), BorderLayout.NORTH);
    JPanel radioBtnPanel = new JPanel();
    radioBtnPanel.setLayout(new BoxLayout(radioBtnPanel, BoxLayout.Y_AXIS));
    radioBtnPanel.add(mPrintDayProgramsRb = new JRadioButton(mLocalizer.msg("fullDayPrograms","Komplette Tagesprogramme")));
    radioBtnPanel.add(mPrintQueueRb = new JRadioButton(mLocalizer.msg("printFromQueue","Druckerwarteschlange")));
    radioBtnPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,15));
    centerPanel.add(radioBtnPanel, BorderLayout.EAST);


    JPanel southPanel = new JPanel(new BorderLayout());
    JPanel btnPanel = new JPanel();
    btnPanel.add(mOkBtn = new JButton(mLocalizer.msg("ok","OK")));
    btnPanel.add(mCancelBtn = new JButton(mLocalizer.msg("cancel","Cancel")));
    southPanel.add(btnPanel, BorderLayout.EAST);
    content.add(southPanel, BorderLayout.SOUTH);
    content.add(centerPanel, BorderLayout.CENTER);

    content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    setSize(300,150);

    ButtonGroup group = new ButtonGroup();
    group.add(mPrintDayProgramsRb);
    group.add(mPrintQueueRb);

    mOkBtn.addActionListener(this);
    mCancelBtn.addActionListener(this);

    mPrintDayProgramsRb.setSelected(true);

  }

  public int getResult() {
    return mResult;
  }


  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == mOkBtn) {
      if (mPrintDayProgramsRb.isSelected()) {
        mResult = PRINT_DAYPROGRAMS;
      }
      else {
        mResult = PRINT_QUEUE;
      }
      hide();
    }
    else if (e.getSource() == mCancelBtn) {
      mResult = CANCEL;
      hide();
    }
  }

}
