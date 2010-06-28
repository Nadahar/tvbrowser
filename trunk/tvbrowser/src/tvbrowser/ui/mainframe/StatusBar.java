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

package tvbrowser.ui.mainframe;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import devplugin.ProgressMonitor;

/**
 * Statusbar
 * @author bodum
 */
public class StatusBar extends JPanel {
  /** Progressbar for Download-Status etc */
  private JProgressBar mProgressBar;
  /** Info-Text */
  private JLabel mInfoLabel;

  /**
   * Create the Statusbar
   */
  public StatusBar() {
    setOpaque(false);
    setLayout(new BorderLayout(2, 0));
    setBorder(BorderFactory.createEmptyBorder(2,0,0,0));
    setPreferredSize(new Dimension(0, 18));

    mInfoLabel = new JLabel();
    mInfoLabel.setBorder(BorderFactory.createEtchedBorder());

    mProgressBar = new JProgressBar();
    mProgressBar.setPreferredSize(new Dimension(200, 10));
    mProgressBar.setBorder(BorderFactory.createEtchedBorder());

    add(mInfoLabel, BorderLayout.CENTER);
    add(mProgressBar, BorderLayout.EAST);
  }

  /**
   * Gets the ProgressBar
   * @return ProgressBar
   */
  public JProgressBar getProgressBar() {
    return mProgressBar;
  }

  /**
   * Gets the Label
   * @return Label
   */
  public JLabel getLabel() {
    return mInfoLabel;
  }

  public ProgressMonitor createProgressMonitor() {
    return new ProgressMonitor(){
      public void setMaximum(int maximum) {
        mProgressBar.setMaximum(maximum);
      }

      public void setValue(int value) {
        mProgressBar.setValue(value);
      }

      public void setMessage(String msg) {
        mInfoLabel.setText(msg);
      }
    };
  }

}