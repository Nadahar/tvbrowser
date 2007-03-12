/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package util.ui;

import devplugin.ContextMenuIf;
import devplugin.Marker;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramInfoHelper;
import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import util.io.IOUtilities;
import util.program.ProgramUtilities;
import util.settings.ProgramPanelSettings;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * A ProgramPanel is a JComponent representing a single program.
 * 
 * @author Martin Oberhauser
 * @author Til Schneider, www.murfman.de
 */
public class ProgramPanel extends JComponent implements ChangeListener {

  private static java.util.logging.Logger mLog = java.util.logging.Logger
      .getLogger(TextAreaIcon.class.getName());

  private static final boolean USE_FULL_HEIGHT = true;
  private static final boolean PAINT_EXPIRED_PROGRAMS_PALE = true;

  private static final Composite NORMAL_COMPOSITE = AlphaComposite.SrcOver;
  private static final Composite PALE_COMPOSITE = AlphaComposite.getInstance(
      AlphaComposite.SRC_OVER, 0.5F);

  /** The title font. */
  private static Font mTitleFont;
  /** The time font. */
  private static Font mTimeFont;
  /** The normal font */
  private static Font mNormalFont;

  /** The width of the left part (the time). */
  private static int WIDTH_LEFT = -1;
  /** The width of the left part (the title and short info). */
  private static int WIDTH_RIGHT = Settings.propColumnWidth.getInt() - WIDTH_LEFT;
  /** The total width. */
  private static int WIDTH = WIDTH_LEFT + WIDTH_RIGHT;

  /** Formatter for the Time-String */
  private static final TimeFormatter TIME_FORMATTER = new TimeFormatter(); 
  
  /** The height. */
  private int mHeight = 0;
  /**
   * The preferred height.
   * <p>
   * It's the height the panel has with a maximum of 3 information rows.
   */
  private int mPreferredHeight = 0;
  /** The start time as String. */
  private String mProgramTimeAsString;
  /** The icon used to render the title. */
  private TextAreaIcon mTitleIcon;
  /** The icon used to render the description. */
  private TextAreaIcon mDescriptionIcon;
  
  /** The icon used to paint the picture with */
  private PictureAreaIcon mPictureAreaIcon;
  
  /** The icons to show on the left side under the start time. */
  private Icon[] mIconArr;
  /** The program. */
  private Program mProgram;

  /** Color of the Text */
  private Color mTextColor = Color.BLACK;

  /** Panel under a Mouse ? */
  private boolean mMouseOver = false;

  private boolean mIsSelected = false;

  /** Orientation Progressbar in X_AXIS */
  final public static int X_AXIS = 0;
  /** Orientation Progressbar in Y_AXIS */
  final public static int Y_AXIS = 1;
  /** Orientation of Progressbar */
  private int mAxis = Y_AXIS;
  
  /** The vertical gap between the programs */
  private static int V_GAP = 5;
  
  private ProgramPanelSettings mSettings;

  /**
   * Creates a new instance of ProgramPanel.
   */
  public ProgramPanel() {
    this(new ProgramPanelSettings(Settings.propPictureType.getInt(), Settings.propPictureStartTime.getInt(), Settings.propPictureEndTime.getInt(), false, Settings.propIsPictureShowingDescription.getBoolean(), Settings.propPictureDuration.getInt(), Settings.propPicturePluginIds.getStringArray()));
  }

  /**
   * Creates a new instance of ProgramPanel.
   * 
   * @param showOnlyDateAndTitle
   *          If this panel should only show date time and title.
   * @since 2.2.1
   * @deprecated Since 2.2.2 Use {@link #ProgramPanel(ProgramPanelSettings)} instead.
   */
  public ProgramPanel(boolean showOnlyDateAndTitle) {
    this(new ProgramPanelSettings(Settings.propPictureType.getInt(), Settings.propPictureStartTime.getInt(), Settings.propPictureEndTime.getInt(), showOnlyDateAndTitle, Settings.propIsPictureShowingDescription.getBoolean(), Settings.propPictureDuration.getInt(), Settings.propPicturePluginIds.getStringArray()));
  }

  
  /**
   * Creates a new instance of ProgramPanel.
   * @param settings The settings for this program panel. 
   * 
   * @since 2.2.2
   */
  public ProgramPanel(ProgramPanelSettings settings) {
    mSettings = settings;
    
    if (mTitleFont == null) {
      updateFonts();
    }

    if (WIDTH_LEFT == -1) {
      WIDTH_LEFT = getFontMetrics(mTimeFont).stringWidth(TIME_FORMATTER.formatTime(24, 59))+7;
      WIDTH_RIGHT = Settings.propColumnWidth.getInt() - WIDTH_LEFT;
      WIDTH = WIDTH_LEFT + WIDTH_RIGHT;
    }
    
    mTitleIcon = new TextAreaIcon(null, mTitleFont, WIDTH_RIGHT - 5);
    mDescriptionIcon = new TextAreaIcon(null, mNormalFont, WIDTH_RIGHT - 5);
    mDescriptionIcon.setMaximumLineCount(3);
    
    setBackground(UIManager.getColor("programPanel.background"));
  }

  /**
   * Creates a new instance of ProgramPanel.
   * 
   * @param prog
   *          The program to show in this panel.
   */
  public ProgramPanel(Program prog) {
    this();
    setProgram(prog);
  }

  /**
   * Creates a new instance of ProgramPanel.
   * 
   * @param prog
   *          The program to show in this panel.
   * @param axis
   *          Orientation of ProgressBar (X_AXIS/Y_AXIS)
   */
  public ProgramPanel(Program prog, int axis) {
    this();
    mAxis = axis;
    setProgram(prog);
  }

  /**
   * Creates a new instance of ProgramPanel.
   * 
   * @param prog
   *          The program to show in this panel.
   * @param showOnlyDateAndTitle
   *          If this panel should only show date time and title.
   * 
   * @since 2.2.1
   * @deprecated Since 2.2.2 Use {@link #ProgramPanel(Program, ProgramPanelSettings)} instead.
   */
  public ProgramPanel(Program prog, boolean showOnlyDateAndTitle) {
    this(showOnlyDateAndTitle);
    setProgram(prog);
  }
  
  /**
   * Creates a new instance of ProgramPanel.
   * 
   * @param prog
   *          The program to show in this panel.
   * @param axis
   *          Orientation of ProgressBar (X_AXIS/Y_AXIS)
   * @param showOnlyDateAndTitle
   *          If this panel should only show date time and title.
   * 
   * @since 2.2.1
   * @deprecated Since 2.2.2 Use {@link #ProgramPanel(Program, int, ProgramPanelSettings)} instead.
   */
  public ProgramPanel(Program prog, int axis, boolean showOnlyDateAndTitle) {
    this(showOnlyDateAndTitle);
    mAxis = axis;
    setProgram(prog);
  }
  
  /**
   * Creates a new instance of ProgramPanel.
   * 
   * @param prog
   *          The program to show in this panel.
   * @param settings The settings for this program panel.
   *
   * @since 2.2.2
   */
  public ProgramPanel(Program prog, ProgramPanelSettings settings) {
    this(settings);
    setProgram(prog);
  }

  /**
   * Creates a new instance of ProgramPanel.
   * 
   * @param prog
   *          The program to show in this panel.
   * @param axis
   *          Orientation of ProgressBar (X_AXIS/Y_AXIS)
   * @param settings The settings for this program panel.
   *
   * @since 2.2.2
   */
  public ProgramPanel(Program prog, int axis, ProgramPanelSettings settings) {
    this(settings);
    mAxis = axis;
    setProgram(prog);
  }

  public ProgramPanel(ProgramPanelSettings settings, int axis) {
	  this(settings);
	  mAxis = axis;
}

/**
   * (Re)Loads the font settings.
   */
  public static void updateFonts() {
    boolean useDefaults = Settings.propUseDefaultFonts.getBoolean();
    if (useDefaults) {
      mTitleFont = Settings.propProgramTitleFont.getDefault();
      mTimeFont = Settings.propProgramTimeFont.getDefault();
      mNormalFont = Settings.propProgramInfoFont.getDefault();
    } else {
      mTitleFont = Settings.propProgramTitleFont.getFont();
      mTimeFont = Settings.propProgramTimeFont.getFont();
      mNormalFont = Settings.propProgramInfoFont.getFont();
    }

  }
  
  /**
   * Change the settings of this panel.
   * 
   * @param settings The settings of this panel.
   */
  public void setProgramPanelSettings(ProgramPanelSettings settings) {
    mSettings = settings;
  }

  /**
   * Repaints the complete panel.
   */
  public void forceRepaint() {
    mTitleIcon = new TextAreaIcon(null, mTitleFont, WIDTH_RIGHT - 5);
    mDescriptionIcon = new TextAreaIcon(null, mNormalFont, WIDTH_RIGHT - 5);
    mDescriptionIcon.setMaximumLineCount(3);
    Program p = mProgram;
    mProgram = null;
    setProgram(p);
  }

  /**
   * (Re)Loads the column width settings.
   */
  public static void updateColumnWidth() {
    WIDTH_RIGHT = Settings.propColumnWidth.getInt() - WIDTH_LEFT;
    WIDTH = WIDTH_LEFT + WIDTH_RIGHT;
  }

  /**
   * Gets the preferred height.
   * <p>
   * It's the height the panel has with a maximum of 3 information rows.
   * 
   * @return The preferred height.
   */
  public int getPreferredHeight() {
    return mPreferredHeight;
  }

  /**
   * Sets the height of this panel
   * 
   * @param height
   */
  public void setHeight(int height) {
    if (mHeight != height) {
      setProgram(getProgram(), height);
      mHeight = height;
    }
  }

  /**
   * Gets the height.
   * 
   * @return The height.
   */
  public int getHeight() {
    return mHeight;
  }

  /**
   * Sets the program this panel shows.
   * 
   * @param program
   *          The program to show in this panel.
   */
  public void setProgram(devplugin.Program program) {
    setProgram(program, -1);
  }

  /**
   * Sets the program this panel shows.
   * 
   * @param program
   *          The program to show in this panel.
   * @param maxHeight
   *          The maximum height the program should have (in pixels).
   */
  public void setProgram(devplugin.Program program, int maxHeight) {    
    Program oldProgram = mProgram;
    mProgram = program;
    
    mTitleIcon.setMaximumLineCount(-1);
    mDescriptionIcon.setMaximumLineCount(-1);    
    
    boolean programChanged = (oldProgram != program);
    if (programChanged) {
      // Get the start time
      mProgramTimeAsString = program.getTimeString();

      // Set the new title
      mTitleIcon.setText(program.getTitle());

      if(!mSettings.isShowingOnlyDateAndTitle() && mProgram.getProgramState() != Program.WAS_DELETED_STATE)
        programHasChanged();
    }
    /* This is for debugging of the marking problem after a data update */
    else if(program.getProgramState() == Program.WAS_DELETED_STATE) {
      setForeground(Color.red);
    } else if(program.getProgramState() == Program.WAS_UPDATED_STATE) {
      setForeground(Color.blue);
    }
    
    boolean dontShow = true;
    
    if(mSettings.isShowingPictureForPlugins()) {
      String[] pluginIds = mSettings.getPluginIds();
      Marker[] markers = mProgram.getMarkerArr();
      
      if(markers != null && pluginIds != null) {
        for (Marker marker : markers) {
          for (String pluginId : pluginIds) {
            if(marker.getId().compareTo(pluginId) == 0) {
              dontShow = false;
              break;
            }
          }
        }
      }
    }
    
    // Create the picture area icon
    if(!mSettings.isShowingOnlyDateAndTitle() && mProgram.getBinaryField(ProgramFieldType.PICTURE_TYPE) != null && ( 
        mSettings.isShowingPictureEver() || !dontShow || 
        (mSettings.isShowingPictureInTimeRange() && 
         !ProgramUtilities.isNotInTimeRange(mSettings.getPictureTimeRangeStart(),mSettings.getPictureTimeRangeEnd(),program)) ||
         (mSettings.isShowingPictureForDuration() && mSettings.getDuration() <= program.getLength())
         )) {
      mPictureAreaIcon = new PictureAreaIcon(program,mNormalFont, WIDTH_RIGHT - 4, mSettings.isShowingPictureDescription(), true, false);
    } else {
      mPictureAreaIcon = new PictureAreaIcon();
    }
    
    // Calculate the maximum description lines
    int titleHeight = mTitleIcon.getIconHeight();
    int maxDescLines = 3;
    if (maxHeight != -1) {
      maxDescLines = (maxHeight - titleHeight - mPictureAreaIcon.getIconHeight() - V_GAP) / mNormalFont.getSize();
    }

    if (programChanged
        || (maxDescLines != mDescriptionIcon.getMaximumLineCount())) {
      int descHeight = 0;
      // (Re)set the description text
      if (!mSettings.isShowingOnlyDateAndTitle()) {
        mDescriptionIcon.setMaximumLineCount(maxDescLines);
        ProgramFieldType[] infoFieldArr = Settings.propProgramInfoFields
            .getProgramFieldTypeArray();
        Reader infoReader = new MultipleFieldReader(program, infoFieldArr);
        try {
          mDescriptionIcon.setText(infoReader);
        } catch (IOException exc) {
          mLog.log(Level.WARNING, "Reading program info failed for " + program,
              exc);
        }
        descHeight = mDescriptionIcon.getIconHeight();
      } else {
        descHeight = 0;
      }
            
      // Calculate the height
      mHeight = titleHeight + descHeight + mPictureAreaIcon.getIconHeight() + V_GAP;
      setPreferredSize(new Dimension(WIDTH, mHeight));

      // Calculate the preferred height
      mPreferredHeight = titleHeight + (maxDescLines * mNormalFont.getSize()) + mPictureAreaIcon.getIconHeight() + V_GAP;
            
      if (mHeight < mPreferredHeight) {
        mPreferredHeight = mHeight;
      }
    }

    if (isShowing()) {
      oldProgram.removeChangeListener(this);
      mProgram.addChangeListener(this);
      revalidate();
      repaint();
    }
  }

  /**
   * Gets the plugin icons for a program.
   * 
   * @param program
   *          The program to get the icons for.
   * @return The icons for the program.
   */
  private Icon[] getPluginIcons(Program program) {
    String[] iconPluginArr = Settings.propProgramTableIconPlugins
        .getStringArray();

    if (mSettings.isShowingOnlyDateAndTitle() || (iconPluginArr == null) || (iconPluginArr.length == 0)) {
      return new Icon[0];
    } else {
      PluginProxyManager mng = PluginProxyManager.getInstance();
      ArrayList<Icon> iconList = new ArrayList<Icon>();
      // Add the icons for each plugin
      for (int pluginIdx = 0; pluginIdx < iconPluginArr.length; pluginIdx++) {
        if (iconPluginArr[pluginIdx].compareTo(Settings.INFO_ID) == 0) {
          int info = program.getInfo();

          if ((info != -1) && (info != 0)) {
            for (int i = 0; i < ProgramInfoHelper.mInfoBitArr.length; i++) {
              if (ProgramInfoHelper.bitSet(info,
                  ProgramInfoHelper.mInfoBitArr[i])
                  && (ProgramInfoHelper.mInfoIconArr[i] != null)) {
                // Add the icon to the list
                iconList.add(ProgramInfoHelper.mInfoIconArr[i]);
              }
            }
          }
        } else if (iconPluginArr[pluginIdx].compareTo(Settings.PICTURE_ID) == 0) {
          if(mProgram.getBinaryField(ProgramFieldType.PICTURE_TYPE) != null) {
            iconList.add(new ImageIcon("imgs/Info_HasPicture.png"));
          }
        } else {
          PluginProxy plugin = mng.getPluginForId(iconPluginArr[pluginIdx]);

          // Check whether this entry still uses the old class name
          if (plugin == null) {
            String asId = "java." + iconPluginArr[pluginIdx];
            plugin = mng.getPluginForId(asId);

            if (plugin != null) {
              // It was the old class name, not an ID
              // -> Change the class name to an ID and save it
              iconPluginArr[pluginIdx] = asId;
              Settings.propProgramTableIconPlugins
                  .setStringArray(iconPluginArr);
            }
          }

          // Now add the icons
          if ((plugin != null) && plugin.isActivated()) {
            Icon[] iconArr = plugin.getProgramTableIcons(program);
            if (iconArr != null) {
              // Add the icons
              for (Icon icon : iconArr) {
                iconList.add(icon);
              }
            }
          }
        }
      }

      // Convert the list to an array and return it
      return iconList.toArray(new Icon[iconList.size()]);
    }
  }

  /**
   * Paints the component.
   * 
   * @param g
   *          The graphics context to paint to.
   */
  public void paintComponent(Graphics g) {
    int width = getWidth();
    int height = USE_FULL_HEIGHT ? getHeight() : mHeight;
    Graphics2D grp = (Graphics2D) g;
    
    // Draw the background if this program is on air
    if (mProgram.isOnAir()) {
      int minutesAfterMidnight = IOUtilities.getMinutesAfterMidnight();
      int progLength = mProgram.getLength();
      int startTime = mProgram.getStartTime();
      int elapsedMinutes;
      if (minutesAfterMidnight < startTime) {
        // The next day has begun -> we have to add 24 * 60 minutes
        // Example: Start time was 23:50 = 1430 minutes after midnight
        // now it is 0:03 = 3 minutes after midnight
        // elapsedMinutes = (24 * 60) + 3 - 1430 = 13 minutes
        elapsedMinutes = (24 * 60) + minutesAfterMidnight - startTime;
      } else {
        elapsedMinutes = minutesAfterMidnight - startTime;
      }

      if (mAxis == X_AXIS) {
        int progressX = 0;
        if (progLength > 0) {
          progressX = elapsedMinutes * width / progLength;
        }

        int pos = Settings.propProgramTableOnAirProgramsShowingBorder.getBoolean() ? 1 : 0;
        
        grp.setColor(Settings.propProgramTableColorOnAirDark.getColor());
        grp.fillRect(pos, pos, progressX - pos, height - pos);
        grp.setColor(Settings.propProgramTableColorOnAirLight.getColor());
        grp.fillRect(progressX, pos, width - progressX - pos * 2, height - pos);

        if(Settings.propProgramTableOnAirProgramsShowingBorder.getBoolean())
          grp.draw3DRect(0, 0, width - 1, height - 1, true);
      } else {
        int progressY = 0;
        if (progLength > 0) {
          progressY = elapsedMinutes * height / progLength;
        }

        int pos = Settings.propProgramTableOnAirProgramsShowingBorder.getBoolean() ? 1 : 0;
        
        grp.setColor(Settings.propProgramTableColorOnAirDark.getColor());
        grp.fillRect(pos, pos, width - pos * 2, progressY - pos);
        grp.setColor(Settings.propProgramTableColorOnAirLight.getColor());
        grp.fillRect(pos, progressY, width - pos * 2, height - progressY - pos);
        
        if(Settings.propProgramTableOnAirProgramsShowingBorder.getBoolean())
          grp.draw3DRect(0, 0, width - 1, height - 1, true);
      }
    }

    // If there are plugins that have marked the program -> paint the background
    Marker[] markedByPluginArr = mProgram.getMarkerArr();
    if (markedByPluginArr.length != 0) {
      switch(mProgram.getMarkPriority()) {
        case Program.MIN_MARK_PRIORITY: grp.setColor(Settings.propProgramTableMarkedMinPriorityColor.getColor());grp.fill3DRect(0, 0, width, height, true);break;
        case Program.MEDIUM_MARK_PRIORITY: grp.setColor(Settings.propProgramTableMarkedMediumPriorityColor.getColor());grp.fill3DRect(0, 0, width, height, true);break;
        case Program.MAX_MARK_PRIORITY: grp.setColor(Settings.propProgramTableMarkedMaxPriorityColor.getColor());grp.fill3DRect(0, 0, width, height, true);break;
        
        default: if(Settings.propProgramTableMarkedDefaultPriorityShowsColor.getBoolean()) {
                   grp.setColor(Settings.propProgramTableMarkedDefaultPriorityColor.getColor());
                   grp.fill3DRect(0, 0, width, height, true); 
                 }
      }
    }

    if (mMouseOver || mIsSelected) {
      Color test = Settings.propMouseOverColor.getColor();
      if (mIsSelected) {
        test = Settings.propKeyboardSelectedColor.getColor();
      }
      grp.setColor(test);
      grp.fillRect(0, 0, width - 1, height - 1);

      Stroke str = grp.getStroke();
      Color col = grp.getColor();
      float dash[] = { 10.0f };
      BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
      grp.setColor(Color.BLACK);
      grp.setStroke(dashed);
      grp.drawRect(0, 0, width - 1, height - 1);
      
      grp.setStroke(str);
      grp.setColor(col);
    }
    
    // Draw all the text
    if (PAINT_EXPIRED_PROGRAMS_PALE && mProgram.isExpired()
        && mTextColor.equals(Color.BLACK)) {
      setForeground(Color.gray);
      grp.setColor(Color.gray);
    } else {
      setForeground(mTextColor);
      grp.setColor(mTextColor);
    }
    grp.setFont(ProgramPanel.mTimeFont);
    grp.drawString(mProgramTimeAsString, 1, mTimeFont.getSize());

    mTitleIcon.paintIcon(this, grp, WIDTH_LEFT, 0);

    if (!mSettings.isShowingOnlyDateAndTitle()) {
      mPictureAreaIcon.paintIcon(this,grp, WIDTH_LEFT, mTitleIcon.getIconHeight());
      
      if (mHeight >= mPreferredHeight) {
        mDescriptionIcon.paintIcon(this, grp, WIDTH_LEFT, mTitleIcon
            .getIconHeight() + mPictureAreaIcon.getIconHeight());
      }

      // Paint the icons pale if the program is expired
      if (PAINT_EXPIRED_PROGRAMS_PALE && mProgram.isExpired()) {
        grp.setComposite(PALE_COMPOSITE);
      }


      // paint the icons of the plugins that have marked the program
      int x = width - 1;
      int y = mTitleIcon.getIconHeight() + mDescriptionIcon.getIconHeight()
          + mPictureAreaIcon.getIconHeight() + 17;
      y = Math.min(y, height - 1);
      for (Marker marker: markedByPluginArr) {
        Icon[] icons = marker.getMarkIcons(mProgram);
        if (icons != null) {
          for(int i = icons.length - 1; i >= 0 ; i--) {
            x -= icons[i].getIconWidth();
            icons[i].paintIcon(this, grp, x, y - icons[i].getIconHeight());
          }
        }
      }

      // Paint the icons on the left side
      if (mIconArr != null) {
        x = 2;
        y = mTimeFont.getSize() + 3;
        for (Icon icon : mIconArr) {
          int iconHeight = icon.getIconHeight();
          if ((y + iconHeight) < mHeight) {
            icon.paintIcon(this, grp, x, y);
            y += iconHeight + 2;
          }
        }
      }
    }
    // Reset the old composite
    if (PAINT_EXPIRED_PROGRAMS_PALE && mProgram.isExpired()) {
      grp.setComposite(NORMAL_COMPOSITE);
    }
  }

  /**
   * Called when the panel is added to a container.
   * <p>
   * registers the panel as ChangeListener at the program.
   */
  public void addNotify() {
    super.addNotify();
    mProgram.addChangeListener(this);
  }

  /**
   * Called when the panel is added to a container.
   * <p>
   * removes the panel as ChangeListener from the program.
   */
  public void removeNotify() {
    super.removeNotify();
    mProgram.removeChangeListener(this);
  }

  /**
   * Gets the program object of this ProgramPanel.
   * 
   * @return the program object of this ProgramPanel.
   */
  public Program getProgram() {
    return mProgram;
  }

  /**
   * Adds a MouseListener that shows the plugin context menu when the user does
   * a right click on the program panel.
   * 
   * @param caller
   *          The ContextMenuIf to exclude from the context menu. When
   *          <code>null</code> no ContextMenuIf is excluded.
   */
  public void addPluginContextMenuMouseListener(final ContextMenuIf caller) {
    addMouseListener(new MouseAdapter() {

      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showPopup(e, caller);
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showPopup(e, caller);
        }
      }

      public void mouseClicked(MouseEvent evt) {
        if (SwingUtilities.isLeftMouseButton(evt) && (evt.getClickCount() == 2)) {
          Plugin.getPluginManager().handleProgramDoubleClick(mProgram, caller);
        }
        if (SwingUtilities.isMiddleMouseButton(evt)
            && (evt.getClickCount() == 1)) {
          Plugin.getPluginManager().handleProgramMiddleClick(mProgram, caller);
        }
      }
    });
  }

  /**
   * Shows the Popup
   * 
   * @param evt
   *          Event for X/Y-Coordinates
   * @param caller
   *          ContextMenuIf that called this
   */
  private void showPopup(MouseEvent evt, ContextMenuIf caller) {
    if (SwingUtilities.isRightMouseButton(evt)) {
      JPopupMenu menu = PluginProxyManager.createPluginContextMenu(mProgram,
          caller);
      menu.show(evt.getComponent(), evt.getX() - 15, evt.getY() - 15);
    }
  }

  /**
   * Should be called, when the program has changed.
   * <p>
   * If you use this program panel directly (this is the case, when you have
   * added it into a JPanel), you don't have to call this method. This is done
   * automatically.
   * <p>
   * But if you use this panel just as renderer (e.g. for a list) you have to
   * register at the programs as ChangeListener and call this method when
   * {@link ChangeListener#stateChanged(javax.swing.event.ChangeEvent)} was
   * called.
   * 
   * @see Program#addChangeListener(ChangeListener)
   */
  public void programHasChanged() {
    // Get the icons from the plugins
    mIconArr = getPluginIcons(mProgram);
  }

  // implements ChangeListener

  /**
   * Called when the state of the program has changed.
   * <p>
   * repaints the panel.
   * 
   * @param evt
   *          The event describing the change.
   */
  public void stateChanged(ChangeEvent evt) {
    if (evt.getSource() == mProgram) {
      programHasChanged();
      repaint();
    }
  }

  /**
   * Sets the Color of the Text
   * 
   * @param col
   *          Color of the Text
   */
  public void setTextColor(Color col) {
    mTextColor = col;
  }

  /**
   * Returns the Color of the Text
   * 
   * @return Color of the Text
   */
  public Color getTextColor() {
    return mTextColor;
  }

  /**
   * Paints the ProgramPanel
   * 
   * @param mouse
   *          under a Mouse and needs highlight?
   * @param isSelected
   *          IsSelected program?
   * @param g
   *          Graphics-Object
   * 
   */
  public void paint(boolean mouse, boolean isSelected, Graphics g) {
    mMouseOver = mouse;
    mIsSelected = isSelected;
    super.paint(g);
  }

  /**
   * @return The smallest height possible.
   */
  public int getMinimumHeight() {
    return mTitleIcon.getIconHeight() + 3;
  }
}