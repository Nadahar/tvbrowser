package treeviewplugin;


/**
 * Represents a genre node
 * @author matthias
 *
 */
public class GenreNode extends TreeViewNode  implements Comparable<GenreNode>{

	private static final long serialVersionUID = 1L;
	private String name=new String();
    
    /** 
     * @see TreeViewNode 
     * @param name
     */
	public GenreNode(String name,TreeViewPlugin plugin){
		super(name,plugin);
		this.name=name;
	}
	
    /**
     * Compare operator to sort nodes in alphabetic order.
     * @param o object to compare with.
     * @return compare value
     */
	public int compareTo(GenreNode o){
		return name.compareTo(o.name);
	}
	
	protected String getPluginTreeDescription(){
		String genreDesc=toString();
		int idx=genreDesc.lastIndexOf(" (");
		if(idx>0)genreDesc=genreDesc.substring(0,idx);
		return genreDesc;
	}

}
