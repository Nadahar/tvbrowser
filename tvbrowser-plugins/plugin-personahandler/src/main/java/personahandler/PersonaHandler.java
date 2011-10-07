package personahandler;

import java.awt.Color;
import java.awt.Frame;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.apache.commons.lang.StringEscapeUtils;

import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.persona.Persona;
import util.ui.persona.PersonaInfo;
import util.ui.persona.PersonaListener;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;

public class PersonaHandler extends Plugin implements PersonaListener {
  private final static Localizer mLocalizer = Localizer.getLocalizerFor(PersonaHandler.class);
  private static Version mVersion = new Version(0,11,0,true);
  private PluginInfo mPluginInfo;
  
  private static PersonaHandler mInstance;
  private PersonaDialog mPersonaDialog;
  
  public PersonaHandler() {
    mInstance = this;
    mPluginInfo = new PluginInfo(PersonaHandler.class,"PersonaHandler",mLocalizer.msg("description","Let's you install, create, delete and edit Personas for TV-Browser in an easy way."),"Ren\u00e9 Mach","GPL",mLocalizer.msg("website","http://enwiki.tvbrowser.org/index.php/PersonaHandler"));
    Persona.getInstance().registerPersonaListener(this);
  }
  
  static PersonaHandler getInstance() {
    return mInstance;
  }
  
  public static Version getVersion() {
    return mVersion;
  }
  
  public PluginInfo getInfo() {
    return mPluginInfo;
  }
  
  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        showPersonaDialog();
      }
    };

    action.putValue(Action.NAME, mLocalizer.msg("buttonActionText","Manage Personas"));
    action.putValue(Action.SMALL_ICON, createImageIcon("apps", "preferences-desktop-theme", 16));
    action.putValue(BIG_ICON, createImageIcon("apps", "preferences-desktop-theme", 22));
    action.putValue(Action.SHORT_DESCRIPTION, getInfo().getDescription());

    return new ActionMenu(action);
  }
  
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      public void saveSettings() {
        //ignore
      }
      
      @Override
      public String getTitle() {
        return "PersonaHandler";
      }
      
      @Override
      public Icon getIcon() {
        return createImageIcon("apps", "preferences-desktop-theme", 16);
      }
      
      @Override
      public JPanel createSettingsPanel() {
        return new PersonaHandlerPanel(false);
      }
    };
  }
  
  private void showPersonaDialog() {
    mPersonaDialog = new PersonaDialog(UiUtilities.getLastModalChildOf(getParentFrame()));
    mPersonaDialog.setVisible(true);
  }
  
  boolean deletePersona(PersonaInfo info) {
    if(Persona.getInstance().removePersona(info)) {
      File dir = new File(Persona.getUserPersonaDir(),info.getId());
      File[] files = dir.listFiles();
      
      for(File file : files) {
        if(!file.delete()) {
          file.deleteOnExit();
        }
      }
      
      if(!dir.delete()) {
        dir.deleteOnExit();
      }
      
      return true;
    }
    
    return false;
  }
  
  PersonaInfo addNewPersona() {
    File dir = new File(Persona.getUserPersonaDir(),UUID.randomUUID().toString());
    
    if(dir.mkdirs()) {
      Properties prop = new Properties();
      prop.setProperty(Persona.NAME_KEY, mLocalizer.msg("newPersona","New Persona") + ((int)(Math.random() * 5000)));
      prop.setProperty(Persona.DESCRIPTION_KEY, mLocalizer.msg("newPersona.description","New created persona"));
      prop.setProperty(Persona.HEADER_IMAGE_KEY, Persona.USER_PERSONA + "/header.jpg");
      prop.setProperty(Persona.FOOTER_IMAGE_KEY, Persona.USER_PERSONA + "/footer.jpg");
      prop.setProperty(Persona.TEXT_COLOR_KEY, "0,0,0");
      prop.setProperty(Persona.SHADOW_COLOR_KEY, "0,0,0");
      prop.setProperty(Persona.ACCENT_COLOR_KEY, "255,255,255");
      prop.setProperty(Persona.DETAIL_URL_KEY, "http://www.tvbrowser.org/");
      
      File propFile = new File(dir,"persona.prop");
      
      try {
        FileOutputStream out = new FileOutputStream(propFile);
        prop.store(out,"");
        out.close();
        
        Persona.getInstance().loadPersonas();
        
        return Persona.getInstance().getPersonaInfo(dir.getName());
      }catch(Exception e) {}
    }
    
    return null;
  }
  
  PersonaInfo getAndInstallPersona(String url) {
try{
    StringBuilder string = new StringBuilder();
    
    String url1 = url.replaceAll(" ","%20");
    try {
      BufferedInputStream in = new BufferedInputStream(IOUtilities.getStream(new URL(url1)));
      
      
      
      int len;
      byte[] buffer = new byte[10240];
      while ((len = (in.read(buffer))) != -1) {
        String buf = new String(buffer,0,len);
        string.append(buf);
      }
      
      
      
      string.trimToSize();
      
      in.close();
      

      
    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      
    }
    
    Pattern p = Pattern.compile("<a href=.*?persona=\"([^\"]*)\">");
    Matcher m = p.matcher(string.toString());

    int lastPos = 0;
    
    String id = "id";
    String name = "name";
    String description = "description";
    String headerURL = "headerURL";
    String footerURL = "footerURL";
    String accentcolor = "accentcolor";
    String textcolor = "textcolor";
    String author = "author";
    String detailURL = "detailURL";
    String iconURL = "iconURL";
    
    while(m.find(lastPos)) {
      String persona = StringEscapeUtils.unescapeJava(m.group(1).replace("{","").replace("}",""));
      StringBuffer buff = new StringBuffer(persona.replace("&quot;","\"").replace("&amp;","&").replace("&lt;","<").replace("&gt;",">"));
      
      boolean openQuote = false; 

      for(int i = 0; i < buff.length(); i++) {
        Character c = buff.charAt(i);
        
        if(c == '\"') {
          openQuote = !openQuote;
        }
        else if(openQuote && c == ',') {
          buff.setCharAt(i,'$');
        }
      }
      
      String[] parts = buff.toString().replace("\"","").split(",");
      
      for(String part : parts) {
        int index = part.indexOf(":");
        
        if(index != -1) {
          String key = part.substring(0,index);
          String value = part.substring(index+1);
          
          if(key.equals(id)) {
            id = value;
          }
          else if(key.equals(name)) {
            name = value;
          }
          else if(key.equals(description)) {
            description = value;
          }
          else if(key.equals(headerURL)) {
            headerURL = value;
          }
          else if(key.equals(footerURL)) {
            footerURL = value;
          }
          else if(key.equals(accentcolor)) {
            accentcolor = value;
          }
          else if(key.equals(textcolor)) {
            textcolor = value;
          }
          else if(key.equals(author)) {
            author = value;
          }
          else if(key.equals(detailURL)) {
            detailURL = value;
          }
          else if(key.equals(iconURL)) {
            iconURL = value;
          }
        }
      }
      break;
    }
    
    File versionDir = new File(Persona.getUserPersonaDir(),id);
    
    if(!versionDir.isDirectory()) {
      if(versionDir.mkdirs()) {
        Properties prop = new Properties();
        
        File headerImage = new File(versionDir,"header.jpg");        
        File footerImage = new File(versionDir,"footer.jpg");
        File iconImage = new File(versionDir,"icon");
        
        try {
          IOUtilities.download(new URL(headerURL),headerImage);
          IOUtilities.download(new URL(footerURL),footerImage);
          IOUtilities.download(new URL(iconURL),iconImage);
        } catch (MalformedURLException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        } catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        
        prop.setProperty(Persona.NAME_KEY, name.replace("$",",") + " by " + author);
        prop.setProperty(Persona.DESCRIPTION_KEY, description.replace("$",",").replace("null",""));
        prop.setProperty(Persona.HEADER_IMAGE_KEY, Persona.USER_PERSONA + "/" + headerImage.getName());
        prop.setProperty(Persona.FOOTER_IMAGE_KEY, Persona.USER_PERSONA + "/" + footerImage.getName());
        prop.setProperty(Persona.DETAIL_URL_KEY, detailURL);
        
        Color textColor = UIManager.getColor("Menu.foreground");
        
        if(!textcolor.equals("null")) {
          textColor = new Color(Integer.parseInt(textcolor.substring(1,3),16),Integer.parseInt(textcolor.substring(3,5),16),Integer.parseInt(textcolor.substring(5,7),16));          
        }
        
        prop.setProperty(Persona.TEXT_COLOR_KEY, textColor.getRed() + "," + textColor.getGreen() + "," + textColor.getBlue());
                      
        double test = (0.2126 * textColor.getRed()) + (0.7152 * textColor.getGreen()) + (0.0722 * textColor.getBlue()); 
        
        Color accentColor = null;
        
        if(!accentcolor.equals("null")) {
          accentColor = new Color(Integer.parseInt(accentcolor.substring(1,3),16),Integer.parseInt(accentcolor.substring(3,5),16),Integer.parseInt(accentcolor.substring(5,7),16));
        }
        else if(test <= 127) {
          accentColor = textColor.brighter().brighter().brighter();
        }
        else if(test > 127) {
          accentColor = textColor.darker().darker().darker();
        }
        
        prop.setProperty(Persona.ACCENT_COLOR_KEY, accentColor.getRed() + "," + accentColor.getGreen() + "," + accentColor.getBlue());
        
        Color shadowColor = null;
        
        if(test <= 30) {
          shadowColor = textColor;
        }
        else if(test <= 40) {
          shadowColor = textColor.brighter().brighter().brighter().brighter().brighter().brighter();
        }
        else if(test <= 60) {
          shadowColor = textColor.brighter().brighter().brighter();
        }
        else if(test <= 100) {
          shadowColor = textColor.brighter().brighter();
        }
        else if(test <= 145) {
          shadowColor = textColor;
        }
        else if(test <= 170) {
          shadowColor = textColor.darker();
        }
        else if(test <= 205) {
          shadowColor = textColor.darker().darker();
        }
        else if(test <= 220){
          shadowColor = textColor.darker().darker().darker();
        }
        else if(test <= 235){
          shadowColor = textColor.darker().darker().darker().darker();
        }
        else {
          shadowColor = textColor.darker().darker().darker().darker().darker();
        }
        
        
        /*if(test <= 50) {
          shadowColor = textColor;
        }
        else if(test <= 100) {
          shadowColor = textColor.brighter().brighter();
        }
        else if(test <= 155) {
          shadowColor = textColor;
        }
        else if(test <= 205) {
          shadowColor = textColor.darker().darker();
        }
        else {
          shadowColor = textColor.darker().darker().darker();
        }*/
        
        prop.setProperty(Persona.SHADOW_COLOR_KEY, shadowColor.getRed() + "," + shadowColor.getGreen() + "," + shadowColor.getBlue());

        try {
          FileOutputStream out = new FileOutputStream(new File(versionDir,"persona.prop"));
          prop.store(out,"");
          out.close();
        } catch (Exception e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        
        Persona.getInstance().loadPersonas();
        
        return Persona.getInstance().getPersonaInfo(id);
      }
    }
  }catch(Throwable t) {t.printStackTrace();}
    
    return null;
  }
  
  public Frame getSuperFrame() {
    return getParentFrame();
  }

  @Override
  public void updatePersona() {
    // TODO Auto-generated method stub
    if(mPersonaDialog != null) {
      mPersonaDialog.updatePersona();
    }
  }
}
