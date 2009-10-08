/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package tvbrowser.ui.configassistant;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.Proxy.Type;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import util.io.NetworkUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

class NetworkCardPanel extends AbstractCardPanel {
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(NetworkCardPanel.class);

  private JPanel mContent;
  private JLabel mHostLB;
  private JTextField mHostTF;
  private JLabel mPortLB;
  private JTextField mPortTF;
  private JCheckBox mProxyCB;
  private JLabel mProxyHeadline;
  private JPasswordField mPasswordTF;
  private JTextField mUserTF;
  private JLabel mPasswordLB;
  private JLabel mUserLB;
  private JCheckBox mAuthCB;

  public NetworkCardPanel(PrevNextButtons btns) {
    super(btns);
  }

  public JPanel getPanel() {
    mContent = new JPanel(new BorderLayout());

    mContent.add(new StatusPanel(StatusPanel.NETWORK), BorderLayout.NORTH);

    JPanel content = new JPanel(new FormLayout("fill:pref:grow, fill:300dlu:grow, fill:pref:grow",
        "fill:pref:grow, pref, 3dlu, pref, 3dlu, pref,3dlu, pref,3dlu, pref, fill:pref:grow"));
    content.setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();

    content.add(UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("preambel", "Preambel")), cc.xy(2, 2));

    mProxyCB = new JCheckBox(mLocalizer.msg("usingProxy", "Using Proxy"));
    mProxyCB.setSelected(Settings.propHttpProxyUseProxy.getBoolean());

    content.add(mProxyCB, cc.xy(2, 6));

    // Proxy - Subpanel
    JPanel proxyPanel = new JPanel(new FormLayout("fill:pref:grow", "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));
    proxyPanel.setBorder(BorderFactory.createEmptyBorder(0, Sizes.dialogUnitXAsPixel(10, proxyPanel), 0, 0));

    mProxyHeadline = new JLabel(mLocalizer.msg("myProxy", "My Proxy"));
    proxyPanel.add(mProxyHeadline, cc.xy(1, 1));

    JPanel hostPanel = new JPanel(new FormLayout("pref, 3dlu, pref, 3dlu, pref, 3dlu, pref", "pref"));

    mHostLB = new JLabel(mLocalizer.msg("host", "Host"));
    hostPanel.add(mHostLB, cc.xy(1, 1));
    mHostTF = new JTextField(20);
    mHostTF.setText(Settings.propHttpProxyHost.getString());
    hostPanel.add(mHostTF, cc.xy(3, 1));
    mPortLB = new JLabel(mLocalizer.msg("port", "Port"));
    hostPanel.add(mPortLB, cc.xy(5, 1));
    mPortTF = new JTextField(4);
    hostPanel.add(mPortTF, cc.xy(7, 1));
    mPortTF.setText(Settings.propHttpProxyPort.getString());

    proxyPanel.add(hostPanel, cc.xy(1, 3));

    mAuthCB = new JCheckBox(mLocalizer.msg("auth", "authentification"));
    mAuthCB.setSelected(Settings.propHttpProxyAuthentifyAtProxy.getBoolean());
    proxyPanel.add(mAuthCB, cc.xy(1, 5));

    JPanel userPanel = new JPanel(new FormLayout("10dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref", "pref"));

    mUserLB = new JLabel(mLocalizer.msg("user", "User"));
    userPanel.add(mUserLB, cc.xy(2, 1));
    mUserTF = new JTextField(10);
    mUserTF.setText(Settings.propHttpProxyUser.getString());
    userPanel.add(mUserTF, cc.xy(4, 1));
    mPasswordLB = new JLabel(mLocalizer.msg("password", "Password"));
    userPanel.add(mPasswordLB, cc.xy(6, 1));
    mPasswordTF = new JPasswordField(10);
    mPasswordTF.setText(Settings.propHttpProxyPassword.getString());
    userPanel.add(mPasswordTF, cc.xy(8, 1));

    proxyPanel.add(userPanel, cc.xy(1, 7));

    content.add(proxyPanel, cc.xy(2, 8));
    content.add(UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("closing", "Closing")), cc.xy(2, 10));
    mContent.add(content, BorderLayout.CENTER);

    updateFieldState();

    mProxyCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateFieldState();
      }
    });

    mAuthCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateFieldState();
      }
    });

    findProxySettings();

    return mContent;
  }

  private void findProxySettings() {
    try {
      System.getProperty("java.net.useSystemProxies", "true");
      List<Proxy> l = ProxySelector.getDefault().select(new URI("http://www.yahoo.com/"));

      for (Iterator<Proxy> iter = l.iterator(); iter.hasNext();) {
        Proxy proxy = iter.next();
        if (proxy.type() == Type.DIRECT) {
          return;
        }
        else {
          System.out.println("proxy hostname : " + proxy.type());
          InetSocketAddress addr = (InetSocketAddress) proxy.address();

          if (addr != null) {
            System.out.println("proxy hostname : " + addr.getHostName());
            System.out.println("proxy port : " + addr.getPort());
            mHostTF.setText(addr.getHostName());
            mPortTF.setText(String.valueOf(addr.getPort()));
            mProxyCB.setSelected(true);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void updateFieldState() {
    boolean state = mProxyCB.isSelected();
    mProxyHeadline.setEnabled(state);
    mHostTF.setEnabled(state);
    mHostLB.setEnabled(state);
    mPortLB.setEnabled(state);
    mPortTF.setEnabled(state);

    mAuthCB.setEnabled(state);

    boolean auth = mAuthCB.isSelected();
    mUserLB.setEnabled(state && auth);
    mUserTF.setEnabled(state && auth);
    mPasswordLB.setEnabled(state && auth);
    mPasswordTF.setEnabled(state && auth);
  }

  public boolean onNext() {
    Settings.propHttpProxyUseProxy.setBoolean(mProxyCB.isSelected());
    Settings.propHttpProxyHost.setString(mHostTF.getText());
    Settings.propHttpProxyPort.setString(mPortTF.getText());
    Settings.propHttpProxyAuthentifyAtProxy.setBoolean(mAuthCB.isSelected());
    Settings.propHttpProxyUser.setString(mUserTF.getText());
    Settings.propHttpProxyPassword.setString(new String(mPasswordTF.getPassword()));

    TVBrowser.updateProxySettings();

    // Check Network!
    if (!NetworkUtilities.checkConnection()) {
      JOptionPane.showMessageDialog(mContent, UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("connectionError",
          "Connection Error")), Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  }

}
