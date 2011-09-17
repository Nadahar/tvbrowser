package tvbrowser.ui.programtable;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tvbrowser.core.Settings;
import util.browserlauncher.Launch;
import util.ui.ChannelContextMenu;
import util.ui.ChannelLabel;
import util.ui.ToolTipWithIcon;
import util.ui.persona.Persona;
import devplugin.Channel;

public class ProgramTableChannelLabel extends ChannelLabel {

  private static Cursor linkCursor=new Cursor(Cursor.HAND_CURSOR);
  private static Font channelNameFont;

  private Channel mChannel;
  private boolean mIsRollover;
  
  public ProgramTableChannelLabel(Channel ch,KeyListener keyListener) {
    super(Settings.propShowChannelIconsInProgramTable.getBoolean(),Settings.propShowChannelNamesInProgramTable.getBoolean());
    mChannel = ch;
    
    addKeyListener(keyListener);
    updatePersona();
    
    setChannel(mChannel);
    setToolTipText("");
    
    // Check whether the font was set
    if (channelNameFont == null) {
      fontChanged();
    }

    // Avoid that a null-font is set
    // (Happens when the font from the config is null)
    if (channelNameFont != null) {
      setFont(channelNameFont);
    }
    
    mIsRollover = false;
    setOpaque(false);
    setHorizontalAlignment(SwingConstants.CENTER);
          
    setCursor(linkCursor);
    addMouseListener(new MouseAdapter(){
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showPopUp(e);
        } else if (e.getButton() == MouseEvent.BUTTON1){
          Launch.openURL(mChannel.getWebpage());
        }
        super.mouseReleased(e);
      }
      
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showPopUp(e);
        }
        super.mouseReleased(e);
      }
      
      public void mouseEntered(MouseEvent e) {
        mIsRollover = true;
        
        int r = (getForeground().getRed()   + getBackground().getRed())   >> 1;
        int g = (getForeground().getGreen() + getBackground().getGreen()) >> 1;
        int b = (getForeground().getBlue()  + getBackground().getBlue())  >> 1;
          
        e.getComponent().setForeground(new Color(r,g,b));
      }
      
      public void mouseExited(MouseEvent e) {
        mIsRollover = false;

        e.getComponent().setForeground(UIManager.getColor("List.selectionForeground"));
      }
    });
  }
  
  private void showPopUp(MouseEvent e) {
    new ChannelContextMenu(e,mChannel,this);
  }

  public static void fontChanged() {
    boolean useDefaults = Settings.propUseDefaultFonts.getBoolean();
    if (useDefaults) {
      channelNameFont = Settings.propChannelNameFont.getDefault();
    } else {
      channelNameFont = Settings.propChannelNameFont.getFont();
    }
  }
  
  @Override
  public JToolTip createToolTip() {
    // don't show tooltip, if disabled in settings
    if (!Settings.propShowChannelTooltipInProgramTable.getBoolean()) {
      return new ToolTipWithIcon(null, null);
    }
    boolean showIcon = false;
    boolean showText = false;
    JToolTip tip;
    Icon channelIcon = mChannel.getIcon();
    if (channelIcon != null && channelIcon instanceof ImageIcon) {
      Icon shownIcon = this.getIcon();
      if (shownIcon != null && (channelIcon.getIconHeight() > shownIcon.getIconHeight() || channelIcon.getIconWidth() > shownIcon.getIconWidth())) {
        showIcon = true;
      }
    }
    if (showIcon) {
      tip = new ToolTipWithIcon((ImageIcon) channelIcon);
//      tip.setMinimumSize(new Dimension(getIcon().getIconWidth() + 2,getIcon().getIconHeight() + 2));
    }
    else {
      tip = new ToolTipWithIcon((ImageIcon)null);
    }
    tip.setBackground(Color.WHITE);
    tip.setComponent(this);
    String text = null;
    if (showText) {
      text = mChannel.getName();
    }
    tip.setTipText(text);
    return tip;
  }

  @Override
  public Point getToolTipLocation(MouseEvent event) {
    FontMetrics metrics = this.getFontMetrics(this.getFont());
    int stringWidth = SwingUtilities.computeStringWidth(metrics, this.getText());
    int x = 0;
    Icon icon = this.getIcon();

    int iconWidth = getIcon().getIconWidth();
    if (mChannel.getIcon() != null) {
      iconWidth = mChannel.getIcon().getIconWidth();
    }

    int iconHeight = getIcon().getIconHeight();
    if (mChannel.getIcon() != null) {
      iconHeight = mChannel.getIcon().getIconHeight();
    }

    if (icon != null) {
      x = (this.getWidth() - stringWidth - this.getIconTextGap() - icon.getIconWidth()) / 2;
      if (x < 0) {
        x = 0;
      }
      x += (icon.getIconWidth() - iconWidth) / 2;
    }
    int y = (this.getHeight() - iconHeight - 2) / 2;
    return new Point(x, y);
  }
  
  /**
   * Updates Persona after change.
   */
  public void updatePersona() {
    if(Persona.getInstance().getHeaderImage() != null && Persona.getInstance().getTextColor() != null) {
      setForeground(Persona.getInstance().getTextColor());
    }
    else {
      setForeground(UIManager.getColor("List.selectionForeground"));
    }
  }
  
  protected void paintComponent(Graphics g) {
    if(Persona.getInstance().getHeaderImage() != null && Persona.getInstance().getTextColor() != null && Persona.getInstance().getShadowColor() != null) {
      Color c = Persona.getInstance().getAccentColor();
      
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
      
      Color textColor = Persona.getInstance().getTextColor();
      
      if(mIsRollover) {
        c = UIManager.getColor("List.selectionBackground");
        
        double test1 = (0.2126 * c.getRed()) + (0.7152 * c.getGreen()) + (0.0722 * c.getBlue());
        double test2 = (0.2126 * textColor.getRed()) + (0.7152 * textColor.getGreen()) + (0.0722 * textColor.getBlue());
        
        if(Math.abs(test2-test1) <= 40) {
          textColor = UIManager.getColor("List.selectionForeground");
        }
      }
      
      g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
      g.fillRect(0,0,getWidth(),getHeight());
            
      Icon icon = getIcon();      
      
      FontMetrics metrics = g.getFontMetrics(getFont());
      int textWidth = metrics.stringWidth(getText());
      int baseLine =  getHeight()/2+ metrics.getMaxDescent()+1;
      
      int iconTextLength = icon.getIconWidth() + getIconTextGap() + textWidth;
      
      int iconX = (getWidth()/2) - (iconTextLength/2);
      int iconY = (getHeight()/2) - (icon.getIconHeight()/2);
      
      icon.paintIcon(this,g,iconX,iconY);
      
      if(!Persona.getInstance().getShadowColor().equals(textColor) && Persona.getInstance().getTextColor().equals(textColor)) {
        g.setColor(Persona.getInstance().getShadowColor());
        
        g.drawString(getText(),iconX+icon.getIconWidth()+getIconTextGap()+1,baseLine+1);
      }
      
      g.setColor(textColor);
      g.drawString(getText(),iconX+icon.getIconWidth()+getIconTextGap(),baseLine);
    }
    else {
      super.paintComponent(g);
    }
  }
}