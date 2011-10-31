package treeviewplugin;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;


/**
 * Implements search algorithm.
 * @author matthias.amrhein
 *
 */
public class SearchEngine {

	/** Translator * */
	private static final util.ui.Localizer mLocalizer = util.ui.Localizer
	.getLocalizerFor(SearchEngine.class);
    /** Localized string */
	private static String noSearchResults = mLocalizer.msg("NoSearchResults",
	"No search results.");

    /** Localized string */
	private static String highMemory = mLocalizer.msg("HighMemory",
	"Your search was aborted due to high memory consumption. Try to reduce selected days or channels.");
	
    /** Localized string */
	private static String messageTitle = mLocalizer.msg("MessageTitle",
	"Info");

	private TreeViewNode lastSearchPosition=null;
	private String lastSearchText=null;
	private TreeViewPlugin plugin=null;
	private JTree broadcastTree=null;


	/** 
     * New search engine.
     * @param p TreeView plugin instance
     * @param tree Tree that should be searched for.
	 */
	public SearchEngine(TreeViewPlugin p,JTree tree) {
		plugin=p;
		broadcastTree=tree;
	}

    /**
     * Reset search states of engine and removes search attributes of all tree nodes.    
     */
	protected void resetSearchState(){
		lastSearchPosition=null;
		lastSearchText=null;
		TreeViewNode node=(TreeViewNode)(broadcastTree.getPathForRow(0).getPath()[0]);
		do{
			node.resetSearchState();
			node=(TreeViewNode)node.getNextNode();	       
		}while(node!=null);
	}

	/**
     * Performs a new search. The search starts from last search position if the search text has not changed.
     * If a matching node is found the tree will be scrolled to this node. If no search result was found 
     * a message is displayed. There is also a message displayed if the search was aborted due to high memory
     * consumption.
     * @param searchText Case insensitiv search text.
	 */
	protected void startSearch(String searchText){	
		try{
			searchText=searchText.toLowerCase();
			TreeViewNode searchPos=null;
			if((lastSearchPosition!=null)&&(lastSearchText.compareTo(searchText)==0)){
				TreeViewNode nextNode=(TreeViewNode)lastSearchPosition.getNextNode();
				if(nextNode!=null)searchPos=nextNode;			
			}else{
				resetSearchState();
				searchPos=(TreeViewNode)(broadcastTree.getPathForRow(0).getPath()[0]);
			}
			
			lastSearchText=searchText;
			TreePath path=search(searchText,searchPos);
			if(path==null){
				// not found, display message
				JOptionPane.showMessageDialog(plugin.getDialog(),noSearchResults,messageTitle,JOptionPane.INFORMATION_MESSAGE);
				
			}else{
				lastSearchPosition=(TreeViewNode)path.getLastPathComponent();
				lastSearchPosition.buildSearchDesc(plugin, searchText);			
				// Scroll to the found element
				if(path.getParentPath()!=null){
					broadcastTree.expandPath(path.getParentPath());
					broadcastTree.scrollRowToVisible(broadcastTree.getRowForPath(path));			
				}
			}
			System.gc();
		}catch(Exception e){				
			try{				
				plugin.log.addStackTrace(e);
			}catch(Exception n){
				n.printStackTrace();
			}
			e.printStackTrace();
		}
	}

    /**
     * Do a case insenstive search in the tree nodes.
     * @param content string to be searched for.
     * @param startNode Node to start with.
     * @return Path with matching node or null if no node could be found.
     */
	private TreePath search(String content,TreeViewNode startNode){
		try{
			int count=0;
			TreeViewNode node=startNode;
			do{

				String hay=node.getDesc(plugin);
				if(hay==null)hay=(String)node.getUserObject();
				hay=hay.toLowerCase();
				if(hay.indexOf(content)>=0){
					return new TreePath(node.getPath());              
				}
				if(count%50==0){

					if(!Tools.isEnoughFreeMemory(plugin.log)){
						JOptionPane.showMessageDialog(plugin.getDialog(),Tools.stringToHtmlDoc(highMemory,120),messageTitle,JOptionPane.INFORMATION_MESSAGE);
						return null;
					}
				}
				node=(TreeViewNode)node.getNextNode();
				count++;
			}while(node!=null);
		}catch(Exception e){				
			try{				
				plugin.log.addStackTrace(e);
			}catch(Exception n){
				n.printStackTrace();
			}
			e.printStackTrace();
		}
		return null;	      
	}


}
