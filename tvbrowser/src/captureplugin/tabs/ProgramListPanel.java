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
package captureplugin.tabs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import util.ui.Localizer;
import captureplugin.CapturePluginData;
import captureplugin.drivers.DeviceIf;
import captureplugin.utils.ProgramTimeComparator;
import devplugin.Program;

/**
 * Panel with List of Recordings
 * 
 * @author bodum
 */
public class ProgramListPanel extends JPanel {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramListPanel.class);
  
    /** Config **/
    private CapturePluginData mData;

    /** JList for Programs */
    private JTable mProgramTable;
    /** List of Programs */
    private DeviceTableModel mProgramTableModel;
    
    private JFrame mParent;
    
    /**
     * Creates the Panel
     * @param parent Parent-Frame
     * @param data Configuration
     */
    public ProgramListPanel(JFrame parent, CapturePluginData data) {
        mParent =parent;
        mData = data;
        mProgramTableModel = new DeviceTableModel();
        createListData();
        createPanel();
    }

    /**
     * Creates the Data for the List
     */
    private void createListData() {
        mProgramTableModel.clearTable();

        Iterator it = mData.getDevices().iterator();
        
        while (it.hasNext()) {
            DeviceIf dev = (DeviceIf) it.next();
            Program[] prgList = dev.getProgramList();

            Arrays.sort(prgList,new ProgramTimeComparator());
            for (int v = 0; v < prgList.length; v++) {
                mProgramTableModel.addProgram(dev, prgList[v]);
            }
        }
        
    }
    
    /**
     * Creates the GUI
     */
    private void createPanel() {
        setLayout(new BorderLayout());

        mProgramTable = new JTable(mProgramTableModel);
        
        mProgramTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mProgramTable.getColumnModel().getColumn(0).setCellRenderer(new DeviceTableCellRenderer());
        mProgramTable.getColumnModel().getColumn(1).setCellRenderer(new ProgramTableCellRenderer());

        mProgramTable.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                int column = mProgramTable.columnAtPoint(e.getPoint());
                if (column != 1) {
                    return;
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = mProgramTable.rowAtPoint(e.getPoint());

                    mProgramTable.changeSelection(row, 0, false, false);
                    
                    Program p = (Program) mProgramTableModel.getValueAt(row, 1);
                    
                    JPopupMenu menu = devplugin.Plugin.getPluginManager().createPluginContextMenu(p);
                    menu.show(mProgramTable, e.getX() - 15, e.getY() - 15);
                } else if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
                    int row = mProgramTable.rowAtPoint(e.getPoint());
                    mProgramTable.changeSelection(row, 0, false, false);
                    Program p = (Program) mProgramTableModel.getValueAt(row, 1);

                    devplugin.Plugin.getPluginManager().handleProgramDoubleClick(p);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(mProgramTable);

        add(scroll, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton delete = new JButton(new ImageIcon(ProgramListPanel.class.getResource("Delete16.gif")));
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deletePressed();
            }
            
        });
        
        btnPanel.add(delete);

        add(btnPanel, BorderLayout.SOUTH);
    }


    /**
     * Delete was pressed
     */
    private void deletePressed() {
       int row = mProgramTable.getSelectedRow();
       
       if ((row > mProgramTableModel.getRowCount()) || (row < 0)) {
           return;
       }
       
       DeviceIf dev = (DeviceIf) mProgramTableModel.getValueAt(row, 0);
       Program prg = (Program) mProgramTableModel.getValueAt(row, 1);

       int ret = JOptionPane.showConfirmDialog(mParent, 
               mLocalizer.msg("ReallyDelete","Really delete recording?"),
               mLocalizer.msg("Delete", "Delete?"),
               JOptionPane.YES_NO_OPTION);
       
       if (ret == JOptionPane.YES_OPTION) {
           dev.remove(mParent, prg);
           
           mProgramTableModel.removeColumn(row);
           
           createListData();
       }
       
    }
    
}