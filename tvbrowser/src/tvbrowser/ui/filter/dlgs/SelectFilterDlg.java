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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.Settings;
import tvbrowser.core.filters.AudioDescriptionFilter;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.filters.PluginFilter;
import tvbrowser.core.filters.SeparatorFilter;
import tvbrowser.core.filters.ShowAllFilter;
import tvbrowser.core.filters.SubtitleFilter;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.ui.mainframe.searchfield.SearchFilter;
import util.ui.DragAndDropMouseListener;
import util.ui.ListDragAndDropHandler;
import util.ui.ListDropAction;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import devplugin.PluginsProgramFilter;
import devplugin.ProgramFilter;

public class SelectFilterDlg extends JDialog implements ActionListener, WindowClosingIf, ListDropAction {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SelectFilterDlg.class);

  private JList mFilterListBox;

  private JFrame mParent;

  private JButton mEditBtn, mRemoveBtn, mNewBtn, mCancelBtn, mOkBtn, mUpBtn, mDownBtn, mSeperator, mDefaultFilterBtn;

  private DefaultListModel mFilterListModel;

  private FilterList mFilterList;
  
  private String mDefaultFilterId;

  public SelectFilterDlg(JFrame parent) {

    super(parent, true);
    
    UiUtilities.registerForClosing(this);

    mFilterList = FilterList.getInstance();
    ProgramFilter defaultFilter = FilterManagerImpl.getInstance().getDefaultFilter();
    mDefaultFilterId = defaultFilter.getClass().getName() + "###" + defaultFilter.getName();
    
   
    mParent = parent;
    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setLayout(new BorderLayout(7, 13));
    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setTitle(mLocalizer.msg("title", "Edit Filters"));

    mFilterListModel = new DefaultListModel();

    ProgramFilter[] filterArr = mFilterList.getFilterArr();
    for (int i = 0; i < filterArr.length; i++) {
      if (!(filterArr[i] instanceof SearchFilter))
        mFilterListModel.addElement(filterArr[i]);
    }

    mFilterListBox = new JList(mFilterListModel);
    mFilterListBox.setCellRenderer(new FilterListCellRenderer());
    
    // Register DnD on the List.
    ListDragAndDropHandler dnDHandler = new ListDragAndDropHandler(mFilterListBox,mFilterListBox,this);    
    new DragAndDropMouseListener(mFilterListBox,mFilterListBox,this,dnDHandler);
    
    mFilterListBox.setVisibleRowCount(5);

    mFilterListBox.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        updateBtns();
      }
    });
    
    mFilterListBox.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
          if(mFilterListBox.getSelectedIndex() != -1 && mEditBtn.isEnabled())
            actionPerformed(new ActionEvent(mEditBtn,ActionEvent.ACTION_PERFORMED, mEditBtn.getActionCommand()));
        }
      }
    });
    
    JPanel btnPanel = new JPanel(new BorderLayout());
    JPanel panel1 = new JPanel(new GridLayout(0, 1, 0, 7));
    mNewBtn = new JButton(mLocalizer.msg("newButton", "new"));
    mEditBtn = new JButton(Localizer.getEllipsisLocalization(Localizer.I18N_EDIT));
    mRemoveBtn = new JButton(Localizer.getLocalization(Localizer.I18N_DELETE));
    mSeperator = new JButton(mLocalizer.msg("seperatorButton", "seperator"));
    mDefaultFilterBtn = new JButton(Localizer.getLocalization(Localizer.I18N_STANDARD));
    mDefaultFilterBtn.setEnabled(false);
    
    mNewBtn.addActionListener(this);
    mEditBtn.addActionListener(this);
    mRemoveBtn.addActionListener(this);
    mSeperator.addActionListener(this);
    mDefaultFilterBtn.addActionListener(this);
    
    panel1.add(mNewBtn);
    panel1.add(mDefaultFilterBtn);
    panel1.add(mEditBtn);
    panel1.add(mRemoveBtn);
    panel1.add(mSeperator);
    btnPanel.add(panel1, BorderLayout.NORTH);

    JPanel panel2 = new JPanel(new GridLayout(0, 1, 0, 7));
    mUpBtn = new JButton(TVBrowserIcons.up(TVBrowserIcons.SIZE_SMALL));
    mDownBtn = new JButton(TVBrowserIcons.down(TVBrowserIcons.SIZE_SMALL));
    mUpBtn.addActionListener(this);
    mDownBtn.addActionListener(this);
    panel2.add(mUpBtn);
    panel2.add(mDownBtn);

    btnPanel.add(panel2, BorderLayout.SOUTH);

    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));

    mOkBtn = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    buttonPn.add(mOkBtn);
    mOkBtn.addActionListener(this);
    getRootPane().setDefaultButton(mOkBtn);

    mCancelBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCancelBtn.addActionListener(this);
    buttonPn.add(mCancelBtn);

    JTextArea ta = UiUtilities.createHelpTextArea(mLocalizer.msg("hint", "Choose a filter to edit or create a new one."));

    contentPane.add(new JScrollPane(mFilterListBox), BorderLayout.CENTER);
    contentPane.add(btnPanel, BorderLayout.EAST);
    contentPane.add(buttonPn, BorderLayout.SOUTH);
    contentPane.add(ta, BorderLayout.NORTH);

    updateBtns();
    Settings.layoutWindow("selectFilterDlg", this, new Dimension(350,400));
  }

  public void updateBtns() {

    Object item = mFilterListBox.getSelectedValue();
    
    mEditBtn
        .setEnabled(item != null
            && !(item instanceof ShowAllFilter || item instanceof PluginFilter || item instanceof SubtitleFilter || item instanceof SeparatorFilter
                || item instanceof PluginsProgramFilter || item instanceof AudioDescriptionFilter));
    mRemoveBtn.setEnabled(item != null
        && !(item instanceof ShowAllFilter || item instanceof PluginFilter || item instanceof SubtitleFilter ||
            item instanceof PluginsProgramFilter || item instanceof AudioDescriptionFilter));

    int inx = mFilterListBox.getSelectedIndex();
    mUpBtn.setEnabled(inx > 0);
    mDownBtn.setEnabled(inx >= 0 && inx < mFilterListModel.getSize() - 1);
    
    Object filter = mFilterListBox.getSelectedValue();
    
    if(filter != null) {
      mDefaultFilterBtn.setEnabled(!(mFilterListBox.getSelectedValue() instanceof SeparatorFilter) && ((!mDefaultFilterId.equals(filter.getClass().getName() + "###" + ((ProgramFilter)filter).getName())) || mDefaultFilterId.trim().length() < 1 && filter instanceof ShowAllFilter));
    }
  }

  public FilterList getFilterList() {
    return mFilterList;
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == mNewBtn) {
      EditFilterDlg dlg = new EditFilterDlg(mParent, mFilterList, null);
      UserFilter filter = dlg.getUserFilter();
      if (filter != null) {
        mFilterListModel.addElement(filter);
      }
    } else if (e.getSource() == mEditBtn) {
      ProgramFilter filter = (ProgramFilter) mFilterListBox.getSelectedValue();
      if (filter instanceof UserFilter) {
        new EditFilterDlg(mParent, mFilterList, (UserFilter) filter);
      }
    } else if (e.getSource() == mRemoveBtn) {
      mFilterListModel.removeElement(mFilterListBox.getSelectedValue());
      mFilterList.remove((ProgramFilter) mFilterListBox.getSelectedValue());
      updateBtns();
    } else if (e.getSource() == mUpBtn) {
      UiUtilities.moveSelectedItems(mFilterListBox,mFilterListBox.getSelectedIndex()-1,true);
    } else if (e.getSource() == mDownBtn) {
      UiUtilities.moveSelectedItems(mFilterListBox,mFilterListBox.getSelectedIndex()+ mFilterListBox.getSelectedIndices().length + 1,true);
    } else if (e.getSource() == mOkBtn) {
      Object[] o = mFilterListModel.toArray();
      ProgramFilter[] filters = new ProgramFilter[o.length];
      for (int i = 0; i < o.length; i++) {
        filters[i] = (ProgramFilter) o[i];
      }
      mFilterList.setProgramFilterArr(filters);

      mFilterList.store();
      
      Settings.propDefaultFilter.setString(mDefaultFilterId);
      FilterManagerImpl.getInstance().setCurrentFilter(FilterManagerImpl.getInstance().getCurrentFilter());
      
      setVisible(false);
    } else if (e.getSource() == mCancelBtn) {
      close();
    } else if (e.getSource() == mSeperator) {
      mFilterListModel.addElement(new SeparatorFilter());
    } else if (e.getSource() == mDefaultFilterBtn) {
      ProgramFilter filter = ((ProgramFilter)mFilterListBox.getSelectedValue());      
      mDefaultFilterId = filter.getClass().getName() + "###" + filter.getName();
      mFilterListBox.repaint();
      updateBtns();
    }

  }

  public void close() {
    mFilterList.create();
    setVisible(false);
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
}