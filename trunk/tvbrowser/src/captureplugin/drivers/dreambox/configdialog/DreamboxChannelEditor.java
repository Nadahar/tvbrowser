/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2007-01-03 09:06:40 +0100 (Mi, 03 Jan 2007) $
 *   $Author: bananeweizen $
 * $Revision: 2979 $
 */
package captureplugin.drivers.dreambox.configdialog;

import captureplugin.drivers.dreambox.DreamboxConfig;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * The Celleditor for the DreamboxChannel
 */
public class DreamboxChannelEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
    private JComboBox mComboBox;
    private DreamboxConfig mConfig;

    public DreamboxChannelEditor(DreamboxConfig config) {
        mConfig = config;
    }

    public Object getCellEditorValue() {
        return mComboBox.getSelectedItem();
    }

    @Override
    public boolean stopCellEditing() {
        if (mComboBox.isEditable()) {
            // Commit edited value.
            mComboBox.actionPerformed(new ActionEvent(this, 0, ""));
        }
        return super.stopCellEditing();
    }

    public void actionPerformed(ActionEvent e) {
        stopCellEditing();
    }

    @Override
    public boolean isCellEditable(EventObject evt) {
        return !(evt instanceof MouseEvent) || ((MouseEvent) evt).getClickCount() >= 2;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        mComboBox = new JComboBox(mConfig.getDreamboxChannels());
        mComboBox.setSelectedItem(value);
        mComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        return mComboBox;
    }
}
