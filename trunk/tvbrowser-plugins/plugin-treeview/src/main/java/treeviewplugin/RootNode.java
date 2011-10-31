package treeviewplugin;


/**
 * Root node of broadcastTree
 * 
 * @author matthias
 * 
 */

public class RootNode extends TreeViewNode {

    private static final long serialVersionUID = 1L;

    /**
     * @see TreeViewNode
     * @param name
     */
    public RootNode(String name,TreeViewPlugin plugin) {
        super(name,plugin);
    }

    /**
     * Returns the nodes "on air" state.
     * 
     * @return false because the root node should never be marked
     */
    public boolean isMarkedOnAir() {
        return false;
    }
    
     /**
     * Returns the nodes "marked by other plugin" state.
     * 
     * @return false because the root node should never be marked
     */
    public boolean isMarkedByPlugin(){
    	return false;
    }
    
    /**
     * Returns the nodes "next on air" state.
     * 
     * @return false because the root node should never be marked
     */
	public boolean isNextOnAir(){
		return false;
	}
}
