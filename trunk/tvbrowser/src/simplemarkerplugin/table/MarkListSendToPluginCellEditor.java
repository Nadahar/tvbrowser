package simplemarkerplugin.table;

import devplugin.ProgramReceiveTarget;
import simplemarkerplugin.MarkList;
import simplemarkerplugin.SimpleMarkerPlugin;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.PluginChooserDlg;
import util.ui.UiUtilities;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;

public class MarkListSendToPluginCellEditor extends AbstractCellEditor implements
    TableCellEditor {

  private ArrayList<ProgramReceiveTarget> mClientPluginTargets;

  @Override
  public boolean isCellEditable(EventObject evt) {
    return !(evt instanceof MouseEvent) || ((MouseEvent) evt).getClickCount() >= 2;
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
    final JButton press = new JButton(MarkerSendToPluginRenderer.getTextForReceiveTargets((MarkList)value));

    mClientPluginTargets = new ArrayList<ProgramReceiveTarget>(((MarkList)value).getPluginTargets());

    press.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Window parent = UiUtilities
            .getLastModalChildOf(MainFrame.getInstance());
        PluginChooserDlg chooser = null;
        chooser = new PluginChooserDlg(parent, mClientPluginTargets.toArray(new ProgramReceiveTarget[mClientPluginTargets.size()]), null,
            SimpleMarkerPlugin.getInstance());

        chooser.setLocationRelativeTo(parent);
        chooser.setVisible(true);

        if (chooser.getReceiveTargets() != null) {
          mClientPluginTargets = new ArrayList<ProgramReceiveTarget>(Arrays.asList(chooser.getReceiveTargets()));
        }
        table.getCellEditor().stopCellEditing();
      }
    });

    return press;
  }

  @Override
  public Object getCellEditorValue() {
    return mClientPluginTargets;
  }
}
