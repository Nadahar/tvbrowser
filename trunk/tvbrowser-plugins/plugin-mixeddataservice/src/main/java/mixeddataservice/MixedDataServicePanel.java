package mixeddataservice;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import tvdataservice.SettingsPanel;
import util.ui.ChannelLabel;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.Channel;
import devplugin.Plugin;



public class MixedDataServicePanel extends SettingsPanel {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

//  private static final Logger mLog = java.util.logging.Logger.getLogger(MixedDataServicePanel.class.getName());
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(MixedDataServicePanel.class);

  static MixedDataServicePanel mInstance;
  private MixedDataService mService;
  private JPanel selChannelPanel;
  public Properties channelDescriptions;
  private JList selList;
  private DefaultListModel selModel = new DefaultListModel();


  public MixedDataServicePanel(){

    setOpaque(false);
    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
   JPanel backgroundPanel = new JPanel(new BorderLayout(0, 10));
   backgroundPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

    ImageIcon newIcon = Plugin.getPluginManager().getIconFromTheme(null,"actions", "document-new", 16);
    ImageIcon editIcon = Plugin.getPluginManager().getIconFromTheme(null,"actions", "document-edit", 16);
    ImageIcon removeIcon = Plugin.getPluginManager().getIconFromTheme(null,"actions", "edit-delete", 16);

    // channel list
    selChannelPanel = new JPanel(new BorderLayout(10, 0));

    selList = new JList(selModel);
    selList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    selList.setCellRenderer(new ChannelCellRenderer());
    selList.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
          e.consume();
          editButtonPressed();
        }
      }
    });
    JScrollPane selListScroller = new JScrollPane(selList);
    selChannelPanel.add(selListScroller, BorderLayout.CENTER);

    backgroundPanel.add(selChannelPanel, BorderLayout.CENTER);


    // button Panel
    JPanel buttonPanel = new JPanel ();
    buttonPanel.setLayout(new GridLayout(0, 1, 0, 4));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));


    // New-Button
    JButton newButton = new JButton(Localizer.getLocalization(Localizer.I18N_ADD));
    newButton.setIcon(newIcon);
    newButton.setToolTipText(mLocalizer.msg("createChannelText", "Create new Mixed Channel"));
    newButton.setHorizontalAlignment(SwingConstants.LEFT);
    newButton.setIconTextGap(8);
    buttonPanel.add(newButton);
    newButton.setMargin(new Insets(1, 3, 1, 3));
    newButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        newButtonPressed(e);
      }
    });

    // Remove-Button
    JButton removeButton = new JButton(Localizer.getLocalization(Localizer.I18N_DELETE));
    removeButton.setIcon(removeIcon);
    removeButton.setToolTipText(mLocalizer.msg("removeChannelText", "Remove selected Channel"));
    removeButton.setHorizontalAlignment(SwingConstants.LEFT);
    removeButton.setIconTextGap(8);
    buttonPanel.add(removeButton);
    removeButton.setMargin(new Insets(1, 3, 1, 3));
    removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeButtonPressed(e);
      }
    });

    buttonPanel.add(Box.createRigidArea(new Dimension(0, removeButton.getInsets().top +removeButton.getIcon().getIconHeight()+removeButton.getInsets().bottom)));

    // Edit-Button
    JButton editButton = new JButton(Localizer.getLocalization(Localizer.I18N_EDIT));
    editButton.setIcon(editIcon);
    editButton.setToolTipText(mLocalizer.msg("editChannelText", "Edit selected Channel"));
    editButton.setHorizontalAlignment(SwingConstants.LEFT);
    editButton.setIconTextGap(8);
    buttonPanel.add(editButton);
    editButton.setMargin(new Insets(1, 3, 1, 3));
    editButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        editButtonPressed();
      }
    });

    JPanel buttonBar = new JPanel(new BorderLayout());
    buttonBar.add(buttonPanel,BorderLayout.NORTH);
    backgroundPanel.add(buttonBar,BorderLayout.EAST);

    this.add(backgroundPanel);
 }



  /**
   * Returns the current instance of the setting panel. If no instance is given, create a new one.
   * @param settings ; the settings of this data service
   * @return instance of setting panel
   */
  public static MixedDataServicePanel getInstance(MixedDataService service) {
    if (mInstance == null) {
      mInstance = new MixedDataServicePanel();
    }
    mInstance.mService=service;
    mInstance.channelDescriptions = new Properties();
    if (new File(mInstance.mService.mixedChannelsDirName + "/mixedChannels.properties").exists()) {
      try {
        InputStream reader = new FileInputStream(mInstance.mService.mixedChannelsDirName + "/mixedChannels.properties");
        mInstance.channelDescriptions.load(reader);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
   Channel[] actChannels = mInstance.mService.getAvailableChannels(null);
    mInstance.selModel.clear();
    for (int i = 0; i < actChannels.length; i++){
      if (actChannels[i] == null){
      }else{
      }

      int index = 0;
      boolean isSearching = true;
      while (isSearching && index < mInstance.selModel.size()){
        String nameAtIndex = mInstance.channelDescriptions.getProperty(mInstance.selModel.get(index).toString()).split(";",2)[0];
        if (nameAtIndex.trim().compareToIgnoreCase(actChannels[i].getName().trim())>0 ){
          isSearching = false;
        } else {
          index++;
        }
      }
      mInstance.selModel.add(index, actChannels[i].getId());
    }
    return mInstance;
  }


  public void ok() {

    // store channel settings
    try{
      channelDescriptions.store(new FileOutputStream(MixedDataService.getInstance().mixedChannelsDirName + "/mixedChannels.properties"), "Mixed Channels Descriptions");
    } catch (IOException e) {
    }


    // store channel icons

    String myChannelId;
    for (Enumeration<?> e = channelDescriptions.keys(); e.hasMoreElements();) {
      myChannelId = e.nextElement().toString();
      Channel alienChannel;
      String [] alienDescriptor = channelDescriptions.getProperty(myChannelId).split(";");
      if ("primary".equals(alienDescriptor[3])){
        alienChannel = HelperMethods.getChannelFromId(alienDescriptor[1], MixedDataService.getPluginManager().getSubscribedChannels());
      } else{
        if ("additional".equals(alienDescriptor[3])){
          alienChannel = HelperMethods.getChannelFromId(alienDescriptor[2], MixedDataService.getPluginManager().getSubscribedChannels());
        } else{
          alienChannel = null;
        }
      }
      String iconFilename = mService.mDataDir + "/icons/" + myChannelId + ".png";
      if (alienChannel != null){
        Icon icon = alienChannel.getIcon();
        if (icon == null){
          icon = new ChannelLabel(alienChannel).getIcon();
        }
        JbUtilities.storeIcon(icon, "png", iconFilename);
      } else {
        File iconFile = new File (iconFilename);
        iconFile.delete();
      }

    }
    Channel[] availableChannels = mService.getAvailableChannels(null);
    Channel[] subScribedChannels = MixedDataService.getPluginManager().getSubscribedChannels();
    for (int i = 0; i < subScribedChannels.length; i++){
      for (int j = 0; j < availableChannels.length; j++){
        if (subScribedChannels[i].getUniqueId().equals(availableChannels[j].getUniqueId())){
          subScribedChannels[i].setDefaultIcon(availableChannels[j].getDefaultIcon());
        }
      }
    }
}

  /**
   * Will be called if cancel was pressed
   * @since 2.7
   */
  public void cancel() {
    if (new File(mService.mixedChannelsDirName + "/mixedChannels.properties").exists()) {
      try {
        InputStream reader = new FileInputStream(mService.mixedChannelsDirName + "/mixedChannels.properties");
        channelDescriptions.load(reader);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }


  /**
   * create new channel
   */
  public void newButtonPressed(ActionEvent e) {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    int pos = selList.getModel().getSize();
    String id = sdf.format(cal.getTime());
    ChannelSettingDialog editDialog;
    Window parent = UiUtilities.getBestDialogParent(this);
    if (parent instanceof JFrame) {
      editDialog = ChannelSettingDialog.getInstance((JFrame) parent);
    } else {
      editDialog = ChannelSettingDialog.getInstance((JDialog) parent);
    }
    editDialog.showGui(id, channelDescriptions);
    String retValue = editDialog.readSettings();
    if (retValue != null){
      channelDescriptions.setProperty(id, retValue);
     selModel.add(pos, id);
    }

  }

  /**
   * edit channel
   */
  public void editButtonPressed() {
    String id = selList.getSelectedValue().toString();

    ChannelSettingDialog editDialog;
    Window parent = UiUtilities.getBestDialogParent(this);
    if (parent instanceof JFrame) {
      editDialog = ChannelSettingDialog.getInstance((JFrame) parent);
    } else {
      editDialog = ChannelSettingDialog.getInstance((JDialog) parent);
    }
    editDialog.showGui(id, channelDescriptions);
    String retValue = editDialog.readSettings();
    if (retValue != null){
      channelDescriptions.setProperty(id, retValue);
      selList.repaint();
    }

  }
  /**
   * remove channel
   */
  public void removeButtonPressed(ActionEvent e) {
    int pos = selList.getSelectedIndex();
    if (pos > -1){
      channelDescriptions.remove(selModel.getElementAt(pos).toString());
      selModel.remove(pos);
    }

  }
}



class ChannelCellRenderer extends DefaultListCellRenderer {

//private static final Logger mLog = java.util.logging.Logger.getLogger(ChannelCellRenderer.class.getName());

  private static final long serialVersionUID = 1L;

  public Component getListCellRendererComponent(JList list,
      Object value,
      int index,
      boolean isSelected,
      boolean hasFocus) {
    JLabel label =
      (JLabel)super.getListCellRendererComponent(list,
          value,
          index,
          isSelected,
          hasFocus);
    String [] channelDescriptor = MixedDataServicePanel.mInstance.channelDescriptions.getProperty(list.getModel().getElementAt(index).toString()).split(";");
    Channel channel;
    if ("primary".equals(channelDescriptor[3])){
      channel = HelperMethods.getChannelFromId(channelDescriptor[1], MixedDataService.getPluginManager().getSubscribedChannels());
    } else{
      if ("additional".equals(channelDescriptor[3])){
        channel = HelperMethods.getChannelFromId(channelDescriptor[2], MixedDataService.getPluginManager().getSubscribedChannels());
      } else{
        channel = null;
      }

    }
    if (channel == null){
      label.setIcon(new ImageIcon (getClass().getResource("icons/mixed.png")));
    } else{
      label.setIcon(new ChannelLabel(channel).getIcon());
    }
    label.setText(channelDescriptor[0]);

    return(label);
  }
}
