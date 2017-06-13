/*
 * SimpleMarkerPlugin by René Mach
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package simplemarkerplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import compat.PersonaCompat;
import compat.PluginCompat;
import compat.ProgramCompat;
import compat.UiCompat;
import compat.VersionCompat;
import devplugin.ActionMenu;
import devplugin.AfterDataUpdateInfoPanel;
import devplugin.ContextMenuAction;
import devplugin.Date;
import devplugin.ImportanceValue;
import devplugin.Plugin;
import devplugin.PluginCenterPanel;
import devplugin.PluginCenterPanelWrapper;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.PluginsFilterComponent;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.Version;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

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
  private static final Version mVersion = new Version(3,26,0,true);

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SimpleMarkerPlugin.class);

  private static SimpleMarkerPlugin mInstance;

  private MarkListsVector mMarkListVector;

  private PluginTreeNode mRootNode = new PluginTreeNode(false,this);

  private boolean mHasRightToUpdate = false, mHasToUpdate = false;

  private int mInfoCounter;
  
  private ManageDialog mManageDialog = null;

  private PluginInfo mPluginInfo;

  private boolean mStartFinished = false;

  private SimpleMarkerSettings mSettings;
  
  private Object mWrapper;
  
  private JPanel mCenterPanelWrapper;
  
  private ManagePanel mMangePanel;
  
  private SimpleMarkerUpdateInfoPanel mInfoPanel;
  
  private Thread mInfoShowingThread;
  
  private static int mActionIdCount;

  /**
   * Standard constructor for this class.
   */
  public SimpleMarkerPlugin() {
    mInstance = this;
    mActionIdCount = 1;
  }

  /**
   * Gets the localizer of this plugin class.
   * <p>
   * @return The localizer.
   */
  public static final Localizer getLocalizer() {
    return mLocalizer;
  }

  public void onActivation() {
    mInfoCounter = 1;
    mMarkListVector = new MarkListsVector();
    updateTree(true);
    
    /*SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {*/
        mCenterPanelWrapper = UiCompat.createPersonaBackgroundPanel();
        
        if(VersionCompat.isCenterPanelSupported()) {
          mWrapper = new PluginCenterPanelWrapper() {
            @Override
            public PluginCenterPanel[] getCenterPanels() {
              return new PluginCenterPanel[] {new SimpleMarkerPanel()};
            }
          };
        }
        
     /* }
    });*/
  }
  
  private void addCenterPanel() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mMangePanel = new ManagePanel(mMarkListVector, null);
        PersonaCompat.getInstance().registerPersonaListener(mMangePanel);
        
        mCenterPanelWrapper.add(mMangePanel,BorderLayout.CENTER);
        mCenterPanelWrapper.updateUI();
      }
    });    
  }
  
  private void updateCenterPanel() {
    if(mMangePanel != null && mCenterPanelWrapper != null) {
      mCenterPanelWrapper.remove(mMangePanel);
      PersonaCompat.getInstance().removePersonaListener(mMangePanel);
      mMangePanel = null;
      
      addCenterPanel();
    }
  }
  
  public void onDeactivation() {
    PersonaCompat.getInstance().registerPersonaListener(mMangePanel);
    mCenterPanelWrapper.remove(mMangePanel);
    mMangePanel = null;
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

      mPluginInfo = new PluginInfo(SimpleMarkerPlugin.class, name, description, "Ren\u00e9 Mach", "GPL");
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
    if(program == null || getPluginManager().getFilterManager() == null) {
      return new ActionMenu(new ContextMenuAction(mLocalizer.msg("mark", "Mark"),createImageIcon("status", "mail-attachment", 16)));
    }
    
    boolean isExampleProgram = getPluginManager().getExampleProgram().equals(program);

    ActionMenu result = null;
    
    if (mMarkListVector.size() == 1) {
      // Create context menu entry
      result = new ActionMenu(mMarkListVector.getListAt(0).getContextMenuAction(program, true));
      
      if(isExampleProgram) {
        result.getAction().putValue(Action.NAME, mLocalizer.msg("mark", "Mark") + " - " + result.getAction().getValue(Action.NAME)); 
      }
    } else {
      ActionMenu[] submenu = new ActionMenu[mMarkListVector.size()];

      for (int i = 0; i < mMarkListVector.size(); i++) {
        submenu[i] = mMarkListVector.getListAt(i).getContextMenuAction(program, false);
        
        if(isExampleProgram) {
          submenu[i].getAction().putValue(Action.NAME, mLocalizer.msg("mark", "Mark") + " - " + submenu[i].getAction().getValue(Action.NAME));
        }
      }
      
      result = new ActionMenu(mLocalizer.msg("mark", "Mark"), createImageIcon("status", "mail-attachment", 16), submenu);
      result.getAction().putValue("showOnlySubMenus", mSettings.isShowingInContextMenu());
    }
    
    return result;
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
    save(false);

    return true;
  }

  public ImportanceValue getImportanceValueForProgram(Program p) {
    if(p != null) {
      String[] lists = mMarkListVector.getNamesOfListsContainingProgram(p);

      short importance = 0;
      byte count = 0;

      for(String list : lists) {
        byte test = mMarkListVector.getListForName(list).getProgramImportance();

        if(test > Program.DEFAULT_PROGRAM_IMPORTANCE) {
          count++;
          importance += test;
        }
      }

      if(count > 0) {
        return new ImportanceValue(count,importance);
      }
    }

    return new ImportanceValue((byte)1,Program.DEFAULT_PROGRAM_IMPORTANCE);
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
  
  public void handleTvDataUpdateStarted() {
    handleTvDataUpdateStarted(null);
  }
  
  public void handleTvDataUpdateStarted(Date until) {
    mInfoPanel = null;
  }
  
  public void handleTvDataUpdateFinished() {
    mHasToUpdate = true;

    if (mHasRightToUpdate) {
      mHasToUpdate = false;

      final ArrayList<Program> deletedPrograms = new ArrayList<Program>();

      for (MarkList list : mMarkListVector) {
        list.revalidateContainingPrograms(deletedPrograms);
      }
      
      if (!deletedPrograms.isEmpty()) {
        mInfoPanel = new SimpleMarkerUpdateInfoPanel(deletedPrograms.toArray(new Program[deletedPrograms.size()]));
      }
    }
    
    mInfoCounter = 100;
    
    if(mInfoShowingThread != null && mInfoShowingThread.isAlive()) {
      mInfoShowingThread.interrupt();
    }
    
    if(mMangePanel != null) {
      mMangePanel.selectPrograms(false);
    }
    
    if(!VersionCompat.isCenterPanelSupported() && mInfoPanel != null) {
      final JDialog d = UiUtilities.createDialog(UiUtilities.getLastModalChildOf(getParentFrame()), true);
      d.setTitle(getInfo().getName());
      d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      
      
      final JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
      close.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if(mInfoPanel != null) {
            mInfoPanel.closed();
          }
          
          d.dispose();
        }
      });
      
      UiUtilities.registerForClosing(new WindowClosingIf() {
        @Override
        public JRootPane getRootPane() {
          return d.getRootPane();
        }
        
        @Override
        public void close() {
          if(mInfoPanel != null) {
            mInfoPanel.closed();
          }
          
          d.dispose();
        }
      });
      
      d.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          if(mInfoPanel != null) {
            mInfoPanel.closed();
          }
           
          d.dispose();
        }
      });
      
      final JPanel content = new JPanel(new FormLayout("100dlu:grow,default","fill:100dlu:grow,5dlu,default"));
      content.setBorder(Borders.DIALOG_BORDER);
      content.add(mInfoPanel, CC.xyw(1, 1, 2));
      content.add(close, CC.xy(2, 3));
      
      d.setContentPane(content);
      layoutWindow("simpleMarkerRemoved", d, new Dimension(400,400));
      d.setVisible(true);
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
    addCenterPanel();
    
    mStartFinished  = true;
    if(mMarkListVector.isEmpty()) {
      mMarkListVector.addElement(new MarkList(mLocalizer.msg("default","default"),1));
    }

    // now really load the programs which we currently only know as by date and id
    for (MarkList markList : mMarkListVector) {
      markList.loadPrograms();
    }

    mHasRightToUpdate = true;
    updateTree(true);
    if (mHasToUpdate) {
      handleTvDataUpdateFinished();
    }


  }

  private void showProgramsList() {try {
    mManageDialog = new ManageDialog(mMarkListVector);

    layoutWindow("manageDlg", mManageDialog, new Dimension(434, 330));
    mManageDialog.setVisible(true);
    updateTree();}catch(Throwable t) {t.printStackTrace();}
  }

  public void readData(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    int version = in.readInt();

    if (version >= 1) {
      int size = in.readInt();
      for (int i = 0; i < size; i++) {
        MarkList list = new MarkList(in);
        
        if(version == 1) {
          list.setActionId(mActionIdCount++);
        }
        //mMarkListVector.addElement();
      }
    }
    
    if(version >= 2) {
      mActionIdCount = in.readInt();
    }
    
    if(version == 1) {
      saveMe();
    }
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2); // version
    out.writeInt(mMarkListVector.size());
    for (int i = 0; i < mMarkListVector.size(); i++) {
      mMarkListVector.getListAt(i).writeData(out);
    }
    out.writeInt(mActionIdCount);
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
    node.addActionMenu(new ActionMenu(mLocalizer
        .msg("grouping.grouping", "Grouping"), groupActions));
  }
  
  protected void updateTree() {
    updateTree(false);
  }

  /**
   * Updates the plugin tree.
   */
  protected void updateTree(boolean scroll) {
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
    
    if(mMangePanel != null) {
      mMangePanel.selectPrograms(scroll);
    }
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
    save(false);
  }

  protected MarkList[] getMarkLists() {
    return mMarkListVector.toArray(new MarkList[mMarkListVector.size()]);
  }

  /**
   * Sets the mark lists.
   * <p>
   * @param markLists The new mark lists.
   */
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

  protected void refreshManageDialog(boolean scroll) {
    if(mManageDialog != null) {
      mManageDialog.selectPrograms(scroll);
    }
  }

  protected void resetManageDialog() {
    mManageDialog = null;
  }

  @SuppressWarnings("unchecked")
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

  protected void save(boolean updateCenterPanel) {
    if(updateCenterPanel) {
      updateCenterPanel();
    }
    
    if(mMangePanel != null) {
      mMangePanel.selectPrograms(false);
    }
    
    saveMe();
  }
  
  public String getPluginCategory() {
    return PluginCompat.CATEGORY_OTHER;
  }

  public PluginCenterPanelWrapper getPluginCenterPanelWrapper() {
    return (PluginCenterPanelWrapper)mWrapper;
  }

  private class SimpleMarkerPanel extends PluginCenterPanel {
    @Override
    public String getName() {
      return getInfo().getName();
    }

    @Override
    public JPanel getPanel() {
      return mCenterPanelWrapper;
    }
  }
  
  public AfterDataUpdateInfoPanel getAfterDataUpdateInfoPanel() {
    mInfoShowingThread = new Thread() {
      public void run() {
        while(mInfoCounter++ < 100) {
          try {
            sleep(100);
          } catch (InterruptedException e) {}
        }
      }
    };
    mInfoShowingThread.start();
    
    if(mInfoShowingThread.isAlive()) {
      try {
        mInfoShowingThread.join();
      } catch (InterruptedException e) {}
    }
    
    if(mInfoPanel != null) {
      return mInfoPanel;
    }
    
    return null;
  }
  
  private class SimpleMarkerUpdateInfoPanel extends AfterDataUpdateInfoPanel {
    private ProgramList mDeletedProgramList;
    
    public SimpleMarkerUpdateInfoPanel(Program[] progArr) {
      mDeletedProgramList = new ProgramList(progArr, new ProgramPanelSettings(new PluginPictureSettings(PluginPictureSettings.NO_PICTURE_TYPE),true,true));
      mDeletedProgramList.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
          if(e.isPopupTrigger()) {
            showPopup(e);
          }
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
          if(e.isPopupTrigger()) {
            showPopup(e);
          }
        }
      });
      
      JScrollPane scroll = new JScrollPane(mDeletedProgramList);
      scroll.setMaximumSize(new Dimension(2048,200));
      
      setLayout(new FormLayout("default:grow","default,5dlu,fill:default:grow,5dlu,default"));
      CellConstraints cc = new CellConstraints();

      add(new JLabel(mLocalizer.msg("deletedProgramsMsg","During the data update the following programs were deleted:")),cc.xy(1,1));
      add(scroll, cc.xy(1,3));
      
      setPreferredSize(new Dimension(200,200));
    }
    
    /**
     * Shows the Popup
     * 
     * @param e Mouse-Event
     */
    private void showPopup(MouseEvent e) {
      int row = mDeletedProgramList.locationToIndex(e.getPoint());

      mDeletedProgramList.setSelectedIndex(row);

      Program p = (Program) mDeletedProgramList.getSelectedValue();
      
      JPopupMenu menu = ProgramCompat.createRemovedProgramContextMenu(p);
      menu.show(mDeletedProgramList, e.getX(), e.getY());
    }
    
    @Override
    public void closed() {
      new Thread() {
        public void run() {
          try {
            sleep(2000);
          } catch (InterruptedException e) {
          }
          
          mInfoPanel = null;
          mInfoCounter = 1;
        }
      };
    }
  }
  
  public static int getAndIncrementActionIdCount() {
    return mActionIdCount++;
  }
}
