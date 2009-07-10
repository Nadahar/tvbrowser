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

package tvbrowser.ui.mainframe;

import util.ui.Localizer;

public class PeriodItem {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(PeriodItem.class);

  private int mDays;

  private static final PeriodItem[] PERIOD_ARR = {
    new PeriodItem(0), new PeriodItem(1), new PeriodItem(2), new PeriodItem(3),
    new PeriodItem(7), new PeriodItem(14),  new PeriodItem(21),  new PeriodItem(UpdateDlg.GETALL)
  };

  public PeriodItem(int days) {
    mDays = days;
  }


  public String toString() {
    switch (mDays) {
      case 0 : return Localizer.getLocalization(Localizer.I18N_TODAY);
      case 1 : return mLocalizer.msg("period.1", "Up to tomorrow");
      case 2 : return mLocalizer.msg("period.2", "Next 2 days");
      case 3 : return mLocalizer.msg("period.3", "Next 3 days");
      case 7 : return mLocalizer.msg("period.7", "Next week");
      case 14 : return mLocalizer.msg("period.14", "Next 2 week");
      case 21 : return mLocalizer.msg("period.21", "Next 3 week");
      case UpdateDlg.GETALL : return mLocalizer.msg("period.1000", "All");
      default : return "---";
    }
  }

  public int getDays() {
    return mDays;
  }



  public int hashCode() {
    return mDays;
  }

  public boolean equals(Object o) {
    if (o instanceof PeriodItem) {
      PeriodItem p = (PeriodItem)o;
      return p.getDays() == mDays;
    }
    return false;
  }
  
  public static final PeriodItem[] getPeriodItems() {
    return PERIOD_ARR.clone();
  }

}
