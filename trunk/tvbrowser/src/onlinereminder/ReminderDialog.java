package onlinereminder;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import util.ui.Localizer;
import util.ui.ProgramTableCellRenderer;
import util.ui.SendToPluginDialog;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;

public class ReminderDialog extends JDialog {
  /** Localisation */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ReminderDialog.class);

  private Configuration mConf;

  private JTable mTable;

  public ReminderDialog(Frame parentFrame, Configuration config) {
    super(parentFrame, true);
    setTitle(mLocalizer.msg("title", "Title"));
    mConf = config;
    createGui();
  }

  private void createGui() {
    JPanel panel = (JPanel) getContentPane();

    panel.setLayout(new FormLayout("fill:default:grow", "fill:default:grow, 3dlu, default"));

    panel.setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();

    ReminderTableModel model = new ReminderTableModel(mConf);

    mTable = new JTable();

    mTable.addMouseListener(new MouseAdapter() {

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

      public void mouseClicked(MouseEvent e) {
        int column = mTable.columnAtPoint(e.getPoint());
        if (column != 1) {
          return;
        }
        if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
          int row = mTable.rowAtPoint(e.getPoint());
          mTable.changeSelection(row, 0, false, false);
          Program p = (Program) mTable.getModel().getValueAt(row, 0);

          devplugin.Plugin.getPluginManager().handleProgramDoubleClick(p, OnlineReminder.getInstance());
        }
        if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1)) {
          int row = mTable.rowAtPoint(e.getPoint());
          mTable.changeSelection(row, 0, false, false);
          Program p = (Program) mTable.getModel().getValueAt(row, 0);

          devplugin.Plugin.getPluginManager().handleProgramMiddleClick(p, OnlineReminder.getInstance());
        }
      }
    });

    installTableModel(model);

    panel.add(new JScrollPane(mTable), cc.xy(1, 1));

    ButtonBarBuilder builder = ButtonBarBuilder.createLeftToRightBuilder();

    JButton send = new JButton(OnlineReminder.getInstance().createImageIcon("actions", "edit-copy", 16));
    send.setToolTipText("Send to other Plugins");

    send.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showSendDialog();
      }
    });

    builder.addFixed(send);
    builder.addRelatedGap();

    JButton delete = new JButton(OnlineReminder.getInstance().createImageIcon("actions", "edit-delete", 16));
    delete.setToolTipText("Delete selected Reminders");

    delete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteItems();
      }
    });

    builder.addFixed(delete);
    builder.addRelatedGap();

    JButton update = new JButton(OnlineReminder.getInstance().createImageIcon("actions", "view-refresh", 16));
    update.setToolTipText("Send changes to Server");

    update.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        update();
      };
    });

    builder.addFixed(update);
    builder.addRelatedGap();

    JButton ok = new JButton("OK");

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (mTable.isEditing()) {
          mTable.getCellEditor().stopCellEditing();
        }
        setVisible(false);
      }
    });

    builder.addGlue();
    builder.addFixed(ok);

    panel.add(builder.getPanel(), cc.xy(1, 3));

    pack();

    setSize(new Dimension(550, 310));
  }

  /**
   * Shows the Popup
   * @param e Mouse-Event
   */
  private void showPopup(MouseEvent e) {
    int row = mTable.rowAtPoint(e.getPoint());

    mTable.changeSelection(row, 0, false, false);

    Program p = (Program) mTable.getModel().getValueAt(row, 0);

    JPopupMenu menu = devplugin.Plugin.getPluginManager().createPluginContextMenu(p, OnlineReminder.getInstance());
    menu.show(mTable, e.getX() - 15, e.getY() - 15);
  }
  
  
  private void installTableModel(ReminderTableModel model) {
    mTable.setModel(model);
    mTable.getColumnModel().getColumn(0).setCellRenderer(new ProgramTableCellRenderer());
    mTable.getColumnModel().getColumn(1).setCellEditor(new MinutesCellEditor());
    mTable.getColumnModel().getColumn(1).setCellRenderer(new MinutesCellRenderer());
  }

  private void deleteItems() {

    int[] selected = mTable.getSelectedRows();

    if (selected.length > 0) {
      Arrays.sort(selected);

      for (int i = 0; i < selected.length; i++) {
        Program prog = (Program) mTable.getValueAt(selected[i], 0);

        mConf.removeProgram(prog);
      }

      final int row = selected[0] - 1;

      installTableModel(new ReminderTableModel(mConf));
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          mTable.scrollRectToVisible(mTable.getCellRect(row, 0, true));
        };
      });
    }

  }

  private void showSendDialog() {
    Program[] programArr = mConf.getPrograms();

    if (programArr.length > 0) {
      SendToPluginDialog send = new SendToPluginDialog(OnlineReminder.getInstance(), this, programArr);
      send.setVisible(true);
    }
  }

  private void update() {
    new Thread(new Updater(mConf)).run();
  }

}
