/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
package devplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;

import tvbrowser.core.TvDataUpdateListener;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.ui.pluginview.Node;
import tvbrowser.ui.pluginview.PluginTreeModel;
import util.program.ProgramUtilities;
import util.ui.Localizer;


/**
 * The PluginTreeNode class represents a single node of the plugin tree view.
 *
 */
public class PluginTreeNode {

  private static final util.ui.Localizer mLocalizer =
      util.ui.Localizer.getLocalizerFor(PluginTreeNode.class);

  private int mNodeType;
  private ArrayList<PluginTreeNode> mChildNodes;
  private Object mObject;
  private ArrayList<PluginTreeListener> mNodeListeners;
  private Marker mMarker;
  private Node mDefaultNode;
  private boolean mGroupingByDate;
  private boolean mGroupWeekly;

  private PluginTreeNode(int type, Object o) {
    mChildNodes = null; // do not initialize to save memory
    mNodeType = type;
    mObject = o;
    mDefaultNode = new Node(type, mObject);
    mNodeListeners = null; // do not initialize to save memory
    mGroupingByDate = true;
    mGroupWeekly = false;
  }

  /**
   * Creates a new PluginTreeNode object with a specified title
   * @param title
   */
  public PluginTreeNode(String title) {
    this(Node.CUSTOM_NODE, title);
  }

  /**
   * Creates a new root PluginTreeNode
   * On TV listings updates, the {@link PluginTreeListener} gets informed.
   * @param marker
   */
  public PluginTreeNode(Marker marker) {
    this(marker, true);
  }

  /**
   * Creates a new root PluginTreeNode
   * @param marker
   * @param handleTvDataUpdate specifies, if the {@link PluginTreeListener}
   * should get called on TV listings updates
   */
  public PluginTreeNode(Marker marker, boolean handleTvDataUpdate) {
    this(Node.PLUGIN_ROOT, marker);
    mMarker = marker;

    if (handleTvDataUpdate) {
      final RemovedProgramsHandler removedProgramsHandler = new RemovedProgramsHandler();

      TvDataUpdater.getInstance().addTvDataUpdateListener(new TvDataUpdateListener() {
        public void tvDataUpdateStarted() {
          removedProgramsHandler.clear();
        }

        public void tvDataUpdateFinished() {
          refreshAllPrograms(removedProgramsHandler);
          update();
          Program[] removedPrograms = removedProgramsHandler.getRemovedPrograms();
          fireProgramsRemoved(removedPrograms);
        }
      });
    }

  }

  /**
   * Creates a new Node containing a program item.
   * @param item
   */
  public PluginTreeNode(ProgramItem item) {
    this(Node.PROGRAM, item);
    mDefaultNode.setAllowsChildren(false);
  }


  public void addNodeListener(PluginTreeListener listener) {
    if (mNodeListeners == null) {
      mNodeListeners = new ArrayList<PluginTreeListener>(1);
    }
    mNodeListeners.add(listener);
  }

  public boolean removeNodeListener(PluginTreeListener listener) {
    if (mNodeListeners == null) {
      return false;
    }
    return mNodeListeners.remove(listener);
  }

  public void removeAllNodeListeners() {
    if (mNodeListeners == null) {
      return;
    }
    mNodeListeners.clear();
    mNodeListeners = null;
  }

  /**
   * Remove all programs from this node which are not available any more
   */
  private synchronized void refreshAllPrograms(RemovedProgramsHandler handler) {
    // non initialized child collection, if it is empty
    if (mChildNodes == null) {
      return;
    }
    for (int i=mChildNodes.size()-1; i>=0; i--) {
      PluginTreeNode node = mChildNodes.get(i);
      node.mMarker = mMarker;
      
      if (node.isLeaf()) {
        ProgramItem progItemInTree = (ProgramItem)node.getUserObject();
        Program progInTree = progItemInTree.getProgram();
        
        if(progInTree.getProgramState() == Program.WAS_DELETED_STATE) {
          removeProgram(progInTree);
          handler.addRemovedProgram(progInTree);
        }
        else if(progInTree.getProgramState() == Program.WAS_UPDATED_STATE) {
          Program updatedProg = Plugin.getPluginManager().getProgram(progInTree.getDate(), progInTree.getID());
          progItemInTree.setProgram(updatedProg);
        }
      }
      else {
        node.refreshAllPrograms(handler);
      }
    }
  }

  private void fireProgramsRemoved(Program[] progArr) {
    if (mNodeListeners == null) {
      return;
    }
    for (int i=0; i<mNodeListeners.size(); i++) {
      PluginTreeListener listener = mNodeListeners.get(i);
      listener.programsRemoved(progArr);
    }
  }

  public Node getMutableTreeNode() {
    return mDefaultNode;
  }

  /**
   * Adds a an action menu to this node
   * @param menu
   */
  public void addActionMenu(ActionMenu menu) {
    mDefaultNode.addActionMenu(menu);
  }


  public void removeAllActions() {
    mDefaultNode.removeAllActionMenus();
  }

  public void addAction(Action action) {
    addActionMenu(new ActionMenu(action));
  }

  public ActionMenu[] getActionMenus() {
    return mDefaultNode.getActionMenus();
  }

  /**
   * Sets the formatter for this node and all of the child nodes.
   * @param formatter the formatter
   */
  public void setNodeFormatter(NodeFormatter formatter) {
    mDefaultNode.setNodeFormatter(formatter);
  }

  /**
   * Enables/Disables the 'grouping by date'-feature.
   * Default is 'enabled'
   *
   * @param enable
   */
  public void setGroupingByDateEnabled(boolean enable) {
    mGroupingByDate = enable;
  }
  
  /**
   * Enables/Disables 'grouping by week' for nodes showing
   * programs by date. Only evaluated if "grouping by date"
   * is enabled.
   * Default is 'disabled'
   * 
   * @see #setGroupingByDateEnabled(boolean)
   *
   * @param enable
   */
  public void setGroupingByWeekEnabled(boolean enable) {
    mGroupWeekly = enable;
  }

  private NodeFormatter getNodeFormatter(boolean isWeekNodesEnabled) {
    return mDefaultNode.getNodeFormatter(isWeekNodesEnabled);
  }

  private void createDefaultNodes() {
    mDefaultNode.removeAllChildren();
    // non initialized child collection, if it is empty
    if (mChildNodes == null) {
      return;
    }
    PluginTreeNode[] items = mChildNodes.toArray(new PluginTreeNode[mChildNodes.size()]);
    Arrays.sort(items, sPluginTreeNodeComparator);
    Date currentDate = null;
    for (int i=0; i<items.length; i++) {
      PluginTreeNode n = items[i];
      if (!n.isLeaf()) {
        if (n.mGroupingByDate) {
          n.createDateNodes();
        }
        else {
          n.createDefaultNodes();
        }
        mDefaultNode.add(n.getMutableTreeNode());
      }
      else {
        if (n.mNodeType == Node.PROGRAM) {
          ProgramItem progItem = (ProgramItem)n.getUserObject();
          Node node = n.getMutableTreeNode();
          
          if (currentDate == null) {
            currentDate = Date.getCurrentDate();
          }
          if(progItem.getProgram().getDate().addDays(1).compareTo(currentDate) >= 0)
            mDefaultNode.add(node);
        }
        else {
          mDefaultNode.add(n.getMutableTreeNode());
        }
      }
    }
  }

  private void createDateNodes() {
    /* We create new folders for each day and assign the program items
       to the appropriate folder */

    mDefaultNode.removeAllChildren();

    // return if no nodes available
    if (mChildNodes == null || mChildNodes.size() == 0) {
      return;
    }

    Map<Date, ArrayList<PluginTreeNode>> dateMap = new HashMap<Date, ArrayList<PluginTreeNode>>();  // key: date; value: ArrayList of ProgramItem objects
    Iterator<PluginTreeNode> it = mChildNodes.iterator();
    Date currentDate = Date.getCurrentDate();
    while (it.hasNext()) {
      PluginTreeNode n = it.next();
      if (!n.isLeaf()) {
        if (n.mGroupingByDate) {
          n.createDateNodes();
        }
        else {
          n.createDefaultNodes();
        }
        mDefaultNode.add(n.getMutableTreeNode());
      }
      else {
      	Date date = ((ProgramItem)n.getUserObject()).getProgram().getDate();
                
        if(date.addDays(1).compareTo(currentDate) >= 0) {
          ArrayList<PluginTreeNode> list = dateMap.get(date);
          if (list == null) {
            list = new ArrayList<PluginTreeNode>();
            dateMap.put(date, list);
          }
        
          list.add(n);
        }
      }
    }
    
    // Create the new nodes
    Set<Date> keySet = dateMap.keySet();
    Date[] dates = new Date[keySet.size()];
    keySet.toArray(dates);
    Arrays.sort(dates);
    Date today = currentDate;
    Date nextDay = today.addDays(1);
    Date yesterDay = today.addDays(-1);
    Node node=null;
    String lastDateStr="";
    int numPrograms = 0;
    for (Date date : dates) {
      numPrograms += dateMap.get(date).size();
    }
    // show week nodes if there are less than 2 programs per day on average
    boolean createWeekNodes = mGroupWeekly && (numPrograms <= dates.length * 2);
    boolean isShowingWeekNodes = createWeekNodes;
    
    for (int i=0; i<dates.length; i++) {
      String dateStr;
      if (yesterDay.equals(dates[i])) {
        dateStr = Localizer.getLocalization(Localizer.I18N_YESTERDAY);
        isShowingWeekNodes = false;
      }
      else if (today.equals(dates[i])) {
        dateStr = Localizer.getLocalization(Localizer.I18N_TODAY);
        isShowingWeekNodes = false;
      }
      else if (nextDay.equals(dates[i])) {
        dateStr = Localizer.getLocalization(Localizer.I18N_TOMORROW);
        isShowingWeekNodes = false;
      }
      else {
        if (createWeekNodes) {
          int weeks = dates[i].getNumberOfDaysSince(today) / 7;
          if (weeks <= 3) {
            dateStr = mLocalizer.msg("weeks."+weeks,"in {0} weeks",weeks);
          }
          else {
            dateStr = mLocalizer.msg("weeks.later","later");
          }
        }
        else {
          dateStr = dates[i].getLongDateString();
        }
      }
      if (!dateStr.equals(lastDateStr)) {
        node = new Node(Node.STRUCTURE_NODE, dateStr);
        mDefaultNode.add(node);
        lastDateStr = dateStr;
      }
      List<PluginTreeNode> list = dateMap.get(dates[i]);
      PluginTreeNode[] nodeArr = new PluginTreeNode[list.size()];
      list.toArray(nodeArr);
      Arrays.sort(nodeArr, sPluginTreeNodeComparator);
      for (int k=0; k<nodeArr.length; k++) {
      	Node newNode = new Node((ProgramItem)nodeArr[k].getUserObject());
        newNode.setNodeFormatter(nodeArr[k].getNodeFormatter(createWeekNodes && isShowingWeekNodes));
        node.add(newNode);
      }
      
      isShowingWeekNodes = createWeekNodes;
    }
  }

  private static Comparator<PluginTreeNode> sPluginTreeNodeComparator = new Comparator<PluginTreeNode>() {
    public int compare(PluginTreeNode n1, PluginTreeNode n2) {
        Object u1 = n1.getUserObject();
        Object u2 = n2.getUserObject();
        if (u1 instanceof ProgramItem && u2 instanceof ProgramItem) {
          return sProgramItemComparator.compare((ProgramItem)u1, (ProgramItem)u2);
        }
        if (u1 instanceof String && u2 instanceof String) {
          return ((String)u1).compareToIgnoreCase((String)u2);
        }
        if (u1 instanceof String) {
          return 1;
        }
        return -1;
    }
  };

  private static Comparator<ProgramItem> sProgramItemComparator = new Comparator<ProgramItem>(){
    public int compare(ProgramItem pi1, ProgramItem pi2) {
        Program p1 = pi1.getProgram();
        Program p2 = pi2.getProgram();
        int result = p1.getDate().compareTo(p2.getDate());
        if (result != 0) {
          return result;
        }
        int t1 = p1.getStartTime();
        int t2 = p2.getStartTime();
        if (t1 < t2) {
          return -1;
        }
        else if (t1 > t2) {
          return 1;
        }
		return 0;
    }
  };


  public Object getUserObject() {
    return mObject;
  }

  public synchronized void removeAllChildren() {
    if (mMarker != null) {
      Program[] programs = getPrograms();
      for (int i=0; i<programs.length; i++) {
        programs[i].unmark(mMarker);
      }
    }
    if (mChildNodes != null) {
      mChildNodes.clear();
      mChildNodes = null;
    }
    mDefaultNode.removeAllChildren();
  }


  public synchronized void add(PluginTreeNode node) {
    // create collection on demand only
    if (mChildNodes == null) {
      mChildNodes = new ArrayList<PluginTreeNode>(1);
    }
    mChildNodes.add(node);    
    node.mMarker = mMarker;
  }

  public boolean contains(Program prog, boolean recursive) {
    PluginTreeNode node = findProgramTreeNode(prog, recursive);
    return node != null;
  }

  public boolean contains(Program prog) {
    return contains(prog, false);
  }

  /**
   * Refreshes the tree. Call this method after you have added/removed/changed
   * nodes of the (sub-)tree
   */
  public synchronized void update() {

    if (mGroupingByDate) {
      createDateNodes();
    }
    else {
      createDefaultNodes();
    }

    PluginTreeModel.getInstance().reload(mDefaultNode);

  }

  public synchronized void addPrograms(List<Program> listNew) {
    Iterator<Program> newIt = listNew.iterator();
    // create sorted lists of current and new programs, but only if this node contains any children at all!
    if (mChildNodes != null && mChildNodes.size() > 0) {
      Program[] currentProgs = getPrograms();
      ArrayList<Program> listCurrent = new ArrayList<Program>(currentProgs.length);
      for (int i = 0; i < currentProgs.length; i++) {
        listCurrent.add(currentProgs[i]);
      }
      Comparator<Program> comp = ProgramUtilities.getProgramComparator();
      Collections.sort(listCurrent, comp);
      Collections.sort(listNew, comp);
      Iterator<Program> currentIt = listCurrent.iterator();
      
      // iterate both lists in parallel and add only new programs
      if (currentIt.hasNext() && newIt.hasNext()) {
        Program newProg = newIt.next();
        Program currentProg = currentIt.next();
        while (newProg != null && currentProg != null) {
          int comparison = comp.compare(newProg, currentProg);
          // new program is sorted first -> add it and investigate next new
          if (comparison < 0) {
            markAndAdd(newProg);
            if (newIt.hasNext()) {
              newProg = newIt.next();
            }
            else {
              newProg = null;
            }
          }
          // old program is sorted first -> go to next old program for comparison
          else if (comparison > 0) {
            if (currentIt.hasNext()) {
              currentProg = currentIt.next();
            }
            else {
              currentProg = null;
            }
          }
          // program already available -> skip
          else if (comparison == 0) {
            if (currentIt.hasNext()) {
              currentProg = currentIt.next();
            }
            else {
              currentProg = null;
            }
            if (newIt.hasNext()) {
              newProg = newIt.next();
            }
            else {
              newProg = null;
            }
          }
        }
      }
    }
    // add all remaining new programs
    while (newIt.hasNext()) {
      markAndAdd(newIt.next());
    }
  }

  private void markAndAdd(Program program) {
    if (mMarker != null) {
      program.mark(mMarker);
    }
    PluginTreeNode node = new PluginTreeNode(new ProgramItem(program));
    add(node);
  }

  /**
   * add a single program to this tree node.
   * If the program is already contained in this sub tree, it is not added again.
   * 
   * @param program
   * @return the tree node containing the program
   */
  public synchronized PluginTreeNode addProgram(Program program) {
    if (program == null) {
      return null;
    }
    // don't search using contains(), this would require a second search
    PluginTreeNode node = findProgramTreeNode(program, false);
    if (node != null) {
      // node already exists
      return node;
    }

    if (mMarker != null) {
      program.mark(mMarker);
    }
    node = new PluginTreeNode(new ProgramItem(program));
    add(node);
    return node;
  }

  /**
   * add a single program node to this tree node.
   * It is not checked if the program is already contained in this sub tree,
   * so you may only use this method with newly created tree nodes!
   * 
   * @param program
   * @return the tree node containing the program
   */
  public synchronized PluginTreeNode addProgramWithoutCheck(Program program) {
    if (mMarker != null) {
      program.mark(mMarker);
    }
    PluginTreeNode node = new PluginTreeNode(new ProgramItem(program));
    add(node);
    return node;
  }

  private PluginTreeNode findProgramTreeNode(PluginTreeNode root, Program prog, boolean recursive) {
    if (root.mChildNodes != null) {
      Iterator<PluginTreeNode> it = root.mChildNodes.iterator();
      while (it.hasNext()) {
        PluginTreeNode node = (PluginTreeNode)it.next();
        if (!node.isLeaf()) {
          if (recursive) {
            PluginTreeNode n = findProgramTreeNode(node, prog, recursive);
            if (n!=null) {
              return n;
            }
          }
        }
        else {
          ProgramItem item = (ProgramItem)node.getUserObject();
          if (item != null && prog.equals(item.getProgram())) {
            return node;
          }
        }
      }
    }
    return null;
  }

  private PluginTreeNode findProgramTreeNode(Program prog, boolean recursive) {
    return findProgramTreeNode(this, prog, recursive);
  }


  public synchronized void removeProgram(ProgramItem item) {
    removeProgram(item.getProgram());
  }

  public synchronized void removeProgram(Program program) {
    PluginTreeNode node = findProgramTreeNode(program, false);
    if (node != null) {
      mChildNodes.remove(node);
      if (mMarker != null) {
        program.unmark(mMarker);
    }
    }
  }

  public synchronized PluginTreeNode addNode(String title) {
    PluginTreeNode node = new PluginTreeNode(title);
    add(node);
    return node;
  }

  public ProgramItem[] getProgramItems() {
    // return if there are no child nodes
    if (mChildNodes == null) {
      return new ProgramItem[0];
    }
    
    // we have child nodes
    ArrayList<Object> list = new ArrayList<Object>();
    Iterator<PluginTreeNode> it = mChildNodes.iterator();
    while (it.hasNext()) {
      PluginTreeNode n = it.next();
      if (n.isLeaf()) {
        list.add(n.getUserObject());
      }
    }

    ProgramItem[] result = new ProgramItem[list.size()];
    list.toArray(result);
    return result;
  }

  public Program[] getPrograms() {
    // return if there are no children
    if (mChildNodes == null) {
      return new Program[0];
    }
    
    // we have child nodes
    ArrayList<Program> list = new ArrayList<Program>();
    Iterator<PluginTreeNode> it = mChildNodes.iterator();
    while (it.hasNext()) {
      PluginTreeNode n = it.next();
      if (n.isLeaf()) {
        ProgramItem item = (ProgramItem)n.getUserObject();
        list.add(item.getProgram());
      }
    }

    Program[] result = new Program[list.size()];
    list.toArray(result);
    return result;
  }


  public void store(ObjectOutputStream out) throws IOException {
    int childrenCnt = 0;
    if (mChildNodes != null) {
      childrenCnt = mChildNodes.size();
    }
    out.writeInt(childrenCnt);

    for (int i=0; i<childrenCnt; i++) {
      PluginTreeNode n = mChildNodes.get(i);
      out.writeInt(n.mNodeType);
      if (!n.isLeaf()) {
        String title = (String)n.getUserObject();
        out.writeObject(title);
      }
      else {
        ProgramItem item = (ProgramItem)n.getUserObject();
        item.write(out);
      }
      n.store(out);
    }
  }

  public void load(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int cnt = in.readInt();
    for (int i=0; i<cnt; i++) {
      int type = in.readInt();
      PluginTreeNode n;
      if (type == Node.PROGRAM) {
        ProgramItem item = new ProgramItem();
        item.read(in);

        n = findProgramTreeNode(item.getProgram(), true);
        if (n == null) {
          n = new PluginTreeNode(item);
          if (item.getProgram() != null) {
            add(n);
            if (mMarker != null) {
              item.getProgram().mark(mMarker);
            }
          }
        }

      }
      else {
        String title = (String)in.readObject();
        n = new PluginTreeNode(title);
        add(n);
      }
      n.load(in);

    }
  }


  /**
   * Get the number of child nodes.
   * 
   * @return number of child nodes
   */
  public int size() {
    if (mChildNodes == null) {
      return 0;
    }
    return mChildNodes.size();
  }

  public synchronized void clear() {
    if (mChildNodes == null) {
      return;
    }
    mChildNodes.clear();
    mChildNodes = null;
  }

  public boolean isEmpty() {
    return (mChildNodes == null || mChildNodes.isEmpty());
  }

  public boolean isLeaf() {
    return (mDefaultNode.getType() == Node.PROGRAM);
  }
  
  @Override
  public String toString() {
    switch (mNodeType) {
    case Node.PLUGIN_ROOT: {
      return "plugin node: " + mObject.toString();
    }
    case Node.PROGRAM: {
      return "program node: " + mObject.toString();
    }
    case Node.STRUCTURE_NODE: {
      return "structure node: " + mObject.toString();
    }
    case Node.CUSTOM_NODE: {
      return "custom node: " + mObject.toString();
    }
    }
    return super.toString();
  }

  public static class RemovedProgramsHandler {
    private ArrayList<Program> mProgArr;
    public RemovedProgramsHandler() {
      mProgArr = new ArrayList<Program>();
    }
    public void clear() {
      mProgArr.clear();
    }

    public void addRemovedProgram(Program prog) {
      mProgArr.add(prog);
    }

    public Program[] getRemovedPrograms() {
      Program[] progArr = new Program[mProgArr.size()];
      mProgArr.toArray(progArr);
      return progArr;
    }
  }

}
