package personatimeswitcher;

import java.util.ArrayList;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import util.ui.Localizer;
import util.ui.persona.Persona;
import util.ui.persona.PersonaInfo;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;

public class PersonaTimeSwitcher extends Plugin {
  private final static Localizer mLocalizer = Localizer.getLocalizerFor(PersonaTimeSwitcher.class);
  private static Version mVersion = new Version(0,2,0,true);
  private PluginInfo mPluginInfo;
  private int mTimeRange;
  private Thread mPersonaChangeThread;
  private boolean mThreadIsRunning;
  private boolean mTvBrowserStartFinished;
  
  public static Version getVersion() {
    return mVersion;
  }
  
  public void loadSettings(Properties settings) {
    mTimeRange = Integer.parseInt(settings.getProperty("timeRange", "60"));
  }
  
  public Properties storeSettings() {
    Properties prop = new Properties();
    prop.setProperty("timeRange", String.valueOf(mTimeRange));
    return prop;
  }
  
  public void handleTvBrowserStartFinished() {
    mTvBrowserStartFinished = true;
  }
  
  public void onActivation() {
    mThreadIsRunning = true;
    mPersonaChangeThread = new Thread() {
      public void run() {
        while(mThreadIsRunning) {
          try {
            if(!Persona.getInstance().getId().equals("51b73c81-7d61-4626-b230-89627c9f5ce7")) {
              PersonaInfo[] infos = Persona.getInstance().getInstalledPersonas();
              ArrayList<PersonaInfo> infoList = new ArrayList<PersonaInfo>();
              
              for(PersonaInfo info : infos) {
                if(!info.isSelectedPersona() && !PersonaInfo.isRandomPersona(info) && !info.getId().equals("51b73c81-7d61-4626-b230-89627c9f5ce7")) {
                  infoList.add(info);
                }
              }
              
              if(infoList.size() > 1) {
                int n = 0;
                
                do {
                  n = (int)(Math.random() * infoList.size());
                }while(infoList.get(n).isSelectedPersona());
                
                while(!mTvBrowserStartFinished) {
                  sleep(100);
                }

                Persona.getInstance().activatePersona(infoList.get(n));
              }
            }
            
            sleep(mTimeRange * 1000);
          } catch (InterruptedException e) {
            // ignore
          }
        }
      }
    };
    
    mPersonaChangeThread.start();
  }
  
  public void onDeactivation() {
    mThreadIsRunning = false;
    
    if(mPersonaChangeThread != null && mPersonaChangeThread.isAlive()) {
      mPersonaChangeThread.interrupt();
    }
  }
  
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private JSpinner mTimeSpinner;
      
      public void saveSettings() {
        mTimeRange = ((Integer)mTimeSpinner.getValue()) * 60;
      }
      
      @Override
      public String getTitle() {
        return "PersonaTimeSwitcher";
      }
      
      @Override
      public Icon getIcon() {
        return createImageIcon("apps", "preferences-desktop-theme", 16);
      }
      
      @Override
      public JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new FormLayout("5dlu,default,4dlu,default,5dlu,default","5dlu,default"));
        mTimeSpinner = new JSpinner(new SpinnerNumberModel(Math.max(1,Math.min(mTimeRange/60, 180)), 1, 180, 1));
        CellConstraints cc = new CellConstraints();
        
        panel.add(new JLabel(mLocalizer.msg("settings.1", "Switch Persona every")), cc.xy(2,2));
        panel.add(mTimeSpinner, cc.xy(4,2));
        panel.add(new JLabel(mLocalizer.msg("settings.2", "minutes")), cc.xy(6,2));
        
        return panel;
      }
    };
  }
  
  public PluginInfo getInfo() {
    return mPluginInfo;
  }
  
  public PersonaTimeSwitcher() {
    mTvBrowserStartFinished = false;
    mTimeRange = 60;
    mPluginInfo = new PluginInfo(PersonaTimeSwitcher.class,"PersonaTimeSwitcher",mLocalizer.msg("description","Switches Persona between installed Personas on a regular time range."),"Ren\u00e9 Mach","GPL");
  }
}
