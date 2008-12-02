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

package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.TabLayout;
import devplugin.Program;

/**
 * This Filter Filters for certain Days of the Week
 * 
 * @author bodum
 * 
 */
public class DayFilterComponent extends AbstractFilterComponent {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(DayFilterComponent.class);

  private int mSelectedDays;
  JCheckBox mMonday, mTuesday, mWednesday, mThursday, mFriday, mSaturday,
      mSunday;

  public DayFilterComponent(String name, String description) {
    super(name, description);
    mSelectedDays = 0;
  }

  public DayFilterComponent() {
    this("", "");
  }

  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    mSelectedDays = in.readInt();
  }

  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mSelectedDays);
  }

  @Override
  public String toString() {
    return mLocalizer.msg("day", "Day");
  }

  public void saveSettings() {
    mSelectedDays = 0;

    if (mMonday.isSelected()) {
      mSelectedDays = mSelectedDays | 1;
    }
    if (mTuesday.isSelected()) {
      mSelectedDays = mSelectedDays | 2;
    }
    if (mWednesday.isSelected()) {
      mSelectedDays = mSelectedDays | 4;
    }
    if (mThursday.isSelected()) {
      mSelectedDays = mSelectedDays | 8;
    }
    if (mFriday.isSelected()) {
      mSelectedDays = mSelectedDays | 16;
    }
    if (mSaturday.isSelected()) {
      mSelectedDays = mSelectedDays | 32;
    }
    if (mSunday.isSelected()) {
      mSelectedDays = mSelectedDays | 64;
    }
  }

  public JPanel getSettingsPanel() {
    JPanel content = new JPanel(new TabLayout(1, 0, 5));
    content.add(new JLabel(mLocalizer.msg("description",
        "This filter accepts programs belonging to the following channels:")),
        BorderLayout.NORTH);

    mMonday = new JCheckBox(mLocalizer.msg("monday", "Monday"));
    content.add(mMonday);

    if ((mSelectedDays & 1) > 0) {
      mMonday.setSelected(true);
    }

    mTuesday = new JCheckBox(mLocalizer.msg("tuesday", "Tuesday"));
    content.add(mTuesday);

    if ((mSelectedDays & 2) > 0) {
      mTuesday.setSelected(true);
    }

    mWednesday = new JCheckBox(mLocalizer.msg("wednesday", "Wednesday"));
    content.add(mWednesday);

    if ((mSelectedDays & 4) > 0) {
      mWednesday.setSelected(true);
    }

    mThursday = new JCheckBox(mLocalizer.msg("thursday", "Thursday"));
    content.add(mThursday);

    if ((mSelectedDays & 8) > 0) {
      mThursday.setSelected(true);
    }

    mFriday = new JCheckBox(mLocalizer.msg("friday", "Friday"));
    content.add(mFriday);

    if ((mSelectedDays & 16) > 0) {
      mFriday.setSelected(true);
    }

    mSaturday = new JCheckBox(mLocalizer.msg("saturday", "Saturday"));
    content.add(mSaturday);

    if ((mSelectedDays & 32) > 0) {
      mSaturday.setSelected(true);
    }

    mSunday = new JCheckBox(mLocalizer.msg("sunday", "Sunday"));
    content.add(mSunday);

    if ((mSelectedDays & 64) > 0) {
      mSunday.setSelected(true);
    }

    return content;
  }

  public boolean accept(Program program) {
    int day = program.getDate().getCalendar().get(Calendar.DAY_OF_WEEK);

    if ((day == Calendar.MONDAY) && ((mSelectedDays & 1) > 0)) {
      return true;
    }
    if ((day == Calendar.TUESDAY) && ((mSelectedDays & 2) > 0)) {
      return true;
    }
    if ((day == Calendar.WEDNESDAY) && ((mSelectedDays & 8) > 0)) {
      return true;
    }
    if ((day == Calendar.THURSDAY) && ((mSelectedDays & 4) > 0)) {
      return true;
    }
    if ((day == Calendar.FRIDAY) && ((mSelectedDays & 16) > 0)) {
      return true;
    }
    if ((day == Calendar.SATURDAY) && ((mSelectedDays & 32) > 0)) {
      return true;
    }
    if ((day == Calendar.SUNDAY) && ((mSelectedDays & 64) > 0)) {
      return true;
    }

    return false;
  }

  public int getVersion() {
    return 1;
  }
}