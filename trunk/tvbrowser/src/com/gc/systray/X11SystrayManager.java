package com.gc.systray;

import java.awt.Component;
import java.awt.event.MouseListener;

/**
 * SystrayManager - SystemTrayIconManager
 * in use for the TVBrowser for the X(11) Server
 *
 * this class is in most cases just a wrapper to X11SystrayWindow which
 * SHOULD NOT be accessed by someone else than X11SystrayWindow
 * @author swalkner
 * @version 1.0
 */
public class X11SystrayManager
{
    private String mNativeObjectFile     = "DesktopIndicator";
    
    private String           mTrayIcon   = null;
    private String           mToolTip    = null;
    private X11SystrayWindow mSystemTray = null;
    
    private boolean mLoaded = false;
    
    /**
     * @param trayIcon path to the image that should be displayed in the tray
     * @param toolTip tool tip that should be display on mouseOver
     */
    public X11SystrayManager(String trayIcon, String toolTip)
    {
        this.mTrayIcon   = trayIcon;
        this.mToolTip    = toolTip;
        
        //first we just initialize the systray and do NOT display it
        mSystemTray = new X11SystrayWindow(null, this.mTrayIcon, this.mToolTip);
        mSystemTray.setVisible(false);
        
        //Load JNI library
        try
        {
            System.loadLibrary(mNativeObjectFile);
            mLoaded = true;
        }
        catch (UnsatisfiedLinkError e)
        {
            System.out.println("could NOT init the systray :(");
        }
    }
    
    /**
     * @return true if library was loaded
     */
    public boolean isLoaded() {
      return mLoaded;
    }
    
    /**
     * add a mouse listener to the systray window
     * @param mouseListener
     */
    public void addMouseListener(MouseListener mouseListener)
    {
        if ( mSystemTray != null )
        {
            mSystemTray.addMouseListener(mouseListener);
        }
    }

    /**
     * display the system tray
     * may be called by the using-class
     */    
    public void systrayShow()
    {
        //just setVisible to true!
        mSystemTray.setVisible(true);
        //and call the native method which 
        //sends the dock request to the xserver
        nativeShowTrayIcon(mSystemTray);
    }
    
    /**
    * hide the system tray
    * may be called by the using-class
    */
    public void systrayHide()
    {
        //the native method destroys the window
        nativeHideTrayIcon();
        //and we just dispose it...
        mSystemTray.dispose();
    }
    
    /**
     * use this method to display the
     * popup menu at the correct position!
     * @return reference to the system tray window
     */
    public Component getSystemTray()
    {
        return mSystemTray;
    }
    
    /**
     * like the "destructor" of the trayicon manager
     * disables the trayicon
     */
    public void finalize()
    {
        nativeDestroyTrayIcon();
    }
    
    //our nice native function declarations:
    //use this function just as a kind of "destructor"
    private synchronized native boolean nativeDestroyTrayIcon() throws UnsatisfiedLinkError;
    
    //use this method to HIDE the systray
    private synchronized native boolean nativeHideTrayIcon() throws UnsatisfiedLinkError;
    
    //use this method to SHOW the systray
    private synchronized native boolean nativeShowTrayIcon(Component systemTray) throws UnsatisfiedLinkError;
}
