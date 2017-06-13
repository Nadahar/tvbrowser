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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Dialog.ModalityType;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;

import devplugin.Version;
import tvbrowser.TVBrowser;
import util.ui.ChannelLabel;
import util.ui.UiUtilities;

/**
 * A compatiblility class for TV-Browser util.ui.UiUtilities.
 * 
 * @author René Mach
 * @since 0.2
 */
public final class UiCompat {
  /**
   * @param helpTextArea
   * @param html
   * @param background
   */
  public static void updateHtmlHelpTextArea(final JEditorPane helpTextArea, String html, Color background) {
    updateHtmlHelpTextArea(helpTextArea, html, UIManager.getColor("Label.foreground"), background);
  }
  
  /**
   * @param helpTextArea The editor pane to update.
   * @param html The text for the editor pane.
   * @param foreground The foreground color. 
   * @param background The background color.
   */
  public static void updateHtmlHelpTextArea(final JEditorPane helpTextArea, String html, Color foreground, Color background) {
    if(TVBrowser.VERSION.compareTo(new Version(3,34,true)) >= 0) {
      try {
        Class<?> clazz = UiUtilities.class;
        Method m = clazz.getMethod("updateHtmlHelpTextArea", JEditorPane.class, String.class, Color.class, Color.class);
        m.invoke(clazz, helpTextArea, html, foreground, background);
      }catch(Exception e) {
        e.printStackTrace();
      }
    }
    else {
      if (html.indexOf("<html>") >= 0) {
        int start = html.indexOf("<html>");
        int end = html.lastIndexOf("</html>");
        
        if(end == -1 || end < start) {
          end = html.length();
        }
        
        html = html.substring(start, end);
      }
      
      Font font = UIManager.getFont("Label.font");
      html = "<html><div style=\"color:" + UiUtilities.getHTMLColorCode(foreground)+";font-family:" + font.getName()
          + "; font-size:" + font.getSize() +";background-color:rgb(" + background.getRed() + "," + background.getGreen() + "," + background.getBlue() + ");\">" + html + "</div></html>";
  
      helpTextArea.setFont(font);
      helpTextArea.setText(html);
    }
  }
  
  /**
   * Creates a panel that has a semi-transparent background
   * created of Persona colors.
   * <p>
   * @return The created JPanel.
   * @since 3.2
   */
  public static JPanel createPersonaBackgroundPanel() {
    JPanel panel = new JPanel(new BorderLayout()){
      protected void paintComponent(Graphics g) {
        if(PersonaCompat.getInstance().getAccentColor() != null && PersonaCompat.getInstance().getHeaderImage() != null) {
         
          Color c = PersonaCompat.testPersonaForegroundAgainst(PersonaCompat.getInstance().getAccentColor());
          
          int alpha = c.getAlpha();
          
          g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
          g.fillRect(0,0,getWidth(),getHeight());
        }
        else {
          super.paintComponent(g);
        }
      }
    };
    panel.setOpaque(false);
    
    return panel;
  }
  
  /**
   * Creates a channel label.
   * NOTE: Not all functions are supported by older TV-Browser version,
   * so the user will see only the supported ones. 
   * 
   * @param channelIconsVisible If the channel icon should be shown.
   * @param textIsVisible If the channel name should be shown.
   * @param showDefaultValues If the default values should be shown.
   * @param showCountry If the country should be shown.
   * @param showJoinedChannelInfo The the joint channel info should be shown.
   * @return The created channel label.
   */
  public static ChannelLabel createChannelLabel(boolean channelIconsVisible, boolean textIsVisible, boolean showDefaultValues, boolean showCountry, boolean showJoinedChannelInfo) {
    return createChannelLabel(channelIconsVisible, textIsVisible, showDefaultValues, showCountry, showJoinedChannelInfo, false);
  }
  
  /**
   * Creates a channel label.
   * NOTE: Not all functions are supported by older TV-Browser version,
   * so the user will see only the supported ones. 
   * 
   * @param channelIconsVisible If the channel icon should be shown.
   * @param textIsVisible If the channel name should be shown.
   * @param showDefaultValues If the default values should be shown.
   * @param showCountry If the country should be shown.
   * @param showJoinedChannelInfo The the joint channel info should be shown.
   * @param showTimeLimitation The time limitation should be shown
   * @return The created channel label.
   */
  public static ChannelLabel createChannelLabel(final boolean channelIconsVisible, final boolean textIsVisible, final boolean showDefaultValues, final boolean showCountry, final boolean showJoinedChannelInfo, final boolean showTimeLimitation) {
    ChannelLabel result = null;
    
    if(VersionCompat.isJointChannelSupported()) {
      try {
        Constructor<ChannelLabel> c = ChannelLabel.class.getConstructor(boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class);
        result = c.newInstance(channelIconsVisible, textIsVisible, showDefaultValues, showCountry, showJoinedChannelInfo, showTimeLimitation);
      }catch(Exception e) {}
    }
    
    if(result == null) {
      result = new ChannelLabel(channelIconsVisible, textIsVisible, showDefaultValues, showCountry);
    }
    
    return result;
  }

  /**
   * Creates a channel label.
   * NOTE: Not all functions are supported by older TV-Browser version,
   * so the user will see only the supported ones. 
   * 
   * @param channelIconsVisible If the channel icon should be shown.
   * @param textIsVisible If the channel name should be shown.
   * @param showDefaultValues If the default values should be shown.
   * @param showCountry If the country should be shown.
   * @param showJoinedChannelInfo The the joint channel info should be shown.
   * @param showTimeLimitation The time limitation should be shown
   * @param showSortNumber The sort number should be shown.
   * @return The created channel label.
   */
  public static ChannelLabel createChannelLabel(boolean channelIconsVisible, boolean textIsVisible, boolean showDefaultValues, boolean showCountry, boolean showJoinedChannelInfo, boolean showTimeLimitation, boolean showSortNumber) {
    ChannelLabel result = null;
    
    if(VersionCompat.isSortNumberSupported()) {
      try {
        Constructor<ChannelLabel> c = ChannelLabel.class.getConstructor(boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class);
        result = c.newInstance(channelIconsVisible, textIsVisible, showDefaultValues, showCountry, showJoinedChannelInfo, showTimeLimitation, showSortNumber);
      }catch(Exception e) {}
    }
    
    if(result == null) {
      result = createChannelLabel(channelIconsVisible, textIsVisible, showDefaultValues, showCountry, showJoinedChannelInfo, showTimeLimitation);
    }
    
    return result;
  }
  
  /**
   * Creates a channel label.
   * NOTE: Not all functions are supported by older TV-Browser version,
   * so the user will see only the supported ones. 
   * 
   * @param channelIconsVisible If the channel icon should be shown.
   * @param textIsVisible If the channel name should be shown.
   * @param showDefaultValues If the default values should be shown.
   * @param showCountry If the country should be shown.
   * @param showJoinedChannelInfo The the joint channel info should be shown.
   * @param showTimeLimitation The time limitation should be shown
   * @param showSortNumber The sort number should be shown.
   * @param paintBackground The user set background color should be used.
   * @return The created channel label.
   */  
  public static ChannelLabel createChannelLabel(boolean channelIconsVisible, boolean textIsVisible, boolean showDefaultValues, boolean showCountry, boolean showJoinedChannelInfo, boolean showTimeLimitation, boolean showSortNumber, boolean paintBackground) {
    ChannelLabel result = null;
    
    if(TVBrowser.VERSION.compareTo(new Version(3,44,95,false)) >= 0) {
      try {
        Constructor<ChannelLabel> c = ChannelLabel.class.getConstructor(boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class);
        result = c.newInstance(channelIconsVisible, textIsVisible, showDefaultValues, showCountry, showJoinedChannelInfo, showTimeLimitation, showSortNumber, paintBackground);
      }catch(Exception e) {}
    }
    
    if(result == null) {
      result = createChannelLabel(channelIconsVisible, textIsVisible, showDefaultValues, showCountry, showJoinedChannelInfo, showTimeLimitation, showSortNumber);
    }
    
    return result;
  }
  
  /**
   * Adds support for rotating through list with up and down keys.
   * <p>
   * @param list The list to add the rotation to
   */
  public static void addKeyRotation(final JList list) {
    list.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if(list.getModel().getSize() > 1) {
          if(e.getKeyCode() == KeyEvent.VK_DOWN && e.getModifiersEx() == 0 && list.getSelectedIndex() == list.getModel().getSize()-1) {
            list.setSelectedIndex(0);
            list.ensureIndexIsVisible(0);
            e.consume();
          }
          else if(e.getKeyCode() == KeyEvent.VK_UP && e.getModifiersEx() == 0 && list.getSelectedIndex() == 0) {
            list.setSelectedIndex(list.getModel().getSize()-1);
            list.ensureIndexIsVisible(list.getModel().getSize()-1);
            e.consume();
          }
        }
      }
    });
  }
  
  /**
   * @return The best modality type for the used TV-Browser version.
   */
  public static ModalityType getSuggestedModalityType() {
    ModalityType result = ModalityType.APPLICATION_MODAL;
    
    if(VersionCompat.isAtLeastTvBrowser4()) {
      result = ModalityType.DOCUMENT_MODAL;
    }
    
    return result;
  }
}
