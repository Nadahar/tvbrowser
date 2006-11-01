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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package captureplugin.drivers.defaultdriver.configpanels;

import captureplugin.drivers.defaultdriver.DeviceConfig;
import util.ui.ChannelTableCellRenderer;
import util.ui.Localizer;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.TableCellEditor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


/**
 * This Panel makes it possible to assign external names to channels
 */
public class ChannelPanel extends JPanel {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ChannelPanel.class);

    private JTable mChannelTable = new JTable();

    /** Settings */
    private DeviceConfig mData;

    /**
     * Creates the Panel
     * @param data Data to use
     */
    public ChannelPanel(DeviceConfig data) {
        mData = data;
        createPanel();
    }
    
    /**
     * creates a JPanel for managing the channels
     */
    private void createPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("ChannelNames", "Channel Names")));

        ChannelTableModel tableModel = new ChannelTableModel(mData);
        mChannelTable.setModel(tableModel);
        mChannelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mChannelTable.getTableHeader().setReorderingAllowed(false);
        mChannelTable.getColumnModel().getColumn(0).setCellRenderer(new ChannelTableCellRenderer());
        
        // Dispache the KeyEvent to the RootPane for Closing the Dialog.
        // Needed for Java 1.4.
        mChannelTable.addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
              mChannelTable.getRootPane().dispatchEvent(e);
          }
        });
        
        JScrollPane sp = new JScrollPane(mChannelTable);

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 0.8;

        addAncestorListener(new AncestorListener() {

          public void ancestorAdded(AncestorEvent event) {
            // TODO Auto-generated method stub
          }

          public void ancestorMoved(AncestorEvent event) {
            // TODO Auto-generated method stub
          }

          public void ancestorRemoved(AncestorEvent event) {
            if (mChannelTable.isEditing()) {
              TableCellEditor editor = mChannelTable.getCellEditor();
              if (editor != null)
                editor.stopCellEditing();
            }
          }
          
        });
        
        add(sp, c);
    }

   
}