/*
 * SimpleMarkerPlugin by Ren� Mach
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
 *     $Date: 2006-06-05 21:02:43 +0200 (Mo, 05 Jun 2006) $
 *   $Author: darras $
 * $Revision: 2466 $
 */
package simplemarkerplugin;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;

import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import devplugin.ActionMenu;

import devplugin.ContextMenuAction;

import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * SimpleMarkerPlugin 1.4 Plugin for TV-Browser since version 2.3 to only mark
 * programs and add them to the Plugin tree.
 * 
 * (Formerly known as Just_Mark ;-))
 * 
 * License: GNU General Public License (GPL)
 * 
 * @author Ren� Mach
 */
public class SimpleMarkerPlugin extends Plugin implements ActionListener {

  /** The localizer for this class. */
  protected static util.ui.Localizer mLocalizer;

  private Program mProg = null;

  private static SimpleMarkerPlugin mInstance;

  private MarkListsVector mMarkListVector;

  private Properties mProperties;

  private PluginTreeNode mRootNode = new PluginTreeNode(this, false);

  private boolean mHasRightToUpdate = false, mHasToUpdate = false;
  
  private ManagePanel mProgramsPanel, mProgramsTitlePanel;

  /**
   * Standard contructor for this class.
   */
  public SimpleMarkerPlugin() {
    mInstance = this;
    mLocalizer = util.ui.Localizer.getLocalizerFor(SimpleMarkerPlugin.class);
    mMarkListVector = new MarkListsVector();
  }

  /**
   * @return The instance of this class.
   */
  public static SimpleMarkerPlugin getInstance() {
    return mInstance;
  }

  /** @return The Plugin Info. */
  public PluginInfo getInfo() {
    return (new PluginInfo("SimpleMarkerPlugin", mLocalizer.msg("description",
        "A simple marker plugin (formerly Just_Mark)"), "Ren� Mach", new Version(
        1, 4, true, "1.4"), "GPL"));
  }

  public void loadSettings(Properties prop) {
    if (prop == null)
      mProperties = new Properties();
    else
      mProperties = prop;
  }

  public Properties storeSettings() {
    return mProperties;
  }

  /**
   * @return The MarkIcons.
   */
  public Icon[] getMarkIconsForProgram(Program p) {
    String[] lists = mMarkListVector.getNamesOfListsContainingProgram(p);
    Icon[] icons = new Icon[lists.length];

    for (int i = 0; i < lists.length; i++)
      icons[i] = mMarkListVector.getListForName(lists[i]).getMarkIcon();

    return icons;
  }

  public SettingsTab getSettingsTab() {
    return (new SimpleMarkerPluginSettingsTab());
  }

  /**
   * @return The ActionMenu for this Plugin.
   */
  public ActionMenu getContextMenuActions(Program p) {
    if (!p.equals(getPluginManager().getExampleProgram()))
      if (p.isExpired())
        return null;

    this.mProg = p;

    if (mMarkListVector.size() == 1) {
      // Create context menu entry
      return new ActionMenu(getDefaultAction(p));
    } else {
      ContextMenuAction menu = new ContextMenuAction();
      menu.setText(mLocalizer.msg("list.name", "Simple Marker"));
      menu.setSmallIcon(createImageIcon("actions", "just-mark", 16));

      Action[] submenu = new Action[mMarkListVector.size()];

      for (int i = 0; i < submenu.length; i++)
        submenu[i] = mMarkListVector.getListAt(i).getContextMenuAction(p);

      return new ActionMenu(menu, submenu);
    }
  }

  private ContextMenuAction getDefaultAction(Program p) {
    ContextMenuAction menu = new ContextMenuAction();
    menu.setText(mLocalizer.msg("mark", "Just mark"));
    if (mMarkListVector.getListAt(0).contains(p))
      menu.setText(mLocalizer.msg("unmark", "Just unmark"));
    menu.putValue(Action.ACTION_COMMAND_KEY, menu.getValue(Action.NAME));
    menu.setSmallIcon(createImageIcon("actions", "just-mark", 16));
    menu.setActionListener(this);

    return menu;
  }

  public boolean canReceivePrograms() {
    return true;
  }

  public void receivePrograms(Program[] programs) {
    for (Program p : programs) {
      if (mMarkListVector.getListAt(0).contains(p) || p.isExpired())
        continue;
      else {
        mMarkListVector.getListAt(0).addElement(p);
        p.mark(this);
      }
    }
    mMarkListVector.getListAt(0).updateNode();
  }

  public void handleTvDataUpdateFinished() {
    mHasToUpdate = true;

    if (mHasRightToUpdate) {
      mHasToUpdate = false;

      for (MarkList list : mMarkListVector)
        list.revalidateContainingPrograms();
    }
  }

  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        showProgramsList();
      }
    };
    // Name of the buttons in the menu and the icon bar
    action.putValue(Action.NAME, "Simple Marker");
    // small icon
    action.putValue(Action.SMALL_ICON, createImageIcon("actions", "just-mark",
        16));
    // big icon
    action.putValue(BIG_ICON, createImageIcon("actions", "just-mark", 22));
    return new ActionMenu(action);
  }

  public void handleTvBrowserStartFinished() {
    mHasRightToUpdate = true;
    updateTree();

    if (mHasToUpdate)
      handleTvDataUpdateFinished();
  }

  private void closeDialog(JDialog dialog) {
    dialog.dispose();
    mProgramsPanel = null;
    mProgramsTitlePanel = null;
  }
  
  private void showProgramsList() {
    final JDialog dialog = UiUtilities.createDialog(UiUtilities
        .getBestDialogParent(getParentFrame()), true);
    dialog.setTitle("SimpleMarkerPlugin");
    dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5));

    mProgramsPanel = new ManagePanel(dialog, mMarkListVector, ManagePanel.MANAGE_PROGRAMS);
    mProgramsTitlePanel = new ManagePanel(dialog, mMarkListVector, ManagePanel.MANAGE_PROGRAM_TITLES);
    
    tabbedPane.addTab(mLocalizer.msg("programList","Program list"), mProgramsPanel);
    tabbedPane.addTab(mLocalizer
        .msg("programListUnmarker", "Programs Unmarker"), mProgramsTitlePanel);

    dialog.getContentPane().setLayout(new BorderLayout(0, 0));
    dialog.getContentPane().add(tabbedPane, BorderLayout.CENTER);

    dialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        closeDialog(dialog);
      }
    });
    
    JButton close = new JButton(mLocalizer.msg("close","Close"));
    close.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeDialog(dialog);
      }
    });

    dialog.getRootPane().setDefaultButton(close);

    UiUtilities.registerForClosing(new WindowClosingIf() {
      public void close() {
        closeDialog(dialog);
      }

      public JRootPane getRootPane() {
        return dialog.getRootPane();
      }
    });

    JButton settings = new JButton(createImageIcon("categories",
        "preferences-desktop", 16));
    settings.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
        Plugin.getPluginManager()
            .showSettings(SimpleMarkerPlugin.getInstance());
      }
    });

    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    buttonPanel.add(settings, BorderLayout.WEST);
    buttonPanel.add(close, BorderLayout.EAST);
    dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    try {
      int x = Integer.parseInt(mProperties.getProperty("x"));
      int y = Integer.parseInt(mProperties.getProperty("y"));
      int width = Integer.parseInt(mProperties.getProperty("width"));
      int height = Integer.parseInt(mProperties.getProperty("height"));

      dialog.setBounds(x, y, width, height);
      dialog.setVisible(true);

    } catch (Exception ee) {
      dialog.setSize(434, 330);
      UiUtilities.centerAndShow(dialog);
    }

    mProperties.setProperty("x", String.valueOf(dialog.getX()));
    mProperties.setProperty("y", String.valueOf(dialog.getY()));
    mProperties.setProperty("width", String.valueOf(dialog.getWidth()));
    mProperties.setProperty("height", String.valueOf(dialog.getHeight()));
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(mLocalizer.msg("mark", "Just mark"))) {
      mMarkListVector.getListAt(0).addElement(mProg);
      mProg.mark(this);
      mMarkListVector.getListAt(0).updateNode();
    } else if (e.getActionCommand().equals(
        mLocalizer.msg("unmark", "Just unmark"))) {
      mMarkListVector.getListAt(0).removeElement(mProg);
      mProg.unmark(this);
      mMarkListVector.getListAt(0).updateNode();
    }
    refreshPanels();
  }

  public void readData(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    int version = in.readInt();

    if (version == 1) {
      int size = in.readInt();
      for (int i = 0; i < size; i++)
        mMarkListVector.addElement(new MarkList(in));
    }
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    out.writeInt(mMarkListVector.size());
    for (int i = 0; i < mMarkListVector.size(); i++)
      mMarkListVector.getListAt(i).writeData(out);
  }

  public boolean canUseProgramTree() {
    return true;
  }

  /**
   * Remove all programs of the list
   * 
   * @param node
   *          The parent node that contains the programs
   * @param e
   *          The ActionEvent.
   */
  public void handleAction(PluginTreeNode node, ActionEvent e) {
    if (e.getActionCommand().equals(
        mLocalizer.msg("unmarkall", "Just unmark all"))) {
      Program[] p = node.getPrograms();

      for (int i = 0; i < p.length; i++) {
        mMarkListVector.remove(p[i]);
        p[i].unmark(this);
      }
      
      revalidate(p);
      updateTree();
    }
  }

  /**
   * Updates the plugin tree.
   */
  public void updateTree() {
    PluginTreeNode node = getRootNode();
    node.removeAllActions();
    node.removeAllChildren();
    node.getMutableTreeNode().setShowLeafCountEnabled(false);

    if (mMarkListVector.size() == 1) {
      mMarkListVector.getListAt(0).createNodes(node, false);
    } else
      for (MarkList list : mMarkListVector) {
        PluginTreeNode temp = node.addNode(list.getName());
        temp.getMutableTreeNode().setShowLeafCountEnabled(false);
        list.createNodes(temp, false);
      }
    node.update();
  }

  protected ImageIcon createIconForTree(int i) {
    switch (i) {
    case 0:
      return createImageIcon("actions", "edit-delete", 16);
    default:
      return createImageIcon("actions", "just-mark", 16);
    }
  }

  protected void revalidate(Program[] programs) {
    for (Program p : programs) {
      if (mMarkListVector.contains(p)) {
        p.mark(this);
        p.validateMarking();
      } else
        p.unmark(this);

    }
    updateTree();
  }

  protected MarkList[] getMarkLists() {
    return mMarkListVector.toArray(new MarkList[mMarkListVector.size()]);
  }

  protected void removeListForName(String name) {
    mMarkListVector.removeListForName(name);
    updateTree();
  }

  protected void removeList(MarkList list) {
    mMarkListVector.removeListForName(list.getName());
  }

  protected MarkList getMarkListForName(String name) {
    return mMarkListVector.getListForName(name);
  }

  protected void setIconFileNameForList(String listName, String fileName) {
    MarkList list = mMarkListVector.getListForName(listName);
    list.setMarkIconFileName(fileName);
  }

  protected void addList(String name) {
    addList(new MarkList(name));
  }

  protected void addList(MarkList list) {
    mMarkListVector.addElement(list);
    updateTree();
  }

  /**
   * @since 1.3 Increase update speed, with no double checking of the programs
   *        of this Plugin.
   */
  public PluginTreeNode getRootNode() {
    return mRootNode;
  }

  protected Icon getIconForFileName(String fileName) {
    if (fileName != null)
      return createImageIconForFileName(fileName);
    else
      return createImageIcon("actions", "just-mark", 16);
  }

  protected Frame getSuperFrame() {
    return getParentFrame();
  }
  
  protected void refreshPanels() {
    if(mProgramsTitlePanel != null && mProgramsPanel != null) {
      mProgramsTitlePanel.selectPrograms(false);
      mProgramsPanel.selectPrograms(false);
    }
  }
}
