package compat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import devplugin.Version;
import tvbrowser.TVBrowser;
import util.ui.ChannelLabel;
import util.ui.UiUtilities;

public final class UiCompat {
  /**
   * @param helpTextArea
   * @param html
   * @param background
   * @since 3.0.2
   */
  public static void updateHtmlHelpTextArea(final JEditorPane helpTextArea, String html, Color background) {
    updateHtmlHelpTextArea(helpTextArea, html, UIManager.getColor("Label.foreground"), background);
  }
  
  /**
   * @param helpTextArea The editor pane to update.
   * @param html The text for the editor pane.
   * @param foreground The foreground color. 
   * @param background The background color.
   * @since 3.3.4
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
  
  public static ChannelLabel createChannelLabel(boolean channelIconsVisible, boolean textIsVisible, boolean showDefaultValues, boolean showCountry, boolean showJoinedChannelInfo) {
    return createChannelLabel(channelIconsVisible, textIsVisible, showDefaultValues, showCountry, showJoinedChannelInfo, false);
  }
  
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
}
