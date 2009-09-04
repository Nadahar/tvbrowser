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
package captureplugin.drivers.simpledevice;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import util.ui.ChannelTableCellRenderer;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import captureplugin.utils.ConfigTableModel;
import captureplugin.utils.ExternalChannelTableCellEditor;
import captureplugin.utils.ExternalChannelTableCellRenderer;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
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
public class SimpleConfigDialog extends JDialog implements WindowClosingIf {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(SimpleConfigDialog.class);

  /** Device */
  private SimpleDevice mDevice;
  /** Connection */
  private SimpleConnectionIf mConnection;
  /** Configuration */
  private SimpleConfig mConfig;
  /** Which Button was pressed */
  private int mReturn = JOptionPane.CANCEL_OPTION;
  /** Table with mapping */
  private JTable mTable;

  private JTextField mName;

  private void initialize(SimpleDevice dev, SimpleConnectionIf connection,
      SimpleConfig config) {
    mConnection = connection;
    mConfig = (SimpleConfig) config.clone();
    mDevice = dev;
    createGui();
  }

  /**
   * Create Dialog
   * 
   * @param parent
   *          Parent
   * @param dev
   *          Device
   * @param connection
   *          Connection
   * @param config
   *          Configuration
   */
  public SimpleConfigDialog(Window parent, SimpleDevice dev,
      SimpleConnectionIf connection, SimpleConfig config) {
    super(parent);
    setModal(true);
    initialize(dev, connection, config);
  }

  /**
   * Create the Gui
   */
  private void createGui() {
    JPanel panel = (JPanel) getContentPane();
    
    setTitle(mLocalizer.msg("title","Device Settings"));
    
    panel.setLayout(new FormLayout("3dlu, pref, 3dlu, fill:pref:grow, 3dlu, pref, 3dlu", "pref, 5dlu, pref, 3dlu, pref, 5dlu, fill:min:grow, 3dlu, pref, 3dlu, pref"));
    panel.setBorder(Borders.DIALOG_BORDER);
    
    CellConstraints cc = new CellConstraints();
    
    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("deviceName","Device name")), cc.xyw(1,1, 7));

    panel.add(new JLabel(mLocalizer.msg("deviceNameInput", "Name")+ ":"), cc.xy(2,3));
    mName = new JTextField(mDevice.getName());
    panel.add(mName, cc.xyw(4,3,3));
    
    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("channelAssignment","Channel assignment")), cc.xyw(1,5, 7));
    
    mTable = new JTable(new ConfigTableModel(mConfig, mLocalizer.msg("external", "external")));
    mTable.getTableHeader().setReorderingAllowed(false);
    mTable.getColumnModel().getColumn(0).setCellRenderer(new ChannelTableCellRenderer());
    mTable.getColumnModel().getColumn(1).setCellRenderer(new ExternalChannelTableCellRenderer());
    mTable.getColumnModel().getColumn(1).setCellEditor(new ExternalChannelTableCellEditor(mConfig));
    panel.add(new JScrollPane(mTable), cc.xyw(2,7,5));
    
    JButton fetch = new JButton(mLocalizer.msg("fetchChannels","Fetch Channellist"));
    
    fetch.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            SimpleChannel[] lists = mConnection.getAvailableChannels();

            if (lists == null) {
                JOptionPane.showMessageDialog(SimpleConfigDialog.this,
                        mLocalizer.msg("errorChannels","Could not load external channels"),
                        mLocalizer.msg("errorTitle","Error"), JOptionPane.ERROR_MESSAGE);
            } else {
                mConfig.setExternalChannels(lists);
            }

            mTable.repaint();
          }
        });
      }
    });
    
    panel.add(fetch, cc.xy(6,9));
    
    ButtonBarBuilder2 builder = new ButtonBarBuilder2();
    builder.addGlue();
    
    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        if (mTable.isEditing()) {
            TableCellEditor editor = mTable.getCellEditor();
            if (editor != null) {
              editor.stopCellEditing();
            }
        }
        mReturn = JOptionPane.OK_OPTION;
        setVisible(false);
      }
    });

    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        setVisible(false);
      }
    });
    
    builder.addButton(new JButton[] {ok, cancel});
    
    panel.add(builder.getPanel(), cc.xyw(1,11,7));
    
    getRootPane().setDefaultButton(ok);
    UiUtilities.registerForClosing(this);
    
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
  public SimpleConfig getConfig() {
    return mConfig;
  }

  /**
   * @return Modified Name
   */
  public String getName() {
    return mName.getText();
  }
  
  public void close() {
    setVisible(false);
  }
  
}