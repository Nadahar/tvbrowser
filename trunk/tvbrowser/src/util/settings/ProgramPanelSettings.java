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

/**
 * A class that contains setting values for the program panel.
 * 
 * @author René Mach
 * @since 2.2.2
 */
public class ProgramPanelSettings {
  /** Show the pictures never */
  public static final int SHOW_PICTURES_NEVER = 0;
  /** Always show the pictures */
  public static final int SHOW_PICTURES_EVER = 1;
  /** Show the pictures in time range */
  public static final int SHOW_PICTURES_IN_TIME_RANGE = 2;
  /** Show the pictures for selected plugins */
  public static final int SHOW_PICTURES_FOR_PLUGINS = 3;
  /** Show the pictures for programs with selected duration */
  public static final int SHOW_PICTURES_FOR_DURATION = 4;
  
  /** Orientation Progressbar in X_AXIS */
  public static final int X_AXIS = 0;
  /** Orientation Progressbar in Y_AXIS */
  public static final int Y_AXIS = 1;
  
  private int mType;
  private int mTimeRangeStart;
  private int mTimeRangeEnd;
  private int mDuration;
  private int mAxis;
  private boolean mShowOnlyDateAndTitle;
  private boolean mShowDescription;
  private boolean mIgnoreProgramImportance;
  private String[] mPluginIds;

  /**
   * Creates an instance of this class with the given values.
   * 
   * @param settings The PluginPictureSettings to be used.
   * @param showOnlyDateAndTitle If the program panel should only contain date and title.
   * @param axis The axis for the ProgramPanel.
   * @since 2.7
   */
  public ProgramPanelSettings(PluginPictureSettings settings, boolean showOnlyDateAndTitle, int axis) {
    mType = settings.isShowingPictures() ? SHOW_PICTURES_EVER : SHOW_PICTURES_NEVER;
    mShowDescription = settings.isShowingDescription();
    
    mShowOnlyDateAndTitle = showOnlyDateAndTitle;
    mAxis = axis;
  }
  
  /**
   * Creates an instance of this class with the given values.
   * 
   * @param settings The PluginPictureSettings to be used.
   * @param showOnlyDateAndTitle If the program panel should only contain date and title.
   * @param axis The axis for the ProgramPanel.
   * @param ignoreProgramImportance If the program importance should be ignored.
   * @since 3.0
   */
  public ProgramPanelSettings(PluginPictureSettings settings, boolean showOnlyDateAndTitle, int axis, boolean ignoreProgramImportance) {
    mType = settings.isShowingPictures() ? SHOW_PICTURES_EVER : SHOW_PICTURES_NEVER;
    mShowDescription = settings.isShowingDescription();
    
    mShowOnlyDateAndTitle = showOnlyDateAndTitle;
    mAxis = axis;
    mIgnoreProgramImportance = ignoreProgramImportance;
  }
  
  /**
   * Creates an instance of this class with the given values.
   * 
   * @param settings The PluginPictureSettings to be used.
   * @param showOnlyDateAndTitle If the program panel should only contain date and title.
   */
  public ProgramPanelSettings(PluginPictureSettings settings, boolean showOnlyDateAndTitle) {
    this(settings, showOnlyDateAndTitle, X_AXIS);
  }
  
  /**
   * Creates an instance of this class with the given values.
   * 
   * @param settings The PluginPictureSettings to be used.
   * @param showOnlyDateAndTitle If the program panel should only contain date and title.
   * @param ignoreProgramImportance If the program importance should be ignored.
   * @since 3.0
   */
  public ProgramPanelSettings(PluginPictureSettings settings, boolean showOnlyDateAndTitle, boolean ignoreProgramImportance) {
    this(settings, showOnlyDateAndTitle, X_AXIS, ignoreProgramImportance);
  }
  
  /**
   * Creates an instance of this class.
   * 
   * @param type The picture showing type.
   * @param timeRangeStart The time range start time.
   * @param timeRangeEnd The time range end time.
   * @param showOnlyDateAndTitle If the program panel should only contain date and title.
   * @param showDescription If the picture description should be shown.
   * @param duration The minimum duration of the programs the pictures should be shown for.
   */
  public ProgramPanelSettings(int type, int timeRangeStart, int timeRangeEnd, boolean showOnlyDateAndTitle, boolean showDescription, int duration) {
    this(type, timeRangeStart, timeRangeEnd,showOnlyDateAndTitle, showDescription, duration, null);
  }
  
  /**
   * Creates an instance of this class.
   * 
   * @param type The picture showing type.
   * @param timeRangeStart The time range start time.
   * @param timeRangeEnd The time range end time.
   * @param showOnlyDateAndTitle If the program panel should only contain date and title.
   * @param showDescription If the picture description should be shown.
   * @param duration The minimum duration of the programs the pictures should be shown for.
   * @param ignoreProgramImportance If the program importance should be ignored.
   * @since 3.0
   */
  public ProgramPanelSettings(int type, int timeRangeStart, int timeRangeEnd, boolean showOnlyDateAndTitle, boolean showDescription, int duration, boolean ignoreProgramImportance) {
    this(type, timeRangeStart, timeRangeEnd,showOnlyDateAndTitle, showDescription, duration, null, ignoreProgramImportance);
  }

  /**
   * Creates an instance of this class.
   * 
   * @param type The picture showing type.
   * @param timeRangeStart The time range start time.
   * @param timeRangeEnd The time range end time.
   * @param showOnlyDateAndTitle If the program panel should only contain date and title.
   * @param showDescription If the picture description should be shown.
   * @param duration The minimum duration of the programs the pictures should be shown for.
   * @param axis The axis for the progress bar of the program panel.
   * @sine 2.7
   */
  public ProgramPanelSettings(int type, int timeRangeStart, int timeRangeEnd, boolean showOnlyDateAndTitle, boolean showDescription, int duration, int axis) {
    this(type, timeRangeStart, timeRangeEnd,showOnlyDateAndTitle, showDescription, duration, null, axis);
  }
  
  /**
   * Creates an instance of this class.
   * 
   * @param type The picture showing type.
   * @param timeRangeStart The time range start time.
   * @param timeRangeEnd The time range end time.
   * @param showOnlyDateAndTitle If the program panel should only contain date and title.
   * @param showDescription If the picture description should be shown.
   * @param pluginIds The ids of the plugins to show the pictures for.
   * @param duration The minimum duration of the programs the pictures should be shown for.
   */
  public ProgramPanelSettings(int type, int timeRangeStart, int timeRangeEnd, boolean showOnlyDateAndTitle, boolean showDescription, int duration, String[] pluginIds) {
    this(type,timeRangeStart,timeRangeEnd,showOnlyDateAndTitle,showDescription,duration,pluginIds,Y_AXIS);
  }

  /**
   * Creates an instance of this class.
   * 
   * @param type The picture showing type.
   * @param timeRangeStart The time range start time.
   * @param timeRangeEnd The time range end time.
   * @param showOnlyDateAndTitle If the program panel should only contain date and title.
   * @param showDescription If the picture description should be shown.
   * @param pluginIds The ids of the plugins to show the pictures for.
   * @param duration The minimum duration of the programs the pictures should be shown for.
   * @param ignoreProgramImportance If the program importance should be ignored.
   * @since 3.0
   */
  public ProgramPanelSettings(int type, int timeRangeStart, int timeRangeEnd, boolean showOnlyDateAndTitle, boolean showDescription, int duration, String[] pluginIds, boolean ignoreProgramImportance) {
    this(type,timeRangeStart,timeRangeEnd,showOnlyDateAndTitle,showDescription,duration,pluginIds,Y_AXIS,ignoreProgramImportance);
  }

  
  /**
   * Creates an instance of this class.
   * 
   * @param type The picture showing type.
   * @param timeRangeStart The time range start time.
   * @param timeRangeEnd The time range end time.
   * @param showOnlyDateAndTitle If the program panel should only contain date and title.
   * @param showDescription If the picture description should be shown.
   * @param pluginIds The ids of the plugins to show the pictures for.
   * @param duration The minimum duration of the programs the pictures should be shown for.
   * @param axis The axis for the progress bar of the program panel.
   * @since 2.7
   */
  public ProgramPanelSettings(int type, int timeRangeStart, int timeRangeEnd, boolean showOnlyDateAndTitle, boolean showDescription, int duration, String[] pluginIds, int axis) {
    this(type,timeRangeStart,timeRangeEnd,showOnlyDateAndTitle,showDescription,duration,pluginIds,axis,false);
  }
  
  /**
   * Creates an instance of this class.
   * 
   * @param type The picture showing type.
   * @param timeRangeStart The time range start time.
   * @param timeRangeEnd The time range end time.
   * @param showOnlyDateAndTitle If the program panel should only contain date and title.
   * @param showDescription If the picture description should be shown.
   * @param pluginIds The ids of the plugins to show the pictures for.
   * @param duration The minimum duration of the programs the pictures should be shown for.
   * @param axis The axis for the progress bar of the program panel.
   * @param ignoreProgramImportance If the program importance should be ignored.
   * @since 3.0
   */
  public ProgramPanelSettings(int type, int timeRangeStart, int timeRangeEnd, boolean showOnlyDateAndTitle, boolean showDescription, int duration, String[] pluginIds, int axis, boolean ignoreProgramImportance) {
    mType = type;
    mTimeRangeStart = timeRangeStart;
    mTimeRangeEnd = timeRangeEnd;
    mShowOnlyDateAndTitle = showOnlyDateAndTitle;
    mShowDescription = showDescription;
    mPluginIds = pluginIds;
    mDuration = duration;
    mAxis = axis;
    mIgnoreProgramImportance = ignoreProgramImportance;
  }
  
  /**
   * @return If the type of the picture showing is set to show pictures in time range.
   */
  public boolean isShowingPictureInTimeRange() {
    return mType == SHOW_PICTURES_IN_TIME_RANGE ||
           mType == SHOW_PICTURES_IN_TIME_RANGE + SHOW_PICTURES_FOR_DURATION + SHOW_PICTURES_FOR_PLUGINS ||
           mType == SHOW_PICTURES_IN_TIME_RANGE + SHOW_PICTURES_FOR_DURATION ||
           mType == SHOW_PICTURES_IN_TIME_RANGE + SHOW_PICTURES_FOR_PLUGINS;
  }

  /**
   * @return If the type of the picture showing is set to show picture always.
   */
  public boolean isShowingPictureEver() {
    return mType == SHOW_PICTURES_EVER;
  }
  
  /**
   * @return If the type of the picture showing is set to show picture never.
   */
  public boolean isShowingPictureNever() {
    return mType == SHOW_PICTURES_NEVER;
  }
  
  /**
   * @return If the type of the picture showing is set to show picture for plugins.
   */
  public boolean isShowingPictureForPlugins() {
    return mType == SHOW_PICTURES_FOR_PLUGINS ||
           mType == SHOW_PICTURES_FOR_PLUGINS + SHOW_PICTURES_IN_TIME_RANGE + SHOW_PICTURES_FOR_DURATION ||
           mType == SHOW_PICTURES_FOR_PLUGINS + SHOW_PICTURES_IN_TIME_RANGE ||
           mType == SHOW_PICTURES_FOR_PLUGINS + SHOW_PICTURES_FOR_DURATION;
  }
  
  /**
   * @return True if the type of the picture showing is set to show picture for duration.
   */
  public boolean isShowingPictureForDuration() {
    return mType == SHOW_PICTURES_FOR_DURATION ||
           mType == SHOW_PICTURES_FOR_DURATION + SHOW_PICTURES_FOR_PLUGINS + SHOW_PICTURES_IN_TIME_RANGE ||
           mType == SHOW_PICTURES_FOR_DURATION + SHOW_PICTURES_FOR_PLUGINS ||
           mType == SHOW_PICTURES_FOR_DURATION + SHOW_PICTURES_IN_TIME_RANGE;
  }
   
  /**
   * @return If the program panel should only contain date and title.
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
  
  /**
   * @return The duration value
   */
  public int getDuration() {
    return mDuration;
  }
  
  /**
   * @return The plugin ids to show the pictures for.
   */
  public String[] getPluginIds() {
    return mPluginIds;
  }
  
  /**
   * Checks if a given type to check contains a type.
   * 
   * @param typeToCheck The type to check.
   * @param containingType The type to which should the typeToCheck is to check for.
   * @return True if the typeToCheck contains the containingType
   */
  public static boolean typeContainsType(int typeToCheck, int containingType) {
    if(containingType == SHOW_PICTURES_FOR_PLUGINS) {
      return
        typeToCheck == SHOW_PICTURES_FOR_PLUGINS ||
        typeToCheck == SHOW_PICTURES_FOR_PLUGINS + SHOW_PICTURES_IN_TIME_RANGE + SHOW_PICTURES_FOR_DURATION ||
        typeToCheck == SHOW_PICTURES_FOR_PLUGINS + SHOW_PICTURES_IN_TIME_RANGE ||
        typeToCheck == SHOW_PICTURES_FOR_PLUGINS + SHOW_PICTURES_FOR_DURATION;
    } else if(containingType == SHOW_PICTURES_FOR_DURATION) {
      return
        typeToCheck == SHOW_PICTURES_FOR_DURATION ||
        typeToCheck == SHOW_PICTURES_FOR_DURATION + SHOW_PICTURES_FOR_PLUGINS + SHOW_PICTURES_IN_TIME_RANGE ||
        typeToCheck == SHOW_PICTURES_FOR_DURATION + SHOW_PICTURES_FOR_PLUGINS ||
        typeToCheck == SHOW_PICTURES_FOR_DURATION + SHOW_PICTURES_IN_TIME_RANGE;
    } else if(containingType == SHOW_PICTURES_IN_TIME_RANGE) {
      return
        typeToCheck == SHOW_PICTURES_IN_TIME_RANGE ||
        typeToCheck == SHOW_PICTURES_IN_TIME_RANGE + SHOW_PICTURES_FOR_DURATION + SHOW_PICTURES_FOR_PLUGINS ||
        typeToCheck == SHOW_PICTURES_IN_TIME_RANGE + SHOW_PICTURES_FOR_DURATION ||
        typeToCheck == SHOW_PICTURES_IN_TIME_RANGE + SHOW_PICTURES_FOR_PLUGINS;
    } else {
      return typeToCheck == containingType;
    }
  }
  
  /**
   * Gets the axis for the ProgramPanel.
   * @return The axis for the ProgramPanel.
   */
  public int getAxis() {
    return mAxis;
  }
  
  /**
   * Sets the value for the only date and title flag.
   * <p>
   * @param value The new value for the only date and tilte flag.
   * @since 2.7
   */
  public void setShowOnlyDateAndTitle(boolean value) {
    mShowOnlyDateAndTitle = value;
  }
  
  /**
   * Gets if the program importance should be ignored.
   * <p>
   * @return If the program importance should be ignored.
   */
  public boolean isIgnoringProgramImportance() {
    return mIgnoreProgramImportance;
  }
}
