/**
 * Created on 20.06.2010
 */
package captureplugin.drivers.topfield;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import util.ui.ChannelTableCellRenderer;
import util.ui.Localizer;
import util.ui.ProgramReceiveTargetSelectionPanel;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import captureplugin.CapturePlugin;
import captureplugin.drivers.topfield.connector.TopfieldConnectionException;
import captureplugin.drivers.topfield.connector.TopfieldConnector;
import captureplugin.utils.ExternalChannelTableCellEditor;
import captureplugin.utils.ExternalChannelTableCellRenderer;
import devplugin.Channel;
import devplugin.Plugin;

/**
 * Dialog to configure a Topfield device.
 * 
 * @author Wolfgang Reh
 */
public class TopfieldConfigurationDialog extends JDialog implements WindowClosingIf {
  private static final Localizer localizer = Localizer.getLocalizerFor(TopfieldConfigurationDialog.class); // @jve:decl-index=0:
  private static final Font BORDER_FONT = new Font("SansSerif", Font.BOLD, 12);
  private static final Color BORDER_COLOR = new Color(59, 59, 59);

  private static final String DIALOG_TITLE = "title"; // @jve:decl-index=0:
  private static final String DEFAULT_TITLE = "Configure Topfield";
  private static final String DEVICE_NAME_LABEL = "deviceNameLabel"; // @jve:decl-index=0:
  private static final String DEFAULT_DEVICE_NAME_LABEL = "Device name:"; // @jve:decl-index=0:
  private static final String DEVICE_ADDRESS_LABEL = "addressLabel"; // @jve:decl-index=0:
  private static final String DEFAULT_DEVICE_ADDRESS_LABEL = "Device address:"; // @jve:decl-index=0:
  private static final String USERNAME_LABEL = "usernameLabel";
  private static final String DEFAULT_USERNAME_LABEL = "User:";
  private static final String PASSWORD_LABEL = "passwordLabel";
  private static final String DEFAULT_PASSWORD_LABEL = "Password:"; // @jve:decl-index=0:
  private static final String GET_CHANNELS_BUTTON = "getChannelsButton"; // @jve:decl-index=0:
  private static final String DEFAULT_GET_CHANNELS_BUTTON = "Get device channels"; // @jve:decl-index=0:
  private static final String ASSIGN_CHANNELS_BUTTON = "assignButton"; // @jve:decl-index=0:
  private static final String DEFAULT_ASSIGN_CHANNELS_BUTTON = "Automatically assign channels"; // @jve:decl-index=0:
  private static final String MISSING_ACCESS_DATA = "missingAccessData"; // @jve:decl-index=0:
  private static final String DEFAULT_MISSING_ACCESS_DATA = "Address or user name is missing"; // @jve:decl-index=0:
  private static final String INVALID_ADDRESS = "invalidAddress"; // @jve:decl-index=0:
  private static final String DEFAULT_INVALID_ADDRESS = "Couldn't contact device, check address"; // @jve:decl-index=0:
  private static final String PREROLL_LABEL = "defaultPreroll"; // @jve:decl-index=0:
  private static final String DEFAULT_PREROLL_LABEL = "Default preroll (min):"; // @jve:decl-index=0:
  private static final String POSTROLL_LABEL = "defaultPostroll"; // @jve:decl-index=0:
  private static final String DEFAULT_POSTROLL_LABEL = "Default postroll (min):"; // @jve:decl-index=0:
  private static final String BASIC_PANEL_LABEL = "basicPanel"; // @jve:decl-index=0:
  private static final String DEFAULT_BASIC_PANEL_LABEL = "Basic"; // @jve:decl-index=0:
  private static final String EXTENDED_PANEL_LABEL = "extendedPanel"; // @jve:decl-index=0:
  private static final String DEFAULT_EXTENDED_PANEL_LABEL = "Extended"; // @jve:decl-index=0:
  private static final String SEND_TO_TITLE = "sendToTitle"; // @jve:decl-index=0:
  private static final String DEFAULT_SEND_TO_TITLE = "Send scheduled programs to:"; // @jve:decl-index=0:
  private static final String RECORDING_LIST_TITLE = "recordingListTitle"; // @jve:decl-index=0:
  private static final String DEFAULT_RECORDING_LIST_TITLE = "Maintain list of recordings:"; // @jve:decl-index=0:
  private static final String TV_BROWSER_RADIO = "tvBrowserRadio"; // @jve:decl-index=0:
  private static final String DEFAULT_TV_BROWSER_RADIO = "in TVBrowser"; // @jve:decl-index=0:
  private static final String DEVICE_RADIO = "deviceRadio"; // @jve:decl-index=0:
  private static final String DEFAULT_DEVICE_RADIO = "on device"; // @jve:decl-index=0:
  private static final String USE_TUNER_4 = "useTuner4"; // @jve:decl-index=0:
  private static final String DEFAULT_USE_TUNER_4 = "Use tuner 4"; // @jve:decl-index=0:
  private static final String CONNECTION_TO_LABEL = "connectionTimoutLabel"; // @jve:decl-index=0:
  private static final String DEFAULT_CONNECTION_TO_LABEL = "Connection timeout (ms):"; // @jve:decl-index=0:
  private static final String CORRECT_TIME_CHECK = "correctTime"; // @jve:decl-index=0:
  private static final String DEFAULT_CORRECT_TIME_CHECK = "Account for time addition on receiver"; // @jve:decl-index=0:

  private boolean configurationOK = false;
  private final TopfieldDevice device;
  private final TopfieldConfiguration configuration;
  private ArrayList<TopfieldServiceInfo> deviceChannels; // @jve:decl-index=0:
  private final Channel[] browserChannels = Plugin.getPluginManager().getSubscribedChannels();
  private TopfieldChannelTableModel channelTableModel;

  private final ButtonGroup recordingsRadios = new ButtonGroup(); // @jve:decl-index=0:
  private JPanel configurationPane = null;
  private JPanel closingPanel = null;
  private JButton okButton = null;
  private JButton cancelButton = null;
  private JPanel valuesPanel = null;
  private JLabel deviceNameLabel = null;
  private JTextField deviceNameEditor = null;
  private JLabel deviceAddressLabel = null;
  private JTextField deviceAddressEditor = null;
  private JLabel userNameLabel = null;
  private JTextField usernameEditor = null;
  private JLabel passwordLabel = null;
  private JPasswordField passwordEditor = null;
  private JButton getDeviceChannelsButton = null;
  private JScrollPane channelScrollPane = null;
  private JTable channelTable = null;
  private JLabel defaultPrerollLabel = null;
  private JLabel defaultPostrollLabel = null;
  private JSpinner defaultPrerollSpinner = null;
  private JSpinner defaultPostrollSpinner = null;
  private JButton automaticAssignButton = null;
  private JTabbedPane settingsTabbedPane = null;
  private JPanel basicPanel = null;
  private JPanel timeExtensionPanel = null;
  private JLabel layoutLabel0 = null;
  private JPanel extendedPanel = null;
  private ProgramReceiveTargetSelectionPanel passOnComponent = null;
  private JPanel recordingListPanel = null;
  private JRadioButton tvBrowserRadio = null;
  private JRadioButton deviceRadio = null;
  private JPanel passOnPanel = null;
  private JCheckBox tuner4Check = null;
  private JLabel connectionTimeoutLabel = null;
  private JSpinner connectionTimoutSpinner = null;
  private JCheckBox correctTimeCheck = null;

  /**
   * Configure a Topfield device.
   * 
   * @param parent
   *          The parent window
   * @param device
   *          The device to use
   * @param configuration
   *          The configuration to modify
   */
  public TopfieldConfigurationDialog(Window parent, TopfieldDevice device, TopfieldConfiguration configuration) {
    super(parent);
    this.device = device;
    this.configuration = configuration.clone();
    initialize();
    setModal(true);
    prefillData();
    UiUtilities.registerForClosing(this);
  }

  /**
   * Fill in initial values.
   */
  private void prefillData() {
    deviceNameEditor.setText(device.getName());
    deviceAddressEditor.setText(configuration.getDeviceAddress());
    usernameEditor.setText(configuration.getUsername());
    passwordEditor.setText(configuration.getPassword());
    defaultPrerollSpinner.setValue(configuration.getDefaultPreroll());
    defaultPostrollSpinner.setValue(configuration.getDefaultPostroll());
    tuner4Check.setSelected(configuration.isUseTuner4());
    correctTimeCheck.setSelected(configuration.isCorrectTime());
    tvBrowserRadio.setSelected(configuration.isRecordingsLocalUnchecked());
    deviceRadio.setSelected(!configuration.isRecordingsLocalUnchecked());
    connectionTimoutSpinner.setValue(configuration.getConnectionTimeout());
  }

  /**
   * Retrieve the channels from the device.
   */
  private void getDeviceChannels() {
    String deviceAddress = deviceAddressEditor.getText().trim();
    String username = usernameEditor.getText().trim();
    char[] password = passwordEditor.getPassword();

    if ((deviceAddress == null) || (username == null)) {
      JOptionPane.showMessageDialog(this, localizer.msg(MISSING_ACCESS_DATA, DEFAULT_MISSING_ACCESS_DATA), localizer
          .msg(DIALOG_TITLE, DEFAULT_TITLE), JOptionPane.ERROR_MESSAGE);
      return;
    }

    configuration.setDeviceAddress(deviceAddress);
    configuration.setUsername(username);
    configuration.setPassword(new String(password));
    configuration.setDefaultPreroll((Integer) defaultPrerollSpinner.getValue());
    configuration.setDefaultPostroll((Integer) defaultPostrollSpinner.getValue());

    TopfieldConnector connector = new TopfieldConnector(configuration);

    try {
      deviceChannels = connector.getDeviceChannels();
      configuration.setDeviceChannels(deviceChannels);
    } catch (TopfieldConnectionException e) {
      JOptionPane.showMessageDialog(this, localizer.msg(INVALID_ADDRESS, DEFAULT_INVALID_ADDRESS), localizer.msg(
          DIALOG_TITLE, DEFAULT_TITLE), JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Automatically assign the channels.
   */
  private void doAutomaticAssignment() {
    for (Channel browserChannel : browserChannels) {
      String browserChannelName = normalizeName(browserChannel.getName());
      for (TopfieldServiceInfo service : deviceChannels) {
        if (normalizeName(service.getName()).compareTo(browserChannelName) == 0) {
          configuration.setExternalChannel(browserChannel, service);
          break;
        }
      }
    }
    channelTableModel.fireTableDataChanged();
  }

  /**
   * Normalizes a channel name. Lower case and no spaces.
   * 
   * @param name
   *          channel name
   * @return normalized channel name
   */
  private String normalizeName(String name) {
    return name.toLowerCase().replaceAll("\\W", "");
  }

  /**
   * @return the configuration
   */
  public TopfieldConfiguration getConfiguration() {
    return (configuration);
  }

  /**
   * This method initializes this
   */
  private void initialize() {
    this.setSize(new Dimension(500, 700));
    this.setTitle(localizer.msg(DIALOG_TITLE, DEFAULT_TITLE));
    this.setContentPane(getConfigurationPane());
  }

  /*
   * (non-Javadoc)
   * 
   * @see util.ui.WindowClosingIf#close()
   */
  @Override
  public void close() {
    setVisible(false);
  }

  /**
   * @return <code>true</code> if the OK button was pressed
   */
  public boolean configurationOK() {
    return configurationOK;
  }

  /**
   * @return The device name
   */
  public String getDeviceName() {
    return deviceNameEditor.getText().trim();
  }

  /**
   * Validate that essential data are present.
   */
  private void validateInput() {
    configurationOK = (deviceNameEditor.getText().trim().length() > 0);
    configurationOK = configurationOK && (deviceAddressEditor.getText().trim().length() > 0);

    if (configurationOK) {
      configuration.setDeviceAddress(deviceAddressEditor.getText().trim());
      configuration.setUsername(usernameEditor.getText().trim());
      configuration.setPassword(new String(passwordEditor.getPassword()));
      configuration.setDefaultPreroll((Integer) defaultPrerollSpinner.getValue());
      configuration.setDefaultPostroll((Integer) defaultPostrollSpinner.getValue());
      configuration.setUseTuner4(tuner4Check.isSelected());
      configuration.setCorrectTime(correctTimeCheck.isSelected());
      configuration.setReceiveTargets(passOnComponent.getCurrentSelection());
    }
    setVisible(!configurationOK);
  }

  /**
   * This method initializes configurationPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getConfigurationPane() {
    if (configurationPane == null) {
      GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
      gridBagConstraints11.gridx = 0;
      gridBagConstraints11.anchor = GridBagConstraints.NORTH;
      gridBagConstraints11.fill = GridBagConstraints.BOTH;
      gridBagConstraints11.weightx = 1.0;
      gridBagConstraints11.weighty = 1.0;
      gridBagConstraints11.insets = new Insets(5, 5, 5, 5);
      gridBagConstraints11.gridy = 0;
      GridBagConstraints gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.anchor = GridBagConstraints.SOUTH;
      gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new Insets(0, 5, 5, 5);
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.gridy = 2;
      configurationPane = new JPanel();
      configurationPane.setLayout(new GridBagLayout());
      configurationPane.add(getClosingPanel(), gridBagConstraints);
      configurationPane.add(getValuesPanel(), gridBagConstraints11);
    }
    return configurationPane;
  }

  /**
   * This method initializes closingPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getClosingPanel() {
    if (closingPanel == null) {
      GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
      gridBagConstraints2.insets = new Insets(0, 0, 0, 5);
      GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
      gridBagConstraints1.gridx = 1;
      gridBagConstraints1.insets = new Insets(0, 5, 0, 0);
      gridBagConstraints1.gridy = 0;
      closingPanel = new JPanel();
      closingPanel.setLayout(new GridBagLayout());
      closingPanel.add(getOkButton(), gridBagConstraints2);
      closingPanel.add(getCancelButton(), gridBagConstraints1);
    }
    return closingPanel;
  }

  /**
   * This method initializes okButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getOkButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setText(Localizer.getLocalization(Localizer.I18N_OK));
      okButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          validateInput();
        }
      });
    }
    return okButton;
  }

  /**
   * This method initializes cancelButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setText(Localizer.getLocalization(Localizer.I18N_CANCEL));
      cancelButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          close();
        }
      });
    }
    return cancelButton;
  }

  /**
   * This method initializes valuesPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getValuesPanel() {
    if (valuesPanel == null) {
      GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
      gridBagConstraints19.fill = GridBagConstraints.BOTH;
      gridBagConstraints19.gridy = 1;
      gridBagConstraints19.weightx = 1.0;
      gridBagConstraints19.weighty = 1.0;
      gridBagConstraints19.gridwidth = 2;
      gridBagConstraints19.anchor = GridBagConstraints.CENTER;
      gridBagConstraints19.gridx = 0;
      defaultPostrollLabel = new JLabel();
      defaultPostrollLabel.setText(localizer.msg(POSTROLL_LABEL, DEFAULT_POSTROLL_LABEL));
      defaultPrerollLabel = new JLabel();
      defaultPrerollLabel.setText(localizer.msg(PREROLL_LABEL, DEFAULT_PREROLL_LABEL));
      passwordLabel = new JLabel();
      passwordLabel.setText(localizer.msg(PASSWORD_LABEL, DEFAULT_PASSWORD_LABEL));
      userNameLabel = new JLabel();
      userNameLabel.setText(localizer.msg(USERNAME_LABEL, DEFAULT_USERNAME_LABEL));
      deviceAddressLabel = new JLabel();
      deviceAddressLabel.setText(localizer.msg(DEVICE_ADDRESS_LABEL, DEFAULT_DEVICE_ADDRESS_LABEL));
      GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
      gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
      gridBagConstraints4.gridy = 0;
      gridBagConstraints4.weightx = 1.0;
      gridBagConstraints4.anchor = GridBagConstraints.WEST;
      gridBagConstraints4.insets = new Insets(0, 3, 0, 0);
      gridBagConstraints4.gridx = 1;
      GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
      gridBagConstraints3.gridx = 0;
      gridBagConstraints3.anchor = GridBagConstraints.EAST;
      gridBagConstraints3.gridy = 0;
      deviceNameLabel = new JLabel();
      deviceNameLabel.setText(localizer.msg(DEVICE_NAME_LABEL, DEFAULT_DEVICE_NAME_LABEL));
      valuesPanel = new JPanel();
      valuesPanel.setLayout(new GridBagLayout());
      valuesPanel.add(deviceNameLabel, gridBagConstraints3);
      valuesPanel.add(getDeviceNameEditor(), gridBagConstraints4);
      valuesPanel.add(getSettingsTabbedPane(), gridBagConstraints19);
    }
    return valuesPanel;
  }

  /**
   * This method initializes deviceNameEditor
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getDeviceNameEditor() {
    if (deviceNameEditor == null) {
      deviceNameEditor = new JTextField();
    }
    return deviceNameEditor;
  }

  /**
   * This method initializes deviceAddressEditor
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getDeviceAddressEditor() {
    if (deviceAddressEditor == null) {
      deviceAddressEditor = new JTextField();
    }
    return deviceAddressEditor;
  }

  /**
   * This method initializes usernameEditor
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getUsernameEditor() {
    if (usernameEditor == null) {
      usernameEditor = new JTextField();
    }
    return usernameEditor;
  }

  /**
   * This method initializes passwordEditor
   * 
   * @return javax.swing.JPasswordField
   */
  private JPasswordField getPasswordEditor() {
    if (passwordEditor == null) {
      passwordEditor = new JPasswordField();
    }
    return passwordEditor;
  }

  /**
   * This method initializes getDeviceChannelsButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getGetDeviceChannelsButton() {
    if (getDeviceChannelsButton == null) {
      getDeviceChannelsButton = new JButton();
      getDeviceChannelsButton.setText(localizer.msg(GET_CHANNELS_BUTTON, DEFAULT_GET_CHANNELS_BUTTON));
      getDeviceChannelsButton.setIcon(TVBrowserIcons.refresh(TVBrowserIcons.SIZE_SMALL));
      getDeviceChannelsButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          getDeviceChannels();
        }
      });
    }
    return getDeviceChannelsButton;
  }

  /**
   * This method initializes channelScrollPane
   * 
   * @return javax.swing.JScrollPane
   */
  private JScrollPane getChannelScrollPane() {
    if (channelScrollPane == null) {
      channelScrollPane = new JScrollPane();
      channelScrollPane.setViewportView(getChannelTable());
    }
    return channelScrollPane;
  }

  /**
   * This method initializes channelTable
   * 
   * @return javax.swing.JTable
   */
  private JTable getChannelTable() {
    if (channelTable == null) {
      channelTable = new JTable();
      channelTableModel = new TopfieldChannelTableModel(configuration);
      channelTable.setModel(channelTableModel);
      channelTable.getTableHeader().setReorderingAllowed(false);
      TableColumnModel columnModel = channelTable.getColumnModel();
      columnModel.getColumn(0).setCellRenderer(new ChannelTableCellRenderer());
      columnModel.getColumn(1).setCellRenderer(new ExternalChannelTableCellRenderer());
      columnModel.getColumn(1).setCellEditor(new ExternalChannelTableCellEditor(configuration));
      columnModel.getColumn(2).setCellEditor(new TopfieldRollTableCellEditor());
      columnModel.getColumn(3).setCellEditor(new TopfieldRollTableCellEditor());

      // Set column width for pre and post roll columns
      TableCellRenderer cellRenderer = channelTable.getTableHeader().getDefaultRenderer();
      Component rendererComponent = cellRenderer.getTableCellRendererComponent(channelTable, channelTableModel
          .getColumnName(2), false, false, -1, 2);
      columnModel.getColumn(2).setMaxWidth(rendererComponent.getPreferredSize().width);
      rendererComponent = cellRenderer.getTableCellRendererComponent(channelTable, channelTableModel.getColumnName(3),
          false, false, -1, 3);
      columnModel.getColumn(3).setMaxWidth(rendererComponent.getPreferredSize().width);
    }
    return channelTable;
  }

  /**
   * This method initializes defaultPrerollSpinner
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getDefaultPrerollSpinner() {
    if (defaultPrerollSpinner == null) {
      defaultPrerollSpinner = new JSpinner();
      SpinnerNumberModel spinnerModel = new SpinnerNumberModel();
      spinnerModel.setMinimum(0);
      spinnerModel.setMaximum(90);
      defaultPrerollSpinner.setModel(spinnerModel);
    }
    return defaultPrerollSpinner;
  }

  /**
   * This method initializes defaultPostrollSpinner
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getDefaultPostrollSpinner() {
    if (defaultPostrollSpinner == null) {
      defaultPostrollSpinner = new JSpinner();
      SpinnerNumberModel spinnerModel = new SpinnerNumberModel();
      spinnerModel.setMinimum(0);
      spinnerModel.setMaximum(90);
      defaultPostrollSpinner.setModel(spinnerModel);
    }
    return defaultPostrollSpinner;
  }

  /**
   * This method initializes automaticAssignButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getAutomaticAssignButton() {
    if (automaticAssignButton == null) {
      automaticAssignButton = new JButton();
      automaticAssignButton.setText(localizer.msg(ASSIGN_CHANNELS_BUTTON, DEFAULT_ASSIGN_CHANNELS_BUTTON));
      automaticAssignButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          doAutomaticAssignment();
        }
      });
    }
    return automaticAssignButton;
  }

  /**
   * This method initializes settingsTabbedPane
   * 
   * @return javax.swing.JTabbedPane
   */
  private JTabbedPane getSettingsTabbedPane() {
    if (settingsTabbedPane == null) {
      settingsTabbedPane = new JTabbedPane();
      settingsTabbedPane.addTab(localizer.msg(BASIC_PANEL_LABEL, DEFAULT_BASIC_PANEL_LABEL), null, getBasicPanel(),
          null);
      settingsTabbedPane.addTab(localizer.msg(EXTENDED_PANEL_LABEL, DEFAULT_EXTENDED_PANEL_LABEL), null,
          getExtendedPanel(), null);
    }
    return settingsTabbedPane;
  }

  /**
   * This method initializes basicPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getBasicPanel() {
    if (basicPanel == null) {
      GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
      gridBagConstraints18.anchor = GridBagConstraints.EAST;
      gridBagConstraints18.gridy = 6;
      gridBagConstraints18.gridx = 1;
      GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
      gridBagConstraints13.fill = GridBagConstraints.BOTH;
      gridBagConstraints13.gridx = 0;
      gridBagConstraints13.gridy = 5;
      gridBagConstraints13.weightx = 1.0;
      gridBagConstraints13.weighty = 1.0;
      gridBagConstraints13.gridwidth = 2;
      GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
      gridBagConstraints12.gridwidth = 2;
      gridBagConstraints12.gridy = 4;
      gridBagConstraints12.gridx = 0;
      GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
      gridBagConstraints20.gridx = 0;
      gridBagConstraints20.fill = GridBagConstraints.HORIZONTAL;
      gridBagConstraints20.weightx = 1.0;
      gridBagConstraints20.gridwidth = 2;
      gridBagConstraints20.anchor = GridBagConstraints.CENTER;
      gridBagConstraints20.gridy = 3;
      GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
      gridBagConstraints10.anchor = GridBagConstraints.WEST;
      gridBagConstraints10.insets = new Insets(0, 3, 0, 0);
      gridBagConstraints10.gridx = 1;
      gridBagConstraints10.gridy = 2;
      gridBagConstraints10.weightx = 1.0;
      gridBagConstraints10.fill = GridBagConstraints.HORIZONTAL;
      GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
      gridBagConstraints9.anchor = GridBagConstraints.EAST;
      gridBagConstraints9.gridy = 2;
      gridBagConstraints9.gridx = 0;
      GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
      gridBagConstraints8.anchor = GridBagConstraints.WEST;
      gridBagConstraints8.insets = new Insets(0, 3, 0, 0);
      gridBagConstraints8.gridx = 1;
      gridBagConstraints8.gridy = 1;
      gridBagConstraints8.weightx = 1.0;
      gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
      GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
      gridBagConstraints7.anchor = GridBagConstraints.EAST;
      gridBagConstraints7.gridy = 1;
      gridBagConstraints7.gridx = 0;
      GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
      gridBagConstraints6.anchor = GridBagConstraints.WEST;
      gridBagConstraints6.insets = new Insets(0, 3, 0, 0);
      gridBagConstraints6.gridx = 1;
      gridBagConstraints6.gridy = 0;
      gridBagConstraints6.weightx = 1.0;
      gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
      GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
      gridBagConstraints5.anchor = GridBagConstraints.EAST;
      gridBagConstraints5.gridy = -1;
      gridBagConstraints5.gridx = -1;
      basicPanel = new JPanel();
      basicPanel.setLayout(new GridBagLayout());
      basicPanel.add(deviceAddressLabel, gridBagConstraints5);
      basicPanel.add(getDeviceAddressEditor(), gridBagConstraints6);
      basicPanel.add(userNameLabel, gridBagConstraints7);
      basicPanel.add(getUsernameEditor(), gridBagConstraints8);
      basicPanel.add(passwordLabel, gridBagConstraints9);
      basicPanel.add(getPasswordEditor(), gridBagConstraints10);
      basicPanel.add(getTimeExtensionPanel(), gridBagConstraints20);
      basicPanel.add(getGetDeviceChannelsButton(), gridBagConstraints12);
      basicPanel.add(getChannelScrollPane(), gridBagConstraints13);
      basicPanel.add(getAutomaticAssignButton(), gridBagConstraints18);
    }
    return basicPanel;
  }

  /**
   * This method initializes timeExtensionPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getTimeExtensionPanel() {
    if (timeExtensionPanel == null) {
      GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
      gridBagConstraints21.gridx = 4;
      gridBagConstraints21.fill = GridBagConstraints.HORIZONTAL;
      gridBagConstraints21.weightx = 1.0;
      gridBagConstraints21.gridy = 0;
      layoutLabel0 = new JLabel();
      layoutLabel0.setText("");
      GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
      gridBagConstraints17.anchor = GridBagConstraints.WEST;
      gridBagConstraints17.gridx = 3;
      gridBagConstraints17.gridy = 0;
      gridBagConstraints17.insets = new Insets(0, 3, 0, 0);
      GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
      gridBagConstraints15.anchor = GridBagConstraints.EAST;
      gridBagConstraints15.gridy = 0;
      gridBagConstraints15.insets = new Insets(0, 11, 0, 0);
      gridBagConstraints15.gridx = 2;
      GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
      gridBagConstraints16.anchor = GridBagConstraints.WEST;
      gridBagConstraints16.gridx = 1;
      gridBagConstraints16.gridy = 0;
      gridBagConstraints16.insets = new Insets(0, 3, 0, 0);
      GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
      gridBagConstraints14.anchor = GridBagConstraints.EAST;
      gridBagConstraints14.gridy = -1;
      gridBagConstraints14.gridx = -1;
      timeExtensionPanel = new JPanel();
      timeExtensionPanel.setLayout(new GridBagLayout());
      timeExtensionPanel.add(defaultPrerollLabel, gridBagConstraints14);
      timeExtensionPanel.add(getDefaultPrerollSpinner(), gridBagConstraints16);
      timeExtensionPanel.add(defaultPostrollLabel, gridBagConstraints15);
      timeExtensionPanel.add(getDefaultPostrollSpinner(), gridBagConstraints17);
      timeExtensionPanel.add(layoutLabel0, gridBagConstraints21);
    }
    return timeExtensionPanel;
  }

  /**
   * This method initializes extendedPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getExtendedPanel() {
    if (extendedPanel == null) {
      GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
      gridBagConstraints30.gridx = 0;
      gridBagConstraints30.fill = GridBagConstraints.HORIZONTAL;
      gridBagConstraints30.anchor = GridBagConstraints.WEST;
      gridBagConstraints30.weightx = 1.0;
      gridBagConstraints30.gridwidth = 2;
      gridBagConstraints30.gridy = 2;
      GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
      gridBagConstraints29.gridx = 1;
      gridBagConstraints29.anchor = GridBagConstraints.WEST;
      gridBagConstraints29.insets = new Insets(0, 3, 0, 0);
      gridBagConstraints29.gridy = 0;
      GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
      gridBagConstraints28.gridx = 0;
      gridBagConstraints28.anchor = GridBagConstraints.WEST;
      gridBagConstraints28.gridy = 0;
      connectionTimeoutLabel = new JLabel();
      connectionTimeoutLabel.setText(localizer.msg(CONNECTION_TO_LABEL, DEFAULT_CONNECTION_TO_LABEL));
      GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
      gridBagConstraints27.gridx = 0;
      gridBagConstraints27.anchor = GridBagConstraints.WEST;
      gridBagConstraints27.fill = GridBagConstraints.HORIZONTAL;
      gridBagConstraints27.weightx = 1.0;
      gridBagConstraints27.gridwidth = 2;
      gridBagConstraints27.gridy = 1;
      GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
      gridBagConstraints26.fill = GridBagConstraints.BOTH;
      gridBagConstraints26.weighty = 1.0;
      gridBagConstraints26.anchor = GridBagConstraints.SOUTH;
      gridBagConstraints26.gridx = 0;
      gridBagConstraints26.gridy = 4;
      gridBagConstraints26.gridwidth = 2;
      gridBagConstraints26.insets = new Insets(3, 0, 0, 0);
      gridBagConstraints26.weightx = 1.0;
      GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
      gridBagConstraints24.fill = GridBagConstraints.HORIZONTAL;
      gridBagConstraints24.gridx = 0;
      gridBagConstraints24.gridy = 3;
      gridBagConstraints24.gridwidth = 2;
      gridBagConstraints24.insets = new Insets(2, 0, 0, 0);
      gridBagConstraints24.weightx = 1.0;
      GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
      gridBagConstraints22.gridx = -1;
      gridBagConstraints22.fill = GridBagConstraints.NONE;
      gridBagConstraints22.weightx = 1.0;
      gridBagConstraints22.anchor = GridBagConstraints.CENTER;
      gridBagConstraints22.weighty = 1.0;
      gridBagConstraints22.gridy = -1;
      extendedPanel = new JPanel();
      extendedPanel.setLayout(new GridBagLayout());
      extendedPanel.add(getTuner4Check(), gridBagConstraints27);
      extendedPanel.add(getRecordingListPanel(), gridBagConstraints24);
      extendedPanel.add(getPassOnPanel(), gridBagConstraints26);
      extendedPanel.add(connectionTimeoutLabel, gridBagConstraints28);
      extendedPanel.add(getConnectionTimoutSpinner(), gridBagConstraints29);
      extendedPanel.add(getCorrectTimeCheck(), gridBagConstraints30);
    }
    return extendedPanel;
  }

  /**
   * This method initializes passOnComponent
   * 
   * @return util.ui.ProgramReceiveTargetSelectionPanel
   */
  private ProgramReceiveTargetSelectionPanel getPassOnComponent() {
    if (passOnComponent == null) {
      passOnComponent = new ProgramReceiveTargetSelectionPanel(UiUtilities.getLastModalChildOf(CapturePlugin
          .getInstance().getSuperFrame()), configuration.getReceiveTargets(), null, CapturePlugin.getInstance(), false,
          null);
    }
    return passOnComponent;
  }

  /**
   * This method initializes recordingListPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getRecordingListPanel() {
    if (recordingListPanel == null) {
      TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
          Color.black), localizer.msg(RECORDING_LIST_TITLE, DEFAULT_RECORDING_LIST_TITLE),
          TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, BORDER_FONT, BORDER_COLOR);
      titledBorder.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.lightGray));
      GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
      gridBagConstraints25.gridx = 0;
      gridBagConstraints25.ipadx = 0;
      gridBagConstraints25.fill = GridBagConstraints.HORIZONTAL;
      gridBagConstraints25.weightx = 1.0;
      gridBagConstraints25.gridy = 2;
      GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
      gridBagConstraints23.gridx = 0;
      gridBagConstraints23.ipadx = 0;
      gridBagConstraints23.anchor = GridBagConstraints.WEST;
      gridBagConstraints23.fill = GridBagConstraints.HORIZONTAL;
      gridBagConstraints23.weightx = 1.0;
      gridBagConstraints23.gridy = 1;
      recordingListPanel = new JPanel();
      recordingListPanel.setLayout(new GridBagLayout());
      recordingListPanel.setBorder(titledBorder);
      recordingListPanel.add(getTvBrowserRadio(), gridBagConstraints23);
      recordingListPanel.add(getDeviceRadio(), gridBagConstraints25);
    }
    return recordingListPanel;
  }

  /**
   * This method initializes tvBrowserRadio
   * 
   * @return javax.swing.JRadioButton
   */
  private JRadioButton getTvBrowserRadio() {
    if (tvBrowserRadio == null) {
      tvBrowserRadio = new JRadioButton();
      tvBrowserRadio.setText(localizer.msg(TV_BROWSER_RADIO, DEFAULT_TV_BROWSER_RADIO));
      tvBrowserRadio.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(java.awt.event.ItemEvent e) {
          changeRecordingsLocal();
        }
      });
      recordingsRadios.add(tvBrowserRadio);
    }
    return tvBrowserRadio;
  }

  /**
   * Set the recordings local flag in the configuration.
   */
  private void changeRecordingsLocal() {
    configuration.setRecordingsLocal(tvBrowserRadio.isSelected());
  }

  /**
   * This method initializes deviceRadio
   * 
   * @return javax.swing.JRadioButton
   */
  private JRadioButton getDeviceRadio() {
    if (deviceRadio == null) {
      deviceRadio = new JRadioButton();
      deviceRadio.setText(localizer.msg(DEVICE_RADIO, DEFAULT_DEVICE_RADIO));
      deviceRadio.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(java.awt.event.ItemEvent e) {
          changeRecordingsLocal();
        }
      });
      recordingsRadios.add(deviceRadio);
    }
    return deviceRadio;
  }

  /**
   * This method initializes passOnPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getPassOnPanel() {
    if (passOnPanel == null) {
      passOnPanel = new JPanel();
      passOnPanel.setLayout(new GridBagLayout());
      passOnPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
          Color.lightGray), localizer.msg(SEND_TO_TITLE, DEFAULT_SEND_TO_TITLE), TitledBorder.DEFAULT_JUSTIFICATION,
          TitledBorder.TOP, BORDER_FONT, BORDER_COLOR));
      GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
      gridBagConstraints22.gridx = 0;
      gridBagConstraints22.ipadx = 0;
      gridBagConstraints22.fill = GridBagConstraints.BOTH;
      gridBagConstraints22.weightx = 1.0;
      gridBagConstraints22.weighty = 1.0;
      gridBagConstraints22.gridy = 0;
      passOnPanel.add(getPassOnComponent(), gridBagConstraints22);
    }
    return passOnPanel;
  }

  /**
   * This method initializes tuner4Check
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getTuner4Check() {
    if (tuner4Check == null) {
      tuner4Check = new JCheckBox();
      tuner4Check.setText(localizer.msg(USE_TUNER_4, DEFAULT_USE_TUNER_4));
    }
    return tuner4Check;
  }

  /**
   * This method initializes connectionTimoutSpinner
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getConnectionTimoutSpinner() {
    if (connectionTimoutSpinner == null) {
      connectionTimoutSpinner = new JSpinner();
      SpinnerNumberModel spinnerModel = new SpinnerNumberModel();
      spinnerModel.setMinimum(10);
      spinnerModel.setMaximum(120000);
      connectionTimoutSpinner.setModel(spinnerModel);
    }
    return connectionTimoutSpinner;
  }

  /**
   * This method initializes correctTimeCheck
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getCorrectTimeCheck() {
    if (correctTimeCheck == null) {
      correctTimeCheck = new JCheckBox();
      correctTimeCheck.setText(localizer.msg(CORRECT_TIME_CHECK, DEFAULT_CORRECT_TIME_CHECK));
    }
    return correctTimeCheck;
  }
} // @jve:decl-index=0:visual-constraint="10,10"
