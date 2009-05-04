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

import javax.swing.JPanel;

import tvbrowser.core.TvDataBase;
import devplugin.Date;

/**
 * common super class for date selection UI components
 * 
 * @author Bananeweizen
 * 
 */
public abstract class AbstractDateSelector extends JPanel {
  private Date mCurChoosenDate = Date.getCurrentDate();
  protected Date mToday;

  protected abstract void markDate(Date addDays);

  protected void setCurrentDate(final Date d) {
    mCurChoosenDate = d;
  }
  
  protected boolean isValidDate(final Date date) {
    return TvDataBase.getInstance().dataAvailable(date);
  }

  public void markPreviousDate() {
    markDate(mCurChoosenDate.addDays(-1));
  }

  public void markNextDate() {
    markDate(mCurChoosenDate.addDays(1));
  }

  /**
   * @since 2.7
   */
  public void markNextWeek() {
    markDate(mCurChoosenDate.addDays(7));
  }

  /**
   * @since 2.7
   */
  public void markPreviousWeek() {
    markDate(mCurChoosenDate.addDays(-7));

  }

  public Date getSelectedDate() {
    return mCurChoosenDate;
  }

}
