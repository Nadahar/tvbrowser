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

package tvbrowser.ui.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.OrderChooser;
import util.ui.UiUtilities;
import util.ui.customizableitems.SelectableItemRendererCenterComponentIf;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ProgramFieldType;
import devplugin.ProgramInfoHelper;
import devplugin.SettingsTab;

/**
 * A settings tab for the program panel.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ProgramPanelSettingsTab implements SettingsTab {

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramPanelSettingsTab.class);

  private static final String PICTURE_ICON_NAME = mLocalizer.msg("hasPicure", "Has picture");

  private OrderChooser mIconPluginOCh;
  private OrderChooser mInfoTextOCh;

  private ColorLabel mProgramItemOnAirColorLb, mProgramItemProgressColorLb, mProgramItemKeyboardSelectedLb;

  private JCheckBox mAllowProgramImportance;
  private JCheckBox mBorderForOnAirPrograms;

  private ArrayList<IconPlugin> mFormatIcons;

  private JCheckBox mHyphenator;
  

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    EnhancedPanelBuilder panel = new EnhancedPanelBuilder("5dlu, fill:50dlu:grow, 3dlu, fill:50dlu:grow, 3dlu");
    panel.setBorder(Borders.DIALOG_BORDER);

    CellConstraints cc = new CellConstraints();

    panel.addParagraph("");
    // icons
    panel.add(DefaultComponentFactory.getInstance()
        .createSeparator(mLocalizer.msg("pluginIcons", "Plugin icons")), cc.xyw(1, panel.getRowCount(), 2));

    IconPlugin[] allPluginArr = getAvailableIconPlugins();
    IconPlugin[] pluginOrderArr = getSelectedIconPlugins(allPluginArr);
    mIconPluginOCh = new OrderChooser(pluginOrderArr, allPluginArr, IconPlugin.class,
        new SelectableItemRendererCenterComponentIf() {
          private DefaultListCellRenderer mRenderer = new DefaultListCellRenderer();

          @Override
          public JPanel createCenterPanel(JList list, Object value, int index, boolean isSelected, boolean isEnabled,
              JScrollPane parentScrollPane, int leftColumnWidth) {
            DefaultListCellRenderer label = (DefaultListCellRenderer) mRenderer.getListCellRendererComponent(list,
                value, index, isSelected, false);
            IconPlugin iconPlugin = (IconPlugin) value;
            label.setIcon(iconPlugin.getIcon());
            label.setHorizontalAlignment(SwingConstants.LEADING);
            label.setVerticalAlignment(SwingConstants.CENTER);
            label.setOpaque(false);

            JPanel panel = new JPanel(new BorderLayout());
            if (isSelected && isEnabled) {
              panel.setOpaque(true);
              panel.setForeground(list.getSelectionForeground());
              panel.setBackground(list.getSelectionBackground());
            } else {
              panel.setOpaque(false);
              panel.setForeground(list.getForeground());
              panel.setBackground(list.getBackground());
            }
            panel.add(label, BorderLayout.WEST);
            return panel;
          }

          @Override
          public void calculateSize(JList list, int index, JPanel contentPane) {
          }
        });

    // info text
    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("infoText", "Info text")), cc
        .xyw(4, panel.getRowCount(), 2));

    ProgramFieldType[] allTypeArr = getAvailableTypes();
    ProgramFieldType[] typeOrderArr = getSelectedTypes();
    mInfoTextOCh = new OrderChooser(typeOrderArr, allTypeArr);

    panel.addGrowingRow();
    panel.add(mIconPluginOCh, cc.xy(2, panel.getRowCount()));
    panel.add(mInfoTextOCh, cc.xy(4, panel.getRowCount()));

    panel.addRow("top:pref");
    panel.add(UiUtilities.createHelpTextArea(mLocalizer.msg("pluginIcons.description", "")), cc.xy(2, panel.getRowCount()));
    panel.add(UiUtilities.createHelpTextArea(mLocalizer.msg("infoText.description", "")), cc.xy(4, panel.getRowCount()));

    panel.addParagraph(mLocalizer.msg("Colors", "Colors"));

    panel.addRow();
    panel.add(mAllowProgramImportance = new JCheckBox(mLocalizer.msg("color.allowTransparency","Allow plugins to set the transparency of a program"),
        Settings.propProgramPanelAllowTransparency.getBoolean()), cc.xyw(2, panel.getRowCount() ,3));
    
    panel.addRow();
    panel.add(mBorderForOnAirPrograms = new JCheckBox(mLocalizer.msg("color.programOnAirWithBorder",
        "Border for programs on air"), Settings.propProgramTableOnAirProgramsShowingBorder.getBoolean()), cc.xyw(2, panel.getRowCount(),
        3));

    JPanel colors = new JPanel();
    Color programItemProgressColor = Settings.propProgramTableColorOnAirDark.getColor();
    Color programItemOnAirColor = Settings.propProgramTableColorOnAirLight.getColor();
    Color programItemKeyboardSelectedColor = Settings.propKeyboardSelectedColor.getColor();

    Color programItemDefaultProgressColor = Settings.propProgramTableColorOnAirDark.getDefaultColor();
    Color programItemDefaultOnAirColor = Settings.propProgramTableColorOnAirLight.getDefaultColor();
    Color programItemDefaultKeyboardSelectedColor = Settings.propKeyboardSelectedColor.getDefaultColor();

    FormLayout formLayout = new FormLayout("default, 5dlu, default, 5dlu, default, 5dlu, default",
        "5dlu, default, 3dlu, default, 3dlu, default");
    colors.setLayout(formLayout);

    colors.add(new JLabel(mLocalizer.msg("color.programOnAir", "Background color for programs on air")), cc.xy(1, 2));
    colors.add(mProgramItemOnAirColorLb = new ColorLabel(programItemOnAirColor), cc.xy(3, 2));
    mProgramItemOnAirColorLb.setStandardColor(programItemDefaultOnAirColor);
    colors.add(new ColorButton(mProgramItemOnAirColorLb), cc.xy(5, 2));

    colors.add(new JLabel(mLocalizer.msg("color.programProgress", "Progress bar for programs on air")), cc.xy(
        1, 4));
    colors.add(mProgramItemProgressColorLb = new ColorLabel(programItemProgressColor), cc.xy(3, 4));
    mProgramItemProgressColorLb.setStandardColor(programItemDefaultProgressColor);
    colors.add(new ColorButton(mProgramItemProgressColorLb), cc.xy(5, 4));

    colors.add(new JLabel(mLocalizer.msg("color.keyboardSelected", "Color for programs selected by keyboard")), cc.xy(1, 6));
    colors.add(mProgramItemKeyboardSelectedLb = new ColorLabel(programItemKeyboardSelectedColor), cc.xy(3, 6));
    mProgramItemKeyboardSelectedLb.setStandardColor(programItemDefaultKeyboardSelectedColor);
    colors.add(new ColorButton(mProgramItemKeyboardSelectedLb), cc.xy(5, 6));

    panel.addRow();
    panel.add(colors, cc.xyw(2, panel.getRowCount(), panel.getColumnCount() - 1));
    
    panel.addParagraph(mLocalizer.msg("text", "Text"));
    panel.addRow();
    panel.add(mHyphenator = new JCheckBox(mLocalizer.msg("hyphenation", "Use hyphenation"), Settings.propProgramPanelHyphenation.getBoolean()), cc.xyw(2, panel.getRowCount(), panel.getColumnCount() - 1));

    return panel.getPanel();
  }

  private IconPlugin[] getAvailableIconPlugins() {
    final ArrayList<IconPlugin> list = new ArrayList<IconPlugin>();

    list.addAll(getFormatIconNames());
    list.add(new IconPlugin(PICTURE_ICON_NAME, new ImageIcon("imgs/Info_HasPicture.png")));

    for (PluginProxy plugin : PluginProxyManager.getInstance().getActivatedPlugins()) {
      final String iconText = plugin.getProgramTableIconText();
      if (iconText != null) {
        Icon[] icons = plugin.getProgramTableIcons(PluginManagerImpl.getInstance().getExampleProgram());
        Icon icon;
        if (icons != null && icons.length > 0) {
          icon = icons[0];
        }
        else {
          icon = null;
        }
        list.add(new IconPlugin(plugin, icon));
      }
    }

    return list.toArray(new IconPlugin[list.size()]);
  }

  private IconPlugin[] getSelectedIconPlugins(final IconPlugin[] allArr) {
    final String[] selPluginArr = Settings.propProgramTableIconPlugins.getStringArray();
    final ArrayList<IconPlugin> list = new ArrayList<IconPlugin>();

    for (String selectedPluginId : selPluginArr) {
      for (IconPlugin iconPlugin : allArr) {
        final String pluginId = iconPlugin.getId();
        if (selectedPluginId.equals(pluginId)) {
          list.add(iconPlugin);
          break;
        }
      }
      if (selectedPluginId.equals(Settings.INFO_ID)) {
        list.addAll(getFormatIconNames());
      }
    }

    return list.toArray(new IconPlugin[list.size()]);
  }

  private List<IconPlugin> getFormatIconNames() {
    if (mFormatIcons == null) {
      mFormatIcons = new ArrayList<IconPlugin>();
      String[] iconFilenames = ProgramInfoHelper.getInfoIconFilenames();
      Icon[] infoIcons = ProgramInfoHelper.getInfoIcons();
      String[] infoMessages = ProgramInfoHelper.getInfoIconMessages();
      for (int i = 0; i < iconFilenames.length; i++) {
        if (infoIcons[i] != null) {
          mFormatIcons.add(new IconPlugin(mLocalizer.msg("formatIcon", "Format: {0}", infoMessages[i]), infoIcons[i]));
        }
      }
    }
    return mFormatIcons;
  }

  private ProgramFieldType[] getAvailableTypes() {
    ArrayList<ProgramFieldType> typeList = new ArrayList<ProgramFieldType>();

    Iterator<ProgramFieldType> typeIter = ProgramFieldType.getTypeIterator();
    while (typeIter.hasNext()) {
      ProgramFieldType type = (ProgramFieldType) typeIter.next();

      if ((type.getFormat() != ProgramFieldType.BINARY_FORMAT) && (type != ProgramFieldType.INFO_TYPE)
          && (type != ProgramFieldType.PICTURE_DESCRIPTION_TYPE) && (type != ProgramFieldType.PICTURE_COPYRIGHT_TYPE)) {
        typeList.add(type);
      }
    }

    return typeList.toArray(new ProgramFieldType[typeList.size()]);
  }

  private ProgramFieldType[] getSelectedTypes() {
    return Settings.propProgramInfoFields.getProgramFieldTypeArray();
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    // icons
    Object[] iconPluginArr = mIconPluginOCh.getOrder();
    String[] pluginIdArr = new String[iconPluginArr.length];
    for (int i = 0; i < iconPluginArr.length; i++) {
      IconPlugin plugin = (IconPlugin) iconPluginArr[i];
      pluginIdArr[i] = plugin.getId();
    }
    Settings.propProgramTableIconPlugins.setStringArray(pluginIdArr);

    // info text
    Object[] infoFieldArr = mInfoTextOCh.getOrder();
    ProgramFieldType[] typeArr = new ProgramFieldType[infoFieldArr.length];
    for (int i = 0; i < typeArr.length; i++) {
      typeArr[i] = (ProgramFieldType) infoFieldArr[i];
    }
    Settings.propProgramInfoFields.setProgramFieldTypeArray(typeArr);

    Settings.propProgramTableOnAirProgramsShowingBorder.setBoolean(mBorderForOnAirPrograms.isSelected());

    Settings.propProgramTableColorOnAirDark.setColor(mProgramItemProgressColorLb.getColor());
    Settings.propProgramTableColorOnAirLight.setColor(mProgramItemOnAirColorLb.getColor());
    Settings.propKeyboardSelectedColor.setColor(mProgramItemKeyboardSelectedLb.getColor());
    Settings.propProgramPanelAllowTransparency.setBoolean(mAllowProgramImportance.isSelected());
    Settings.propProgramPanelHyphenation.setBoolean(mHyphenator.isSelected());
  }

  /**
   * Returns the icon of the tab-sheet.
   */
  public Icon getIcon() {
    return null;
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("title", "Program display");
  }

  private static class IconPlugin {

    private PluginProxy mPlugin;
    private String mName;
    private Icon mIcon;

    public IconPlugin(final PluginProxy plugin, Icon icon) {
      mPlugin = plugin;
      mIcon = icon;
    }

    public Icon getIcon() {
      return mIcon;
    }

    public IconPlugin(final String name, final Icon icon) {
      mName = name;
      mPlugin = null;
      mIcon = icon;
    }

    public String getId() {
      if (mPlugin != null) {
        return mPlugin.getId();
      } else if (mName != null && mName.compareTo(PICTURE_ICON_NAME) == 0) {
        return Settings.PICTURE_ID;
      } else {
        String[] infoFilenames = ProgramInfoHelper.getInfoIconFilenames();
        Icon[] infoIcons = ProgramInfoHelper.getInfoIcons();
        String[] infoMessages = ProgramInfoHelper.getInfoIconMessages();
        for (int i = 0; i < infoFilenames.length; i++) {
          if (infoIcons[i] != null) {
            if (mLocalizer.msg("formatIcon", "Format: {0}", infoMessages[i]).equals(mName)) {
              return "FORMAT_" + i;
            }
          }
        }
      }
      return null;
    }

    public String toString() {
      if (mPlugin != null) {
        return mPlugin.getProgramTableIconText();
      } else {
        return mName;
      }
    }

  }

}
