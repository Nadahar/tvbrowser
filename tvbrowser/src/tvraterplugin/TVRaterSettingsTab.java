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
 */

package tvraterplugin;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import util.browserlauncher.Launch;
import util.io.IOUtilities;
import util.ui.ImageUtilities;
import util.ui.LinkButton;
import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * This class contains the Settings-Tab to configurate the plugin
 * 
 * @author bodo tasche
 */
public class TVRaterSettingsTab implements SettingsTab {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TVRaterSettingsTab.class);

  private TVRaterSettings _settings;

  private JTextField _name;

  private JPasswordField _password;

  // private JCheckBox _includeFav;
  private JCheckBox _ownRating;

  private JComboBox _updateTime;

  /**
   * @param settings
   */
  public TVRaterSettingsTab(TVRaterSettings settings) {
    _settings = settings;
  }

  public JPanel createSettingsPanel() {
    FormLayout layout = new FormLayout("5dlu,pref,5dlu,pref:grow,pref,3dlu,pref,5dlu",
        "5dlu,pref,3dlu,pref,10dlu,pref,5dlu,pref,1dlu,pref,2dlu,pref,default:grow,pref");
    layout.setColumnGroups(new int[][] {{5,7}});
    
    PanelBuilder pb = new PanelBuilder(layout);
    CellConstraints cc = new CellConstraints();
    
    _ownRating = new JCheckBox(mLocalizer.msg("ownRating", "Use own rating if available"), _settings.getPreferOwnRating());

    String[] updateStrings = { mLocalizer.msg("update", "only when updating TV listings"),
        mLocalizer.msg("everyTime", "every Time a rating is made"),
        mLocalizer.msg("eachStart", "at each start of TV-Browser"), mLocalizer.msg("manual", "manual Update"), };
    
    _updateTime = new JComboBox(updateStrings);
    switch (_settings.getUpdateInterval()) {
    case OnDataUpdate: {
      _updateTime.setSelectedIndex(0);
      break;
    }
    case OnRating: {
      _updateTime.setSelectedIndex(1);
      break;
    }
    case OnStart: {
      _updateTime.setSelectedIndex(2);
      break;
    }
    case Manually: {
      _updateTime.setSelectedIndex(3);
      break;
    }
    default: {
      _updateTime.setSelectedIndex(0);
    }
    }
    
    _name = new JTextField(_settings.getName());    
    _password = new JPasswordField(IOUtilities.xorEncode(_settings.getPassword(), 21));
    
    JButton newAccount = new JButton(mLocalizer.msg("newAccount", "Create new Account"));
    JButton lostPassword = new JButton(mLocalizer.msg("lostPassword", "Lost Password?"));
    
    pb.add(_ownRating, cc.xyw(2,2,6));
    pb.addLabel(mLocalizer.msg("transmit", "Transmit data") + ":", cc.xy(2,4));
    pb.add(_updateTime, cc.xyw(4,4,4));
    pb.addSeparator(mLocalizer.msg("accountsetting", "Account settings"), cc.xyw(1,6,8));
    pb.addLabel(mLocalizer.msg("name", "Name") + ":", cc.xy(2,8));
    pb.add(_name, cc.xyw(4,8,4));
    pb.addLabel(mLocalizer.msg("password", "Password") + ":", cc.xy(2,10));
    pb.add(_password, cc.xyw(4,10,4));
    pb.add(newAccount, cc.xy(5,12));
    pb.add(lostPassword, cc.xy(7,12));
    
    LinkButton urlLabel = new LinkButton("http://tvaddicted.de", "http://tvaddicted.de");
    urlLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

    JPanel urlPanel = new JPanel(new BorderLayout(0,0));
    urlPanel.add(urlLabel, BorderLayout.CENTER);
    
    pb.add(urlPanel, cc.xyw(2,14,6));

    newAccount.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Launch.openURL("http://tvaddicted.de/index.php?Page=newuser");
      }
    });

    lostPassword.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Launch.openURL("http://tvaddicted.de/index.php?Page=lostpasswd");
      }
    });

    return pb.getPanel();
  }

  public void saveSettings() {
    _settings.setName(_name.getText());
    _settings.setPassword(IOUtilities.xorEncode(new String(_password.getPassword()), 21));
    _settings.setPreferOwnRating(_ownRating.isSelected());
    switch (_updateTime.getSelectedIndex()) {
    case 0: {
      _settings.setUpdateInterval(UpdateInterval.OnDataUpdate);
      break;
    }
    case 1: {
      _settings.setUpdateInterval(UpdateInterval.OnRating);
      break;
    }
    case 2: {
      _settings.setUpdateInterval(UpdateInterval.OnStart);
      break;
    }
    case 3: {
      _settings.setUpdateInterval(UpdateInterval.Manually);
      break;
    }
    default: {
      _settings.setUpdateInterval(UpdateInterval.OnDataUpdate);
    }
    }
  }

  public Icon getIcon() {
    String iconName = "tvraterplugin/imgs/tvrater.png";
    return ImageUtilities.createImageIconFromJar(iconName, getClass());
  }

  public String getTitle() {
    return mLocalizer.msg("tabName", "TV Rater");
  }
}