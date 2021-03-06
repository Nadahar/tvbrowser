/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package tvbrowser.ui.finder;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import devplugin.Date;
import tvbrowser.core.TvDataBase;
import tvbrowser.ui.mainframe.MainFrame;

/**
 * common super class for date selection UI components
 * 
 * @author Bananeweizen
 * 
 */
public abstract class AbstractDateSelector extends JPanel implements
    MouseListener {
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(AbstractDateSelector.class);

  private Date mCurChoosenDate = Date.getCurrentDate();
  protected Date mToday;

  protected abstract void markDate(Date addDays, boolean informPluginPanels);
  
  protected abstract void markDate(Date addDays, Runnable callback, boolean informPluginPanels);

  protected void setCurrentDate(final Date d) {
    mCurChoosenDate = d;
  }
  
  protected boolean isValidDate(final Date date) {
    return TvDataBase.getInstance().dataAvailable(date);
  }

  public void markPreviousDate() {
    markDate(mCurChoosenDate.addDays(-1),true);
  }

  public void markNextDate() {
    markDate(mCurChoosenDate.addDays(1),true);
  }
  
  public void markPreviousDate(Runnable callback) {
    markDate(mCurChoosenDate.addDays(-1),callback,true);
  }

  public void markNextDate(Runnable callback) {
    markDate(mCurChoosenDate.addDays(1),callback,true);
  }

  /**
   * @since 2.7
   */
  public void markNextWeek() {
    markDate(mCurChoosenDate.addDays(7),true);
  }

  /**
   * @since 2.7
   */
  public void markPreviousWeek() {
    markDate(mCurChoosenDate.addDays(-7),true);

  }

  public Date getSelectedDate() {
    return mCurChoosenDate;
  }

  public void mousePressed(final MouseEvent e) {
    if (e.isPopupTrigger()) {
      showPopup(e);
    }
  }

  public void mouseReleased(final MouseEvent e) {
    if (e.isPopupTrigger()) {
      showPopup(e);
    }
  }
  
  /**
   * Show the Popup of the FinderPanel
   * 
   * @param e The mouse event.
   */
  protected void showPopup(final MouseEvent e) {

    JPopupMenu menu = new JPopupMenu();

    JMenuItem update = new JMenuItem(mLocalizer.msg("update", "Update"));

    update.addActionListener(evt -> {
      MainFrame.getInstance().updateTvData();
    });

    menu.add(update);

    int x = e.getX() + ((JComponent) e.getSource()).getX();
    int y = e.getY() + ((JComponent) e.getSource()).getY();

    menu.show(this, x, y);
  }

  public void mouseClicked(final MouseEvent event) {
  }

  public void mouseEntered(final MouseEvent arg0) {
  }

  public void updateContent() {
    Date date = Date.getCurrentDate();
    if (date.equals(mToday)) {
      return;
    }

    mToday = date;
    rebuildControls();
  }

  abstract protected void rebuildControls();

  protected Date getFirstDate() {
    return Date.getCurrentDate().addDays(-1);
  }
  
  final protected void askForDataUpdate(final Date date) {
    int numberOfDays = date.getNumberOfDaysSince(Date.getCurrentDate());
    if (numberOfDays >= 0) {
      MainFrame.getInstance().askForDataUpdate(
          mLocalizer.msg("noDataFor", "No TV data available for {0}.", date
              .toString()), numberOfDays);
    }
  }
  
  public Date getCurrentDate() {
    return mCurChoosenDate;
  }
}
