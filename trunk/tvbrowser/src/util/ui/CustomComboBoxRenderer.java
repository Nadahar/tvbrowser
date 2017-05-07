package util.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public abstract class CustomComboBoxRenderer extends DefaultListCellRenderer {
  private ListCellRenderer<Object> mBackendRenderer;
  
  public CustomComboBoxRenderer(ListCellRenderer<Object> backendRenderer) {
    mBackendRenderer = backendRenderer;
  }
  
  public ListCellRenderer<Object> getBackendRenderer() {
    return mBackendRenderer;
  }
  
  abstract public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus);
  
  protected Component getSuperListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
  }
}
