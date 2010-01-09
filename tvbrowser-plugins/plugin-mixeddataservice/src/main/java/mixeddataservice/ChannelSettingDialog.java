package mixeddataservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Properties;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import util.ui.ChannelLabel;
import util.ui.Localizer;
import util.ui.UiUtilities;

import devplugin.Channel;



public class ChannelSettingDialog extends JDialog{

  private static final long serialVersionUID = 1L;

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannelSettingDialog.class);
  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(ChannelSettingDialog.class.getName());

  private static ChannelSettingDialog mInstance;

  private JPanel basicPanel;

  private JLabel[]fieldLabel;
  private String[] fieldName;
  private int[] fieldIndex;
  private JComboBox[] fieldSetting;

  private JLabel myChannelLabel;
  private JTextField myNameField;
  private JComboBox iconBox;
  private JComboBox categoriesBox;
  private JComboBox webBox;
  private JComboBox countryBox;

  private int numberOfFields;
  private String [] detailMix;
  private ChannelLabel chn1Label;
  private ChannelLabel chn2Label;

  private String myServiceName = "mixeddataservice.MixedDataService";
  private String defaultMix = "primary;primary;primary;primary;primary;after;primary;primary;after;mix;primary;primary;after;primary;primary;primary;primary;primary;primary;primary;primary;primary;primary;primary;primary;primary;primary;primary;primary;primary";
  private String myId;
  private String id1;
  private String id2;
  private String myName;
  private String myIcon;
  private String myCategories;
  private String myWebpage;
  private String myCountry;
  private boolean okPressed;
  public boolean countryCodeFixed;

  private String alienIds[];
 
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

    setTitle(mLocalizer.msg("editChannel", "Edit Details"));

    JPanel backgroundPanel = (JPanel) getContentPane();
    backgroundPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.PAGE_AXIS));    


    basicPanel = new JPanel();
    basicPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));


    JScrollPane bScroller = new JScrollPane(basicPanel);
    bScroller.setPreferredSize(new Dimension(400, 240));

    backgroundPanel.add(bScroller);

    JPanel tuningPanel = new JPanel();
    GridBagLayout tuningLayout = new GridBagLayout();
    GridBagConstraints tuningConstraints = new GridBagConstraints();
    tuningPanel.setLayout(tuningLayout);

    String fieldsFileName = "files/fields.properties";
    final InputStream stream = getClass().getResourceAsStream(fieldsFileName);
    Properties fieldsProp = new Properties();
    try {
      fieldsProp.load(stream);
    } catch (IOException ioe) {
      mLog.warning(ioe.toString());
    }

    numberOfFields=fieldsProp.size();
    fieldLabel = new JLabel[numberOfFields];
    fieldName = new String [numberOfFields];
    fieldSetting = new JComboBox[numberOfFields];
    fieldIndex = new int[numberOfFields];

    for (int i=0; i< numberOfFields; i++) {
      String fieldDesc[]= fieldsProp.getProperty(Integer.toString(i), "-1").split(";");
      fieldIndex [i] = Integer.parseInt((String) fieldDesc[0]);
      fieldName [i]= mLocalizer.msg(fieldDesc[1], fieldDesc[1]);
      fieldLabel[i]= new JLabel(fieldName[i]);
      tuningConstraints = makegbc(0, i, 1, 1);
      tuningConstraints.anchor = GridBagConstraints.WEST;
      tuningLayout.setConstraints(fieldLabel[i], tuningConstraints);
      tuningPanel.add(fieldLabel[i]);

      String []items = new String[]{mLocalizer.msg("primary", "primary"), mLocalizer.msg("additional", "additional")};
      if (fieldIndex[i]==13){
        items = new String[]{mLocalizer.msg("primary", "primary"), mLocalizer.msg("additional", "additional"), mLocalizer.msg("add", "add")};
      }
      if (fieldIndex[i]==7||fieldIndex[i]==8||fieldIndex[i]==12||fieldIndex[i]==16){
        items = new String[]{mLocalizer.msg("primary", "primary"), mLocalizer.msg("additional", "additional"), mLocalizer.msg("before", "before"), mLocalizer.msg("after", "after")};
      }

      fieldSetting[i]= new JComboBox(items);
      tuningConstraints = makegbc(1, i, 1, 1);
      tuningConstraints.fill = GridBagConstraints.HORIZONTAL;
      tuningConstraints.gridwidth = GridBagConstraints.REMAINDER;
      tuningLayout.setConstraints(fieldSetting[i], tuningConstraints);
      fieldSetting[i].setEditable(false);
      tuningPanel.add(fieldSetting[i]);
    }

    JScrollPane tScroller = new JScrollPane(tuningPanel);
    tScroller.setPreferredSize(new Dimension(400, 280));
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
    countryCodeFixed = false;

    String[] chnSetting;
    String descString = channelDescriptions.getProperty(id);
    if (descString==null){
      isEditing = false;
      descString = id + ";;;;;;xx;" + defaultMix;
    } 

    chnSetting = descString.split(";",8);

    myId =id;
    myName=chnSetting[0];
    id1=chnSetting[1];
    id2=chnSetting[2];
    myIcon=chnSetting[3];
    myCategories = chnSetting[4];
    myWebpage=chnSetting[5];
    myCountry=chnSetting[6];
    detailMix = chnSetting[7].split(";");

    mixedDataSources = channelDescriptions;
    nxtvepgAlternatives = new Properties();
    String nxtvepgAlternativesFile = MixedDataService.getInstance().mixedChannelsDirName + "/nxtvepgAlternatives.properties";
    if (new File(nxtvepgAlternativesFile).exists()) {
      try {
        InputStream reader = new FileInputStream(nxtvepgAlternativesFile);
        nxtvepgAlternatives.load(reader);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    sharedChannelSources = new Properties();
    String sharedChannelSourcesFile = MixedDataService.getInstance().mixedChannelsDirName + "/sharedChannels.properties";
    if (new File(sharedChannelSourcesFile).exists()) {
      try {
        InputStream reader = new FileInputStream(sharedChannelSourcesFile);
        sharedChannelSources.load(reader);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    Channel[] subscribedList = MixedDataService.getPluginManager().getSubscribedChannels();
    ArrayList <Channel> alienList = new ArrayList <Channel> ();
    for (int i = 0; i < subscribedList.length; i++) {
      String [] channelId = subscribedList[i].getUniqueId().split("_");
      if (!(("mixeddataservice.MixedDataService".equals(channelId[0])&& (myId.equals(channelId[3]) || isMixedAncestor(channelId[3])))||("nextviewdataservice.NextViewDataService".equals(channelId[0]) && isNxtvepgAncestor(channelId[3]))||("sharedchannelservice.SharedChannelService".equals(channelId[0]) && isSharedAncestor(channelId[3])))){
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

    Channel channel1 = HelperMethods.getChannelFromId(id1, alienSubscribed);
    Channel channel2 = HelperMethods.getChannelFromId(id2, alienSubscribed);

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

    JLabel chn1LabelText = new JLabel (mLocalizer.msg("primary", "Primary Source"));
    basicConstraints = makegbc(0, 2, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicLayout.setConstraints(chn1LabelText, basicConstraints);
    basicPanel.add(chn1LabelText);

    chn1Label = new ChannelLabel(true,true);
    if (channel1!=null){
      chn1Label.setChannel(channel1); 
    }
    chn1Label.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
          e.consume();
          selectChannel (chn1Label, 1);
        }
      }
    });
    basicConstraints = makegbc(1, 2, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicLayout.setConstraints(chn1Label, basicConstraints);
    basicPanel.add(chn1Label);

    JButton chn1Edit = new JButton();
    chn1Edit.setText(Localizer.getLocalization(Localizer.I18N_SELECT));
    chn1Edit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectChannel (chn1Label, 1);
      }
    });
    basicConstraints = makegbc(2, 2, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicConstraints.fill = GridBagConstraints.HORIZONTAL;
    basicConstraints.gridwidth = GridBagConstraints.REMAINDER;
    basicLayout.setConstraints(chn1Edit, basicConstraints);
    basicPanel.add(chn1Edit);

    JLabel chn2LabelText = new JLabel (mLocalizer.msg("additional", "Additional Source"));
    basicConstraints = makegbc(0, 3, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicLayout.setConstraints(chn2LabelText, basicConstraints);
    basicPanel.add(chn2LabelText);

    chn2Label = new ChannelLabel(true, true);
    if (channel2!=null){
      chn2Label.setChannel(channel2); 
    }
    chn2Label.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
          e.consume();
          selectChannel (chn2Label, 2);
        }
      }
    });
    basicConstraints = makegbc(1, 3, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicLayout.setConstraints(chn2Label, basicConstraints);
    basicPanel.add(chn2Label);

    JButton chn2Edit = new JButton();
    chn2Edit.setText(Localizer.getLocalization(Localizer.I18N_SELECT));
    chn2Edit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectChannel (chn2Label, 2);
      }
    });    
    basicConstraints = makegbc(2, 3, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicConstraints.fill = GridBagConstraints.HORIZONTAL;
    basicConstraints.gridwidth = GridBagConstraints.REMAINDER;
    basicLayout.setConstraints(chn2Edit, basicConstraints);
    basicPanel.add(chn2Edit);


    String []items2 = new String[]{mLocalizer.msg("primary", "primary"), mLocalizer.msg("additional", "additional")};
    String []items3 = new String[]{Localizer.getLocalization(Localizer.I18N_STANDARD), mLocalizer.msg("primary", "primary"), mLocalizer.msg("additional", "additional")};

    JLabel iconLabel = new JLabel (mLocalizer.msg("channelIcon", "Channel Icon"));
    basicConstraints = makegbc(0, 4, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicLayout.setConstraints(iconLabel, basicConstraints);
    basicPanel.add(iconLabel);

    iconBox = new JComboBox(items3);
    if (myIcon.equals("primary")&& channel1!=null){
      myChannelLabel.setIcon(chn1Label.getIcon());
      iconBox.setSelectedIndex(1);
    }
    else{
      if (myIcon.equals("additional")&& channel2!=null){
        myChannelLabel.setIcon(chn2Label.getIcon());
        iconBox.setSelectedIndex(2);
      }
      else{
        myChannelLabel.setIcon(new ImageIcon (getClass().getResource("icons/mixed.png")));
        iconBox.setSelectedIndex(0);
      }
    }

    iconBox.addActionListener( new ActionListener() { 
      public void actionPerformed( ActionEvent e ){
        JComboBox cb = (JComboBox) e.getSource();
        if (cb.getSelectedIndex()==2 && id2.length()>0){
          myChannelLabel.setIcon(chn2Label.getIcon());
        } else {
          if (cb.getSelectedIndex()==1 && id1.length()>0){
            myChannelLabel.setIcon(chn1Label.getIcon());
          } else{
            myChannelLabel.setIcon(new ImageIcon (getClass().getResource("icons/mixed.png")));
          }
        }
      }
    });


    basicConstraints = makegbc(1, 4, 2, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicConstraints.fill = GridBagConstraints.HORIZONTAL;
    basicConstraints.gridwidth = GridBagConstraints.REMAINDER;
    basicLayout.setConstraints(iconBox, basicConstraints);
    basicPanel.add(iconBox);

    JLabel webLabel = new JLabel (mLocalizer.msg("webPage", "Web Page"));
    basicConstraints = makegbc(0, 5, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicLayout.setConstraints(webLabel, basicConstraints);
    basicPanel.add(webLabel);

    webBox = new JComboBox(items2);
    if (myWebpage.equals("additional")){
      webBox.setSelectedIndex(1);
    }
    else{
      webBox.setSelectedIndex(0);
    }
    basicConstraints = makegbc(1, 5, 2, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicConstraints.fill = GridBagConstraints.HORIZONTAL;
    basicConstraints.gridwidth = GridBagConstraints.REMAINDER;
    basicLayout.setConstraints(webBox, basicConstraints);
    basicPanel.add(webBox);

    JLabel categoriesLabel = new JLabel (mLocalizer.msg("category", "Category"));
    basicConstraints = makegbc(0, 6, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicLayout.setConstraints(categoriesLabel, basicConstraints);
    basicPanel.add(categoriesLabel);

    categoriesBox = new JComboBox(items2);
    if (myCategories.equals("additional")){
      categoriesBox.setSelectedIndex(1);
    }
    else{
      categoriesBox.setSelectedIndex(0);
    }
    basicConstraints = makegbc(1, 6, 2, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicConstraints.fill = GridBagConstraints.HORIZONTAL;
    basicConstraints.gridwidth = GridBagConstraints.REMAINDER;
    basicLayout.setConstraints(categoriesBox, basicConstraints);
    basicPanel.add(categoriesBox);

    JLabel countryLabel = new JLabel (mLocalizer.msg("countryCode", "Country Code"));
    basicConstraints = makegbc(0, 7, 1, 1);
    basicConstraints.insets = new Insets(10, 10, 10, 10);
    basicLayout.setConstraints(countryLabel, basicConstraints);
    basicPanel.add(countryLabel);

    countryBox = new JComboBox(new String[]{myCountry});
    countryBox.setEditable(true);
    countryBox.setSelectedIndex(0);
    ActionListener countryBoxListener = new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        countryCodeFixed = true;
      }
    };
    countryBox.addActionListener(countryBoxListener);
    basicConstraints = makegbc(1, 7, 2, 1);
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
    setBoxes ();
    myNameField.requestFocus();
    myNameField.selectAll();
    UiUtilities.centerAndShow(mInstance);
  }

  private void selectChannel (ChannelLabel label, int index){
    ChannelChooserDialog dialog;
    Window parent = UiUtilities.getBestDialogParent(mInstance);
    if (parent instanceof JFrame) {
      dialog = new ChannelChooserDialog((JFrame)parent);
    } else {
      dialog = new ChannelChooserDialog((JDialog)parent);
    }
    String id;
    if (index == 1){
      id = id1;
    } else {
      id = id2;
    }
    dialog.createGui(myNameField.getText(), myChannelLabel.getIcon(), alienIds, id);
    String result = dialog.getSelectedChannel();
    if (result != null){
      if (index == 1){
        id1 = result;
      } else {
        id2 = result;
      }
    }
    Channel channel = HelperMethods.getChannel(result.split("_"));
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
        if (!countryCodeFixed && (index==1 || id1.length()==0 || !((String)countryBox.getSelectedItem()).equals(HelperMethods.getChannel(id1.split("_")).getCountry()))) {
          countryBox.setSelectedIndex(count);
          countryCodeFixed= false;
        }
      }
    }
    label.setChannel(channel);
    if (iconBox.getSelectedIndex()>0 && channel!=null){
      myChannelLabel.setIcon(label.getIcon());
    }
    sharedChannelSources.setProperty(id, readSettings());

    basicPanel.repaint();

  }


  private void setBoxes (){
    int maxCounter = Math.min(detailMix.length, numberOfFields);
    for (int i=0; i< maxCounter; i++) {
      if (detailMix[i].equals("additional")){
        fieldSetting[i].setSelectedIndex(1);
      }
      else{
        fieldSetting[i].setSelectedIndex(0);
      }
      if (fieldIndex[i]==13){
        if (detailMix[i].equals("mix")){
          fieldSetting[i].setSelectedIndex(2);
        }
      }
      if (fieldIndex[i]==7||fieldIndex[i]==8||fieldIndex[i]==12||fieldIndex[i]==16){
        if (detailMix[i].equals("before")){
          fieldSetting[i].setSelectedIndex(2);
        }
        if (detailMix[i].equals("after")){
          fieldSetting[i].setSelectedIndex(3);
        }
      }
    }
  }


  public String readSettings(){

    if (!okPressed){
      return null;
    }

    StringBuffer buffer = new StringBuffer(myNameField.getText().replaceAll(";", ",") + ";" + id1 + ";" + id2);

    if (iconBox.getSelectedIndex()==1){
      buffer.append(";primary");
    } else{
      if (iconBox.getSelectedIndex()==2){
        buffer.append(";additional");
      } else{
        buffer.append(";");
      }
    }

    if (categoriesBox.getSelectedIndex()==1){
      buffer.append(";additional");
    } else{
      buffer.append(";primary");
    }


    if (webBox.getSelectedIndex()==1){
      buffer.append(";additional");
    } else{
      buffer.append(";primary");
    }

    myCountry = ((String)countryBox.getSelectedItem()).trim().toLowerCase();
    if (!(myCountry.length()==2 && myCountry.matches("[a-z]+"))){
      myCountry = "xx";
    }
    buffer.append(";" + myCountry);

    for (int i = 0; i<numberOfFields; i++){
      String setting;
      if (fieldSetting[i].getSelectedIndex()==0){
        setting = "primary";
      }
      else {
        setting= "additional";
      }
      if (fieldIndex[i]==13){
        if (fieldSetting[i].getSelectedIndex()==2){
          setting = "mix";
        }
      }
      if (fieldIndex[i]==7||fieldIndex[i]==8||fieldIndex[i]==12||fieldIndex[i]==16){
        if (fieldSetting[i].getSelectedIndex()==2){
          setting = "before";
        }
        if (fieldSetting[i].getSelectedIndex()==3){
          setting = "after";
        }
      }
      buffer.append(";" + setting);
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

}
