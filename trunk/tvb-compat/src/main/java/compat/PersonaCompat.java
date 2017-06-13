/*
 * TV-Browser Compat
 * Copyright (C) 2017 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date: 2014-06-17 15:59:09 +0200 (Di, 17 Jun 2014) $
 *   $Author: ds10 $
 * $Revision: 8152 $
 */
package compat;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import devplugin.Version;
import tvbrowser.TVBrowser;
/**
 * Compatibility class for TV-Browser util.persona.Persona class.
 * 
 * @author René Mach
 * @since 0.2
 */
public final class PersonaCompat {
  private static PersonaCompat INSTANCE;
  private static Object PERSONA_OBJECT;
  
  private ArrayList<PersonaCompatListener> mListPersonaListeners;
  
  private PersonaCompat() {
    INSTANCE = this;
    PERSONA_OBJECT = null;
    mListPersonaListeners = new ArrayList<PersonaCompatListener>();
    
    if(isSupported()) {
      try {
        Method m = Class.forName("util.ui.persona.Persona").getDeclaredMethod("getInstance");
        PERSONA_OBJECT = m.invoke(null);
        
        Class<?> personaListenerClass = Class.forName("util.ui.persona.PersonaListener");
        Method addPersonaListener = PERSONA_OBJECT.getClass().getDeclaredMethod("registerPersonaListener", personaListenerClass);
        
        Object personaListener = Proxy.newProxyInstance(personaListenerClass.getClassLoader(), new Class<?>[] {personaListenerClass}, new HandlerPersonaListener(this));
        
        addPersonaListener.invoke(PERSONA_OBJECT, personaListener);
      } catch (Exception e) {
        // Ignore
      }
    }
  }
  
  /**
   * @return The one instance of this class.
   */
  public static synchronized PersonaCompat getInstance() {
    if(INSTANCE == null) {
      new PersonaCompat();
    }
    
    return INSTANCE;
  }
  
  /**
   * Checks if Personas are supported by the used TV-Browser version.
   * 
   * @return <code>true</code> for TV-Browser version since 3.1, <code>false</code> otherwise.
   */
  public boolean isSupported() {
    return TVBrowser.VERSION.compareTo(new Version(3,10,true)) >= 0;
  }
  
  /**
   * Register a listener to listen for Persona change events.
   * 
   * @param listener The listener to register.
   */
  public void registerPersonaListener(final PersonaCompatListener listener) {
    if(mListPersonaListeners.contains(listener)) {
      mListPersonaListeners.add(listener);
    }
  }
  
  /**
   * Unregister a listener for Persona change events.
   * 
   * @param listener The listener to unregister.
   */
  public void removePersonaListener(final PersonaCompatListener listener) {
    mListPersonaListeners.remove(listener);
  }
  /**
   * Test the given color against the Persona foreground color
   * and returns a color that is readable on the given color.
   * <p>
   * @param c The color to test.
   * @return The readable Color.
   */
  public static Color testPersonaForegroundAgainst(Color c) {
    Color result = null;
    
    if(PERSONA_OBJECT != null) {
      result = (Color)invoke("testPersonaForegroundAgainst", new Class<?>[] {Color.class}, c);
    }
    
    if(result == null) {
      double test = (0.2126 * getInstance().getTextColor().getRed()) + (0.7152 * getInstance().getTextColor().getGreen()) + (0.0722 * getInstance().getTextColor().getBlue());
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
      
      result = new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha);
    }
    
    return result;
  }
  
  /**
   * Get the image for the header.
   * <p>
   * @return The image for the header or <code>null</code> if there is none.
   */
  public BufferedImage getHeaderImage() {
    return (BufferedImage)invoke("getHeaderImage");
  }
  
  /**
   * Get the image for the footer.
   * <p>
   * @return The image for the footer or <code>null</code> if there is none.
   */
  public BufferedImage getFooterImage() {
    return (BufferedImage)invoke("getFooterImage");
  }
  
  /**
   * Gets the color for the text foreground.
   * <p>
   * @return The color for the text foreground.
   */
  public Color getTextColor() {
    return (Color)invoke("getTextColor");
  }
  
  /**
   * Gets the color for the text shadow.
   * If equal to text color no shadow is painted.
   * <p>
   * @return The color for the text shadow.
   */
  public Color getShadowColor() {
    return (Color)invoke("getShadowColor");
  }
  
  /**
   * Gets the accent color.
   * <p>
   * @return The color for the text shadow.
   */
  public Color getAccentColor() {
    return (Color)invoke("getAccentColor");
  }
  
  private void handlePersonaUpdate() {
    for(PersonaCompatListener l : mListPersonaListeners) {
      l.updatePersona();
    }
  }
  
  private static Object invoke(final String methodName) {
    return invoke(methodName, null);
  }
  
  private static Object invoke(final String methodName, Class<?>[] clazz, Object... parameters) {
    Object result = null;
    
    if(PERSONA_OBJECT != null) {
      try {
        Method m = PERSONA_OBJECT.getClass().getDeclaredMethod(methodName, clazz);
        result = m.invoke(PERSONA_OBJECT, parameters);
      } catch (Exception e) {
        // Ignore
      }
    }
    
    return result;
  }
  
  private static final class HandlerPersonaListener implements InvocationHandler {
    private PersonaCompat mPersonaCompat;
    
    private HandlerPersonaListener(PersonaCompat personaCompat) {
      mPersonaCompat = personaCompat;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if(method != null && (args == null || args.length == 0)) {
        if(method.getName().equals("updatePersona")) {
          mPersonaCompat.handlePersonaUpdate();
        }
      }
      
      return null;
    }
    
  }
}
