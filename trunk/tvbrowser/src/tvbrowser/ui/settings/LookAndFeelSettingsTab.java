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
package tvbrowser.ui.settings;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.icontheme.IconTheme;
import tvbrowser.ui.settings.looksSettings.JGoodiesLNFSettings;
import tvbrowser.ui.settings.looksSettings.SkinLNFSettings;
import util.ui.LinkButton;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsTab;

public class LookAndFeelSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(LookAndFeelSettingsTab.class);

  private JComboBox mLfComboBox;

  private JPanel mSettingsPn;

  private JButton mConfigBtn;

  private JComboBox mIconThemes;
  
  private JComboBox mPluginViewPosition;

  private JTextArea mInfoArea; 
  
  private static int mStartLookAndIndex;
  private static int mStartIconIndex;
  private static int mStartPluginViewPositionIndex;

  private static String mJGoodiesStartTheme;
  private static boolean mJGoodiesStartShadow;
  
  private static String mSkinLFStartTheme;
  
  private static boolean mSomethingChanged = false;

  private static class LookAndFeelObj {
    private UIManager.LookAndFeelInfo info;

    public LookAndFeelObj(UIManager.LookAndFeelInfo info) {
      this.info = info;
    }

    public String toString() {
      return info.getName();
    }

    public String getLFClassName() {
      return info.getClassName();
    }
  }

  private LookAndFeelObj[] getLookAndFeelObjs() {
    UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
    LookAndFeelObj[] result = new LookAndFeelObj[info.length];
    for (int i = 0; i < info.length; i++) {
      result[i] = new LookAndFeelObj(info[i]);
    }

    return result;
  }

  public JPanel createSettingsPanel() {
    FormLayout layout = new FormLayout("5dlu, pref, 3dlu, fill:default:grow, 3dlu, pref, 5dlu", "");

    CellConstraints cc = new CellConstraints();
    mSettingsPn = new JPanel(layout);
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);

    layout.appendRow(new RowSpec("pref"));
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("lookAndFeel", "Look and Feel")), cc.xyw(1, 1, 7));

    layout.appendRow(new RowSpec("5dlu"));
    layout.appendRow(new RowSpec("pref"));

    mSettingsPn.add(new JLabel(mLocalizer.msg("channelPosition", "Channel list position") +":"), cc.xy(2, 3));
    
    mPluginViewPosition = new JComboBox(new String[] {Localizer.getLocalization(Localizer.I18N_LEFT),Localizer.getLocalization(Localizer.I18N_RIGHT)});    
    
    if(Settings.propPluginViewIsLeft.getBoolean()) {
      mPluginViewPosition.setSelectedIndex(1);
    }
    else {
      mPluginViewPosition.setSelectedIndex(0);
    }
    
    mPluginViewPosition.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        checkIfAreaIsToMakeVisible();
      };
    });
    
    mSettingsPn.add(mPluginViewPosition, cc.xy(4,3));
    
    layout.appendRow(new RowSpec("5dlu"));
    layout.appendRow(new RowSpec("pref"));

    mSettingsPn.add(new JLabel(mLocalizer.msg("theme", "Theme") +":"), cc.xy(2, 5));

    LookAndFeelObj[] obj = getLookAndFeelObjs();
    mLfComboBox = new JComboBox(obj);

    String lf = Settings.propLookAndFeel.getString();
    for (int i = 0; i < obj.length; i++) {
      if (obj[i].getLFClassName().equals(lf)) {
        mLfComboBox.setSelectedItem(obj[i]);
      }
    }
    
    mLfComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        lookChanged();
      };
    });
    
    mSettingsPn.add(mLfComboBox, cc.xy(4, 5));
    
    mConfigBtn = new JButton(mLocalizer.msg("config", "Config"));
    mConfigBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        configTheme();
      }
    });
    
    mSettingsPn.add(mConfigBtn, cc.xy(6, 5));

    layout.appendRow(new RowSpec("3dlu"));
    layout.appendRow(new RowSpec("pref"));

    mSettingsPn.add(new JLabel(mLocalizer.msg("icons", "Icons") + ":"), cc.xy(2, 7));
    
    mIconThemes = new JComboBox(IconLoader.getInstance().getAvailableThemes());
    mIconThemes.setRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value != null) {
          label.setText(((IconTheme)value).getName());
          label.setToolTipText(((IconTheme)value).getComment());
        }
        return label;
      }
    });
    
    if (Settings.propIcontheme.getString() != null) {
      IconTheme theme = IconLoader.getInstance().getIconTheme(new File(Settings.propIcontheme.getString().replace('/',File.separatorChar)));
      if (theme.loadTheme())
        mIconThemes.setSelectedItem(theme);
      else
        mIconThemes.setSelectedItem(IconLoader.getInstance().getDefaultTheme());
    } else {
      mIconThemes.setSelectedItem(IconLoader.getInstance().getDefaultTheme());
    }
    
    mSettingsPn.add(mIconThemes, cc.xy(4, 7));

    layout.appendRow(new RowSpec("3dlu"));
    layout.appendRow(new RowSpec("pref"));

    mSettingsPn.add(new LinkButton(mLocalizer.msg("findMoreIcons","You can find more Icons on our Web-Page."),
        "http://www.tvbrowser.org/iconthemes.php"), cc.xy(4, 9));
    
    layout.appendRow(new RowSpec("fill:3dlu:grow"));
    layout.appendRow(new RowSpec("pref"));

    mInfoArea = UiUtilities.createHelpTextArea(mLocalizer.msg("restartNote", "Please Restart"));
    mInfoArea.setForeground(Color.RED);
    mInfoArea.setVisible(mSomethingChanged);

    mSettingsPn.add(mInfoArea, cc.xyw(1, 11, 6));
    
    if(!mSomethingChanged) {
      mStartLookAndIndex = mLfComboBox.getSelectedIndex();
      mStartIconIndex = mIconThemes.getSelectedIndex();
      mStartPluginViewPositionIndex = mPluginViewPosition.getSelectedIndex();
      mJGoodiesStartTheme = Settings.propJGoodiesTheme.getString();
      mJGoodiesStartShadow = Settings.propJGoodiesShadow.getBoolean();
      mSkinLFStartTheme = Settings.propSkinLFThemepack.getString();
    }
    
    mIconThemes.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        checkIfAreaIsToMakeVisible();
      }
    });
    
    lookChanged();    
    
    return mSettingsPn;
  }
  
  private void checkIfAreaIsToMakeVisible() {
    mInfoArea.setVisible(
        mLfComboBox.getSelectedIndex() != mStartLookAndIndex ||
        mIconThemes.getSelectedIndex() != mStartIconIndex ||
        mJGoodiesStartTheme.compareTo(Settings.propJGoodiesTheme.getString()) != 0 ||
        mJGoodiesStartShadow != Settings.propJGoodiesShadow.getBoolean() ||
        mSkinLFStartTheme.compareTo(Settings.propSkinLFThemepack.getString()) != 0 ||
        mPluginViewPosition.getSelectedIndex() != mStartPluginViewPositionIndex);
  }

  protected void configTheme() {
    String classname = ((LookAndFeelObj)mLfComboBox.getSelectedItem()).getLFClassName();
    
    if (classname.startsWith("com.jgoodies")) {
      JGoodiesLNFSettings settings = new JGoodiesLNFSettings((JDialog) UiUtilities.getBestDialogParent(mSettingsPn));
      UiUtilities.centerAndShow(settings);
    } else if(classname.startsWith("com.l2fprod.gui.plaf.skin.SkinLookAndFeel")) {
      SkinLNFSettings settings = new SkinLNFSettings((JDialog) UiUtilities.getBestDialogParent(mSettingsPn));
      UiUtilities.centerAndShow(settings);
    }
    
    checkIfAreaIsToMakeVisible();
  }

  protected void lookChanged() {
    String classname = ((LookAndFeelObj)mLfComboBox.getSelectedItem()).getLFClassName();
    
    if (classname.startsWith("com.jgoodies") || classname.startsWith("com.l2fprod")) {
      mConfigBtn.setEnabled(true);
    } else {
      mConfigBtn.setEnabled(false);
    }
    
    checkIfAreaIsToMakeVisible();
  }

  public void saveSettings() {
    LookAndFeelObj obj = (LookAndFeelObj) mLfComboBox.getSelectedItem();
    Settings.propLookAndFeel.setString(obj.getLFClassName());
    
    IconTheme theme = (IconTheme) mIconThemes.getSelectedItem();
    Settings.propIcontheme.setString(theme.getBase().getAbsolutePath().substring(System.getProperty("user.dir").length()+1).replace(File.separatorChar,'/'));
    
    mSomethingChanged = mInfoArea.isVisible();
    
    Settings.propPluginViewIsLeft.setBoolean(mPluginViewPosition.getSelectedIndex() == 1);
  }

  public Icon getIcon() {
    return null/*IconLoader.getInstance().getIconFromTheme("apps", "preferences-desktop-theme", 16)*/;
  }

  public String getTitle() {
    return mLocalizer.msg("graphical", "Graphical settings");
  }

}