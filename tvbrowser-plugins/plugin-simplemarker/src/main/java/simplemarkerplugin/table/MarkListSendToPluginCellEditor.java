/*
 * SimpleMarkerPlugin by René Mach
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package simplemarkerplugin.table;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import simplemarkerplugin.MarkList;
import simplemarkerplugin.SimpleMarkerPlugin;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.PluginChooserDlg;
import util.ui.UiUtilities;
import devplugin.ProgramReceiveTarget;

/**
 * The cell editor for the send to plugins column
 *
 */
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
