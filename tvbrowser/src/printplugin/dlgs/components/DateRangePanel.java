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

package printplugin.dlgs.components;

import devplugin.Date;

import javax.swing.*;
import java.awt.*;



public class DateRangePanel extends JPanel {

  static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(DateRangePanel.class);

  private JComboBox mDateCb;
  private JSpinner mDayCountSpinner;


  public DateRangePanel() {

    setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("period","Zeitraum")));

    JPanel pn = new JPanel();
    pn.setLayout(new BoxLayout(pn,BoxLayout.X_AXIS));
    pn.add(new JLabel(mLocalizer.msg("from","Von")));
    pn.add(mDateCb = new JComboBox(createDateObjects(21)));
    pn.add(new JLabel(mLocalizer.msg("TVlistingsFor", "Programm fuer")));
    pn.add(mDayCountSpinner = new JSpinner(new SpinnerNumberModel(5,1,28,1)));
    pn.add(new JLabel(mLocalizer.msg("days","Tage")));


    setLayout(new BorderLayout());
    add(pn, BorderLayout.WEST);



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
    mDateCb.setSelectedItem(date);
  }

  public void setNumberOfDays(int days) {
    mDayCountSpinner.setValue(new Integer(days));
  }

  public Date getFromDate() {
    return (Date)mDateCb.getSelectedItem();
  }

  public int getNumberOfDays() {
    return ((Integer)mDayCountSpinner.getValue()).intValue();
  }

}
