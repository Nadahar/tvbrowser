package tvbrowser.ui.settings;

import devplugin.SettingsTab;
import devplugin.Channel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import util.ui.UiUtilities;
import util.ui.TabLayout;
import tvbrowser.core.ChannelList;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 05.02.2005
 * Time: 12:42:23
 */
public class ChannelsSettingsTabNew2 implements SettingsTab {

   private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ChannelsSettingsTabNew.class);



   private JButton mUnsubscribeBt, mSubscribeBt, mUpBt, mDownBt, mConfigChannelsBt;

  private JList mList;
  private DefaultListModel mListModel;

  public ChannelsSettingsTabNew2() {

  }

  public JPanel createSettingsPanel() {
    JPanel topPanel = new JPanel(new GridLayout(2,1));
    topPanel.setBorder(BorderFactory.createEmptyBorder(10,10,11,11));
    JPanel northPanel = createNorthPanel();
    JPanel southPanel = createSouthPanel();

    topPanel.add(northPanel);
    topPanel.add(southPanel);
    return topPanel;
  }

  private JPanel createNorthPanel() {
     JPanel content = new JPanel(new BorderLayout());


    JPanel panel = new JPanel(new BorderLayout(3,3));

    CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(
      BorderFactory.createEtchedBorder(),
      BorderFactory.createEmptyBorder(3,3,3,3)
    );

    panel.setBorder(compoundBorder);


    String msg = mLocalizer.msg("tooltip.updateChannellist", "Refresh list");



    JPanel btnPanel = new JPanel(new BorderLayout());
    btnPanel.setBorder(BorderFactory.createEmptyBorder(6,0,6,0));
    JPanel subscribePanel = new JPanel(new GridLayout(1,4));
    mSubscribeBt = new JButton(new ImageIcon("imgs/Down24.gif"));
    msg = mLocalizer.msg("tooltip.subscribe", "Subscribe channel");
    mSubscribeBt.setToolTipText(msg);
    mSubscribeBt.setHorizontalTextPosition(SwingConstants.LEADING);
    mSubscribeBt.setMargin(UiUtilities.ZERO_INSETS);

    mUnsubscribeBt = new JButton(new ImageIcon("imgs/Up24.gif"));
    msg = mLocalizer.msg("tooltip.unsubscribe", "Unsubscribe channel");
    mUnsubscribeBt.setToolTipText(msg);
    mUnsubscribeBt.setMargin(UiUtilities.ZERO_INSETS);

    subscribePanel.add(mSubscribeBt);
    subscribePanel.add(mUnsubscribeBt);
    subscribePanel.add(new JLabel());
    subscribePanel.add(new JLabel());

   // panel.add(createChannelTable(), BorderLayout.CENTER);
    JPanel northPanel = new JPanel(new GridLayout(1,2));
    JPanel listPanel = createAvailableChannelsPanel();
    JPanel filterPanel = createRightPanel();
    northPanel.add(listPanel);
    northPanel.add(filterPanel);
    panel.add(northPanel, BorderLayout.CENTER);


    btnPanel.add(subscribePanel, BorderLayout.CENTER);


    content.add(panel, BorderLayout.CENTER);
    content.add(btnPanel, BorderLayout.SOUTH);



    return content;


  }

  private JPanel createSouthPanel() {
     JPanel topPanel = new JPanel(new BorderLayout());

     CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(
       BorderFactory.createEtchedBorder(),
       BorderFactory.createEmptyBorder(3,3,3,3)
     );
     topPanel.setBorder(compoundBorder);

     JPanel content = new JPanel(new GridLayout(1,2, 3,3));
     JPanel listBoxPanel = createSubscribedChannelListBoxPanel();
     JPanel channelInfoPanel = createChannelInfoPanel();

     content.add(listBoxPanel);
     content.add(channelInfoPanel);

     //topPanel.add(new JLabel("Subscribed Channels:"), BorderLayout.NORTH);
     topPanel.add(content, BorderLayout.CENTER);

     return topPanel;
   }


  private JPanel createSubscribedChannelListBoxPanel() {

    mUpBt = new JButton(new ImageIcon("imgs/Up24.gif"));
    String msg = mLocalizer.msg("tooltip.up", "Move selected rows up");
    mUpBt.setToolTipText(msg);
    mUpBt.setMargin(UiUtilities.ZERO_INSETS);

    mDownBt = new JButton(new ImageIcon("imgs/Down24.gif"));
    msg = mLocalizer.msg("tooltip.down", "Move selected rows down");
    mDownBt.setToolTipText(msg);
    mDownBt.setMargin(UiUtilities.ZERO_INSETS);

    JPanel btnPanel = new JPanel(new GridLayout(2,1));
    JPanel topPn = new JPanel(new BorderLayout());
    topPn.add(mUpBt, BorderLayout.SOUTH);

    JPanel bottomPn = new JPanel(new BorderLayout());
    bottomPn.add(mDownBt, BorderLayout.NORTH);

    btnPanel.add(topPn);
    btnPanel.add(bottomPn);



    JPanel content = new JPanel(new BorderLayout(3,3));
    JPanel titlePn = new JPanel(new BorderLayout());
    titlePn.add(new JLabel("Subscribed Channels:"), BorderLayout.WEST);
    content.add(titlePn, BorderLayout.NORTH);
    JPanel listPanel = new JPanel(new BorderLayout(4,4));
    mListModel = new DefaultListModel();
    Channel[] channels = ChannelList.getSubscribedChannels();
    for (int i=0; i<channels.length; i++) {
      mListModel.addElement(channels[i]);
    }
    mList = new JList(mListModel);
    listPanel.add(new JScrollPane(mList), BorderLayout.CENTER);
    listPanel.add(btnPanel, BorderLayout.EAST);

    mConfigChannelsBt = new JButton("configure selected channels");

    content.add(listPanel, BorderLayout.CENTER);
    content.add(mConfigChannelsBt, BorderLayout.SOUTH);

    return content;
  }


  private JPanel createChannelInfoPanel() {
     JPanel topPanel = new JPanel(new BorderLayout());
     topPanel.setBorder(BorderFactory.createTitledBorder("Channel details:"));

     JPanel content = new JPanel(new TabLayout(2));
     content.add(new JLabel("Channel:"));
     content.add(new JLabel("Eurosport"));
     content.add(new JLabel("Category:"));
     content.add(new JLabel("Sport"));
     content.add(new JLabel("Country:"));
     content.add(new JLabel("Deutschland"));
     content.add(new JLabel("Provider:"));
     content.add(new JLabel("Bodo Tasche"));
     content.add(new JLabel("Timezone:"));
     content.add(new JLabel("GMT+1"));

     topPanel.add(content, BorderLayout.NORTH);

     return topPanel;
   }


  private JPanel createAvailableChannelsPanel() {
    JPanel panel = new JPanel(new BorderLayout(4,4));

    JScrollPane listPn = new JScrollPane(new JList(ChannelList.getAvailableChannels()));
    JPanel pn = new JPanel(new BorderLayout());
    pn.add(listPn, BorderLayout.CENTER);
    panel.add(pn, BorderLayout.CENTER);
    panel.add(new JLabel("Available channels:"), BorderLayout.NORTH);



    JButton updateBtn = new JButton(new ImageIcon("imgs/Refresh24.gif"));
    //String msg = mLocalizer.msg("tooltip.up", "Move selected rows up");
    //mUpBt.setToolTipText(msg);
    updateBtn.setMargin(UiUtilities.ZERO_INSETS);

    JPanel btnPn = new JPanel(new BorderLayout());
    btnPn.add(updateBtn,BorderLayout.NORTH);
    panel.add(btnPn, BorderLayout.EAST);

  //  panel.add(new JButton("Update channellist"), BorderLayout.SOUTH);

   /*
    mSubscribeBt = new JButton("Subscribe", new ImageIcon("imgs/Down24.gif"));
    String msg = mLocalizer.msg("tooltip.subscribe", "Subscribe channel");
    mSubscribeBt.setToolTipText(msg);
    mSubscribeBt.setHorizontalTextPosition(SwingConstants.LEADING);
    mSubscribeBt.setMargin(UiUtilities.ZERO_INSETS);


    mUnsubscribeBt = new JButton("Unsubscribe", new ImageIcon("imgs/Up24.gif"));
    msg = mLocalizer.msg("tooltip.unsubscribe", "Unsubscribe channel");
    mUnsubscribeBt.setToolTipText(msg);
    mUnsubscribeBt.setMargin(UiUtilities.ZERO_INSETS);
       *

    JPanel btnPanel = new JPanel(new GridLayout(1,2));
    btnPanel.add(mSubscribeBt);
    btnPanel.add(mUnsubscribeBt);     */

   // panel.add(btnPanel, BorderLayout.SOUTH);

    return panel;
  }


  public JPanel createRightPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel filterPanel = new JPanel(new TabLayout(2));
    filterPanel.setBorder(BorderFactory.createTitledBorder("Filter"));
    panel.add(filterPanel, BorderLayout.NORTH);


    filterPanel.add(new JLabel("Category:"));
    filterPanel.add(new JComboBox());
    filterPanel.add(new JLabel("Country:"));
    filterPanel.add(new JComboBox());
    filterPanel.add(new JLabel("Provider:"));
    filterPanel.add(new JComboBox());


    return panel;
  }


  public void saveSettings() {

  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return "Channels (2)";
  }
}
