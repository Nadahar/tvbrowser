/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourcceforge.net)
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

package tvbrowser.ui.settings;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import util.ui.*;

import tvbrowser.core.Settings;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class ProxySettingsTab implements devplugin.SettingsTab {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ProxySettingsTab.class);
  
  private ProxySettingsPanel mHttpProxySettingsPanel,
    mFtpProxySettingsPanel;
  
  private JPanel mSettingsPn;

  
  
  /**
   * Creates a new instance of ProxySettingsTab.
   */
  public ProxySettingsTab() {
  }

  
  
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    mSettingsPn = new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    JPanel main = new JPanel(new TabLayout(1));
    mSettingsPn.add(main, BorderLayout.NORTH);
    
    // HTTP proxy
    String msgProxy = mLocalizer.msg("httpProxy", "HTTP Proxy");
    String msgUseProxy = mLocalizer.msg("useHttpProxy", "Use proxy for HTTP");
    mHttpProxySettingsPanel = new ProxySettingsPanel(msgProxy, msgUseProxy);
    mHttpProxySettingsPanel.setUseProxy(Settings.propHttpProxyUseProxy.getBoolean());
    mHttpProxySettingsPanel.setHost(Settings.propHttpProxyHost.getString());
    mHttpProxySettingsPanel.setPort(Settings.propHttpProxyPort.getString());
    mHttpProxySettingsPanel.setAuthentifyAtProxy(Settings.propHttpProxyAuthentifyAtProxy.getBoolean());
    mHttpProxySettingsPanel.setUser(Settings.propHttpProxyUser.getString());
    mHttpProxySettingsPanel.setPassword(Settings.propHttpProxyPassword.getString());
    main.add(mHttpProxySettingsPanel);
    
    // FTP proxy
    msgProxy = mLocalizer.msg("ftpProxy", "FTP Proxy");
    msgUseProxy = mLocalizer.msg("useFtpProxy", "Use proxy for FTP");
    mFtpProxySettingsPanel = new ProxySettingsPanel(msgProxy, msgUseProxy);
    mFtpProxySettingsPanel.setUseProxy(Settings.propFtpProxyUseProxy.getBoolean());
    mFtpProxySettingsPanel.setHost(Settings.propFtpProxyHost.getString());
    mFtpProxySettingsPanel.setPort(Settings.propFtpProxyPort.getString());
    mFtpProxySettingsPanel.setAuthentifyAtProxy(Settings.propFtpProxyAuthentifyAtProxy.getBoolean());
    mFtpProxySettingsPanel.setUser(Settings.propFtpProxyUser.getString());
    mFtpProxySettingsPanel.setPassword(Settings.propFtpProxyPassword.getString());
    main.add(mFtpProxySettingsPanel);
    
    return mSettingsPn;
  }


  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Settings.propHttpProxyUseProxy.setBoolean(mHttpProxySettingsPanel.getUseProxy());
    Settings.propHttpProxyHost.setString(mHttpProxySettingsPanel.getHost());
    Settings.propHttpProxyPort.setString(mHttpProxySettingsPanel.getPort());
    Settings.propHttpProxyAuthentifyAtProxy.setBoolean(mHttpProxySettingsPanel.getAuthentifyAtProxy());
    Settings.propHttpProxyUser.setString(mHttpProxySettingsPanel.getUser());
    Settings.propHttpProxyPassword.setString(mHttpProxySettingsPanel.getPassword());
    
    Settings.propFtpProxyUseProxy.setBoolean(mFtpProxySettingsPanel.getUseProxy());
    Settings.propFtpProxyHost.setString(mFtpProxySettingsPanel.getHost());
    Settings.propFtpProxyPort.setString(mFtpProxySettingsPanel.getPort());
    Settings.propFtpProxyAuthentifyAtProxy.setBoolean(mFtpProxySettingsPanel.getAuthentifyAtProxy());
    Settings.propFtpProxyUser.setString(mFtpProxySettingsPanel.getUser());
    Settings.propFtpProxyPassword.setString(mFtpProxySettingsPanel.getPassword());
  }

  
  
  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return new ImageIcon("imgs/Server16.gif");
  }
  
  
  
  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("name", "Proxy");
  }
  
  // inner class ProxySettingsPanel
  
  
  protected class ProxySettingsPanel extends JPanel {

    private JCheckBox mUseProxyChB, mAuthentifyAtProxyChB;
    private JTextField mHostTF, mPortTF, mUserTF;
    private JPasswordField mPasswordPF;
    private JLabel mHostLb, mPortLb, mUserLb, mPasswordLb;

    
    
    public ProxySettingsPanel(String msgProxy, String msgUseProxy) {
      super(new TabLayout(1));

      ActionListener updateEnabledListener = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          updateEnabled();
        }
      };

      String msg;
      JPanel p1;
      
      setBorder(BorderFactory.createTitledBorder(msgProxy));

      mUseProxyChB = new JCheckBox(msgUseProxy);
      mUseProxyChB.addActionListener(updateEnabledListener);
      add(mUseProxyChB);

      p1 = new JPanel(new TabLayout(4));
      add(p1);

      mHostLb = new JLabel(mLocalizer.msg("host", "Host"));
      p1.add(mHostLb);
      mHostTF = new JTextField(20);
      p1.add(mHostTF);

      mPortLb = new JLabel(mLocalizer.msg("port", "Port"));
      p1.add(mPortLb);
      mPortTF = new JTextField(4);
      p1.add(mPortTF);

      msg = mLocalizer.msg("authentifyAtProxy", "Authentify at proxy");
      mAuthentifyAtProxyChB = new JCheckBox(msg);
      mAuthentifyAtProxyChB.addActionListener(updateEnabledListener);
      add(mAuthentifyAtProxyChB);

      p1 = new JPanel(new TabLayout(4));
      add(p1);

      mUserLb = new JLabel(mLocalizer.msg("user", "User"));
      p1.add(mUserLb);
      mUserTF = new JTextField(10);
      p1.add(mUserTF);

      mPasswordLb = new JLabel(mLocalizer.msg("mPassword", "Password"));
      p1.add(mPasswordLb);
      mPasswordPF = new JPasswordField(10);
      p1.add(mPasswordPF);
    }
    


    private void updateEnabled() {
      boolean useProxy = mUseProxyChB.isSelected();
      boolean authentify = mAuthentifyAtProxyChB.isSelected();

      mHostLb.setEnabled(useProxy);
      mHostTF.setEnabled(useProxy);
      mPortLb.setEnabled(useProxy);
      mPortTF.setEnabled(useProxy);
      mAuthentifyAtProxyChB.setEnabled(useProxy);
      mUserLb.setEnabled(useProxy && authentify);
      mUserTF.setEnabled(useProxy && authentify);
      mPasswordLb.setEnabled(useProxy && authentify);
      mPasswordPF.setEnabled(useProxy && authentify);
    }


    public void setUseProxy(boolean value) {
      mUseProxyChB.setSelected(value);
      updateEnabled();
    }


    public boolean getUseProxy() {
      return mUseProxyChB.isSelected();
    }


    public void setHost(String value) {
      mHostTF.setText(value);
    }


    public String getHost() {
      return mHostTF.getText();
    }


    public void setPort(String value) {
      mPortTF.setText(value);
    }


    public String getPort() {
      return mPortTF.getText();
    }


    public void setAuthentifyAtProxy(boolean value) {
      mAuthentifyAtProxyChB.setSelected(value);
      updateEnabled();
    }


    public boolean getAuthentifyAtProxy() {
      return mAuthentifyAtProxyChB.isSelected();
    }


    public void setUser(String value) {
      mUserTF.setText(value);
    }


    public String getUser() {
      return mUserTF.getText();
    }


    public void setPassword(String value) {
      mPasswordPF.setText(value);
    }


    public String getPassword() {
      return new String(mPasswordPF.getPassword());
    }
    
  }
  
}
