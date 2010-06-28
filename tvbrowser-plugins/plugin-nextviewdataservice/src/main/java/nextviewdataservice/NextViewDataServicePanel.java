/*
 * NextViewDataService Plugin by Andreas Hessel (Vidrec@gmx.de)
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
 */
package nextviewdataservice;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import tvdataservice.SettingsPanel;
import util.misc.OperatingSystem;
import util.ui.ChannelLabel;
import util.ui.UiUtilities;
import devplugin.Channel;
import devplugin.Plugin;

/**
 * The settings panel of this data service
 * @author jb
 */
public final class NextViewDataServicePanel extends SettingsPanel implements FocusListener {

  private static final long serialVersionUID = 1L;
//private static final Logger mLog = java.util.logging.Logger.getLogger(NextViewDataServicePanel.class.getName());
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(NextViewDataServicePanel.class);

  private static NextViewDataServicePanel mInstance;

  private NextViewDataService mService;
  private Properties mProp;
  private JTextField pathTextField;
  private JTextField iniTextField;
  private JTextField dbTextField;
  private JComboBox providerComboBox;
  private JCheckBox doAutoRun;
  private JCheckBox doDataMix;
  private JCheckBox getAlternativeIcons;
  private JCheckBox releaseLock;
  private JFormattedTextField autoStartField;
  private JFormattedTextField autoRepeatField;

  private JPanel alternativeSelectionPanel;
  private JPanel alternativeDetailPanel;
  private int channelCount;
  private int subChannelCount;
  private JCheckBox [] channelDataMix;
  private JCheckBox [] channelAlternativeIcon;
  private JLabel[][] alternativeChannelField;
  private String [] alternativeId;
  private Channel [] channelList;
  private Properties alternativeChannelsDesc;
  private String [][] alternativeMixTable;
  private String [] defaultMix = {"replace","replace","replace","replace","replace","before","replace","replace","before","mix","replace","replace","before","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace"};

  private final JCheckBox noneData;
  private final JCheckBox allData;
  private final JCheckBox noneIcon;
  private final JCheckBox allIcon;


  public Font italic;
  public Font plain;



  /**
   * Initialize a new settings panel
   * @param prop ; the settings of this data service
   */
  public NextViewDataServicePanel(Properties prop) {
    mProp = prop;
    mService = NextViewDataService.getInstance();
    setOpaque(false);

    ImageIcon folderIcon = Plugin.getPluginManager().getIconFromTheme(null,"actions", "document-open", 16);
    Dimension buttonSize = new Dimension(40, 28);

    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));


    /* tabbed pane */
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

    /* basic pane */
    JPanel basicPanel = new JPanel();
    basicPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 5));
    GridBagLayout gbl1 = new GridBagLayout();
    GridBagConstraints gbc1 = new GridBagConstraints();
    basicPanel.setLayout(gbl1);

    gbc1.fill = GridBagConstraints.HORIZONTAL;
    gbc1.anchor = GridBagConstraints.NORTHWEST;
    gbc1.weightx = 1.0;
    gbc1.weighty = 10.0;

    // Label AppPfad
    JLabel pathLabel = new JLabel(mLocalizer.msg("nxtvepg_path", "Path to NextViewEPG"));
    gbc1.gridwidth = GridBagConstraints.REMAINDER;
    basicPanel.add(pathLabel, gbc1);

    // Textfield AppPfad
    pathTextField = new JTextField();
    gbc1.weightx = 1.0;
    gbc1.weighty = 10.0;
    gbc1.anchor = GridBagConstraints.NORTHWEST;
    gbc1.gridwidth = 2;
    basicPanel.add(pathTextField, gbc1);
    pathTextField.setPreferredSize(buttonSize);


    // File-AppButton
    JButton pathButton = new JButton();
    gbc1.weightx = 0.0;
    gbc1.gridwidth = GridBagConstraints.REMAINDER;
    pathButton.setToolTipText(mLocalizer.msg("selectApplication", "Select..."));
    pathButton.setIcon(folderIcon);
    basicPanel.add(pathButton, gbc1);
    pathButton.setPreferredSize(buttonSize);
    pathButton.setSize(buttonSize);
    pathButton.setMargin(new Insets(0, 0, 0, 0));
    pathButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        pathButtonPressed(e);
      }
    });

    JLabel dummyLabel1 = new JLabel(" ");
    dummyLabel1.setPreferredSize (new Dimension (10,10));
    basicPanel.add(dummyLabel1, gbc1);


    // Label IniPfad
    JLabel iniLabel = new JLabel(mLocalizer.msg("nxtvepg_ini", "Path to nxtvepg Configuration File"));
    gbc1.gridwidth = GridBagConstraints.REMAINDER;
    basicPanel.add(iniLabel, gbc1);

    // Textfield IniPfad
    iniTextField = new JTextField();
    gbc1.weightx = 1.0;
    gbc1.weighty = 10.0;
    gbc1.anchor = GridBagConstraints.NORTHWEST;
    gbc1.gridwidth = 2;
    basicPanel.add(iniTextField, gbc1);
    iniTextField.setPreferredSize(buttonSize);


    // File-IniButton
    JButton iniButton = new JButton();
    gbc1.weightx = 0.0;
    gbc1.gridwidth = GridBagConstraints.REMAINDER;
    iniButton.setIcon(folderIcon);
    iniButton.setToolTipText(mLocalizer.msg("selectIni", "Select..."));
    basicPanel.add(iniButton, gbc1);
    iniButton.setPreferredSize(buttonSize);
    iniButton.setSize(buttonSize);
    iniButton.setMargin(new Insets(0, 0, 0, 0));
    iniButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        iniButtonPressed(e);
      }
    });

    JLabel dummyLabel2 = new JLabel(" ");
    dummyLabel2.setPreferredSize (new Dimension (10,10));
    basicPanel.add(dummyLabel2, gbc1);

    // Label dbPfad
    JLabel dbLabel = new JLabel(mLocalizer.msg("nxtvepg_db", "Path to nxtvepg Data Base Directory"));
    gbc1.gridwidth = GridBagConstraints.REMAINDER;
    basicPanel.add(dbLabel, gbc1);

    // Textfield dbPath
    dbTextField = new JTextField();
    gbc1.weightx = 1.0;
    gbc1.weighty = 10.0;
    gbc1.anchor = GridBagConstraints.NORTHWEST;
    gbc1.gridwidth = 2;
    basicPanel.add(dbTextField, gbc1);
    dbTextField.setPreferredSize(buttonSize);

    // File-dbPath
    JButton dbButton = new JButton();
    gbc1.weightx = 0.0;
    gbc1.gridwidth = GridBagConstraints.REMAINDER;
    dbButton.setIcon(folderIcon);
    dbButton.setToolTipText(mLocalizer.msg("selectDirectory", "Select..."));
    basicPanel.add(dbButton, gbc1);
    dbButton.setPreferredSize(buttonSize);
    dbButton.setSize(buttonSize);
    dbButton.setMargin(new Insets(0, 0, 0, 0));
    dbButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        dbButtonPressed(e);
      }
    });

    JLabel dummyLabel3 = new JLabel(" ");
    dummyLabel3.setPreferredSize (new Dimension (10,10));
    basicPanel.add(dummyLabel3, gbc1);

    // Label Provider
    JLabel providerLabel = new JLabel(mLocalizer.msg("provider", "Provider"));
    gbc1.gridwidth = GridBagConstraints.REMAINDER;
    gbc1.weightx = 1.0;
    gbc1.weighty = 1.0;
    basicPanel.add(providerLabel, gbc1);
    providerLabel.setPreferredSize(new Dimension(80, 20));

    // ComboBox Provider
    providerComboBox = new JComboBox(new String[]{"merged", "Kabel1", "TV5", "SF1", "TSR1", "TSI1"});
    providerComboBox.setEditable(true);
    gbc1.weightx = 1.0;
    gbc1.weighty = 40.0;
    gbc1.gridwidth = GridBagConstraints.REMAINDER;
    providerComboBox.setPreferredSize(buttonSize);
    basicPanel.add(providerComboBox, gbc1);



    /* group timer panel */

    JPanel timerPanel = new JPanel();
    timerPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 0));

    GridBagLayout gbl21 = new GridBagLayout();
    GridBagConstraints gbc21 = new GridBagConstraints();
    timerPanel.setLayout(gbl21);

    gbc21.anchor = GridBagConstraints.NORTHWEST;
    gbc21.fill = GridBagConstraints.HORIZONTAL;
    gbc21.gridwidth = GridBagConstraints.REMAINDER;
    gbc21.weightx = 1.0;
    gbc21.weighty = 1.0;

    // AutoUpdate Checkbox

    JPanel timePanel = new JPanel();
    timePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
    GridBagLayout gbl22 = new GridBagLayout();
    GridBagConstraints gbc22 = new GridBagConstraints();

    timePanel.setLayout(gbl22);

    gbc22.fill = GridBagConstraints.HORIZONTAL;
    gbc22.gridwidth = GridBagConstraints.REMAINDER;
    gbc22.anchor = GridBagConstraints.WEST;
    gbc22.weightx = 1.0;
    gbc22.weighty = 1.0;

    doAutoRun = new JCheckBox("   " + mLocalizer.msg("nxtvepgAutoRun", "Auto-Update Mode for program data"));
    doAutoRun.setOpaque(false);

    timePanel.add(doAutoRun, gbc22);
    JLabel dummyLabel4 = new JLabel(" ");
    dummyLabel4.setPreferredSize (new Dimension (10,10));
    timePanel.add(dummyLabel4, gbc22);

    // Label StartTime
    JLabel autoStart = new JLabel(mLocalizer.msg("nxtvepgStart", "delay:")+ "  ");
    gbc22.weightx = 0.1;
    gbc22.weighty = 5.0;
    gbc22.anchor = GridBagConstraints.WEST;
    gbc22.gridwidth = 1;
    timePanel.add(autoStart, gbc22);


    // AutoUpdate StartTime
    autoStartField = new JFormattedTextField();
    gbc22.weightx = 0.3;
    gbc22.anchor = GridBagConstraints.EAST;
    gbc22.gridwidth = 1;
    timePanel.add(autoStartField, gbc22);
    autoStartField.setHorizontalAlignment(JTextField.RIGHT);
    autoStartField.setPreferredSize(buttonSize);
    autoStartField.addFocusListener(this);



    // Label TimeUnit
    JLabel startUnit = new JLabel("  " + mLocalizer.msg("autoUnit", " minutes"));
    gbc22.weightx = 1.0;
    gbc22.gridwidth = GridBagConstraints.REMAINDER;
    timePanel.add(startUnit, gbc22);


    // Label Repetition
    JLabel autoRepeat = new JLabel(mLocalizer.msg("nxtvepgRepeat", "frequenzy:")+ "  ");
    gbc22.weightx = 0.1;
    gbc22.weighty = 5.0;
    gbc22.anchor = GridBagConstraints.WEST;
    gbc22.gridwidth = 1;
    timePanel.add(autoRepeat, gbc22);

    // AutoUpdate Repetition
    autoRepeatField = new JFormattedTextField();
    gbc22.weightx = 0.3;
    gbc22.anchor = GridBagConstraints.EAST;
    gbc22.gridwidth = 1;
    timePanel.add(autoRepeatField, gbc22);
    autoRepeatField.setPreferredSize(buttonSize);
    autoRepeatField.setHorizontalAlignment(JTextField.RIGHT);
    autoRepeatField.addFocusListener(this);

    // Label TimeUnit
    JLabel repeatUnit = new JLabel("  " + mLocalizer.msg("autoUnit", " minutes"));
    gbc22.weightx = 1.0;
    gbc22.gridwidth = GridBagConstraints.REMAINDER;
    timePanel.add(repeatUnit, gbc22);
    repeatUnit.setPreferredSize(new Dimension(20, 20));

    gbc21.gridwidth = GridBagConstraints.REMAINDER;
    gbc21.weightx = 1.0;
    gbc21.weighty = 1.0;
    timerPanel.add(timePanel, gbc21);
    timePanel.setOpaque(false);

    // Release update lock
    releaseLock = new JCheckBox("   " + mLocalizer.msg("nxtvepgReleaseLock", "release external auto update lock"));
    gbc21.gridwidth = GridBagConstraints.REMAINDER;
    gbc21.weightx = 1.0;
    gbc21.weighty = 1.0;
    releaseLock.setOpaque(false);
    timerPanel.add(releaseLock, gbc21);

    JPanel fooPanel = new JPanel();
    gbc21.gridheight = GridBagConstraints.REMAINDER;
    timerPanel.add(fooPanel,gbc21);

    /* mixer pane */

    JPanel mixerPanel = new JPanel();
    mixerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

    GridBagLayout gbl31 = new GridBagLayout();
    GridBagConstraints gbc31 = new GridBagConstraints();
    mixerPanel.setLayout(gbl31);

    gbc31.anchor = GridBagConstraints.NORTHWEST;
    gbc31.fill = GridBagConstraints.HORIZONTAL;
    gbc31.gridwidth = GridBagConstraints.REMAINDER;
    gbc31.weightx = 1.0;
    gbc31.weighty = 1.0;

    // Data Mixer Checkbox

    JPanel mixPanel = new JPanel();
    mixPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    GridBagLayout gbl33 = new GridBagLayout();
    GridBagConstraints gbc32 = new GridBagConstraints();

    mixPanel.setLayout(gbl33);

    gbc32.fill = GridBagConstraints.HORIZONTAL;
    gbc32.gridwidth = GridBagConstraints.REMAINDER;
    gbc32.anchor = GridBagConstraints.WEST;
    gbc32.weightx = 1.0;
    gbc32.weighty = 1.0;
    gbc32.insets = new Insets(0, 10, 0, 0);

    doDataMix = new JCheckBox("   " + mLocalizer.msg("nxtvepgDataMix", "Add program data from different source"));
    doDataMix.setOpaque(false);


    mixPanel.add(doDataMix, gbc32);
    JLabel dummyLabel5 = new JLabel(" ");
    dummyLabel5.setPreferredSize (new Dimension (10,10));
    mixPanel.add(dummyLabel5, gbc32);

    // CheckBox for alternative channel icons

    getAlternativeIcons = new JCheckBox("   " + mLocalizer.msg("alternativeChannelIcons", "Use channel icons of the alternative channels"));
    getAlternativeIcons.setOpaque(false);


    mixPanel.add(getAlternativeIcons, gbc32);

    gbc31.gridwidth = GridBagConstraints.REMAINDER;
    gbc31.weightx = 1.0;
    gbc31.weighty = 1.0;
    mixerPanel.add(mixPanel, gbc31);
    mixerPanel.setOpaque(false);

    // Selection Panel //

    alternativeSelectionPanel = new JPanel();
//    alternativeSelectionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    allData = new JCheckBox ();
    allData.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        allDataPressed(e);
        noneData.setSelected(false);
        allData.setSelected(true);
      }
    });
    noneData = new JCheckBox ();
    noneData.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        noDataPressed(e);
        allData.setSelected(false);
        noneData.setSelected(true);
      }
    });
    allIcon = new JCheckBox ();
    allIcon.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        allIconPressed(e);
        noneIcon.setSelected(false);
        allIcon.setSelected(true);
      }
    });
    noneIcon = new JCheckBox ();
    noneIcon.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        noIconPressed(e);
        allIcon.setSelected(false);
        noneIcon.setSelected(true);
      }
    });


    mixerPanel.add(alternativeSelectionPanel, gbc31);

    // Detail Panel //

    alternativeDetailPanel = new JPanel();
    alternativeDetailPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));

    mixerPanel.add(alternativeDetailPanel, gbc31);

    /* */
    tabbedPane.add(mLocalizer.msg("basicSettings", "Sources"), new JScrollPane(basicPanel));
    tabbedPane.add(mLocalizer.msg("timerSettings", "Update Timer"), new JScrollPane(timerPanel));

    int version = (tvbrowser.TVBrowser.VERSION.getMajor()*100) +  tvbrowser.TVBrowser.VERSION.getMinor();
    if (version>= 270) {
      tabbedPane.add(mLocalizer.msg("mixerSettings", "Data Mix"), new JScrollPane(mixerPanel));
    }
    this.add(tabbedPane);

  }

  /**
   * Returns the current instance of the setting panel. If no instance is given, create a new one.
   * @param settings ; the settings of tis data service
   * @return instance of setting panel
   */
  public static SettingsPanel getInstance(Properties settings) {
    if (mInstance == null || mInstance.channelCount != mInstance.mService.getAvailableChannels().length) {
      mInstance = new NextViewDataServicePanel(settings);
    }

    mInstance.updatePanel();
    return mInstance;
  }

  /**
   * Fill the text fields with current settings
   */
  public void updatePanel() {

    String provider = (String) mProp.getProperty(NextViewDataService.PROVIDER);
    if (provider.equals("0d92")) {
      provider = "Kabel1";
    } else if (provider.equals("f500")) {
      provider = "TV5";
    } else if (provider.equals("04c1")) {
      provider = "SF1";
    } else if (provider.equals("04c2")) {
      provider = "TSR1";
    } else if (provider.equals("04c2")) {
      provider = "TSI1";
    }
    int index = 0;
    for (index = 0; index < providerComboBox.getItemCount(); index++) {
      if (((String) providerComboBox.getItemAt(index)).equals(provider)) {
        providerComboBox.setSelectedIndex(index);
        break;
      }
    }
    if (index == providerComboBox.getItemCount()) {
      providerComboBox.addItem(provider);
      providerComboBox.setSelectedIndex(index);
    }
    pathTextField.setText((String) mProp.getProperty(NextViewDataService.PATH));
    iniTextField.setText((String) mProp.getProperty(NextViewDataService.RCFILE));
    dbTextField.setText((String) mProp.getProperty(NextViewDataService.DBDIR));

    if (mProp.getProperty(NextViewDataService.AUTORUN).equals("YES")) {
      doAutoRun.setSelected(true);
    }
    autoStartField.setText((String) mProp.getProperty(NextViewDataService.AUTOSTART));
    autoRepeatField.setText((String) mProp.getProperty(NextViewDataService.AUTOREPETITION));

    releaseLock.setSelected(!new File(mService.mDataDir + "/autoUpLck.lck").exists());
    releaseLock.setVisible(!releaseLock.isSelected());

    int version = (tvbrowser.TVBrowser.VERSION.getMajor()*100) +  tvbrowser.TVBrowser.VERSION.getMinor();
    if (version>= 270) {
      if (mProp.getProperty(NextViewDataService.DATAMIX).equals("YES")) {
        doDataMix.setSelected(true);
      }
      if (mProp.getProperty(NextViewDataService.ALTERNATIVEICONS).equals("YES")) {
        getAlternativeIcons.setSelected(true);
      }
      alternativeSelectionPanel.removeAll();
      GridBagLayout selectionGbl = new GridBagLayout();
      alternativeSelectionPanel.setLayout(selectionGbl);
      GridBagConstraints selectionGbc;
      Channel[] subscribedList = NextViewDataService.getPluginManager().getSubscribedChannels();
      ArrayList <Channel> chnList = new ArrayList <Channel> ();
       for (Channel element : subscribedList) {
        String [] channelId = element.getUniqueId().split("_");
        index = 0;
        boolean isSearching = true;
        while (isSearching && index < chnList.size()){
          String [] chnId = chnList.get(index).getUniqueId().split("_");
          if (chnList.get(index).getName().trim().compareToIgnoreCase(element.getName().trim())>0 ){
            isSearching = false;
          } else {
            if (chnList.get(index).getName().trim().compareToIgnoreCase(element.getName().trim())==0 && chnId[2].compareTo(channelId[2])>0){
              isSearching = false;
            }else {
              if (chnList.get(index).getName().trim().compareToIgnoreCase(element.getName().trim())==0 && chnId[2].compareTo(channelId[2])==0 && chnId[0].compareTo(channelId[0])>0){
                isSearching = false;
              }else {
                if (chnList.get(index).getName().trim().compareToIgnoreCase(element.getName().trim())==0 && chnId[2].compareTo(channelId[2])==0 && chnId[0].compareTo(channelId[0])==0 && chnId[1].compareTo(channelId[1])>0){
                  isSearching = false;
                } else{
                  index++;
                }
              }
            }
          }
        }
        chnList.add(index, element);
      }
      subscribedList = chnList.toArray(new Channel[chnList.size()]);

      Channel[] availableList = mService.getAvailableChannels();
      channelCount = availableList.length;
      subChannelCount = 0;
      for (Channel element : subscribedList) {
        for (Channel element2 : availableList) {
          if (element.getUniqueId().equals(element2.getUniqueId())) {
            subChannelCount++;
          }
        }
      }

      // these arrays have corresponing indexes!
      channelList = new Channel[subChannelCount]; //subscribed nxtv channels;
      alternativeId = new String[subChannelCount]; //uniqueIds of the alternative channels
      boolean[][] checkings = new boolean[subChannelCount][2]; // dataMix and logo flags

      alternativeChannelsDesc = new Properties();
      alternativeMixTable = new String[subChannelCount][30];

      try {
        alternativeChannelsDesc.load(new FileInputStream(NextViewDataService.getInstance().mixedChannelsDirName + "/nxtvepgAlternatives.properties"));
      } catch (IOException e) {
      }

      subChannelCount = 0;
      for (Channel element : subscribedList) {
        for (Channel element2 : availableList) {
          if (element.getUniqueId().equals(element2.getUniqueId())) {
            channelList[subChannelCount] = element2;
            String channelDesc = alternativeChannelsDesc.getProperty(element2.getId());
            if (channelDesc != null) {
              String[] chnDescs = channelDesc.split(";");
              alternativeId[subChannelCount] = chnDescs[2];
              for (int k = 0; k < 2; k++) {
                if ("1".equals(chnDescs[k])) {
                  checkings[subChannelCount][k] = true;
                } else {
                  checkings[subChannelCount][k] = false;
                }
              }
              for (int k = 0; k < 30; k++) {
                alternativeMixTable[subChannelCount][k] = chnDescs[k + 3];
              }
            } else {
              alternativeMixTable[subChannelCount]=defaultMix;
              alternativeId[subChannelCount] = "";
              checkings[subChannelCount][0] = false;
              checkings[subChannelCount][1] = false;
            }
            subChannelCount++;
          }
        }
      }
      channelDataMix = new JCheckBox[subChannelCount];
      channelAlternativeIcon = new JCheckBox[subChannelCount];
      alternativeChannelField = new JLabel[subChannelCount][4];
      selectionGbc = makegbc(0, 0, 7, 1);
      selectionGbc.anchor = GridBagConstraints.WEST;
      selectionGbc.fill = GridBagConstraints.HORIZONTAL;
      selectionGbc.gridwidth = GridBagConstraints.REMAINDER;
      selectionGbc.insets = new Insets(20, 10, 20, 10);
      JLabel altChannelDoubleClick = new JLabel(mLocalizer.msg("altChannelDoubleClick", "double click to edit:"));
      italic = new Font(altChannelDoubleClick.getFont().getFontName(), Font.ITALIC, altChannelDoubleClick.getFont().getSize());
      plain = new Font(altChannelDoubleClick.getFont().getFontName(), Font.PLAIN, altChannelDoubleClick.getFont().getSize());
      Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
      fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
      Font underline = new Font(altChannelDoubleClick.getFont().getFontName(), altChannelDoubleClick.getFont().getStyle(), altChannelDoubleClick.getFont().getSize()).deriveFont(fontAttributes);
      altChannelDoubleClick.setFont(underline);
      selectionGbl.setConstraints(altChannelDoubleClick, selectionGbc);
      alternativeSelectionPanel.add(altChannelDoubleClick);

      selectionGbc = makegbc(0, 1, 1, 1);
      selectionGbc.anchor = GridBagConstraints.WEST;
      selectionGbc.insets = new Insets(0, 10, 10, 10);
      JLabel nxtvepg = new JLabel(mLocalizer.msg("nxtvepg", "Nxtvepg"));
      selectionGbl.setConstraints(nxtvepg, selectionGbc);
      alternativeSelectionPanel.add(nxtvepg);


      selectionGbc = makegbc(1, 1, 1, 1);
      selectionGbc.insets = new Insets(0, 10, 10, 10);
      JLabel detailData = new JLabel(mLocalizer.msg("data", "data"));
      selectionGbl.setConstraints(detailData, selectionGbc);
      alternativeSelectionPanel.add(detailData);

      selectionGbc = makegbc(2, 1, 1, 1);
      selectionGbc.insets = new Insets(0, 10, 10, 10);
      JLabel detailIcon = new JLabel(mLocalizer.msg("icon", "icon"));
      selectionGbl.setConstraints(detailIcon, selectionGbc);
      alternativeSelectionPanel.add(detailIcon);

      selectionGbc = makegbc(3, 1, 1, 1);
      selectionGbc.insets = new Insets(0, 30, 10, 10);
      selectionGbc.anchor = GridBagConstraints.WEST;
      JLabel nLabel = new JLabel(mLocalizer.msg("source", "Source"));
      selectionGbl.setConstraints(nLabel, selectionGbc);
      alternativeSelectionPanel.add(nLabel);

      selectionGbc = makegbc(4, 1, 1, 1);
      selectionGbc.insets = new Insets(0, 10, 10, 10);
      JLabel cLabel = new JLabel(mLocalizer.msg("country", "Country"));
      selectionGbl.setConstraints(cLabel, selectionGbc);
      alternativeSelectionPanel.add(cLabel);

      selectionGbc = makegbc(5, 1, 1, 1);
      selectionGbc.insets = new Insets(0, 10, 10, 10);
      selectionGbc.anchor = GridBagConstraints.WEST;
      JLabel pLabel = new JLabel(mLocalizer.msg("dataPlugin", "Data Plugin"));
      selectionGbl.setConstraints(pLabel, selectionGbc);
      alternativeSelectionPanel.add(pLabel);

      selectionGbc = makegbc(6, 1, 1, 1);
      selectionGbc.insets = new Insets(0, 10, 10, 10);
      selectionGbc.anchor = GridBagConstraints.WEST;
      selectionGbc.fill = GridBagConstraints.HORIZONTAL;
      selectionGbc.gridwidth = GridBagConstraints.REMAINDER;
      JLabel gLabel = new JLabel(mLocalizer.msg("group", "Channel Group"));
      selectionGbl.setConstraints(gLabel, selectionGbc);
      alternativeSelectionPanel.add(gLabel);

      String[] idTemp = new String[subscribedList.length];
      for (int i = 0; i < idTemp.length; i++) {
        idTemp[i] = subscribedList[i].getUniqueId();
      }

      final String[] subscribedIds = idTemp;

      if (alternativeChannelsDesc != null) {

        ChannelLabel[] channelLabel= new ChannelLabel[subChannelCount];
        final NextViewDataServicePanel settingPanel = this;

        for (int i = 0; i < subChannelCount; i++) {

          final int fIndex = i;

          selectionGbc = makegbc(0, i + 2, 1, 1);
          selectionGbc.anchor = GridBagConstraints.WEST;
          channelLabel[i] = new ChannelLabel(channelList[i]);
          channelLabel[i].addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
              if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
                e.consume();
                changeAlternativeChannel(fIndex, subscribedIds, settingPanel);
              }
            }
          });
          selectionGbl.setConstraints(channelLabel[i], selectionGbc);
          alternativeSelectionPanel.add(channelLabel[i]);

          selectionGbc = makegbc(1, i + 2, 1, 1);
          channelDataMix[i] = new JCheckBox();
          channelDataMix[i].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              allData.setSelected(false);
              noneData.setSelected(false);
            }
          });
          channelDataMix[i].setSelected(checkings[i][0]);
          selectionGbl.setConstraints(channelDataMix[i], selectionGbc);
          alternativeSelectionPanel.add(channelDataMix[i]);

          selectionGbc = makegbc(2, i + 2, 1, 1);
          channelAlternativeIcon[i] = new JCheckBox();
          channelAlternativeIcon[i].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              allIcon.setSelected(false);
              noneIcon.setSelected(false);
            }
          });
          channelAlternativeIcon[i].setSelected(checkings[i][1]);
          selectionGbl.setConstraints(channelAlternativeIcon[i], selectionGbc);
          alternativeSelectionPanel.add(channelAlternativeIcon[i]);

          String[] displayText = HelperMethods.getChannelName(alternativeId[i], mLocalizer.msg("notNamed", "n.n."));

          for (int j = 0; j < 4; j++) {
            alternativeChannelField[i][j] = new JLabel(displayText[j]);
            alternativeChannelField[i][j].addMouseListener(new MouseAdapter() {
              public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
                  e.consume();
                  changeAlternativeChannel(fIndex, subscribedIds, settingPanel);
                }
              }
            });
            selectionGbc = makegbc(3 + j, i + 2, 1, 1);
            if (j == 0) {
              selectionGbc.insets = new Insets(0, 30, 0, 10);
            }
            if (j != 1) {
              selectionGbc.anchor = GridBagConstraints.WEST;
            }
            if (j == 3) {
              selectionGbc.fill = GridBagConstraints.HORIZONTAL;
              selectionGbc.gridwidth = GridBagConstraints.REMAINDER;
            }
            selectionGbl.setConstraints(alternativeChannelField[i][j], selectionGbc);
            alternativeSelectionPanel.add(alternativeChannelField[i][j]);
          }
          setAlienFont(alternativeChannelField[i][0], alternativeId[i], subscribedIds);
        }
      }
      selectionGbc = makegbc(1, subChannelCount + 2, 1, 1);
      JLabel detailData1 = new JLabel(mLocalizer.msg("data", "data"));
      selectionGbl.setConstraints(detailData1, selectionGbc);
      alternativeSelectionPanel.add(detailData1);
      selectionGbc = makegbc(2, subChannelCount + 2, 1, 1);
      JLabel detailIcon2 = new JLabel(mLocalizer.msg("icon", "icon"));
      selectionGbl.setConstraints(detailIcon2, selectionGbc);
      alternativeSelectionPanel.add(detailIcon2);
      selectionGbc = makegbc(0, subChannelCount + 3, 1, 1);
      selectionGbc.anchor = GridBagConstraints.EAST;
      JLabel selectAllLabel = new JLabel(mLocalizer.msg("selectAll", "select all"));
      selectionGbl.setConstraints(selectAllLabel, selectionGbc);
      alternativeSelectionPanel.add(selectAllLabel);
      selectionGbc = makegbc(1, subChannelCount + 3, 1, 1);
      selectionGbl.setConstraints(allData, selectionGbc);
      alternativeSelectionPanel.add(allData);
      selectionGbc = makegbc(2, subChannelCount + 3, 1, 1);
      selectionGbl.setConstraints(allIcon, selectionGbc);
      alternativeSelectionPanel.add(allIcon);
      selectionGbc = makegbc(0, subChannelCount + 4, 1, 1);
      selectionGbc.anchor = GridBagConstraints.EAST;
      JLabel deSelectAllLabel = new JLabel(mLocalizer.msg("selectNone", "select none"));
      selectionGbl.setConstraints(deSelectAllLabel, selectionGbc);
      alternativeSelectionPanel.add(deSelectAllLabel);
      selectionGbc = makegbc(1, subChannelCount + 4, 1, 1);
      selectionGbl.setConstraints(noneData, selectionGbc);
      alternativeSelectionPanel.add(noneData);
      selectionGbc = makegbc(2, subChannelCount + 4, 1, 1);
      selectionGbl.setConstraints(noneIcon, selectionGbc);
      alternativeSelectionPanel.add(noneIcon);
      alternativeSelectionPanel.repaint();
      alternativeDetailPanel.removeAll();
      if (subChannelCount > 0) {
        JButton detailButton = new JButton(mLocalizer.msg("detailButtonText", "Change details for all channels"));
        final NextViewDataServicePanel settingPanel = this;
        detailButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AlternativeFineTuningDialog detailsDialog;
            Window parent = UiUtilities.getBestDialogParent(settingPanel);
            if (parent instanceof JFrame) {
              detailsDialog = AlternativeFineTuningDialog.getInstance((JFrame) parent);
            } else {
              detailsDialog = AlternativeFineTuningDialog.getInstance((JDialog) parent);
            }
            detailsDialog.showGui("noID", "All Channels", null, alternativeMixTable[0]);
            String[] detailMix = detailsDialog.readSettings();
            if (detailMix!=null) {
              for (int i = 0; i < subChannelCount; i++) {
                alternativeMixTable[i] = detailMix;
              }
            }

          }
        });

        alternativeDetailPanel.setLayout(new BoxLayout(alternativeDetailPanel, BoxLayout.LINE_AXIS));
        alternativeDetailPanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 10, 0));
        alternativeDetailPanel.add(detailButton);
        alternativeDetailPanel.add(Box.createHorizontalGlue());
      }
      alternativeDetailPanel.repaint();
    }

  }

  /**
   * Will be called if cancel was pressed
   * @since 2.7
   */
  public void cancel() {
  }


  /**
   * Transfer data from textfield to the settings of this data service
   */
  public void ok() {

    String provider = (String) providerComboBox.getSelectedItem();
    provider = provider.trim();
    if (provider.equals("Kabel1")) {
      provider = "0d92";
    } else if (provider.equals("TV5")) {
      provider = "f500";
    } else if (provider.equals("SF1")) {
      provider = "04c1";
    } else if (provider.equals("TSR1")) {
      provider = "04c2";
    } else if (provider.equals("TSI1")) {
      provider = "04c3";
    } else if (provider.equals("FF") || provider.length() == 0) {
      provider = "merged";
    }


    mProp.setProperty(NextViewDataService.PROVIDER, provider);
    mProp.setProperty(NextViewDataService.PATH, pathTextField.getText().trim());
    mProp.setProperty(NextViewDataService.RCFILE, iniTextField.getText().trim());
    mProp.setProperty(NextViewDataService.DBDIR, dbTextField.getText().trim());


    String newAutoRun = "NO";
    if (doAutoRun.isSelected()) {
      newAutoRun = "YES";
    }


    if (!mProp.getProperty(NextViewDataService.AUTORUN).equals(newAutoRun) || !autoStartField.getText().equals(mProp.get(NextViewDataService.AUTOSTART)) || !autoRepeatField.getText().equals((String) mProp.get(NextViewDataService.AUTOREPETITION))) {
      if (mService.firstUpdate == mService.nextUpdate) {
        mService.nextUpdate = mService.nextUpdate - Integer.parseInt(mProp.getProperty(NextViewDataService.AUTOSTART)) * 60000L;
        mService.nextUpdate = mService.nextUpdate + Integer.parseInt(autoStartField.getText()) * 60000L;
        mService.firstUpdate = mService.nextUpdate;
      } else {
        mService.nextUpdate = mService.nextUpdate - Integer.parseInt(mProp.getProperty(NextViewDataService.AUTOREPETITION)) * 60000L;
        mService.nextUpdate = mService.nextUpdate + Integer.parseInt(autoRepeatField.getText()) * 60000L;
      }

      mProp.setProperty(NextViewDataService.AUTORUN, newAutoRun);
      mProp.setProperty(NextViewDataService.AUTOSTART, autoStartField.getText());
      mProp.setProperty(NextViewDataService.AUTOREPETITION, autoRepeatField.getText());
    }

    if (releaseLock.isVisible() && releaseLock.isSelected()) {
      new File(mService.mDataDir + "/autoUpLck.lck").delete();
    }


    for (int i = 0; i < subChannelCount; i++) {

      if (alternativeId[i].equals("")){
        alternativeChannelsDesc.remove(channelList[i].getId());
      }
      else {
        String dataFlg;
        if (channelDataMix[i].isSelected()) {
          dataFlg = "1;";
        } else {
          dataFlg = "0;";
        }
        String iconFlg;
        if (channelAlternativeIcon[i].isSelected()) {
          iconFlg = "1;";
        } else {
          iconFlg = "0;";
        }
        StringBuffer outBuffer = new StringBuffer(dataFlg+iconFlg+alternativeId[i]);
        for (int j = 0; j<30; j++){
          outBuffer.append(";" + alternativeMixTable[i][j]);
        }
        alternativeChannelsDesc.setProperty(channelList[i].getId(), outBuffer.toString());
      }
      try{
        alternativeChannelsDesc.store(new FileOutputStream(NextViewDataService.getInstance().mixedChannelsDirName + "/nxtvepgAlternatives.properties"), "Nxtvepg Alternative Sources");
      } catch (IOException e) {
      }
    }

    String dataMixSelection = "NO";
    if (doDataMix.isSelected()) {
      dataMixSelection = "YES";
    }
    mProp.setProperty(NextViewDataService.DATAMIX, dataMixSelection);

    String iconSelection = "NO";
    if (getAlternativeIcons.isSelected()) {
      iconSelection = "YES";
      HashMap<String, Channel> alternativeChannels = HelperMethods.getAlternativeChannels(1);
      if (alternativeChannels != null) {
        for (Channel chn : channelList) {
          if (chn!=null) {
            if (alternativeChannels.get(chn.getId()) != null && alternativeChannels.get(chn.getId()).hasIcon()) {
              Icon icon = alternativeChannels.get(chn.getId()).getIcon();
              if (icon == null){
                icon = new ChannelLabel(alternativeChannels.get(chn.getId())).getIcon();
              }
              JbUtilities.storeIcon(icon, "png", mService.mDataDir + "/alternative_icons/" + chn.getId() + ".png");
              chn.setDefaultIcon(icon);
            }
            else{
              Icon icon = new ImageIcon (getClass().getResource("icons/nxtvepg.png"));
              URL iconUrl = getClass().getResource("icons/" + chn.getId() + ".png");
              if (iconUrl != null){
                icon = new ImageIcon(iconUrl);
              }
              chn.setDefaultIcon(icon);

            }
          }
        }
      }
    }
    else{
      for (Channel chn : channelList){
        if (chn!=null) {
          Icon icon = new ImageIcon(getClass().getResource("icons/nxtvepg.png"));
          URL iconUrl = getClass().getResource("icons/" + chn.getId() + ".png");
          if (iconUrl != null) {
            icon = new ImageIcon(iconUrl);
          }
          chn.setDefaultIcon(icon);
        }
      }
    }

    mProp.setProperty(NextViewDataService.ALTERNATIVEICONS, iconSelection);

  }

  /**
   * invoked when the user clicks the Button to open the
   * FileChooser Dialog: nxtvepg application path
   */
  public void pathButtonPressed(ActionEvent e) {
    JFileChooser appf = new JFileChooser();
    appf.setCurrentDirectory(new File(getNxtvApplication(pathTextField.getText().trim())));
    appf.setFileFilter(setNxtvAppFilter());
    appf.setDialogTitle(mLocalizer.msg("selectApplication", "Select..."));
    if (appf.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      pathTextField.setText(appf.getSelectedFile().toString());
    }
  }

  /**
   * invoked when the user clicks the Button to open the
   * FileChooser Dialog: nxtvepg ini/rc file
   */
  public void iniButtonPressed(ActionEvent e) {
    JFileChooser inif = new JFileChooser();
    inif.setCurrentDirectory(new File(getNxtvIni()));
    if (OperatingSystem.isLinux()) {
      inif.setFileHidingEnabled(false);
    }
    inif.setDialogTitle(mLocalizer.msg("selectIni", "Select..."));
    if (inif.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      iniTextField.setText(inif.getSelectedFile().toString());
    }
  }

  /**
   * invoked when the user clicks the Button to open the
   * FileChooser Dialog: data base directory
   */
  public void dbButtonPressed(ActionEvent e) {
    JFileChooser dbf = new JFileChooser();
    dbf.setCurrentDirectory(new File(getNxtvDir()));
    dbf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (OperatingSystem.isLinux()) {
      dbf.setFileHidingEnabled(false);
    }
    dbf.setDialogTitle(mLocalizer.msg("selectDirectory", "Select"));
    if (dbf.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      dbTextField.setText(dbf.getSelectedFile().toString());
    }

  }



  public void allDataPressed(ActionEvent e) {

    for (int i = 0; i < subChannelCount; i++){
      channelDataMix[i].setSelected(true);
    }
  }
  public void allIconPressed(ActionEvent e) {

    for (int i = 0; i < subChannelCount; i++){
      channelAlternativeIcon[i].setSelected(true);
    }
  }
  public void noDataPressed(ActionEvent e) {

    for (int i = 0; i < subChannelCount; i++){
      channelDataMix[i].setSelected(false);
    }
  }
  public void noIconPressed(ActionEvent e) {
    for (int i = 0; i < subChannelCount; i++){
      channelAlternativeIcon[i].setSelected(false);
    }
  }

  /**
   * File filter for FileChooser Dialog:
   * nxtvepg application path
   */
  public FileFilter setNxtvAppFilter() {

    String fie = "";
    if (OperatingSystem.isWindows()) {
      fie = "nxtvepg.exe";
    }
    if (OperatingSystem.isLinux()) {
      fie = "nxtvepg";
    }

    final String nxtvApp = fie;
    FileFilter ff = new FileFilter() {

      public boolean accept(File f) {
        // show subdirectories
        if (f.isDirectory()) {
          return true;
        }
        if (f.getName().equalsIgnoreCase(nxtvApp)) {
          return true;
        }
        return f.getName().toLowerCase().endsWith(nxtvApp);
      }

      public String getDescription() {
        return nxtvApp;
      }
    };
    return ff;
  }

  /**
   * Returns default path for the nxtvepg application if no
   * other path is specified by the user.
   * @param nxtvApp
   * @return full pathname of the nxtvepg application
   */
  public static String getNxtvApplication(String nxtvApp) {

    if (nxtvApp.equals("")) {
      if (OperatingSystem.isWindows() && (new File("C:/Programme/nxtvepg/nxtvepg.exe").exists())) {
        return "C:/Programme/nxtvepg/nxtvepg.exe";
      }
      if (OperatingSystem.isLinux()) {
        if (new File("/usr/bin/nxtvepg").exists()) {
          return "/usr/bin/nxtvepg";
        }
        if (new File("/usr/local/bin/nxtvepg").exists()) {
          return "/usr/local/bin/nxtvepg";
        }
      }
    }
    return nxtvApp;
  }


  /**
   * Returns default path for the nxtvepg data base directory if no
   * other path is specified by the user.
   * @return full pathname
   */
  public String getNxtvDir() {
    String nxtvDir = dbTextField.getText().trim();
    if (nxtvDir.equals("")) {
      if (OperatingSystem.isWindows() && (new File("C:/Programme/nxtvepg").exists())) {
        return "C:/Programme/nxtvepg";
      }
      if (OperatingSystem.isLinux()) {
        if (new File(System.getProperty("user.dir") + "/.nxtvdb").exists()) {
          return System.getProperty("user.dir") + "/.nxtvdb";
        }
        if (new File("/var/tmp/nxtvdb").exists()) {
          return "/var/tmp/nxtvdb";
        }
        if (new File("/usr/tmp/nxtvdb/nxtvdb-").exists()) {
          return "/usr/tmp/nxtvdb/nxtvdb-";
        }
      }
    }
    return nxtvDir;
  }

  /**
   * Returns default path for the nxtvepg ini/rc file if no
   * other path is specified by the user.
   * @return full pathname
   */
  public String getNxtvIni() {
    String nxtvIni = iniTextField.getText().trim();
    if (nxtvIni == null || nxtvIni.equals("")) {
      if (OperatingSystem.isWindows() && (new File("C:/Programme/nxtvepg/nvtvepg.ini").exists())) {
        return "C:/Programme/nxtvepg/nvtvepg.ini";
      }
      if (OperatingSystem.isLinux() && (new File(System.getProperty("user.dir") + "/.nxtvepgrc").exists())) {
        return System.getProperty("user.dir") + "/.nxtvepgrc";
      }
    }

    return nxtvIni;
  }


  public void focusGained(FocusEvent e) {
  }

  /**
   * Prevent illegal input for the auto update interval
   */
  public void focusLost(FocusEvent e) {
    int minUpdateTime = 30;
    try {
      Integer.parseInt((String) autoStartField.getText());
    } catch (Exception ex) {
      autoStartField.setText((String) mProp.getProperty(NextViewDataService.AUTOSTART));
    }

    try {
      Integer.parseInt(autoRepeatField.getText());
    } catch (Exception ex) {
      autoRepeatField.setText(mProp.getProperty(NextViewDataService.AUTOREPETITION));
    }

    try {
      minUpdateTime = tvbrowser.core.Settings.propDataServiceAutoUpdateTime.getInt();
    } catch (Exception ex) {
      minUpdateTime = 30;
    }

    if (minUpdateTime > Integer.parseInt(autoStartField.getText())) {
      autoStartField.setText(Integer.toString(minUpdateTime));
    }
    if (minUpdateTime > Integer.parseInt(autoRepeatField.getText())) {
      autoRepeatField.setText(Integer.toString(minUpdateTime));
    }

  }

  private void changeAlternativeChannel (int index, String[] alienIds, NextViewDataServicePanel settingPanel){
    AlternativeChannelDialog dialog;
    Window parent = UiUtilities.getBestDialogParent(settingPanel);
    String prevId;
    prevId = alternativeId[index];

    if (parent instanceof JFrame) {
      dialog = new AlternativeChannelDialog((JFrame)parent);
    } else {
      dialog = new AlternativeChannelDialog((JDialog)parent);

    }
    Properties actAlternatives = new Properties();
   for (int i=0; i< alternativeId.length; i++){
        actAlternatives.put(channelList[i].getId(), alternativeId[i]);
    }
    String actIndex = channelList[index].getId();
    dialog.createGui(actIndex, channelList[index].getName(), new ChannelLabel(channelList[index]), alienIds, prevId, alternativeMixTable[index], actAlternatives);
    String newText = dialog.getAlternativeChannel();
    if (newText!=null) {
      alternativeId[index] = newText;
      String[] newDisplayText = HelperMethods.getChannelName(newText, mLocalizer.msg("notNamed", "n.n."));
      for (int i = 0; i < 4; i++) {
        alternativeChannelField[index][i].setText(newDisplayText[i]);
      }
      alternativeMixTable[index] = dialog.getDetails();
      setAlienFont(alternativeChannelField[index][0], alternativeId[index], alienIds);
    }
  }

  private void setAlienFont (JLabel label, String id, String [] testIds){

    label.setFont(italic);

    boolean notFound = true;
    int j=0;
    while (notFound && j<testIds.length){
      if (testIds[j].equals(id)){
        label.setFont(plain);
        notFound = false;
      }
      j++;
    }

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
    gbc.insets = new Insets(0, 10, 0, 10);
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;
    return gbc;
  }
}

