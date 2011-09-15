/*
 * TV-Browser
 * Copyright (C) 2011 TV-Browser team (dev@tvbrowser.org)
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
package util.ui.persona;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;

import javax.swing.ButtonModel;
import javax.swing.JMenu;
import javax.swing.UIManager;

import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;

/**
 * A class to handle Personas for TV-Browser.
 * <p>
 * @author René Mach
 * @since 3.0.3
 */
public class Persona {
  private static Persona mInstance;
  private HashMap<String,PersonaInfo> mPersonaMap;
  private final static String PERSONA_DIR = "personas";
  
  private String mName;
  private String mDescription;
  private BufferedImage mHeaderImage;
  private BufferedImage mFooterImage;
  private Color mTextColor;
  private Color mShadowColor;
  private Color mAccentColor;
  
  /** The name key for the Persona properties */
  public final static String NAME_KEY = "name";
  /** The description key for the Persona properties */
  public final static String DESCRIPTION_KEY = "description";
  /** The header image key for the Persona properties */
  public final static String HEADER_IMAGE_KEY = "headerImage";
  /** The footer image key for the Persona properties */
  public final static String FOOTER_IMAGE_KEY = "footerImage";
  /** The text color key for the Persona properties */
  public final static String TEXT_COLOR_KEY = "textColor";
  /** The shadow color key for the Persona properties */
  public final static String SHADOW_COLOR_KEY = "shadowColor";
  /** The accent color key for the Persona properties */
  public final static String ACCENT_COLOR_KEY = "accentColor";
  
  /** The key for the space holder for images in the user Persona directory */
  public final static String USER_PERSONA = "{user.persona}";
  /** The key for the space holder for images in the global TV-Browser Persona directory */
  public final static String TVB_PERSONA = "{tvb.persona}";
  
  private Persona() {
    mInstance = this;
    mPersonaMap = new HashMap<String,PersonaInfo>(1);
    loadPersonas();
    applyPersona();
  }
  
  /**
   * Applies the current selected Persona.
   */
  public void applyPersona() {
    try {
    PersonaInfo personaInfo = mPersonaMap.get(Settings.propSelectedPersona.getString());
    
    if(personaInfo == null) {
      Settings.propSelectedPersona.setString(new PersonaInfo().getId());
      personaInfo = mPersonaMap.get(Settings.propSelectedPersona.getString());
    }
    
    if(personaInfo != null) {
      mName = personaInfo.getName();
      mDescription = personaInfo.getDescription();
      mHeaderImage = personaInfo.getHeaderImage();
      mFooterImage = personaInfo.getFooterImage();
      mTextColor = personaInfo.getTextColor();
      mShadowColor = personaInfo.getShadowColor();
      mAccentColor = personaInfo.getAccentColor();
    }
    else {
      mName = "Standard";
      mDescription = "Standard";
      mHeaderImage = null;
      mFooterImage = null;
      mTextColor = null;
      mShadowColor = null;
      mAccentColor = null;
    }
    
    MainFrame.getInstance().updatePersona();
    }catch(Throwable t) {t.printStackTrace();}
  }
  
  /**
   * Loads all available Personas.
   */
  public void loadPersonas() {try {
    mPersonaMap.clear();
    
    PersonaInfo defaultInfo = new PersonaInfo();
    mPersonaMap.put(defaultInfo.getId(),defaultInfo);
    
    // load personas in TV-Browser directory
    checkDir(new File(PERSONA_DIR));
    // load personas in user settings directory
    checkDir(new File(Settings.getUserSettingsDirName(),PERSONA_DIR));}catch(Throwable t) {t.printStackTrace();}
  }
  
  private void checkDir(File parentDir) {
    if(parentDir.isDirectory()) {
      File[] dirs = parentDir.listFiles(new FileFilter() {
        public boolean accept(File pathname) {
          return pathname.isDirectory();
        }
      });
      
      for(File dir : dirs) {
        File test = new File(dir.getAbsolutePath(),"persona.prop");
        
        if(test.isFile()) {
          try {
            PersonaInfo info = new PersonaInfo(test);
            mPersonaMap.put(dir.getAbsoluteFile().getName(),info);
          }catch(IndexOutOfBoundsException e) {}
        }
      }
    }
  }
  
  /**
   * Get the instance of this class.
   * <p>
   * @return The instance of this class.
   */
  public static Persona getInstance() {
    if(mInstance == null) {
      new Persona();
    }
    
    return mInstance;
  }
  
  /**
   * Get the image for the header.
   * <p>
   * @return The image for the header or <code>null</code> if there is none.
   */
  public BufferedImage getHeaderImage() {
    return mHeaderImage;
  }
  
  /**
   * Get the image for the footer.
   * <p>
   * @return The image for the footer or <code>null</code> if there is none.
   */
  public BufferedImage getFooterImage() {
    return mFooterImage;
  }
  
  /**
   * Gets the color for the text foreground.
   * <p>
   * @return The color for the text foreground.
   */
  public Color getTextColor() {
    return mTextColor;
  }
  
  /**
   * Gets the color for the text shadow.
   * If equal to text color no shadow is painted.
   * <p>
   * @return The color for the text shadow.
   */
  public Color getShadowColor() {
    return mShadowColor;
  }
  
  /**
   * Gets the accent color.
   * <p>
   * @return The color for the text shadow.
   */
  public Color getAccentColor() {
    return mAccentColor;
  }
  
  /**
   * Get the name of the current Persona.
   * <p>
   * @return The name of the current Persona.
   */
  public String getName() {
    return mName;
  }
  
  /**
   * Get the description of the current Persona.
   * <p>
   * @return The description of the current Persona.
   */
  public String getDescription() {
    return mDescription;
  }
  
  /**
   * Create a menu that uses the Persona for painting.
   * <p>
   * @return A menu that uses the Persona for painting.
   */
  public JMenu createPersonaMenu() {
    JMenu menu = new JMenu() {
      @Override protected void fireStateChanged() {
          ButtonModel m = getModel();
          if(m.isPressed() && m.isArmed()) {
              setOpaque(true);
          }else if(m.isSelected()) {
              setOpaque(true);
          }else if(isRolloverEnabled() && m.isRollover()) {
              setOpaque(true);
          }else{
              setOpaque(false);
          }
          super.fireStateChanged();
      };
      
      protected void paintComponent(Graphics g) {
        if(mHeaderImage != null && mTextColor != null && mShadowColor != null) {
          if(isOpaque()) {
            g.setColor(UIManager.getColor("List.selectionBackground"));
            g.fillRect(0,0,getWidth(),getHeight());
          }
          
          FontMetrics metrics = g.getFontMetrics(getFont());
          int textWidth = metrics.stringWidth(getText());
          
          int x = getWidth()/2-textWidth/2;
          int y = getHeight()-metrics.getDescent()-getInsets().bottom;
                    
          int mnemonicIndex = getText().indexOf(KeyEvent.getKeyText(getMnemonic()));
          String test = getText().substring(0,mnemonicIndex+1);
          
          int mnemonicWidth = metrics.stringWidth(KeyEvent.getKeyText(getMnemonic()));
          int start = metrics.stringWidth(test) - mnemonicWidth;
          
          if(!mShadowColor.equals(mTextColor)) {
            g.setColor(mShadowColor);
            g.drawString(getText(),x+1,y+1);
            g.drawString(getText(),x+2,y+2);
            
            g.drawLine(x + start + 1,y+2,x+start+mnemonicWidth,y+2);
            g.drawLine(x + start + 1,y+3,x+start+mnemonicWidth,y+3);
          }
          
          g.setColor(mTextColor);
          g.drawString(getText(),x,y);
          g.drawLine(x + start,y+1,x+start+mnemonicWidth-1,y+1);
        }
        else {
          super.paintComponent(g);
        }
      }
    };
    menu.setOpaque(false);
    menu.setBackground(new Color(0,0,0,0));
  
    return menu;
  }
  
  /**
   * Get all installed Personas.
   * <p>
   * @return All installed Personas.
   */
  public PersonaInfo[] getInstalledPersonas() {
    return mPersonaMap.values().toArray(new PersonaInfo[mPersonaMap.size()]);
  }
}
