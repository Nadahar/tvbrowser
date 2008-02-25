package tvbrowser.ui.programtable;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingConstants;
import javax.swing.UIManager;

import tvbrowser.core.Settings;
import util.browserlauncher.Launch;
import util.ui.ChannelContextMenu;
import devplugin.Channel;

public class ChannelLabel extends util.ui.ChannelLabel {

  private static Cursor linkCursor=new Cursor(Cursor.HAND_CURSOR);
  private static Font channelNameFont;

  private Channel mChannel;
  
  public ChannelLabel(Channel ch) {
    super(Settings.propShowChannelIconsInProgramTable.getBoolean(),Settings.propShowChannelNamesInProgramTable.getBoolean());
    mChannel = ch;
    
    setForeground(UIManager.getColor("List.selectionForeground"));

    setChannel(mChannel);

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
  
  protected Channel getChannel() {
    return mChannel;
  }

}