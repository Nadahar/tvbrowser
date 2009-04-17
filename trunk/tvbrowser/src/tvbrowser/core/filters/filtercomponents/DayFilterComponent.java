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
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.customizableitems.SelectableItemList;
import devplugin.Program;

/**
 * This Filter filters for certain Days of the Week
 * 
 * @author bodum
 * 
 */
public class DayFilterComponent extends AbstractFilterComponent {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(DayFilterComponent.class);
  private final String[] allDays = new String[] {mLocalizer.msg("monday", "Monday"), mLocalizer.msg("tuesday", "Tuesday"), mLocalizer.msg("wednesday", "Wednesday"), mLocalizer.msg("thursday", "Thursday"), mLocalizer.msg("friday", "Friday"), mLocalizer.msg("saturday", "Saturday"), mLocalizer.msg("sunday", "Sunday")};

  private int mSelectedDays;
  private SelectableItemList mList;

  public DayFilterComponent(final String name, final String description) {
    super(name, description);
    mSelectedDays = 0;
  }

  public DayFilterComponent() {
    this("", "");
  }

  public void read(final ObjectInputStream in, final int version) throws IOException,
      ClassNotFoundException {
    mSelectedDays = in.readInt();
  }

  public void write(final ObjectOutputStream out) throws IOException {
    out.writeInt(mSelectedDays);
  }

  @Override
  public String toString() {
    return mLocalizer.msg("day", "Day");
  }

  public void saveSettings() {
    mSelectedDays = 0;
    Object[] selection = mList.getSelection();
    ArrayList<String> listSelection = new ArrayList<String>(selection.length);
    for (Object object : selection) {
      listSelection.add((String) object);
    }
    
    int bit = 1;
    for (int day = 0; day < 7; day++) {
      if (listSelection.contains(allDays[day])) {
        mSelectedDays |= bit;
      }
      bit <<= 1;
    }
  }

  public JPanel getSettingsPanel() {
    final JPanel content = new JPanel(new BorderLayout());
    content.add(new JLabel(mLocalizer.msg("description",
        "This filter accepts programs belonging to the following channels:")),
        BorderLayout.NORTH);

    ArrayList<String> selectedDays = new ArrayList<String>();

    int bit = 1;
    for (int day = 0; day < 7; day++) {
      if ((mSelectedDays & bit) > 0) {
        selectedDays.add(allDays[day]);
      }
      bit <<= 1;
    }

    mList = new SelectableItemList(selectedDays.toArray(), allDays);
    content.add(mList, BorderLayout.CENTER);
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