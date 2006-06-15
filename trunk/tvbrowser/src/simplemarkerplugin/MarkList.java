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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import devplugin.Date;
import devplugin.NodeFormatter;
import devplugin.Plugin;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramItem;

/**
 * SimpleMarkerPlugin 1.4 Plugin for TV-Browser since version 2.3 to only mark
 * programs and add them to the Plugin tree.
 * 
 * (Formerly  known as Just_Mark ;-))
 * 
 * A class that is a list with marked programs.
 * 
 * @author Ren� Mach
 */
public class MarkList extends Vector<Program> {
  private static final long serialVersionUID = 1L;

  private String mName;
  private PluginTreeNode mRootNode;
  private Hashtable<String, LinkedList<Program>> mProgram = new Hashtable<String, LinkedList<Program>>();
  private Icon mMarkIcon;
  private String mMarkIconPath;

  /**
   * The constructor for a new list.
   * 
   * @param name
   *          The name of the list.
   */
  public MarkList(String name) {
    mName = name;
    mMarkIcon = SimpleMarkerPlugin.getInstance().getIconForFileName(null);
  }

  /**
   * @return The name of this list.
   */
  public String getName() {
    return mName;
  }

  /**
   * Set the name of this list.
   * 
   * @param name
   *          The name of this list.
   */
  public void setName(String name) {
    mName = name;
  }

  /**
   * Load the list entries from the data file.
   * 
   * @param in
   *          The ObjectInputStream of the data file.
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public MarkList(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    int version = in.readInt();

    if (version == 1) {
      mName = (String) in.readObject();

      SimpleMarkerPlugin.getInstance().removeListForName(mName);

      int size = in.readInt();
      for (int i = 0; i < size; i++) {
        Date programDate = new Date(in);
        String progId = (String) in.readObject();

        Program program = Plugin.getPluginManager().getProgram(programDate,
            progId);

        // Only add items that were able to load their program
        if (program != null && !program.isExpired()) {
          addElement(program);
          program.mark(SimpleMarkerPlugin.getInstance());
        }
      }

      mMarkIconPath = (String) in.readObject();

      if (mMarkIconPath == null || mMarkIconPath.compareTo("null") == 0
          || !(new File(mMarkIconPath)).isFile())
        mMarkIcon = SimpleMarkerPlugin.getInstance().getIconForFileName(null);
      else
        mMarkIcon = SimpleMarkerPlugin.getInstance().getIconForFileName(
            mMarkIconPath);
    }
  }

  /**
   * Write the list to the data file.
   * 
   * @param out
   *          The ObjectOutputStream of the data file.
   * @throws IOException
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // Version
    out.writeObject(mName);
    out.writeInt(size());

    for (Program p : this) {
      p.getDate().writeData(out);
      out.writeObject(p.getID());
    }

    out.writeObject(mMarkIconPath);
  }

  /**
   * 
   * @param p
   *          The Program.
   * @return The action for the Program for this List;
   */
  public Action getContextMenuAction(final Program p) {
    AbstractAction action = new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        if (contains(p)) {
          removeElement(p);
          SimpleMarkerPlugin.getInstance().revalidate(new Program[] {p});
        } else {
          addElement(p);
          p.mark(SimpleMarkerPlugin.getInstance());
          p.validateMarking();
        }
        createNodes(mRootNode, true);
        SimpleMarkerPlugin.getInstance().refreshPanels();
      }
    };

    if (contains(p)) {
      action.putValue(Action.NAME, SimpleMarkerPlugin.mLocalizer.msg(
          "list.unmark", "Unmark")
          + " " + getName());
      action.putValue(Action.SMALL_ICON, SimpleMarkerPlugin.getInstance()
          .createIconForTree(0));
    } else {
      action.putValue(Action.NAME, SimpleMarkerPlugin.mLocalizer.msg(
          "list.mark", "Mark")
          + " " + getName());
      action.putValue(Action.SMALL_ICON, mMarkIcon);
    }

    return action;
  }

  /**
   * Remove all programs of the list
   * 
   * @param node
   *          The parent node that contains the programs
   * 
   */
  public void handleAction(PluginTreeNode node) {
    Program[] programs = node.getPrograms();

    for (Program p : programs)
      remove(p);
    
    SimpleMarkerPlugin.getInstance().revalidate(programs);

    createNodes(mRootNode, true);
  }

  /**
   * Create the tree part of this list.
   * 
   * @param root
   *          The root node of this list.
   * @param update
   *          The tree is to update
   */
  public void createNodes(PluginTreeNode root, boolean update) {
    mRootNode = root;
    root.removeAllChildren();

    PluginTreeNode pNode = new PluginTreeNode(SimpleMarkerPlugin.mLocalizer
        .msg("programs", "Programs"));
    PluginTreeNode dNode = new PluginTreeNode(SimpleMarkerPlugin.mLocalizer
        .msg("days", "Days"));
    pNode.setGroupingByDateEnabled(false);

    GroupUnmarkAction menu = new GroupUnmarkAction(dNode, this);
    menu.setSmallIcon(SimpleMarkerPlugin.getInstance().createIconForTree(1));
    menu.setText(SimpleMarkerPlugin.mLocalizer.msg("unmarkall",
        "Just unmark all"));

    dNode.addAction(menu);
    updateTable();
    Hashtable<String, LinkedList<Program>> program = getSortedPrograms();

    for (String name : program.keySet()) {
      LinkedList<Program> list1 = program.get(name);
      PluginTreeNode curNode = pNode.addNode(name);

      // Create context menu entry
      menu = new GroupUnmarkAction(curNode, this);
      menu.setSmallIcon(SimpleMarkerPlugin.getInstance().createIconForTree(1));
      menu.setText(SimpleMarkerPlugin.mLocalizer.msg("unmarkall",
          "Just unmark all"));

      curNode.addAction(menu);
      curNode.setGroupingByDateEnabled(false);

      for (Program p : list1) {
        PluginTreeNode prog = curNode.addProgram(p);
        prog.setNodeFormatter(new NodeFormatter() {
          public String format(ProgramItem pitem) {
            Program p = pitem.getProgram();
            Date d = p.getDate();
            String progdate;

            if (d.equals(Date.getCurrentDate()))
              progdate = SimpleMarkerPlugin.mLocalizer.msg("today", "today");
            else if (d.equals(Date.getCurrentDate().addDays(1)))
              progdate = SimpleMarkerPlugin.mLocalizer.msg("tomorrow",
                  "tomorrow");
            else
              progdate = p.getDateString();

            return (progdate + "  " + p.getTimeString() + "  " + p.getChannel());
          }
        });
        dNode.addProgram(p);
      }
    }
    root.add(pNode);
    root.add(dNode);

    if (update)
      root.update();
  }

  protected void updateNode() {
    if(mRootNode != null)
      createNodes(mRootNode, true);
  }

  protected void revalidateContainingPrograms() {
    for (int i = size() - 1; i >= 0; i--) {
      Program containingProgram = remove(i);

      if (containingProgram.getProgramState() == Program.WAS_UPDATED_STATE) {
        Program updatedProg = SimpleMarkerPlugin.getPluginManager().getProgram(
            containingProgram.getDate(), containingProgram.getID());
        addElement(updatedProg);
      } else if (containingProgram.getProgramState() == Program.IS_VALID_STATE)
        addElement(containingProgram);
    }
    updateNode();
  }

  private void updateTable() {
    mProgram.clear();
    for (int i = 0; i < size(); i++) {
      Program p = (Program) elementAt(i);
      if (p == null || p.isExpired())
        continue;
      if (!mProgram.containsKey(p.getTitle())) {
        LinkedList<Program> list1 = new LinkedList<Program>();
        mProgram.put(p.getTitle(), list1);
        list1.addFirst(p);
      } else {
        LinkedList<Program> list1 = mProgram.get(p.getTitle());
        list1.addLast(p);
      }
    }
  }

  protected void setMarkIconFileName(String fileName) {
    mMarkIconPath = fileName;
    mMarkIcon = SimpleMarkerPlugin.getInstance().getIconForFileName(fileName);
  }

  protected Icon getMarkIcon() {
    return mMarkIcon;
  }
  
  protected String getMarkIconPath() {
    return mMarkIconPath;
  }

  /**
   * @return The table with the programs.
   */
  public Hashtable<String, LinkedList<Program>> getSortedPrograms() {
    return mProgram;
  }

  /**
   * @param title
   *          The title of the Programs to remove
   */
  public void removeProgramsWithTitle(String title) {
    LinkedList<Program> list = mProgram.remove(title);
    Program[] programs = list.toArray(new Program[list.size()]);
    
    for (Program p : programs)
      remove(p);
    
    SimpleMarkerPlugin.getInstance().revalidate(programs);
  }  
}
