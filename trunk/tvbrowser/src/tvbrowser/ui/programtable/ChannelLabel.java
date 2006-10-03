package tvbrowser.ui.programtable;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.SwingConstants;

import tvbrowser.core.Settings;
import util.browserlauncher.Launch;
import util.ui.ChannelContextMenu;
import devplugin.Channel;

public class ChannelLabel extends util.ui.ChannelLabel {

  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(ChannelPanel.class);
  
  private static Cursor linkCursor=new Cursor(Cursor.HAND_CURSOR);
  private static Font channelNameFont;


  private Channel mChannel;
  
  public ChannelLabel(Channel ch) {
    super();
    mChannel = ch;
    
    if (Settings.propEnableChannelIcons.getBoolean() &&
        Settings.propShowChannelIconsInProgramTable.getBoolean()) {
      // Set Icon if it's available
      setIcon(ch.getIcon());
    }
    
    if (Settings.propShowChannelNamesInProgramTable.getBoolean()) {
      // Set the channel name as text
      String channelName = ch.getName();
      if (channelName == null) {
        channelName = mLocalizer.msg("unknown", "Unknown");
      }
      setText(channelName);
    }

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
        e.getComponent().setForeground(Color.blue);
      }
      
      public void mouseExited(MouseEvent e) {
        e.getComponent().setForeground(Color.black);
      }        
    });
  }
  
  private void showPopUp(MouseEvent e) {
    new ChannelContextMenu(e,mChannel,this);
  }

  public void setIcon(Icon icon) {
    if (Settings.propEnableChannelIcons.getBoolean() &&
        Settings.propShowChannelIconsInProgramTable.getBoolean()) {
      super.setIcon(icon);
    }
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