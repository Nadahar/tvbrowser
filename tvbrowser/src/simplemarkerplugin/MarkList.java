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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import util.io.IOUtilities;
import util.program.ProgramUtilities;
import util.ui.Localizer;
import devplugin.Date;
import devplugin.NodeFormatter;
import devplugin.Plugin;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramItem;
import devplugin.ProgramReceiveTarget;

/**
 * SimpleMarkerPlugin 1.4 Plugin for TV-Browser since version 2.3 to only mark
 * programs and add them to the Plugin tree.
 * 
 * (Formerly known as Just_Mark ;-))
 * 
 * A class that is a list with marked programs.
 * 
 * @author René Mach
 */
public class MarkList extends Vector<Program> {
  private static final long serialVersionUID = 1L;

  private String mName;
  private String mId;
  transient private PluginTreeNode mRootNode;
  private Hashtable<String, LinkedList<Program>> mProgram = new Hashtable<String, LinkedList<Program>>();
  private Icon mMarkIcon;
  private String mMarkIconPath;
  private int mMarkPriority;

  /**
   * The constructor for a new list.
   * 
   * @param name
   *          The name of the list.
   */
  public MarkList(String name) {
    mName = name;
    mMarkIcon = SimpleMarkerPlugin.getInstance().getIconForFileName(null);
    mId = name + System.currentTimeMillis();
    mMarkPriority = Program.MIN_MARK_PRIORITY;
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

    if (version >= 3)
      mMarkPriority = in.readInt();

    if (version >= 1) {
      mName = (String) in.readObject();
      mId = in.readUTF();

      SimpleMarkerPlugin.getInstance().removeListForName(mName);
      SimpleMarkerPlugin.getInstance().addList(this);

      int size = in.readInt();
      for (int i = 0; i < size; i++) {
        Date programDate = new Date(in);
        String progId = (String) in.readObject();

        Program program = Plugin.getPluginManager().getProgram(programDate,
            progId);

        // Only add items that were able to load their program
        if (program != null) {
          addElement(program);
          program.mark(SimpleMarkerPlugin.getInstance());
          program.validateMarking();
        }
      }

      mMarkIconPath = (String) in.readObject();

      if (mMarkIconPath == null
          || mMarkIconPath.compareTo("null") == 0
          || (version > 3 && !(new File(SimpleMarkerPlugin.getPluginManager()
              .getTvBrowserSettings().getTvBrowserUserHome()
              + mMarkIconPath)).isFile()))
        mMarkIcon = SimpleMarkerPlugin.getInstance().getIconForFileName(null);
      else {

        if (version == 1) {
          File dir = new File(SimpleMarkerPlugin.getPluginManager()
              .getTvBrowserSettings().getTvBrowserUserHome(),
              "simplemarkericons");
          File src = new File(mMarkIconPath);

          if (!dir.isDirectory())
            dir.mkdir();

          String ext = mMarkIconPath;
          ext = ext.substring(ext.lastIndexOf('.'));

          try {
            IOUtilities.copy(src, new File(dir, mName + ext));
          } catch (Exception e) {
            e.printStackTrace();
          }

          mMarkIconPath = dir + "/" + mName + ext;
        }

        if (version <= 3) {
          mMarkIconPath = mMarkIconPath.substring(mMarkIconPath
              .lastIndexOf("simplemarkericons") - 1);

          if (!new File(SimpleMarkerPlugin.getPluginManager()
              .getTvBrowserSettings().getTvBrowserUserHome()
              + mMarkIconPath).isFile()) {
            mMarkIcon = SimpleMarkerPlugin.getInstance().getIconForFileName(
                null);
            return;
          }
        }

        mMarkIcon = SimpleMarkerPlugin.getInstance().getIconForFileName(
            SimpleMarkerPlugin.getPluginManager().getTvBrowserSettings()
                .getTvBrowserUserHome()
                + mMarkIconPath);
      }
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
    out.writeInt(4); // Version
    out.writeInt(mMarkPriority);
    out.writeObject(mName);
    out.writeUTF(mId);
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
          SimpleMarkerPlugin.getInstance().revalidate(new Program[] { p });
        } else {
          addElement(p);
          p.mark(SimpleMarkerPlugin.getInstance());
          p.validateMarking();
        }
        createNodes(mRootNode, true);
        SimpleMarkerPlugin.getInstance().refreshManagePanel(false);
        SimpleMarkerPlugin.getInstance().save();
      }
    };

    if (contains(p)) {
      action.putValue(Action.NAME, SimpleMarkerPlugin.mLocalizer.msg(
          "list.unmark", "Remove program from '{0}'", getName()));
      action.putValue(Action.SMALL_ICON, SimpleMarkerPlugin.getInstance()
          .createIconForTree(0));
    } else {
      action.putValue(Action.NAME, SimpleMarkerPlugin.mLocalizer.msg(
          "list.mark", "Add program to '{0}'", getName()));
      action.putValue(Action.SMALL_ICON, mMarkIcon);
      action.putValue(Program.MARK_PRIORITY, getMarkPriority());
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
    if (node == null) {
      return;
    }
    Program[] programs = node.getPrograms();

    for (Program p : programs) {
      remove(p);
    }

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

    final SimpleMarkerSettings settings = SimpleMarkerPlugin.getInstance()
        .getSettings();
    PluginTreeNode programRoot = null;
    PluginTreeNode dateRoot = null;
    if (settings.isGroupingByBoth()) {
      programRoot = new PluginTreeNode(Localizer
          .getLocalization(Localizer.I18N_PROGRAMS));
      programRoot.setGroupingByDateEnabled(false);
      dateRoot = new PluginTreeNode(SimpleMarkerPlugin.mLocalizer.msg("days",
          "Days"));
    }

    updateTable();
    Hashtable<String, LinkedList<Program>> sortedPrograms = getSortedPrograms();

    if (!sortedPrograms.isEmpty()) {
      if (settings.isGroupingByBoth()) {
        dateRoot.addAction(getUnmarkAction(dateRoot));
        programRoot.addAction(getUnmarkAction(programRoot));
      }

      root.removeAllActions();
      root.addAction(getUnmarkAction(root));

      final Date currentDate = Date.getCurrentDate();
      for (Entry<String, LinkedList<Program>> entry : sortedPrograms.entrySet()) {
        String name = entry.getKey();
        LinkedList<Program> sameTitlePrograms = entry.getValue();

        PluginTreeNode titleNode = null;
        if (settings.isGroupingByBoth()) {
          titleNode = programRoot.addNode(name);
        } else if (settings.isGroupingByTitle()) {
          titleNode = root.addNode(name);
        }
        if (titleNode != null) {
          titleNode.addAction(getUnmarkAction(titleNode));
          titleNode.setGroupingByDateEnabled(false);
        }

        for (Program program : sameTitlePrograms) {
          if (titleNode != null) {
            PluginTreeNode prog = titleNode.addProgram(program);
            prog.setNodeFormatter(new NodeFormatter() {
              public String format(ProgramItem pitem) {
                Program p = pitem.getProgram();
                Date progDate = p.getDate();
                String progdate;

                if (progDate.equals(currentDate.addDays(-1))) {
                  progdate = Localizer
                      .getLocalization(Localizer.I18N_YESTERDAY);
                } else if (progDate.equals(currentDate)) {
                  progdate = Localizer.getLocalization(Localizer.I18N_TODAY);
                } else if (progDate.equals(currentDate.addDays(1))) {
                  progdate = Localizer.getLocalization(Localizer.I18N_TOMORROW);
                } else {
                  progdate = p.getDateString();
                }

                return (progdate + "  " + p.getTimeString() + "  " + p
                    .getChannel());
              }
            });
          }
          if (settings.isGroupingByBoth()) {
            dateRoot.addProgram(program);
          } else if (settings.isGroupingByDate()) {
            root.addProgram(program);
          }
        }
      }
      if (settings.isGroupingByBoth()) {
        root.add(programRoot);
        root.add(dateRoot);
      }

      if (update) {
        root.update();
      }
    }
    SimpleMarkerPlugin.getInstance().addGroupingActions(root);
  }

  private GroupUnmarkAction getUnmarkAction(PluginTreeNode dateNode) {
    GroupUnmarkAction menu = new GroupUnmarkAction(dateNode, this);
    menu.setSmallIcon(SimpleMarkerPlugin.getInstance().createIconForTree(1));
    menu.setText(SimpleMarkerPlugin.mLocalizer.msg("unmarkall",
        "Just unmark all"));
    return menu;
  }

  protected void updateNode() {
    if (mRootNode != null)
      createNodes(mRootNode, true);
  }

  protected void revalidateContainingPrograms(ArrayList<Program> deletedPrograms) {
    for (int i = size() - 1; i >= 0; i--) {
      Program containingProgram = remove(i);

      if (containingProgram.getProgramState() == Program.WAS_UPDATED_STATE) {
        Program updatedProg = SimpleMarkerPlugin.getPluginManager().getProgram(
            containingProgram.getDate(), containingProgram.getID());
        addElement(updatedProg);
      } else if (containingProgram.getProgramState() == Program.IS_VALID_STATE) {
        addElement(containingProgram);
      } else if (containingProgram.getDate().compareTo(Date.getCurrentDate()) >= 0
          && containingProgram.getProgramState() == Program.WAS_DELETED_STATE
          && !deletedPrograms.contains(containingProgram)) {
        deletedPrograms.add(containingProgram);
      }
    }
    updateNode();
  }

  private void updateTable() {
    mProgram.clear();
    for (int i = 0; i < size(); i++) {
      Program p = elementAt(i);
      if (p == null)
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
    mMarkIconPath = "/simplemarkericons/" + fileName;
    mMarkIcon = SimpleMarkerPlugin.getInstance().getIconForFileName(
        SimpleMarkerPlugin.getPluginManager().getTvBrowserSettings()
            .getTvBrowserUserHome()
            + mMarkIconPath);
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
   * Return the programs with the given title.
   * 
   * @param title
   *          The title of the programs to get.
   * @return The program with the given title.
   */
  public Program[] getProgramsWithTitle(String title) {
    LinkedList<Program> list = mProgram.get(title);
    return (list.toArray(new Program[list.size()]));
  }

  /**
   * Removes the programs with the given title.
   * 
   * @param title
   *          The title of the Programs to remove
   * @return The programs that were deleted.
   */
  public Program[] removeProgramsWithTitle(String title) {
    LinkedList<Program> list = mProgram.remove(title);
    Program[] programs = list.toArray(new Program[list.size()]);

    for (Program p : programs)
      remove(p);

    SimpleMarkerPlugin.getInstance().revalidate(programs);

    return programs;
  }

  /**
   * @return The ProgramReceiveTarget of this list.
   */
  public ProgramReceiveTarget getReceiveTarget() {
    return new ProgramReceiveTarget(SimpleMarkerPlugin.getInstance(),
        SimpleMarkerPlugin.mLocalizer.msg("list.export", "Add to '{0}'", mName),
        mId);
  }

  /**
   * Gets the id String of this MarkList
   * 
   * @return The id String of this MarkList.
   */
  public String getId() {
    return mId;
  }

  public String toString() {
    return mName;
  }

  public void addElement(Program p) {
    if (isEmpty())
      super.addElement(p);
    else {
      int index = 0;
      Comparator<Program> c = ProgramUtilities.getProgramComparator();

      for (int i = 0; i < size(); i++) {
        int value = c.compare(elementAt(i), p);
        if (value < 1)
          index = i + 1;
        else
          break;
      }

      insertElementAt(p, index);
    }
  }

  /**
   * Gets the mark priority of this list.
   * 
   * @return The mark priority of this list.
   */
  public int getMarkPriority() {
    return mMarkPriority;
  }

  /**
   * Sets the mark priority of this list.
   * 
   * @param value
   *          The new mark priority.
   */
  public void setMarkPriority(int value) {
    mMarkPriority = value;
  }
}
