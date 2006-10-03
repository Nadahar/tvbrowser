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
 */
package util.settings;

import util.ui.PictureSettingsPanel;

/**
 * A class that contains setting values for the program panel.
 * 
 * @author Ren� Mach
 * @since 2.2.2
 */
public class ProgramPanelSettings {
  private int mType;
  private int mTimeRangeStart;
  private int mTimeRangeEnd;
  private boolean mShowOnlyDateAndTitle; 
  private boolean mShowDescription;
  
  /**
   * Creates an instance of this class.
   * 
   * @param type The picture showing type.
   * @param timeRangeStart The time range start time.
   * @param timeRangeEnd The time range end time.
   * @param showOnlyDateAndTitle If the program panel should only contain date and title.
   * @param showDescription If the picture description should be shown.
   */
  public ProgramPanelSettings(int type, int timeRangeStart, int timeRangeEnd, boolean showOnlyDateAndTitle, boolean showDescription) {
    mType = type;
    mTimeRangeStart = timeRangeStart;
    mTimeRangeEnd = timeRangeEnd;
    mShowOnlyDateAndTitle = showOnlyDateAndTitle;
    mShowDescription = showDescription;
  }
  
  /**
   * @return If the type of the picture showing is set to show pictures in time range. 
   */
  public boolean isShowingPictureInTimeRange() {
    return mType == PictureSettingsPanel.SHOW_IN_TIME_RANGE;
  }

  /**
   * @return If the type of the picture showing is set to show picture always.
   */
  public boolean isShowingPictureEver() {
    return mType == PictureSettingsPanel.SHOW_EVER;
  }
  
  /**
   * @return If the type of the picture showing is set to show picture never.
   */
  public boolean isShowingPictureNever() {
    return mType == PictureSettingsPanel.SHOW_NEVER;
  }
   
  /**
   * @return If the program panel should only containg date and title.
   */
  public boolean isShowingOnlyDateAndTitle() {
    return mShowOnlyDateAndTitle;
  }

  /**
   * @return If the picture description should be shown.
   */
  public boolean isShowingPictureDescription() {
    return mShowDescription;
  }
  
  /**
   * @return The type of the picture showing.
   */
  public int getPictureShowingType() {
    return mType;
  }
  
  /**
   * @return The time range start time.
   */
  public int getPictureTimeRangeStart() {
    return mTimeRangeStart;
  }

  /**
   * @return The time range end time.
   */
  public int getPictureTimeRangeEnd() {
    return mTimeRangeEnd;
  }
}
