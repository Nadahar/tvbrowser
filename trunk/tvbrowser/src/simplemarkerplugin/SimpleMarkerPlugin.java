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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
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

import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.programtable.ProgramTableModel;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.PluginsFilterComponent;
import devplugin.Program;
import devplugin.ProgramFilter;
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
public class SimpleMarkerPlugin extends Plugin implements ActionListener {
  private static final Version mVersion = new Version(2,70,0);
  
  /** The localizer for this class. */
  protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SimpleMarkerPlugin.class);

  private Program mProg = null;

  private static SimpleMarkerPlugin mInstance;

  private MarkListsVector mMarkListVector;

  private Properties mProperties;

  private PluginTreeNode mRootNode = new PluginTreeNode(this, false);

  private boolean mHasRightToUpdate = false, mHasToUpdate = false;
  
  private ManagePanel mManagePanel = null;

  private PluginInfo mPluginInfo;

  private boolean mStartFinished = false;

  /**
   * Standard constructor for this class.
   */
  public SimpleMarkerPlugin() {
    mInstance = this;
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
    if (prop == null) {
      mProperties = new Properties();
    } else {
      mProperties = prop;
    }
  }

  public Properties storeSettings() {
    return mProperties;
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
  public ActionMenu getContextMenuActions(Program p) {
    if(p == null || p.equals(getPluginManager().getExampleProgram()) || getPluginManager().getFilterManager() == null) {
      return new ActionMenu(new ContextMenuAction(mLocalizer.msg("mark", "Mark"),createImageIcon("status", "mail-attachment", 16)));
    }
    
    this.mProg = p;
    
    Object[] submenu = new Object[mMarkListVector.size() + 2];
    ContextMenuAction menu = new ContextMenuAction();
    menu.setText(mLocalizer.msg("mark", "Mark"));
    menu.setSmallIcon(createImageIcon("status", "mail-attachment", 16));
    
    if (mMarkListVector.size() == 1) {
      // Create context menu entry
      submenu[0] = getDefaultAction(p);
    } else {
      for (int i = 0; i < mMarkListVector.size(); i++) {
        submenu[i] = mMarkListVector.getListAt(i).getContextMenuAction(p);
      }
    }
    
    submenu[submenu.length-2] = ContextMenuSeparatorAction.getDisabledOnTaskMenuInstance();
    submenu[submenu.length-1] = getExtendedMarkMenu();
    
    return new ActionMenu(menu, submenu);
  }

  private ActionMenu getExtendedMarkMenu() {
    // get all non-default filters
    ArrayList<ProgramFilter> markFilters = new ArrayList<ProgramFilter>();
    for (ProgramFilter filter : getPluginManager().getFilterManager().getAvailableFilters()) {
      if (filter != null && (!(filter.equals(getPluginManager().getFilterManager().getDefaultFilter()))) && (!(getPluginManager().getFilterManager().isPluginFilter(filter)))) {
        markFilters.add(filter);
      }
    }
    // create an action for each filter
    AbstractAction[] filtersAction = new AbstractAction[markFilters.size()];
    for (int i = 0; i < markFilters.size(); i++) {
      final ProgramFilter filter = markFilters.get(i);
      filtersAction[i] = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          // collect all (visible) programs of this day which match the filter
          ArrayList<Program> progList = new ArrayList<Program>();
          ProgramTableModel model = MainFrame.getInstance().getProgramTableModel();

          int columnCount = model.getColumnCount();
          for (int col = 0; col < columnCount; col++) {
            int rowCount = model.getRowCount(col);
            for (int row = 0; row < rowCount; row++) {
              Program program = model.getProgramPanel(col, row).getProgram();
              if (filter.accept(program)) {
                progList.add(program);
              }
            }
          }
          // now mark all those programs on the default list
          for (Program program : progList) {
            mMarkListVector.getListAt(0).addElement(program);
            program.mark(SimpleMarkerPlugin.this);
          }
          mMarkListVector.getListAt(0).updateNode();
          save();
        }};
      filtersAction[i].putValue(Action.NAME, filter.getName());
    }
    // create the new (sub) menu
    ContextMenuAction menuExtended = new ContextMenuAction();
    menuExtended.setText(mLocalizer.msg("extendedMark", "Mark programs of filter..."));
    menuExtended.setActionListener(this);
    menuExtended.putValue(Plugin.DISABLED_ON_TASK_MENU,true);
    menuExtended.putValue(Program.MARK_PRIORITY, mMarkListVector.getListAt(0).getMarkPriority());
    
    // workaround for not correct menu painting
    menuExtended.putValue(Action.SMALL_ICON, new Icon() {
      public int getIconHeight() {
        return 16;
      }

      public int getIconWidth() {
        return 16;
      }

      public void paintIcon(Component c, Graphics g, int x, int y) {}
    });
    
    ActionMenu actionMenuExtendedMark = new ActionMenu(menuExtended, filtersAction);
    return actionMenuExtendedMark;
  }

  private ContextMenuAction getDefaultAction(Program p) {
    ContextMenuAction menu = new ContextMenuAction();
    menu.setText(mLocalizer.msg("markProgram", "Mark program"));
    
    if (mMarkListVector.getListAt(0).contains(p)) {
      menu.setText(mLocalizer.msg("unmark", "Remove marking"));
    }
    else {
      menu.putValue(Program.MARK_PRIORITY, mMarkListVector.getListAt(0).getMarkPriority());
    }
    
    menu.putValue(Action.ACTION_COMMAND_KEY, menu.getValue(Action.NAME));
    menu.setSmallIcon(createImageIcon("status", "mail-attachment", 16));
    menu.setActionListener(this);

    return menu;
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
        targetList.addElement(p);
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
      
      if(!deletedPrograms.isEmpty() && mProperties.getProperty("showDeletedProgram","true").equals("true")) {
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
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(mLocalizer.msg("markProgram", "Mark program"))) {
      mMarkListVector.getListAt(0).addElement(mProg);
      mProg.mark(this);
      mMarkListVector.getListAt(0).updateNode();
    } else if (e.getActionCommand().equals(
        mLocalizer.msg("unmark", "Remove marking"))) {
      mMarkListVector.getListAt(0).removeElement(mProg);
      mProg.unmark(this);
      mMarkListVector.getListAt(0).updateNode();
    }
    
    refreshManagePanel(false);
    save();
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
      Program[] programs = node.getPrograms();

      for (Program program : programs) {
        mMarkListVector.remove(program);
        program.unmark(this);
      }
      
      revalidate(programs);
      updateTree();
    }
  }

  /**
   * Updates the plugin tree.
   */
  public void updateTree() {
    if (!mStartFinished) {
      return;
    }
    PluginTreeNode node = getRootNode();
    node.removeAllActions();
    node.removeAllChildren();
    node.getMutableTreeNode().setShowLeafCountEnabled(false);

    if (mMarkListVector.size() == 1) {
      mMarkListVector.getListAt(0).createNodes(node, false);
    } else {
      for (MarkList list : mMarkListVector) {
        PluginTreeNode temp = node.addNode(list.getName());
        temp.getMutableTreeNode().setShowLeafCountEnabled(false);
        temp.getMutableTreeNode().setIcon(list.getMarkIcon());
        temp.getMutableTreeNode().setProgramReceiveTarget(list.getReceiveTarget());
        list.createNodes(temp, false);
      }
    }
    
    node.update();
  }

  protected ImageIcon createIconForTree(int i) {
    switch (i) {
    case 0:
      return createImageIcon("actions", "edit-delete", 16);
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
      // Make sure the compiler not has to make unsafe class cast, therefor class is casted manually to needed type
      return (Class<? extends PluginsFilterComponent>[]) new Class[] {MarkListFilterComponent.class};
    }

    return null;
  }
    
  protected Properties getSettings() {
    return mProperties;
  }
  
  protected void save() {
    saveMe();
  }
}
