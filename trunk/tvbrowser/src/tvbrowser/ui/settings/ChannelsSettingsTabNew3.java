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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.ChannelList;
import tvbrowser.core.TvDataServiceManager;
import tvbrowser.ui.customizableitems.CustomizableItemsPanel;
import tvbrowser.ui.customizableitems.SortableItemList;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.ChannelListCellRenderer;
import util.ui.LinkButton;
import util.ui.TabLayout;
import util.ui.UiUtilities;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;
import devplugin.Channel;

/**
 * TV-Browser
 *
 * @author Bodo Tasche
 */
public class ChannelsSettingsTabNew3 implements devplugin.SettingsTab {
  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ChannelsSettingsTabNew3.class);
  
  private JPanel mSettingsPn;
  
  private boolean mShowAllButtons;

  private JList mAllChannels;
  private JList mSubscribedChannels;
  private JButton mLeftBtn, mRightBtn;
  
  public ChannelsSettingsTabNew3() {
    this(true);
  }
 
  public ChannelsSettingsTabNew3(boolean showAllButtons) {
    mShowAllButtons=showAllButtons;
  }
  
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {

  	mSettingsPn = new JPanel(new GridBagLayout());
  	
  	GridBagConstraints c = new GridBagConstraints();
  	
  	c.fill = GridBagConstraints.BOTH;
  	c.weightx = 1.0;
  	c.weighty = 1.0;
  	
  	GridBagConstraints d = new GridBagConstraints();
  	
  	d.fill = GridBagConstraints.NONE;
  	d.weightx = 0;
  	d.weighty = 0;
  	
  	
  	mSettingsPn.add(createLeftPanel(), c);
  	
  	mSettingsPn.add(createMiddlePanel(), d);
  	
  	c.gridwidth = GridBagConstraints.REMAINDER;
  	mSettingsPn.add(createRightPanel(), c);
  	
  	
  	c.fill = GridBagConstraints.HORIZONTAL;
  	c.weightx = 1.0;
  	c.weighty = 0;
  	
  	mSettingsPn.add(new LinkButton("Wollen Sie eigene Sender anbieten? Dann klicken sie hier!","http://wiki.tvbrowser.org/index.php/Eigene_TV-Daten_anbieten"), c);

  	addLeftRightActions();
  	fillChannelListBox();
  	
    return mSettingsPn;
  }

  private JPanel createLeftPanel() {
  	JPanel panel = new JPanel(new BorderLayout(4,4));
  	panel.setBorder(BorderFactory.createTitledBorder("Verfügbare Kanäle:"));
  	
  	JPanel top = new JPanel(new TabLayout(2));
  	
  	top.add(new JLabel("Type:"));
  	top.add(new JComboBox());
  	top.add(new JLabel("Country:"));
  	top.add(new JComboBox());
  	top.add(new JLabel("Provider:"));
  	top.add(new JComboBox());
  	panel.add(top, BorderLayout.NORTH);
  	
  	mAllChannels = new JList(new DefaultListModel());
  	mAllChannels.setCellRenderer(new ChannelListCellRenderer());
  	
  	panel.add(new JScrollPane(mAllChannels), BorderLayout.CENTER);
  	
  	JButton refresh = new JButton("Senderliste aktualisieren"); 
  	
    refresh.addActionListener(new ActionListener() {
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
  	
  	panel.add(refresh, BorderLayout.SOUTH);
  	
  	return panel;
  }
  
  private JPanel createRightPanel() {
  	JPanel panel = new JPanel( new BorderLayout(4,4));
  	panel.setBorder(BorderFactory.createTitledBorder("Abonnierte Kanäle:"));
  	
  	SortableItemList channelList = new SortableItemList();
  	
  	panel.add(channelList, BorderLayout.CENTER);
  
  	mSubscribedChannels = channelList.getList();
  	mSubscribedChannels.setCellRenderer(new ChannelListCellRenderer());
  	
  	JPanel details = new JPanel(new TabLayout(2));
  	
  	JLabel det = new JLabel("Details:");
  	det.setFont(det.getFont().deriveFont(Font.BOLD));
  	details.add(det);
  	details.add(new JLabel());
  	details.add(new JLabel("Channel:"));
  	details.add(new JLabel("Eurosport"));
  	details.add(new JLabel("Category:"));
  	details.add(new JLabel("Sport"));
  	details.add(new JLabel("Country:"));
  	details.add(new JLabel("Deutschland"));
  	details.add(new JLabel("Provider:"));
  	details.add(new JLabel("Bodo Tasche"));
  	details.add(new JLabel("Timezone:"));
  	details.add(new JLabel("GMT+1"));
  	
  	JPanel southPanel = new JPanel(new BorderLayout(4,4));
  	
  	southPanel.add(details, BorderLayout.CENTER);

  	final JButton configChannel = new JButton("Ausgew. Sender konfigurieren");
  	
    configChannel.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          Object[] o=mSubscribedChannels.getSelectedValues();
          
          Channel[] channelList=new Channel[o.length];
          for (int i=0;i<o.length;i++) {
            channelList[i]=(Channel)o[i];
          }
          ChannelConfigDlg dlg=new ChannelConfigDlg(mSettingsPn,mLocalizer.msg("configSelectedChannels","Configure selected channels"),channelList);
          dlg.centerAndShow();
          mSubscribedChannels.updateUI();
        }      
      });

  	mSubscribedChannels.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			configChannel.setEnabled(mSubscribedChannels.getSelectedIndex() != -1);
		}
  	});
  	
  	southPanel.add(configChannel, BorderLayout.SOUTH);
  	
  	panel.add(southPanel, BorderLayout.SOUTH);
  	
  	return panel;
  }
  
  private JPanel createMiddlePanel() {
  	JPanel panel = new JPanel(new GridLayout(2,1));
  	
  	JPanel btnLeftPanel = new JPanel(new BorderLayout());

  	mRightBtn = new JButton(new ImageIcon("imgs/Forward24.gif"));
	mRightBtn.setMargin(UiUtilities.ZERO_INSETS);
  	mRightBtn.setEnabled(false);
  	
  	btnLeftPanel.add(mRightBtn, BorderLayout.SOUTH);
  	
  	panel.add(btnLeftPanel);

  	JPanel btnRightPanel = new JPanel(new BorderLayout());
  	
  	mLeftBtn =  new JButton(new ImageIcon("imgs/Back24.gif"));
  	mLeftBtn.setMargin(UiUtilities.ZERO_INSETS);
  	mLeftBtn.setEnabled(false);
	
  	btnRightPanel.add(mLeftBtn, BorderLayout.NORTH);
  	panel.add(btnRightPanel);
  	
  	return panel;
  }
  

  private void addLeftRightActions() {
  	mAllChannels.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			mRightBtn.setEnabled(mAllChannels.getSelectedIndex() != -1);
		}
  	});
  	mSubscribedChannels.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			mLeftBtn.setEnabled(mSubscribedChannels.getSelectedIndex() != -1);
		}
  	});
  	
  }
  
  private void fillChannelListBox() {
    
  	((DefaultListModel)mSubscribedChannels.getModel()).removeAllElements();
  	((DefaultListModel)mAllChannels.getModel()).removeAllElements();
  	
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
    	((DefaultListModel)mAllChannels.getModel()).addElement(availableChannelArr[i]);
    }

    // Add the subscribed channels    
    for (int i = 0; i < subscribedChannelArr.length; i++) {
    	((DefaultListModel)mSubscribedChannels.getModel()).addElement(subscribedChannelArr[i]);
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
    return "Kanäle";
  }

}