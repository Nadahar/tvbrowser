package speechplugin;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.ui.Localizer;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

public class SpeechPluginSettingsTab implements SettingsTab {

  /** Localizer */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(SpeechPluginSettingsTab.class);

  private JList mVoicesList;

  private SpeechPluginSettings mSettings;

  public SpeechPluginSettingsTab(SpeechPluginSettings settings) {
    mSettings = settings;
  }

  public JPanel createSettingsPanel() {
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,fill:default:grow", "5dlu,pref,2dlu,fill:default:grow"));
    CellConstraints cc = new CellConstraints();

    List<String> voices = SpeechPlugin.getAvailableVoices("general");
    mVoicesList = new JList(voices.toArray());
    String voice = mSettings.getVoice();
    if (voice != null) {
      int index = voices.indexOf(voice);
      if (index >= 0) {
        mVoicesList.setSelectedIndex(index);
      }
    }
    mVoicesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mVoicesList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        listSelectionChanged();
      }
    });
    GridBagConstraints c = new GridBagConstraints();

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.insets = new Insets(0, 0, 5, 5);

    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JScrollPane(mVoicesList), c);
    pb.addSeparator(mLocalizer.msg("voices", "Voices"), cc.xyw(1, 2, 2));
    pb.add(panel, cc.xy(2, 4));

    return pb.getPanel();
  }

  private void listSelectionChanged() {
    mSettings.setVoice(mVoicesList.getSelectedValue().toString());
  }

  public Icon getIcon() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getTitle() {
    // ignored since 2.7
    return null;
  }

  public void saveSettings() {
    // nothing to do, settings life cycle is managed by plugin
  }

}
