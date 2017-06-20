package tvbrowser.extras.reminderplugin;

import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.SettingsItem;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.ui.mainframe.MainFrame;
import util.programkeyevent.ProgramKeyAndContextMenuListener;
import util.programkeyevent.ProgramKeyEventHandler;
import util.programmouseevent.ProgramMouseAndContextMenuListener;
import util.programmouseevent.ProgramMouseEventHandler;
import util.settings.PluginPictureSettings;
import util.ui.ProgramList;
import util.ui.ProgramTableCellRenderer;
import util.ui.SendToPluginDialog;
import util.ui.TVBrowserIcons;
import util.ui.TabListenerPanel;
import util.ui.UiUtilities;
import util.ui.persona.Persona;
import util.ui.persona.PersonaListener;

public class ReminderListPanel extends TabListenerPanel implements PersonaListener, ProgramMouseAndContextMenuListener {
  private static final util.ui.Localizer mLocalizer = ReminderListDialog.mLocalizer; 
  
  private JButton mUndo, mDelete, mSend, mScrollToPreviousDay, mScrollToNextDay;
  private ReminderList mReminderList;
  private JTable mTable;
  
  private ReminderTableModel mModel;
  private RevertCache<ReminderListItem[]> mRevertCache;
  
  private JComboBox<String> mTitleSelection;
  private JLabel mFilterLabel;
  private long mLastEditorClosing;
  private JScrollPane tableScroll;
  
  public ReminderListPanel(ReminderList list, JButton close) {
    mLastEditorClosing = 0;
    mReminderList = list;
    createGui(close);
  }
  
  private void createGui(JButton close) {
    FormLayout layout = new FormLayout("default,5dlu,50dlu:grow", "default,5dlu,fill:default:grow");
    setLayout(layout);
    setOpaque(false);
    
    if(close == null) {
      setBorder(Borders.DLU4);
    }

    CellConstraints cc = new CellConstraints();
    
    mRevertCache = new RevertCache<ReminderListItem[]>(3);
    mModel = new ReminderTableModel(mReminderList, mTitleSelection = new JComboBox<>());
    
    mTable = new JTable();
    setDefaultFocusOwner(mTable);
    mTable.getTableHeader().setResizingAllowed(false);
    mTable.getTableHeader().setReorderingAllowed(false);
    mTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()) {
          int[] selection = mTable.getSelectedRows();
          
          if(selection.length == 1) {
            SwingUtilities.invokeLater(() -> {
              Rectangle r = mTable.getCellRect(selection[0], 0, false);
              mTable.scrollRectToVisible(r);
            });
          }
        }
      }
    });
    mTable.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
          mTable.getRootPane().dispatchEvent(e);
        }
      }
    });
    
    ProgramMouseEventHandler mouseEventHandler = new ProgramMouseEventHandler(this, ReminderPluginProxy.getInstance()) {
      public void mouseClicked(final MouseEvent e) {
        final int row = mTable.rowAtPoint(e.getPoint());
        final int[] rows = mTable.getSelectedRows();
        
        if(mTable.getValueAt(row, 0).equals(PluginManagerImpl.getInstance().getExampleProgram()) && rows.length <= 1) {
          mTable.getSelectionModel().setSelectionInterval(row+1, row+1);
        }
        else {
          final int column = mTable.columnAtPoint(e.getPoint());
          
          if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 1) && column == 1) {
            mTable.editCellAt(row,column);
            ((MinutesCellEditor)mTable.getCellEditor()).getComboBox().addPopupMenuListener(new PopupMenuListener() {
              
              @Override
              public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
              
              @Override
              public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                mLastEditorClosing = System.currentTimeMillis();
              }
              
              @Override
              public void popupMenuCanceled(PopupMenuEvent e) {}
            });
            ((MinutesCellEditor)mTable.getCellEditor()).getComboBox().showPopup();
          }
          else {
            super.mouseClicked(e);
          }
          
          mTable.repaint();
        }
      }
    };

    mTable.addMouseListener(mouseEventHandler);
    mTable.addKeyListener(new ProgramKeyEventHandler(new ProgramKeyAndContextMenuListener() {
      @Override
      public void showContextMenu(Program program) {
        final Rectangle rect = mTable.getCellRect(mTable.getSelectedRow(), 0, false);
        final Point p = new Point((int)(rect.x + rect.width * 1/4.)+15, (int)(rect.y + rect.height * 2/3.) + 15);
        
        ReminderListPanel.this.showContextMenu(p);
      }
      
      @Override
      public void keyEventActionFinished() {}
      
      @Override
      public Program getProgramForKeyEvent(KeyEvent e) {
        int row = mTable.getSelectedRow();
        return (Program) mTable.getModel().getValueAt(row, 0);
      }
    }, ReminderPluginProxy.getInstance()));
    
    installTableModel(mModel);
    
    UiUtilities.addKeyRotation(mTable, index -> {
      if(index >= 0 && index < mTable.getRowCount()) {
        return (Program)mTable.getValueAt(index, 0);
      }
      
      return null;
    });
    
    add(mFilterLabel = new JLabel(mLocalizer.msg("titleFilterText","Show only programs with the following title:")), cc.xy(1,1));
    add(mTitleSelection, cc.xy(3,1));

    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.setLeftToRight(true);

    JButton config = new JButton(TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));

    config.setToolTipText(mLocalizer.msg("config", "Configure Reminder"));
    
    config.addActionListener(e -> {
      MainFrame.getInstance().showSettingsDialog(SettingsItem.REMINDER);
    });

    if(close != null) {
      builder.addFixed(config);
      builder.addRelatedGap();
    }
    
    mSend = new JButton(TVBrowserIcons.copy(TVBrowserIcons.SIZE_SMALL));
    mSend.setToolTipText(mLocalizer.msg("send", "Send to other Plugins"));
    mSend.setEnabled(mTable.getRowCount() > 0);

    mSend.addActionListener(e -> {
      showSendDialog();
    });

    builder.addFixed(mSend);
    builder.addRelatedGap();

    mDelete = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mDelete.setToolTipText(mLocalizer.msg("delete", "Remove all/selected programs from reminder list"));
    mDelete.setEnabled(mTable.getRowCount() > 0);
    
    mDelete.addActionListener(e -> {
      deleteItems();
    });

    builder.addFixed(mDelete);
    builder.addRelatedGap();
    
    mUndo = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "edit-undo", 16));
    mUndo.setToolTipText(mLocalizer.msg("undo","Undo"));
    mUndo.setEnabled(false);
    
    mUndo.addActionListener(e -> {
      undo();
    });
    
    builder.addFixed(mUndo);
    
    mScrollToPreviousDay = new JButton(TVBrowserIcons.left(TVBrowserIcons.SIZE_SMALL));
    mScrollToPreviousDay.setToolTipText(ProgramList.getPreviousActionTooltip());
    mScrollToPreviousDay.setEnabled(mTable.getRowCount() > 0);
    mScrollToPreviousDay.addActionListener(e -> {
      int row = mTable.rowAtPoint(mTable.getVisibleRect().getLocation())-1;
      
      if(row > 0) {
        Object o = mTable.getValueAt(row, 0);
        
        if(o.equals(PluginManagerImpl.getInstance().getExampleProgram())) {
          o = mTable.getValueAt(row-1, 0);
          row--;
        }
        
        if(row > 0) {
          Date current = ((Program)o).getDate();
          
          for(int i = row-1; i >= 0; i--) {
            Object test = mTable.getValueAt(i, 0);
            
            if(!test.equals(PluginManagerImpl.getInstance().getExampleProgram()) && test instanceof Program && current.compareTo(((Program)test).getDate()) > 0) {
              mTable.scrollRectToVisible(mTable.getCellRect(i+1, 0, true));
              return;
            }
          }
        }
      }
      
      if(mTable.getRowCount() > 0) {
        mTable.scrollRectToVisible(mTable.getCellRect(0, 0, true));
      }
    });
        
    builder.addUnrelatedGap();
    builder.addFixed(mScrollToPreviousDay);
    
    mScrollToNextDay = new JButton(TVBrowserIcons.right(TVBrowserIcons.SIZE_SMALL));
    mScrollToNextDay.setToolTipText(ProgramList.getNextActionTooltip());
    mScrollToNextDay.setEnabled(mTable.getRowCount() > 0);
    mScrollToNextDay.addActionListener(e -> {
      int row = mTable.rowAtPoint(mTable.getVisibleRect().getLocation());
      
      if(row < mTable.getRowCount() - 1) {
        Object o = mTable.getValueAt(row, 0);
        
        if(o.equals(PluginManagerImpl.getInstance().getExampleProgram())) {
          o = mTable.getValueAt(row+1, 0);
          row++;
        }
        
        if(row < mTable.getRowCount() - 1) {
          Date current = ((Program)o).getDate();
          
          for(int i = row + 1; i < mTable.getRowCount(); i++) {
            Object test = mTable.getValueAt(i, 0);
            
            if(test instanceof Program && current.compareTo(((Program)test).getDate()) < 0) {
              Rectangle rect = mTable.getCellRect(i-(ReminderPlugin.getInstance().showDateSeparators() ? 1 : 0), 0, true);
              rect.setSize(rect.width, mTable.getVisibleRect().height);
              
              mTable.scrollRectToVisible(rect);
              return;
            }
          }            
        }
      }
    });
    
    builder.addRelatedGap();
    builder.addFixed(mScrollToNextDay);
    
    if(close != null) {
      builder.addUnrelatedGap();
      builder.addGlue();
      builder.addFixed(close);
    }
    builder.getPanel().setOpaque(false);
    
    tableScroll = new JScrollPane(mTable);
    tableScroll.setBorder(BorderFactory.createEmptyBorder());
    tableScroll.setBackground(UIManager.getColor("TextField.background"));
    tableScroll.getViewport().setBackground(UIManager.getColor("TextField.background"));
    
    if(close != null) {
      layout.appendRow(RowSpec.decode("3dlu"));
      layout.appendRow(RowSpec.decode("default"));

      add(tableScroll, cc.xyw(1, 3, 3));
      add(builder.getPanel(), cc.xyw(1, 5, 3));
    }
    else {
      layout.appendColumn(ColumnSpec.decode("5dlu"));
      layout.appendColumn(ColumnSpec.decode("default"));
      
      add(tableScroll, cc.xyw(1, 3, 5));
      add(builder.getPanel(), cc.xy(5, 1));
    }
  }
  

  private void installTableModel(ReminderTableModel model) {
    DefaultTableColumnModel cModel = new DefaultTableColumnModel() {
      public TableColumn getColumn(int n) {
        TableColumn column = super.getColumn(n);
        
        if(n == 1) {
          column.setMaxWidth(mTable.getFontMetrics(mTable.getFont()).stringWidth(ReminderConstants.REMIND_AFTER_VALUE_ARR[ReminderConstants.REMIND_AFTER_VALUE_ARR.length-1].toString())+20);
          column.setPreferredWidth(column.getMaxWidth());
        }
        
        return column;
      }
    };
    
    mTable.setColumnModel(cModel);
    mTable.setModel(model);
    mTable.setSelectionBackground(Settings.propKeyboardSelectedColor.getColor());
    
    final ProgramTableCellRenderer backend = new ProgramTableCellRenderer(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE));
    
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
      private JPanel mDateSeparator;
      private JLabel mDateLabel;
      
      @SuppressWarnings("unused")
      public void initialize() {
        mDateSeparator = new JPanel(new FormLayout("0dlu:grow,default,0dlu:grow","5dlu,min,5dlu"));
        mDateSeparator.setBorder(BorderFactory.createMatteBorder(2, 0, 2, 0, UIManager.getColor("Label.foreground")));
        
        mDateLabel = new JLabel();
        mDateLabel.setFont(mDateLabel.getFont().deriveFont(mDateLabel.getFont().getSize2D() + 4).deriveFont(Font.BOLD));
        
        mDateSeparator.add(mDateLabel, new CellConstraints().xy(2, 2));
      }
      
      public Component getTableCellRendererComponent(final JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = backend.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        try {
          if(value instanceof Program && value.equals(PluginManagerImpl.getInstance().getExampleProgram())) {
            mDateLabel.setText(((Program)table.getModel().getValueAt(row+1, 0)).getDateString());
            
            if(table.getModel().getRowCount() > row + 1 && table.getRowHeight(row) != mDateSeparator.getPreferredSize().height) {
              table.setRowHeight(row, mDateSeparator.getPreferredSize().height);
            }
            
            c = mDateSeparator;
          }
        }catch(Exception e) {e.printStackTrace();}
        
        return c;
      }
    };
    
    try {
      final Method initialize = renderer.getClass().getDeclaredMethod("initialize");
      initialize.invoke(renderer);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    
    mTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
    mTable.getColumnModel().getColumn(1).setCellEditor(new MinutesCellEditor());
    mTable.getColumnModel().getColumn(1).setCellRenderer(new MinutesCellRenderer());
    updateButtons();
    
    SwingUtilities.invokeLater(() -> {
      try {
        mTable.updateUI();
      }catch(Exception e1) {}
    });
  }
  

  private void deleteItems() {
    int[] selected = mTable.getSelectedRows();

    if (selected.length < 1 && mTable.getRowCount() > 0) {
      mTable.getSelectionModel().setSelectionInterval(0, mTable.getRowCount() - 1);
      selected = mTable.getSelectedRows();
    }
    
    final boolean frameReminders = ReminderPropertyDefaults.getPropertyDefaults().getValueFromProperties(ReminderPropertyDefaults.KEY_FRAME_REMINDERS_SHOW,ReminderPlugin.getInstance().getSettings()).equalsIgnoreCase("true");

    if (selected.length > 0) {
      Arrays.sort(selected);

      ArrayList<ReminderListItem> itemList = new ArrayList<ReminderListItem>();
      
      for (int element : selected) {
        Program prog = (Program) mTable.getValueAt(element, 0);

        ReminderListItem item = mReminderList.removeWithoutChecking(prog);
        
        if(frameReminders) {
          FrameReminders.getInstance().removeReminder(item);
        }
        
        if(item != null) {
          itemList.add(item);
        }
      }
      
      mRevertCache.push(itemList.toArray(new ReminderListItem[itemList.size()]));

      final int row = selected[0] - 1;

      installTableModel(new ReminderTableModel(mReminderList, mTitleSelection));
      SwingUtilities.invokeLater(() -> {
        mTable.scrollRectToVisible(mTable.getCellRect(row, 0, true));
      });
      
      ReminderPlugin.getInstance().updateRootNode(true,false);
    }
    
    updateButtons();
  }
  
  private void undo() {
    for(ReminderListItem item : mRevertCache.pop()) {
      mReminderList.addWithoutChecking(item);
      item.getProgram().mark(ReminderPluginProxy.getInstance());
    }
    
    mUndo.setEnabled(!mRevertCache.isEmpty());
   
    installTableModel(new ReminderTableModel(mReminderList, mTitleSelection));
    ReminderPlugin.getInstance().updateRootNode(true);
    
    updateButtons();
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
        Program test = (Program) mTable.getValueAt(row, 0);
        
        if(!test.equals(PluginManagerImpl.getInstance().getExampleProgram())) {
          programs.add(test);
        }
      }

      programArr = programs.toArray(new Program[programs.size()]);
    }

    if (programArr.length > 0) {
      SendToPluginDialog send = new SendToPluginDialog(ReminderPluginProxy.getInstance(),ReminderListDialog.getInstance() != null ? (Window)ReminderListDialog.getInstance() : MainFrame.getInstance(), programArr);
      send.setVisible(true);
    }
  }
  
  void stopCellEditing() {
    if (mTable.isEditing()) {
      mTable.getCellEditor().stopCellEditing();
    }
  }
  
  public void installTableModel(boolean scroll) {
    installTableModel(new ReminderTableModel(mReminderList, mTitleSelection));
    
    if(scroll) {
      SwingUtilities.invokeLater(() -> {
        mTable.scrollRectToVisible(new Rectangle(0,0));
      });
    }
  }
  
  void updateTableEntries() {System.out.println("hier");
    mModel.updateTableEntries();
    updateButtons();
    if(tableScroll != null) {
      tableScroll.getViewport().getSize();
    }
  }
  
  private void updateButtons() {
    if(mDelete != null) {
      mDelete.setEnabled(mTable.getRowCount() > 0);
      mSend.setEnabled(mTable.getRowCount() > 0);
      mScrollToPreviousDay.setEnabled(mTable.getRowCount() > 0);
      mScrollToNextDay.setEnabled(mTable.getRowCount() > 0);
      mUndo.setEnabled(!mRevertCache.isEmpty());
    }
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
  
  private class RevertCache<E> {
    private ArrayList<E> mStackList;
    private int mSize;
    
    public RevertCache(int size) {
      mSize = size;
      mStackList = new ArrayList<E>(size);
    }
    
    public void push(E value) {
      if(mStackList.size() >= mSize) {
        mStackList.remove(0);
      }
      
      mStackList.add(value);
    }
    
    public E pop() {
      if(mStackList.size() < 1) {
        return null;
      }
      
      return mStackList.remove(mStackList.size()-1);
    }
        
    public boolean isEmpty() {
      return mStackList.isEmpty();
    }
  }

  @Override
  public Program getProgramForMouseEvent(MouseEvent e) {
    int column = mTable.columnAtPoint(e.getPoint());
    
    if(column == 0 && mLastEditorClosing + Plugin.SINGLE_CLICK_WAITING_TIME + 50 < System.currentTimeMillis()) {
      int row = mTable.rowAtPoint(e.getPoint());
      
      Program p = (Program) mTable.getModel().getValueAt(row, 0);
      
      return p;
    }
    
    return null;
  }

  @Override
  public void mouseEventActionFinished() {
   // mTable.repaint();
  }

  @Override
  public void showContextMenu(MouseEvent e) {
    showContextMenu(e.getPoint());
  }
  
  private void showContextMenu(Point point) {
    int row = mTable.rowAtPoint(point);

    mTable.changeSelection(row, 0, false, false);

    Program p = (Program) mTable.getModel().getValueAt(row, 0);

    JPopupMenu menu = PluginManagerImpl.getInstance().createPluginContextMenu(p, ReminderPluginProxy.getInstance());
    UiUtilities.registerForClosing(menu);
    menu.show(mTable, point.x - 15, point.y - 15);    
  }
  
  public static final int SCROLL_TO_DATE_TYPE = 0;
  public static final int SCROLL_TO_NOW_TYPE = 1;
  public static final int SCROLL_TO_NEXT_TIME_TYPE = 2;
  public static final int SCROLL_TO_TIME_TYPE = 3;
  
  
  
  public void scrollTo(int type, Date date, int time) {
    Program example = PluginManagerImpl.getInstance().getExampleProgram();
    int startRow = 0;
    int rowTime = -1;
        
    if(date == null) {
      int row = mTable.rowAtPoint(mTable.getVisibleRect().getLocation());
      
      if(row < mTable.getRowCount()) {
        Program test = null;
        
        do {
          Object o = mTable.getValueAt(row, 0);
          
          if(o instanceof Program) {
        	  test = (Program)mTable.getValueAt(row, 0);
          }
          
          row++;
        }while(test != null && test.equals(example) && row < mTable.getRowCount());
        
        if(test != null) {
          date = test.getDate();
          
          if(type == SCROLL_TO_NEXT_TIME_TYPE) {
            startRow = row-1;
            rowTime = test.getStartTime();
          }
        }
      }
    }
    
    for(int i = startRow; i < mTable.getRowCount(); i++) {
      Program test = (Program)mTable.getValueAt(i, 0);
      
      if(test instanceof Program && !test.equals(example)) {
        boolean condition = false;
        int sub = 0;
        
        switch(type) {
          case SCROLL_TO_DATE_TYPE: condition = date.compareTo(test.getDate()) == 0;break;
          case SCROLL_TO_NOW_TYPE: condition = !test.isExpired();break;
          case SCROLL_TO_NEXT_TIME_TYPE: condition = ((test.getDate().compareTo(date) == 0 && test.getStartTime() >= time && rowTime < time) || (test.getDate().compareTo(date) > 0 && test.getStartTime() >= time)) && rowTime != time;break;
          case SCROLL_TO_TIME_TYPE: {
            condition = (test.getDate().compareTo(date) == 0 && test.getStartTime() >= time) || test.getDate().compareTo(date) > 0;
            
            if(condition && (test.getStartTime() > time || test.getDate().compareTo(date) > 0)) {
              sub = 1;
            }
          }break;
        }
        
        if(condition) {
          int add = 0;
          
          if(i > 0 && ((Program)mTable.getValueAt(i-1, 0)).equals(example)) {
            add = 1;
          }
          
          final Rectangle rect = mTable.getCellRect(i-add-sub, 0, true);
          rect.setSize(rect.width, mTable.getVisibleRect().height);
          
          SwingUtilities.invokeLater(() -> {
            mTable.scrollRectToVisible(rect);
          });
          
          return;
        }
      }
    }
  }
}
