package just_mark;

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
 * Just_Mark 0.2beta Plugin for TV-Browser 1.1 to
 * only mark programs and add them to the Plugin tree.
 * 
 * @author Ren� Mach
 */
public class Just_Mark extends Plugin implements ActionListener/*,Runnable*/{

  /** The localizer for this class. */
  public static util.ui.Localizer mLocalizer;
  
  private Vector mPrograms = new Vector();
	private Program mProg = null;
	private String mActionCommand = null;
	private static Just_Mark mInstance; 
	
  /**
   * Standard contructor for this class. */
	public Just_Mark() {
		mInstance = this;
		mLocalizer = util.ui.Localizer.getLocalizerFor(Just_Mark.class);
	}
	
  /**
   * @return The instance of this class.
   */
	public static Just_Mark getInstance() {
		return mInstance;
	}
	
  /** @return The Plugin Info. */
  public PluginInfo getInfo() {   
    return (new PluginInfo("Just_Mark", "Simple Mark Plguin", "Ren� Mach", new Version(0,
        2, false), "GPL"));
  }
  
	/** 
   * @return The MarkIcon. */
	protected String getMarkIconName() {
		return "just_mark/kaddressbook2.png";
	}
	
  /**
   * @return The ActionMenu for this Plugin.
   */
	public ActionMenu getContextMenuActions(Program p) {		
		if(!p.equals(getPluginManager().getExampleProgram()))
			if(p.isExpired())
				return null;
		
		this.mProg = p;
		
		// Create context menu entry
		ContextMenuAction menu = new ContextMenuAction();		
		String text = "Just Mark";
		if(mPrograms.contains(p))
			text = "Just Unmark";
		mActionCommand = text;
		menu.setSmallIcon(createImageIcon("just_mark/kaddressbook2.png"));
		menu.setText(text);		
		menu.setActionListener(this);

		return new ActionMenu(menu);
	}
	
	public boolean canReceivePrograms() {
		return true;
	}

	public void receivePrograms(Program[] p) {
		for (int i = 0; i < p.length; i++) {
			if(mPrograms.contains(p[i]))
				continue;
			else {
				mPrograms.addElement(p[i]);
				p[i].mark(this);
			}
		}
		updateTree();
	}
	
	public void actionPerformed(ActionEvent e) {
		if(mActionCommand != null && mActionCommand.equals("Just Mark")) {
			mPrograms.addElement(mProg);
			mProg.mark(this);
			updateTree();
		}
		else if(mActionCommand != null && mActionCommand.equals("Just Unmark")) {
			mPrograms.removeElement(mProg);			
			mProg.unmark(this);
			updateTree();
		}
	}
	
	public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {				
		int size = in.readInt();
		for(int i = 0; i < size; i++) {
			String date = (String) in.readObject();
			int year = Integer.parseInt(date.substring(0,4));
			int month = Integer.parseInt(date.substring(4,6));
			int day = Integer.parseInt(date.substring(6));
			
			Date progDate = new Date(year,month,day);
			String progId = (String) in.readObject();
			
			Program program = Plugin.getPluginManager().getProgram(progDate, progId);

      // Only add items that were able to load their program
      if (program != null) {
        mPrograms.addElement(program);
      }
		}
    updateTree();
	}

	public void writeData(ObjectOutputStream out) throws IOException {		
		out.writeInt(mPrograms.size());
		for(int i = 0; i < mPrograms.size(); i++) {
			Program p = (Program)mPrograms.elementAt(i);
			out.writeObject(p.getDate().getValue() + "");
			out.writeObject(p.getID());
		}
	}
	
	public boolean canUseProgramTree() {
		return true;
	}
	
  /**
   * Updates the plugin tree.
   */
	public void updateTree() {
		PluginTreeNode node = getRootNode();
		node.removeAllActions();
		node.removeAllChildren();

		PluginTreeNode pNode = node.addNode(mLocalizer.msg("programs", "Programs"));
		PluginTreeNode dNode = node.addNode(mLocalizer.msg("days", "Days"));
		pNode.setGroupingByDateEnabled(false);
		
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
