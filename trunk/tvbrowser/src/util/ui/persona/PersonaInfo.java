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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import javax.imageio.ImageIO;

import util.ui.Localizer;

/**
 * A class that contains infos about a Persona.
 * <p>
 * @author René Mach
 * @since 3.0.3
 */
public class PersonaInfo {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(PersonaInfo.class);
  
  private File mSettings;
  private String mName;
  private String mDescription;
  private File mHeaderFile;
  private File mFooterFile;
  private Color mTextColor;
  private Color mShadowColor;
  private Color mAccentColor;
  private BufferedImage mCachedHeaderImage;
  private BufferedImage mCachedFooterImage;
  
  private String mId;
  
  PersonaInfo() {
    mName = mLocalizer.msg("noPersona","No Persona");
    mDescription = mLocalizer.msg("noPersonaDesc","No Persona selected");
    mId = "51b73c81-7d61-4626-b230-89627c9f5ce7";
  }
  
  /**
   * Create a new persona info from the given settings file.
   * <p>
   * @param settings The settings to read the info from.
   * @throws IndexOutOfBoundsException Thrown if somethind was wrong in the settings file.
   */
  public PersonaInfo(File settings) throws IndexOutOfBoundsException {
    mSettings = settings;
    
    Properties prop = new Properties();
    
    try {
      FileInputStream in = new FileInputStream(settings);
      prop.load(in);
      in.close();
    }catch(Exception e) {}
    
    mName = prop.getProperty(Persona.NAME_KEY+"."+Locale.getDefault().getLanguage(),prop.getProperty(Persona.NAME_KEY));
    mDescription = prop.getProperty(Persona.DESCRIPTION_KEY+"."+Locale.getDefault().getLanguage(),prop.getProperty(Persona.DESCRIPTION_KEY));
    mHeaderFile = getImageFile(Persona.HEADER_IMAGE_KEY,prop,settings);
    mFooterFile = getImageFile(Persona.FOOTER_IMAGE_KEY,prop,settings);
    
    String[] textColor = prop.getProperty(Persona.TEXT_COLOR_KEY).trim().split(",");
    String[] shadowColor = prop.getProperty(Persona.SHADOW_COLOR_KEY).trim().split(",");
    String[] accentColor = prop.getProperty(Persona.ACCENT_COLOR_KEY).trim().split(",");
    
    mTextColor = new Color(Integer.parseInt(textColor[0]),Integer.parseInt(textColor[1]),Integer.parseInt(textColor[2]));
    mShadowColor = new Color(Integer.parseInt(shadowColor[0]),Integer.parseInt(shadowColor[1]),Integer.parseInt(shadowColor[2]));
    
    mAccentColor = new Color(Integer.parseInt(accentColor[0]),Integer.parseInt(accentColor[1]),Integer.parseInt(accentColor[2]));
    
    mCachedHeaderImage = null;
    mCachedFooterImage = null;
  }
  
  private File getImageFile(String key, Properties prop, File file) {
    String value = prop.getProperty(key);
    
    if(value != null) {
      value = value.replace(Persona.TVB_PERSONA,file.getParent());
      value = value.replace(Persona.USER_PERSONA,file.getParent());
      
      return new File(value);
    }

    return null;
  }
  
  /**
   * Gets the name of this Persona.
   * <p>
   * @return The name of this Persona.
   */
  public String getName() {
    return mName;
  }
  
  /**
   * Gets the description of this Persona.
   * <p>
   * @return The description of this Persona.
   */
  public String getDescription() {
    return mDescription;
  }
  
  /**
   * Gets the text color of this Persona.
   * <p>
   * @return The text color of this Persona.
   */
  public Color getTextColor() {
    return mTextColor;
  }
  
  /**
   * Gets the shadow color of this Persona.
   * <p>
   * @return The shadow color of this Persona.
   */
  public Color getShadowColor() {
    return mShadowColor == null ? mTextColor : mShadowColor;
  }
  
  /**
   * Gets the accent color of this Persona.
   * <p>
   * @return The accent color of this Persona.
   */
  public Color getAccentColor() {
    return mAccentColor;
  } 
  
  /**
   * Gets the header image of this Persona.
   * <p>
   * @return The header image of this Persona.
   */
  public BufferedImage getHeaderImage() {
    if(mCachedHeaderImage == null && mHeaderFile != null && mHeaderFile.isFile()) {
      try {
        mCachedHeaderImage = ImageIO.read(mHeaderFile);
      } catch (IOException e) {}
    }
    
    return mCachedHeaderImage;
  }
  
  /**
   * Gets the footer image of this Persona.
   * <p>
   * @return The footer image of this Persona.
   */
  public BufferedImage getFooterImage() {
    if(mCachedFooterImage == null && mFooterFile != null && mFooterFile.isFile()) {
      try {
        mCachedFooterImage = ImageIO.read(mFooterFile);
      } catch (IOException e) {}
    }
    
    return mCachedFooterImage;
  }
  
  /**
   * Gets the id of this Persona.
   * <p>
   * @return The id of this Persona.
   */
  public String getId() {
    return mId == null ? mSettings.getParentFile().getName() : mId;
  }
}

