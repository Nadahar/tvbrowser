package tvbrowser.extras.reminderplugin;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.ui.mainframe.MainFrame;
import util.settings.PluginPictureSettings;
import util.ui.ProgramTableCellRenderer;
import util.ui.SendToPluginDialog;
import util.ui.TVBrowserIcons;
import util.ui.persona.Persona;
import util.ui.persona.PersonaListener;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Plugin;
import devplugin.Program;
import devplugin.SettingsItem;

public class ReminderListPanel extends JPanel implements PersonaListener {
  private static final util.ui.Localizer mLocalizer = ReminderListDialog.mLocalizer; 
  
  private JButton mUndo, mDelete, mSend;
  private ReminderList mReminderList;
  private JTable mTable;
  
  private ReminderTableModel mModel;
  private ReminderListItem[] mDeletedItems;
  
  private JComboBox mTitleSelection;
  private JLabel mFilterLabel;
  
  public ReminderListPanel(ReminderList list, JButton close) {
    mReminderList = list;
    createGui(close);
  }
  
  private void createGui(JButton close) {
    FormLayout layout = new FormLayout("default,5dlu,50dlu:grow", "default,5dlu,fill:default:grow");
    setLayout(layout);
    setOpaque(false);
    setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();
    
    mModel = new ReminderTableModel(mReminderList, mTitleSelection = new JComboBox());

    mTable = new JTable();
    mTable.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
          mTable.getRootPane().dispatchEvent(e);
        }
      }
    });

    mTable.addMouseListener(new MouseAdapter() {
      private Thread mLeftClickThread;
      private boolean mPerformingSingleClick = false;

      private Thread mMiddleSingleClickThread;
      private boolean mPerformingMiddleSingleClick = false;
      
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
        if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 1) && e.getModifiersEx() == 0) {
          int column = mTable.columnAtPoint(e.getPoint());

          if (column == 1) {
            int row = mTable.rowAtPoint(e.getPoint());
            mTable.editCellAt(row,column);
            ((MinutesCellEditor)mTable.getCellEditor()).getComboBox().showPopup();
          }
          
          mLeftClickThread = new Thread("Single click") {
            public void run() {
              try {
                mPerformingSingleClick = false;
                sleep(Plugin.SINGLE_CLICK_WAITING_TIME);
                mPerformingSingleClick = true;
                
                if (mTable.columnAtPoint(e.getPoint()) == 1) {
                  return;
                }

                int row = mTable.rowAtPoint(e.getPoint());

                mTable.changeSelection(row, 0, false, false);
                Program p = (Program) mTable.getModel().getValueAt(row, 0);
                
                Plugin.getPluginManager().handleProgramSingleClick(p, ReminderPluginProxy.getInstance());
                mPerformingSingleClick = false;
              } catch (InterruptedException ex) { // ignore
              }
            }
          };
          
          mLeftClickThread.setPriority(Thread.MIN_PRIORITY);
          mLeftClickThread.start();
        }
        else if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2) && e.getModifiersEx() == 0) {
          if(!mPerformingSingleClick && mLeftClickThread != null && mLeftClickThread.isAlive()) {
            mLeftClickThread.interrupt();
          }
          
          if(!mPerformingSingleClick) {
            int column = mTable.columnAtPoint(e.getPoint());
  
            if (column == 1) {
              return;
            }
  
            int row = mTable.rowAtPoint(e.getPoint());
  
            mTable.changeSelection(row, 0, false, false);
            Program p = (Program) mTable.getModel().getValueAt(row, 0);
  
            PluginManagerImpl.getInstance().handleProgramDoubleClick(p, ReminderPluginProxy.getInstance());
          }
        }
        else if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1)) {
          mMiddleSingleClickThread = new Thread("Single click") {
            public void run() {
              try {
                mPerformingMiddleSingleClick = false;
                sleep(Plugin.SINGLE_CLICK_WAITING_TIME);
                mPerformingMiddleSingleClick = true;
                
                if (mTable.columnAtPoint(e.getPoint()) == 1) {
                  return;
                }

                int row = mTable.rowAtPoint(e.getPoint());

                mTable.changeSelection(row, 0, false, false);
                Program p = (Program) mTable.getModel().getValueAt(row, 0);
                
                Plugin.getPluginManager().handleProgramMiddleClick(p, ReminderPluginProxy.getInstance());
                mPerformingMiddleSingleClick = false;
              } catch (InterruptedException ex) { // ignore
              }
            }
          };
          
          mMiddleSingleClickThread.setPriority(Thread.MIN_PRIORITY);
          mMiddleSingleClickThread.start();
        }
        else if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 2)) {
          if(!mPerformingMiddleSingleClick && mMiddleSingleClickThread != null && mMiddleSingleClickThread.isAlive()) {
            mMiddleSingleClickThread.interrupt();
          }
          
          if(!mPerformingMiddleSingleClick) {
            int column = mTable.columnAtPoint(e.getPoint());
  
            if (column == 1) {
              return;
            }
  
            int row = mTable.rowAtPoint(e.getPoint());
  
            mTable.changeSelection(row, 0, false, false);
            Program p = (Program) mTable.getModel().getValueAt(row, 0);
  
            PluginManagerImpl.getInstance().handleProgramMiddleDoubleClick(p, ReminderPluginProxy.getInstance());
          }
        }
        
        mTable.repaint();
      }
    });

    installTableModel(mModel);
    
    add(mFilterLabel = new JLabel(mLocalizer.msg("titleFilterText","Show only programs with the following title:")), cc.xy(1,1));
    add(mTitleSelection, cc.xy(3,1));

    ButtonBarBuilder2 builder = ButtonBarBuilder2.createLeftToRightBuilder();

    JButton config = new JButton(TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));

    config.setToolTipText(mLocalizer.msg("config", "Configure Reminder"));
    
    config.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showSettingsDialog(SettingsItem.REMINDER);
      }
    });

    if(close != null) {
      builder.addFixed(config);
      builder.addRelatedGap();
    }
    
    mSend = new JButton(TVBrowserIcons.copy(TVBrowserIcons.SIZE_SMALL));
    mSend.setToolTipText(mLocalizer.msg("send", "Send to other Plugins"));
    mSend.setEnabled(mTable.getRowCount() > 0);

    mSend.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showSendDialog();
      }
    });

    builder.addFixed(mSend);
    builder.addRelatedGap();

    mDelete = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mDelete.setToolTipText(mLocalizer.msg("delete", "Remove all/selected programs from reminder list"));
    mDelete.setEnabled(mTable.getRowCount() > 0);
    
    mDelete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteItems();
      }
    });

    builder.addFixed(mDelete);
    builder.addRelatedGap();
    
    mUndo = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "edit-undo", 16));
    mUndo.setToolTipText(mLocalizer.msg("undo","Undo"));
    mUndo.setEnabled(false);
    
    mUndo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        undo();
      }
    });
    
    builder.addFixed(mUndo);
    builder.addRelatedGap();



    builder.addGlue();
    
    if(close != null) { 
      builder.addFixed(close);
    }
    builder.getPanel().setOpaque(false);
    
    if(close != null) {
      layout.appendRow(RowSpec.decode("3dlu"));
      layout.appendRow(RowSpec.decode("default"));

      add(new JScrollPane(mTable), cc.xyw(1, 3, 3));
      add(builder.getPanel(), cc.xyw(1, 5, 3));
    }
    else {
      layout.appendColumn(ColumnSpec.decode("5dlu"));
      layout.appendColumn(ColumnSpec.decode("default"));
      
      add(new JScrollPane(mTable), cc.xyw(1, 3, 5));
      add(builder.getPanel(), cc.xy(5, 1));
    }
  }
  

  private void installTableModel(ReminderTableModel model) {
    DefaultTableColumnModel cModel = new DefaultTableColumnModel() {
      public TableColumn getColumn(int n) {
        TableColumn column = super.getColumn(n);
        
        if(n == 1) {
          column.setMaxWidth(300);
          column.setPreferredWidth(250);
        }
        
        return column;
      }
    };
    
    mTable.setColumnModel(cModel);
    mTable.setModel(model);
    mTable.getColumnModel().getColumn(0).setCellRenderer(new ProgramTableCellRenderer(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE)));
    mTable.getColumnModel().getColumn(1).setCellEditor(new MinutesCellEditor());
    mTable.getColumnModel().getColumn(1).setCellRenderer(new MinutesCellRenderer());
  }
  

  private void deleteItems() {
    int[] selected = mTable.getSelectedRows();

    if (selected.length < 1 && mTable.getRowCount() > 0) {
      mTable.getSelectionModel().setSelectionInterval(0, mTable.getRowCount() - 1);
      selected = mTable.getSelectedRows();
    }

    if (selected.length > 0) {
      Arrays.sort(selected);

      ArrayList<ReminderListItem> itemList = new ArrayList<ReminderListItem>();
      
      for (int element : selected) {
        Program prog = (Program) mTable.getValueAt(element, 0);

        ReminderListItem item = mReminderList.removeWithoutChecking(prog);
        
        if(item != null) {
          itemList.add(item);
        }
      }
      
      mDeletedItems = itemList.toArray(new ReminderListItem[itemList.size()]);

      final int row = selected[0] - 1;

      installTableModel(new ReminderTableModel(mReminderList, mTitleSelection));
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          mTable.scrollRectToVisible(mTable.getCellRect(row, 0, true));
        };
      });
      
      ReminderPlugin.getInstance().updateRootNode(true);
    }
    
    mDelete.setEnabled(mTable.getRowCount() > 0);
    mSend.setEnabled(mTable.getRowCount() > 0);
    mUndo.setEnabled(mDeletedItems != null && mDeletedItems.length > 0);
  }
  
  private void undo() {
    for(ReminderListItem item : mDeletedItems) {
      mReminderList.addWithoutChecking(item);
      item.getProgram().mark(ReminderPluginProxy.getInstance());
    }
    
    mDeletedItems = null;
    mUndo.setEnabled(false);
   
    installTableModel(new ReminderTableModel(mReminderList, mTitleSelection));
    ReminderPlugin.getInstance().updateRootNode(true);
    
    mDelete.setEnabled(mTable.getRowCount() > 0);
    mSend.setEnabled(mTable.getRowCount() > 0);
  }

  private void showSendDialog() {
    int[] rows = mTable.getSelectedRows();
    Program[] programArr;

    if (rows == null || rows.length == 0) {
      ReminderListItem[] items = mReminderList.getReminderItems();
      programArr = new Program[items.length];

      for (int i = 0; i < items.length; i++) {
        programArr[i] = items[i].getProgram();
      }
    } else {
      ArrayList<Program> programs = new ArrayList<Program>();

      for (int row : rows) {
        programs.add((Program) mTable.getValueAt(row, 0));
      }

      programArr = programs.toArray(new Program[programs.size()]);
    }

    if (programArr.length > 0) {
      SendToPluginDialog send = new SendToPluginDialog(ReminderPluginProxy.getInstance(),ReminderListDialog.getInstance() != null ? (Window)ReminderListDialog.getInstance() : MainFrame.getInstance(), programArr);
      send.setVisible(true);
    }
  }

  /**
   * Shows the Popup
   * 
   * @param e Mouse-Event
   */
  private void showPopup(MouseEvent e) {
    int row = mTable.rowAtPoint(e.getPoint());

    mTable.changeSelection(row, 0, false, false);

    Program p = (Program) mTable.getModel().getValueAt(row, 0);

    JPopupMenu menu = PluginManagerImpl.getInstance().createPluginContextMenu(p, ReminderPluginProxy.getInstance());
    menu.show(mTable, e.getX() - 15, e.getY() - 15);
  }
  
  void stopCellEditing() {
    if (mTable.isEditing()) {
      mTable.getCellEditor().stopCellEditing();
    }
  }
  
  void installTableModel() {
    installTableModel(new ReminderTableModel(mReminderList, mTitleSelection));
  }
  
  void updateTableEntries() {
    mModel.updateTableEntries();
  }

  @Override
  public void updatePersona() {
    if(Persona.getInstance().getHeaderImage() != null) {
      mFilterLabel.setForeground(Persona.getInstance().getTextColor());
    }
    else {
      mFilterLabel.setForeground(UIManager.getColor("Label.foreground"));
    }
  }
}
