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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import util.ui.TabLayout;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class ProxySettingsTab implements devplugin.SettingsTab {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ProxySettingsTab.class);
  
  private ProxySettingsPanel mHttpProxySettingsPanel;
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
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);
    
    JPanel main = new JPanel(new TabLayout(1));
    mSettingsPn.add(main, BorderLayout.CENTER);
    
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
    TVBrowser.updateProxySettings();
  }
  
  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("apps", "preferences-system-network-proxy", 16);
  }
  
  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("name", "Proxy");
  }
  
  // inner class ProxySettingsPanel
  
  
  protected static class ProxySettingsPanel extends JPanel {

    private JCheckBox mUseProxyChB, mAuthentifyAtProxyChB;
    private JTextField mHostTF, mPortTF, mUserTF;
    private JPasswordField mPasswordPF;
    private JLabel mHostLb, mPortLb, mUserLb, mPasswordLb;

    
    
    public ProxySettingsPanel(String msgProxy, String msgUseProxy) {
      super(new FormLayout("5dlu, 10dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, fill:pref:grow", "pref, 5dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));

      ActionListener updateEnabledListener = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          updateEnabled();
        }
      };

      String msg;
      
      CellConstraints cc = new CellConstraints();
      
      add(DefaultComponentFactory.getInstance().createSeparator(msgProxy), cc.xyw(1,1, 10));

      mUseProxyChB = new JCheckBox(msgUseProxy);
      mUseProxyChB.addActionListener(updateEnabledListener);
      add(mUseProxyChB, cc.xyw(2,3,6));

      mHostLb = new JLabel(mLocalizer.msg("host", "Host"));
      add(mHostLb, cc.xyw(3,5,1));
      mHostTF = new JTextField(20);
      add(mHostTF, cc.xy(5,5));

      mPortLb = new JLabel(mLocalizer.msg("port", "Port"));
      add(mPortLb, cc.xy(7,5));
      mPortTF = new JTextField(4);
      add(mPortTF, cc.xy(9,5));

      msg = mLocalizer.msg("authentifyAtProxy", "Authentify at proxy");
      mAuthentifyAtProxyChB = new JCheckBox(msg);
      mAuthentifyAtProxyChB.addActionListener(updateEnabledListener);
      add(mAuthentifyAtProxyChB, cc.xyw(3,7,5));

      JPanel panel = new JPanel(new FormLayout("10dlu, pref, 3dlu, fill:pref:grow", "pref, 3dlu, pref"));
      
      mUserLb = new JLabel(mLocalizer.msg("user", "User"));
      panel.add(mUserLb, cc.xy(2,1));
      mUserTF = new JTextField(10);
      panel.add(mUserTF, cc.xy(4,1));
      mPasswordLb = new JLabel(mLocalizer.msg("password", "Password"));
      panel.add(mPasswordLb, cc.xy(2,3));
      mPasswordPF = new JPasswordField(10);
      panel.add(mPasswordPF, cc.xy(4,3));
      
      add(panel, cc.xyw(3,9,7));
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
