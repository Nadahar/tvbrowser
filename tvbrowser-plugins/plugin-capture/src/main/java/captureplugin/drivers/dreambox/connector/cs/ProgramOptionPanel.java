package captureplugin.drivers.dreambox.connector.cs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.Localizer;

/**
 * @author fishhead
 * 
 */
public class ProgramOptionPanel extends JPanel implements ActionListener {

  private static final String CMD_ONLY_CREATE_ZAP_TIMER = "CMD_ONLY_ZAP";
  private static final String CMD_USE_HD_SERVICE = "CMD_HD";

  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(ProgramOptionPanel.class);

  private final JCheckBox cbMon;
  private final JCheckBox cbTue;
  private final JCheckBox cbWed;
  private final JCheckBox cbThu;
  private final JCheckBox cbFre;
  private final JCheckBox cbSat;
  private final JCheckBox cbSun;
  private final JCheckBox cbZapBeforeEvent;
  private final JComboBox cmbAfterEvent;
  private final JLabel lbLocation;
  private final JComboBox cmbLocation;
  private final JLabel lbTag;
  private final JComboBox cmbTag;
  private final JCheckBox cbUseHdService;
  private final JCheckBox cbOnlyCreateZapTimer;

  /**
   * Aufnahme-Panel erzeugen
   * 
   * @param locationThread
   * @param movieThread
   */
  public ProgramOptionPanel(E2LocationHelper locationThread,
      E2MovieHelper movieThread) {

    // Location / Category
    JPanel panelGB = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;

    // AfterEvent
    cmbAfterEvent = new JComboBox(new String[] {
        mLocalizer.msg("afterEventNothing", "Nothing"),
        mLocalizer.msg("afterEventStandby", "Standby"),
        mLocalizer.msg("afterEventDeepstandby", "Deepstandby"),
        mLocalizer.msg("afterEventAuto", "Auto") });
    cmbAfterEvent.setSelectedIndex(3);

    c.insets = new Insets(4, 6, 0, 10);
    c.gridx = 0;
    c.gridy = 0;
    panelGB.add(cmbAfterEvent, c);

    // Repeated
    cbMon = new JCheckBox(mLocalizer.msg("Mon", "Mon"));
    cbTue = new JCheckBox(mLocalizer.msg("Tue", "Tue"));
    cbWed = new JCheckBox(mLocalizer.msg("Wed", "Wed"));
    cbThu = new JCheckBox(mLocalizer.msg("Thu", "Thu"));
    cbFre = new JCheckBox(mLocalizer.msg("Fre", "Fre"));
    cbSat = new JCheckBox(mLocalizer.msg("Sat", "Sat"));
    cbSun = new JCheckBox(mLocalizer.msg("Sun", "Sun"));

    c.insets = new Insets(0, 2, 0, 2);
    c.gridx = 1;
    panelGB.add(cbMon, c);
    c.gridx++;
    panelGB.add(cbTue, c);
    c.gridx++;
    panelGB.add(cbWed, c);
    c.gridx++;
    panelGB.add(cbThu, c);
    c.gridx++;
    panelGB.add(cbFre, c);
    c.gridx = 1;
    c.gridy++;
    panelGB.add(cbSat, c);
    c.gridx++;
    panelGB.add(cbSun, c);

    // BeforeEvent
    cbZapBeforeEvent = new JCheckBox(mLocalizer.msg("beforeEvent",
        "Vor der Aufnahme auf den Kanal wechseln"));
    c.insets = new Insets(8, 2, 0, 2);
    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 6;
    panelGB.add(cbZapBeforeEvent, c);
    c.gridwidth = 1;

    // Locations
    List<String> locations = locationThread.getLocations();
    if (locations != null) {
      cmbLocation = new JComboBox(locations.toArray());
    } else {
      cmbLocation = new JComboBox(new String[] { "" });
    }

    String defaultLocation = locationThread.getDefaultLocation();
    if (!defaultLocation.equals("")) {
        int defaultIndex = locations.indexOf(defaultLocation);
        cmbLocation.setSelectedIndex(defaultIndex);
    }
    
    c.insets = new Insets(8, 6, 4, 2);
    c.gridx = 0;
    c.gridy++;
    lbLocation = new JLabel(mLocalizer.msg("location", "Aufnahmeort: "));
    panelGB.add(lbLocation, c);
    c.gridx++;
    c.gridwidth = 4;
    panelGB.add(cmbLocation, c);
    c.gridwidth = 1;

    // Tags
    Set<String> tags = movieThread.getTags();
    if (tags == null) {
      tags = new TreeSet<String>();
      tags.add("");
    }
    Object[] oaTags = tags.toArray();
    cmbTag = new JComboBox(oaTags);
    cmbTag.setEditable(true);
    c.insets = new Insets(2, 6, 4, 2);
    c.gridx = 0;
    c.gridy++;
    lbTag = new JLabel(mLocalizer.msg("tags", "Kategorie: "));
    panelGB.add(lbTag, c);
    c.gridx++;
    c.gridwidth = 4;
    panelGB.add(cmbTag, c);
    c.gridwidth = 1;

    // HD-Sender verwenden
    cbUseHdService = new JCheckBox(mLocalizer.msg("useHdService",
        "HD Sender verwenden"));
    cbUseHdService.addActionListener(this);
    cbUseHdService.setActionCommand(CMD_USE_HD_SERVICE);
    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 6;
    panelGB.add(cbUseHdService, c);

    if (!E2ServiceHelper.exits()) {
      hideUseHdService();
    }

    // nur Umschalt-Timer erzeugen
    cbOnlyCreateZapTimer = new JCheckBox(mLocalizer.msg("onlyZapTimer",
        "Nur Umschalt-Timer erzeugen"));
    cbOnlyCreateZapTimer.addActionListener(this);
    cbOnlyCreateZapTimer.setActionCommand(CMD_ONLY_CREATE_ZAP_TIMER);
    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 6;
    panelGB.add(cbOnlyCreateZapTimer, c);

    this.setLayout(new BorderLayout());
    this.add(panelGB, BorderLayout.WEST);
  }

  /**
   * CheckBox ZapBeforeEvent setzen
   * 
   * @param selected
   */
  public void setZapBeforeEvent(boolean selected) {
    cbZapBeforeEvent.setSelected(selected);
  }

  /**
   * @return repeated
   */
  public int getRepeated() {
    int repeated = 0;
    if (cbMon.isSelected()) {
      repeated += 1;
    }
    if (cbTue.isSelected()) {
      repeated += 2;
    }
    if (cbWed.isSelected()) {
      repeated += 4;
    }
    if (cbThu.isSelected()) {
      repeated += 8;
    }
    if (cbFre.isSelected()) {
      repeated += 16;
    }
    if (cbSat.isSelected()) {
      repeated += 32;
    }
    if (cbSun.isSelected()) {
      repeated += 64;
    }
    return repeated;
  }

  /**
   * @param repeated
   */
  public void setRepeated(int repeated) {
    if ((repeated & 1) == 1) {
      cbMon.setSelected(true);
    }
    if ((repeated & 2) == 2) {
      cbTue.setSelected(true);
    }
    if ((repeated & 4) == 4) {
      cbWed.setSelected(true);
    }
    if ((repeated & 8) == 8) {
      cbThu.setSelected(true);
    }
    if ((repeated & 16) == 16) {
      cbFre.setSelected(true);
    }
    if ((repeated & 32) == 32) {
      cbSat.setSelected(true);
    }
    if ((repeated & 64) == 64) {
      cbSun.setSelected(true);
    }
  }

  /**
   * @return beforeEvent
   */
  public boolean isZapBeforeEvent() {
    return cbZapBeforeEvent.isSelected();
  }

  /**
   * @return afterEvent
   */
  public int getSelectedAfterEvent() {
    return cmbAfterEvent.getSelectedIndex();
  }

  /**
   * @param afterEvent
   */
  public void setSelectedAfterEvent(int afterEvent) {
    cmbAfterEvent.setSelectedIndex(afterEvent);
  }

  /**
   * @return location
   */
  public String getSelectedLocation() {
    return (String) cmbLocation.getSelectedItem();
  }

  /**
   * @param location
   */
  public void setSelectedLocation(String location) {
    cmbLocation.setSelectedItem(location);
  }

  /**
   * @return tag
   */
  public String getSelectedTag() {
    return (String) cmbTag.getSelectedItem();
  }

  /**
   * @param tag
   */
  public void setSelectedTag(String tag) {
    cmbTag.setSelectedItem(tag);
  }

  /**
   * @return OnlyCreateZapTimer
   */
  public boolean isOnlyCreateZapTimer() {
    return cbOnlyCreateZapTimer.isSelected();
  }

  /**
   * @return useHdService
   */
  public boolean isUseHdService() {
    return cbUseHdService.isSelected();
  }

  /**
   * CheckBox Use HD Service setzen
   * 
   * @param useHdService
   */
  public void setUseHdService(boolean useHdService) {
    cbUseHdService.setSelected(useHdService);
  }

  /**
   * Enable/Disable components
   */
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(CMD_ONLY_CREATE_ZAP_TIMER)) {
      boolean notSelected = !cbOnlyCreateZapTimer.isSelected();
      cmbAfterEvent.setEnabled(notSelected);
      cbMon.setEnabled(notSelected);
      cbTue.setEnabled(notSelected);
      cbWed.setEnabled(notSelected);
      cbThu.setEnabled(notSelected);
      cbFre.setEnabled(notSelected);
      cbSat.setEnabled(notSelected);
      cbSun.setEnabled(notSelected);
      cbZapBeforeEvent.setEnabled(notSelected);
      cbUseHdService.setEnabled(notSelected);
      lbLocation.setEnabled(notSelected);
      cmbLocation.setEnabled(notSelected);
      lbTag.setEnabled(notSelected);
      cmbTag.setEnabled(notSelected);
    } else if (e.getActionCommand().equals(CMD_USE_HD_SERVICE)) {
      // nothing
    }
  }

  /**
   * hide OnlyCreateZapTimer
   */
  public void hideOnlyCreateZapTimer() {
    cbOnlyCreateZapTimer.setVisible(false);
  }

  /**
   * hide UseHdService
   */
  public void hideUseHdService() {
    cbUseHdService.setVisible(false);
  }

}
