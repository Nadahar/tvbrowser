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
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.Border;

import tvbrowser.core.PluginManager;
import tvbrowser.core.Settings;
import util.ui.OrderChooser;
import util.ui.UiUtilities;
import devplugin.Plugin;
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
  
  
  /**
   * Creates a new instance of ProgramTableSettingsTab.
   */
  public ProgramPanelSettingsTab() {
  }


  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    String msg;
    JPanel p1, p2;
    
    Border helpTextBorder = BorderFactory.createEmptyBorder(10, 0, 0, 0);
    
    mSettingsPn = new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    p1 = new JPanel(new GridLayout(1, 2));
    mSettingsPn.add(p1, BorderLayout.NORTH);
    
    // icons
    p2 = new JPanel(new BorderLayout());
    msg = mLocalizer.msg("pluginIcons", "Plugin icons");
    p2.setBorder(BorderFactory.createTitledBorder(msg));
    p1.add(p2);

    IconPlugin[] allPluginArr = getAvailableIconPlugins();
    IconPlugin[] pluginOrderArr = getSelectedIconPlugins(allPluginArr);
    mIconPluginOCh = new OrderChooser(pluginOrderArr, allPluginArr);
    p2.add(mIconPluginOCh, BorderLayout.CENTER);

    msg = mLocalizer.msg("pluginIcons.description", "");
    JTextArea helpTA = UiUtilities.createHelpTextArea(msg);
    helpTA.setBorder(helpTextBorder);
    p2.add(helpTA, BorderLayout.SOUTH);
    
    // info text
    p2 = new JPanel(new BorderLayout());
    msg = mLocalizer.msg("infoText", "Info text");
    p2.setBorder(BorderFactory.createTitledBorder(msg));
    p1.add(p2);

    ProgramFieldType[] allTypeArr = getAvailableTypes();
    ProgramFieldType[] typeOrderArr = getSelectedTypes();
    mInfoTextOCh = new OrderChooser(typeOrderArr, allTypeArr);
    p2.add(mInfoTextOCh, BorderLayout.CENTER);

    msg = mLocalizer.msg("infoText.description", "");
    helpTA = UiUtilities.createHelpTextArea(msg);
    helpTA.setBorder(helpTextBorder);
    p2.add(helpTA, BorderLayout.SOUTH);

    return mSettingsPn;
  }
  
  
  private IconPlugin[] getAvailableIconPlugins() {
    ArrayList list = new ArrayList();
    
    Plugin[] pluginArr = PluginManager.getInstance().getInstalledPlugins();
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
    String[] selPluginArr = Settings.getProgramTableIconPlugins();
    ArrayList list = new ArrayList();
    
    for (int i = 0; i < selPluginArr.length; i++) {
      // Find the corresponing IconPlugin and put it into the list
      for (int j = 0; j < allArr.length; j++) {
        String className = allArr[j].getPlugin().getClass().getName();
        if (className.equals(selPluginArr[i])) {
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
    return Settings.getProgramInfoFields();
  }
  
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    // icons
    Object[] iconPluginArr = mIconPluginOCh.getOrder();
    String[] classNameArr = new String[iconPluginArr.length];
    for (int i = 0; i < iconPluginArr.length; i++) {
      IconPlugin plugin = (IconPlugin) iconPluginArr[i];
      classNameArr[i] = plugin.getPlugin().getClass().getName();
      System.out.println("Setting '" + classNameArr[i] + "'");
    }
    Settings.setProgramTableIconPlugins(classNameArr);
    
    // info text
    Object[] infoFieldArr = mInfoTextOCh.getOrder();
    ProgramFieldType[] typeArr = new ProgramFieldType[infoFieldArr.length];
    for (int i = 0; i < typeArr.length; i++) {
      typeArr[i] = (ProgramFieldType) infoFieldArr[i];
    }
    Settings.setProgramInfoFields(typeArr);
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
    
    private Plugin mPlugin;
    
    public IconPlugin(Plugin plugin) {
      mPlugin = plugin;
    }
    
    public Plugin getPlugin() {
      return mPlugin;
    }
    
    public String toString() {
      return mPlugin.getProgramTableIconText();
    }
    
  }

}

