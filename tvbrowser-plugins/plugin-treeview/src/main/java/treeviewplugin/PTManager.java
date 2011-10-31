package treeviewplugin;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Vector;

import devplugin.Channel;
import devplugin.Plugin;
import devplugin.PluginTreeNode;
import devplugin.Program;

public class PTManager {
	
	private static final util.ui.Localizer mLocalizer = util.ui.Localizer
				.getLocalizerFor(PTManager.class);
	
	/** String for localization * */
	private String noChannelSetText = mLocalizer.msg("NoChannelSetText",
			"no channel set");
	
	class PTCategory{
		protected PluginTreeNode ptNode=null;
		protected boolean isDefaultCategory=false;
		protected Vector<Channel> channels=new Vector<Channel>();
        protected Properties props=new Properties();
	}
	
	protected PTManager(){
		addCategory(noChannelSetText,true);
	}
	
	
	private String lastSelectedCategory=new String();
	
	private PluginTreeNode ptRoot=new PluginTreeNode("TreeView");
	
	private Vector<PTCategory> catNodes=new Vector<PTCategory>();
	
	protected PluginTreeNode getRoot(){		
		return ptRoot;
	}
	protected String getStartupCategory(){
		return lastSelectedCategory;
	}
	
	protected Vector<Channel> getChannelsOfStartupCategory(){
		return getChannelsOfCategory(lastSelectedCategory);
	}
	
	protected Vector<Channel> getChannelsOfCategory(String categoryName){
		PTCategory ptCat=getPTCategory(categoryName);
		if(ptCat!=null)return ptCat.channels;
		else return new Vector<Channel>();		
	}
	
	protected void setLastSelectedCategory(String categoryName){
		lastSelectedCategory=categoryName;		
	}
	
	protected PTCategory addCategory(String category,boolean isDefault){
		Enumeration<PTCategory> en=catNodes.elements();
		PTCategory foundCat=null;
		while(en.hasMoreElements()){
			PTCategory pt=en.nextElement();
			if((((String)pt.ptNode.getUserObject()).compareTo(category)==0)){
				foundCat=pt;
				break;
			}
		}
		if(foundCat==null){
			PluginTreeNode pt=new PluginTreeNode(category);
			foundCat=new PTCategory();
			foundCat.ptNode=pt;
			foundCat.isDefaultCategory=isDefault;
			catNodes.add(foundCat);
			if(!isDefault)ptRoot.add(pt);
		}
		return foundCat;
	}
	
	protected void renameCategory(String oldCategoryName,String newCategoryName){
		Enumeration<PTCategory> en=catNodes.elements();		
		while(en.hasMoreElements()){
			PTCategory pt=en.nextElement();
			if((((String)pt.ptNode.getUserObject()).compareTo(oldCategoryName)==0)){
				// TODO: We need a solution to rename the existing node OR to copy childs of old nodes to new nodes.
				PluginTreeNode ptNew=new PluginTreeNode(newCategoryName);				
				pt.ptNode=ptNew;
				pt.ptNode.update();
				break;
			}
		}
	}
	
	
	protected void updateCategories(Vector data){
		// Check for removed nodes
		Enumeration<PTCategory>eNodes=catNodes.elements();
		while(eNodes.hasMoreElements()){
			PTCategory cat=eNodes.nextElement();
			Enumeration edata=data.elements();
			boolean found=false;
			while(edata.hasMoreElements()){
				String categorieName=(String)((Vector)edata.nextElement()).elementAt(0);
				if((categorieName!=null)&&(categorieName.length()>0)&&(categorieName.compareTo((String)cat.ptNode.getUserObject())==0)){
					found=true;
					break;
				}
			}	
			if(!found){
				// remove node
				if(!cat.isDefaultCategory){					
					catNodes.remove(cat);
				}
			}
		}
		Enumeration edata=data.elements();
		while(edata.hasMoreElements()){
			String categorieName=(String)((Vector)edata.nextElement()).elementAt(0);
			if((categorieName!=null)&&(categorieName.length()>0)){
				//System.out.println("Adding "+categorieName);
				addCategory(categorieName,false);
			}		
		}
		
		// Rebuild root childs and update root
		eNodes=catNodes.elements();
		ptRoot.removeAllChildren();
		while(eNodes.hasMoreElements()){
			PTCategory cat=eNodes.nextElement();
			//System.out.println("List "+cat.ptNode.getUserObject());
			if(!cat.isDefaultCategory)ptRoot.add(cat.ptNode);
		}
		ptRoot.update();
	}
	
	protected String[] getPTCategoryNames(boolean addNoUpdate){
		
		String arr[]=new String[catNodes.size()+(addNoUpdate?0:-1)];
		
		int i=0;
		if(addNoUpdate){
			arr[0]=noChannelSetText;
			i++;
		}
		Enumeration<PTCategory> en=catNodes.elements();		
		while(en.hasMoreElements()){
			PluginTreeNode pt=en.nextElement().ptNode;
			String categoryName=(String)pt.getUserObject();			
			if(categoryName.compareTo(noChannelSetText)!=0)arr[i++]=categoryName;			
		}
		return arr;
	}
	
	protected void copyTreeToPTCategory(TreeViewNode tv,String categoryName,Vector<Channel> channels){
		if(categoryName.compareTo(noChannelSetText)==0)return; 
		PTCategory ptCat=getPTCategory(categoryName);
		if(ptCat!=null){
			ptCat.ptNode.removeAllChildren();
			ptCat.channels=channels;
			addChildToPluginTree(tv,ptCat.ptNode);
			ptRoot.update();
		}
	}
	
	private PTCategory getPTCategory(String category){		
		Enumeration<PTCategory> en=catNodes.elements();
		while(en.hasMoreElements()){
			PTCategory ptCat=en.nextElement();
			PluginTreeNode pt=ptCat.ptNode;
			if((((String)pt.getUserObject()).compareTo(category)==0)){
				return ptCat;
			}
		}
		return null;
	}

	/**
	 * Used to copy the last tree content to plugin tree when dialog is closed.
	 * @param node TreeViewNode
	 * @param pn PluginTreeNode
	 */
	private void addChildToPluginTree(TreeViewNode node,PluginTreeNode pn){
		try{
			int numChilds=node.getChildCount();
			for(int i=0;i<numChilds;i++){
				
				TreeViewNode child=(TreeViewNode)node.getChildAt(i);				
							
				if(!child.isLeaf()){					
					PluginTreeNode childPN=new PluginTreeNode(child.getPluginTreeDescription());				
					
					pn.add(childPN);
					childPN.setGroupingByDateEnabled(false);
					childPN.setGroupingByWeekEnabled(false);
					//childPN.getMutableTreeNode().setShowLeafCountEnabled(false);
					addChildToPluginTree(child,childPN);
					
				}else{						
					Program p=child.getProgram();
					if(p!=null){
						PluginTreeNode prog=pn.addProgram(p);
						prog.setNodeFormatter(new PluginTreeNodeFormatter());
					}
				}
			}
		}catch (Exception e) {
			/*log.addStackTrace(e);
			e.printStackTrace();
			exception = e;*/
		}		
		//log.msg("[TVP.addChildToPluginTree] left");
	}
	
	protected void loadSettings(Properties p) {
		// Load Categories		
		try{
			
			int num = Integer.valueOf(p.getProperty("PTM.Category.Num","0")).intValue();
			for(int i=0;i<num;i++){
				String categoryName=p.getProperty("PTM.Category.Entry"+i,null);
				if(categoryName!=null){
					boolean isDefault=false;
					if(categoryName.compareTo(noChannelSetText)==0)isDefault=true;
					PTCategory ptCategory=addCategory(categoryName,isDefault);
					// Search channels of category
					int numChannels = Integer.valueOf(p.getProperty("PTM."+categoryName+".Channel.Num","0")).intValue();
					HashSet<String> channelNames=new HashSet<String>();
					for(int j=0;j<numChannels;j++){
						String channelName=p.getProperty("PTM."+categoryName+".Channel.Entry"+j,null);
						if(channelName!=null){
							channelNames.add(channelName);
						}
					}
					Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();
					for(int c=0;c<channels.length;c++){
						if(channelNames.contains(channels[c].getDefaultName())){
							ptCategory.channels.add(channels[c]);
						}
					}
                    // Load Properties of category 
                    Enumeration eKeys=p.keys();
                    while(eKeys.hasMoreElements()){
                        String key=(String)eKeys.nextElement();
                        if(key.startsWith("PTM."+categoryName+".")){
                            String value=(String)p.get(key);
                            ptCategory.props.setProperty(key,value);
                        }
                    }
				}
			}
			lastSelectedCategory=p.getProperty("PTM.LastSelectedCategory","");
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	protected Properties storeSettings(Properties p) {
		// Remove old PluginTree settings
		Enumeration<?>names=p.propertyNames();
		while(names.hasMoreElements()){
			String name=(String)names.nextElement();
			if(name.startsWith("PTM."))p.remove(name);
		}
		
		// Add new entries
		
		Enumeration<PTCategory> eCat=catNodes.elements();
		int j=0;
		while(eCat.hasMoreElements()){
			PTCategory ptCat=eCat.nextElement();
			String categoryName=((String)ptCat.ptNode.getUserObject());
			//if(categoryName.compareTo(noChannelSetText)!=0){
				p.setProperty("PTM.Category.Entry"+j,categoryName);
				p.setProperty("PTM."+categoryName+".Channel.Num", Integer.toString(ptCat.channels.size()));
				Enumeration<Channel>eChannels=ptCat.channels.elements();
				int i=0;
				while(eChannels.hasMoreElements()){				
					p.setProperty("PTM."+categoryName+".Channel.Entry"+i, eChannels.nextElement().getDefaultName());
					i++;
				}
	            // Properties
	            Enumeration eProp=ptCat.props.keys();
	            while(eProp.hasMoreElements()){
	                String key=(String)eProp.nextElement();
	                String value=(String)ptCat.props.get(key);
	                p.setProperty(key,value);
	            }
	            
				j++;
			//}
		}
		p.setProperty("PTM.Category.Num", Integer.toString(j));
		
		// Last selectedCategory
		p.setProperty("PTM.LastSelectedCategory",lastSelectedCategory);
		return p;
	}
	
	protected void saveCategorySetting(String categoryName,String key,String value){		
        PTCategory ptCat=getPTCategory(categoryName);
        if(ptCat!=null){
            ptCat.props.setProperty("PTM."+categoryName+"."+key,value);
        }else{
            //System.out.println("[TreeViewPlugin.PTManager.saveCategorySetting] ERROR: Category not found "+categoryName);
        }
    }
    
    protected String loadCategorySetting(String categoryName,String key){
        return loadCategorySetting(categoryName,key,"");
    }
    
    protected String loadCategorySetting(String categoryName,String key,String defaultValue){
    	
        PTCategory ptCat=getPTCategory(categoryName);
        String ret=null;
        if(ptCat!=null){
            ret= (String)ptCat.props.getProperty("PTM."+categoryName+"."+key);
        }else{
            //System.out.println("[TreeViewPlugin.PTManager.loadCategorySetting] ERROR: Category not found "+categoryName);        	
        }
        if((ret==null)||(ret.length()==0)){
            return defaultValue;
        }else{
            return ret;
        }
    }
	
}
