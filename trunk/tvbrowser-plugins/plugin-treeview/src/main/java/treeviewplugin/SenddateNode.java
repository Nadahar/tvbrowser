package treeviewplugin;


import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import devplugin.Marker;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFieldType;



/**
 * Represents a certain send date of a broadcast
 * @author matthias
 *
 */
public class SenddateNode extends TreeViewNode implements ChangeListener{
	private static final long serialVersionUID = 1L;
	//private static final int numDescriptionChars=750;	
    private Program program=null;    
	
    
    
	/**
	 * Constructs a node object based on program information.
	 * @param program
	 */
	public SenddateNode(Program program,TreeViewNode parent,boolean nextOnAir,TreeViewPlugin plugin)throws Exception{
		super(null,plugin); 		
		this.program=program;
		parent.add(this);
		
		setIsMarkedOnAir(program.isOnAir());	        
        setIsMarkedByPlugin(checkPluginMarkerState());      
        setIsNextOnAir(nextOnAir);
		
		Marker[] markers=program.getMarkerArr();
		if((markers!=null)&&(markers.length>0))setIsMarkedByPlugin(true);
		program.addChangeListener(this);		
	}
	
    /**
     * Unregister change listener. Must be called if object is destroied.
     */
	public void unregisterChangeListener(){
		program.removeChangeListener(this);
	}
	
	public Program getProgram(){
		return program;
	}
	
	/**
     * Checks wether the "markedByPlugin" state of this node has changed. 
	 */
	public void stateChanged(ChangeEvent arg0) {
		try{
		if(plugin==null||plugin.getDialog()==null){
			return;
		}
		boolean newIsMarkedByPlugin=checkPluginMarkerState();
        // Do we need a change?
		if(newIsMarkedByPlugin!=isMarkedByPlugin()){
			// We need a change, set new state and ask
			isMarkedByPlugin=newIsMarkedByPlugin;
            walkUpToRootAndSetStates();  
            plugin.getDialog().repaintTree();            
		}	
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
     * Checks wether this program is marked by an other plugin
     * @return true if it is marked otherwise false
     */
    private boolean checkPluginMarkerState(){
        Marker[] markers=program.getMarkerArr();
        if((markers!=null)&&(markers.length>0)){        
            return true;
        }else{
            return false;
        }
    }


	/**
	 * Returns the set icon.
	 * @return icon or null if program is not set
	 */
	public Icon getIcon(){	
		if(program!=null){
			if(isMarkedByPlugin){
				Marker[] markers=program.getMarkerArr();
		        if((markers!=null)&&(markers.length>0)){
		        	Icon icon=markers[0].getMarkIcon();
		        	if(icon!=null)return icon;		        	
		        }
			}
			return program.getChannel().getIcon();
		}
		return null;
	}
	/**
	 * Handles the click events on a senddate node. 
	 * Opens the detail program info dialog or context menu
 	 * @param evt Incoming event
	 * @param plugin Reference to owning plugin
	 * @param tree Reference to owning Swing component
	 */
	public void clicked(MouseEvent evt,Plugin plugin,JComponent tree){		
		if((evt.getClickCount()>1)&&(evt.getModifiers()==MouseEvent.BUTTON1_MASK)){			
			Plugin.getPluginManager().handleProgramDoubleClick(program,plugin);
		}else if(evt.getModifiers()==MouseEvent.BUTTON3_MASK){
			 JPopupMenu menu = devplugin.Plugin.getPluginManager().createPluginContextMenu(program, plugin);
			 menu.show(tree, evt.getX() - 15, evt.getY()-15);								 
		}else if(evt.getModifiers()==MouseEvent.BUTTON2_MASK){
			Plugin.getPluginManager().handleProgramMiddleClick(program,plugin);
		}
		
	}
	
	/**
	 * Creates the node descrition.
	 * @return description
	 */
	public String getDesc(TreeViewPlugin plugin){
		if(desc!=null)return desc;
		try{
            StringBuffer buffer=new StringBuffer();
            
			// Add program name and date
            buffer.append("<html><body><i>");
			String name=program.getChannel().getName();
            if(name!=null){
                buffer.append(program.getChannel().getName());
            }
            buffer.append(" - ");
            buffer.append(program.getDateString());
            buffer.append(" ");
            buffer.append(program.getTimeFieldAsString(ProgramFieldType.START_TIME_TYPE));
            String endTime=program.getTimeFieldAsString(ProgramFieldType.END_TIME_TYPE);
            if(endTime!=null){
            	buffer.append("-");
            	buffer.append(endTime);
            }
						            
			// Add episode
			String episode=program.getTextField(ProgramFieldType.EPISODE_TYPE);
			if((episode!=null)&&(episode.length()>0)){				
                buffer.append(" - ");
                buffer.append(episode);
			}
			buffer.append("</i>");
			String shortInfo=program.getShortInfo();			
			String longInfo=program.getDescription();
			
            int shortInfoLength=0;
            if((shortInfo!=null)&&(shortInfo.length()>0)){
                shortInfoLength=shortInfo.length();
            }
			if((longInfo!=null)&&(longInfo.length()>shortInfoLength)){ 
				buffer.append("<br><hr>");
			    buffer.append(Tools.addHTMLLineBreaks(new StringBuffer(longInfo),DistinctBroadcastDialog.numInfoLineChars));
            }else{
                if(shortInfo!=null){
                	buffer.append("<br><hr>");
                	buffer.append(Tools.addHTMLLineBreaks(new StringBuffer(shortInfo),DistinctBroadcastDialog.numInfoLineChars));
                }
            }
			buffer.append("</body></html>");			
			return buffer.toString();
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
	
	
		
	/**
	 * Returns true if this node should have a border
	 * @return true
	 */
	public boolean useBorder(){
		return true;
	}		
}
