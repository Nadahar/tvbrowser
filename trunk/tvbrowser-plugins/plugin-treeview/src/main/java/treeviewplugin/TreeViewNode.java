package treeviewplugin;

import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;


import devplugin.Plugin;
import devplugin.Program;

/**
 * Parent class of all node objects.
 * @author matthias
 *
 */
public class TreeViewNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;
    /** True if node should be marked as "is on air" */
	private boolean isOnAir=false;
	/** True if marked by an plugin*/
	protected boolean isMarkedByPlugin=false;
     /** True if node should be marked as "is next on air" */
	private boolean isNextOnAir=false;
	/** Reference to plugin */
	protected TreeViewPlugin plugin=null;
	protected String desc=null;
	

	/**
	 * Constructs a node object.
	 * @param name Name that is shown in the broadcast tree
	 */
	public TreeViewNode(String name,TreeViewPlugin plugin){
		super(name);
		isOnAir=false;
		isMarkedByPlugin=false;
		isNextOnAir=false;
		this.plugin=plugin;
	}
    /**
     * Sets the marker states based on child node states and calles parent node to do the same.
     *
     */
    public void walkUpToRootAndSetStates(){    	
        //Check children states
        if(!isLeaf()){ // Is this node a leaf(Senddatenode? So we need no child check
            // It is not a leaf, so we have to check the childs
            boolean newIsOnAir=false;
            boolean newIsMarkedByPlugin=false;
            boolean newIsNextOnAir=false;
            int numChilds=getChildCount();        
            for(int i=0;i<numChilds;i++){
                TreeViewNode child=(TreeViewNode)getChildAt(i);
                if(child.isMarkedByPlugin)newIsMarkedByPlugin=true;
                if(child.isNextOnAir)newIsNextOnAir=true;
                if(child.isOnAir)newIsOnAir=true;
            }
            isMarkedByPlugin=newIsMarkedByPlugin;
            isNextOnAir=newIsNextOnAir;
            isOnAir=newIsOnAir;
            
        }
        if(plugin!=null)plugin.getDialog().treeNodeChanged(this);        
        TreeViewNode node=(TreeViewNode)getParent();
        if(node==null)return; // No parent, must be root node
        // Ask parent to do the same check
        node.walkUpToRootAndSetStates();        
    }
	
	/**
	 * Handels the clicked event. Methode is doing nothing until it is overwritten.
	 * @param evt Incoming event
	 * @param plugin Reference to owning plugin
	 * @param tree Reference to owning Swing component
	 */
	public void clicked(MouseEvent evt,Plugin plugin,JComponent tree){
	}
	
	/**
	 * Returns the nodes "on air" state.
	 * @return true if on air
	 */
	public boolean isMarkedOnAir(){
		return isOnAir;
	}
    
    /**
     * Returns the nodes "next on air" state.
     * @return true if next on air
     */
	public boolean isNextOnAir(){
		return isNextOnAir;
	}
	
    /**
     * Returns the nodes "marked by plugin " state.
     * @return true if marked by plugin
     */
	public boolean isMarkedByPlugin(){
		return isMarkedByPlugin;
	}
	
	/**
	 * Sets the marked on air state of the node. If state is already "true" it is not reseted to "false".
	 * This method calls the same method of the parent node. 
	 * @param i new state
	 */
	public void setIsMarkedOnAir(boolean i){
		if(isOnAir==false)isOnAir=i;
		TreeViewNode parent=((TreeViewNode)getParent());
		if(parent!=null)parent.setIsMarkedOnAir(i);
	}

    /**
     * Sets the marked by plugin state of the node. If state is already "true" it is not reseted to "false".
     * This methode calls the same methode of the parent node. 
     * @param i new state
     */
	public void setIsMarkedByPlugin(boolean i){		
		if(isMarkedByPlugin==false)isMarkedByPlugin=i;
		TreeViewNode parent=((TreeViewNode)getParent());
		if(parent!=null)parent.setIsMarkedByPlugin(i);						
	}
		
    /**
     * Sets the next on air state of the node. If state is already "true" it is not reseted to "false".
     * This methode calls the same methode of the parent node. 
     * @param i new state
     */
	public void setIsNextOnAir(boolean i){
		if(isNextOnAir==false)isNextOnAir=i;
		TreeViewNode parent=((TreeViewNode)getParent());
		if(parent!=null)parent.setIsNextOnAir(i);
	}
	
	/**
	 * Returns the set icon
	 * @return null
	 */
	public Icon getIcon(){
		return null;
	}
    
    /**
     * Resets the highlighted search description     
     */
    protected void resetSearchState(){
        desc=null;
    }
	
	/**
	 * Returns the node desctiption. 
	 * @see SenddateNode.java
	 * @return null
	 */
	public String getDesc(TreeViewPlugin plugin){
		if(desc!=null)return desc;
		return null;
	}
    
	/**
     *  Unregisters the change listener of all childs.      
	 */
	public void unregisterChangeListener(){
		int childCount=getChildCount();
		for(int i=0;i<childCount;i++){
			((TreeViewNode)getChildAt(i)).unregisterChangeListener();
		}
	}
	
	public Program getProgram(){
		return null;
	}
	
	/**
	 * Returns true if this node should have a border
	 * @return false
	 */
	public boolean useBorder(){
		return false;
	}
	
    /**
     * Creates a description string that highlights the given search words.
     * @param plugin Reference to TreeViewPlugin. 
     * @param search item.
     */
	protected void buildSearchDesc(TreeViewPlugin plugin,String search){
		try{
			String d=getDesc(plugin);
			StringBuffer buffer=null;
			if(d==null){
				buffer=new StringBuffer();
				buffer.append("<html><body>");
				buffer.append((String)getUserObject());
				buffer.append("</body></html>");
			}else{
				buffer=new StringBuffer(d);
			}
			String divColorOn="<font bgcolor=\"#FFF570\">";
			String lowHay=buffer.toString().toLowerCase();
			int index=0;		
			while((index=lowHay.indexOf(search,index))>=0){
				// Check wether it is in an html tag
				int closingTag=lowHay.lastIndexOf(">",index);
				int openingTag=lowHay.lastIndexOf("<",index);
				if(openingTag>closingTag){
					index+=search.length();
					continue;
				}
				buffer.insert(index,divColorOn);
				index=index+divColorOn.length()+search.length();
				buffer.insert(index,"</font>");
				index+=6;
				lowHay=buffer.toString().toLowerCase();
			}
			
			desc=buffer.toString();			
		}catch(Exception e){				
			try{				
				plugin.log.addStackTrace(e);
			}catch(Exception n){
				n.printStackTrace();
			}
			e.printStackTrace();
		}
	}	
	
	protected String getPluginTreeDescription(){
		return toString();
	}
}
