/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package tvbrowser.ui.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataServiceManager;
import tvbrowser.ui.customizableitems.CustomizableItemsPanel;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.ChannelListCellRenderer;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;
import devplugin.Channel;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ChannelsSettingsTab implements devplugin.SettingsTab {
  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ChannelsSettingsTab.class);
  
  private JPanel mSettingsPn;
  
  private CustomizableItemsPanel mChannelListPanel;
  
  private boolean mShowAllButtons;

  
  
  public ChannelsSettingsTab() {
    this(true);
  }
 
  public ChannelsSettingsTab(boolean showAllButtons) {
    mShowAllButtons=showAllButtons;
  }
  
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    String msg;
    
    mSettingsPn = new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
   
    String leftText = mLocalizer.msg("availableChannels", "Available channels");
    String rightText = mLocalizer.msg("subscribedChannels", "Subscribed channels");
    mChannelListPanel = CustomizableItemsPanel.createCustomizableItemsPanel(leftText, rightText);
    mChannelListPanel.setBorder(BorderFactory.createEmptyBorder(3,5,10,5));
   
    
    
   
    JTextArea textArea=new JTextArea(2,40);
    textArea.setOpaque(false);
    textArea.setEditable(false);
    textArea.setDisabledTextColor(Color.black);
    textArea.setEnabled(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    msg = mLocalizer.msg("infoText", "Use the left and right buttons to "
      + "subscribe or unsubscribe channels and the up and down buttons to "
      + "create your personal ordering of your favourite channels.");
    textArea.setText(msg);
    textArea.setBorder(BorderFactory.createEmptyBorder(10,5,5,0));
    
    JPanel southPn=new JPanel();
    southPn.setLayout(new BoxLayout(southPn,BoxLayout.Y_AXIS));
    
    if (mShowAllButtons) {
    
      final JButton configChannelBtn=new JButton(mLocalizer.msg("configSelectedChannels","Configure selected channels"));
      JButton updateChannelListBtn=new JButton(mLocalizer.msg("updateChannelList","Update channel list"));
    
      
    
      JPanel btnPanel=new JPanel(new GridLayout(1,2));
      JPanel btnBorderLeft=new JPanel(new BorderLayout());
      btnBorderLeft.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
      btnBorderLeft.add(updateChannelListBtn,BorderLayout.CENTER);
      JPanel btnBorderRight=new JPanel(new BorderLayout());
      btnBorderRight.setBorder(BorderFactory.createEmptyBorder(0,0,0,8));
      btnBorderRight.add(configChannelBtn,BorderLayout.CENTER);
    
      btnPanel.add(btnBorderLeft);
      btnPanel.add(btnBorderRight);
    
      southPn.add(btnPanel);
      
      final JList rightList=mChannelListPanel.getRightList();
      
      mChannelListPanel.getRightList().setCellRenderer(new ChannelListCellRenderer());
      mChannelListPanel.getLeftList().setCellRenderer(new ChannelListCellRenderer());
      int []sel=rightList.getSelectedIndices();
      configChannelBtn.setEnabled(sel.length>0);
      rightList.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          int[] locSel=rightList.getSelectedIndices();
          configChannelBtn.setEnabled(locSel.length>0);
        }
      });
      
      updateChannelListBtn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          
          final ProgressWindow win=new ProgressWindow(tvbrowser.ui.mainframe.MainFrame.getInstance());
        
          win.run(new Progress(){
            public void run() {
              tvdataservice.TvDataService services[]=TvDataServiceManager.getInstance().getDataServices();
              for (int i=0;i<services.length;i++) {
                if (services[i].supportsDynamicChannelList()) {
                  try {
                    services[i].checkForAvailableChannels(win);
                  }catch (TvBrowserException exc) {
                    ErrorHandler.handle(exc);
                  }
                }
              }
              
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ChannelList.create();
                    fillChannelListBox();
                }
                  
              });
            }
           
          });
       
        }  
      });
    
      configChannelBtn.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          Object[] o=mChannelListPanel.getRightSelections();
          
          Channel[] channelList=new Channel[o.length];
          for (int i=0;i<o.length;i++) {
            channelList[i]=(Channel)o[i];
          }
          ChannelConfigDlg dlg=new ChannelConfigDlg(mSettingsPn,mLocalizer.msg("configSelectedChannels","Configure selected channels"),channelList);
          dlg.centerAndShow();
        }      
      });
    }
    
    
    southPn.add(textArea);
    
    mSettingsPn.add(southPn,BorderLayout.SOUTH);
    mSettingsPn.add(mChannelListPanel,BorderLayout.CENTER);
    
      
    fillChannelListBox();     

    return mSettingsPn;
  }


  private void fillChannelListBox() {
    
    mChannelListPanel.clearLeft();
    mChannelListPanel.clearRight();
    
    // Split the channels in subscribed and available
    Iterator iter = ChannelList.getChannels();
    int subscribedChannelCount = ChannelList.getNumberOfSubscribedChannels();
    Channel[] subscribedChannelArr = new Channel[subscribedChannelCount];
    ArrayList availableChannelList = new ArrayList();
    while (iter.hasNext()) {
      Channel channel = (Channel) iter.next();
      
      if (ChannelList.isSubscribedChannel(channel)) {
        int pos = ChannelList.getPos(channel);
        ChannelList.getSubscribedChannels()[pos].copySettingsToChannel(channel);
        subscribedChannelArr[pos] = channel;
      } else {
        availableChannelList.add(channel);
      }
    }
    
    // Sort the available channels
    Channel[] availableChannelArr = new Channel[availableChannelList.size()];
    availableChannelList.toArray(availableChannelArr);
    Arrays.sort(availableChannelArr, createChannelComparator());

    // Add the available channels
    for (int i = 0; i < availableChannelArr.length; i++) {
      mChannelListPanel.addElementLeft(availableChannelArr[i]);
    }

    // Add the subscribed channels    
    for (int i = 0; i < subscribedChannelArr.length; i++) {
      mChannelListPanel.addElementRight(subscribedChannelArr[i]);
    }
  }

  private Comparator createChannelComparator() {
    return new Comparator() {
      public int compare(Object o1, Object o2) {
        return o1.toString().compareTo(o2.toString());
      }
    };
  }

  
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Object[] list = mChannelListPanel.getElementsRight();
    
    // Convert the list into a Channel[] and fill channels
    Channel[] channelArr = new Channel[list.length];
    for (int i = 0; i < list.length; i++) {
      channelArr[i] = (Channel) list[i];      
    }
    
    ChannelList.setSubscribeChannels(channelArr);
    Settings.propSubscribedChannels.setChannelArray(channelArr);
  }


  
  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return null;
  }
  
  
  
  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("channels", "Channels");
  }

}