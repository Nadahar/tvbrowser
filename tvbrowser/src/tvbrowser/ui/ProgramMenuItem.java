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
package tvbrowser.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.Settings;
import tvbrowser.extras.programinfo.ProgramInfo;
import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;

import devplugin.Program;

/**
 * A class that contains a program in a JMenuItem.
 * 
 * @author René Mach
 * 
 */
public class ProgramMenuItem extends JMenuItem implements ActionListener {

  private static final long serialVersionUID = 1L;
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(ProgramMenuItem.class);

  private Program mProgram;
  private Color mForeground;
  private Insets mBorderInsets;
  boolean mSelected;
  private Timer mTimer;

  /**
   * Creates the JMenuItem.
   * 
   * @param p
   *          The program to show
   * @param showStartTime
   *          If the start time of the program should be shown.
   */
  public ProgramMenuItem(Program p, boolean showStartTime) {
    super();
    mProgram = p;

    StringBuffer buffer = new StringBuffer(
        "<html><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");

    if (Settings.propProgramsInTrayContainsChannel.getBoolean())
      buffer.append("<td width=\"90\"><b>").append(p.getChannel().getName())
          .append("</b></td>");
    if (showStartTime)
      buffer.append("<td width=\"40\"><b>").append(p.getTimeString()).append(
          "</b></td>");

    buffer.append("<td>").append(p.getTitle()).append(
        "</td></tr></table></html>");

    setText(buffer.toString());

    if (Settings.propProgramsInTrayContainsChannelIcon.getBoolean()) {
      setIcon(UiUtilities.createChannelIcon(p.getChannel().getIcon()));
      setVerticalTextPosition(ProgramMenuItem.TOP);
    }

    if (p.getMarkerArr().length > 0)
      setForeground(Color.red);

    mForeground = getForeground();
    mSelected = false;

    addActionListener(this);
    this.addChangeListener(new ChangeListener() {

      public void stateChanged(ChangeEvent e) {
        if (!mSelected) {
          setForeground(UIManager.getDefaults().getColor(
              "MenuItem.selectionForeground") != null ? UIManager.getDefaults()
              .getColor("MenuItem.selectionForeground") : UIManager
              .getDefaults().getColor("Menu.selectionForeground"));
          mSelected = true;
        } else {
          setForeground(mForeground);
          mSelected = false;
        }
      }

    });
    mBorderInsets = (this.getBorder().getBorderInsets(this));
    this.setBorder(BorderFactory.createEmptyBorder(1, mBorderInsets.left, 0,
        mBorderInsets.right));

    if (Settings.propProgramsInTrayShowTooltip.getBoolean()) {
      int end = p.getStartTime() + p.getLength();

      if (end > 1440)
        end -= 1440;

      String hour = String.valueOf(end / 60);
      String minute = String.valueOf(end % 60);

      StringBuffer info = new StringBuffer(p.getShortInfo() == null ? "" : p
          .getShortInfo());

      if (minute.length() == 1)
        minute = "0" + minute;

      for (int i = 20; i < info.length(); i += 24) {
        int index = info.indexOf(" ", i);
        if (index == -1)
          index = info.indexOf("\n", i);
        if (index != -1) {
          info.deleteCharAt(index);
          info.insert(index, "<br>");
          i += index - i;
        }

      }

      StringBuffer toolTip = new StringBuffer(mLocalizer.msg("to", "To: "))
          .append(hour).append(":").append(minute).append(
              info.length() > 0 ? "<br>" : "").append(info);

      setToolTipText("<html>" + toolTip + "</html>");
    }

    mTimer = new Timer(10000, new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        repaint();
      }

    });
  }

  public void actionPerformed(ActionEvent e) {
    ProgramInfo.getInstance().getContextMenuActions(mProgram).getAction()
        .actionPerformed(e);
  }

  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (mProgram.isOnAir()) {
      if (!mTimer.isRunning())
        mTimer.start();

      Graphics2D g2d = (Graphics2D) g;

      int x = Settings.propProgramsInTrayContainsChannelIcon.getBoolean() ? getIcon()
          .getIconWidth()
          + mBorderInsets.left
          : mBorderInsets.left;
      int width = getWidth() - x;
      int height = getHeight();

      Insets i = getInsets();

      int minutesAfterMidnight = IOUtilities.getMinutesAfterMidnight();
      int progLength = mProgram.getLength();
      int startTime = mProgram.getHours() * 60 + mProgram.getMinutes();
      int elapsedMinutes;
      if (minutesAfterMidnight < startTime) {
        // The next day has begun -> we have to add 24 * 60 minutes
        // Example: Start time was 23:50 = 1430 minutes after midnight
        // now it is 0:03 = 3 minutes after midnight
        // elapsedMinutes = (24 * 60) + 3 - 1430 = 13 minutes
        elapsedMinutes = (24 * 60) + minutesAfterMidnight - startTime;
      } else {
        elapsedMinutes = minutesAfterMidnight - startTime;
      }

      int progressX = 0;
      if (progLength > 0) {
        progressX = elapsedMinutes * (width - i.left - i.right) / progLength;
      }

      g2d.setColor(Settings.propProgramTableColorOnAirLight.getColor());
      g2d.fillRect(x + i.left + progressX - i.right - i.left, i.top + 1, width
          - i.right - i.left, height - i.bottom - i.top - 3);
      g2d.setColor(Settings.propProgramTableColorOnAirDark.getColor());

      g2d.fillRect(i.left + x, i.top + 1, progressX - i.right - i.left, height
          - i.bottom - i.top - 3);
    } else if (mProgram.isExpired()) {
      if (mTimer.isRunning())
        mTimer.stop();
      mForeground = Color.gray;
      setForeground(Color.gray);
    }
  }
}
