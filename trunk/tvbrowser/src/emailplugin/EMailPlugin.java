/*
 * EMailPlugin by Bodo Tasche
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package emailplugin;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;

import util.program.AbstractPluginProgramFormating;
import util.program.LocalPluginProgramFormating;
import util.ui.Localizer;
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
 * This Plugin makes it possible to send an email with short info's about the
 * Program
 * 
 * @author bodum
 */
public class EMailPlugin extends Plugin {
  private static final Version mVersion = new Version(3,0);
  
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(EMailPlugin.class);

  /** Properties */
  private EMailSettings mSettings;
  
  /** The Default-Parameters */
  private static LocalPluginProgramFormating DEFAULT_CONFIG = new LocalPluginProgramFormating("emailDefault", mLocalizer.msg("defaultName","EmailPlugin - Default"),"{title}","{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n","UTF-8");
  
  private AbstractPluginProgramFormating[] mConfigs = null;
  private LocalPluginProgramFormating[] mLocalFormattings = null;
  
  private PluginInfo mPluginInfo;
  
  /**
   * Creates an instance of this class.
   */
  public EMailPlugin() {
    createDefaultConfig();
    createDefaultAvailable();
  }

  private void createDefaultConfig() {
    mConfigs = new AbstractPluginProgramFormating[1];
    mConfigs[0] = DEFAULT_CONFIG;
  }

  private void createDefaultAvailable() {
    mLocalFormattings = new LocalPluginProgramFormating[1];
    mLocalFormattings[0] = DEFAULT_CONFIG;
  }
  
  public static Version getVersion() {
    return mVersion;
  }
  
  public PluginInfo getInfo() {
    if(mPluginInfo == null) {
      String name = mLocalizer.msg("pluginName", "Email");
      String desc = mLocalizer.msg("description", "Send email with an external program");
      String author = "Bodo Tasche";
      
      mPluginInfo = new PluginInfo(EMailPlugin.class, name, desc, author);
    }
    
    return mPluginInfo;
  }

  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("actions", "mail-message-new", 16);
  }
  
  public ActionMenu getContextMenuActions(final Program program) {
    if(mConfigs.length > 1) {
      ContextMenuAction copyToSystem = new ContextMenuAction(mLocalizer.ellipsisMsg("contextMenuText", "Send via EMail"));
      
      ArrayList<AbstractAction>list = new ArrayList<AbstractAction>();
    
      for (final AbstractPluginProgramFormating config : mConfigs) {
        if(config != null && config.isValid()) {
          list.add(new AbstractAction(config.getName()) {
            public void actionPerformed(ActionEvent e) {
          
              Program[] programArr = { program };
              new MailCreator(EMailPlugin.this, mSettings, config).createMail(getParentFrame(), programArr);
            }
          });
        }
        }
      
      copyToSystem.putValue(Action.SMALL_ICON, createImageIcon("actions", "mail-message-new", 16));

      return new ActionMenu(copyToSystem, list.toArray(new AbstractAction[list.size()]));
    }
    else {
      AbstractAction copyToSystem = new AbstractAction(mLocalizer.msg("contextMenuText", "Send via EMail")) {
        public void actionPerformed(ActionEvent evt) {
          Program[] programArr = { program };
          new MailCreator(EMailPlugin.this, mSettings, mConfigs.length != 1 ? DEFAULT_CONFIG : mConfigs[0]).createMail(getParentFrame(), programArr);
        }
      };
      
      copyToSystem.putValue(Action.NAME, mLocalizer.msg("contextMenuText", "Send via EMail"));
      copyToSystem.putValue(Action.SMALL_ICON, createImageIcon("actions", "mail-message-new", 16));
      
      return new ActionMenu(copyToSystem);
    }
  }

  public boolean canReceiveProgramsWithTarget() {
    return true;
  }
  
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>();
    
    for(AbstractPluginProgramFormating config : mConfigs) {
      if(config != null && config.isValid()) {
        list.add(new ProgramReceiveTarget(this, config.getName(), config.getId()));
      }
    }
    
    if(list.isEmpty()) {
      list.add(new ProgramReceiveTarget(this, DEFAULT_CONFIG.getName(), DEFAULT_CONFIG.getId()));
    }
    
    return list.toArray(new ProgramReceiveTarget[list.size()]);
  }

  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget target) {
    AbstractPluginProgramFormating formating = null;
    
    if(target == null) {
      return false;
    }
    
    if(target.isReceiveTargetWithIdOfProgramReceiveIf(this,DEFAULT_CONFIG.getId())) {
      formating = DEFAULT_CONFIG;
    } else {
      for(AbstractPluginProgramFormating config : mConfigs) {
        if(target.isReceiveTargetWithIdOfProgramReceiveIf(this,config.getId())) {
          formating = config;
          break;
        }
      }
    }
    
    if(formating != null) {
      new MailCreator(this, mSettings, formating).createMail(getParentFrame(), programArr);
      return true;
    }
    
    return false;
  }

  public SettingsTab getSettingsTab() {
    return new EMailSettingsTab(this, mSettings);
  }

  public void loadSettings(final Properties properties) {
    mSettings = new EMailSettings(properties);
/*
    if(settings != null && settings.containsKey("ParamToUse")) {
      mConfigs = new AbstractPluginProgramFormating[1];
      mConfigs[0] = new LocalPluginProgramFormating(mLocalizer.msg("defaultName","Send e-mail - Default"),"{title}",settings.getProperty("ParamToUse"),settings.getProperty("encoding", "UTF-8"));
      mLocalFormattings = new LocalPluginProgramFormating[1];
      mLocalFormattings[0] = (LocalPluginProgramFormating)mConfigs[0];
      DEFAULT_CONFIG = mLocalFormattings[0];
      
      settings.remove("ParamToUse");
      settings.remove("encoding");
    }
*/
  }

  public Properties storeSettings() {
    return mSettings.storeSettings();
  }
  
  protected static LocalPluginProgramFormating getDefaultFormatting() {
    return new LocalPluginProgramFormating(mLocalizer.msg("defaultName","EmailPlugin - Default"),"{title}","{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n","UTF-8");
  }

  protected LocalPluginProgramFormating[] getAvailableLocalPluginProgramFormattings() {
    return mLocalFormattings;
  }
  
  protected void setAvailableLocalPluginProgramFormattings(LocalPluginProgramFormating[] value) {
    if(value == null || value.length < 1) {
      createDefaultAvailable();
    } else {
      mLocalFormattings = value;
    }
  }

  protected AbstractPluginProgramFormating[] getSelectedPluginProgramFormattings() {
    return mConfigs;
  }
  
  protected void setSelectedPluginProgramFormattings(AbstractPluginProgramFormating[] value) {
    if(value == null || value.length < 1) {
      createDefaultConfig();
    } else {
      mConfigs = value;
    }
  }
  
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // write version
    
    if(mConfigs != null) {
      ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();
      
      for(AbstractPluginProgramFormating config : mConfigs) {
        if(config != null) {
          list.add(config);
        }
      }
      
      out.writeInt(list.size());
      
      for(AbstractPluginProgramFormating config : list) {
        config.writeData(out);
      }
    } else {
      out.writeInt(0);
    }
    
    if(mLocalFormattings != null) {
      ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();
      
      for(AbstractPluginProgramFormating config : mLocalFormattings) {
        if(config != null) {
          list.add(config);
        }
      }
      
      out.writeInt(list.size());
      
      for(AbstractPluginProgramFormating config : list) {
        config.writeData(out);
      }
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
          if(value.equals(DEFAULT_CONFIG)) {
            DEFAULT_CONFIG = (LocalPluginProgramFormating)value;
          }
        
          list.add(value);
        }
      }
    
      mConfigs = list.toArray(new AbstractPluginProgramFormating[list.size()]);
    
      mLocalFormattings = new LocalPluginProgramFormating[in.readInt()];
    
      for(int i = 0; i < mLocalFormattings.length; i++) {
        LocalPluginProgramFormating value = (LocalPluginProgramFormating)AbstractPluginProgramFormating.readData(in);
        LocalPluginProgramFormating loadedInstance = getInstanceOfFormatingFromSelected(value);
      
        mLocalFormattings[i] = loadedInstance == null ? value : loadedInstance;
      }
    }catch(Exception e) {}
  }
  
  private LocalPluginProgramFormating getInstanceOfFormatingFromSelected(LocalPluginProgramFormating value) {
    for(AbstractPluginProgramFormating config : mConfigs) {
      if(config.equals(value)) {
        return (LocalPluginProgramFormating)config;
      }
    }
    
    return null;
  }
}