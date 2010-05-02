package sharedchanneldataservice;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import util.ui.ChannelLabel;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.Channel;
import devplugin.Plugin;


public class ChannelSettingDialog extends JDialog{

  private static final long serialVersionUID = 1L;

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannelSettingDialog.class);
//  private static final Logger mLog = java.util.logging.Logger.getLogger(ChannelSettingDialog.class.getName());

  private static ChannelSettingDialog mInstance;
  private String myServiceName = "sharedchanneldataservice.SharedChannelDataService";

  private JPanel basicPanel;
  private JPanel tablePanel;
  private JPanel tuningPanel;
  private JPanel editButtonPanel;

  public JTable selector;
  private String []selectorHeader = {mLocalizer.msg("time", "Start Time"),Localizer.getLocalization(Localizer.I18N_CHANNEL)};
  private String [][] selectorData;

  private String myId;
  private String myName;
  private int myIconIndex;
  private int myCategoryIndex;
  private int myWebpageIndex;
  private String myCountry;

  private boolean okPressed;

  private JLabel myChannelLabel;
  private JTextField myNameField;
  private JComboBox iconBox;
  private JList categoriesList;
  private JComboBox webBox;
  private JComboBox countryBox;

  private String []alienIds;
  private Channel [] selAlienChns;

  private Properties nxtvepgAlternatives;
  private Properties sharedChannelSources;
  private Properties mixedDataSources;

  /**
   * Create the Dialog
   * @param parent Parent-Frame
   */
  public ChannelSettingDialog(JFrame parent) {
    super(parent, true);
    createGui();
  }
  /**
   * Create the Dialog
   * @param parent Parent-Dialog
   */
  public ChannelSettingDialog(JDialog parent) {
    super(parent, true);
    createGui();
  }

  private void createGui(){

    setTitle(mLocalizer.msg("defineChannel", "Define Shared Channel"));

    ImageIcon newIcon = Plugin.getPluginManager().getIconFromTheme(null,"actions", "document-new", 16);
    ImageIcon editIcon = Plugin.getPluginManager().getIconFromTheme(null,"actions", "document-edit", 16);
    ImageIcon removeIcon = Plugin.getPluginManager().getIconFromTheme(null,"actions", "edit-delete", 16);

    JPanel backgroundPanel = (JPanel) getContentPane();
    backgroundPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.PAGE_AXIS));


    basicPanel = new JPanel();
    basicPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));


    JScrollPane bScroller = new JScrollPane(basicPanel);
    bScroller.setPreferredSize(new Dimension(280, 140));

    backgroundPanel.add(bScroller);

    tuningPanel = new JPanel(new BorderLayout(10, 0));


    tablePanel = new JPanel();
    tuningPanel.add(tablePanel, BorderLayout.CENTER);

    // tuning button Panel
    editButtonPanel = new JPanel ();
    editButtonPanel.setLayout(new GridLayout(0, 1, 0, 4));
    editButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));


    // New-Button
    JButton newButton =  new JButton(Localizer.getLocalization(Localizer.I18N_ADD));
    newButton.setIcon(newIcon);
    newButton.setToolTipText(mLocalizer.msg("createChannelText", "Create New Channel"));
    newButton.setHorizontalAlignment(SwingConstants.LEFT);
    newButton.setIconTextGap(8);
    editButtonPanel.add(newButton);
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
    editButtonPanel.add(removeButton);
    removeButton.setMargin(new Insets(1, 3, 1, 3));
    removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeButtonPressed(e);
      }
    });

    editButtonPanel.add(Box.createRigidArea(new Dimension(0, removeButton.getInsets().top +removeButton.getIcon().getIconHeight()+removeButton.getInsets().bottom)));


    // Edit-Button
    JButton editButton = new JButton(Localizer.getLocalization(Localizer.I18N_EDIT));
    editButton.setIcon(editIcon);
    editButton.setToolTipText(mLocalizer.msg("editChannelText", "Edit selected Channel"));
    editButton.setHorizontalAlignment(SwingConstants.LEFT);
    editButton.setIconTextGap(8);
    editButtonPanel.add(editButton);
    editButton.setMargin(new Insets(1, 3, 1, 3));
    editButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        editButtonPressed();
      }
    });


    JPanel buttonBar = new JPanel(new BorderLayout());
    buttonBar.add(editButtonPanel,BorderLayout.NORTH);
    tuningPanel.add(buttonBar,BorderLayout.EAST);

    JScrollPane tScroller = new JScrollPane(tuningPanel);
    backgroundPanel.add(tScroller);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));


    buttonPanel.add(Box.createHorizontalGlue());

    JButton cancelButton = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }

    });
    buttonPanel.add(cancelButton);

    JButton okButton = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed=true;
        setVisible(false);
      }
    });
    buttonPanel.add(okButton);

    backgroundPanel.add (buttonPanel);

    pack();
  }

  public void showGui (String id, Properties channelDescriptions){
    okPressed=false;
    boolean isEditing = true;

    String[] chnSetting;
    String descString = channelDescriptions.getProperty(id);
    if (descString==null){
      isEditing = false;
      descString = id + ";-1;1;-1;xs;";
    }

    chnSetting = descString.split(";",6);

    myId =id;
    myName=chnSetting[0];
    myIconIndex=Integer.parseInt((String)chnSetting[1]);
    myCategoryIndex = Integer.parseInt((String)chnSetting[2]);
    myWebpageIndex=Integer.parseInt((String)chnSetting[3]);
    myCountry=chnSetting[4];

    mixedDataSources = new Properties();
    String mixedDataSourcesFile = SharedChannelDataService.getInstance().mixedChannelsDirName + "/mixedChannels.properties";
    if (new File(mixedDataSourcesFile).exists()) {
      try {
        InputStream reader = new FileInputStream(mixedDataSourcesFile);
        mixedDataSources.load(reader);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    nxtvepgAlternatives = new Properties();
    String nxtvepgAlternativesFile = SharedChannelDataService.getInstance().mixedChannelsDirName + "/nxtvepgAlternatives.properties";
    if (new File(nxtvepgAlternativesFile).exists()) {
      try {
        InputStream reader = new FileInputStream(nxtvepgAlternativesFile);
        nxtvepgAlternatives.load(reader);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    sharedChannelSources = channelDescriptions;


    Channel[] subscribedList = SharedChannelDataService.getPluginManager().getSubscribedChannels();
    ArrayList <Channel> alienList = new ArrayList <Channel> ();
    for (int i = 0; i < subscribedList.length; i++) {
      String [] channelId = subscribedList[i].getUniqueId().split("_");
      if (!(("sharedchanneldataservice.SharedChannelDataService".equals(channelId[0])&& (myId.equals(channelId[3]) || isSharedAncestor(channelId[3])))||("nextviewdataservice.NextViewDataService".equals(channelId[0]) && isNxtvepgAncestor(channelId[3]))||("mixeddataservice.MixedDataService".equals(channelId[0]) && isMixedAncestor(channelId[3])))){
        int index = 0;
        boolean isSearching = true;
        while (isSearching && index < alienList.size()){
          String [] alienId = alienList.get(index).getUniqueId().split("_");
          if (alienList.get(index).getName().trim().compareToIgnoreCase(subscribedList[i].getName().trim())>0 ){
            isSearching = false;
          } else {
            if (alienList.get(index).getName().trim().compareToIgnoreCase(subscribedList[i].getName().trim())==0 && alienId[2].compareTo(channelId[2])>0){
              isSearching = false;
            }else {
              if (alienList.get(index).getName().trim().compareToIgnoreCase(subscribedList[i].getName().trim())==0 && alienId[2].compareTo(channelId[2])==0 && alienId[0].compareTo(channelId[0])>0){
                isSearching = false;
              }else {
                if (alienList.get(index).getName().trim().compareToIgnoreCase(subscribedList[i].getName().trim())==0 && alienId[2].compareTo(channelId[2])==0 && alienId[0].compareTo(channelId[0])==0 && alienId[1].compareTo(channelId[1])>0){
                  isSearching = false;
                } else{
                  index++;
                }
              }
            }
          }
        }
        alienList.add(index, subscribedList[i]);
      }
    }

    Channel[] alienSubscribed = alienList.toArray(new Channel[alienList.size()]);
    alienIds = new String [alienSubscribed.length];
    for (int i = 0; i < alienSubscribed.length; i++){
      alienIds[i] = alienSubscribed[i].getUniqueId();
    }

    basicPanel.removeAll();
    GridBagLayout basicLayout = new GridBagLayout();
    GridBagConstraints basicConstraints = new GridBagConstraints();
    basicPanel.setLayout(basicLayout);


    myChannelLabel = new JLabel ();
    basicConstraints = makegbc(0, 0, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicLayout.setConstraints(myChannelLabel, basicConstraints);
    basicPanel.add(myChannelLabel);

    JLabel idLabel = new JLabel ("ID: " + myId);
    basicConstraints = makegbc(1, 0, 1, 2);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicConstraints.fill = GridBagConstraints.HORIZONTAL;
    basicConstraints.gridwidth = GridBagConstraints.REMAINDER;
    basicLayout.setConstraints(idLabel, basicConstraints);
    basicPanel.add(idLabel);

    JLabel nameLabel = new JLabel (mLocalizer.msg("name", "Channel Name"));
    basicConstraints = makegbc(0, 1, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicLayout.setConstraints(nameLabel, basicConstraints);
    basicPanel.add(nameLabel);

    myNameField = new JTextField (myName);
    basicConstraints = makegbc(1, 1, 2, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicConstraints.fill = GridBagConstraints.HORIZONTAL;
    basicConstraints.gridwidth = GridBagConstraints.REMAINDER;
    basicLayout.setConstraints(myNameField, basicConstraints);
    basicPanel.add(myNameField);


    updateGui (id, chnSetting[5]);


    String []chnDescSplit =chnSetting[5].split(";");
   selAlienChns = new Channel [chnDescSplit.length/2];
    for (int i = 0; i< selAlienChns.length; i++){
       selAlienChns[i]= HelperMethods.getChannelFromId(chnDescSplit[(i*2)+1], subscribedList);
    }

    JLabel iconLabel = new JLabel (mLocalizer.msg("channelIcon", "Channel Icon"));
    basicConstraints = makegbc(0, 2, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicLayout.setConstraints(iconLabel, basicConstraints);
    basicPanel.add(iconLabel);

   String[]chnNames = new String [selAlienChns.length+1];
   chnNames[0] = mLocalizer.msg("none", "n.n");
    for (int i = 0; i< selAlienChns.length; i++){
       if (selAlienChns[i]!=null){
        chnNames[i+1]=selAlienChns[i].getName();
      } else {
       chnNames[i+1]=mLocalizer.msg("none", "n.n");
      }
    }

    iconBox = new JComboBox(chnNames);
    iconBox.setSelectedIndex(myIconIndex+1);
    if (myIconIndex >=0 && selAlienChns[myIconIndex]!=null){
      ChannelLabel alienLabel = new ChannelLabel ();
      alienLabel.setChannel(selAlienChns[myIconIndex]);
      myChannelLabel.setIcon(alienLabel.getIcon());
    }
    else{
      myChannelLabel.setIcon(new ImageIcon (getClass().getResource("icons/shared.png")));
    }

    iconBox.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ){
        JComboBox cb = (JComboBox) e.getSource();
        Icon newIcon = null;
        if (cb.getSelectedIndex()>0){
          ChannelLabel alienLabel = new ChannelLabel ();
          Channel channel = HelperMethods.getChannel(selectorData[cb.getSelectedIndex()-1][1].split("_"));
          if (channel!=null) {
            alienLabel.setChannel(channel);
            newIcon = alienLabel.getIcon();
          }
        }
        if (newIcon == null){
          newIcon = new ImageIcon (getClass().getResource("icons/shared.png"));
        }
        myChannelLabel.setIcon(newIcon);
      }
    });


    basicConstraints = makegbc(1, 2, 2, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicConstraints.fill = GridBagConstraints.HORIZONTAL;
    basicConstraints.gridwidth = GridBagConstraints.REMAINDER;
    basicLayout.setConstraints(iconBox, basicConstraints);
    basicPanel.add(iconBox);

    JLabel webLabel = new JLabel (mLocalizer.msg("webPage", "Web Page"));
    basicConstraints = makegbc(0, 3, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicLayout.setConstraints(webLabel, basicConstraints);
    basicPanel.add(webLabel);

    webBox = new JComboBox(chnNames);
    webBox.setSelectedIndex(myWebpageIndex+1);
    basicConstraints = makegbc(1, 3, 2, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicConstraints.fill = GridBagConstraints.HORIZONTAL;
    basicConstraints.gridwidth = GridBagConstraints.REMAINDER;
    basicLayout.setConstraints(webBox, basicConstraints);
    basicPanel.add(webBox);

    JLabel categoriesLabel = new JLabel (mLocalizer.msg("categories", "Categories"));
    basicConstraints = makegbc(0, 4, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicLayout.setConstraints(categoriesLabel, basicConstraints);
    basicPanel.add(categoriesLabel);

    String []categories = new String [12];
    int [] myCategories = getMyCategories ();
    for (int i = 0; i < categories.length; i++){
      categories[i] = mLocalizer.msg(Integer.toString(i), "Category " + i);
    }
    categoriesList = new JList(categories);
    categoriesList.setSelectedIndices(myCategories);
    categoriesList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent evt) {
        if (categoriesList.getSelectedIndex()==0){
          if (evt.getFirstIndex()==0){
            myCategoryIndex=0;
            categoriesList.setSelectedIndex(0);
          }
          categoriesList.repaint();
        } else {
          myCategoryIndex=0;
          int [] selectedIndices = categoriesList.getSelectedIndices();
          for (int i = 0; i<selectedIndices.length; i++){
            myCategoryIndex = myCategoryIndex + (1<< (selectedIndices[i]-1));
          }

        }
      }
    });
    basicConstraints = makegbc(1, 4, 2, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicConstraints.fill = GridBagConstraints.HORIZONTAL;
    basicConstraints.gridwidth = GridBagConstraints.REMAINDER;
    basicLayout.setConstraints(categoriesList, basicConstraints);
    basicPanel.add(categoriesList);


    JLabel countryLabel = new JLabel (mLocalizer.msg("countryCode", "Country Code"));
    basicConstraints = makegbc(0, 5, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicLayout.setConstraints(countryLabel, basicConstraints);
    basicPanel.add(countryLabel);

     countryBox = new JComboBox(new String[]{myCountry});
    countryBox.setEditable(true);
    countryBox.setSelectedIndex(0);
    basicConstraints = makegbc(1, 5, 2, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicConstraints.fill = GridBagConstraints.HORIZONTAL;
    basicConstraints.gridwidth = GridBagConstraints.REMAINDER;
    basicLayout.setConstraints(countryBox, basicConstraints);
    basicPanel.add(countryBox);

    if (isEditing){
      countryLabel.setVisible(false);
      countryBox.setVisible(false);
    }


    basicPanel.repaint();
    myNameField.requestFocus();
    myNameField.selectAll();
     pack();
    UiUtilities.centerAndShow(mInstance);
  }


  public void updateGui (String id, String alienString){


    tablePanel.removeAll();
    String[]alienDesc;
    if (alienString.equals("")){
      alienDesc = new String[0];
    }else {
      alienDesc = alienString.split(";");
    }

    int rowCount = alienDesc.length/2;
   selectorData = new String [rowCount][2];
    for (int i = 0; i < rowCount;i++){
      selectorData[i][0]=alienDesc[i*2];
      selectorData[i][1]=alienDesc[(i*2)+1];
    }
    selector = new JTable( selectorData, selectorHeader ){
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    selector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    SharedTableCellRenderer render = new SharedTableCellRenderer();
    for (int i = 0; i < 2; i++) {
      selector.getColumnModel().getColumn(i).setCellRenderer(render);
    }
    selector.getTableHeader().setDefaultRenderer(new SharedHeaderRenderer());
    selector.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent arg0) {
      }
    });
    selector.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
          e.consume();
          editButtonPressed();
        }
      }
    });

    JScrollPane sScrollPane = new JScrollPane(selector);
    sizeColumnsToFit(sScrollPane, selector.getRowCount());
    selector.setRowHeight(28);
    int vScrollBarWidth = sScrollPane.getVerticalScrollBar().getPreferredSize().width;
    int headerHeight = selector.getTableHeader().getPreferredSize().height +6;
    Dimension dimTable = selector.getPreferredSize();
    dimTable.setSize(Math.min(280, dimTable.getWidth() + vScrollBarWidth), Math.min(280, dimTable.getHeight()+ headerHeight));
    sScrollPane.setPreferredSize(new Dimension(dimTable.width, dimTable.height));
    tablePanel.add(sScrollPane);
  }




  /**
   * insert channel
   */
  public void newButtonPressed(ActionEvent e) {
    ChannelChooserDialog dialog;
    Window parent = UiUtilities.getBestDialogParent(mInstance);
    if (parent instanceof JFrame) {
      dialog = new ChannelChooserDialog((JFrame)parent);
    } else {
      dialog = new ChannelChooserDialog((JDialog)parent);
    }
    dialog.createGui(myNameField.getText(), myChannelLabel.getIcon(), alienIds, "","00:00");
    String channelId = dialog.getSelectedChannel();
    String startTime = dialog.getSelectedTime();
    if (channelId != null&& startTime!=null && channelId.length()>0 ) {
      addChannel (channelId, startTime, false, false);
      pack();
      tablePanel.repaint();
    }

  }
  /**
   * edit channel
   */
  public void editButtonPressed() {
    int index = selector.getSelectedRow();
    ChannelChooserDialog dialog;
    Window parent = UiUtilities.getBestDialogParent(mInstance);
    if (parent instanceof JFrame) {
      dialog = new ChannelChooserDialog((JFrame)parent);
    } else {
      dialog = new ChannelChooserDialog((JDialog)parent);
    }
    dialog.createGui(myNameField.getText(), myChannelLabel.getIcon(), alienIds, selectorData[index][1],selectorData[index][0]);
    String channelId = dialog.getSelectedChannel();
    String startTime = dialog.getSelectedTime();
    if (channelId != null&& startTime!=null && channelId.length()>0 ) {
      boolean isNewIconIndex = iconBox.getSelectedIndex()== index+1;
      boolean isNewWebIndex = webBox.getSelectedIndex()== index+1;
      removeChannel (index);
      addChannel (channelId, startTime, isNewIconIndex, isNewWebIndex);
      pack();
      tablePanel.repaint();
    }

  }


  public void removeButtonPressed(ActionEvent e) {
    int index = selector.getSelectedRow();
    if (index >-1){
      removeChannel (index);
      pack();
      tablePanel.repaint();
    }
  }

  /**
   * remove channel
   */
  public void removeChannel(int index){

    StringBuffer newAlien = new StringBuffer();
    int iconIndex = iconBox.getSelectedIndex();
    int webIndex = webBox.getSelectedIndex();

    String [] newChnNames = new String [iconBox.getItemCount()-1];
    newChnNames[0] = iconBox.getItemAt(0).toString();

    if (selector.getRowCount()>1) {
     for (int i = 0; i < index; i++) {
        newAlien.append(";" + selectorData[i][0] + ";" + selectorData[i][1]);
        newChnNames[i+1] = iconBox.getItemAt(i+1).toString();
      }

      if (index == iconIndex-1){
        iconIndex=0;
      }
      if (index == webIndex-1){
        webIndex=0;
      }
     if (index < iconIndex-1){
        iconIndex--;
      }
      if (index < webIndex-1){
        webIndex--;
      }

      for (int i = index + 1; i < selector.getRowCount(); i++) {
        newAlien.append(";" + selectorData[i][0] + ";" + selectorData[i][1]);
        newChnNames[i] = iconBox.getItemAt(i+1).toString();
     }

    } else {
      iconIndex=0;
      webIndex=0;
     newAlien.append(";");
    }
    updateGui(myId, newAlien.substring(1));

    iconBox.setModel(new DefaultComboBoxModel(newChnNames));
    iconBox.setSelectedIndex(iconIndex);
    webBox.setModel(new DefaultComboBoxModel(newChnNames));
    webBox.setSelectedIndex(webIndex);

  }

  private void addChannel (String channelId, String startTime, boolean isNewIconIndex, boolean isNewWebIndex){
    StringBuffer newAlien = new StringBuffer();
    int index = 0;
    int iconIndex = iconBox.getSelectedIndex();
    int webIndex = webBox.getSelectedIndex();


    String [] newChnNames = new String [iconBox.getItemCount()+1];
    newChnNames[0] = iconBox.getItemAt(0).toString();
    for (index = 0; index<selector.getRowCount(); index++){
      if (selectorData[index][0].compareTo(startTime)<=0){
        newAlien.append (";" + selectorData[index][0]+ ";" +selectorData[index][1]);
        newChnNames[index+1] = iconBox.getItemAt(index+1).toString();
      } else {
        break;
      }
    }

     newAlien.append (";" + startTime+ ";" +channelId);

    if (isNewIconIndex) {
      iconIndex = index+1;
     } else {
      if (index < iconIndex) {
        iconIndex++;
      }
    }
    if (isNewWebIndex) {
      webIndex = index+1;
    } else {
      if (index < webIndex) {
        webIndex++;
      }
    }
    Channel channel = HelperMethods.getChannel(channelId.split("_"));
    if (channel!=null){
      newChnNames[index+1]=channel.getName();
    } else {
      newChnNames[index+1]=mLocalizer.msg("none", "n.n");
    }
    for (int i = index; i<selector.getRowCount(); i++){
      newAlien.append (";" + selectorData[i][0]+ ";" +selectorData[i][1]);
      newChnNames[i+2] = iconBox.getItemAt(i+1).toString();
    }

    updateGui (myId, newAlien.substring(1));
    iconBox.setModel(new DefaultComboBoxModel(newChnNames));
    iconBox.setSelectedIndex(iconIndex);
    webBox.setModel(new DefaultComboBoxModel(newChnNames));
    webBox.setSelectedIndex(webIndex);

    if (channel!=null && countryBox.isVisible()) {
      String newCCode = channel.getCountry();
      if (newCCode!=null && newCCode.length()==2) {
        int count;
        for (count = 0; count < countryBox.getItemCount(); count++) {
          if (((String) countryBox.getItemAt(count)).equals(newCCode)) {
            break;
          }
        }
        if (count == countryBox.getItemCount()) {
          countryBox.addItem(newCCode);
        }
      }
    }

  }

  private void sizeColumnsToFit(JScrollPane tScroll, int maxRows) {

    TableColumn col = selector.getColumnModel().getColumn(0);
    int prefSize =  selector.getFontMetrics(selector.getFont()).stringWidth(selectorHeader[0]);
    for (int i=0; i< maxRows; i++){
      int test = selector.getFontMetrics(selector.getFont()).stringWidth(selectorData[i][0]);
      if (prefSize<test){
        prefSize = test;
      }
      col.setPreferredWidth(prefSize+24);
      col.setWidth(prefSize+24);
    }
    col = selector.getColumnModel().getColumn(1);
    prefSize =  selector.getFontMetrics(selector.getFont()).stringWidth(selectorHeader[1]);
    for (int i=0; i< maxRows; i++){
      Channel chn = HelperMethods.getChannelFromId(selectorData[i][1], SharedChannelDataService.getPluginManager().getSubscribedChannels());
      int test = new ChannelLabel(chn).getIcon().getIconWidth() + selector.getFontMetrics(selector.getFont()).stringWidth(chn.getName());
      if (prefSize<test){
        prefSize = test;
      }
      col.setPreferredWidth(prefSize+24);
  col.setWidth(prefSize+24);
    }
    }

  public int [] getMyCategories (){
    boolean [] hasCategory = new boolean [12];
    int tempIndex = myCategoryIndex;
    int counter = 0;
    for (int i=10; i>=0; i--){
      if (tempIndex>=(1<<i)){
        hasCategory[i]= true;
        tempIndex = tempIndex-(1<<i);
        counter++;
      }
      else{
        hasCategory[i]= false;
        }
      }

    int [] categories;
    if (counter < 1){
      categories = new int [1];
      categories[0] = 0;
    } else{
      categories = new int[counter];
      counter = 0;
      for (int i = 0; i< hasCategory.length; i++){
        if (hasCategory[i]){
          categories[counter] = i+1;
          counter++;
        }
      }
    }
    return categories;
  }

  public String readSettings(){

    if (!okPressed){
      return null;
    }

    StringBuffer buffer = new StringBuffer(myNameField.getText().replaceAll(";", ","));

    buffer.append(";" + (iconBox.getSelectedIndex()-1));
    buffer.append(";" + myCategoryIndex);
    buffer.append(";" + (webBox.getSelectedIndex()-1));

    myCountry = ((String)countryBox.getSelectedItem()).trim().toLowerCase();
    if (!(myCountry.length()==2 && myCountry.matches("[a-z]+"))){
      myCountry = "xx";
    }
    buffer.append(";" + myCountry);
    if (selector.getRowCount()==0 ) {
      buffer.append(";");
      }else{
      for (int i = 0; i < selector.getRowCount(); i++) {
        buffer.append(";" + selectorData[i][0] + ";" + selectorData[i][1]);
      }
    }
    return buffer.toString();
  }
  private boolean isNxtvepgAncestor (String testId){
    boolean retValue = false;
    if (nxtvepgAlternatives.getProperty(testId)!=null){
      String [] propValue = nxtvepgAlternatives.getProperty(testId).split(";");
      if (propValue[2].startsWith(myServiceName) && propValue[2].endsWith(myId)){
        retValue = true;
      }else {
        String [] channelId = propValue[2].split("_");
        retValue = retValue ||("mixeddataservice.MixedDataService".equals(channelId[0])&& isMixedAncestor(channelId[3]))||("nextviewdataservice.NextViewDataService".equals(channelId[0]) && isNxtvepgAncestor(channelId[3]))||("sharedchannelservice.SharedChannelService".equals(channelId[0]) && isSharedAncestor(channelId[3]));
      }
    }
    return retValue;
  }

  private boolean isMixedAncestor (String testId){
    boolean retValue = false;
    if (mixedDataSources.getProperty(testId)!=null){
      String [] propValue = mixedDataSources.getProperty(testId).split(";");
      for (int i =1; i<3; i++){
        if (propValue[i].startsWith(myServiceName) && propValue[i].endsWith(myId)){
          retValue = true;
        } else {
          String [] channelId = propValue[i].split("_");
          retValue = retValue ||("mixeddataservice.MixedDataService".equals(channelId[0])&& isMixedAncestor(channelId[3]))||("nextviewdataservice.NextViewDataService".equals(channelId[0]) && isNxtvepgAncestor(channelId[3]))||("sharedchannelservice.SharedChannelService".equals(channelId[0]) && isSharedAncestor(channelId[3]));
        }
      }
    }
    return retValue;
  }


  private boolean isSharedAncestor (String testId){
    boolean retValue = false;
    if (sharedChannelSources.getProperty(testId)!=null){
      String [] propValue = sharedChannelSources.getProperty(testId).split(";");
      for (int i =1; i< propValue.length; i++){
       if (propValue[i].startsWith(myServiceName) && propValue[i].endsWith(myId)){
             retValue = true;
        }else {
          String [] channelId = propValue[i].split("_");
          if (channelId.length==4) {
            retValue = retValue || ("mixeddataservice.MixedDataService".equals(channelId[0]) && isMixedAncestor(channelId[3])) || ("nextviewdataservice.NextViewDataService".equals(channelId[0]) && isNxtvepgAncestor(channelId[3])) || ("sharedchannelservice.SharedChannelService".equals(channelId[0]) && isSharedAncestor(channelId[3]));
          }
        }
      }
    }
    return retValue;
  }


  /* makegbc
   * helper function for the GridBagLayout */
  private static GridBagConstraints makegbc(
      int x, int y, int width, int height) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = width;
    gbc.gridheight = height;
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    return gbc;
  }
  /**
   * Returns the current instance of this panel. If no instance is given, create a new one.
   * @return instance of setting panel
   */
  public static ChannelSettingDialog getInstance(JDialog parent) {
    if (mInstance == null || mInstance.getParent()instanceof JFrame) {
      mInstance = new ChannelSettingDialog(parent);
    }
    return mInstance;
  }
  /**
   * Returns the current instance of this panel. If no instance is given, create a new one.
   * @return instance of setting panel
   */
  public static ChannelSettingDialog getInstance(JFrame parent) {
    if (mInstance == null || mInstance.getParent()instanceof JDialog) {
      mInstance = new ChannelSettingDialog(parent);
    }
    return mInstance;
  }
}




class SharedHeaderRenderer extends JPanel implements TableCellRenderer {
  private static final long serialVersionUID = 1L;
  private JLabel label = new JLabel();



  public SharedHeaderRenderer(){
    super(new GridLayout(1, 1));
    this.add(label);
  }



  public Component getTableCellRendererComponent(JTable table,
      Object value,
      boolean isSelected,
      boolean hasFocus,
      int row, int column) {
    label.setText(value.toString());
    label.setFont(table.getFont());
    setBorder(new EmptyBorder(0, 0, 0, 0));
    Border border = new LineBorder(Color.BLACK);
    Border margin;
    label.setHorizontalAlignment(SwingConstants.LEFT);
    margin = new EmptyBorder(0, 7, 0, 4);
    label.setBorder(new CompoundBorder(border, margin));


    setOpaque(true);

    return this;
  }
}


class SharedTableCellRenderer extends DefaultTableCellRenderer {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  //private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(StatTableCellRenderer.class.getName());

  public SharedTableCellRenderer(){

  }
  public java.awt.Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ){

    JLabel label =
      (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column );

    setBorder(new EmptyBorder(4,8,4,4));
    setHorizontalAlignment(JLabel.LEFT);

    if (column == 0){
      label.setIcon(null);
    }else{
      String alienId = (String)value;
      Channel channel = HelperMethods.getChannelFromId(alienId, SharedChannelDataService.getPluginManager().getSubscribedChannels());
      if (channel != null) {
        label.setIcon(new ChannelLabel(channel).getIcon());
        label.setText(channel.getName());
      } else {
        label.setIcon(new ImageIcon (getClass().getResource("icons/shared.png")));
      }
    }
    return(label);

  }
}

