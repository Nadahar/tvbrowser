package tvbrowser.ui.programtable;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import tvbrowser.core.Settings;
import tvbrowser.ui.settings.ChannelConfigDlg;
import util.ui.UiUtilities;
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
    setIcon(ch.getIcon());
        /*Settings.propEnableChannelIcons.getBoolean()) {
      // Set Icon if it's available
      if (Settings.propShowChannelIconsInProgramTable.getBoolean()*/
    if ( !(Settings.propEnableChannelIcons.getBoolean() && Settings.propShowChannelIconsInProgramTable.getBoolean()) || Settings.propShowChannelNames.getBoolean()) {
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
          util.ui.BrowserLauncher.openURL(mChannel.getWebpage());
        }
        super.mouseReleased(e);
      }
      
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showPopUp(e);
        }
        super.mouseReleased(e);
      }
      
      public void showPopUp(MouseEvent e) {
        JPopupMenu channelPopup = new JPopupMenu();
        
        JMenuItem item = new JMenuItem("Konfigurieren");
        
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            ChannelConfigDlg dialog;
            
            Window w = UiUtilities.getBestDialogParent(ChannelLabel.this);
            if (w instanceof JDialog) {
              dialog = new ChannelConfigDlg((JDialog)w, mChannel);
            } else {
              dialog = new ChannelConfigDlg((JFrame)w, mChannel);
            }
            dialog.centerAndShow();
            setChannel(mChannel);
          }
        });
        
        channelPopup.add(item);
        
        channelPopup.show(ChannelLabel.this, e.getPoint().x, e.getPoint().y);
      }
      
      public void mouseEntered(MouseEvent e) {
        e.getComponent().setForeground(Color.blue);
      }
      
      public void mouseExited(MouseEvent e) {
        e.getComponent().setForeground(Color.black);
      }        
    });
  }

  public void setIcon(Icon icon) {
    if (Settings.propEnableChannelIcons.getBoolean()) {
      // Set Icon if it's available
      if (Settings.propShowChannelIconsInProgramTable.getBoolean()) {
        super.setIcon(icon);
      }
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

}