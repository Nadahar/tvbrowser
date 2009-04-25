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


import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import printplugin.PrintPlugin;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;


public class MainPrintDialog extends JDialog implements ActionListener, WindowClosingIf {

  /** The localizer for this class. */
   private static final util.ui.Localizer mLocalizer
       = util.ui.Localizer.getLocalizerFor(MainPrintDialog.class);


  public static final int PRINT_DAYPROGRAMS = 1;
  public static final int PRINT_QUEUE = 2;
  private static final int CANCEL = 0;

  private JRadioButton mPrintDayProgramsRb, mPrintQueueRb;
  private int mResult = CANCEL;
  private JButton mOkBtn, mCancelBtn;

  public MainPrintDialog(Frame parent) {
    super(parent, true);
    setTitle(mLocalizer.msg("title","Drucken"));
    UiUtilities.registerForClosing(this);
    
    JPanel content = (JPanel)getContentPane();
    content.setLayout(new BorderLayout());

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(new JLabel(mLocalizer.msg("whatDoYouWantToPrint","Was wollen Sie drucken?")), BorderLayout.NORTH);
    JPanel radioBtnPanel = new JPanel();
    radioBtnPanel.setLayout(new BoxLayout(radioBtnPanel, BoxLayout.Y_AXIS));
    radioBtnPanel.add(mPrintDayProgramsRb = new JRadioButton(mLocalizer.msg("fullDayPrograms","Komplette Tagesprogramme")));
    radioBtnPanel.add(mPrintQueueRb = new JRadioButton(mLocalizer.msg("printFromQueue","Druckerwarteschlange")));
    radioBtnPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,15));
    centerPanel.add(radioBtnPanel, BorderLayout.WEST);


    JPanel southPanel = new JPanel(new BorderLayout());
    JPanel btnPanel = new JPanel();
    btnPanel.add(mOkBtn = new JButton(Localizer.getLocalization(Localizer.I18N_OK)));
    btnPanel.add(mCancelBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL)));
    southPanel.add(btnPanel, BorderLayout.EAST);
    content.add(southPanel, BorderLayout.SOUTH);
    content.add(centerPanel, BorderLayout.CENTER);

    content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    ButtonGroup group = new ButtonGroup();
    group.add(mPrintDayProgramsRb);
    group.add(mPrintQueueRb);

    mOkBtn.addActionListener(this);
    mCancelBtn.addActionListener(this);

    boolean queueHasElements = PrintPlugin.getInstance().canPrintQueue();
    mPrintQueueRb.setEnabled(queueHasElements);

    if (queueHasElements) {
      mPrintQueueRb.setSelected(true);
      mPrintQueueRb.requestFocusInWindow();
    }
    else {
      mPrintDayProgramsRb.setSelected(true);
      mPrintDayProgramsRb.requestFocusInWindow();
    }
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
      setVisible(false);
    }
    else if (e.getSource() == mCancelBtn) {
      close();
    }
  }

  public void close() {
    mResult = CANCEL;
    setVisible(false);
  }

}
