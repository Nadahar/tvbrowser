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
package tvbrowser.core.search;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramSearcher;

/**
 * An abstract searcher implementation that reduces the checks on String checks.
 * 
 * @author Til Schneider, www.murfman.de
 */
public abstract class AbstractSearcher implements ProgramSearcher {
  /** Translator */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(AbstractSearcher.class);

  /** The comparator that compares two programs by their start time and date */
  private static Comparator mStartTimeComparator;
  
  /** Indicates if the special characters should be replaced.*/
  protected boolean mReplaceSpCh = false;

  /** Is the Search running ? */
  private boolean mSearchRunning = false;

  /** Waiting Dialog */
  private JDialog mWaitingDialog;

  /**
   * Gets or creates the start time comperator.
   * 
   * @return The start time comperator
   */
  private static Comparator getStartTimeComparator() {
    if (mStartTimeComparator == null) {
      // Create an comparator that compares two programs by their start time and date
      mStartTimeComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
          Program prog1 = (Program) o1;
          Program prog2 = (Program) o2;

          int dateComp = prog1.getDate().compareTo(prog2.getDate());
          if (dateComp == 0) {
            // Both program are at the same date -> Check the start time
            return prog1.getStartTime() - prog2.getStartTime();
          } else {
            return dateComp;
          }
        }
      };
    }

    return mStartTimeComparator;
  }


  /**
   * Checks whether a field of a program matches to the criteria of this
   * searcher.
   * 
   * @param prog The program to check.
   * @param fieldArr The fields to search in.
   * @return Whether at least one field of the program matches.
   */
  public boolean matches(Program prog, ProgramFieldType[] fieldArr) {
    if (fieldArr == null) {
      // Search in nothing? This won't match...
      return false;
    }

    /* Concatenate all all fields into one string and do the match */
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < fieldArr.length; i++) {
      // Get the field value as String
      String value = null;
      if (fieldArr[i] != null) {
        if (fieldArr[i].getFormat() == ProgramFieldType.TEXT_FORMAT) {
          value = prog.getTextField(fieldArr[i]);
        }
        else if (fieldArr[i].getFormat() == ProgramFieldType.INT_FORMAT) {
          value = prog.getIntFieldAsString(fieldArr[i]);
        }
        else if (fieldArr[i].getFormat() == ProgramFieldType.TIME_FORMAT) {
          value = prog.getTimeFieldAsString(fieldArr[i]);
        }
      }

      if (value != null) {
        buf.append(value).append(" ");
      }
    }

    /* Remove special characters */
    String s = buf.toString();
    
    if(mReplaceSpCh)
      s = s.replaceAll("\\p{Punct}", ";");
    s = s.replaceAll("\n", " ");
    s = s.trim();
    
    if(s.length() == 0)
      return false;
    else
      return matches(s);
  }
  
  /**
   * Searches the TV database for programs that match the criteria of this
   * searcher.
   * 
   * @param fieldArr The fields to search in
   * @param startDate The date to start the search.
   * @param nrDays The number of days to include after the start date. If
   *        negative the days before the start date are used.
   * @param channels The channels to search in.
   * @param sortByStartTime Should the results be sorted by the start time?
   *        If not, the results will be grouped by date and channel and the
   *        search will be faster.
   * @return The matching programs.
   */
  public synchronized Program[] search(ProgramFieldType[] fieldArr, Date startDate,
                          int nrDays, Channel[] channels, boolean sortByStartTime)
  {
    mSearchRunning = true;
    new Thread(new Runnable() {
      public void run() {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (mSearchRunning)
          createDialog();
        if (mSearchRunning && mWaitingDialog != null) {
          UiUtilities.centerAndShow(mWaitingDialog);
          mWaitingDialog.setVisible(mSearchRunning);
        }
          
      }
    }).start();
    
    // Should we search in all channels?
    if (channels == null) {
      channels = Settings.propSubscribedChannels.getChannelArray(false);
    }

    if (nrDays < 0) {
      // Search in the past
      startDate = startDate.addDays(nrDays);
      nrDays = Math.abs(nrDays);
    }

    // Perform the actual search
    ArrayList hitList = new ArrayList();
    int lastDayWithData = 0;
    for (int day = 0; day <= nrDays; day++) {
      for (int channelIdx = 0; channelIdx < channels.length; channelIdx++) {
        Channel channel = channels[channelIdx];
        if (channel != null) {
            ChannelDayProgram dayProg = TvDataBase.getInstance().getDayProgram(startDate, channel);
            if (dayProg != null) {
              // This day has data -> Remember it
              lastDayWithData = day;

              // Search this day program
              for (int i = 0; i < dayProg.getProgramCount(); i++) {
                Program prog = dayProg.getProgramAt(i);
                if (matches(prog, fieldArr)) {
                  hitList.add(prog);
                }
              }
            }
        }
      }

      // Give up if we did not find data for the last 10 days
      if ((day - lastDayWithData) > 10) {
        break;
      }

      // The next day
      startDate = startDate.addDays(1);
    }

    // Convert the list into an array
    Program[] hitArr = new Program[hitList.size()];
    hitList.toArray(hitArr);

    // Sort the array if wanted
    if (sortByStartTime) {
      Arrays.sort(hitArr, getStartTimeComparator());
    }

    mSearchRunning = false;
    if (mWaitingDialog != null)
      mWaitingDialog.setVisible(false);
    
    // return the result
    return hitArr;
  }


  private void createDialog() {
    Window comp = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
    if (comp instanceof Dialog) {
      mWaitingDialog = new JDialog((Dialog) comp, false);
    } else {
      mWaitingDialog = new JDialog((Frame) comp, false);
    }
    mWaitingDialog.setUndecorated(true);
    mWaitingDialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));

    JPanel panel = (JPanel) mWaitingDialog.getContentPane();
    panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    panel.setLayout(new FormLayout("fill:3dlu:grow, pref, fill:3dlu:grow", "fill:3dlu:grow, pref, 3dlu, pref, 3dlu, pref, fill:3dlu:grow"));
    CellConstraints cc = new CellConstraints();

    JLabel header = new JLabel(mLocalizer.msg("searching", "Searching"));
    header.setFont(header.getFont().deriveFont(Font.BOLD));

    panel.add(header, cc.xy(2, 2));

    panel.add(
        new JLabel(mLocalizer.msg("pleaseWait", "Please Wait")), cc
            .xy(2, 4));

    JProgressBar bar = new JProgressBar();
    bar.setIndeterminate(true);
    panel.add(bar, cc.xy(2, 6));

    mWaitingDialog.pack();
  };

  /**
   * Checks whether a value matches to the criteria of this searcher.
   * 
   * @param value The value to check
   * @return Whether the value matches.
   */
  protected abstract boolean matches(String value);

}
