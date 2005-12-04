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
package clipboardplugin;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.ui.ImageUtilities;
import util.ui.ProgramList;
import util.ui.SendToPluginDialog;
import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.PluginTreeNode;
import devplugin.Program;

/**
 * Creates a Dialog with a List of Programs
 * 
 * @author bodo
 */
public class ClipboardDialog extends JDialog {

  /** The localizer used by this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ClipboardDialog.class);

  /** JList component */
  private ProgramList mProgramJList;

  /** ParentFrame */
  private Frame mFrame;

  /** Vector with Programs */
  private Vector mClipList;

  /** Plugin that called this Dialog */
  private Plugin mPlugin;

  /** Settings for the Plugin */
  private Properties mSettings;

  /** The Tree-Node */
  private PluginTreeNode mNode;
  
  /**
   * Creates the Dialog
   * 
   * @param frame Frame for modal
   * @param plugin Plugin for reference
   * @param node PluginTreeNode that contains all Programs
   */
  public ClipboardDialog(Frame frame, Plugin plugin, Properties settings, PluginTreeNode node) {
    super(frame, true);
    mSettings = settings;
    mNode = node;
    setTitle(mLocalizer.msg("viewList", "View List:"));
    
    Program[] programs = node.getPrograms();
    
    mClipList = new Vector();
    for (int i = 0; i < programs.length; i++) {
      mClipList.add(programs[i]);
    }
    mFrame = frame;
    mPlugin = plugin;
    createGUI();
  }

  /**
   * Returns the current Time in minutes
   * 
   * @return Time in minutes
   */
  private int getCurrentTime() {
    Calendar cal = Calendar.getInstance();
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }

  /**
   * Creates the GUI
   */
  private void createGUI() {
    JPanel content = (JPanel) this.getContentPane();
    content.setLayout(new BorderLayout());
    content.setBorder(UiUtilities.DIALOG_BORDER);

    mProgramJList = new ProgramList(mClipList);
    mProgramJList.addMouseListeners(mPlugin);

    JScrollPane scroll = new JScrollPane(mProgramJList);

    content.add(scroll, BorderLayout.CENTER);

    JPanel buttonRight = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(0, 5, 5, 0);
    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = GridBagConstraints.REMAINDER;

    JButton upButton = new JButton(ClipboardPlugin.getPluginManager().getIconFromTheme(mPlugin, "actions", "go-up", 16));
    upButton.setToolTipText(mLocalizer.msg("up", "Moves the selected program up"));
    upButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        upItems();
      }
    });
    buttonRight.add(upButton, c);

    JButton downButton = new JButton(ClipboardPlugin.getPluginManager().getIconFromTheme(mPlugin, "actions", "go-down", 16));
    downButton.setToolTipText(mLocalizer.msg("down", "Moves the selected program down"));
    downButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        downItems();
      }
    });
    buttonRight.add(downButton, c);

    c.weighty = 1.0;

    JButton deleteButton = new JButton(ImageUtilities
        .createImageIconFromJar("clipboardplugin/Delete16.gif", getClass()));
    deleteButton.setToolTipText(mLocalizer.msg("delete", "Deletes the selected program"));

    deleteButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        deleteItems();
      }
    });

    buttonRight.add(deleteButton, c);

    content.add(buttonRight, BorderLayout.EAST);

    JButton sendButton = new JButton();

    sendButton.setToolTipText(mLocalizer.msg("send", "Send Program to another Plugin"));
    sendButton.setIcon(new ImageIcon("imgs/SendToPlugin.png"));
    sendButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        showSendDialog();
      }

    });

    CellConstraints cc = new CellConstraints();
    JPanel buttonPn = new JPanel(new FormLayout("default, 3dlu, default, fill:default:grow, default", "fill:default"));
    buttonPn.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

    content.add(buttonPn, BorderLayout.SOUTH);

    JButton copyToSystemBtn = new JButton();
    copyToSystemBtn.setIcon(ImageUtilities
        .createImageIconFromJar("clipboardplugin/clipboard.png", getClass()));
    copyToSystemBtn.setToolTipText(mLocalizer.msg("toSystem", "To System-Clipbord"));
    
    copyToSystemBtn.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        ((ClipboardPlugin)mPlugin).copyProgramsToSystem((Program[])mClipList.toArray(new Program[0]));
      }

    });

    buttonPn.add(sendButton, cc.xy(1,1));
    buttonPn.add(copyToSystemBtn, cc.xy(3,1));

    JButton closeButton = new JButton(mLocalizer.msg("close", "Close"));
    closeButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        dispose();
      }
    });

    buttonPn.add(closeButton, cc.xy(5,1));
    getRootPane().setDefaultButton(closeButton);

  }

  /**
   * Move Item Down
   */
  protected void downItems() {
    moveSelectedItems(1);
  }

  /**
   * Move Item Up
   */
  protected void upItems() {
    moveSelectedItems(-1);
  }

  /**
   * Moves the selected Items
   * 
   * @param nrRows # Rows to Move
   */
  protected void moveSelectedItems(int nrRows) {
    int[] selection = mProgramJList.getSelectedIndices();
    if (selection.length == 0)
      return;

    for (int i = 0; i < selection.length; i++) {
      if (selection[i] >= mClipList.size()) {
        mProgramJList.setSelectedIndex(-1);
        return;
      }
    }

    int insertPos = selection[0] + nrRows;
    if (insertPos < 0) {
      insertPos = 0;
    }
    if (insertPos > mClipList.size() - selection.length) {
      insertPos = mClipList.size() - selection.length;
    }

    // Markierte Zeilen merken und entfernen
    Object[] selectedRows = new Object[selection.length];
    for (int i = selectedRows.length - 1; i >= 0; i--) {
      selectedRows[i] = mClipList.elementAt(selection[i]);
      mClipList.removeElementAt(selection[i]);
    }

    // Zeilen wieder einfï¿½gen
    for (int i = 0; i < selectedRows.length; i++) {
      mClipList.insertElementAt(selectedRows[i], insertPos + i);
    }

    // Zeilen markieren
    mProgramJList.getSelectionModel().setSelectionInterval(insertPos, insertPos + selection.length - 1);

    // Scrollen
    int scrollPos = insertPos;
    if (nrRows > 0)
      scrollPos += selection.length;
    mProgramJList.ensureIndexIsVisible(scrollPos);
    mProgramJList.updateUI();
  }

  /**
   * Delete-Button was pressed.
   */
  protected void deleteItems() {
    if ((mClipList.size() == 0) || (mProgramJList.getSelectedIndex() >= mClipList.size())) {
      return;
    }

    Object[] obj = mProgramJList.getSelectedValues();
    for (int i = 0; i < obj.length; i++) {
      mClipList.remove(obj[i]);
      mNode.removeProgram((Program) obj[i]);
      Program prg = (Program) obj[i];
      prg.unmark(mPlugin);
    }

    if (mClipList.size() == 0) {
      setVisible(false);
    }

    mNode.update();
    mProgramJList.updateUI();
  }

  /**
   * Shows the Send-Dialog
   */
  private void showSendDialog() {
    Program[] prgList = new Program[mClipList.size()];

    for (int i = 0; i < mClipList.size(); i++) {
      prgList[i] = (Program) mClipList.get(i);
    }

    SendToPluginDialog send = new SendToPluginDialog(mPlugin, this, prgList);

    send.setVisible(true);
  }

}