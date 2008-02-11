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

import captureplugin.CapturePlugin;
import captureplugin.CapturePluginData;
import captureplugin.drivers.DeviceIf;
import captureplugin.utils.ProgramTimeComparator;
import devplugin.Plugin;
import devplugin.Program;
import util.settings.PluginPictureSettings;
import util.ui.Localizer;
import util.ui.ProgramTableCellRenderer;
import util.ui.UiUtilities;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

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

    private Window mParent;

    /**
     * Creates the Panel
     * @param parent Parent-Frame
     * @param data Configuration
     */
    public ProgramListPanel(Window parent, CapturePluginData data) {
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

        for (DeviceIf dev : mData.getDevices()) {
            Program[] prgList = dev.getProgramList();

            if (prgList != null) {
                Arrays.sort(prgList, new ProgramTimeComparator());
                for (Program program : prgList) {
                    mProgramTableModel.addProgram(dev, program);
                }
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
        mProgramTable.getColumnModel().getColumn(1).setCellRenderer(new ProgramTableCellRenderer(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE)));

        mProgramTable.addMouseListener(new MouseAdapter() {
            private Thread mLeftSingleClickThread;
          
            public void mousePressed(MouseEvent evt) {
              if (evt.isPopupTrigger()) {
                showPopup(evt);
              }
            }

            public void mouseReleased(MouseEvent evt) {
              if (evt.isPopupTrigger()) {
                showPopup(evt);
              }
            }

            public void mouseClicked(final MouseEvent e) {
                int column = mProgramTable.columnAtPoint(e.getPoint());
                if (column != 1) {
                    return;
                }
                if(e.getModifiersEx() == 0) {
                  if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 1)) {
                    mLeftSingleClickThread = new Thread() {
                      public void run() {
                        try {
                          sleep(Plugin.SINGLE_CLICK_WAITING_TIME);
  
                          int row = mProgramTable.rowAtPoint(e.getPoint());
                          mProgramTable.changeSelection(row, 0, false, false);
                          Program p = (Program) mProgramTableModel.getValueAt(row, 1);
  
                          devplugin.Plugin.getPluginManager().handleProgramSingleClick(p, CapturePlugin.getInstance());
                        } catch (InterruptedException e) { // ignore
                        }
                      }
                    };
                    
                    mLeftSingleClickThread.setPriority(Thread.MIN_PRIORITY);
                    mLeftSingleClickThread.start();
                  }
                  if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
                      if(mLeftSingleClickThread != null && mLeftSingleClickThread.isAlive()) {
                        mLeftSingleClickThread.interrupt();
                      }
                      
                      int row = mProgramTable.rowAtPoint(e.getPoint());
                      mProgramTable.changeSelection(row, 0, false, false);
                      Program p = (Program) mProgramTableModel.getValueAt(row, 1);
  
                      devplugin.Plugin.getPluginManager().handleProgramDoubleClick(p, CapturePlugin.getInstance());
                  }
                  if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1)) {
                      int row = mProgramTable.rowAtPoint(e.getPoint());
                      mProgramTable.changeSelection(row, 0, false, false);
                      Program p = (Program) mProgramTableModel.getValueAt(row, 1);
  
                      devplugin.Plugin.getPluginManager().handleProgramMiddleClick(p, CapturePlugin.getInstance());
                  }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(mProgramTable);

        add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton delete = new JButton(CapturePlugin.getInstance().createImageIcon("actions", "edit-delete", 16));
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deletePressed();
            }

        });

        btnPanel.add(delete);

        add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * Shows the Popup
     * @param e Mouse-Event
     */
    private void showPopup(MouseEvent e) {
      int row = mProgramTable.rowAtPoint(e.getPoint());

      mProgramTable.changeSelection(row, 0, false, false);

      Program p = (Program) mProgramTableModel.getValueAt(row, 1);

      JPopupMenu menu = devplugin.Plugin.getPluginManager().createPluginContextMenu(p, CapturePlugin.getInstance());
      menu.show(mProgramTable, e.getX() - 15, e.getY() - 15);
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

       int ret = JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(mParent),
               mLocalizer.msg("ReallyDelete","Really delete recording?"),
               Localizer.getLocalization(Localizer.I18N_DELETE)+"?",
               JOptionPane.YES_NO_OPTION);

       if (ret == JOptionPane.YES_OPTION) {
           dev.remove(UiUtilities.getLastModalChildOf(mParent), prg);

           mProgramTableModel.removeColumn(row);

           createListData();
       }

    }

}