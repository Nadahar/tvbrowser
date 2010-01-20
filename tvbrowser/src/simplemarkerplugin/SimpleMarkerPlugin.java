/*
 * SimpleMarkerPlugin by René Mach
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
package simplemarkerplugin;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.PluginsFilterComponent;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
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
 * @author René Mach
 */
public class SimpleMarkerPlugin extends Plugin {
  private static final Version mVersion = new Version(2,70,0);
  
  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SimpleMarkerPlugin.class);

  private static SimpleMarkerPlugin mInstance;

  private MarkListsVector mMarkListVector;

  private PluginTreeNode mRootNode = new PluginTreeNode(this, false);

  private boolean mHasRightToUpdate = false, mHasToUpdate = false;
  
  private ManagePanel mManagePanel = null;

  private PluginInfo mPluginInfo;

  private boolean mStartFinished = false;

  private SimpleMarkerSettings mSettings;

  /**
   * Standard constructor for this class.
   */
  public SimpleMarkerPlugin() {
    mInstance = this;
  }

  public static final Localizer getLocalizer() {
    return mLocalizer;
  }
  
  public void onActivation() {
    mMarkListVector = new MarkListsVector();
    updateTree();
  }
  
  public static Version getVersion() {
    return mVersion;
  }
  
  /**
   * @return The instance of this class.
   */
  public static SimpleMarkerPlugin getInstance() {
    return mInstance;
  }

  /** @return The Plugin Info. */
  public PluginInfo getInfo() {
    if(mPluginInfo == null) {
      String name = mLocalizer.msg("name","Marker plugin");
      String description = mLocalizer.msg("description", "A simple marker plugin (formerly Just_Mark)");
      
      mPluginInfo = new PluginInfo(SimpleMarkerPlugin.class, name, description, "René Mach", "GPL");
    }
    
    return mPluginInfo;
  }

  public void loadSettings(Properties prop) {
    mSettings = new SimpleMarkerSettings(prop);
    addGroupingActions(mRootNode);
  }

  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  /**
   * @return The MarkIcons.
   */
  public Icon[] getMarkIconsForProgram(Program p) {
    if(p == null || p.equals(getPluginManager().getExampleProgram())) {
      return new Icon[] {mMarkListVector.get(0).getMarkIcon()};
    }
    
    String[] lists = mMarkListVector.getNamesOfListsContainingProgram(p);
    Icon[] icons = new Icon[lists.length];

    for (int i = 0; i < lists.length; i++) {
      icons[i] = mMarkListVector.getListForName(lists[i]).getMarkIcon();
    }

    return icons;
  }
  
  public SettingsTab getSettingsTab() {
    return (new SimpleMarkerPluginSettingsTab());
  }

  /**
   * @return The ActionMenu for this Plugin.
   */
  public ActionMenu getContextMenuActions(final Program program) {
    if(program == null || program.equals(getPluginManager().getExampleProgram()) || getPluginManager().getFilterManager() == null) {
      return new ActionMenu(new ContextMenuAction(mLocalizer.msg("mark", "Mark"),createImageIcon("status", "mail-attachment", 16)));
    }
    
    if (mMarkListVector.size() == 1) {
      // Create context menu entry
      return new ActionMenu(mMarkListVector.getListAt(0).getContextMenuAction(program, true));
    } else {
      Object[] submenu = new Object[mMarkListVector.size()];
      ContextMenuAction menu = new ContextMenuAction();
      menu.setText(mLocalizer.msg("mark", "Mark"));
      menu.setSmallIcon(createImageIcon("status", "mail-attachment", 16));
      
      for (int i = 0; i < mMarkListVector.size(); i++) {
        submenu[i] = mMarkListVector.getListAt(i).getContextMenuAction(program, false);
      }
      return new ActionMenu(menu, submenu);
    }
    
  }
  
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return mMarkListVector.getReceiveTargets();
  }
  
  public boolean receivePrograms(Program[] programs, ProgramReceiveTarget target) {
    MarkList targetList = mMarkListVector.getMarkListForTarget(target);

    if(targetList == null) {
      return false;
    }

    for (Program p : programs) {
      if (!targetList.contains(p)) {
        targetList.addProgram(p);
        p.mark(this);
        p.validateMarking();
      }
    }
    targetList.updateNode();
    save();

    return true;
  }
  
  public int getMarkPriorityForProgram(Program p) {
    int priority = Program.NO_MARK_PRIORITY;
    
    if(p != null) {
      String[] lists = mMarkListVector.getNamesOfListsContainingProgram(p);
      
      for(String list : lists) {
        priority = Math.max(priority,mMarkListVector.getListForName(list).getMarkPriority());
        
        if(priority == Program.MAX_MARK_PRIORITY) {
          break;
        }
      }
    }
    
    return priority;
  }

  public void handleTvDataUpdateFinished() {
    mHasToUpdate = true;

    if (mHasRightToUpdate) {
      mHasToUpdate = false;

      ArrayList<Program> deletedPrograms = new ArrayList<Program>();
      
      for (MarkList list : mMarkListVector) {
        list.revalidateContainingPrograms(deletedPrograms);
      }
      
      if (!deletedPrograms.isEmpty() && mSettings.showDeletedPrograms()) {
        ProgramList deletedProgramList = new ProgramList(deletedPrograms.toArray(new Program[deletedPrograms.size()]), new ProgramPanelSettings(new PluginPictureSettings(PluginPictureSettings.NO_PICTURE_TYPE),true));
        
        Window parent = UiUtilities.getLastModalChildOf(getParentFrame());
        JDialog deletedListDialog = new JDialog(parent);
        
        deletedListDialog.setModal(false);
        deletedListDialog.getContentPane().setLayout(new FormLayout("default:grow","default,5dlu,fill:default:grow,5dlu,default"));
        deletedListDialog.setTitle(getInfo().getName() + " - " + mLocalizer.msg("deletedPrograms","Deleted programs"));
        ((JPanel)deletedListDialog.getContentPane()).setBorder(Borders.DIALOG_BORDER);
        
        CellConstraints cc = new CellConstraints();
        
        deletedListDialog.getContentPane().add(new JLabel(mLocalizer.msg("deletedProgramsMsg","During the data update the following programs were deleted:")),cc.xy(1,1));
        deletedListDialog.getContentPane().add(new JScrollPane(deletedProgramList), cc.xy(1,3));
        
        final JDialog dlg = deletedListDialog;
        
        JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
        ok.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            dlg.dispose();
          }
        });
        
        deletedListDialog.getContentPane().add(ok, cc.xy(1,5));
        
        UiUtilities.registerForClosing(new WindowClosingIf() {
          public void close() {
            dlg.dispose();
          }

          public JRootPane getRootPane() {
            return dlg.getRootPane();
          }
        });
        
        layoutWindow("deletedListDialog", deletedListDialog, new Dimension(400,300));
        
        new Thread("simpleMarkerShowDeletedListDlg") {
          public void run() {
            while(UiUtilities.containsModalDialogChild(getParentFrame())) {
              try {
                Thread.sleep(500);
              } catch (InterruptedException e) {}
            }
            
            dlg.setVisible(true);
          }
        }.start();
      }
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
    action.putValue(Action.NAME, mLocalizer.msg("name","Marker plugin"));
    // small icon
    action.putValue(Action.SMALL_ICON, createImageIcon("status", "mail-attachment",
        16));
    // big icon
    action.putValue(BIG_ICON, createImageIcon("status", "mail-attachment", 22));
    return new ActionMenu(action);
  }

  public void handleTvBrowserStartFinished() {
    mStartFinished  = true;
    if(mMarkListVector.isEmpty()) {
      mMarkListVector.addElement(new MarkList(mLocalizer.msg("default","default")));
    }
    
    mHasRightToUpdate = true;
    updateTree();
    if (mHasToUpdate) {
      handleTvDataUpdateFinished();
    }
    
    
  }
  
  private void showProgramsList() {
    final JDialog dialog = UiUtilities.createDialog(getParentFrame(), true);
    dialog.setTitle(mLocalizer.msg("name","Marker plugin"));
    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    
    mManagePanel = new ManagePanel(dialog, mMarkListVector);
    
    layoutWindow("manageDlg", dialog, new Dimension(434, 330));
    
    dialog.setVisible(true);
    updateTree();
  }

  public void readData(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    int version = in.readInt();

    if (version == 1) {
      int size = in.readInt();
      for (int i = 0; i < size; i++) {
        new MarkList(in);
        //mMarkListVector.addElement();
      }
    }
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    out.writeInt(mMarkListVector.size());
    for (int i = 0; i < mMarkListVector.size(); i++) {
      mMarkListVector.getListAt(i).writeData(out);
    }
  }

  public boolean canUseProgramTree() {
    return true;
  }

  protected void addGroupingActions(PluginTreeNode node) {
    ActionMenu displayBoth = new ActionMenu(new AbstractAction(mLocalizer.msg(
        "grouping.both", "By title and date")) {
      public void actionPerformed(ActionEvent e) {
        mSettings.setNodeGroupingByBoth();
        updateTree();
      }
    }, mSettings.isGroupingByBoth());
    ActionMenu displayTitle = new ActionMenu(new AbstractAction(mLocalizer.msg(
        "grouping.title", "By title")) {
      public void actionPerformed(ActionEvent e) {
        mSettings.setNodeGroupingByTitle();
        updateTree();
      }
    }, mSettings.isGroupingByTitle());
    ActionMenu displayDate = new ActionMenu(new AbstractAction(mLocalizer.msg(
        "grouping.date", "By date")) {
      public void actionPerformed(ActionEvent e) {
        mSettings.setNodeGroupingByDate();
        updateTree();
      }
    }, mSettings.isGroupingByDate());
    ActionMenu[] groupActions = new ActionMenu[] { displayBoth, displayTitle,
        displayDate };
    node.addActionMenu(new ActionMenu(new ContextMenuAction(mLocalizer
        .msg("grouping.grouping", "Grouping")), groupActions));
  }

  /**
   * Updates the plugin tree.
   */
  protected void updateTree() {
    if (!mStartFinished) {
      return;
    }
    PluginTreeNode root = getRootNode();
    root.removeAllChildren();
    root.removeAllActions();
    root.getMutableTreeNode().setShowLeafCountEnabled(false);

    for (Program p : getPluginManager().getMarkedPrograms()) {
      if (!mMarkListVector.contains(p)) {
        p.unmark(this);
      } else {
        p.validateMarking();
      }
    }

    if (mMarkListVector.size() == 1) {
      mMarkListVector.getListAt(0).createNodes(root, false);
    } else {
      for (MarkList list : mMarkListVector) {
        PluginTreeNode listNode = root.addNode(list.getName());
        listNode.getMutableTreeNode().setShowLeafCountEnabled(false);
        listNode.getMutableTreeNode().setIcon(list.getMarkIcon());
        listNode.getMutableTreeNode().setProgramReceiveTarget(
            list.getReceiveTarget());
        list.createNodes(listNode, false);
      }
    }
    addGroupingActions(root);
    root.update();
  }

  protected ImageIcon createIconForTree(int i) {
    switch (i) {
    case 0:
      return TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL);
    default:
      return createImageIcon("status", "mail-attachment", 16);
    }
  }

  protected void revalidate(Program[] programs) {
    for (Program p : programs) {
      if (mMarkListVector.contains(p)) {
        p.mark(this);
        p.validateMarking();
      } else {
        p.unmark(this);
      }

    }
    updateTree();
    save();
  }

  protected MarkList[] getMarkLists() {
    return mMarkListVector.toArray(new MarkList[mMarkListVector.size()]);
  }

  public void setMarkLists(MarkList[] markLists) {
    mMarkListVector = new MarkListsVector();
    mMarkListVector.addAll(Arrays.asList(markLists));
    updateTree();
  }

  protected String[] getMarkListNames() {
    return mMarkListVector.getMarkListNames();
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

  protected MarkList getMarkListForId(String id) {
    return mMarkListVector.getListForId(id);
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
    if (fileName != null) {
      return createImageIconForFileName(fileName);
    } else {
      return createImageIcon("status", "mail-attachment", 16);
    }
  }

  protected Frame getSuperFrame() {
    return getParentFrame();
  }
 
  protected void refreshManagePanel(boolean scroll) {
    if(mManagePanel != null) {
      mManagePanel.selectPrograms(scroll);
    }
  }
  
  protected void resetManagePanel() {
    mManagePanel = null;
  }
  
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
    if(mMarkListVector.size() > 1) {
      // Make sure the compiler not has to make unsafe class cast, therefore class is casted manually to needed type
      return new Class[] {MarkListFilterComponent.class};
    }

    return null;
  }
    
  protected SimpleMarkerSettings getSettings() {
    return mSettings;
  }
  
  protected void save() {
    saveMe();
  }

}
