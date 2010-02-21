package tvbrowsermini;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.customizableitems.SelectableItemList;

import com.jgoodies.forms.layout.CellConstraints;

import devplugin.Channel;
import devplugin.Plugin;
import devplugin.ProgramFieldType;
import devplugin.SettingsTab;


class TVBrowserMiniSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TVBrowserMiniSettingsTab.class);
  private TVBrowserMiniSettings mSettings;
  private JTextField tfPath;
  private JRadioButton rbAccepted;
  private JComboBox cbDays;
  private Frame parentFrame;
  private JComboBox cbDevice;

  private SelectableItemList mChannelList;
  private SelectableItemList mFieldsList;


  protected TVBrowserMiniSettingsTab(TVBrowserMiniSettings settings, Frame aFrame) {
    this.mSettings = settings;
    parentFrame = aFrame;
  }


  public JPanel createSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    EnhancedPanelBuilder panel = new EnhancedPanelBuilder("5dlu,pref,3dlu,pref:grow,3dlu,pref");

    // device
    cbDevice = new JComboBox();
    cbDevice.addItem("PDA");
    cbDevice.addItem("Android");
    if (mSettings.isDeviceAndroid()) {
      cbDevice.setSelectedIndex(1);
    } else {
      cbDevice.setSelectedIndex(0);
    }

    panel.addRow();
    panel.add(new JLabel(mLocalizer.msg("device", "Device")), cc.xy(2, panel.getRowCount()));
    panel.add(cbDevice, cc.xyw(4, panel.getRowCount(), panel.getColumnCount() - 3));

    // days
    this.cbDays = new JComboBox();
    this.cbDays.addItem(mLocalizer.msg("alldays", "all days"));
    this.cbDays.addItem(mLocalizer.msg("oneday", "max. 1 day"));
    for (int i = 2; i < 29; i++) {
      this.cbDays.addItem(mLocalizer.msg("max", "max.") + " " + i + " " + mLocalizer.msg("day", "day"));
    }
    this.cbDays.setSelectedIndex(mSettings.getDaysToExport());

    panel.addRow();
    panel.add(new JLabel(mLocalizer.msg("days", "Days to export")), cc.xy(2, panel.getRowCount()));
    panel.add(cbDays, cc.xyw(4, panel.getRowCount(), panel.getColumnCount() - 3));

    // path
    tfPath = new JTextField(mSettings.getPath(), 25);
    JButton browse = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT));
    browse.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new util.ui.ExtensionFileFilter(".tvd", "TV-Browser Data File (*.tvd)"));
        int retVal = fileChooser.showSaveDialog(UiUtilities.getBestDialogParent(parentFrame.getParent()));
        if (retVal == JFileChooser.APPROVE_OPTION) {
          if (fileChooser.getSelectedFile().toString().toLowerCase().endsWith(".tvd")) {
            tfPath.setText(fileChooser.getSelectedFile().getPath());
          } else {
            tfPath.setText(fileChooser.getSelectedFile().getPath() + ".tvd");
          }
        }
      }
    });

    panel.addRow();
    panel.add(new JLabel(mLocalizer.msg("file", "File name")), cc.xy(2, panel.getRowCount()));
    panel.add(tfPath, cc.xy(4, panel.getRowCount()));
    panel.add(browse, cc.xy(6, panel.getRowCount()));

    // disclaimer
    JTextArea disclaim = new JTextArea();
    disclaim.setText(mLocalizer.msg("nubs", "I accept to use the data exported with this plugin only with the applications authorized by the manufacturer of TV-Browser (www.tvbrowser.org). Using the data in other applications is prohibited. \nFor further questions look in the TV-Browser Wiki: http://enwiki.tvbrowser.org/"));
    disclaim.setEditable(false);
    JScrollPane scrollMain = new JScrollPane(disclaim);
    scrollMain.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollMain.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    disclaim.setLineWrap(true);
    disclaim.setWrapStyleWord(true);
    rbAccepted = new JRadioButton(mLocalizer.msg("accept", "accept"));
    JRadioButton rbNotAccepted = new JRadioButton(mLocalizer.msg("reject", "don't accept"));
    if (mSettings.getAccepted()) {
      rbAccepted.setSelected(true);
    } else {
      rbNotAccepted.setSelected(true);
    }

    ButtonGroup bg = new ButtonGroup();
    bg.add(rbAccepted);
    bg.add(rbNotAccepted);

    EnhancedPanelBuilder disclaimerPanel = new EnhancedPanelBuilder("pref:grow");
    disclaimerPanel.addGrowingRow();
    disclaimerPanel.add(disclaim, cc.xy(1, disclaimerPanel.getRowCount()));
    disclaimerPanel.addRow();
    disclaimerPanel.add(rbAccepted, cc.xy(1, disclaimerPanel.getRowCount()));
    disclaimerPanel.addRow();
    disclaimerPanel.add(rbNotAccepted, cc.xy(1, disclaimerPanel.getRowCount()));

    // channels
    JPanel channelPanel = new JPanel(new BorderLayout());
    mChannelList = new SelectableItemList(TVBrowserMini.getInstance().getSelectedChannels(), Plugin.getPluginManager().getSubscribedChannels());
    channelPanel.add(mChannelList, BorderLayout.CENTER);

    // fields
    JPanel fieldsPanel = new JPanel(new BorderLayout());
    mFieldsList = new SelectableItemList(TVBrowserMini.getInstance().getSelectedFields(), TVBrowserMini.getInstance().getAvailableFields());
    fieldsPanel.add(mFieldsList, BorderLayout.CENTER);

    // tabbed pane
    panel.addGrowingRow();

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add(mLocalizer.msg("disclaimer", "Disclaimer"), disclaimerPanel.getPanel());
    tabbedPane.add(mLocalizer.msg("tabChannels", "Channel"), channelPanel);
    tabbedPane.add(mLocalizer.msg("tabElements", "Elements"), fieldsPanel);

    panel.add(tabbedPane, cc.xyw(1, panel.getRowCount(), panel.getColumnCount()));

    return panel.getPanel();
  }

  public Icon getIcon() {
    return TVBrowserMini.getInstance().getPluginIcon();
  }

  public String getTitle() {
    return TVBrowserMini.mLocalizer.msg("pluginName", "TV-Browser Mini Export");
  }

  public void saveSettings() {
    mSettings.setPath(tfPath.getText());
    mSettings.setAccepted(rbAccepted.isSelected());
    mSettings.setDaysToExport(this.cbDays.getSelectedIndex());
    mSettings.setAndroidDevice(cbDevice.getSelectedIndex() == 1);
    // Save the channels
    Object[] o = mChannelList.getSelection();
    Channel[] selectedChannels = new Channel[o.length];

    for (int i = 0; i < o.length; i++) {
      selectedChannels[i] = (Channel) o[i];
    }

    TVBrowserMini.getInstance().setSelectedChannels(selectedChannels);

    // save program fields
    Object[] fields = mFieldsList.getSelection();
    ArrayList<ProgramFieldType> selected = new ArrayList<ProgramFieldType>(fields.length);
    for (int i = 0; i < fields.length; i++) {
      selected.add((ProgramFieldType) fields[i]);
    }
    for (ProgramFieldType programFieldType : TVBrowserMini.getInstance().getAvailableFields()) {
      mSettings.setProgramField(programFieldType, selected.contains(programFieldType));
    }
  }

}
