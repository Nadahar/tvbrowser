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

import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import devplugin.ProgressMonitor;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ProgressBarProgressMonitor implements ProgressMonitor {

    private static final Logger mLog = Logger.getLogger(ProgressBarProgressMonitor.class.getName());

    private JProgressBar mProgressBar;

    private JLabel mLabel;

    public ProgressBarProgressMonitor(JProgressBar progressBar, JLabel label) {
        mProgressBar = progressBar;
        mLabel = label;
    }

    public ProgressBarProgressMonitor(JProgressBar progressBar) {
        this(progressBar, null);
    }

    public void setMaximum(final int maximum) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                mProgressBar.setMaximum(maximum);
            }
        });
    }

    public void setValue(final int value) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                mProgressBar.setValue(value);
            }
        });
    }

    public void setMessage(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (mLabel != null) {
                    mLabel.setText(msg);
                    mLog.info("Progress: " + msg);
                }
            }
        });
    }

}