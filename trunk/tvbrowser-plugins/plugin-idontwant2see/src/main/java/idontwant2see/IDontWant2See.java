/*
 * IDontWant2See - Plugin for TV-Browser
 * Copyright (C) 2008 René Mach
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package idontwant2see;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.ui.settings.util.ColorLabel;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginsFilterComponent;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * A very simple filter plugin to easily get rid of stupid programs in the
 * program table.
 * 
 * @author René Mach
 */
public final class IDontWant2See extends Plugin {
  private static final String RECEIVE_TARGET_EXCLUDE_EXACT = "target_exclude_exact";

  static final Localizer mLocalizer = Localizer
      .getLocalizerFor(IDontWant2See.class);
  
  private static Date mCurrentDate = Date.getCurrentDate();
  private ArrayList<IDontWant2SeeListEntry> mSearchList;
  private PluginsProgramFilter mFilter;
  private static IDontWant2See mInstance;
  private boolean mSimpleMenu;
  private boolean mSwitchToMyFilter;
  private boolean mDateWasSet;
  private String mLastEnteredExclusionString;
  private Date mLastUsedDate;

  private static final Pattern PATTERN_TITLE_PART = Pattern.compile("(.*)"
      + "((" // one of two alternatives
      + "\\(?(" // optional brackets
      + "(Teil \\d+)" + "|"
      + "(Teil \\d+/\\d+)" + "|"
      + "(Teil \\d+ von \\d+)" + "|"
      + "(\\d+/\\d+)" + ")\\)?" + ")|(" // or
      + "\\((" // mandatory brackets
      + "(Fortsetzung)" + "|" + "(\\d+)" + ")\\)" + "))" + "$"); // at the end
                                                                 // only
  
  public static Version getVersion() {
    return new Version(0,10,1,true);
  }
  
  /**
   * Creates an instance of this plugin.
   */
  public IDontWant2See() {
    mInstance = this;
    mSearchList = new ArrayList<IDontWant2SeeListEntry>();
    mSimpleMenu = true;
    mSwitchToMyFilter = true;
    mLastEnteredExclusionString = "";
    mLastUsedDate = getCurrentDate();
    mDateWasSet = false;
    
    mFilter = new PluginsProgramFilter(this) {
      public String getSubName() {
        return "";
      }

      public boolean accept(Program prog) {
        return acceptInternal(prog);
      }
    };
  }
  
  protected static IDontWant2See getInstance() {
    return mInstance;
  }
  
  public void handleTvDataUpdateFinished() {
    setCurrentDate();
    mDateWasSet = false;
    
    for(IDontWant2SeeListEntry entry : mSearchList) {
      entry.resetDateWasSetFlag();
    }
  }

  private static void setCurrentDate() {
    mCurrentDate = Date.getCurrentDate();
  }
  
  protected boolean acceptInternal(Program prog) {
    if(!mDateWasSet) {
      mLastUsedDate = getCurrentDate();
      mDateWasSet = true;
    }
    
    for(IDontWant2SeeListEntry entry : mSearchList) {
      if(entry.matches(prog)) {
        return false;
      }
    }
    
    return true;
  }
  
  public PluginInfo getInfo() {
    return new PluginInfo(IDontWant2See.class,
        mLocalizer.msg("name","I don't want to see!"),
        mLocalizer.msg("desc","Removes all programs with an entered search text in the title from the program table."),
        "René Mach", "GPL");
  }
  
  private int getSearchTextIndexForProgram(Program p) {
    if(p != null) {
      for(int i = 0; i < mSearchList.size(); i++) {
        if(mSearchList.get(i).matches(p)) {
          return i;
        }
      }
    }
    
    return -1;
  }
  
  public ActionMenu getButtonAction() {
    ContextMenuAction baseAction = new ContextMenuAction(mLocalizer.msg("name","I don't want to see!"),createImageIcon("apps","idontwant2see",16));
    
    ContextMenuAction openExclusionList = new ContextMenuAction(mLocalizer.msg("editExclusionList","Edit exclusion list"),createImageIcon("apps","idontwant2see",16));
    openExclusionList.putValue(Plugin.BIG_ICON, createImageIcon("apps","idontwant2see",22));
    openExclusionList.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Window w = UiUtilities.getLastModalChildOf(getParentFrame());
        
        JDialog temDlg = null;
        
        if(w instanceof JDialog) {
          temDlg = new JDialog((JDialog)w,true);
        }
        else {
          temDlg = new JDialog((JFrame)w,true);
        }
        
        final JDialog exclusionListDlg = temDlg;
        exclusionListDlg.setTitle(mLocalizer
            .msg("name", "I don't want to see!")
            + " - "
            + mLocalizer.msg("editExclusionList", "Edit exclusion list"));
        
        UiUtilities.registerForClosing(new WindowClosingIf() {
          public void close() {
            exclusionListDlg.dispose();
          }

          public JRootPane getRootPane() {
            return exclusionListDlg.getRootPane();
          }
        });
        
        final ExclusionTablePanel exclusionPanel = new ExclusionTablePanel();
        
        JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
        ok.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            exclusionPanel.saveSettings();
            exclusionListDlg.dispose();
          }
        });
        
        JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
        cancel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            exclusionListDlg.dispose();
          }
        });
        
        FormLayout layout = new FormLayout("0dlu:grow,default,3dlu,default","fill:default:grow,2dlu,default,5dlu,default");
        layout.setColumnGroups(new int[][] {{2,4}});
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(layout, (JPanel) exclusionListDlg
            .getContentPane());
        pb.setDefaultDialogBorder();
        
        pb.add(exclusionPanel, cc.xyw(1,1,4));
        pb.addSeparator("", cc.xyw(1,3,4));
        pb.add(ok, cc.xy(2,5));
        pb.add(cancel, cc.xy(4,5));
        
        layoutWindow("exclusionListDlg", exclusionListDlg, new Dimension(600,
            450));
        exclusionListDlg.setVisible(true);
      }
    });
    
    ContextMenuAction undo = new ContextMenuAction(mLocalizer.msg("undoLastExclusion","Undo last exclusion"), createImageIcon("action","edit-undo",16));
    undo.putValue(Plugin.BIG_ICON, createImageIcon("actions","edit-undo",22));
    undo.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(mLastEnteredExclusionString.length() > 0) {
          for(int i = mSearchList.size()-1; i >= 0; i--) {
            if(mSearchList.get(i).getSearchText().equals(mLastEnteredExclusionString)) {
              mSearchList.remove(i);
            }
          }
          
          mLastEnteredExclusionString = "";
          
          updateFilter(true);
        }
      }
    });
    
    return new ActionMenu(baseAction,new Action[] {openExclusionList,undo});
  }
  
  public ActionMenu getContextMenuActions(final Program p) {
    if (p == null) {
      return null;
    }
    // check if this program is already hidden
    final int index = getSearchTextIndexForProgram(p);
    
    // return menu to hide the program
    if (index == -1 || p.equals(getPluginManager().getExampleProgram())) {
      AbstractAction actionDontWant = getActionDontWantToSee(p);

      if (mSimpleMenu) {
        Matcher matcher = PATTERN_TITLE_PART.matcher(p.getTitle());
        if (matcher.matches()) {
          actionDontWant = getActionInputTitle(p);
        }
        actionDontWant.putValue(Action.NAME,mLocalizer.msg("name","I don't want to see!"));
        actionDontWant.putValue(Action.SMALL_ICON,createImageIcon("apps","idontwant2see",16));
        
        return new ActionMenu(actionDontWant);
      }
      else {
        AbstractAction actionInput = getActionInputTitle(p);
        ContextMenuAction baseAction = new ContextMenuAction(mLocalizer.msg("name","I don't want to see!"),
            createImageIcon("apps","idontwant2see",16));
        
        return new ActionMenu(baseAction, new Action[] {actionDontWant,actionInput});
      }
    }

    // return menu to show the program
    return new ActionMenu(getActionShowAgain(p));
  }

  private ContextMenuAction getActionShowAgain(final Program p) {
    return new ContextMenuAction(mLocalizer
        .msg("menu.reshow", "I want to see!"), createImageIcon("actions",
        "edit-paste", 16)) {
      public void actionPerformed(ActionEvent e) {
        final int index = getSearchTextIndexForProgram(p);
        mSearchList.remove(index);
        updateFilter(!mSwitchToMyFilter);
      }
    };
  }

  private AbstractAction getActionInputTitle(final Program p) {
    return new AbstractAction(mLocalizer.msg("menu.userEntered",
        "User entered value")) {
      public void actionPerformed(ActionEvent e) {
        JCheckBox caseSensitive = new JCheckBox(mLocalizer.msg("caseSensitive",
            "case sensitive"));
        JTextField input = new JTextField(p.getTitle());
        
        input.addAncestorListener(new AncestorListener() {
          public void ancestorAdded(AncestorEvent event) {
            event.getComponent().requestFocus();
          }

          public void ancestorMoved(AncestorEvent event) {}

          public void ancestorRemoved(AncestorEvent event) {}
        });

        if (JOptionPane.showConfirmDialog(UiUtilities
            .getLastModalChildOf(getParentFrame()), new Object[] {
            mLocalizer.msg("exclusionText",
                "What should be excluded? (You can use the wildcard *)"),
            input, caseSensitive }, mLocalizer.msg("exclusionTitle",
            "Exclusion value entering"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
          String test = "";

          if (input.getText() != null) {
            test = input.getText().replaceAll("\\*+", "\\*").trim();

            if (test.length() >= 0 && !test.equals("*")) {
              mSearchList.add(new IDontWant2SeeListEntry(input.getText(),
                  caseSensitive.isSelected()));
              mLastEnteredExclusionString = input.getText();
              updateFilter(!mSwitchToMyFilter);
            }
          }

          if (test.trim().length() <= 1) {
            JOptionPane.showMessageDialog(UiUtilities
                .getLastModalChildOf(getParentFrame()), mLocalizer.msg(
                "notValid", "The entered text is not valid."), Localizer
                .getLocalization(Localizer.I18N_ERROR),
                JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    };
  }

  private AbstractAction getActionDontWantToSee(final Program p) {
    return new AbstractAction(mLocalizer.msg("menu.completeCaseSensitive",
        "Complete title case-sensitive")) {
      public void actionPerformed(ActionEvent e) {
        mSearchList.add(new IDontWant2SeeListEntry(p.getTitle(), true));
        mLastEnteredExclusionString = p.getTitle();
        updateFilter(!mSwitchToMyFilter);
      }
    };
  }
  
  private void updateFilter(final boolean update) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if(!update) {
          getPluginManager().getFilterManager().setCurrentFilter(mFilter);
        }
        else {
          getPluginManager().getFilterManager().setCurrentFilter(getPluginManager().getFilterManager().getCurrentFilter());
        }
      }
    });
    saveMe();
  }
  
  public PluginsProgramFilter[] getAvailableFilter() {
    return new PluginsProgramFilter[] {mFilter};
  }
  
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt(); //read Version
    
    int n = in.readInt();
    
    if(version <= 2) {
      for(int i = 0; i < n; i++) {
        StringBuilder value = new StringBuilder("*");
        value.append(in.readUTF()).append("*");
        
        mSearchList.add(new IDontWant2SeeListEntry(value.toString(),false));
      }
      
      if(version == 2) {
        n = in.readInt();
        
        for(int i = 0; i < n; i++) {
          mSearchList.add(new IDontWant2SeeListEntry(in.readUTF(),true));
        }
        
        mSimpleMenu = false;
      }
    }
    else {
      for(int i = 0; i < n; i++) {
        mSearchList.add(new IDontWant2SeeListEntry(in, version));
      }
      
      mSimpleMenu = in.readBoolean();
      
      if(version >= 4) {
        mSwitchToMyFilter = in.readBoolean();
      }
      if(version >= 5) {
        mLastEnteredExclusionString = in.readUTF();
      }
      if(version >= 6) {
        mLastUsedDate = new Date(in);
      }
    }
  }
  
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(6); //version
    out.writeInt(mSearchList.size());
    
    for(IDontWant2SeeListEntry entry : mSearchList) {
      entry.writeData(out);
    }
    
    out.writeBoolean(mSimpleMenu);
    out.writeBoolean(mSwitchToMyFilter);
    
    out.writeUTF(mLastEnteredExclusionString);
    
    mLastUsedDate.writeData(out);
  }
  
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private JCheckBox mAutoSwitchToMyFilter;
      private JRadioButton mSimpleContextMenu;
      private JRadioButton mCascadedContextMenu;
      private ExclusionTablePanel mExclusionPanel;
      
      public JPanel createSettingsPanel() {
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default,0dlu:grow,default",
            "default,10dlu,default,5dlu,default,5dlu,default,5dlu,fill:default:grow"));        
        
        PanelBuilder pb2 = new PanelBuilder(new FormLayout("default,2dlu,default",
            "default,1dlu,default,default"));
        
        mSimpleContextMenu = new JRadioButton(mLocalizer.msg("settings.menu.simple","Direct in the context menu:"),mSimpleMenu);
        mSimpleContextMenu.setHorizontalTextPosition(JRadioButton.RIGHT);
        
        mCascadedContextMenu = new JRadioButton(mLocalizer.msg("settings.menu.cascaded","In a sub menu:"),!mSimpleMenu);
        mCascadedContextMenu.setHorizontalTextPosition(JRadioButton.RIGHT);
        mCascadedContextMenu.setVerticalAlignment(JRadioButton.TOP);
        
        ButtonGroup bg = new ButtonGroup();
        
        bg.add(mSimpleContextMenu);
        bg.add(mCascadedContextMenu);
        
        pb2.add(mSimpleContextMenu, cc.xy(1,1));
        pb2.addLabel("-" + mLocalizer.msg("name","I don't want to see!") + " (" + mLocalizer.msg("menu.completeCaseSensitive","Instant exclusion with title") + ")", cc.xy(3,1));
        
        pb2.add(mCascadedContextMenu, cc.xy(1,3));
        pb2.addLabel("-" + mLocalizer.msg("menu.completeCaseSensitive","Instant exclusion with title"), cc.xy(3,3));
        pb2.addLabel("-" + mLocalizer.msg("menu.userEntered","User entered value"), cc.xy(3,4));
        
        mAutoSwitchToMyFilter = new JCheckBox(mLocalizer.msg("settings.autoFilter","Automatically activate filter on adding/removing"),mSwitchToMyFilter);
                
        pb.add(mAutoSwitchToMyFilter, cc.xyw(2,1,3));
        pb.addSeparator(mLocalizer.msg("settings.contextMenu","Context menu"), cc.xyw(1,3,4));
        pb.add(pb2.getPanel(), cc.xyw(2,5,3));
        pb.addSeparator(mLocalizer.msg("settings.search","Search"), cc.xyw(1,7,4));
        pb.add(mExclusionPanel = new ExclusionTablePanel(), cc.xyw(2,9,3));
        
        JPanel p = new JPanel(new FormLayout("0dlu,0dlu:grow","5dlu,fill:default:grow"));
        p.add(pb.getPanel(), cc.xy(2,2));
        
        return p;
      }

      public Icon getIcon() {
        return createImageIcon("apps","idontwant2see",16);
      }

      public String getTitle() {
        return null;
      }

      public void saveSettings() {
        mSimpleMenu = mSimpleContextMenu.isSelected();
        mSwitchToMyFilter = mAutoSwitchToMyFilter.isSelected();
        
        mExclusionPanel.saveSettings();
      }      
    };
  }
  
  protected static Date getCurrentDate() {
    return mCurrentDate;
  }
  
  @SuppressWarnings("unchecked")
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
    return (Class<? extends PluginsFilterComponent>[]) new Class[] {IDontWant2SeeFilterComponent.class};
  }
  
  private class ExclusionTablePanel extends JPanel {
    private JTable mTable;
    private IDontWant2SeeSettingsTableModel mTableModel;
    
    protected ExclusionTablePanel() {
      mTableModel = new IDontWant2SeeSettingsTableModel(mSearchList,mLastEnteredExclusionString);
      
      final IDontWant2SeeSettingsTableRenderer renderer = new IDontWant2SeeSettingsTableRenderer(mLastUsedDate);        
      mTable = new JTable(mTableModel);
      mTableModel.setTable(mTable);
      mTable.setRowHeight(25);
      mTable.setPreferredScrollableViewportSize(new Dimension(200,150));
      mTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
      mTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
      mTable.getColumnModel().getColumn(1).setMaxWidth(Locale.getDefault().getLanguage().equals("de") ? Sizes.dialogUnitXAsPixel(80,mTable) : Sizes.dialogUnitXAsPixel(55,mTable));
      mTable.getColumnModel().getColumn(1).setMinWidth(mTable.getColumnModel().getColumn(1).getMaxWidth());
      mTable.getTableHeader().setReorderingAllowed(false);
      mTable.getTableHeader().setResizingAllowed(false);
      
      final JScrollPane scrollPane = new JScrollPane(mTable);
      
      mTable.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          int column = mTable.columnAtPoint(e.getPoint());
          
          if(column == 1) {
            int row = mTable.rowAtPoint(e.getPoint());
            
            mTable.getModel().setValueAt(!((Boolean)mTable.getValueAt(row,column)),row,1);
            mTable.repaint();
          }
        }
      });
      
      mTable.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if(e.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSelectedRows();
            e.consume();
          }
          else if(mTable.getSelectedColumn() == 1 && 
              (e.getKeyCode() == KeyEvent.VK_F2 || e.getKeyCode() == KeyEvent.VK_SPACE)) {
            mTable.getModel().setValueAt(!((Boolean)mTable.getValueAt(mTable.getSelectedRow(),1)),
                mTable.getSelectedRow(),1);
            mTable.repaint();
          }
        }
      });
      
      addAncestorListener(new AncestorListener() {
        public void ancestorAdded(AncestorEvent event) {
          for(int row = 0; row < mTableModel.getRowCount(); row++) {
            if(mTableModel.isLastChangedRow(row)) {
            	Rectangle rect = mTable.getCellRect(row,0,true);
            	rect.setBounds(0,scrollPane.getVisibleRect().height + rect.y - rect.height,0,0);
            	
              mTable.scrollRectToVisible(rect);
              break;
            }
          }
        }

        public void ancestorMoved(AncestorEvent event) {}
        public void ancestorRemoved(AncestorEvent event) {}
      });
      
      JButton add = new JButton(mLocalizer.msg("settings.add","Add entry"),
          createImageIcon("actions","document-new",16));
      add.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          mTableModel.addRow();
          mTable.scrollRectToVisible(mTable.getCellRect(mTableModel.getRowCount()-1,0,true));
        }
      });
      
      final JButton delete = new JButton(mLocalizer.msg("settings.delete",
          "Delete selected entries"),createImageIcon("actions","edit-delete",16));
      delete.setEnabled(false);
      delete.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          deleteSelectedRows();
        }
      });
      
      mTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if(!e.getValueIsAdjusting()) {
            delete.setEnabled(e.getFirstIndex() >= 0);
          }
        }
      });
      
      FormLayout layout = new FormLayout("default,0dlu:grow,default",
          "fill:default:grow,1dlu,default,4dlu,default,5dlu,pref");
      PanelBuilder pb = new PanelBuilder(layout,this);
      CellConstraints cc = new CellConstraints();
      
      int y = 1;
      
      pb.add(scrollPane, cc.xyw(1,y++,3));
      
      PanelBuilder pb2 = new PanelBuilder(
          new FormLayout("default,3dlu:grow,default,3dlu:grow,default,3dlu:grow,default",
              "default"));
      
      ColorLabel blueLabel = new ColorLabel(IDontWant2SeeSettingsTableRenderer.LAST_CHANGED_COLOR);
      blueLabel.setText(mLocalizer.msg("changed","Last change"));
      pb2.add(blueLabel, cc.xy(1,1));
      
      ColorLabel yellowLabel = new ColorLabel(IDontWant2SeeSettingsTableRenderer.LAST_USAGE_7_COLOR);
      yellowLabel.setText(mLocalizer.msg("unusedSince","Not used for {0} days",IDontWant2SeeSettingsTableRenderer.OUTDATED_7_DAY_COUNT));
      pb2.add(yellowLabel, cc.xy(3,1));
      
      ColorLabel orangeLabel = new ColorLabel(IDontWant2SeeSettingsTableRenderer.LAST_USAGE_30_COLOR);
      orangeLabel.setText(mLocalizer.msg("unusedSince","Not used for {0} days",IDontWant2SeeSettingsTableRenderer.OUTDATED_30_DAY_COUNT));
      pb2.add(orangeLabel, cc.xy(5,1));
      
      ColorLabel redLabel = new ColorLabel(IDontWant2SeeSettingsTableRenderer.NOT_VALID_COLOR);
      redLabel.setText(mLocalizer.msg("invalid","Invalid"));
      pb2.add(redLabel, cc.xy(7,1));
      
      pb.add(pb2.getPanel(), cc.xyw(1,++y,3));
      
      y++;
      pb.add(add, cc.xy(1,++y));
      pb.add(delete, cc.xy(3,y++));
      pb.add(UiUtilities.createHelpTextArea(mLocalizer.msg("settings.help",
      "To edit a value double click a cell. You can use wildcard * to search for any text.")), cc.xyw(1,++y,3));
    }
    
    private void deleteSelectedRows() {
      int selectedIndex = mTable.getSelectedRow();
      int[] selection = mTable.getSelectedRows();
      
      for(int i = selection.length-1; i >= 0; i--) {
        mTableModel.deleteRow(selection[i]);
      }
      
      if ((selectedIndex > 0) && (selectedIndex<mTable.getRowCount())) {
        mTable.setRowSelectionInterval(selectedIndex,selectedIndex);
      }
      else if(mTable.getRowCount() > 0) {
        if(mTable.getRowCount() - selectedIndex > 0) {
          mTable.setRowSelectionInterval(0,0);
        }
        else {
          mTable.setRowSelectionInterval(mTable.getRowCount()-1,mTable.getRowCount()-1);
        }
      }
    }
    
    protected void saveSettings() {
      if(mTable.isEditing()) {
        mTable.getCellEditor().stopCellEditing();
      }
      
      mSearchList = mTableModel.getChangedList();
      
      if(mTableModel.getLastChangedValue() != null) {
        mLastEnteredExclusionString = mTableModel.getLastChangedValue();
      }
      
      updateFilter(true);
    }
  }

  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return new ProgramReceiveTarget[] { new ProgramReceiveTarget(this,
        mLocalizer.msg("programTarget", "Exclude programs"),
        RECEIVE_TARGET_EXCLUDE_EXACT) };
  }

  @Override
  public boolean receivePrograms(Program[] programArr,
      ProgramReceiveTarget receiveTarget) {
    if (receiveTarget.getTargetId().equals(RECEIVE_TARGET_EXCLUDE_EXACT)) {
      if (programArr.length > 0) {
        for (Program program : programArr) {
          if (getSearchTextIndexForProgram(program) == -1) {
            mSearchList
                .add(new IDontWant2SeeListEntry(program.getTitle(), true));
            mLastEnteredExclusionString = program.getTitle();
          }
        }
        updateFilter(!mSwitchToMyFilter);
      }
      return true;
    }
    return false;
  }
  
}
