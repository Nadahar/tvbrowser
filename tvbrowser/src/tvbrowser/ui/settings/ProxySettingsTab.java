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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import util.ui.*;

import tvbrowser.core.Settings;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class ProxySettingsTab extends devplugin.SettingsTab {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ProxySettingsTab.class);
  
  private ProxySettingsPanel mHttpProxySettingsPanel,
    mFtpProxySettingsPanel;
  
  
  
  /**
   * Creates a new instance of ProxySettingsTab.
   */
  public ProxySettingsTab() {
    setLayout(new FlowLayout(FlowLayout.LEADING));
    
    JPanel main = new JPanel(new TabLayout(1));
    add(main);
    
    // HTTP proxy
    String msgProxy = mLocalizer.msg("httpProxy", "HTTP Proxy");
    String msgUseProxy = mLocalizer.msg("useHttpProxy", "Use proxy for HTTP");
    mHttpProxySettingsPanel = new ProxySettingsPanel(msgProxy, msgUseProxy);
    mHttpProxySettingsPanel.setSettings(Settings.getHttpProxySettings());
    main.add(mHttpProxySettingsPanel);
    
    // FTP proxy
    msgProxy = mLocalizer.msg("ftpProxy", "FTP Proxy");
    msgUseProxy = mLocalizer.msg("useFtpProxy", "Use proxy for FTP");
    mFtpProxySettingsPanel = new ProxySettingsPanel(msgProxy, msgUseProxy);
    mFtpProxySettingsPanel.setSettings(Settings.getFtpProxySettings());
    main.add(mFtpProxySettingsPanel);
  }

  
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void ok() {
    Settings.ProxySettings httpSettings = mHttpProxySettingsPanel.getSettings();
    Settings.ProxySettings ftpSettings = mFtpProxySettingsPanel.getSettings();
    
    Settings.setProxySettings(httpSettings, ftpSettings);
  }
  
  

  /**
   * Returns the name of the tab-sheet.
   */
  public String getName() {
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

      mPasswordLb = new JLabel(mLocalizer.msg("password", "Password"));
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
    
    
    
    public void setSettings(Settings.ProxySettings proxySettings) {
      mUseProxyChB.setSelected(proxySettings.mUseProxy);
      mHostTF.setText(proxySettings.mHost);
      mPortTF.setText(proxySettings.mPort);
      mAuthentifyAtProxyChB.setSelected(proxySettings.mAuthentifyAtProxy);
      mUserTF.setText(proxySettings.mUser);
      mPasswordPF.setText(proxySettings.mPassword);
      
      updateEnabled();
    }
    
    
    
    public Settings.ProxySettings getSettings() {
      Settings.ProxySettings proxySettings = new Settings.ProxySettings();
      
      proxySettings.mUseProxy = mUseProxyChB.isSelected();
      proxySettings.mHost = mHostTF.getText();
      proxySettings.mPort = mPortTF.getText();
      proxySettings.mAuthentifyAtProxy = mAuthentifyAtProxyChB.isSelected();
      proxySettings.mUser = mUserTF.getText();
      proxySettings.mPassword = new String(mPasswordPF.getPassword());
      
      return proxySettings;
    }
    
  }
  
}
