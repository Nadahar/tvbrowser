package treeviewplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SpinnerListModel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.Date;


/**
 * Dialog class
 * @author matthias
 *
 */
public class DistinctBroadcastDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	/** Translator * */
	private static final util.ui.Localizer mLocalizer = util.ui.Localizer
			.getLocalizerFor(DistinctBroadcastDialog.class);

	// Button arrangement constants
	private static final int miniumBottomPanelHeight =185;
	
	private boolean dialogIsReady=false;
	//private HashSet<String> persistentChannels=new HashSet<String>();
	
	// Dialog elements
	private JSplitPane verticalSplitPane;

	private JScrollPane broadcastTreeScrollPane;

	private JScrollPane channelListScrollPane;

	private JCheckBox onAirCheckBox;

	private JCheckBox hidePastBox;
	private JComboBox ptCategoryBox;
	private JLabel ptCategoryLabel;
	

	private JLabel daysLabel;

	private JSpinner daysToCheckSpinner;

	private JCheckBox hideGenreBox;

	private JPanel buttonPanel;

	private JTree broadcastTree;

	private JList channelList;

	private JSplitPane horizontalSplitPane;
	
	private JTextField searchField;
	private JButton searchButton;
	private SearchEngine searchEngine;
	private JComboBox startDayComboBox;
	private JLabel startDayLabel;
	
	
	private Font treeFont=null;
	private Font treeFontMarked=null;
	
	private RenderThread renderThread=null;
	private boolean dataContainsGenres=true;
	
	private static Border treeCellBorder=BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
	private static Border treeCellEmptyBorder=BorderFactory.createEmptyBorder();
	
	public static int numInfoLineChars=100;
	
	/** Reference to our calling plugin */
	private TreeViewPlugin plugin = null;
	
	/** Root node of JTree */
	private RootNode root = new RootNode(mLocalizer.msg("DefaultRootNode",
			"No channel selected"),plugin);
	
	/** String for localization * */
	private String dialogTitleWorking = mLocalizer.msg("DialogNameWorking",
			"Tree View - working, please wait...");

	/** String for localization * */
	private String hidePastCheckBoxText = mLocalizer.msg(
			"ShowFutureCheckBoxText", "Show only future broadcasts");
	

	/** String for localization * */
	private String hideGenresCheckBoxText = mLocalizer.msg(
			//"HideGenresCheckBoxText", "Hide genres");
			"ShowTitlesOnlyCheckBoxText", "Show only titles");

	/** String for localization * */
	private String onAirCheckBoxText = mLocalizer.msg("OnAirCheckBoxText",
			"Show only on air");

	/** String for localization * */
	private String checkDaysLabelText = mLocalizer.msg("CheckDaysLabelText",
			"Days in tree:");
	
	/** String for localization * */
	private String startDayLabelText = mLocalizer.msg("StartDayLabelText",
	"Begin with day:");
	
	/** String for localization * */
	private String startDayToolTip = mLocalizer.msg("StartDayToolTip",
	"Select day from which the program is displayed in tree.");

	/** String for localization * */
	private String hidGenresToolTip = mLocalizer.msg(
			"HideGenresCheckBoxToolTip", "Check this to hide the genre level.");

	/** String for localization * */
	private String hidPastToolTip = mLocalizer
			.msg("HidePastCheckBoxToolTip",
					"Check this to hide all broadcasts of the current day that are already aired.");

	/** String for localization * */
	private String daysToCheckToolTip = mLocalizer
			.msg("DaysToCheckToolTip",
					"Select number of days that should be displayed. '1' means current day only.");

	/** String for localization * */
	private String onAirToolTip = mLocalizer.msg("OnAirToolTip",
			"Check this to see only broadcasts that are currently on air.");
	
	private String searchFieldToolTip = mLocalizer.msg("SearchFieldToolTip",
		"Insert search term and hit return or press search button. The search will be done case insensitiv.");
	
	private String searchButtonToolTip = mLocalizer.msg("SearchButtonToolTip",
			"Push button to start your search. The search will halt on the the next node that contains your term. Hit again to continue your search.");
	
	private String ptCategoryLabelText = mLocalizer.msg("PluginTreeCategoryLabelText",
			"Update PluginTree");
	
	private String ptCategoryLabelToolTip = mLocalizer.msg("PluginTreeCategoryLabelTollTip",
		"Select PluginTree category that is updated after dialog is closed. Use settings dialog to create new categories.");
	
	/** String for localization * */
	private String searchButtonText = mLocalizer.msg("SearchButtonText",
			"Search");
	

	/** node tooltips */
	private String nodeOnAir= mLocalizer.msg("nodeOnAirToolTip","Is currently on air.");
    private String nodeNextOnAir=mLocalizer.msg("nodeNextOnAirToolTip","Is next on air.");
    private String nodeMarkedByPlugin=mLocalizer.msg("nodeMarkedToolTip","Marked by other plugin.");
    
    // used to build combinations
    private static String nodeToolTipOnAir=null;
    private static String nodeToolTipNextOnAir=null;
    private static String nodeToolTipMarkedByPlugin=null;
    private static String nodeToolTipOnAirMarkedByPlugin=null;
    private static String nodeToolTipNextOnAirMarkedByPlugin=null;
    private static String nodeToolTipEmpty="";
    private static Color markedOnAirNonSelectColor=new Color(224, 227, 255);
    private static Color markedOnAirSelectColor=new Color(143, 144, 255);
    private static Color markedNextOnAirNonSelectColor=new Color(224, 255, 242);
    private static Color markedNextOnAirSelectColor=new Color(143, 235, 182);
    private static Color unmarkedSelectionColor=new Color(204, 205, 255);
    private static Color toolTipBackgroundColor=Color.white;
    private static Color listSelectionColor=new Color(153, 160, 255);
    

	/** Maximum analysed end date displayed in title * */
	private String maxEndDate = "";


	/**
	 * Use this constructor ONLY for developement purposes to render the dialog
	 * 
	 * @param frame
	 */
	protected DistinctBroadcastDialog(JFrame frame) {
		initGUI();
	}
	
	public void treeNodeChanged(TreeViewNode node){
		((DefaultTreeModel)broadcastTree.getModel()).nodeChanged(node);
	}
	
	public void repaintTree(){
		broadcastTree.repaint();
	}

	/**
	 * Constructs the Dialog
	 * 
	 * @param frame
	 *            Reference to parent frame
	 * @param plugin
	 *            Reference to caling plugin
	 */
	protected DistinctBroadcastDialog(JFrame frame, TreeViewPlugin plugin) { //
		super(frame,true);
		this.plugin = plugin;		
		initGUI();
		
	}
	
	protected void setDataContainsGenres(boolean genres){
		dataContainsGenres=genres;
	}
	
    /**
     * Returns font object used in channel list. 
     * @return Channel list font.
     */
	public Font getChannelListFont(){
		return treeFont;
	}
    
	/**
	 * Used to render the cells of the channel list
	 */
	public class ChannelListCellRenderer	extends JPanel	implements ListCellRenderer
	{	  
		private static final long serialVersionUID = 1L;
		//private JLabel label = null;
		private JLabel iconLabel=null;
		private JLabel textLabel=null;
		

	    public ChannelListCellRenderer()
	    {
	    	// Set Component gap
	    	super(new FlowLayout(FlowLayout.LEFT,5,0));	    

	        // JPanel is not opaque
	        setOpaque(true);
	    
	        iconLabel=new JLabel();
	        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
	        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
	        iconLabel.setBorder(BorderFactory.createLineBorder(Color.black));
	        iconLabel.setOpaque(false);
	        
	        
	        textLabel=new JLabel();

	        textLabel.setOpaque(false);
	        add(iconLabel);
	        add(textLabel);
	        
	    }

	    public Component getListCellRendererComponent(JList list, // JList 
	                                                  Object value, // ChannelHolder
	                                                  int index,    // Cell 
	                                                  boolean isSelected,  // is selected
	                                                  boolean hasFocus)  // has focus
	    {	    	
	    	iconLabel.setIcon(((ChannelHolder)value).getIcon());	    	

	    	iconLabel.setPreferredSize(new Dimension(
	    			((ChannelHolder)value).getReservedIconWidth()
	    			, ((ChannelHolder)value).getReservedIconHeight()));
	    	textLabel.setText(((ChannelHolder)value).getText());
	    	textLabel.setFont(treeFont);
	    		     
	        if(isSelected)setBackground(listSelectionColor); // Has focus
	        else setBackground(list.getBackground()); // Has no focus
	        
	        
	        return this;
	    }
	}

	/**
	 * Tree node cell renderer. Used to color currently on air broadcasts and
	 * set icons.
	 * 
	 */
	public class BroadcastTreeCellRender extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);

			TreeViewNode node = (TreeViewNode) value;
            
            boolean tooltipSet=false;
            // Set font
            if(node.isMarkedByPlugin()){
                if(node.isMarkedOnAir()){
                    setToolTipText(nodeToolTipOnAirMarkedByPlugin);
                    tooltipSet=true;
                }else
                if(node.isNextOnAir()){
                    setToolTipText(nodeToolTipNextOnAirMarkedByPlugin);
                    tooltipSet=true;
                }else{
                    setToolTipText(nodeToolTipMarkedByPlugin);
                    tooltipSet=true;
                }
                if(getFont()!=treeFontMarked){
                	setFont(treeFontMarked);   
                	
                }
            }else{
                setFont(treeFont);
            }   

			if (node.isMarkedOnAir()) { 
                if(!tooltipSet)setToolTipText(nodeToolTipOnAir);
				setBackgroundNonSelectionColor(markedOnAirNonSelectColor);
				setBackgroundSelectionColor(markedOnAirSelectColor);
			} else 
			if(node.isNextOnAir()){
                if(!tooltipSet)setToolTipText(nodeToolTipNextOnAir);
				setBackgroundNonSelectionColor(markedNextOnAirNonSelectColor);
				setBackgroundSelectionColor(markedNextOnAirSelectColor);
			}else{				
				if(!tooltipSet)setToolTipText(nodeToolTipEmpty);
				setBackgroundNonSelectionColor(Color.WHITE);
				setBackgroundSelectionColor(unmarkedSelectionColor);
			}			
		
            
			
			// Set icon
			Icon icon = node.getIcon();			
			if(icon!=null) setIcon(icon);	
			
			String desc=node.getDesc(plugin);
			if(desc!=null)setText(desc);
			
			if(node.useBorder()){
				setBorder(treeCellBorder);
			}else{
				setBorder(treeCellEmptyBorder);
			}

			return this;
		}
	}
	
	private String getToolTipTable(Color color1,String text1,boolean bold1,Color color2,String text2,boolean bold2){
		StringBuffer buf=new StringBuffer();
		buf.append("<html><table>");
		// Row 1
		buf.append("<tr bgcolor=\"");
		buf.append(Tools.getHTMLColor(color1));
		buf.append("\"><td>");
		if(bold1)buf.append("<b>");
		buf.append(text1);
		if(bold1)buf.append("</b>");
		buf.append("</td></tr>");
		//Row 2
		if(color2!=null&&text2!=null){
			buf.append("<tr bgcolor=\"");
			buf.append(Tools.getHTMLColor(color2));
			buf.append("\"><td>");
			if(bold2)buf.append("<b>");
			buf.append(text2);
			if(bold2)buf.append("</b>");
			buf.append("</td></tr>");
		}
		
		
		buf.append("</table></html>");
		return buf.toString();
	}

	/**
	 * Creates and arranges dialog elements.
	 * 
	 */
	private void initGUI(){
		try {
            // Build node ToolTips            
            
            nodeToolTipOnAir=getToolTipTable(markedOnAirNonSelectColor,nodeOnAir,false,null,null,false);
            nodeToolTipNextOnAir=getToolTipTable(markedNextOnAirNonSelectColor,nodeNextOnAir,false,null,null,false);
            nodeToolTipMarkedByPlugin=getToolTipTable(toolTipBackgroundColor,nodeMarkedByPlugin,true,null,null,false);
            nodeToolTipOnAirMarkedByPlugin=getToolTipTable(markedOnAirNonSelectColor,nodeOnAir,false,toolTipBackgroundColor,nodeMarkedByPlugin,true);
            nodeToolTipNextOnAirMarkedByPlugin=getToolTipTable(markedNextOnAirNonSelectColor,nodeNextOnAir,false,toolTipBackgroundColor,nodeMarkedByPlugin,true);
                        			
			this.setMinimumSize(new java.awt.Dimension(200, 200));

			// Vertical Split Pane
			verticalSplitPane = new JSplitPane();
			verticalSplitPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			getContentPane().add(verticalSplitPane, BorderLayout.CENTER);
			verticalSplitPane.addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent evt) {
					verticalSplitPaneComponentResized(evt);
				}
			});

			// Horizontal Split Pane
			horizontalSplitPane = new JSplitPane();
			horizontalSplitPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			verticalSplitPane.add(horizontalSplitPane, JSplitPane.LEFT);
			horizontalSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			horizontalSplitPane.setDividerLocation(-1);

			// Broadcast Tree
			broadcastTreeScrollPane = new JScrollPane();
			verticalSplitPane.add(broadcastTreeScrollPane, JSplitPane.RIGHT);

			broadcastTree = new JTree(root);			
			broadcastTreeScrollPane.setViewportView(broadcastTree);
			broadcastTree.setCellRenderer(new BroadcastTreeCellRender());
			broadcastTree.setAutoscrolls(true);
			broadcastTree.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent evt) {
					broadcastTreeMousePressed(evt);
				}
			});			
			// Enable tooltips for broadcast tree
			ToolTipManager.sharedInstance().registerComponent(broadcastTree);
						

			// Channel List
			channelListScrollPane = new JScrollPane();
			horizontalSplitPane.add(channelListScrollPane, JSplitPane.TOP);
			

			ListModel channelListModel = new DefaultComboBoxModel();
			channelList = new JList();
			channelListScrollPane.setViewportView(channelList);
			channelList.setModel(channelListModel);			
			channelList.setCellRenderer(new ChannelListCellRenderer());
			channelList.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent arg0) {					
					if ((arg0.getValueIsAdjusting() == false)&&(dialogIsReady)) {					
						refreshData(false);
					}
				}
			});			

			// Buttons Panel  			
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new FormLayout("fill:default:grow" // Columns
					, "60dlu,35dlu,35dlu"));
		
			JPanel buttonPanelTop=new JPanel();
			JPanel buttonPanelDown=new JPanel();
			JPanel buttonPanelSearch=new JPanel();
			
			horizontalSplitPane.add(buttonPanel, JSplitPane.BOTTOM);

            // Define layout of button panel
			buttonPanelTop.setLayout(new FormLayout("5dlu,fill:default,10dlu:grow,70dlu,5dlu" // Columns
					, "15dlu,10dlu,15dlu,15dlu"));
			buttonPanelDown.setLayout(new FormLayout("5dlu,fill:default" // Columns
					, "1dlu,10dlu,10dlu,10dlu"));			
			buttonPanelSearch.setLayout(new FormLayout("5dlu,50dlu:grow,10dlu:grow,5dlu" // Columns
					,"15dlu,15dlu"));
			
		    CellConstraints cc = new CellConstraints();
		    buttonPanel.add(buttonPanelTop,cc.xy(1, 1));
		    buttonPanel.add(buttonPanelDown,cc.xy(1, 2));
		    buttonPanel.add(buttonPanelSearch,cc.xy(1, 3));
			
		    // Start Day combo Box
			startDayLabel=new JLabel();
			startDayLabel.setText(startDayLabelText);			
			startDayLabel.setToolTipText(startDayToolTip);
			buttonPanelTop.add(startDayLabel,cc.xy(2, 3));
			
			Date days[]=plugin.getDaysWithData(null);
			
			startDayComboBox=new JComboBox(days);			
			startDayComboBox.setToolTipText(startDayToolTip);
			startDayComboBox.setSelectedIndex(0);
			startDayComboBox.setEditable(false);			
			startDayComboBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					refreshData(false);			
				}
				
			});
			
			buttonPanelTop.add(startDayComboBox,cc.xy(4, 3));
						
			daysLabel = new JLabel();
			buttonPanelTop.add(daysLabel,cc.xy(2, 4));
			daysLabel.setText(checkDaysLabelText);
			daysLabel.setToolTipText(daysToCheckToolTip);
			
			String values[] = new String[days.length];
			for (int i = 0; i < values.length; i++) {
				values[i] = new Integer(i + 1).toString();
			}
			SpinnerListModel daysToCheckSpinnerModel = new SpinnerListModel(
					values);
			daysToCheckSpinner = new JSpinner();
			daysToCheckSpinner.setToolTipText(daysToCheckToolTip);
			buttonPanelTop.add(daysToCheckSpinner,cc.xy(4, 4));
			daysToCheckSpinner.setModel(daysToCheckSpinnerModel);
						
			daysToCheckSpinner.getEditor().setPreferredSize(
					new java.awt.Dimension(30, 17));
			daysToCheckSpinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					refreshData(false);
				}
			});
			
			

			//Check Boxes			
			hideGenreBox = new JCheckBox();
			buttonPanelDown.add(hideGenreBox,cc.xy(2, 2));
			hideGenreBox.setText(hideGenresCheckBoxText);			
			hideGenreBox.setToolTipText(hidGenresToolTip);
			hideGenreBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if(!dataContainsGenres)return;
					refreshData(false);
				}
			});
			
			hidePastBox = new JCheckBox();
			buttonPanelDown.add(hidePastBox,cc.xy(2, 3));
			hidePastBox.setText(hidePastCheckBoxText);
			hidePastBox.setSelected(true);
			hidePastBox.setToolTipText(hidPastToolTip);
			hidePastBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					refreshData(false);
				}
			});
			
			onAirCheckBox = new JCheckBox();
			buttonPanelDown.add(onAirCheckBox,cc.xy(2, 4));
			onAirCheckBox.setText(onAirCheckBoxText);
			onAirCheckBox.setToolTipText(onAirToolTip);
			onAirCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					refreshData(false);
				}
			});
			
            // Create search dialog elements
			searchEngine=new SearchEngine(plugin,broadcastTree);
			searchField=new JTextField();
			searchField.setToolTipText(Tools.stringToHtmlDoc(searchFieldToolTip,70));
			searchField.addKeyListener(new KeyListener(){
				public void keyTyped(KeyEvent keyEvent) {
					if(searchField.getText().length()>0){
						if(!searchButton.isEnabled())searchButton.setEnabled(true);						
						if(keyEvent.getKeyChar()=='\n'){
							doSearch();
						}
					}
					else{
						if(searchButton.isEnabled())searchButton.setEnabled(false);
					}	
				}
				public void keyPressed(KeyEvent arg0) {}
				public void keyReleased(KeyEvent arg0) {}
				}
			);
			buttonPanelSearch.add(searchField,cc.xy(2,1));
			searchButton=new JButton(searchButtonText);
			searchButton.setToolTipText(Tools.stringToHtmlDoc(searchButtonToolTip,70));
			searchButton.addMouseListener(new MouseListener(){

				public void mouseClicked(MouseEvent arg0) {}

				public void mouseEntered(MouseEvent arg0) {}

				public void mouseExited(MouseEvent arg0) {}
				
				public void mouseReleased(MouseEvent arg0) {}

				public void mousePressed(MouseEvent arg0) {
					if(searchButton.isEnabled())doSearch();
				}
		
			});
			searchButton.setEnabled(false);
			buttonPanelSearch.add(searchButton,cc.xy(3,1));
			
			ptCategoryLabel=new JLabel(ptCategoryLabelText);
			ptCategoryLabel.setToolTipText(Tools.stringToHtmlDoc(ptCategoryLabelToolTip,70));
			String categoryNames[]=plugin.getPTManager().getPTCategoryNames(true);
		    ComboBoxModel ptCategoryBoxModel = new DefaultComboBoxModel(categoryNames);	    
		    ptCategoryBoxModel.setSelectedItem(categoryNames[0]);
		    ptCategoryBox = new JComboBox();			
		    ptCategoryBox.setModel(ptCategoryBoxModel);		    	
		    ptCategoryBox.setToolTipText(Tools.stringToHtmlDoc(ptCategoryLabelToolTip,70));
		    ptCategoryBox.setSelectedItem(plugin.getPTManager().getStartupCategory());
		    ptCategoryBox.addItemListener(new ItemListener(){

				public void itemStateChanged(ItemEvent event) {					
					if(event.getStateChange()==ItemEvent.DESELECTED){						
						String categoryName=(String)event.getItem();		
						leaveCategory(categoryName);
						
					}else if(event.getStateChange()==ItemEvent.SELECTED){
						String categoryName=(String)ptCategoryBox.getSelectedItem();
						enterCategory(categoryName);
						plugin.getPTManager().setLastSelectedCategory(categoryName);
					}
					
				}
		    	
		    });		    
		    		
		    buttonPanelTop.add(ptCategoryLabel,cc.xy(2, 1));
		    buttonPanelTop.add(ptCategoryBox,cc.xyw(4,1,1));
			
			this.setSize(600, 400);
			this.setPreferredSize(new Dimension(600, 400));
			renderThread=new RenderThread(plugin,searchEngine,broadcastTree,root);
			renderThread.start();
			if (plugin != null)
				loadSettings(plugin.getSettings());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected String getSelectedCategory(){
		if(ptCategoryBox.getSelectedIndex()<=0)return null;
		else return (String)ptCategoryBox.getSelectedItem();
	}
    
    private void leaveCategory(String categoryName){
    	
        if((categoryName!=null)&&(categoryName.length()>0)){
            plugin.getPTManager().copyTreeToPTCategory(root, categoryName, getSelectedChannels());

            // CheckBox settings
            plugin.getPTManager().saveCategorySetting(categoryName,"CheckBox.HidePast", Boolean.toString(hidePastBox
                    .isSelected()));            
            plugin.getPTManager().saveCategorySetting(categoryName,"CheckBox.HideGenre", Boolean.toString(hideGenreBox
                    .isSelected()));
            plugin.getPTManager().saveCategorySetting(categoryName,"CheckBox.OnAir", Boolean.toString(onAirCheckBox
                    .isSelected()));        

            // Spinner setting
            plugin.getPTManager().saveCategorySetting(categoryName,"Spinner.Days", (String) (daysToCheckSpinner.getModel()
                    .getValue()));
                    
            plugin.getPTManager().saveCategorySetting(categoryName,"ComboBox.StartDay",((Date)startDayComboBox.getSelectedItem()).getDateString());
        }
        
    }
    
    protected void enterCategory(String categoryName){
    	boolean readyState=dialogIsReady;
    	setReady(false);
    	//System.out.println("Enter Cat: "+categoryName);
        if(categoryName!=null){
            
            Vector<Channel> channels=plugin.getPTManager().getChannelsOfCategory(categoryName);
            if(channels.size()>0){
                Enumeration<Channel> e=channels.elements();
                Vector<Integer> selIndices=new Vector<Integer>();
                while(e.hasMoreElements()){
                    Channel channel=e.nextElement();
                    String name=channel.getName();
                    if(name!=null){
                        // Find channel indices an set them
                        int size=channelList.getModel().getSize();
                        for(int i=0;i<size;i++){
                            ChannelHolder ch=(ChannelHolder)channelList.getModel().getElementAt(i);
                            if(ch.getDefaultChannelName().compareTo(name)==0){
                                selIndices.add(new Integer(i));
                            }
                        }
                    }
                }
                Enumeration<Integer> indices=selIndices.elements();
                int sel[]=new int[selIndices.size()];
                int i=0;
                while(indices.hasMoreElements()){
                    Integer el=indices.nextElement();
                    sel[i++]=el.intValue();
                }
                channelList.setSelectedIndices(sel);
                
                // Load dialog settings
//              CheckBox settings
                try {                    
                    String hidePastSelected = plugin.getPTManager().loadCategorySetting(categoryName,"CheckBox.HidePast");
                    String hideGenreSelected = plugin.getPTManager().loadCategorySetting(categoryName,"CheckBox.HideGenre");
                    String onAirSelected = plugin.getPTManager().loadCategorySetting(categoryName,"CheckBox.OnAir");         
                    if (hidePastSelected != null)
                        hidePastBox.setSelected(Boolean.parseBoolean(hidePastSelected));
                    if (hideGenreSelected != null)
                        hideGenreBox.setSelected(Boolean
                                .parseBoolean(hideGenreSelected));
                    if (onAirSelected != null)
                        onAirCheckBox.setSelected(Boolean.parseBoolean(onAirSelected));         

                    // Spinner setting
                    String daysToCheck = plugin.getPTManager().loadCategorySetting(categoryName,"Spinner.Days","1");         
                    if (daysToCheck != null){
                        int maxDays=startDayComboBox.getModel().getSize();
                        if(maxDays<Integer.parseInt(daysToCheck)){
                            daysToCheckSpinner.getModel().setValue(new String(Integer.toString(maxDays)));
                        }else{
                            daysToCheckSpinner.getModel().setValue(daysToCheck);
                        }
                    }
                        
                    
                    // Start day combo box
                    String startDaySelected = plugin.getPTManager().loadCategorySetting(categoryName,"ComboBox.StartDay");
                    if(startDaySelected!=null){
                        int items=startDayComboBox.getItemCount();
                        for(int j=0;i<items;i++){
                            Date date=(Date)startDayComboBox.getItemAt(j);
                            if(date.getDateString().compareTo(startDaySelected)==0){
                                startDayComboBox.setSelectedIndex(j);
                                break;
                            }
                        }               
                    }
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }/*else{
                channelList.setSelectedIndex(0);
            }*/
        }
        setReady(readyState);
        if(readyState)refreshData(false);
    }

	
	
    /**
     * Triggers new search
     */
	public void doSearch(){		
		searchEngine.startSearch(searchField.getText());
		repaintTree();	
	}

	/**
	 * Returns selected start date.
	 * @return start date
	 */
	public Date getStartDate(){
		return (Date)startDayComboBox.getSelectedItem();
	}
	/**
	 * Addes persistent dialog properties to the given properties object.
	 * 
	 * @param p
	 * @return Passed properties object that contains updated information.
	 */
	protected Properties storeSettings(Properties p) {
		// Dialog properties
		p.setProperty("DialogLocation.X", Integer.toString(this.getX()));
		p.setProperty("DialogLocation.Y", Integer.toString(this.getY()));
		p.setProperty("DialogSize.Width", Integer.toString(this.getWidth()));
		p.setProperty("DialogSize.Height", Integer.toString(this.getHeight()));

		// Divider locations
		p.setProperty("DividerLocation.Vertical", Integer
				.toString(verticalSplitPane.getDividerLocation()));
		p.setProperty("DividerLocation.Horizontal", Integer
				.toString(horizontalSplitPane.getDividerLocation()));

		String categoryName=(String)ptCategoryBox.getSelectedItem();
		leaveCategory(categoryName);
		
		/*if(updatePluginTreeBox.isSelected()){
			// Remove old PluginTree settings
			Enumeration<?>names=p.propertyNames();
			while(names.hasMoreElements()){
				String name=(String)names.nextElement();
				if(name.startsWith("Channel.Entry"))p.remove(name);
			}
			// Save selected channels
			Object selectedChannels[]=channelList.getSelectedValues();
			p.setProperty("Channel.Num", Integer.toString(selectedChannels.length));
			for(int i=0;i<selectedChannels.length;i++){			
				p.setProperty("Channel.Entry"+i,((ChannelHolder)selectedChannels[i]).getDefaultChannelName());
			}
			
		}*/
		// Remove old style settings
		Enumeration<?>names=p.propertyNames();
		while(names.hasMoreElements()){
			String name=(String)names.nextElement();
			if(name.startsWith("Channel."))p.remove(name);
		}
		return p;
	}

	/**
	 * Loads the persistent settings
	 * @param p  properties
	 */
	private void loadSettings(Properties p) {

		// Dialog properties
		try {
			String dlgX = p.getProperty("DialogLocation.X");
			String dlgY = p.getProperty("DialogLocation.Y");
			if ((dlgX != null) && (dlgY != null))
				setLocation(Integer.valueOf(dlgX), Integer.valueOf(dlgY));

			String dlgWidth = p.getProperty("DialogSize.Width");
			String dlgHeight = p.getProperty("DialogSize.Height");
			if ((dlgWidth != null) && (dlgHeight != null)) {
				int width = Integer.valueOf(dlgWidth);
				int height = Integer.valueOf(dlgHeight);
				if ((width > 0) && (height > 0)) {
					setPreferredSize(new Dimension(width, height));
					setSize(new Dimension(width, height));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Divider locations
		try {
			String dividerVertical = p.getProperty("DividerLocation.Vertical");
			String dividerHorizontal = p
					.getProperty("DividerLocation.Horizontal");
			if ((dividerVertical != null) && (dividerHorizontal != null)) {
				int vertical = Integer.valueOf(dividerVertical);
				int horizontal = Integer.valueOf(dividerHorizontal);
				if ((vertical > 0) && (horizontal > 0)) {
					verticalSplitPane.setDividerLocation(vertical);
					horizontalSplitPane.setDividerLocation(horizontal);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		
		// Font
		try{
			String fontName=p.getProperty("Font", "Arial");
			String fontHeight=p.getProperty("FontHeight", "14");
			treeFont=new Font(fontName,Font.PLAIN,Integer.parseInt(fontHeight));
			treeFontMarked=new Font(fontName,Font.BOLD,Integer.parseInt(fontHeight));
			broadcastTree.setRowHeight(0);
			numInfoLineChars=Integer.parseInt(p.getProperty("InfoLineLength", "100"));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		/*try{
			String startupCategory=plugin.getPTManager().getStartupCategory();
			enterCategory(startupCategory);
		}catch(Exception e){
			e.printStackTrace();
		}*/
		
		// Add debug hook
		try{
			broadcastTree.addKeyListener(new KeyListener(){

				public void keyPressed(KeyEvent arg0) {									
				}

				public void keyReleased(KeyEvent e) {					
					if(e.isAltDown()&&(e.isShiftDown()&&(e.getKeyChar()=='D'))&&(plugin!=null)&&(plugin.log!=null)&&(!plugin.log.isEnabled())){
						plugin.log.setEnabled(true);
						setTitle("DEBUG ENABLED!!!");
					}
				}

				public void keyTyped(KeyEvent arg0) {
				}
				
			});
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}

    
	
	
	/**
	 * Sets a fixed size to the bottom component
	 * @param evt
	 */
	private void verticalSplitPaneComponentResized(ComponentEvent evt) {
		Dimension d = verticalSplitPane.getSize();
		horizontalSplitPane.setDividerLocation((int) d.getHeight()
				- miniumBottomPanelHeight);
	}

	/**
	 * Returns the root node of the JTree
	 * @return JTree root node
	 */
	protected RootNode getRootNode() {
		return root;
	}

	/**
	 * Returns channel list. Its a bit dirty, we should change this.
	 * @return Channel list.
	 */
	protected JList getChannelList() {
		return channelList;
	}

	/**
	 * Returns state of hide genres check box
	 * @return true if selected
	 */
	protected boolean isHideGenres() {
		return hideGenreBox.isSelected();
	}

	/**
	 * Returns state of on air checkbox.
	 * @return true if selected 
	 */
	protected boolean isOnAirBroadcasts() {
		return onAirCheckBox.isSelected();
	}

	/**
	 * Returns state of hide broadcasts check box
	 * @return true if selected 
	 */
	protected boolean isHidePastBroadcasts() {
		return hidePastBox.isSelected();
	}

	/**
	 * Returns day value of spinner dialog
	 * @return days
	 */
	protected int getDaysToCheck() {
		int days = 1;
		try {
			days = Integer.valueOf(
					(String) (daysToCheckSpinner.getModel().getValue()))
					.intValue();
		} catch (Exception e) {
		}
		return days;
	}

	/**
	 * Sets max end date shown in dialog title
	 * @param med
	 */
	protected void setMaxEndDate(Date med) {
		Date now=new Date();
		if(med.getValue()<now.getValue())med=now;
		maxEndDate = med.getLongDateString();
	}

	/**
	 * Sets dialog title
	 *
	 */
	protected void setDialogTitle() {
		setTitle(mLocalizer.msg("DialogName", "Tree View",getStartDate().getLongDateString(),maxEndDate));
	}

	/**
	 * Expands the root node
	 */
	protected void expandRootNode() {
		try{
			broadcastTree.expandPath(broadcastTree.getPathForRow(0));
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}	

	/**
	 * Triggers plugin to recreate the JTree data and repaints the JTree.
	 *
	 */
	protected void refreshData(boolean blocking){			
		if ((plugin == null)||(!dialogIsReady))
			return;				
		plugin.log.msg("[DBD.refreshData] entered");
		setTitle(dialogTitleWorking);
		if(blocking) renderThread.doWork();
		else renderThread.startWork();
		plugin.log.msg("[DBD.refreshData] left");		
	}
	
	/**
	 * The tree will show no data until state is set to ready.	 
	 */
	protected void setReady(boolean r){
		plugin.log.msg("[DBD.setReady] dialog set ready: "+r);
		dialogIsReady=r;
	}
	

	/**
	 * Handles mouse events on broadcast tree
	 * @param evt
	 */
	private void broadcastTreeMousePressed(MouseEvent evt) {
		TreePath path = broadcastTree.getClosestPathForLocation(evt.getX(), evt
				.getY());
		TreeViewNode d = (TreeViewNode) path.getLastPathComponent();
		d.clicked(evt,plugin,broadcastTree);
		toFront();
	}


	protected void stopRenderThread(){		
		if(renderThread!=null)renderThread.exit();
	}
	
	protected Vector<Channel> getSelectedChannels(){	
		Object selectedChannels[]= channelList.getSelectedValues();
		Vector<Channel> vecSelChannels = new Vector<Channel>();
	
		for (int i = 0; i < selectedChannels.length; i++) {				
			vecSelChannels.add(((ChannelHolder)selectedChannels[i]).getChannel());
		}
		return vecSelChannels;
	}
}
