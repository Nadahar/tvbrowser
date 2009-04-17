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

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.ui.DragAndDropMouseListener;
import util.ui.ListDragAndDropHandler;
import util.ui.ListDropAction;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
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
  private Icon mStartIcon = TVBrowserIcons.refresh(TVBrowserIcons.SIZE_SMALL);

  /** Stop-Icon */
  private Icon mStopIcon = WebPlugin.getInstance().createImageIcon("actions", "process-stop", 16);

  /** Parent */
  private JFrame mParent;

  private JRadioButton mRbShowDetails;

  private JRadioButton mRbShowTitle;

  /**
   * reference to the plugin
   */
  private WebPlugin webPlugin;

  /**
   * Create the Tab
   * 
   * @param frame Parent-Frame
   * @param addresses List of Addresses
   */
  public WebSettingsTab(JFrame frame, ArrayList<WebAddress> addresses, WebPlugin plugin) {
    mParent = frame;
    mOriginal = addresses;

    mListModel = new DefaultListModel();

    for (int i = 0; i < mOriginal.size(); i++) {
      mListModel.addElement((mOriginal.get(i)).clone());
    }

    this.webPlugin = plugin;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#createSettingsPanel()
   */
  public JPanel createSettingsPanel() {

    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,fill:default:grow", "5dlu,pref,2dlu,pref,2dlu,fill:default:grow"));
    CellConstraints cc = new CellConstraints();

    mRbShowDetails = new JRadioButton(mLocalizer.msg("showDetails","Show search menu for title, actors, and other fields"), webPlugin.getShowDetailMenus());
    mRbShowTitle = new JRadioButton(mLocalizer.msg("showTitle","Show title search only"), !webPlugin.getShowDetailMenus());
    
    ButtonGroup bg = new ButtonGroup();
    
    bg.add(mRbShowDetails);
    bg.add(mRbShowTitle);
    PanelBuilder detailsBuilder = new PanelBuilder(new FormLayout("12dlu,default,2dlu,default:grow","pref,2dlu,pref,5dlu"));
    detailsBuilder.add(mRbShowDetails, cc.xyw(1,1,4));
    detailsBuilder.add(mRbShowTitle, cc.xyw(1,3,4));

    pb.add(detailsBuilder.getPanel(), cc.xy(2, 2));
    
    
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

      @Override
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

    JPanel panel = new JPanel(new GridBagLayout());
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

    pb.addSeparator(mLocalizer.msg("WebPages", "Web Pages"), cc.xyw(1, 4, 2));
    pb.add(panel, cc.xy(2, 6));

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
        mAddressList.repaint();
        listSelectionChanged();
      }

    });

    mNew = new JButton(TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
    mNew.setToolTipText(mLocalizer.msg("New", "Add a new Site"));
    mNew.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        newPressed();
      }
    });

    mEdit = new JButton(TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
    mEdit.setToolTipText(mLocalizer.msg("Edit", "Edit Site"));

    mEdit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        editPressed();
      }
    });

    mDelete = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mDelete.setToolTipText(mLocalizer.msg("DeleteSite", "Delete Site"));

    mDelete.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        deletePressed();
      }

    });

    mUp = new JButton(TVBrowserIcons.up(TVBrowserIcons.SIZE_SMALL));
    mUp.setToolTipText(mLocalizer.msg("Up", "Move selected Site up"));

    mUp.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        moveSelectedItem(-1);
      }

    });

    mDown = new JButton(TVBrowserIcons.down(TVBrowserIcons.SIZE_SMALL));
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
      if (f.exists()) {
        try {
          f.delete();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      adr.setIconFile(null);
    }
    mAddressList.repaint();
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

    Window parent = UiUtilities.getLastModalChildOf(mParent);
    WebAddressEditDialog editor = new WebAddressEditDialog(parent, newadr);

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

    Window parent = UiUtilities.getLastModalChildOf(mParent);
    WebAddressEditDialog editor = new WebAddressEditDialog(parent, seladr);
    UiUtilities.centerAndShow(editor);

    mAddressList.repaint();
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
    webPlugin.setShowDetailMenus(mRbShowDetails.isSelected());
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