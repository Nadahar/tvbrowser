package clockplugin;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Constructor;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import devplugin.ActionMenu;
import devplugin.ButtonAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * Clock Plugin for TV-Browser. License: GPL
 *
 * @author René Mach
 */
public class ClockPlugin extends Plugin {

  private static ClockPlugin mInstance;
  private Properties mProperties;
  private Clock mClock;
  private int mShowTime;
  private boolean mMoveOnScreen, mShowForever, mUsePersonaColors, mTransparentBackground;
  private TitleBarClock mTitleBarClock;
  private Point mLocation;
  private Dimension mParentSize;

  private static final Version mVersion = new Version(1, 80, 0, true);

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ClockPlugin.class);

  public static Version getVersion() {
    return mVersion;
  }

  /** Plugin info */
  public PluginInfo getInfo() {
    Class<?> pluginInfo = PluginInfo.class;
    
    try {
      Constructor<?> constructor = pluginInfo.getConstructor(new Class[] {Class.class,String.class,String.class,String.class,String.class});

      return (PluginInfo)constructor.newInstance(new Object[] {ClockPlugin.class,"ClockPlugin",mLocalizer.msg("desc",
      "Clock for TV-Browser"),"Ren\u00e9 Mach","GPL"});
    } catch (Exception e) {
      try {
        Constructor<?> constructor = pluginInfo.getConstructor(new Class[] {String.class,String.class,String.class,Version.class,String.class});

        return (PluginInfo)constructor.newInstance(new Object[] {"ClockPlugin",mLocalizer.msg("desc",
        "Clock for TV-Browser"),"René Mach",mVersion,"GPL"});
      }catch(Exception ee) {};
    }
    return new PluginInfo();
  }

  /**
   * Constructor.
   */
  public ClockPlugin() {
    mInstance = this;
    mMoveOnScreen = false;
    mShowForever = false;
    mUsePersonaColors = false;
    mTransparentBackground = false;
  }

  /**
   *
   * @return The instance of this Plugin.
   */
  public static ClockPlugin getInstance() {
    return mInstance;
  }

  /**
   *
   * @return The time to show the time dialog in seconds.
   */
  public int getTimeValue() {
    String time = mProperties.getProperty("time");
    if (time != null && time.length() != 0) {
      return Integer.parseInt(time);
    } else {
      return 5;
    }
  }

  /**
   *
   * @param time
   *          The value the user selected to show the time dialog in seconds.
   */
  public void storeTimeValue(int time) {
    mShowTime = time;
    mProperties.setProperty("time", String.valueOf(time));
  }

  public void loadSettings(Properties p) {
    if (p == null) {
      mProperties = new Properties();
      mShowTime = 5;
    } else {
      mProperties = p;
      String time = p.getProperty("time");

      if (time != null && time.length() > 0) {
        mShowTime = Integer.parseInt(time);
      } else {
        mShowTime = 5;
      }
    }

    if (mProperties.getProperty("titleBarClock","false").equals("true")) {
      mTitleBarClock = new TitleBarClock();
    }
    
    mMoveOnScreen = mProperties.getProperty("moveOnScreen","false").equals("true");
    mShowForever = mProperties.getProperty("showForever","false").equals("true");
    mUsePersonaColors = mProperties.getProperty("usePersonaColors","false").equals("true");
    mTransparentBackground = mProperties.getProperty("transparentBackground","false").equals("true");
    
    if (mProperties.getProperty("showBorder") == null) {
      mProperties.setProperty("showBorder", "true");
    }
  }

  public void onDeactivation() {
    if (mClock != null && mClock.getThread().isAlive()) {
      mClock.stopp();
    }
  }

  public Properties storeSettings() {
    return mProperties;
  }

  public SettingsTab getSettingsTab() {
    return (new ClockSettingsTab());
  }

  public ActionMenu getButtonAction() {
    ButtonAction action = new ButtonAction();
    action.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        toggleOnOffClock();
      }
    });

    action
        .putValue(Action.NAME, mLocalizer.msg("name", "Clock for TV-Browser"));
    action
        .putValue(Action.SMALL_ICON, getPluginManager().getIconFromTheme(this,"apps","clock",16));
    // big icon
    action.putValue(BIG_ICON, getPluginManager().getIconFromTheme(this,"apps","clock",22));

    return new ActionMenu(action);
  }

  /**
   *
   * @return The font size of the clock.
   */
  public int getFontValue() {
    String fontsize = mProperties.getProperty("fontsize");
    if (fontsize == null || fontsize.length() == 0) {
      mProperties.setProperty("fontsize", "28");
      return 28;
    } else {
      return Integer.parseInt(fontsize);
    }
  }

  /**
   *
   * @param i
   *          The font size of the clock.
   */
  public void setFontValue(int i) {
    mProperties.setProperty("fontsize", i + "");
  }

  /**
   *
   * @param value
   *          Show the clock forever?
   */
  public void setShowForever(boolean value) {
    mProperties.setProperty("showForever", value + "");

    if (mClock != null && mClock.getThread().isAlive()) {
      mClock.setShowForever(value);
    } else if (value) {
      toggleOnOffClock();
    }
  }

  /**
   *
   * @return Show the clock forever or not.
   */
  public boolean getShowForever() {
    String value = mProperties.getProperty("showForever");
    if (value != null && value.length() > 0) {
      if (value.equals("true")) {
        return true;
      }
    }

    return false;
  }

  /**
   *
   * @param value
   *          Clock moveOnScreen with TV-Browser?
   */
  public void setMoveOnScreen(boolean value) {
    mProperties.setProperty("moveOnScreen", value + "");
    mMoveOnScreen = value;
    mLocation = getParentFrame().getLocation();
  }

  /**
   *
   * @return Clock moveOnScreen with TV-Browser.
   */
  public boolean getMoveOnScreen() {
    String value = mProperties.getProperty("moveOnScreen");
    if (value != null && value.length() > 0) {
      if (value.equals("true")) {
        return true;
      }
    }

    return false;
  }

  /**
   *
   * @param value
   *          Clock has border?
   */
  public void setShowBorder(boolean value) {
    mProperties.setProperty("showBorder", value + "");
    if (mClock != null && mClock.getThread().isAlive()) {
      mClock.setBorder(value);
    }
  }
  
  /**
   * Sets the new value for transparent background.
   * <p>
   * @param value <code>true</code> if the background should be transparent.
   */
  public void setTransparentBackground(boolean value) {
    mProperties.setProperty("transparentBackground", String.valueOf(value));
    mTransparentBackground = value;
    mClock.setTransparentBackground(value);
  }
  
  /**
   * @return <code>true</code> if the background of the clock should be transparent.
   */
  public boolean isUsingTransparentBackground() {
    return mTransparentBackground;
  }

  /**
   * Sets the new value for is using of persona colors.
   * <p>
   * @param value <code>true</code> if persona colors should be used.
   */
  public void setIsUsingPersonaColors(boolean value) {
    mProperties.setProperty("usePersonaColors", String.valueOf(value));
    mUsePersonaColors = value;
  }
  
  /**
   * @return If the clock should use the persona colors.
   */
  public boolean isUsingPersonaColors() {
    return mUsePersonaColors;
  }
  
  /**
   *
   * @return Clock has Border.
   */
  public boolean getShowBorder() {
    String value = mProperties.getProperty("showBorder");
    if (value != null && value.length() > 0) {
      if (value.equals("true")) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   *
   * @param value
   *          Clock in the title bar?
   */
  public void setTitleBarClock(boolean value) {
    mProperties.setProperty("titleBarClock", value + "");
    if (value) {
      if (mTitleBarClock == null || !mTitleBarClock.isAlive()) {
        mTitleBarClock = new TitleBarClock();
      }
    } else if (mTitleBarClock != null && mTitleBarClock.isAlive()) {
      mTitleBarClock.stopp();
    }
  }

  /**
   *
   * @return Clock in the title bar.
   */
  public boolean getTitleBarClock() {
    String value = mProperties.getProperty("titleBarClock");
    if (value != null && value.length() > 0) {
      if (value.equals("true")) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  // show the clock
  private synchronized void toggleOnOffClock() {
    if (getParentFrame().isUndecorated() && (mClock == null || !mClock.getThread().isAlive())) {
      return;
    }

    if ((mClock == null || !mClock.isVisible()) && getParentFrame().isVisible()) {
      mClock = new Clock(mShowTime, mProperties);
      mClock.setTransparentBackground(mTransparentBackground);
      Thread t = new Thread() {
        public void run() {
          try {
            while(mClock.getThread().isAlive()) {
              Thread.sleep(500);
            }
          }catch(Exception e) {e.printStackTrace();}

          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              mClock.setVisible(false);
              mClock.dispose();
            }
          });
        }
      };
      t.setPriority(Thread.MIN_PRIORITY);
      t.start();
    }
    else if(mClock != null && mClock.isVisible()) {
      mClock.stopp();
    }
  }

  public void handleTvBrowserStartFinished() {
    if (mShowForever && mClock == null) {
      toggleOnOffClock();
    }

    mLocation = getParentFrame().getLocation();
    mParentSize = getParentFrame().getSize();

    getParentFrame().addComponentListener(new ComponentAdapter() {
      boolean mWasVisible = false;

      public void componentHidden(ComponentEvent e) {
        if(mClock != null && mClock.getThread().isAlive()) {
          mClock.setShowForever(true);
        }
      }

      public void componentResized(ComponentEvent e) {
        if (mMoveOnScreen && !getParentFrame().isUndecorated()) {
          Dimension d = getParentFrame().getSize();

          int diff = d.width - mParentSize.width;

          if(diff != 0) {
            if (mClock != null && mClock.getThread().isAlive()) {
              mClock.setLocation(mClock.getLocation().x + diff, mClock
                  .getLocation().y);
              mProperties.setProperty("xPos", mClock.getX() + "");
            }
            else {
              int xalt = Integer.parseInt(mProperties.getProperty("xPos"));
              mProperties.setProperty("xPos", (xalt + diff) + "");
            }
            mParentSize = d;
          }
        }
        if(getParentFrame().isUndecorated() && mClock != null && mClock.getThread().isAlive()) {
          mClock.stopp();
          mClock.setVisible(false);
          mClock.dispose();
          mWasVisible = true;
        }
        else if(!getParentFrame().isUndecorated() && mWasVisible) {
          toggleOnOffClock();
          mWasVisible = false;
        }
      }

      public void componentMoved(ComponentEvent e) {
        if (mMoveOnScreen && !getParentFrame().isUndecorated()) {
          Point p = getParentFrame().getLocation();

          if (!p.equals(mLocation) && mLocation != null) {
            int x = mLocation.x - p.x;
            int y = mLocation.y - p.y;
            mLocation = p;

            if (mClock != null && mClock.getThread().isAlive()) {
              mClock.setLocation(mClock.getLocation().x - x, mClock
                  .getLocation().y
                  - y);
              mProperties.setProperty("xPos", mClock.getX() + "");
              mProperties.setProperty("yPos", mClock.getY() + "");
            } else {
              try {
                int xalt = Integer.parseInt(mProperties.getProperty("xPos"));
                int yalt = Integer.parseInt(mProperties.getProperty("yPos"));
                mProperties.setProperty("xPos", (xalt - x) + "");
                mProperties.setProperty("yPos", (yalt - y) + "");
              } catch (Exception ee) {
              }
            }
          }
        }
      }

      public void componentShown(ComponentEvent e) {
        if (!getShowForever()) {
          if(mClock != null && mClock.getThread().isAlive()) {
            mClock.setShowForever(false);
          }
        }
        else if(mClock == null || !mClock.getThread().isAlive()) {
          toggleOnOffClock();
        }
      }
    });
  }

  /**
   *
   * @return The parent frame of this Plugin.
   */
  public Frame getSuperFrame() {
    return getParentFrame();
  }
  
  public String getPluginCategory() {
    //Plugin.OTHER_CATEGORY
    return "misc";
  }
}
