package tvbrowser.ui.programtable;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
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
import devplugin.Channel;

public class ProgramTableChannelLabel extends ChannelLabel {

  private static Cursor linkCursor=new Cursor(Cursor.HAND_CURSOR);
  private static Font channelNameFont;

  private Channel mChannel;
  
  public ProgramTableChannelLabel(Channel ch) {
    super(Settings.propShowChannelIconsInProgramTable.getBoolean(),Settings.propShowChannelNamesInProgramTable.getBoolean());
    mChannel = ch;
    
    setForeground(UIManager.getColor("List.selectionForeground"));

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
        int r = (getForeground().getRed()   + getBackground().getRed())   >> 1;
        int g = (getForeground().getGreen() + getBackground().getGreen()) >> 1;
        int b = (getForeground().getBlue()  + getBackground().getBlue())  >> 1;
        
        e.getComponent().setForeground(new Color(r,g,b));
      }
      
      public void mouseExited(MouseEvent e) {
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
}