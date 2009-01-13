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
package clipboardplugin;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import util.program.LocalPluginProgramFormating;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * This Plugin is an internal Clipboard.
 * 
 * @author bodo
 */
public class ClipboardPlugin extends Plugin {
  private static final Version mVersion = new Version(2,60);

  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ClipboardPlugin.class);

  /** The Default-Parameters */
  private static LocalPluginProgramFormating DEFAULT_CONFIG = new LocalPluginProgramFormating("clipDefault", mLocalizer.msg("defaultName","CliboardPlugin - Default"),"{title}","{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n","UTF-8");
  
  private AbstractPluginProgramFormating[] mConfigs = null;
  private LocalPluginProgramFormating[] mLocalFormatings = null;

  private PluginInfo mPluginInfo;
  
  /**
   * Creates an instance of this Plugin   
   */
  public ClipboardPlugin() {
    createDefaultConfig();
    createDefaultAvailable();
  }
  
  private void createDefaultConfig() {
    mConfigs = new AbstractPluginProgramFormating[1];
    mConfigs[0] = DEFAULT_CONFIG;
  }
  
  private void createDefaultAvailable() {
    mLocalFormatings = new LocalPluginProgramFormating[1];
    mLocalFormatings[0] = DEFAULT_CONFIG;        
  }
  
  public ActionMenu getContextMenuActions(final Program program) {
    ImageIcon img = createImageIcon("actions", "edit-paste", 16);

    if(mConfigs.length > 1) {
      ContextMenuAction copyToSystem = new ContextMenuAction(mLocalizer.msg("copyToSystem", "Copy to System-Clipboard")+ "...");
      
      ArrayList<AbstractAction> list = new ArrayList<AbstractAction>();
    
      for (int i = 0; i < mConfigs.length; i++) {
        final AbstractPluginProgramFormating config = mConfigs[i];
        if (config != null && config.isValid()) {
          final Program[] programs = { program };
          AbstractAction copyAction = new AbstractAction(config.getName()) {
            public void actionPerformed(ActionEvent e) {
              copyProgramsToSystem(programs, config);
            }
          };
          String text = getTextForConfig(programs, config);
          copyAction.setEnabled(text == null || !text.equalsIgnoreCase(""));
          list.add(copyAction);
        }
      }
    
      copyToSystem.putValue(Action.SMALL_ICON, img);

      return new ActionMenu(copyToSystem, list.toArray(new AbstractAction[list.size()]));
    }
    else {
      AbstractAction copyToSystem = new AbstractAction(mLocalizer.msg("copyToSystem", "Copy to System-Clipboard")) {
        public void actionPerformed(ActionEvent evt) {
          Program[] list = { program };
          copyProgramsToSystem(list,mConfigs.length != 1 ? DEFAULT_CONFIG : mConfigs[0]);
        }
      };
      
      copyToSystem.putValue(Action.SMALL_ICON, img);
      
      return new ActionMenu(copyToSystem);
    }
  }

  public static Version getVersion() {
    return mVersion;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#getInfo()
   */
  public PluginInfo getInfo() {
    if(mPluginInfo == null) {
      String name = mLocalizer.msg("pluginName", "Clipboard");
      String desc = mLocalizer.msg("description",
          "Copy programs to the Clipboard");
      String author = "Bodo Tasche";
        
      mPluginInfo = new PluginInfo(ClipboardPlugin.class, name, desc, author);
    }
    
    return mPluginInfo;
  }

 
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }
  
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>();
    
    for(AbstractPluginProgramFormating config : mConfigs)
      if(config != null && config.isValid())
        list.add(new ProgramReceiveTarget(this, config.getName(), config.getId()));
    
    if(list.isEmpty())
      list.add(new ProgramReceiveTarget(this, DEFAULT_CONFIG.getName(), DEFAULT_CONFIG.getId()));
    
    return list.toArray(new ProgramReceiveTarget[list.size()]);
  }

  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget target) {
    AbstractPluginProgramFormating formating = null;
    
    if(target == null)
      return false;
    
    if(target.isReceiveTargetWithIdOfProgramReceiveIf(this,DEFAULT_CONFIG.getId()))
      formating = DEFAULT_CONFIG;
    else
      for(AbstractPluginProgramFormating config : mConfigs)
        if(target.isReceiveTargetWithIdOfProgramReceiveIf(this,config.getId())) {
          formating = config;
          break;
        }
    
    if(formating != null) {
      copyProgramsToSystem(programArr, formating);
      return true;
    }
    
    return false;
  }

  public void loadSettings(Properties settings) {
    if(settings != null && settings.containsKey("ParamToUse")) {
      mConfigs = new AbstractPluginProgramFormating[1];
      mConfigs[0] = new LocalPluginProgramFormating(mLocalizer.msg(
          "defaultName", "ClipboardPlugin - Default"), "{title}", settings
          .getProperty("ParamToUse"), "UTF-8");
      mLocalFormatings = new LocalPluginProgramFormating[1];
      mLocalFormatings[0] = (LocalPluginProgramFormating)mConfigs[0];
      DEFAULT_CONFIG = mLocalFormatings[0];
    }
  }

  public SettingsTab getSettingsTab() {
    return new ClipboardSettingsTab(this);
  }

  public String getTextForConfig(Program[] programs, AbstractPluginProgramFormating config) {
    String param = config.getContentValue();//mSettings.getProperty("ParamToUse", DEFAULT_PARAM);

    StringBuffer buffer = new StringBuffer();
    ParamParser parser = new ParamParser();

    int i = 0;

    while (!parser.hasErrors() && (i < programs.length)) {
      String prgResult = parser.analyse(param, programs[i]);
      buffer.append(prgResult);
      i++;
    }

    if (parser.hasErrors()) {
      return null;
    } else {
      return buffer.toString();
    }
  }
  
  /**
   * Copy Programs to System-Clipboard
   * 
   * @param programs Programs to Copy
   * @param config The program formating.
   */
  public void copyProgramsToSystem(Program[] programs, AbstractPluginProgramFormating config) {
    String param = config.getContentValue();//mSettings.getProperty("ParamToUse", DEFAULT_PARAM);

    StringBuffer result = new StringBuffer();
    ParamParser parser = new ParamParser();

    int i = 0;

    while (!parser.hasErrors() && (i < programs.length)) {
      String prgResult = parser.analyse(param, programs[i]);
      result.append(prgResult);
      i++;
    }

    if (parser.hasErrors()) {
      JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(getParentFrame()), parser.getErrorString(),
          "Error", JOptionPane.ERROR_MESSAGE);
    } else {
      Clipboard clip = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
      clip.setContents(new StringSelection(result.toString()), null);
    }
  }

  /*
   * (non-Javadoc)
   * @see devplugin.Plugin#getMarkIconFromTheme()
   */
  @Override
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("actions", "edit-paste", 16);
  }
  
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // write version
    
    if(mConfigs != null) {
      ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();
      
      for(AbstractPluginProgramFormating config : mConfigs)
        if(config != null)
          list.add(config);
      
      out.writeInt(list.size());
      
      for(AbstractPluginProgramFormating config : list)
        config.writeData(out);
    }
    else
      out.writeInt(0);
    
    if(mLocalFormatings != null) {
      ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();
      
      for(AbstractPluginProgramFormating config : mLocalFormatings)
        if(config != null)
          list.add(config);
      
      out.writeInt(list.size());
      
      for(AbstractPluginProgramFormating config : list)
        config.writeData(out);      
    }
    
  }
  
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    try {
      in.readInt();
    
      int n = in.readInt();
    
      ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();
        
      for(int i = 0; i < n; i++) {
        AbstractPluginProgramFormating value = AbstractPluginProgramFormating.readData(in);
      
        if(value != null) { 
          if(value.equals(DEFAULT_CONFIG))
            DEFAULT_CONFIG = (LocalPluginProgramFormating)value;
        
          list.add(value);
        }
      }
    
      mConfigs = list.toArray(new AbstractPluginProgramFormating[list.size()]);
    
      mLocalFormatings = new LocalPluginProgramFormating[in.readInt()];
    
      for(int i = 0; i < mLocalFormatings.length; i++) {
        LocalPluginProgramFormating value = (LocalPluginProgramFormating)LocalPluginProgramFormating.readData(in);
        LocalPluginProgramFormating loadedInstance = getInstanceOfFormatingFromSelected(value);
      
        mLocalFormatings[i] = loadedInstance == null ? value : loadedInstance;
      }
    }catch(Exception e) {}
  }
  
  private LocalPluginProgramFormating getInstanceOfFormatingFromSelected(LocalPluginProgramFormating value) {
    for(AbstractPluginProgramFormating config : mConfigs)
      if(config.equals(value))
        return (LocalPluginProgramFormating)config;
    
    return null;
  }
  
  protected static LocalPluginProgramFormating getDefaultFormating() {    
    return new LocalPluginProgramFormating(mLocalizer.msg("defaultName","ClipboardPlugin - Default"),"{title}","{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n","UTF-8");
  }

  protected LocalPluginProgramFormating[] getAvailableLocalPluginProgramFormatings() {
    return mLocalFormatings;
  }
  
  protected void setAvailableLocalPluginProgramFormatings(LocalPluginProgramFormating[] value) {
    if(value == null || value.length < 1)
      createDefaultAvailable();
    else
      mLocalFormatings = value;
  }

  protected AbstractPluginProgramFormating[] getSelectedPluginProgramFormatings() {
    return mConfigs;
  }
  
  protected void setSelectedPluginProgramFormatings(AbstractPluginProgramFormating[] value) {
    if(value == null || value.length < 1)
      createDefaultConfig();
    else
      mConfigs = value;
  }
}