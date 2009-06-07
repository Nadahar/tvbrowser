package tvbrowsermini;

import com.jgoodies.forms.factories.Borders;
import devplugin.Channel;
import devplugin.SettingsTab;
import util.ui.UiUtilities;
import util.ui.customizableitems.SelectableItemList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;


public class TVBrowserMiniSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TVBrowserMiniSettingsTab.class);
  private Properties settings;
  private static JTextField path;
  private JRadioButton accepted;
  private JComboBox cbDays;
  private static Frame parentFrame;
  private JComboBox cbDevice;

  Checkbox cbShortDescription, cbDescription, cbOriginalTitel, cbGenre, cbProductionLocation, cbProductionTime, cbDirector, cbScript, cbActor, cbMusic, cbFSK, cbForminformation, cbShowView, cbEpisode, cbOriginalEpisode, cbModeration, cbWebside, cbRepetitionOn, cbRepetitionOf, cbVPS;


  private SelectableItemList mChannelList;


  public TVBrowserMiniSettingsTab(Properties settings, Frame aFrame) {
    this.settings = settings;
    parentFrame = aFrame;
  }


  public JPanel createSettingsPanel() {
    JPanel content = new JPanel(new BorderLayout());
    JPanel north = new JPanel(new FlowLayout());
    JPanel main = new JPanel(new BorderLayout());
    JPanel mainCenter = new JPanel(new BorderLayout());
    JPanel mainSouth = new JPanel(new FlowLayout());

    path = new JTextField(settings.getProperty("path"), 25);
    Button browse = new Button(mLocalizer.msg("buttonBrowse", "browse"));
    browse.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new util.ui.ExtensionFileFilter(".tvd", "TV-Browser Data File (*.tvd)"));
        int retVal = fileChooser.showSaveDialog(UiUtilities.getBestDialogParent(TVBrowserMiniSettingsTab.parentFrame.getParent()));
        if (retVal == JFileChooser.APPROVE_OPTION) {
          if (fileChooser.getSelectedFile().toString().toLowerCase().endsWith(".tvd"))
            TVBrowserMiniSettingsTab.path.setText(fileChooser.getSelectedFile().getPath());
          else
            TVBrowserMiniSettingsTab.path.setText(fileChooser.getSelectedFile().getPath() + ".tvd");
        }
      }
    });


    this.cbDays = new JComboBox();
    this.cbDays.addItem(mLocalizer.msg("alldays", "all days"));
    this.cbDays.addItem(mLocalizer.msg("oneday", "max. 1 day"));
    for (int i = 2; i < 29; i++) {
      this.cbDays.addItem(mLocalizer.msg("max", "max.") + " " + i + " " + mLocalizer.msg("day", "day"));
    }
    try {
      this.cbDays.setSelectedIndex(Integer.parseInt(settings.getProperty("exportDays")));
    }
    catch (Exception e) {
      this.cbDays.setSelectedIndex(0);
    }

    cbDevice = new JComboBox();
    cbDevice.addItem("PDA");
    cbDevice.addItem("Android");
    if (settings.getProperty("device", "0").equals("1"))
      cbDevice.setSelectedIndex(1);
    else
      cbDevice.setSelectedIndex(0);

    JTextArea disclaim = new JTextArea();
    disclaim.setText(mLocalizer.msg("nubs", "I accept to use the data exported with this plugin only with the applications authorized by the manufacturer of TV-Browser (www.tvbrowser.org). Using the data in other applications is prohibited. \nFor further questions look in the TV-Browser Wiki: http://enwiki.tvbrowser.org/"));
    disclaim.setEditable(false);
    JScrollPane scrollMain = new JScrollPane(disclaim);
    scrollMain.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollMain.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    disclaim.setLineWrap(true);
    disclaim.setWrapStyleWord(true);
    this.accepted = new JRadioButton(mLocalizer.msg("disclaim", "accept"));
    JRadioButton notAccepted = new JRadioButton(mLocalizer.msg("dontDisclaim", "don't accept"));
    if (settings.getProperty("accept").equals("1")) {
      this.accepted.setSelected(true);
    } else {
      notAccepted.setSelected(true);
    }

    ButtonGroup bg = new ButtonGroup();
    bg.add(this.accepted);
    bg.add(notAccepted);


    north.add(cbDevice);
    north.add(this.cbDays);
    north.add(path);
    north.add(browse);
    mainCenter.add(scrollMain, BorderLayout.CENTER);
    mainSouth.add(this.accepted);
    mainSouth.add(notAccepted);

    main.add(mainCenter, BorderLayout.CENTER);
    main.add(mainSouth, BorderLayout.SOUTH);
    content.add(north, BorderLayout.NORTH);
    content.add(main, BorderLayout.CENTER);


    JPanel channelPanel = new JPanel(new BorderLayout());
    channelPanel.setBorder(Borders.DIALOG_BORDER);

    mChannelList = new SelectableItemList(TVBrowserMini.getInstance().getSelectedChannels(), TVBrowserMini.getPluginManager().getSubscribedChannels());
    channelPanel.add(mChannelList, BorderLayout.CENTER);


    JPanel elementsPanel = new JPanel(new GridLayout(10, 2));

    this.cbShortDescription = new Checkbox(" " + mLocalizer.msg("shortDescription", "short description"), Boolean.parseBoolean(settings.getProperty("elementShortDescription")));
    this.cbDescription = new Checkbox(" " + mLocalizer.msg("description", "description"), Boolean.parseBoolean(settings.getProperty("elementDescription")));
    this.cbOriginalTitel = new Checkbox(" " + mLocalizer.msg("originaltitle", "original title"), Boolean.parseBoolean(settings.getProperty("elementOriginalTitel")));
    this.cbGenre = new Checkbox(" " + mLocalizer.msg("genre", "genre"), Boolean.parseBoolean(settings.getProperty("elementGenre")));
    this.cbProductionLocation = new Checkbox(" " + mLocalizer.msg("produced", "place of production"), Boolean.parseBoolean(settings.getProperty("elementProductionLocation")));
    this.cbProductionTime = new Checkbox(" " + mLocalizer.msg("producedIn", "produced in"), Boolean.parseBoolean(settings.getProperty("elementProductionTime")));
    this.cbDirector = new Checkbox(" " + mLocalizer.msg("director", "director"), Boolean.parseBoolean(settings.getProperty("elementDirector")));
    this.cbScript = new Checkbox(" " + mLocalizer.msg("script", "script"), Boolean.parseBoolean(settings.getProperty("elementScript")));
    this.cbActor = new Checkbox(" " + mLocalizer.msg("actor", "actor"), Boolean.parseBoolean(settings.getProperty("elementActor")));
    this.cbMusic = new Checkbox(" " + mLocalizer.msg("music", "music"), Boolean.parseBoolean(settings.getProperty("elementMusic")));
    this.cbFSK = new Checkbox(" " + mLocalizer.msg("fsk", "fsk"), Boolean.parseBoolean(settings.getProperty("elementFSK")));
    this.cbForminformation = new Checkbox(" " + mLocalizer.msg("forminformation", "form information"), Boolean.parseBoolean(settings.getProperty("elementForminformation")));
    this.cbShowView = new Checkbox(" " + mLocalizer.msg("showview", "Show-View"), Boolean.parseBoolean(settings.getProperty("elementShowView")));
    this.cbEpisode = new Checkbox(" " + mLocalizer.msg("episode", "episode"), Boolean.parseBoolean(settings.getProperty("elementEpisode")));
    this.cbOriginalEpisode = new Checkbox(" " + mLocalizer.msg("originalEpisode", "original episode"), Boolean.parseBoolean(settings.getProperty("elementOriginalEpisode")));
    this.cbModeration = new Checkbox(" " + mLocalizer.msg("moderation", "moderation"), Boolean.parseBoolean(settings.getProperty("elementModeration")));
    this.cbWebside = new Checkbox(" " + mLocalizer.msg("url", "webside"), Boolean.parseBoolean(settings.getProperty("elementWebside")));
    this.cbRepetitionOn = new Checkbox(" " + mLocalizer.msg("repetitionon", "repetition on"), Boolean.parseBoolean(settings.getProperty("elementRepetitionOn")));
    this.cbRepetitionOf = new Checkbox(" " + mLocalizer.msg("repetitionof", "repetition of"), Boolean.parseBoolean(settings.getProperty("elementRepetitionOf")));
    this.cbVPS = new Checkbox(" " + mLocalizer.msg("vps", "VPS"), Boolean.parseBoolean(settings.getProperty("elementVPS")));


    elementsPanel.add(cbEpisode, 0);
    elementsPanel.add(cbOriginalEpisode, 1);
    elementsPanel.add(cbGenre, 2);
    elementsPanel.add(cbShortDescription, 3);
    elementsPanel.add(cbDescription, 4);
    elementsPanel.add(cbOriginalTitel, 5);
    elementsPanel.add(cbProductionLocation, 6);
    elementsPanel.add(cbProductionTime, 7);
    elementsPanel.add(cbDirector, 8);
    elementsPanel.add(cbScript, 9);
    elementsPanel.add(cbActor, 10);
    elementsPanel.add(cbModeration, 11);
    elementsPanel.add(cbMusic, 12);
    elementsPanel.add(cbFSK, 13);
    elementsPanel.add(cbForminformation, 14);
    elementsPanel.add(cbRepetitionOn, 15);
    elementsPanel.add(cbRepetitionOf, 16);
    elementsPanel.add(cbShowView, 17);
    elementsPanel.add(cbVPS, 18);
    elementsPanel.add(cbWebside, 19);


    elementsPanel.setBorder(Borders.DIALOG_BORDER);


    JTabbedPane tabbedPane = new JTabbedPane();

    tabbedPane.add(mLocalizer.msg("tabMain", "Main"), content);
    tabbedPane.add(mLocalizer.msg("tabChannels", "Channel"), channelPanel);
    tabbedPane.add(mLocalizer.msg("tabElements", "Elements"), elementsPanel);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(tabbedPane, BorderLayout.CENTER);

    return mainPanel;
  }

  public Icon getIcon() {
    return TVBrowserMini.getInstance().createImageIcon("apps", "tuxbox", 16);
  }

  public String getTitle() {
    return TVBrowserMini.mLocalizer.msg("pluginName", "TV-Browser Mini Export");
  }

  public void saveSettings() {
    settings.setProperty("path", path.getText());
    if (this.accepted.isSelected()) {
      settings.setProperty("accept", "1");
    } else {
      settings.setProperty("accept", "0");
    }
    settings.setProperty("exportDays", String.valueOf(this.cbDays.getSelectedIndex()));
    settings.setProperty("device", String.valueOf(cbDevice.getSelectedIndex()));

    // Save elements
    settings.setProperty("elementShortDescription", String.valueOf(this.cbShortDescription.getState()));
    settings.setProperty("elementDescription", String.valueOf(this.cbDescription.getState()));
    settings.setProperty("elementOriginalTitel", String.valueOf(this.cbOriginalTitel.getState()));
    settings.setProperty("elementGenre", String.valueOf(this.cbGenre.getState()));
    settings.setProperty("elementProductionLocation", String.valueOf(this.cbProductionLocation.getState()));
    settings.setProperty("elementProductionTime", String.valueOf(this.cbProductionTime.getState()));
    settings.setProperty("elementDirector", String.valueOf(this.cbDirector.getState()));
    settings.setProperty("elementScript", String.valueOf(this.cbScript.getState()));
    settings.setProperty("elementActor", String.valueOf(this.cbActor.getState()));
    settings.setProperty("elementMusic", String.valueOf(this.cbMusic.getState()));
    settings.setProperty("elementFSK", String.valueOf(this.cbFSK.getState()));
    settings.setProperty("elementForminformation", String.valueOf(this.cbForminformation.getState()));
    settings.setProperty("elementShowView", String.valueOf(this.cbShowView.getState()));
    settings.setProperty("elementEpisode", String.valueOf(this.cbEpisode.getState()));
    settings.setProperty("elementOriginalEpisode", String.valueOf(this.cbOriginalEpisode.getState()));
    settings.setProperty("elementModeration", String.valueOf(this.cbModeration.getState()));
    settings.setProperty("elementWebside", String.valueOf(this.cbWebside.getState()));
    settings.setProperty("elementRepetitionOn", String.valueOf(this.cbRepetitionOn.getState()));
    settings.setProperty("elementRepetitionOf", String.valueOf(this.cbRepetitionOf.getState()));
    settings.setProperty("elementVPS", String.valueOf(this.cbVPS.getState()));

    // Save the channels
    Object[] o = mChannelList.getSelection();
    Channel[] selectedChannels = new Channel[o.length];

    for (int i = 0; i < o.length; i++) {
      selectedChannels[i] = (Channel) o[i];
    }

    TVBrowserMini.getInstance().setSelectedChannels(selectedChannels);
  }

}
