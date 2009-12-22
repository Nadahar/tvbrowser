package simplemarkerplugin.table;

import devplugin.Plugin;
import devplugin.Program;
import simplemarkerplugin.MarkList;
import simplemarkerplugin.SimpleMarkerPlugin;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

public class MarkerPriorityRenderer extends DefaultTableCellRenderer {
  private JPanel mPanel;
  private JLabel mLabel;

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(table, value,
        isSelected, hasFocus, row, column);

    if (mPanel == null) {
      mPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      mLabel = new JLabel();
      mPanel.add(mLabel);
    }

    Color color = c.getBackground();

    int priority = ((MarkList)value).getMarkPriority();

    switch(priority) {
      case Program.MIN_MARK_PRIORITY: mLabel.setText(SimpleMarkerPlugin.mLocalizer.msg("settings.min","Minimum"));break;
      case Program.LOWER_MEDIUM_MARK_PRIORITY: mLabel.setText(SimpleMarkerPlugin.mLocalizer.msg("settings.lowerMedium","Lower medium"));break;
      case Program.MEDIUM_MARK_PRIORITY: mLabel.setText(SimpleMarkerPlugin.mLocalizer.msg("settings.medium","Medium"));break;
      case Program.HIGHER_MEDIUM_MARK_PRIORITY: mLabel.setText(SimpleMarkerPlugin.mLocalizer.msg("settings.higherMedium","Higher Medium"));break;
      case Program.MAX_MARK_PRIORITY: mLabel.setText(SimpleMarkerPlugin.mLocalizer.msg("settings.max","Maximum"));break;

      default: mLabel.setText(SimpleMarkerPlugin.mLocalizer.msg("settings.noPriority","None"));break;
    }
    
    Color testColor = Plugin.getPluginManager().getTvBrowserSettings().getColorForMarkingPriority(priority);

    if(color != null && !isSelected) {
      color = testColor;
    }

    mPanel.setBackground(color);
    mPanel.setOpaque(true);
    mLabel.setForeground(c.getForeground());

    return mPanel;
  }
}
