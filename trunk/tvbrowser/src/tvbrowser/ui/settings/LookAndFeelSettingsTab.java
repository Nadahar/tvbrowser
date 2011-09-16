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
import java.util.Arrays;
import java.util.Comparator;

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
import util.ui.persona.Persona;
import util.ui.persona.PersonaInfo;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsTab;

public final class LookAndFeelSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(LookAndFeelSettingsTab.class);

  private JComboBox mLfComboBox;

  private JPanel mSettingsPn;

  private JButton mConfigBtn;

  private JComboBox mIconThemes;

  private JComboBox mPluginViewPosition;

  private JComboBox mDateLayout;
  
  private JComboBox mPersonaSelection;

  private JTextArea mRestartMessage;

  private int mStartLookAndIndex;
  private int mStartIconIndex;
  private int mStartPluginViewPositionIndex;

  private String mJGoodiesStartTheme;
  private boolean mJGoodiesStartShadow;

  private String mSkinLFStartTheme;

  private boolean mSomethingChanged = false;

  private static class LookAndFeelObj implements Comparable<LookAndFeelObj> {
    private UIManager.LookAndFeelInfo info;

    public LookAndFeelObj(UIManager.LookAndFeelInfo info) {
      this.info = info;
    }

    @Override
    public String toString() {
      return info.getName();
    }

    public String getLFClassName() {
      return info.getClassName();
    }

    public int compareTo(LookAndFeelObj other) {
      return this.toString().compareTo(other.toString());
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

    layout.appendRow(RowSpec.decode("pref"));
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("lookAndFeel", "Look and Feel")), cc.xyw(1, 1, 7));

    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));

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
        updateRestartMessage();
      }
    });

    mSettingsPn.add(mPluginViewPosition, cc.xy(4,3));

    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    mSettingsPn.add(new JLabel(mLocalizer.msg("dateFormat", "Layout of Datelist")+":"), cc.xy(2, 5));

    mDateLayout = new JComboBox(new String[] {
            mLocalizer.msg("dateFormat.datelist", "List"),
            mLocalizer.msg("dateFormat.calendarTable", "Calendar (Table)"),
            mLocalizer.msg("dateFormat.calendarButtons", "Calendar (Buttons)")
    });

    mDateLayout.setSelectedIndex(Settings.propViewDateLayout.getInt());

    mDateLayout.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        updateRestartMessage();
      }
    });

    mSettingsPn.add(mDateLayout, cc.xy(4,5));

    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    mSettingsPn.add(new JLabel(mLocalizer.msg("theme", "Theme") +":"), cc.xy(2, 7));

    LookAndFeelObj[] lfObjects = getLookAndFeelObjs();
    Arrays.sort(lfObjects);
    mLfComboBox = new JComboBox(lfObjects);

    String lfName = Settings.propLookAndFeel.getString();
    for (LookAndFeelObj lfObject : lfObjects) {
      if (lfObject.getLFClassName().equals(lfName)) {
        mLfComboBox.setSelectedItem(lfObject);
      }
    }

    mLfComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        lookChanged();
      }
    });

    mSettingsPn.add(mLfComboBox, cc.xy(4, 7));

    mConfigBtn = new JButton(mLocalizer.msg("config", "Config"));
    mConfigBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        configTheme();
      }
    });

    mSettingsPn.add(mConfigBtn, cc.xy(6, 7));
    
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    
    mSettingsPn.add(new JLabel(mLocalizer.msg("persona", "Persona") + ":"), cc.xy(2, 9));
    
    PersonaInfo[] installedPersonas = Persona.getInstance().getInstalledPersonas();
    Arrays.sort(installedPersonas,new Comparator<PersonaInfo>() {
      @Override
      public int compare(PersonaInfo o1, PersonaInfo o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    
    mPersonaSelection = new JComboBox(installedPersonas);
    
    for(PersonaInfo info : installedPersonas) {
      if(Settings.propSelectedPersona.getString().equals(info.getId())) {
        mPersonaSelection.setSelectedItem(info);
      }
    }
    
    mPersonaSelection.setRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value != null) {
          label.setText(((PersonaInfo)value).getName());
          label.setToolTipText(((PersonaInfo)value).getDescription());
        }
        return label;
      }
    });
    
    mSettingsPn.add(mPersonaSelection, cc.xy(4,9));
    
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    mSettingsPn.add(new JLabel(mLocalizer.msg("icons", "Icons") + ":"), cc.xy(2, 11));

    mIconThemes = new JComboBox(IconLoader.getInstance().getAvailableThemes());
    mIconThemes.setRenderer(new DefaultListCellRenderer() {
      @Override
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
      IconTheme theme = IconLoader.getInstance().getIconTheme(IconLoader.getInstance().getIconThemeFile(Settings.propIcontheme.getString()));
      if (theme.loadTheme()) {
        mIconThemes.setSelectedItem(theme);
      } else {
        mIconThemes.setSelectedItem(IconLoader.getInstance().getDefaultTheme());
      }
    } else {
      mIconThemes.setSelectedItem(IconLoader.getInstance().getDefaultTheme());
    }

    mSettingsPn.add(mIconThemes, cc.xy(4, 11));

    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    mSettingsPn.add(new LinkButton(mLocalizer.msg("findMoreIcons","You can find more Icons on our Web-Page."),
        "http://www.tvbrowser.org/iconthemes.php"), cc.xy(4, 13));

    layout.appendRow(RowSpec.decode("fill:3dlu:grow"));
    layout.appendRow(RowSpec.decode("pref"));

    mRestartMessage = UiUtilities.createHelpTextArea(mLocalizer.msg("restartNote", "Please Restart"));
    mRestartMessage.setForeground(Color.RED);
    mRestartMessage.setVisible(mSomethingChanged);

    mSettingsPn.add(mRestartMessage, cc.xyw(1, 15, 6));

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
        updateRestartMessage();
      }
    });

    lookChanged();

    return mSettingsPn;
  }

  private void updateRestartMessage() {
    mRestartMessage.setVisible(
        mLfComboBox.getSelectedIndex() != mStartLookAndIndex ||
        mIconThemes.getSelectedIndex() != mStartIconIndex ||
        mJGoodiesStartTheme.compareTo(Settings.propJGoodiesTheme.getString()) != 0 ||
        mJGoodiesStartShadow != Settings.propJGoodiesShadow.getBoolean() ||
        mSkinLFStartTheme.compareTo(Settings.propSkinLFThemepack.getString()) != 0 ||
        mPluginViewPosition.getSelectedIndex() != mStartPluginViewPositionIndex);
  }

  void configTheme() {
    String classname = ((LookAndFeelObj)mLfComboBox.getSelectedItem()).getLFClassName();

    if (classname.startsWith("com.jgoodies")) {
      JGoodiesLNFSettings settings = new JGoodiesLNFSettings((JDialog) UiUtilities.getBestDialogParent(mSettingsPn));
      UiUtilities.centerAndShow(settings);
    } else if(classname.startsWith("com.l2fprod.gui.plaf.skin.SkinLookAndFeel")) {
      SkinLNFSettings settings = new SkinLNFSettings((JDialog) UiUtilities.getBestDialogParent(mSettingsPn));
      UiUtilities.centerAndShow(settings);
    }

    updateRestartMessage();
  }

  void lookChanged() {
    String classname = ((LookAndFeelObj)mLfComboBox.getSelectedItem()).getLFClassName();

    if (classname.startsWith("com.jgoodies") || classname.startsWith("com.l2fprod")) {
      mConfigBtn.setEnabled(true);
    } else {
      mConfigBtn.setEnabled(false);
    }

    updateRestartMessage();
  }

  public void saveSettings() {
    LookAndFeelObj obj = (LookAndFeelObj) mLfComboBox.getSelectedItem();
    Settings.propLookAndFeel.setString(obj.getLFClassName());

    IconTheme theme = (IconTheme) mIconThemes.getSelectedItem();
    Settings.propIcontheme.setString("icons/" + theme.getBase().getName());

    mSomethingChanged = mRestartMessage.isVisible();

    Settings.propPluginViewIsLeft.setBoolean(mPluginViewPosition.getSelectedIndex() == 1);
    Settings.propViewDateLayout.setInt(mDateLayout.getSelectedIndex());
    Settings.propSelectedPersona.setString(((PersonaInfo)mPersonaSelection.getSelectedItem()).getId());
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("apps", "preferences-desktop-theme", 16);
  }

  public String getTitle() {
    return mLocalizer.msg("graphical", "Graphical settings");
  }

}