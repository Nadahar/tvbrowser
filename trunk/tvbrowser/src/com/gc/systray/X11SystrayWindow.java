package com.gc.systray;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;

/**
 * @author stefan
 * this is THE icon which is displayed in the systray
 * to hide we have to destroy the window
 * otherwise it will be shown in the systray
 */
public class X11SystrayWindow extends JWindow
{
    private String mTrayIcon      = null;
    private String mToolTip       = null;
    
    private JLabel mLblSystemTray = null;
    
    public X11SystrayWindow(JFrame owner, String trayIcon, String toolTip)
    {
        super(owner);
        this.mTrayIcon   = trayIcon;
        this.mToolTip    = toolTip;
        
        initUi();
    }
    
    public void setVisible(boolean b) {
      setLocation(-100, -100);
      if (b) {
        requestFocus();
      }
      super.setVisible(b);
    }
    
    /**
     * inits the ui of the TrayIconWindow
     */
    private void initUi()
    {
        setFocusableWindowState(true);
        setFocusable(false);
        
        setSize(new Dimension(24, 24));
        getContentPane().setLayout(new BorderLayout());
        
        File trayIconFile = new File(mTrayIcon);
        if ( trayIconFile.exists() )
        {
            mLblSystemTray = new JLabel(new ImageIcon(mTrayIcon, mToolTip));
        }
        else
        {
            mLblSystemTray = new JLabel("TVBrowser");
        }
        getContentPane().add(mLblSystemTray, BorderLayout.CENTER);
    
        setVisible(false);
    }
}
