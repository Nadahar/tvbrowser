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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.ui.OrderChooser;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ProgramFieldType;
import devplugin.SettingsTab;

/**
 * A settings tab for the program panel.
 *
 * @author Til Schneider, www.murfman.de
 */
public class ProgramPanelSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ProgramPanelSettingsTab.class);
  
  private JPanel mSettingsPn;
  
  private OrderChooser mIconPluginOCh;
  private OrderChooser mInfoTextOCh;

  private ColorLabel mProgramItemOnAirColorLb, mProgramItemProgressColorLb, mProgramItemMarkedColorLb, mProgramItemKeyboardSelectedLb;

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    mSettingsPn = new JPanel(new FormLayout("5dlu, fill:50dlu:grow, 3dlu, fill:50dlu:grow, 3dlu", 
        "pref, 5dlu, fill:pref:grow, 3dlu, top:pref, 5dlu, pref, 5dlu, pref"));
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);
    
    CellConstraints cc = new CellConstraints();

    // icons
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("pluginIcons", "Plugin icons")), cc.xyw(1,1,2));
    
    IconPlugin[] allPluginArr = getAvailableIconPlugins();
    IconPlugin[] pluginOrderArr = getSelectedIconPlugins(allPluginArr);
    mIconPluginOCh = new OrderChooser(pluginOrderArr, allPluginArr);
    
    mSettingsPn.add(mIconPluginOCh, cc.xy(2,3));
    mSettingsPn.add(UiUtilities.createHelpTextArea(mLocalizer.msg("pluginIcons.description", "")), cc.xy(2,5));
    
    // info text
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("infoText", "Info text")), cc.xyw(4,1,2));

    ProgramFieldType[] allTypeArr = getAvailableTypes();
    ProgramFieldType[] typeOrderArr = getSelectedTypes();
    mInfoTextOCh = new OrderChooser(typeOrderArr, allTypeArr);

    mSettingsPn.add(mInfoTextOCh, cc.xy(4,3));
    mSettingsPn.add(UiUtilities.createHelpTextArea(mLocalizer.msg("infoText.description", "")), cc.xy(4,5));
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("Colors", "Colors")), cc.xyw(1,7,5));

    JPanel colors = new JPanel();
    Color programItemProgressColor = Settings.propProgramTableColorOnAirDark.getColor();
    Color programItemOnAirColor = Settings.propProgramTableColorOnAirLight.getColor();
    Color programItemMarkedColor = Settings.propProgramTableColorMarked.getColor();
    Color programItemKeyboardSelectedColor = Settings.propKeyboardSelectedColor.getColor();

    Color programItemDefaultProgressColor = Settings.propProgramTableColorOnAirDark.getDefaultColor();
    Color programItemDefaultOnAirColor = Settings.propProgramTableColorOnAirLight.getDefaultColor();
    Color programItemDefaultMarkedColor = Settings.propProgramTableColorMarked.getDefaultColor();
    Color programItemDefaultKeyboardSelectedColor = Settings.propKeyboardSelectedColor.getDefaultColor();

    FormLayout formLayout = new FormLayout("default, 5dlu, default, 5dlu, default, 5dlu, default",
            "default, 3dlu, default, 3dlu, default, 3dlu, default");
    CellConstraints c = new CellConstraints();
    colors.setLayout(formLayout);

    colors.add(new JLabel(mLocalizer.msg("color.programOnAir","Hintergrundfarbe fuer laufende Sendung")), c.xy(1,1));
    colors.add(mProgramItemOnAirColorLb = new ColorLabel(programItemOnAirColor), c.xy(3,1));
    mProgramItemOnAirColorLb.setStandardColor(programItemDefaultOnAirColor);
    colors.add(new ColorButton(mProgramItemOnAirColorLb), c.xy(5,1));

    colors.add(new JLabel(mLocalizer.msg("color.programProgress", "Fortschrittanzeige fuer laufende Sendung")), c.xy(1,3));
    colors.add(mProgramItemProgressColorLb = new ColorLabel(programItemProgressColor), c.xy(3,3));
    mProgramItemProgressColorLb.setStandardColor(programItemDefaultProgressColor);
    colors.add(new ColorButton(mProgramItemProgressColorLb), c.xy(5,3));

    colors.add(new JLabel(mLocalizer.msg("color.programMarked","Markierung durch Plugins")), c.xy(1,5));
    colors.add(mProgramItemMarkedColorLb = new ColorLabel(programItemMarkedColor), c.xy(3,5));
    mProgramItemMarkedColorLb.setStandardColor(programItemDefaultMarkedColor);
    colors.add(new ColorButton(mProgramItemMarkedColorLb), c.xy(5,5));

    colors.add(new JLabel(mLocalizer.msg("color.keyboardSelected","Markierung durch Plugins")), c.xy(1,7));
    colors.add(mProgramItemKeyboardSelectedLb = new ColorLabel(programItemKeyboardSelectedColor), c.xy(3,7));
    mProgramItemKeyboardSelectedLb.setStandardColor(programItemDefaultKeyboardSelectedColor);
    colors.add(new ColorButton(mProgramItemKeyboardSelectedLb), c.xy(5,7));

    mSettingsPn.add(colors, cc.xyw(2,9,4));

    return mSettingsPn;
  }
  
  
  private IconPlugin[] getAvailableIconPlugins() {
    ArrayList list = new ArrayList();
    
    list.add(new IconPlugin("Infos"));
    
    PluginProxy[] pluginArr = PluginProxyManager.getInstance().getActivatedPlugins();
    for (int i = 0; i < pluginArr.length; i++) {
      String iconText = pluginArr[i].getProgramTableIconText();
      if (iconText != null) {
        list.add(new IconPlugin(pluginArr[i]));
      }
    }
    
    IconPlugin[] asArr = new IconPlugin[list.size()];
    list.toArray(asArr);
    return asArr;
  }


  private IconPlugin[] getSelectedIconPlugins(IconPlugin[] allArr) {
    String[] selPluginArr = Settings.propProgramTableIconPlugins.getStringArray();
    ArrayList list = new ArrayList();
    
    for (int i = 0; i < selPluginArr.length; i++) {
      // Find the corresponing IconPlugin and put it into the list
      for (int j = 0; j < allArr.length; j++) {
        String pluginId = allArr[j].getId()/*.getPlugin().getId()*/;
        if (pluginId.equals(selPluginArr[i])) {
          list.add(allArr[j]);
          break;
        }
      }
    }

    IconPlugin[] asArr = new IconPlugin[list.size()];
    list.toArray(asArr);
    return asArr;
  }

  
  private ProgramFieldType[] getAvailableTypes() {
    ArrayList typeList = new ArrayList();
    
    Iterator typeIter = ProgramFieldType.getTypeIterator();
    while (typeIter.hasNext()) {
      ProgramFieldType type = (ProgramFieldType) typeIter.next();
      
      if ((type.getFormat() != ProgramFieldType.BINARY_FORMAT)
        && (type != ProgramFieldType.INFO_TYPE))
      {
        typeList.add(type);
      }
    }
    
    ProgramFieldType[] typeArr = new ProgramFieldType[typeList.size()];
    typeList.toArray(typeArr);
    return typeArr;
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
      pluginIdArr[i] = plugin/*.getPlugin()*/.getId();
    }
    Settings.propProgramTableIconPlugins.setStringArray(pluginIdArr);
    
    // info text
    Object[] infoFieldArr = mInfoTextOCh.getOrder();
    ProgramFieldType[] typeArr = new ProgramFieldType[infoFieldArr.length];
    for (int i = 0; i < typeArr.length; i++) {
      typeArr[i] = (ProgramFieldType) infoFieldArr[i];
    }
    Settings.propProgramInfoFields.setProgramFieldTypeArray(typeArr);

    Settings.propProgramTableColorMarked.setColor(mProgramItemMarkedColorLb.getColor());
    Settings.propProgramTableColorOnAirDark.setColor(mProgramItemProgressColorLb.getColor());
    Settings.propProgramTableColorOnAirLight.setColor(mProgramItemOnAirColorLb.getColor());
    Settings.propKeyboardSelectedColor.setColor(mProgramItemKeyboardSelectedLb.getColor());
  }
  
  
  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return null;
  }
  
  
  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("programPanel", "Program display");
  }
  
  
  class IconPlugin {
    
    private PluginProxy mPlugin;
    private String mName;
    
    public IconPlugin(PluginProxy plugin) {
      mPlugin = plugin;
    }
    
    public IconPlugin(String name) {
      mName = name;
      mPlugin = null;
    }
    
    public String getId() {
      if(mPlugin != null)
        return mPlugin.getId();
      else
        return "info.id";
    }
    
    public String toString() {
      if(mPlugin != null)
        return mPlugin.getProgramTableIconText();
      else
        return mName;
    }
    
  }

}

