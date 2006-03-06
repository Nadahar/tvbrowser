package justmark;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.Action;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Date;
import devplugin.NodeFormatter;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramItem;
import devplugin.Version;

/**
 * JustMark 0.6 Plugin for TV-Browser since version 2.0 to only mark programs
 * and add them to the Plugin tree.
 * 
 * License: GNU General Public License (GPL)
 * 
 * @author René Mach
 */
public class JustMark extends Plugin implements ActionListener {

  /** The localizer for this class. */
  public static util.ui.Localizer mLocalizer;

  private Vector mPrograms = new Vector();
  private Program mProg = null;
  private static JustMark mInstance;

  /**
   * Standard contructor for this class.
   */
  public JustMark() {
    mInstance = this;
    mLocalizer = util.ui.Localizer.getLocalizerFor(JustMark.class);
  }

  /**
   * @return The instance of this class.
   */
  public static JustMark getInstance() {
    return mInstance;
  }

  /** @return The Plugin Info. */
  public PluginInfo getInfo() {
    return (new PluginInfo("JustMark", "Simple Mark Plugin", "René Mach",
        new Version(0, 6, true), "GPL"));
  }

  /**
   * @return The MarkIcon.
   */
  protected String getMarkIconName() {
    return "justmark/kaddressbook2.png";
  }

  /**
   * @return The ActionMenu for this Plugin.
   */
  public ActionMenu getContextMenuActions(Program p) {
    if (!p.equals(getPluginManager().getExampleProgram()))
      if (p.isExpired())
        return null;

    this.mProg = p;

    // Create context menu entry
    ContextMenuAction menu = new ContextMenuAction();
    menu.setText(mLocalizer.msg("mark", "Just Mark"));
    if (mPrograms.contains(p))
      menu.setText(mLocalizer.msg("unmark", "Just Unmark"));
    menu.putValue(Action.ACTION_COMMAND_KEY,menu.getValue(Action.NAME));
    menu.setSmallIcon(createImageIcon("justmark/kaddressbook2.png"));
    menu.setActionListener(this);
    
    return new ActionMenu(menu);
  }

  public boolean canReceivePrograms() {
    return true;
  }

  public void receivePrograms(Program[] p) {
    for (int i = 0; i < p.length; i++) {
      if (mPrograms.contains(p[i]) || p[i].isExpired())
        continue;
      else {
        mPrograms.addElement(p[i]);
        p[i].mark(this);
      }
    }
    updateTree();
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(mLocalizer.msg("mark", "Just Mark"))) {
      mPrograms.addElement(mProg);
      mProg.mark(this);
      updateTree();
    } else if (e.getActionCommand().equals(
        mLocalizer.msg("unmark", "Just Mark"))) {
      mPrograms.removeElement(mProg);
      mProg.unmark(this);
      updateTree();
    }
  }

  public void readData(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    int version = in.readInt();
    if (version == 1) {
      int size = in.readInt();
      for (int i = 0; i < size; i++) {
        Date programDate = new Date(in);
        String progId = (String) in.readObject();

        Program program = Plugin.getPluginManager().getProgram(programDate,
            progId);

        // Only add items that were able to load their program
        if (program != null) {
          mPrograms.addElement(program);
        }
      }
      updateTree();
    }
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    out.writeInt(mPrograms.size());
    for (int i = 0; i < mPrograms.size(); i++) {
      Program p = (Program) mPrograms.elementAt(i);
      p.getDate().writeData(out);
      out.writeObject(p.getID());
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
        mLocalizer.msg("unmarkall", "Just Unmark All"))) {
      Program[] p = node.getPrograms();

      for (int i = 0; i < p.length; i++) {
        mPrograms.remove(p[i]);
        p[i].unmark(this);
      }
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

    PluginTreeNode pNode = node.addNode(mLocalizer.msg("programs", "Programs"));
    PluginTreeNode dNode = node.addNode(mLocalizer.msg("days", "Days"));
    pNode.setGroupingByDateEnabled(false);

    GroupUnmarkAction menu = new GroupUnmarkAction(dNode);
    menu.setSmallIcon(createImageIcon("justmark/kaddressbook2.png"));
    menu.setText(mLocalizer.msg("unmarkall", "Just Unmark All"));

    dNode.addAction(menu);
    Hashtable program = new Hashtable();

    for (int i = 0; i < mPrograms.size(); i++) {
      Program p = (Program) mPrograms.elementAt(i);
      if (p == null || p.isExpired())
        continue;
      if (!program.containsKey(p.getTitle())) {
        LinkedList list1 = new LinkedList();
        program.put(p.getTitle(), list1);
        list1.addFirst(p);
      } else {
        LinkedList list1 = (LinkedList) program.get(p.getTitle());
        list1.addLast(p);
      }
    }

    Enumeration en = program.keys();
    while (en.hasMoreElements()) {
      String name = (String) en.nextElement();
      LinkedList list1 = (LinkedList) program.get(name);
      PluginTreeNode curNode = pNode.addNode(name);

      // Create context menu entry
      menu = new GroupUnmarkAction(curNode);
      menu.setSmallIcon(createImageIcon("justmark/kaddressbook2.png"));
      menu.setText(mLocalizer.msg("unmarkall", "Just Unmark All"));

      curNode.addAction(menu);
      curNode.setGroupingByDateEnabled(false);
      Iterator it = list1.iterator();

      while (it.hasNext()) {
        Program p = (Program) it.next();

        PluginTreeNode prog = curNode.addProgram(p);
        prog.setNodeFormatter(new NodeFormatter() {
          public String format(ProgramItem pitem) {
            Program p = pitem.getProgram();
            Date d = p.getDate();
            String progdate;

            if (d.equals(Date.getCurrentDate()))
              progdate = mLocalizer.msg("today", "today");
            else if (d.equals(Date.getCurrentDate().addDays(1)))
              progdate = mLocalizer.msg("tomorrow", "tomorrow");
            else
              progdate = p.getDateString();

            return (progdate + "  " + p.getTimeString() + "  " + p.getChannel());
          }
        });
        dNode.addProgram(p);
      }
    }
    node.update();
  }
}
