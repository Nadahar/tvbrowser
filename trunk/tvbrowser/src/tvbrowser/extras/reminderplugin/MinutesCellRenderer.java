package tvbrowser.extras.reminderplugin;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.icontheme.IconLoader;

public class MinutesCellRenderer extends DefaultTableCellRenderer {
  private JPanel mPanel;
  private JLabel mTextLabel, mIconLabel;
  
  public MinutesCellRenderer() {
    mPanel = new JPanel(new FormLayout("pref,pref:grow,pref,2dlu","pref:grow"));
    CellConstraints cc = new CellConstraints();
    mTextLabel = new JLabel();
    mIconLabel = new JLabel(IconLoader.getInstance().getIconFromTheme("actions","document-edit",16));
    
    mPanel.add(mTextLabel, cc.xy(1,1));
    mPanel.add(mIconLabel, cc.xy(3,1));
  }
  
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component def = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    
    if (value instanceof Integer) {
      Integer minutes = (Integer) value;
      
      mTextLabel.setText(ReminderFrame.getStringForMinutes(minutes.intValue()));
      
      mTextLabel.setOpaque(def.isOpaque());
      mTextLabel.setForeground(def.getForeground());
      mTextLabel.setBackground(def.getBackground());
      
      mPanel.setOpaque(def.isOpaque());
      mPanel.setBackground(def.getBackground());
      
      return mPanel;
    }
    
    return def;
  }
  
}
