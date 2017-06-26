package switchplugin;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import compat.PluginCompat;
import util.paramhandler.ParamParser;
import util.ui.Localizer;
import util.ui.UiUtilities;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * A Plugin to start an external Application, without any marking in the
 * ProgramTable.
 * 
 * @author René Mach
 * 
 */
public class SwitchPlugin extends Plugin {
  static Logger mLog = Logger.getLogger(SwitchPlugin.class.getName());
  protected static Localizer mLocalizer = Localizer
      .getLocalizerFor(SwitchPlugin.class);

  private static SwitchPlugin instance;
  private Properties mProp;
  private Hashtable mChannels = new Hashtable();
  private static Version mVersion = new Version(0,59,0);
  private ProgramReceiveTarget[] mReceiveTarget;
  
  public static Version getVersion() {
    return mVersion;
  }
  
  /** @return The Plugin Info. */
  public PluginInfo getInfo() {
    return (new PluginInfo(SwitchPlugin.class,"SwitchPlugin", mLocalizer.msg("description",
        "Runs external application on clicking"), "Ren\u00e9 Mach"));
  }

  /** Constructor */
  public SwitchPlugin() {
    instance = this;
    mProp = new Properties();
  }

  public Properties storeSettings() {
    return mProp;
  }

  public void loadSettings(Properties settings) {
    if (settings == null)
      mProp = new Properties();
    else
      mProp = settings;

    loadChannels();
  }

  /**
   * @return The MarkIcon.
   */
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("devices", "video-display", 16);
  }

  public ActionMenu getContextMenuActions(final Program p) {
    if (p == null || (!p.equals(getPluginManager().getExampleProgram()) && mProp.getProperty("app", "").trim().length() < 1)) {
      return null;
    }

    String text = mProp.getProperty("context", mLocalizer.msg("run", "Switch"));

    ContextMenuAction menu = new ContextMenuAction(text);
    menu.setSmallIcon(createImageIcon("devices","video-display",16));

    menu.setActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
         new Thread() {
           public void run() {
             setPriority(Thread.MIN_PRIORITY);
             actionFor(p, false);
           }
         }.start();
      }
    });

    return new ActionMenu(menu);
  }

  private void actionFor(Program p, boolean isSend) {
    ParamParser parser = new ParamParser(new SwitchParamLibrary());

    String params = parser.analyse(mProp.getProperty("para", ""), p);

    if (parser.hasErrors()) {
      JOptionPane
          .showMessageDialog(
              UiUtilities.getBestDialogParent(getParentFrame()),
              mLocalizer
                  .msg("emsg",
                      "Configuration not complete.\nPlease configure the plugin complete."),
              "SwitchPlugin " + mLocalizer.msg("error", "Error"),
              JOptionPane.ERROR_MESSAGE);
      return;
    }

    boolean ask = mProp.getProperty("ask", "true").equals("true");

    if (ask) {
      int i = -1;

      if (!isSend)
        i = JOptionPane.showConfirmDialog(UiUtilities
            .getBestDialogParent(getParentFrame()), getProperty("question",
            mLocalizer.msg("qmsg",
                "Should the external application be executed now?")),
            "SwitchPlugin", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE, p.getChannel().getIcon());
      else {
        String[] msg = {
            p.getChannel().getDefaultName() + " " + p.getTimeString(),
            p.getTitle(),
            " ",
            getProperty("question", mLocalizer.msg("qmsg",
                "Should the external application be executed now?")) };

        JOptionPane pane = new JOptionPane();
        pane.setMessageType(JOptionPane.QUESTION_MESSAGE);
        pane.setMessage(msg);
        pane.setOptionType(JOptionPane.YES_NO_OPTION);
        pane.setIcon(p.getChannel().getIcon());

        JDialog d = pane.createDialog(getParentFrame(), "SwitchPlugin");

        try {
          Class[] c = { boolean.class };

          Method alwaysOnTop = d.getClass().getMethod("setAlwaysOnTop", c);
          Boolean[] b = { Boolean.TRUE };
          alwaysOnTop.invoke(d, b);
        } catch (Throwable e) {}

        d.setVisible(true);
        
        try {
          i = ((Integer) pane.getValue()).intValue();
        }catch(Exception e) {
          i = 1;
        }
      }
      if (i == 0)
        execute(params);
    } else
      execute(params);
  }

  private static String[] getSplittedCmdLine(String appPath, String args) {
    ArrayList<String> splitted = new ArrayList<String>();
    
    splitted.add(appPath.replace("\"+", ""));
    
    String[] parts = args.split(" +");
    
    for(int i = 0; i < parts.length; i++) {
  	  if(!containsEvenNumberOfQuotes(parts[i])) {
		String newPart = parts[i];
		
		int j = i;
		
		do {
		  j++;
			
		  newPart += " " + parts[j]; 
		}while(!parts[j].endsWith("\"") && j < parts.length-1);
		
		i = j;
		
		if(!newPart.endsWith("\"")) {
		  newPart += "\"";
		}
		
		splitted.add(newPart);
	  }
	  else {
		splitted.add(parts[i]);
	  }
    }
    
    return splitted.toArray(new String[splitted.size()]);
  }
  
  private static boolean containsEvenNumberOfQuotes(String value) {
	int quoteCount = 0;
	
    for(int i = 0; i < value.length(); i++) {
      if(value.charAt(i) == '\"') {
        quoteCount++;
      }
    }
    
    return quoteCount % 2 == 0;
  }
  
  private void execute(final String para) {
    String temp = mProp.getProperty("app", "").trim();

    if (temp.length() > 0) {
      File path = new File(temp.substring(0, temp.lastIndexOf(File.separator) + 1));

      if(path == null || !path.isDirectory())
        path = new File(System.getProperty("user.dir"));
      
      String[] pro = getSplittedCmdLine(temp, para);
      
      try {
        Process p = Runtime.getRuntime().exec(pro, null, path);
        new ProcessStreamReader(p.getErrorStream());
        new ProcessStreamReader(p.getInputStream());
      } catch (Exception e) {
    	mLog.log(Level.SEVERE, "SwitchPlugin: Could not execute '" + temp + " " + para + "'.", e);
      }
    }
  }

  private void loadChannels() {
    mChannels.clear();
    String value = mProp.getProperty("channels", "");

    Channel[] ch = getPluginManager().getSubscribedChannels();

    if (value.trim().length() > 0) {
      String[] entries = value.split(";");

      int i = 0;
      while (i < entries.length) {
        mChannels.put(entries[i], entries[i + 1].compareTo("null") == 0 ? ""
            : entries[i + 1]);
        i += 2;
      }

    } else
      for (int i = 0; i < ch.length; i++)
        mChannels.put(ch[i].toString(), "");
  }

  public SettingsTab getSettingsTab() {
    return new SwitchPluginSettingsTab();
  }

  protected static SwitchPlugin getInstance() {
    return instance;
  }

  protected String getExternalNameFor(String channel) {
    String ch = (String) mChannels.get(channel);
    return (ch != null && ch.trim().length() > 0) ? ch : null;
  }

  protected Frame getSuperFrame() {
    return getParentFrame();
  }

  protected Object[][] getTableEntries() {
    Channel[] ch = getPluginManager().getSubscribedChannels();
    Object[][] channelTableValues = new Object[ch.length][2];

    for (int i = 0; i < ch.length; i++) {
      System.out.println(ch[i].toString());
      
      String entry = (String) mChannels.get(ch[i].getDefaultName());

      channelTableValues[i][0] = ch[i];
      channelTableValues[i][1] = entry;
    }

    return channelTableValues;
  }

  protected String getProperty(String key, String def) {
    return mProp.getProperty(key, def);
  }

  protected void setProperty(String key, String value) {
    mProp.setProperty(key, value);

    if (key.compareToIgnoreCase("channels") == 0)
      loadChannels();
  }

  public boolean canReceiveProgramsWithTarget() {
    return true;
  }
  
  /**
   * Old method keep for compatibility. 
   * <p>
   * @return If the plugin can receive programs.
   */
  public boolean canReceivePrograms() {
    return canReceiveProgramsWithTarget();
  }
  
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    if(mReceiveTarget == null) {
      mReceiveTarget = new ProgramReceiveTarget[1];
      mReceiveTarget[0] = new ProgramReceiveTarget(this, mProp.getProperty("context", mLocalizer.msg("run", "Switch")), "switchPlugin");
    }
    
    return mReceiveTarget;
  }
  
  public boolean receivePrograms(Program[] p, ProgramReceiveTarget receiveTarget) {
    receivePrograms(p);
    return true;
  }

  public void receivePrograms(Program[] p) {
    for (int i = 0; i < p.length; i++)
      actionFor(p[i], true);
  }
  
  public String getPluginCategory() {
    return PluginCompat.CATEGORY_REMOTE_CONTROL_SOFTWARE;
  }
}
