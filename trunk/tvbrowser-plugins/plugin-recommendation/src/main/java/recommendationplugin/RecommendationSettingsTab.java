package recommendationplugin;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsTab;

public class RecommendationSettingsTab implements SettingsTab {

  private RecommendationPlugin mPlugin;
  private JTable mTable;
  private RecommendationSettings mSettings;

  public RecommendationSettingsTab(final RecommendationPlugin plugin, final RecommendationSettings settings) {
    mPlugin = plugin;
    mSettings = settings;
  }

  public JPanel createSettingsPanel() {
    final FormLayout layout = new FormLayout("3dlu,fill:min:grow,3dlu");
    final CellConstraints cc = new CellConstraints();

    final JPanel panel = new JPanel(layout);

    int line = 1;

    layout.appendRow(RowSpec.decode("fill:min:grow"));
    layout.appendRow(RowSpec.decode("3dlu"));

    final RecommendationTableModel model = new RecommendationTableModel(mPlugin.getAllWeightings());
    mTable = new JTable(model);
    mTable.getColumnModel().getColumn(1).setCellRenderer(new TableSliderRenderer());
    mTable.getColumnModel().getColumn(1).setCellEditor(new TableSliderEditor());
    mTable.getTableHeader().setReorderingAllowed(false);
    panel.add(new JScrollPane(mTable), cc.xy(2, line));

    return panel;
  }

  public void saveSettings() {
    for (RecommendationWeighting weighting : ((RecommendationTableModel)mTable.getModel()).getWeightings()) {
      mSettings.setWeighting(weighting);
    }
    mPlugin.initializeWeightings();
    mPlugin.updateRecommendations();
  }

  public Icon getIcon() {
    return mPlugin.getPluginIcon();
  }

  public String getTitle() {
    return mPlugin.getInfo().getName();
  }
}
