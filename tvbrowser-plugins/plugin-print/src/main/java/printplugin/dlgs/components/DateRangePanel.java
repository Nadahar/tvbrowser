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
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */

package printplugin.dlgs.components;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Date;

public class DateRangePanel extends JPanel {

  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(DateRangePanel.class);

  private JComboBox mDateCb;
  private JSpinner mDayCountSpinner;

  public DateRangePanel() {
    CellConstraints cc = new CellConstraints();
    
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,2dlu,pref,2dlu,pref,2dlu,pref,2dlu,pref:grow",
        "pref,5dlu,pref,10dlu"), this);
    pb.addSeparator(mLocalizer.msg("period","Period"), cc.xyw(1,1,10));
    pb.addLabel(mLocalizer.msg("from","Von"), cc.xy(2,3));
    pb.add(mDateCb = new JComboBox(createDateObjects(21)), cc.xy(4,3));
    pb.addLabel(mLocalizer.msg("TVlistingsFor", "Program for"), cc.xy(6,3));
    pb.add(mDayCountSpinner = new JSpinner(new SpinnerNumberModel(5,1,28,1)), cc.xy(8,3));
    pb.addLabel(mLocalizer.msg("days","Days"), cc.xy(10,3));
  }

  private Date[] createDateObjects(int days) {
    Date[] result = new Date[days];
    Date today = Date.getCurrentDate();
    
    for (int i=0;i<result.length;i++) {
      result[i]=today.addDays(i);
    }
    
    return result;
  }

  public void setFromDate(Date date) {
    if (date != null) {
      mDateCb.setSelectedItem(date);
    }
  }

  public void setNumberOfDays(int days) {
    mDayCountSpinner.setValue(days);
  }

  public Date getFromDate() {
    return (Date)mDateCb.getSelectedItem();
  }

  public int getNumberOfDays() {
    return ((Integer)mDayCountSpinner.getValue()).intValue();
  }

}
