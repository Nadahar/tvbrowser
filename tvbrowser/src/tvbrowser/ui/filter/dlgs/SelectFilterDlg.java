/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package tvbrowser.ui.filter.dlgs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListCellRenderer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.tree.TreePath;

import tvbrowser.core.Settings;
import tvbrowser.core.filters.AudioDescriptionFilter;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.filters.PluginFilter;
import tvbrowser.core.filters.SeparatorFilter;
import tvbrowser.core.filters.ShowAllFilter;
import tvbrowser.core.filters.SubtitleFilter;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.ListDropAction;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.PluginsProgramFilter;
import devplugin.ProgramFilter;

public class SelectFilterDlg extends JDialog implements ActionListener, WindowClosingIf, ListDropAction {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SelectFilterDlg.class);

  private static SelectFilterDlg mInstance;

  private JButton mHelpBtn, mNewFolder, mEditBtn, mRemoveBtn, mNewBtn, mCancelBtn, mOkBtn, mUpBtn, mDownBtn, mSeperator, mDefaultFilterBtn;


  private FilterList mFilterList;

  private String mDefaultFilterId;
  FilterTree mFilterTree;
  
  public static SelectFilterDlg create(JFrame parent) {
    if(mInstance == null) {
      new SelectFilterDlg(parent);
    }
    
    return mInstance;
  }
  
  static SelectFilterDlg getInstance() {
    return mInstance;
  }
  
  private SelectFilterDlg(JFrame parent) {
    super(parent, mLocalizer.msg("title", "Edit Filters"), true);
    mInstance = this;
    
    UiUtilities.registerForClosing(this);

    mFilterList = FilterList.getInstance();
    ProgramFilter defaultFilter = FilterManagerImpl.getInstance().getDefaultFilter();
    mDefaultFilterId = defaultFilter.getClass().getName() + "###" + defaultFilter.getName();

    FormLayout layout = new FormLayout("default,default:grow,default","default,4dlu,fill:default:grow,5dlu,default");
    
    PanelBuilder pb = new PanelBuilder(layout, (JPanel)getContentPane());
    pb.setDefaultDialogBorder();
    
    mFilterTree = new FilterTree();
    
    mNewBtn = UiUtilities.createToolBarButton(FilterTree.mLocalizer.msg("newFilter", "New Filter"),TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_LARGE));
    mNewFolder = UiUtilities.createToolBarButton(FilterTree.mLocalizer.msg("newFolder", "New folder"),IconLoader.getInstance().getIconFromTheme("actions", "folder-new", 22));
    mEditBtn = UiUtilities.createToolBarButton(Localizer.getEllipsisLocalization(Localizer.I18N_EDIT),TVBrowserIcons.edit(TVBrowserIcons.SIZE_LARGE));
    mRemoveBtn = UiUtilities.createToolBarButton(Localizer.getLocalization(Localizer.I18N_DELETE),TVBrowserIcons.delete(TVBrowserIcons.SIZE_LARGE));
    mSeperator = UiUtilities.createToolBarButton(FilterTree.mLocalizer.msg("newSeparator", "Add separator"),IconLoader.getInstance().getIconFromTheme("emblems", "separator", 22));
    mDefaultFilterBtn = UiUtilities.createToolBarButton(Localizer.getLocalization(Localizer.I18N_STANDARD),IconLoader.getInstance().getIconFromTheme("actions", "view-filter", 22));
    mUpBtn = UiUtilities.createToolBarButton(mLocalizer.msg("up","Move selected value up"),TVBrowserIcons.up(TVBrowserIcons.SIZE_LARGE));
    mDownBtn = UiUtilities.createToolBarButton(mLocalizer.msg("down","Move selected value down"),TVBrowserIcons.down(TVBrowserIcons.SIZE_LARGE));
    
    JToolBar toolbarPn = new JToolBar();
    toolbarPn.setBorder(BorderFactory.createEmptyBorder());
    toolbarPn.setFloatable(false);
    
    toolbarPn.add(mNewFolder);
    addToolbarSeperator(toolbarPn);
    toolbarPn.add(mNewBtn);
    toolbarPn.add(mEditBtn);
    toolbarPn.add(mSeperator);
    toolbarPn.add(mRemoveBtn);
    addToolbarSeperator(toolbarPn);
    toolbarPn.add(mDefaultFilterBtn);
    addToolbarSeperator(toolbarPn);
    toolbarPn.add(mUpBtn);
    toolbarPn.add(mDownBtn);

    mNewBtn.addActionListener(this);
    mEditBtn.addActionListener(this);
    mRemoveBtn.addActionListener(this);
    mSeperator.addActionListener(this);
    mDefaultFilterBtn.addActionListener(this);
    mUpBtn.addActionListener(this);
    mDownBtn.addActionListener(this);
    mNewFolder.addActionListener(this);
    
    mHelpBtn = Utilities.createHelpButton();
    mOkBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    mOkBtn.addActionListener(this);
    getRootPane().setDefaultButton(mOkBtn);
    layout.setColumnGroups(new int[][] {{1,3}});

    JScrollPane scrollPane = new JScrollPane(mFilterTree);
    pb.add(scrollPane, CC.xyw(1,3,3));
    pb.add(mHelpBtn, CC.xy(1,5));
    pb.add(mOkBtn, CC.xy(3,5));
    pb.add(toolbarPn, CC.xyw(1,1,3)); 
    
    updateBtns();
    Settings.layoutWindow("selectFilterDlg", this, new Dimension(500,500));
  }

  private void addToolbarSeperator(JToolBar toolbarPn) {
    JPanel p = new JPanel();
    p.setSize(10,10);
    p.setMaximumSize(new Dimension(10,10));
    toolbarPn.add(p);
    toolbarPn.addSeparator();

    p = new JPanel();
    p.setSize(4,10);
    p.setMaximumSize(new Dimension(4,10));
    toolbarPn.add(p);
  }
  
  public void updateBtns() {
    if(mFilterTree.getSelectionCount() > 0) {
      FilterNode node = (FilterNode)mFilterTree.getSelectionPath().getLastPathComponent();
      
      int row = mFilterTree.getRowForPath(mFilterTree.getSelectionPath());
      
      mUpBtn.setEnabled(row > 1);
      mDownBtn.setEnabled(row > 0 && row != mFilterTree.getRowCount()-1);
      
      if(node.containsFilter()) {
        String id = node.getFilter().getClass().getName();
        String name = node.getFilter().getName();
        
        mDefaultFilterBtn.setEnabled(!((Settings.propDefaultFilter.getString().equals(id + "###" + name)) ||
            (Settings.propDefaultFilter.getString().trim().length() < 1 && node.getFilter() instanceof ShowAllFilter)));
        
        mEditBtn.setEnabled(!(node.getFilter() instanceof ShowAllFilter || node.getFilter() instanceof PluginFilter || node.getFilter() instanceof SubtitleFilter || node.getFilter() instanceof AudioDescriptionFilter || node.getFilter() instanceof PluginsProgramFilter));
      }
      else {
        mEditBtn.setEnabled(row > 0 && node.isDirectoryNode());
        mDefaultFilterBtn.setEnabled(false);
      }
      
      mRemoveBtn.setEnabled(row > 0 && node.isDeletingAllowed());
      
    }
    else {
      mUpBtn.setEnabled(false);
      mDownBtn.setEnabled(false);
      mDefaultFilterBtn.setEnabled(false);
      mRemoveBtn.setEnabled(false);
    }
  }

  public FilterList getFilterList() {
    return mFilterList;
  }

  public void actionPerformed(ActionEvent e) {try{
    if(e.getSource() == mOkBtn) {
      close();
    }
    else {
      TreePath path1 = null;
      int[] selectionRows = mFilterTree.getSelectionRows();
      if (selectionRows != null && selectionRows.length > 0) {
        path1 = mFilterTree.getPathForRow(selectionRows[0]);
      }
  
      if(path1 == null) {
        path1 = new TreePath(mFilterTree.getRoot());
      }
  
      final TreePath path = path1;
      final FilterNode last = (FilterNode)path.getLastPathComponent();
  
      mFilterTree.setSelectionPath(path);
      
      if (e.getSource() == mNewBtn) {
        createNewFilter(last);
      } else if (e.getSource() == mEditBtn) {
        editSelectedFilter(last);
      } else if (e.getSource() == mRemoveBtn) {
        deleteSelectedItem(last);
      } else if (e.getSource() == mUpBtn) {
        mFilterTree.moveSelectedFilter(-1);
        updateBtns();
      } else if (e.getSource() == mDownBtn) {
        mFilterTree.moveSelectedFilter(1);
        updateBtns();
      } else if (e.getSource() == mSeperator) {
        addSeparator(last);
      } else if (e.getSource() == mDefaultFilterBtn) {
        setDefaultFilter(last);
      } else if (e.getSource() == mNewFolder) {
        createNewFolder(last);
      }
    }
    }catch(Throwable t) {t.printStackTrace();}
  
  }

  public void close() {
    mFilterList.store();
    FilterManagerImpl.getInstance().setCurrentFilter(FilterManagerImpl.getInstance().getCurrentFilter());
    setVisible(false);
    mInstance = null;
  }

  public void drop(JList source, JList target, int rows, boolean move) {
    UiUtilities.moveSelectedItems(target,rows,true);
  }

  private class FilterListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel tc = (JLabel)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);

      if(value instanceof ProgramFilter) {
        String id = value.getClass().getName();
        String name = ((ProgramFilter)value).getName();

        if((mDefaultFilterId.equals(id + "###" + name)) ||
            (mDefaultFilterId.length() < 1 && value instanceof ShowAllFilter)) {
          tc.setFont(tc.getFont().deriveFont(Font.BOLD));
        }
      }

      return tc;
    }
  }
  
  void editSelectedFilter(FilterNode node) {
    new EditFilterDlg(this, FilterList.getInstance(), (UserFilter)node.getFilter());
    mFilterTree.updateUI();
    updateBtns();
  }
  
  void deleteSelectedItem(FilterNode node) {
    if(JOptionPane.showConfirmDialog(this,mLocalizer.msg("delete","Do you really want to delete the selected value?"),
        mLocalizer.msg("deleteTitle", "Delete selected value..."),JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      int[] rows = mFilterTree.getSelectionRows();
      
      if(node.isDirectoryNode() && node.getChildCount() < 1) {
        FilterNode parent = (FilterNode)node.getParent();
        parent.remove(node);
        mFilterTree.getModel().reload(parent);
      }
      else if((node.containsFilter() || node.containsSeparator()) && node.isDeletingAllowed()) {
        String id = node.getFilter().getClass().getName();
        String name = node.getFilter().getName();
        
        if((Settings.propDefaultFilter.getString().equals(id + "###" + name)) ||
            (Settings.propDefaultFilter.getString().trim().length() < 1 && node.getFilter() instanceof ShowAllFilter)) {
          Settings.propDefaultFilter.resetToDefault();
          MainFrame.getInstance().setProgramFilter(FilterManagerImpl.getInstance().getAllFilter());
        }
        
        mFilterTree.getModel().removeNodeFromParent(node);
        mFilterTree.updateUI();
        //TODO
      }
      
      if(rows[0] > mFilterTree.getRowCount() - 1) {
        rows[0] = mFilterTree.getRowCount() -1;
      }
      
      mFilterTree.setSelectionRows(rows);
      updateBtns();
    }
  }
  
  void createNewFilter(FilterNode parent) {
    EditFilterDlg dlg = new EditFilterDlg(this, FilterList.getInstance(), null);
    UserFilter filter = dlg.getUserFilter();
    if (filter != null) {
      FilterNode node = new FilterNode(filter);
      int rows[] = mFilterTree.getSelectionRows();
      
      if(parent.equals(mFilterTree.getRoot()) || parent.isDirectoryNode()) {
        parent.add(node);
        mFilterTree.expandPath(new TreePath(parent.getPath()));
      } else {
        ((FilterNode)parent.getParent()).insert(node,parent.getParent().getIndex(parent));
      }
      
      mFilterTree.reload((FilterNode)node.getParent());
      mFilterTree.setSelectionRows(rows);
    }
    
    updateBtns();
  }
  
  void createNewFolder(FilterNode parent) {
    String value = JOptionPane.showInputDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), FilterTree.mLocalizer.msg("folderName","Folder name:"), FilterTree.mLocalizer.msg("newFolder","New folder"));

    if(value != null && value.length() > 0) {
      FilterNode node = new FilterNode(value);

      if(parent.equals(mFilterTree.getRoot()) || parent.isDirectoryNode()) {
        parent.add(node);
        mFilterTree.expandPath(new TreePath(parent.getPath()));
      } else {
        ((FilterNode)parent.getParent()).insert(node,parent.getParent().getIndex(parent));
      }

      mFilterTree.reload((FilterNode)node.getParent());
    }
    updateBtns();
  }
  
  void addSeparator(FilterNode parent) {
    FilterNode node = new FilterNode(new SeparatorFilter());
    int rows[] = mFilterTree.getSelectionRows();
    
    if(parent.equals(mFilterTree.getRoot()) || parent.isDirectoryNode()) {
      parent.add(node);
      mFilterTree.expandPath(new TreePath(parent.getPath()));
    } else {
      ((FilterNode)parent.getParent()).insert(node,parent.getParent().getIndex(parent));
    }
    
    mFilterTree.reload((FilterNode)node.getParent());
    mFilterTree.setSelectionRows(rows);
    
    updateBtns();
  }
  
  void setDefaultFilter(FilterNode node) {
    String defaultFilterId = node.getFilter().getClass().getName() + "###" + node.getFilter().getName();
    Settings.propDefaultFilter.setString(defaultFilterId);
    mFilterTree.updateUI();
  }
}