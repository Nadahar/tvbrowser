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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import tvbrowser.core.Settings;

/**
 * A class to handle Personas for TV-Browser.
 * <p>
 * @author René Mach
 * @since 3.0.3
 */
public final class Persona {
  private static Persona mInstance;
  private HashMap<String,PersonaInfo> mPersonaMap;
  private final static String PERSONA_DIR = "personas";
  
  private final static int mBorderWidth = 9;
  private final static int mBorderHeight = 4;
  
  private String mDetailURL;
  private String mId;
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
  /** The detail url key for the Persona properties */
  public final static String DETAIL_URL_KEY = "detailURL";
  
  /** The key for the space holder for images in the user Persona directory */
  public final static String USER_PERSONA = "{user.persona}";
  /** The key for the space holder for images in the global TV-Browser Persona directory */
  public final static String TVB_PERSONA = "{tvb.persona}";
  
  private ArrayList<PersonaListener> mPersonaListenerList;
  
  private Persona() {
    mInstance = this;
    mPersonaMap = new HashMap<String,PersonaInfo>(1);
    mPersonaListenerList = new ArrayList<PersonaListener>();
    loadPersonas();    
    applyPersona();
  }
  
  /**
   * Register the PersonaListener to listen to Persona changes.
   * <p>
   * @param listener The listener to register. 
   */
  public void registerPersonaListener(PersonaListener listener) {
    mPersonaListenerList.add(listener);
  }
  
  /**
   * Remove the given listener.
   * <p>
   * @param listener The listener to remove.
   */
  public void removePersonaListerner(PersonaListener listener) {
    mPersonaListenerList.remove(listener);
  }
  
  /**
   * Applies the current selected Persona.
   */
  public void applyPersona() {
    String id = Settings.propSelectedPersona.getString();
    
    if(Settings.propRandomPersona.getBoolean() && mPersonaMap.size() > 2) {
      PersonaInfo[] installedPersonas = getInstalledPersonas();
      
      int index = 0;
      
      do {
        index = (int)(Math.random()*installedPersonas.length);
      }while(installedPersonas[index].getId().equals(PersonaInfo.DEFAULT_ID) || installedPersonas[index].getId().equals(PersonaInfo.RANDOM_ID));
      
      id = installedPersonas[index].getId();
    }
    
    try {
    PersonaInfo personaInfo = mPersonaMap.get(id);
    
    if(personaInfo == null) {
      Settings.propSelectedPersona.setString(new PersonaInfo().getId());
      personaInfo = mPersonaMap.get(Settings.propSelectedPersona.getString());
    }
    
    if(personaInfo != null) {
      mId = personaInfo.getId();
      mName = personaInfo.getName();
      mDescription = personaInfo.getDescription();
      mHeaderImage = personaInfo.getHeaderImage();
      mFooterImage = personaInfo.getFooterImage();
      mTextColor = personaInfo.getTextColor();
      mShadowColor = personaInfo.getShadowColor();
      mAccentColor = personaInfo.getAccentColor();
      mDetailURL = personaInfo.getDetailURL();
    }
    else {
      mId = "DUMMY";
      mName = "Standard";
      mDescription = "Standard";
      mHeaderImage = null;
      mFooterImage = null;
      mTextColor = null;
      mShadowColor = null;
      mAccentColor = null;
      mDetailURL = "http://www.tvbrowser.org";
    }
    
    for(PersonaListener listener : mPersonaListenerList) {
      listener.updatePersona();
    }
    
    }catch(Throwable t) {t.printStackTrace();}
  }
  
  /**
   * Loads all available Personas.
   */
  public void loadPersonas() {try {
    mPersonaMap.clear();
    
    PersonaInfo defaultInfo = new PersonaInfo();
    mPersonaMap.put(defaultInfo.getId(),defaultInfo);
    defaultInfo = new PersonaInfo(true);
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
   * Get the detail url of the current Persona.
   * <p>
   * @return The detail url of the current Persona.
   */
  public String getDetailURL() {
    return mDetailURL;
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
          
          if(!mShadowColor.equals(mTextColor) && !isOpaque()) {
            g.setColor(mShadowColor);
            g.drawString(getText(),x+1,y+1);
            g.drawString(getText(),x+2,y+2);
            
            g.drawLine(x + start + 1,y+2,x+start+mnemonicWidth,y+2);
            g.drawLine(x + start + 1,y+3,x+start+mnemonicWidth,y+3);
          }
          
          if(!isOpaque()) {
            g.setColor(mTextColor);
          }
          else {
            g.setColor(UIManager.getColor("List.selectionForeground"));
          }
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
     PersonaInfo[] installedPersonas = mPersonaMap.values().toArray(new PersonaInfo[mPersonaMap.size()]);
     
     Arrays.sort(installedPersonas,new Comparator<PersonaInfo>() {
       @Override
       public int compare(PersonaInfo o1, PersonaInfo o2) {
         return o1.getName().compareToIgnoreCase(o2.getName());
       }
     });
     
     return installedPersonas;
  }
  
  /**
   * Update the persona 
   * @param id
   */
  public void updatePersona(String id) {
    if(id != null) {
      PersonaInfo info = mPersonaMap.get(id);
      
      if(info != null && info.isEditable()){
        info.load();
        
        if(id.equals(mId)) {
          applyPersona();
        }
      }
    }
  }
  
  /**
   * Gets the id of the currently viewed persona
   * <p>
   * @return The id of the currently viewed persona.
   */
  public String getId() {
    return mId;
  }
  
  /**
   * @return The directory for the user personas.
   */
  public static File getUserPersonaDir() {
    return new File(Settings.getUserSettingsDirName(),PERSONA_DIR);
  }
  
  /**
   * @param id The id of the Persona to get.
   * @return The PersonaInfo for the given id.
   */
  public PersonaInfo getPersonaInfo(String id) {
    return mPersonaMap.get(id);
  }
  
  /**
   * Activates the given Persona. 
   * <p>
   * @param info The Persona to activate.
   */
  public void activatePersona(PersonaInfo info) {
    if(info != null && mPersonaMap.get(info.getId()) != null) {
      if(!info.getId().equals(PersonaInfo.RANDOM_ID)) {
        Settings.propSelectedPersona.setString(info.getId());
        Settings.propRandomPersona.setBoolean(false);
      }
      else {
        Settings.propRandomPersona.setBoolean(true);
      }
      
      applyPersona();
    }
  }
  
  /**
   * Removes the given Persona form the list.
   * <p>
   * @param info The Persona to remove.
   * @return <code>true</code> if the Persona could be removed.
   */
  public boolean removePersona(PersonaInfo info) {
    if(info != null && !info.isSelectedPersona() && !info.getId().equals(PersonaInfo.DEFAULT_ID) && !info.getId().equals(PersonaInfo.RANDOM_ID)) {
      return mPersonaMap.remove(info.getId()) != null;
    }
    
    return false;
  }
  
  /**
   * Paints the given button on the given Graphics with persona settings.
   * <p>
   * @param g The graphics to paint on.
   * @param b The button to paint.
   */
  public static void paintButton(Graphics g, JButton b) {
    Color c = testPersonaForegroundAgainst(Persona.getInstance().getAccentColor());
    Color textColor = Persona.getInstance().getTextColor();
    int alpha = c.getAlpha();
    
    if(b.getModel().isArmed() || b.getModel().isRollover() || b.isFocusOwner()) {
      c = UIManager.getColor("List.selectionBackground");
      
      double test1 = (0.2126 * c.getRed()) + (0.7152 * c.getGreen()) + (0.0722 * c.getBlue());
      double test2 = (0.2126 * textColor.getRed()) + (0.7152 * textColor.getGreen()) + (0.0722 * textColor.getBlue());
      
      if(Math.abs(test2-test1) <= 40) {
        textColor = UIManager.getColor("List.selectionForeground");
      }
    }
    
    if(b.getModel().isPressed()) {
      alpha -= 50;
    }
    else if(b.isFocusOwner() && !b.getModel().isRollover()) {
      alpha -= 100;
    }
    
    g.setColor(Persona.getInstance().getTextColor());
    g.draw3DRect(0,0,b.getWidth()-1,b.getHeight()-1,!b.getModel().isPressed());
    g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
    g.fillRect(1,1,b.getWidth()-2,b.getHeight()-2);
    
    Icon icon = b.isEnabled() ? b.getIcon() : b.getDisabledIcon();      
    
    FontMetrics metrics = g.getFontMetrics(b.getFont());
    int textWidth = metrics.stringWidth(b.getText());
    int baseLine =  b.getHeight()/2+ metrics.getMaxDescent()+1;
    
    int iconTextLength = (icon != null ? (icon.getIconWidth() + b.getIconTextGap()) : 0) + textWidth;
    int iconX = (b.getWidth()/2) - (iconTextLength/2);
    
    if(icon != null) {
      int iconY = (b.getHeight()/2) - (icon.getIconHeight()/2);
      
      icon.paintIcon(b,g,iconX,iconY);
      iconX = iconX + icon.getIconWidth()+b.getIconTextGap();
    }
    
    if(!b.isEnabled()) {
      textColor = Color.lightGray;
    }
    
    if(!Persona.getInstance().getShadowColor().equals(textColor) && Persona.getInstance().getTextColor().equals(textColor)) {
      g.setColor(Persona.getInstance().getShadowColor());
      
      g.drawString(b.getText(),iconX+1,baseLine+1);
    }
    
    g.setColor(textColor);
    g.drawString(b.getText(),iconX,baseLine);
  }
  
  /**
   * Test the given color against the Persona foreground color
   * and returns a color that is readable on the given color.
   * <p>
   * @param c The color to test.
   * @return The readable Color.
   */
  public static Color testPersonaForegroundAgainst(Color c) {
    double test = (0.2126 * Persona.getInstance().getTextColor().getRed()) + (0.7152 * Persona.getInstance().getTextColor().getGreen()) + (0.0722 * Persona.getInstance().getTextColor().getBlue());
    int alpha = 100;
    
    if(test <= 30) {
      c = Color.white;
      alpha = 200;
    }
    else if(test <= 40) {
      c = c.brighter().brighter().brighter().brighter().brighter().brighter();
      alpha = 200;
    }
    else if(test <= 60) {
      c = c.brighter().brighter().brighter();
      alpha = 160;
    }
    else if(test <= 100) {
      c = c.brighter().brighter();
      alpha = 140;
    }
    else if(test <= 145) {
      alpha = 120;
    }
    else if(test <= 170) {
      c = c.darker();
      alpha = 120;
    }
    else if(test <= 205) {
      c = c.darker().darker();
      alpha = 120;
    }
    else if(test <= 220){
      c = c.darker().darker().darker();
      alpha = 100;
    }
    else if(test <= 235){
      c = c.darker().darker().darker().darker();
      alpha = 100;
    }
    else {
      c = Color.black;
      alpha = 100;
    }
    
    c = new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha);
    
    return c;
  }
  
  /**
   * @return A JPanel with the Persona as Background.
   */
  public static JPanel createPersonaBackgroundPanel() {
    return new PersonaBackgroundPanel();
  }
  
  /**
   * Creates a button that uses the Pesona colors.
   * <p>
   * @param text Text of the button
   * @return The created button.
   */
  public static JButton createPersonaButton(String text) {
    return createPersonaButton(text,null);
  }

  /**
   * Creates a button that uses the Pesona colors.
   * <p>
   * @param text Text of the button
   * @param icon The icon for the button
   * @return The created button.
   */
  public static JButton createPersonaButton(String text, Icon icon) {
    JButton button=new JButton(text,icon) {
      protected void paintComponent(Graphics g) {
        if(Persona.getInstance().getHeaderImage() != null && Persona.getInstance().getTextColor() != null && Persona.getInstance().getShadowColor() != null) {
          Persona.paintButton(g,this);
        }
        else {
          super.paintComponent(g);
        }
      }
    };
    
    if(button != null && Persona.getInstance().getHeaderImage() != null) {
      button.setBorder(BorderFactory.createEmptyBorder(mBorderHeight,mBorderWidth,mBorderHeight,mBorderWidth));
      button.setRolloverEnabled(true);
      button.setOpaque(false);
    }
    
    return button;
  }
  
  /**
   * Gets the border for a Persona button
   * <p>
   * @return The border for a Persona button.
   */
  public static Border getPersonaButtonBorder() {
    return BorderFactory.createEmptyBorder(mBorderHeight,mBorderWidth,mBorderHeight,mBorderWidth);
  }
}
