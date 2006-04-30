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
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mär 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */
package captureplugin.drivers.elgatodriver.configdialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import util.ui.ChannelTableCellRenderer;
import util.ui.Localizer;
import captureplugin.drivers.elgatodriver.ElgatoConfig;
import captureplugin.drivers.elgatodriver.ElgatoConnection;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

/**
 * Config Dialog
 *  
 * @author bodum
 */
public class ElgatoConfigDialog extends JDialog {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ElgatoConfigDialog.class);

  /** Connection */
  private ElgatoConnection mConnection;
  /** Configuration */
  private ElgatoConfig mConfig;
  /** Which Button was pressed */
  private int mReturn = JOptionPane.CANCEL_OPTION;
  /** Table with mapping */
  private JTable mTable;

  /**
   * Create Dialog
   * @param frame Parent
   * @param connection Connection
   * @param config Configuration
   */
  public ElgatoConfigDialog(JFrame frame, ElgatoConnection connection, ElgatoConfig config) {
    super(frame, true);
    mConnection = connection;
    mConfig = (ElgatoConfig) config.clone();
    createGui();
  }

  /**
   * Create Dialog
   * @param dialog Parent
   * @param connection Connection
   * @param config Configuration
   */
  public ElgatoConfigDialog(JDialog dialog, ElgatoConnection connection, ElgatoConfig config) {
    super(dialog, true);
    mConnection = connection;
    mConfig = (ElgatoConfig) config.clone();
    createGui();
  }

  /**
   * Create the Gui
   */
  private void createGui() {
    JPanel panel = (JPanel) getContentPane();
    
    setTitle(mLocalizer.msg("title","Elgato EyeTV Settings"));
    
    panel.setLayout(new FormLayout("fill:pref:grow, 3dlu, pref, 3dlu, pref", "pref, 5dlu, fill:min:grow, 3dlu, pref, 3dlu, pref"));
    panel.setBorder(Borders.DIALOG_BORDER);
    
    CellConstraints cc = new CellConstraints();
    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("channelAssignment","Channel assignment")), cc.xyw(1,1, 3));
    
    mTable = new JTable(new ConfigTableModel(mConfig));
    mTable.getTableHeader().setReorderingAllowed(false);
    mTable.getColumnModel().getColumn(0).setCellRenderer(new ChannelTableCellRenderer());
    mTable.getColumnModel().getColumn(1).setCellRenderer(new ElgatoChannelRenderer());
    mTable.getColumnModel().getColumn(1).setCellEditor(new ElgatoChannelEditor(mConfig));
    panel.add(new JScrollPane(mTable), cc.xyw(1,3,5));
    
    JButton fetch = new JButton(mLocalizer.msg("fetchChannels","Fetch Channellist"));
    
    fetch.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            mConfig.setElgatoChannels(mConnection.getAvailableChannels());
            mTable.updateUI();
          }
        });
      }
    });
    
    panel.add(fetch, cc.xy(3,5));
    
    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();
    
    JButton ok = new JButton(mLocalizer.msg("ok", "OK"));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        if (mTable.isEditing()) {
            TableCellEditor editor = mTable.getCellEditor();
            if (editor != null)
                editor.stopCellEditing();
        }
        mReturn = JOptionPane.OK_OPTION;
        setVisible(false);
      };
    });

    JButton cancel = new JButton(mLocalizer.msg("cancel","Cancel"));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        setVisible(false);
      };
    });
    
    builder.addGriddedButtons(new JButton[] {ok, cancel});
    
    panel.add(builder.getPanel(), cc.xyw(1,7, 3));
    
    setSize(Sizes.dialogUnitXAsPixel(250, this), Sizes.dialogUnitXAsPixel(200, this));
  }

  /**
   * Was the OK-Button pressed ?
   * @return true if OK was pressed
   */
  public boolean wasOkPressed() {
    return mReturn == JOptionPane.OK_OPTION;
  }

  /**
   * @return Modified configuration
   */
  public ElgatoConfig getConfig() {
    return mConfig;
  }
  
}