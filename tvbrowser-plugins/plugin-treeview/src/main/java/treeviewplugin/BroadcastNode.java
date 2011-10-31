package treeviewplugin;



/**
 * Represents a broadcast node. 
 * @author matthias
 *
 */
public class BroadcastNode extends TreeViewNode implements Comparable<BroadcastNode>{

	private static final long serialVersionUID = 1L;
	private String name=new String();

     /** 
     * @see TreeViewNode 
     * @param name
     */
	public BroadcastNode(String name,TreeViewPlugin plugin){
		super(name,plugin);
		this.name=name;
	}
	
	/**
	 * Compare operator to sort nodes in alphabetic order.
	 * @param o object to compare with.
	 * @return compare value
	 */
	public int compareTo(BroadcastNode o){
		return name.compareToIgnoreCase(o.name);
	}
		

}
