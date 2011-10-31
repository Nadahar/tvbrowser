package treeviewplugin;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JFrame;
import javax.swing.JList;



import devplugin.*;

/**
 * This plugin displays the program of one or more channels in a tree view
 * sorted by genre or names.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * 
 * @author matthias
 * 
 */


public class TreeViewPlugin extends devplugin.Plugin {
	
	

	/** Translator */
	private static final util.ui.Localizer mLocalizer = util.ui.Localizer
			.getLocalizerFor(TreeViewPlugin.class);

	/** Settings */
	private Properties mSettings = new Properties();

	/** Dialog instance */
	private DistinctBroadcastDialog dialog = null;

	/** Used to set Error handling dialog */
	private Exception exception = null;
	
	private static boolean tvBrowserStartFinished=false;
	
	/** Log console for debugging purposes */
	protected LogConsole log=new LogConsole();
	
	private boolean pluginTreeEnabled=true;	
	
	private PTManager ptManager=new PTManager();
	
	
	
	public void handleTvBrowserStartFinished() {		
		if(pluginTreeEnabled){

			JFrame frame = (JFrame) getParentFrame();
			dialog = new DistinctBroadcastDialog(frame, this);
			
			String categoryNames[]=ptManager.getPTCategoryNames(false);
			for(int i=0;i<categoryNames.length;i++){
				dialog.setReady(false);
				Vector<Channel> selectedChannels =ptManager.getChannelsOfCategory(categoryNames[i]);
				dialog.enterCategory(categoryNames[i]);
				if (selectedChannels.size()>0) {			
					addInitialChannels(selectedChannels);													
					dialog.setReady(true);					
					dialog.refreshData(true);				
					ptManager.copyTreeToPTCategory(dialog.getRootNode(),categoryNames[i],selectedChannels);
				}
			}
			dialog.stopRenderThread();
			dialog=null;	
		}		
		tvBrowserStartFinished=true;
	}
	/**
	 * Opens the dialog initially.
	 * 
	 * @param program
	 *            selected program. Null if no program is selected.
	 */
	private void showDialog(Program program) {
		// Create Dialog
		if(!tvBrowserStartFinished)return;		
		
		JFrame frame = (JFrame) getParentFrame();
		dialog = new DistinctBroadcastDialog(frame, this);
		log=new LogConsole();

		// Add listener to handle window close events, that updates the settings
		// to be stored
		dialog.addWindowListener(new WindowListener() {

			public void windowOpened(WindowEvent arg0) {
			}

			public void windowClosing(WindowEvent arg0) {				
				if(dialog.getSelectedCategory()!=null){
					// Add current tree to plugin tree
					String category=dialog.getSelectedCategory();
					if(category!=null){
						ptManager.copyTreeToPTCategory(dialog.getRootNode(),category,dialog.getSelectedChannels());
					}
					
				}
					
				mSettings = ((DistinctBroadcastDialog) arg0.getComponent())
						.storeSettings(mSettings);
				
				// Deregister Program change listeners
				TreeViewNode root= ((DistinctBroadcastDialog) arg0.getComponent()).getRootNode();
				root.unregisterChangeListener();
				
				if( (exception != null)||(log.isEnabled()))				
					showErrorMessage();
				dialog.stopRenderThread();
				
				dialog = null;

			}

			public void windowClosed(WindowEvent arg0) {
			}

			public void windowIconified(WindowEvent arg0) {
			}

			public void windowDeiconified(WindowEvent arg0) {
			}

			public void windowActivated(WindowEvent arg0) {
			}

			public void windowDeactivated(WindowEvent arg0) {
			}

		});
		dialog.enterCategory(getPTManager().getStartupCategory());
		Vector<Channel> selectedChannels = new Vector<Channel>();
		// Use selected channel
		if (program != null) {
			selectedChannels.add(program.getChannel());
		} else {			
			selectedChannels=ptManager.getChannelsOfStartupCategory();
			
			// Use persistant settings
			if (selectedChannels.size()==0){
				Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();
				if(channels.length >= 1) { // No persistant settings, use first channel			
					selectedChannels.add(channels[0]);				
				} else { // No channels... 
					selectedChannels=null;
				}
			}
		}
		addInitialChannels(selectedChannels);
		dialog.setReady(true);
		dialog.refreshData(true);
		// Expand root node and show dialog
		dialog.expandRootNode();
		dialog.setDialogTitle();
		dialog.setVisible(true);
	}		


	/**
	 * Initially sets the selected channel
	 * 
	 * @param markedChannel
	 */
	private void addInitialChannels(Vector<Channel> markedChannel) {
		int fontSize=dialog.getChannelListFont().getSize();
		
		JList channelList = dialog.getChannelList();
		DefaultComboBoxModel listModel = (DefaultComboBoxModel) channelList.getModel();
				
		Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();
		Vector<Integer> selectedIndices=new Vector<Integer>();
		for (int i = 0; i < channels.length; i++) {

			ChannelHolder ch=new ChannelHolder(channels[i],fontSize);			
			listModel.addElement(ch);
			if (markedChannel != null){// && (markedChannel == channels[i])) {				
				Iterator<Channel> it=markedChannel.iterator();
				while(it.hasNext()){
					Channel c=it.next();
					if(c==channels[i]){					
						selectedIndices.add(new Integer(listModel.getSize() - 1));												
						break;
					}
				}							
			}
		}
		if(selectedIndices.size()>0){
			Iterator<Integer> it=selectedIndices.iterator();
			int si[]=new int[selectedIndices.size()];
			int i=0;
			while(it.hasNext()){
				Integer s=it.next();
				si[i]=s.intValue();
				i++;
			}
			channelList.setSelectedIndices(si);
			channelList.ensureIndexIsVisible(si[0]);
		}
	}
	
	private final static Integer INSERTED=0;
	private final static Integer ONAIRFOUND=1;
	private final static Integer NEXTONAIRFOUND=2;

	/**
	 * Builds hashtable structure
	 * 
	 * @param selectedChannels
	 * @return 
	 */
	private Hashtable<String/* Genre */, Hashtable<String/* Title */, BroadcastNode>> buildDistinctTable(
			Vector<Channel> selectedChannels) {		
		
		log.msg("[TVP.buildDistinctTable] entered");
		String noGenreText = mLocalizer.msg("TreeNode-NoGenre", "No genre");
		Hashtable<String/* Genre */, Hashtable<String/* Title */, BroadcastNode>> distinctTable = new Hashtable<String, Hashtable<String, BroadcastNode>>();
		
		boolean hidePastBroadcasts = dialog.isHidePastBroadcasts();
		boolean onAirBroadcasts = dialog.isOnAirBroadcasts();
		boolean useGenres = !dialog.isHideGenres();
		int daysToCheck=dialog.getDaysToCheck();
		Date startDate=dialog.getStartDate();		
		Date maxEndDate = startDate;
		log.msg("[TVP.buildDistinctTable] past: "+hidePastBroadcasts+" onAir: "
				+onAirBroadcasts+" genres: "+useGenres+" days: "+daysToCheck+" selectedChannels: "+selectedChannels.size());

		int count = 0;
		int dayCounter = 0;		
		Date date = new Date();
		// We want to see "onAir" broadcasts after midnight.
		// Therefore we have to check yesterday and ignore all broadcasts that
		// are not on air
		date = date.addDays(-1);
		boolean daySelected=false;
		Date nowDate = new Date();
		if(nowDate.getValue()!=startDate.getValue()){			
			daySelected=true;
			dayCounter++;
			date = date.addDays(1);
		}
		boolean everythingWasFine=false;
		boolean genreFound=false;
		Hashtable<Channel,Integer> channelStates=new Hashtable<Channel,Integer>(); 
		// Process all days
		try {
			do {
				// We do not start at today, so lets go to the future
				if((daySelected)&&(date.getValue()<startDate.getValue())){					
					date = date.addDays(1);
					dayCounter++;
					daysToCheck++;
					continue;
				}
				boolean foundProgram = false;
				// For each day process all selected channels
				Iterator<Channel> itSelectedChannels = selectedChannels
						.iterator();
				while (itSelectedChannels.hasNext()) {

					Channel currentChannel = itSelectedChannels.next();
					
					// First day, so lets add the channel to our Channel table
					if(dayCounter==0){
						channelStates.put(currentChannel,INSERTED);
					}

					
					// System.out.println("Channel:
					// "+currentChannel.getDefaultName()+" "+date);

					Iterator<Program> it = Plugin.getPluginManager()
							.getChannelDayProgram(date, currentChannel);
					// Find all Broadcasts of this channel

					// Process all programs of this channel and day
					boolean markNextAsOnAir=false;
					// Do we have to mark the next broadcast as "next on air"?
					Integer channelState=channelStates.get(currentChannel);
					
					if((!daySelected)&&(dayCounter>=2)&&(channelState==INSERTED)){
						channelState=ONAIRFOUND; // We did not found an on air broadcast so simply mark the next
						channelStates.put(currentChannel,ONAIRFOUND);
					}
					
					if((channelState!=null)&&(channelState==ONAIRFOUND))markNextAsOnAir=true;
					while ((it != null) && (it.hasNext()))  {
						foundProgram = true;
						Program programOfDay = it.next();
						// System.out.println(" - "+date+"
						// "+programOfDay.getTitle());
						if ((dayCounter == 0) && (!programOfDay.isOnAir()))
							continue;
						if ((hidePastBroadcasts) && programOfDay.isExpired())
							continue;
						if ((onAirBroadcasts) && (!programOfDay.isOnAir()))
							continue;

						
						
						// Do not localize this string!
						String genre = "default";
						if (useGenres) {
							genre = programOfDay
									.getTextField(ProgramFieldType.GENRE_TYPE);
							if (genre == null){
								genre = noGenreText;
							}else genreFound=true;								
						}else{
							if(programOfDay.getTextField(ProgramFieldType.GENRE_TYPE)!=null){
								genreFound=true;
							}
						}
						
						// Find genre table, create it if not found
						Hashtable<String/* Title */, BroadcastNode> biTable = distinctTable
								.get(genre);
						if (biTable == null) {
							biTable = new Hashtable<String/* Title */, BroadcastNode>();
							distinctTable.put(genre, biTable);
						}

						String title = programOfDay.getTitle();

						// Find broadcast
						BroadcastNode bi = biTable.get(title);
						if (bi == null) {
							bi = new BroadcastNode(title,this);
							biTable.put(title, bi);
						}
						//Create new Senddate node and add it to Broadcast node
						new SenddateNode(programOfDay,bi,markNextAsOnAir,this); 
						if(markNextAsOnAir){
							markNextAsOnAir=false;
							channelStates.put(currentChannel,NEXTONAIRFOUND);							
						}
						if(programOfDay.isOnAir()){							
							channelStates.put(currentChannel,ONAIRFOUND);
							markNextAsOnAir=true;
						}
						count++;

					}
				}
				
				// Stop if processing consumes to mutch memory
				if(!Tools.isEnoughFreeMemory(log))break;
					
				dayCounter++;
				if((dayCounter>2)&&(onAirBroadcasts))continue;
				if ((!foundProgram) && (dayCounter > 1)){
					log.msg("[TVP.buildDistinctTable] break after days: "+dayCounter);
					break;
				}else
					maxEndDate = date;
				date = date.addDays(1);
				
			} while (dayCounter <= daysToCheck);
			everythingWasFine=true;
			dialog.setDataContainsGenres(genreFound);
		} catch (Exception e) {
			log.addStackTrace(e);
			e.printStackTrace();
			exception = e;
		}
		
		dialog.setMaxEndDate(maxEndDate);
		log.msg("[TVP.buildDistinctTable] left, table: "+distinctTable.size()+" count: "+count+" dayCounter: "+dayCounter+" everythingWasFine: "+everythingWasFine);
		return distinctTable;
	}
	
	

	/**
	 * Converts the hashtable structure returned by buildDistinctTable to the
	 * node structure needed by JTree
	 * 
	 * @param distinctTable
	 * @param useGenres
	 */
	private  void addGenreNodesToRoot(
			Hashtable<String/* Genre */, Hashtable<String/* Title */, BroadcastNode>> distinctTable) {		
		try {			
			log.msg("[TVP.addGenreNodesToRoot] entered");			
			
			RootNode rootNode = dialog.getRootNode();
			boolean useGenres = !dialog.isHideGenres();
			// Sort Genres
			TreeSet<String> sortedGenres = new TreeSet<String>();

			Enumeration<String> eGenres = distinctTable.keys();
			while (eGenres.hasMoreElements()) {
				String genre = eGenres.nextElement();
				sortedGenres.add(genre);
			}

			// And now decide which node we need
			Iterator<String> itSortedGenres = sortedGenres.iterator();
			while (itSortedGenres.hasNext()) {
				String genre = itSortedGenres.next();

				// Sort the broadcast infos
				TreeSet<BroadcastNode> broadcastTreeSet = new TreeSet<BroadcastNode>(
						distinctTable.get(genre).values());

				TreeViewNode parentNode = null;
				// Do not localize this string!
				if ((genre.compareTo("default") == 0)
						|| (distinctTable.size() == 1)) {
					parentNode = rootNode;
					useGenres = false;
				} else {
					// parentNode=new BroadcastTreeNode(genre+"
					// ("+broadcastTreeSet.size()+")",BroadcastTreeNode.TYPES.GENRE.ordinal());
					parentNode = new GenreNode(genre + " ("
							+ broadcastTreeSet.size() + ")",this);
					rootNode.add(parentNode);							
				}

				// ...and add it to the node
				addBroadcastsToNode(parentNode, broadcastTreeSet, false);
			}

			// Set a nice title for the root node
			String rootInfo = new String();
			if (useGenres)
				rootInfo = mLocalizer.msg("TreeNode-RootNumGenres", "Genres: ",
						rootNode.getChildCount());
			else
				rootInfo = mLocalizer.msg("TreeNode-RootNumBroadcasts",
						"Broadcasts: ", rootNode.getChildCount());

			rootNode.setUserObject(rootInfo);			

		} catch (Exception e) {
			log.addStackTrace(e);
			e.printStackTrace();
			exception = e;
		}
		log.msg("[TVP.addGenreNodesToRoot] left");
		
	}
	
	/**
	 * Adds sorted broadcast nodes to genre or root node.
	 * 
	 * @param parentNode
	 * @param treeSet
	 * @param showDescription
	 */
	private void addBroadcastsToNode(TreeViewNode parentNode,
			TreeSet<BroadcastNode> treeSet, boolean showDescription) {
		try {
			log.msg("[TVP.addBroadcastsToNode] entered");
			Iterator<BroadcastNode> itEntry = treeSet.iterator();
			while (itEntry.hasNext()) {
				BroadcastNode broadcastNode = itEntry.next();
				parentNode.add(broadcastNode);
				parentNode.setIsMarkedOnAir(broadcastNode.isMarkedOnAir());
				parentNode.setIsMarkedByPlugin(broadcastNode.isMarkedByPlugin());
				parentNode.setIsNextOnAir(broadcastNode.isNextOnAir());
			}
		} catch (Exception e) {
			log.addStackTrace(e);
			e.printStackTrace();
			exception = e;
		}
		log.msg("[TVP.addBroadcastsToNode] left");
	}

	/**
     * Returns an array of date objects for which data is available on the given channels.
     * @param channels channels that should be searched for. null means use all subscribed channels.
     * @return date array
     */
    protected Date[] getDaysWithData(Channel[] channels){               
        if(channels==null){
            channels = Plugin.getPluginManager().getSubscribedChannels();
        }
       
        Date date=new Date();
        Vector<Date> vecDates=new Vector<Date>();
        for (int i = 0; i < channels.length; i++) {
            Iterator<Program> it=null;
            while(((it=Plugin.getPluginManager().getChannelDayProgram(date, channels[i]))!=null)&&(it.hasNext())){               
                vecDates.add(date);
                date=date.addDays(1);
            }
        }
        // We add the current day if there is no data available
        if(vecDates.size()==0){
        	vecDates.add(new Date());
        }
        // Copy to array
        Date[] arrDates=new Date[vecDates.size()];
        for(int i=0;i<arrDates.length;i++){
            arrDates[i]=vecDates.elementAt(i);
        }
        return arrDates;
    }
    
    protected PTManager getPTManager(){
    	return ptManager;
    }
	
	/**
	 * Refreshs the JTree content.
	 */
	public void refresh() {		
		if (dialog == null)
			return;
		try {
			log.msg("[TVP.refresh] entered");			
			
			// Find currently selected channels			
			Vector<Channel> vecSelChannels = dialog.getSelectedChannels();								
			Hashtable<String/* Genre */, Hashtable<String/* Title */, BroadcastNode>> distinctTable = buildDistinctTable(vecSelChannels);			
			// Remove old nodes an add new ones
			dialog.getRootNode().removeAllChildren();
			addGenreNodesToRoot(distinctTable);
			distinctTable = null;
			// Expand root node
			dialog.setDialogTitle();
			dialog.expandRootNode();
		} catch (Exception e) {
			log.addStackTrace(e);
			e.printStackTrace();
			exception = e;
		}

		log.msg("[TVP.refresh] left");
	}
	
    /**
     * Returns reference to main dialog.
     * @return dialog
     */
	public DistinctBroadcastDialog getDialog(){
		return dialog;
	}

	//
	// Persistent methods
	//

	/**
	 * Returns the serializable plugin settings.
	 * @return settings
	 */
	protected Properties getSettings() {
		return mSettings;
	}

	/**
	 * Store the Settings
	 */
	public Properties storeSettings() {
		ptManager.storeSettings(mSettings);
		return mSettings;
	}

	/**
	 * Load settings
	 */
	public void loadSettings(Properties settings) {
		if (settings == null)
			settings = new Properties();
		mSettings = settings;
		pluginTreeEnabled=mSettings.getProperty("EnablePluginTree", "true").equals("true");
		ptManager.loadSettings(settings);
	}

	//
	// Plugin interface methods
	//

	public ActionMenu getContextMenuActions(final Program program) {
		if(mSettings.getProperty("IncludeInContextMenu", "false").equals("true")){
			AbstractAction action = new AbstractAction() {
				private static final long serialVersionUID = 1L;
	
				public void actionPerformed(ActionEvent evt) {
					showDialog(program);
				}
			};
			action.putValue(Action.NAME, mLocalizer.msg("ContextMenu", "TreeView"));		
			action.putValue(Action.SMALL_ICON, createImageIcon("actions", "treeview", 16));
	        action.putValue(BIG_ICON, createImageIcon("actions", "treeview", 22));
	
			return new ActionMenu(action);
		}
		return null;
	}

	public ActionMenu getButtonAction() {
		AbstractAction action = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) {
				showDialog();
			}
		};
		action.putValue(Action.NAME, mLocalizer.msg("ButtonText", "TreeView"));
		action.putValue(Action.SHORT_DESCRIPTION, getInfo().getDescription());
        action.putValue(Action.SMALL_ICON, createImageIcon("actions", "treeview", 16));
        action.putValue(BIG_ICON, createImageIcon("actions", "treeview", 22));
		return new ActionMenu(action);
	}

	protected void showDialog() {
		showDialog(null);
	}
	
	public boolean canUseProgramTree() {
		return pluginTreeEnabled;	    
	}
	
	public PluginTreeNode getRootNode(){
		if(pluginTreeEnabled)return ptManager.getRoot();
		else return null;
	}
	

	/**
	 * Returns plugin information
	 */
	public PluginInfo getInfo() {
		String name = mLocalizer.msg("pluginName", "Tree View Plugin");
		String desc = mLocalizer
				.msg(
						"description",
						"Displays the program of one or more channels in a tree view sorted by genre or names.");
		String author = "Matthias Amrhein";
		Version version = new Version(0, 37, false/* true=stable */);
		return new PluginInfo(name, desc, author, version);
	}
    
    public SettingsTab getSettingsTab() {
        return new TreeViewSettings(this,mSettings);
    }

	//
	// Error Handling
	//

	/**
	 * Shows an error dialog if an exception occured while using the plugin.
	 * 
	 */
	private void showErrorMessage() {		
		if((exception == null)&&(!log.isEnabled()))
			return;
		JDialog alert = new JDialog();
		alert.setTitle("TreeView Plugin Error Message");
		JScrollPane scrollPane=new JScrollPane();
		JTextPane text = new JTextPane();		
		alert.setSize(300,300);
		alert.setLocation(0, 0);
		
		scrollPane.setViewportView(text);
		alert.add(scrollPane);
		String out=new String();
		
		if((log!=null)&&(log.isEnabled())){
			out+="Log: ";
			out+=log.getLog();
			out+="\n";
		}
		if(exception!=null){
			StringWriter sw = new StringWriter();
			exception.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			out+="An error happend while you were using Treeview plugin:\n"
				+ stacktrace;
		}		
		text.setText(out);
		
		alert.pack();
		alert.setVisible(true);
		alert.setAlwaysOnTop(true);
		exception = null;
	}
	public boolean isPluginTreeEnabled() {
		return pluginTreeEnabled;
	}
}
