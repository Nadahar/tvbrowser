package simplemarkerplugin.table;

import simplemarkerplugin.MarkList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

public class MarkerIDRenderer extends DefaultTableCellRenderer {
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

    int count = ((MarkList)value).size();

    if(count > 0) {
      mLabel.setText(value.toString() + " [" + count + "]");
    } else {
      mLabel.setText(value.toString());
    }

    mPanel.setBackground(color);
    mPanel.setOpaque(true);
    mLabel.setForeground(c.getForeground());

    return mPanel;
  }
}
