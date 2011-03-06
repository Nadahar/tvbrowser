/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package util.ui.progress;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import util.ui.UIThreadRunner;
import util.ui.UiUtilities;

public class ProgressWindow implements devplugin.ProgressMonitor {

  private JLabel mLabel;

  private JDialog mDialog;

  private JProgressBar mBar;

  public ProgressWindow(Component parent, String msg) {
    mDialog = UiUtilities.createDialog(parent, true);

    JPanel content = (JPanel) mDialog.getContentPane();
    content.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    content.setLayout(new BorderLayout());
    mLabel = new JLabel(msg);
    mLabel.setHorizontalAlignment(SwingConstants.CENTER);
    content.add(mLabel, BorderLayout.CENTER);

    mBar = new JProgressBar();
    mBar.setVisible(false);

    content.add(mBar, BorderLayout.SOUTH);

    mDialog.setSize(500, 70);
    mDialog.setUndecorated(true);
    mDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  }

  public ProgressWindow(Component parent) {
    this(parent, "");
  }

  public void run(final Progress progress) {
    Thread thread = new Thread("Progress window") {
      public void run() {
        progress.run();
        while (!mDialog.isVisible()) {
        }
        mDialog.setVisible(false);
        mDialog.dispose();
      }
    };
    thread.start();
    UiUtilities.centerAndShow(mDialog);
  }

  public void setMaximum(final int maximum) {
    UIThreadRunner.invokeLater(new Runnable() {
      @Override
      public void run() {
        if(maximum == -1) {
          mBar.setVisible(false);
        } else {
          mBar.setMaximum(maximum);
          mBar.setVisible(true);
          mBar.setStringPainted(true);
        }
      }
    });
  }

  public void setValue(final int value) {
    UIThreadRunner.invokeLater(new Runnable() {

      @Override
      public void run() {
        mBar.setValue(value);
      }
    });
  }

  public void setMessage(final String msg) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mLabel.setText(msg);
      }
    });
  }

}