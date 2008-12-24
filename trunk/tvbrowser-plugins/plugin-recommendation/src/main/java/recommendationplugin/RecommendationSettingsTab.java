package recommendationplugin;

import devplugin.SettingsTab;

import javax.swing.JPanel;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.JScrollPane;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.RowSpec;

public class RecommendationSettingsTab implements SettingsTab {

  private RecommendationPlugin mPlugin;

  public RecommendationSettingsTab(final RecommendationPlugin plugin) {
    mPlugin = plugin;
  }

  public JPanel createSettingsPanel() {
    final FormLayout layout = new FormLayout("3dlu,fill:min:grow,3dlu");
    final CellConstraints cc = new CellConstraints();

    final JPanel panel = new JPanel(layout);

    int line = 1;

    layout.appendRow(RowSpec.decode("fill:min:grow"));
    layout.appendRow(RowSpec.decode("3dlu"));

    final RecommendationTableModel model = new RecommendationTableModel(mPlugin.getEnabledInput());
    final JTable table = new JTable(model);

    panel.add(new JScrollPane(table), cc.xy(2,line));
    

    return panel;
  }

  public void saveSettings() {
  }

  public Icon getIcon() {
    return mPlugin.getPluginIcon();
  }

  public String getTitle() {
    return mPlugin.getInfo().getName();
  }
}
