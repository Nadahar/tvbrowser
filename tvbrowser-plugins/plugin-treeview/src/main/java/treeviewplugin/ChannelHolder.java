package treeviewplugin;


import java.awt.Image;
import javax.swing.ImageIcon;




import devplugin.Channel;

/**
 * Used to hold Channel and sized icon objects. 
 * @author matthias.amrhein
 *
 */
public class ChannelHolder {
	
	private Channel channel=null;
	private ImageIcon sizedIcon=null;
	private static ImageIcon defaultIcon=null;	
	private int reservedWidth=0;
	private int reservedHeight=0;
			
	
	/**
     * Creates a new object based on the given channel and rendering font  
     * @param c Channel object
     * @param fontHeight Used to scale icon
	 */
	public ChannelHolder(Channel c,int fontHeight){
		channel=c;
		reservedHeight=(int)((double)fontHeight*1.5);
		reservedWidth=reservedHeight*2;
		
		ImageIcon icon=(ImageIcon)channel.getIcon();
		if(icon==null){
			icon=(ImageIcon)channel.getDefaultIcon();
		}
		if(icon==null){
			if(defaultIcon==null){
				try{
					defaultIcon = new ImageIcon("./imgs/tvbrowser16.png");
				}catch(Exception e){}
			}
			icon=defaultIcon;
		}
		if(icon!=null){
			int height=icon.getIconHeight();
			int width=icon.getIconWidth();
			
			double sch=(double)(reservedHeight)/((double)height);
			double scw=((double)reservedWidth)/((double)width);
			double sc=sch;
			if(scw<sc)sc=scw;
			
			width=(int)(((double)width)*sc+0.5);
			height=(int)(((double)height)*sc+0.5);
			
			 
			sizedIcon=new ImageIcon(icon.getImage().getScaledInstance(width,height,Image.SCALE_SMOOTH));
		   
		}		
		
	    
	}
	
	/**
	*	Returns reserved width of icon
	**/
	protected int getReservedIconWidth(){
		return reservedWidth;
	}
	
	/**
	*	Returns reserved height of icon
	**/
	
	protected int getReservedIconHeight(){
		return reservedHeight;
	}
	
    
	/**
     *  Returns channel name.
     * @return channel name
	 */
	public String toString(){
		return channel.getName();
	}
	  
    /**
     *  Returns channel.
     * @return channel
     */
	public Channel getChannel(){
		return channel;
	}
    /**
     * Returns scaled icon.
     * @return Scaled icon
     */
	public ImageIcon getIcon(){		
		return sizedIcon; 		
	}
    
    /** 
     * Returns channel name (alias of toString())
     * @return channel name
     */
	public String getText(){
		return toString();
	}
	
	/**
	 * Returns the default channel name.
	 * @return default channel name
	 */
	protected String getDefaultChannelName(){
		return channel.getDefaultName();
	}
}