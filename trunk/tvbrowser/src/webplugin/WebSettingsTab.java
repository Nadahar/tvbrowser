/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package webplugin;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.ui.DragAndDropMouseListener;
import util.ui.ListDragAndDropHandler;
import util.ui.ListDropAction;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * The Settings-Tab
 */
public class WebSettingsTab implements SettingsTab,  ListDropAction {
  /** Localizer */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(WebSettingsTab.class);

  /** The original List */
  private ArrayList<WebAddress> mOriginal;

  /** Work-List */
  private DefaultListModel mListModel;

  /** JList */
  private JList mAddressList;

  /** A few Buttons */
  private JButton mStartStop;

  private JButton mNew;

  private JButton mEdit;

  private JButton mDelete;

  private JButton mUp;

  private JButton mDown;
  
  private JButton mResetIcons;

  /** Start-Icon */
  private Icon mStartIcon = WebPlugin.getInstance().createImageIcon("actions", "view-refresh", 16);

  /** Stop-Icon */
  private Icon mStopIcon = WebPlugin.getInstance().createImageIcon("actions", "process-stop", 16);

  /** Parent */
  private JFrame mParent;

  /**
   * Create the Tab
   * 
   * @param frame Parent-Frame
   * @param addresses List of Addresses
   */
  public WebSettingsTab(JFrame frame, ArrayList<WebAddress> addresses) {
    mParent = frame;
    mOriginal = addresses;

    mListModel = new DefaultListModel();

    for (int i = 0; i < mOriginal.size(); i++) {
      mListModel.addElement(((WebAddress) mOriginal.get(i)).clone());
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#createSettingsPanel()
   */
  public JPanel createSettingsPanel() {

    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,fill:default:grow", "5dlu,pref,fill:default:grow"));
    CellConstraints cc = new CellConstraints();

    JPanel panel = new JPanel(new GridBagLayout());

    mAddressList = new JList(mListModel);
    // Register DnD on the List.
    ListDragAndDropHandler dnDHandler = new ListDragAndDropHandler(mAddressList,mAddressList,this);    
    new DragAndDropMouseListener(mAddressList,mAddressList,this,dnDHandler);

    mAddressList.setSelectedIndex(0);
    mAddressList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mAddressList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        listSelectionChanged();
      }
    });
    mAddressList.addMouseListener(new MouseAdapter() {

      public void mouseClicked(MouseEvent e) {
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
          editPressed();
        }
        super.mouseClicked(e);
      }
    });

    mAddressList.setCellRenderer(new WebAddressRenderer());

    GridBagConstraints c = new GridBagConstraints();

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.insets = new Insets(0, 0, 5, 5);

    panel.add(new JScrollPane(mAddressList), c);

    c = new GridBagConstraints();
    c.insets = new Insets(5, 0, 5, 5);
    c.weightx = 0;
    c.weighty = 0;
    c.fill = GridBagConstraints.NONE;

    createButtons();

    panel.add(mStartStop, c);
    c.insets = new Insets(5, 0, 5, 5);
    panel.add(mNew, c);
    panel.add(mEdit, c);
    panel.add(mDelete, c);
    panel.add(mResetIcons, c);
    
    GridBagConstraints filler = new GridBagConstraints();

    filler.weightx = 1.0;
    filler.fill = GridBagConstraints.HORIZONTAL;

    panel.add(new JPanel(), filler);

    panel.add(mUp, c);

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(5, 0, 5, 5);

    panel.add(mDown, c);

    listSelectionChanged();

    pb.addLabel(mLocalizer.msg("WebPages", "Web Pages") + ":", cc.xy(2, 2));
    pb.add(panel, cc.xy(2, 3));

    return pb.getPanel();
  }

  /**
   * Create the Buttons
   */
  private void createButtons() {
    mStartStop = new JButton(mStartIcon);
    mStartStop.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        WebAddress adr = (WebAddress) mAddressList.getSelectedValue();
        adr.setActive(!adr.isActive());
        mAddressList.updateUI();
        listSelectionChanged();
      }

    });

    mNew = new JButton(WebPlugin.getInstance().createImageIcon("actions", "document-new", 16));
    mNew.setToolTipText(mLocalizer.msg("New", "Add a new Site"));
    mNew.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        newPressed();
      }
    });

    mEdit = new JButton(WebPlugin.getInstance().createImageIcon("actions", "document-edit", 16));
    mEdit.setToolTipText(mLocalizer.msg("Edit", "Edit Site"));

    mEdit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        editPressed();
      }
    });

    mDelete = new JButton(WebPlugin.getInstance().createImageIcon("actions", "edit-delete", 16));
    mDelete.setToolTipText(mLocalizer.msg("DeleteSite", "Delete Site"));

    mDelete.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        deletePressed();
      }

    });

    mUp = new JButton(WebPlugin.getInstance().createImageIcon("actions", "go-up", 16));
    mUp.setToolTipText(mLocalizer.msg("Up", "Move selected Site up"));

    mUp.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        moveSelectedItem(-1);
      }

    });

    mDown = new JButton(WebPlugin.getInstance().createImageIcon("actions", "go-down", 16));
    mDown.setToolTipText(mLocalizer.msg("Down", "Move selected Site down"));

    mDown.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        moveSelectedItem(1);
      }

    });
    
    mResetIcons = new JButton(WebPlugin.getInstance().createImageIcon("apps", "system-software-update", 16));
    mResetIcons.setToolTipText(mLocalizer.msg("Reload", "Reload Icons on next Update"));

    mResetIcons.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetIcons();
      }

    }); 
  }

  /**
   * Rest the Icons
   */
  protected void resetIcons() {
    int max = mAddressList.getModel().getSize();
    
    for (int i=0;i<max;i++) {
      WebAddress adr = (WebAddress) mAddressList.getModel().getElementAt(i);
      File f = new File(adr.getIconFile());
      if (f.exists())
        f.delete();
      adr.setIconFile(null);
    }
    mAddressList.updateUI();
  }

  /**
   * The Selection was changed
   */
  private void listSelectionChanged() {
    WebAddress adr = (WebAddress) mAddressList.getSelectedValue();

    if (adr == null) {
      mStartStop.setIcon(mStartIcon);
      mStartStop.setToolTipText(mLocalizer.msg("Enable", "Enable Site"));
      mStartStop.setEnabled(false);
      mEdit.setEnabled(false);
      mDelete.setEnabled(false);
      mUp.setEnabled(false);
      mDown.setEnabled(false);
      return;
    }

    mStartStop.setEnabled(true);
    if (!adr.isActive()) {
      mStartStop.setIcon(mStartIcon);
      mStartStop.setToolTipText(mLocalizer.msg("Enable", "Enable Site"));
    } else {
      mStartStop.setIcon(mStopIcon);
      mStartStop.setToolTipText(mLocalizer.msg("Disable", "Disable Site"));
    }

    mEdit.setEnabled(adr.isUserEntry());
    mDelete.setEnabled(adr.isUserEntry());

    mUp.setEnabled(mAddressList.getSelectedIndex() != 0);
    mDown.setEnabled(mAddressList.getSelectedIndex() < mListModel.size() - 1);

  }

  /**
   * Move a selected Item #rows
   * 
   * @param rows Rows to move the selected Item
   */
  private void moveSelectedItem(int rows) {

    int selected = mAddressList.getSelectedIndex();

    WebAddress adr = (WebAddress) mAddressList.getSelectedValue();

    DefaultListModel model = (DefaultListModel) mAddressList.getModel();
    model.removeElement(adr);
    model.add(selected + rows, adr);

    mAddressList.setSelectedValue(adr, true);
  }

  /**
   * Delete was pressed
   */
  private void deletePressed() {

    int result = JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(mParent), mLocalizer.msg(
        "DeleteQuesiton", "Delete selected Item?"), Localizer.getLocalization(Localizer.I18N_DELETE)+"?", JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);

    if (result != JOptionPane.YES_OPTION) {
      return;
    }

    int num = mAddressList.getSelectedIndex();

    mListModel.removeElementAt(num);

    num--;

    if (num >= mListModel.size() - 1) {
      num = mListModel.size() - 1;
    }

    mAddressList.setSelectedIndex(num);

  }

  /**
   * New was pressed
   */
  private void newPressed() {
    WebAddress newadr = new WebAddress("", null, null, true, true);

    WebAddressEditDialog editor;

    Window win = UiUtilities.getLastModalChildOf(mParent);

    if (win instanceof JDialog) {
      editor = new WebAddressEditDialog((JDialog) win, newadr);
    } else {
      editor = new WebAddressEditDialog((JFrame) win, newadr);
    }

    UiUtilities.centerAndShow(editor);

    if (editor.getReturnValue() == JOptionPane.OK_OPTION) {
      mListModel.addElement(newadr);
      mAddressList.setSelectedIndex(mListModel.size() - 1);

    }

  }

  /**
   * Edit was pressed
   */
  private void editPressed() {
    WebAddress seladr = (WebAddress) mAddressList.getSelectedValue();

    if (!seladr.isUserEntry()) {
      return;
    }

    WebAddressEditDialog editor;

    Window win = UiUtilities.getLastModalChildOf(mParent);

    if (win instanceof JDialog) {
      editor = new WebAddressEditDialog((JDialog) win, seladr);
    } else {
      editor = new WebAddressEditDialog((JFrame) win, seladr);
    }

    UiUtilities.centerAndShow(editor);

    mAddressList.updateUI();
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#saveSettings()
   */
  public void saveSettings() {
    mOriginal.clear();
    
    for (Object o : mListModel.toArray()) {
      mOriginal.add((WebAddress) o);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#getIcon()
   */
  public Icon getIcon() {
    return WebPlugin.getInstance().createImageIcon("actions", "web-search", 16);
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#getTitle()
   */
  public String getTitle() {
    return mLocalizer.msg("WebPlugin", "WebPlugin");
  }

  public void drop(JList source, JList target, int row, boolean move) {
    UiUtilities.moveSelectedItems(target,row,true);
  }

}