package simplemarkerplugin.table;

import simplemarkerplugin.MarkList;
import simplemarkerplugin.SimpleMarkerPlugin;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

public class MarkerSendToPluginRenderer extends DefaultTableCellRenderer {
  private JPanel mPanel;
  private JLabel mLabel;

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(table, value,
        isSelected, hasFocus, row, column);

    if (mPanel == null) {
      mPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      mLabel = new JLabel();
      mPanel.add(mLabel);
    }

    Color color = c.getBackground();

    mLabel.setText(getTextForReceiveTargets((MarkList)value));

    mPanel.setBackground(color);
    mPanel.setOpaque(true);
    mLabel.setForeground(c.getForeground());

    return mPanel;
  }

  public static String getTextForReceiveTargets(MarkList list) {
    if (list.getPluginTargets().size() == 0) {
      return SimpleMarkerPlugin.getLocalizer().msg("settings.sendToNone", "None");
    } else if(list.getPluginTargets().size() == 1) {
      return list.getPluginTargets().iterator().next().getTargetName();
    } else {
      return SimpleMarkerPlugin.getLocalizer().msg("settings.sendToPlugins", "{0} Plugins", list.getPluginTargets().size());      
    }
  }
}
