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
package calendarexportplugin;

import java.util.Properties;

import util.settings.PropertyBasedSettings;

final public class CalendarExportSettings extends PropertyBasedSettings {

  private static final int CLASSIFICATION_PUBLIC = 0;
  private static final int CLASSIFICATION_PRIVATE = 1;
  private static final int CLASSIFICATION_CONFIDENTIAL = 2;
  private static final int SHOW_TIME_BUSY = 0;
  private static final int SHOW_TIME_FREE = 1;

  public CalendarExportSettings(Properties properties) {
    super(properties);
  }

  public boolean getMarkItems() {
    return get(CalendarExportPlugin.PROP_MARK_ITEMS, true);
  }

  public void setNullTime(final boolean value) {
    set(CalendarExportPlugin.PROP_NULLTIME, value);
  }

  public void setCategory(final String category) {
    set(CalendarExportPlugin.PROP_CATEGORY, category);
  }

  public void setUseAlarm(final boolean useAlarm) {
    set(CalendarExportPlugin.PROP_ALARM, useAlarm);
  }

  public String getCategory() {
    return get(CalendarExportPlugin.PROP_CATEGORY, "");
  }

  public boolean getNullTime() {
    return get(CalendarExportPlugin.PROP_NULLTIME, false);
  }

  public void setAlarmMinutes(final int minutes) {
    set(CalendarExportPlugin.PROP_ALARMBEFORE, minutes);
  }

  public void setMarkItems(final boolean mark) {
    set(CalendarExportPlugin.PROP_MARK_ITEMS, mark);
  }

  public boolean getUseAlarm() {
    return get(CalendarExportPlugin.PROP_ALARM, true);
  }

  public int getAlarmMinutes() {
    return get(CalendarExportPlugin.PROP_ALARMBEFORE, 0);
  }

  public void setActiveExporters(final String list) {
    set(CalendarExportPlugin.PROP_ACTIVE_EXPORTER, list);
  }

  public String getActiveExporters() {
    return get(CalendarExportPlugin.PROP_ACTIVE_EXPORTER);
  }

  public boolean isClassificationPublic() {
    return getClassificationInternal() == CLASSIFICATION_PUBLIC;
  }

  private int getClassificationInternal() {
    return get(CalendarExportPlugin.PROP_CLASSIFICATION, CLASSIFICATION_PUBLIC);
  }

  public boolean isClassificationPrivate() {
    return getClassificationInternal() == CLASSIFICATION_PRIVATE;
  }

  public boolean isClassificationConfidential() {
    return getClassificationInternal() == CLASSIFICATION_CONFIDENTIAL;
  }

  public void setClassification(final Classification classification) {
    switch (classification) {
    case Private: {
      setClassification(CLASSIFICATION_PRIVATE);
      break;
    }
    case Confidential: {
      setClassification(CLASSIFICATION_CONFIDENTIAL);
      break;
    }
    default: {
      setClassification(CLASSIFICATION_PUBLIC);
    }
    }
  }

  private void setClassification(int classification) {
    set(CalendarExportPlugin.PROP_CLASSIFICATION, classification);
  }

  public boolean isShowBusy() {
    return getShowTime() == SHOW_TIME_BUSY;
  }

  private int getShowTime() {
    return get(CalendarExportPlugin.PROP_SHOWTIME, SHOW_TIME_BUSY);
  }

  public boolean isShowFree() {
    return getShowTime() == SHOW_TIME_FREE;
  }

  public void setReservation(final Reservation reservation) {
    switch(reservation) {
    case Free: {
      setReservation(SHOW_TIME_FREE);
      break;
    }
    default: {
      setReservation(SHOW_TIME_BUSY);
    }
    }
  }

  private void setReservation(final int showTime) {
    set(CalendarExportPlugin.PROP_SHOWTIME, showTime);
  }

  public void setExporterProperty(final String key, final String value) {
    set(key, value);
  }

  public String getExporterProperty(final String key) {
    return getExporterProperty(key, "");
  }

  public String getExporterProperty(final String key, final String defaultValue) {
    return get(key, defaultValue);
  }

  public Classification getClassification() {
    if (isClassificationConfidential()) {
      return Classification.Confidential;
    }
    else if (isClassificationPrivate()) {
      return Classification.Private;
    }
    return Classification.Public;
  }

  public boolean getExporterProperty(final String key, final boolean defaultValue) {
    return get(key, defaultValue);
  }

  public int getExporterProperty(final String key, final int defaultValue) {
    return get(key, defaultValue);
  }

  public void setExporterProperty(final String key, boolean value) {
    set(key, value);
    // TODO Auto-generated method stub

  }
}
