package fixprogramduration;

import java.util.Iterator;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramInfoHelper;
import devplugin.SettingsTab;
import devplugin.Version;

public class FixProgramDuration extends Plugin {
  private static final Version VERSION = new Version(0, 10, true);
  private static final String ACCEPTABLE_TIME_KEY = "ACCEPTABLE_TIME_KEY";
  private static final String NOT_FOR_SPORT_KEY = "NOT_FOR_SPORT_KEY";
  private Properties mProperties;
  
  @Override
  public void onActivation() {
    if(mProperties == null) {
      mProperties = new Properties();
    }
  }
  
  public static Version getVersion() {
    return VERSION;
  }
  
  @Override
  public PluginInfo getInfo() {
    return new PluginInfo(FixProgramDuration.class, "FixProgramDuration", "Fixes duration of too long programs.", "René Mach");
  }
  
  @Override
  public void handleTvDataAdded(MutableChannelDayProgram newProg) {
    Iterator<Program> programs = newProg.getPrograms();
    
    Program compare = null;
    
    if(programs.hasNext()) {
      compare = programs.next();
    }
    
    while(programs.hasNext()) {
      Program next = programs.next();
      
      if(compare != null && isNotSportProgram(compare) && compare.getLength() >= getAcceptableDuration() && (compare.getStartTime()+compare.getLength() > next.getStartTime())) {
        int nettoTime = compare.getIntField(ProgramFieldType.NET_PLAYING_TIME_TYPE);
        
        if(nettoTime > 0) {
          if((compare.getStartTime() + nettoTime) <= next.getStartTime()) {
            ((MutableProgram)compare).setLength(next.getStartTime()-compare.getStartTime());
          }
          else {
            ((MutableProgram)compare).setLength(compare.getStartTime()+nettoTime);
          }
        }
        else {
          ((MutableProgram)compare).setLength(next.getStartTime()-compare.getStartTime());
        }
      }
      
      compare = next;
      
      if(!programs.hasNext() && isNotSportProgram(next) && (next.getLength() >= getAcceptableDuration())) {
        int nettoTime = compare.getIntField(ProgramFieldType.NET_PLAYING_TIME_TYPE);
        
        if(nettoTime > 0) {
          ((MutableProgram)next).setLength(nettoTime + nettoTime/3);
        }
        else {
          ((MutableProgram)next).setLength(getAcceptableDuration());
        }
      }
    }
  }
  
  private boolean isNotSportProgram(Program p) {
    return !getNotForSports() || p.getInfo() == -1 || !ProgramInfoHelper.bitSet(p.getInfo(), Program.INFO_CATEGORIE_SPORTS);
  }
  
  private int getAcceptableDuration() {
    return Integer.parseInt(mProperties.getProperty(ACCEPTABLE_TIME_KEY, "240"));
  }
  
  private boolean getNotForSports() {
    return Boolean.parseBoolean(mProperties.getProperty(NOT_FOR_SPORT_KEY, "true"));
  }
  
  @Override
  public void loadSettings(Properties settings) {
    if(settings != null) {
      mProperties = settings;
    }
  }
  
  @Override
  public Properties storeSettings() {
    return mProperties;
  }
  
  @Override
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private JSpinner mTimeSelection;
      private JCheckBox mNotForSports;
      
      @Override
      public void saveSettings() {
        mProperties.setProperty(ACCEPTABLE_TIME_KEY, String.valueOf(mTimeSelection.getValue()));
        mProperties.setProperty(NOT_FOR_SPORT_KEY, String.valueOf(mNotForSports.isSelected()));
      }
      
      @Override
      public String getTitle() {
        return getInfo().getName();
      }
      
      @Override
      public Icon getIcon() {
        return null;
      }
      
      @Override
      public JPanel createSettingsPanel() {
        mTimeSelection = new JSpinner(new SpinnerNumberModel(getAcceptableDuration(), 120, 360, 30));
        mNotForSports = new JCheckBox("Don't fix long sport type programs",getNotForSports());
        
        JPanel settings = new JPanel(new FormLayout("5dlu,default,3dlu,default,3dlu,default","5dlu,default,2dlu,default"));
        
        settings.add(new JLabel("Check programs for wrong duration with duration of at least"), CC.xy(2, 2));
        settings.add(mTimeSelection, CC.xy(4, 2));
        settings.add(new JLabel("minutes."), CC.xy(6, 2));
        settings.add(mNotForSports, CC.xyw(2, 4, 5));
        
        return settings;
      }
    };
  }
}
