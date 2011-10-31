package treeviewplugin;

import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerListModel;
import javax.swing.table.DefaultTableModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;;

public class TreeViewSettings implements SettingsTab {
	
	private Properties mSettings=null;
	private TreeViewPlugin plugin=null;
	/** Translator */
	private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TreeViewSettings.class);
	
	private JComboBox fontComboBox=null;
	private JSpinner fontHeightSpinner=null;
	private JCheckBox includeInContextMenuCheckBox=null;
	private JSpinner infoLineLengthSpinner=null;
	private JCheckBox pluginTreeCheckBox=null;
	private JTable categoryTable=null;
	private MyTableModel categoryTableModel=null;
	private JScrollPane categoryScrollPane=null;
	private JButton addCategoryButton=null;
	private JButton removeCategoryButton=null;
	
	/** String for localization * */
	private String fontComboBoxText = mLocalizer.msg("FontComboBox", "Font: ");
	
	/** String for localization * */
	private String fontHeightSpinnerText = mLocalizer.msg("FontHeightSpinner", "Font size: ");
	
	/** String for localization * */
	private String includeInContextMenuCheckBoxText = mLocalizer.msg("IncludeContextMenuCheckBox", "Include in context menu: ");
	
	/** String for localization * */
	private String titelText = mLocalizer.msg("Titel", "TreeView - Plugin");
	
	/** String for localization * */
	private String fontToolTip = mLocalizer.msg("FontToolTip", "Select font used in tree.");
	
	/** String for localization * */
	private String fontHeightToolTip = mLocalizer.msg("FontHeightToolTip", "Select font height used in tree.");
	
	/** String for localization * */
	private String includeInContextMenuToolTip = mLocalizer.msg("IncludeContextMenuToolTip", "Select checkbox if TreeView plugin should be displayed in context menu.");
	
	/** String for localization * */
	private String lineLengtLabel=mLocalizer.msg("LineLengtText","Tree line length:");
	
	/** String for localization * */
	private String lineLengtToolTip=mLocalizer.msg("LineLengtTextToolTip","Select number of characters that should be shown in one line of the tree info nodes.");
	
	/** String for localization * */
	private String pluginTreeText=mLocalizer.msg("PluginTreeText","Enabled PluginTree.");
	
	/** String for localization * */
	private String pluginTreeTextToolTip=mLocalizer.msg("PluginTreeTextToolTip","Select checkbox if TreeView should use PluginTree.");
	
	/** String for localization * */
	private String pluginTreeTableHeaderCategory=mLocalizer.msg("PluginTreeTableHeaderCategory","PluginTree category");
	
	/** String for localization * */
	private String pluginTreeTableAddButton=mLocalizer.msg("PluginTreeTableAddButton","Add category");
	
	/** String for localization * */
	private String pluginTreeTableRemoveButton=mLocalizer.msg("PluginTreeTableRemoveButton","Remove category");
	
	private class MyTableModel extends DefaultTableModel{
		private static final long serialVersionUID = 1L;

		/**
		 * Overriden to detected category renaming
		 */
		public void setValueAt(Object aValue,
                int row,
                int column){
			//System.out.println("Old: "+getValueAt(row,column)+" New: "+aValue);
			String oldCategoryName=(String)getValueAt(row,column);
			String newCategoryName=(String)aValue;
			if((oldCategoryName!=null)&&(newCategoryName!=null)){
				plugin.getPTManager().renameCategory(oldCategoryName, newCategoryName);
			}
			super.setValueAt(aValue,row, column);
			
		}
	}
	
	/**
     * Constructs a new settings dialog 
     * @param plg   Reference to plugin (currently not used).
     * @param props Settings.
	 */
	public TreeViewSettings(TreeViewPlugin plg,Properties props){
		plugin=plg;
		mSettings=props;
	}

	/**
	 * Creates the settings panel.
	 */
	public JPanel createSettingsPanel() {

	    JLabel fontHeightLabel=new JLabel(fontHeightSpinnerText);
	    fontHeightLabel.setToolTipText(fontHeightToolTip);
	    fontHeightSpinner=new JSpinner();
	    int start=8;
	    int end=60;
	    String values[] = new String[end-start+1];
		for (int i = 0; i < values.length; i++) {
			values[i] = new Integer(start +i ).toString();
		}
		SpinnerListModel fontHeightModel = new SpinnerListModel(values);				
						
		fontHeightSpinner.setModel(fontHeightModel);
		String strFontHeight=mSettings.getProperty("FontHeight", "14");
		fontHeightSpinner.getModel().setValue(strFontHeight);
		fontHeightSpinner.setToolTipText(fontHeightToolTip);

	    
	    JLabel fontLabel=new JLabel(fontComboBoxText);
	    fontLabel.setToolTipText(fontToolTip);
	    
	    String names[]=GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	    ComboBoxModel fontComboBoxModel = new DefaultComboBoxModel(names);	    
	    fontComboBoxModel.setSelectedItem(mSettings.getProperty("Font","Arial"));
		fontComboBox = new JComboBox();			
		fontComboBox.setModel(fontComboBoxModel);
		fontComboBox.setToolTipText(fontToolTip);		
				
		includeInContextMenuCheckBox=new JCheckBox();		
		includeInContextMenuCheckBox.setSelected(mSettings.getProperty("IncludeInContextMenu", "false").equals("true"));
		includeInContextMenuCheckBox.setToolTipText(includeInContextMenuToolTip);
		includeInContextMenuCheckBox.setText(includeInContextMenuCheckBoxText);		
		
		infoLineLengthSpinner=new JSpinner();
		start=50;
	    end=1000;
		String lineLengthValues[] = new String[end-start+1];
		for (int i = 0; i < lineLengthValues.length; i++) {
			lineLengthValues[i] = new Integer(start +i ).toString();
		}
		SpinnerListModel infoLineLengthModel= new SpinnerListModel(lineLengthValues);
		infoLineLengthSpinner.setModel(infoLineLengthModel);		
		infoLineLengthModel.setValue(mSettings.getProperty("InfoLineLength", "100"));
		infoLineLengthSpinner.setToolTipText(lineLengtToolTip);
		JLabel infoLineLengthLabel=new JLabel(lineLengtLabel);
		infoLineLengthLabel.setToolTipText(lineLengtToolTip);
		
		pluginTreeCheckBox=new JCheckBox();
		pluginTreeCheckBox.setSelected(mSettings.getProperty("EnablePluginTree", "true").equals("true"));
		pluginTreeCheckBox.setText(pluginTreeText);
		pluginTreeCheckBox.setToolTipText(pluginTreeTextToolTip);
		
		categoryScrollPane=new JScrollPane();
		categoryTableModel=new MyTableModel();
		categoryTable=new JTable(categoryTableModel);
		categoryScrollPane.add(categoryTable);
		
		String headers[]=new String[1];
		headers[0]=pluginTreeTableHeaderCategory;
		categoryTableModel.setColumnIdentifiers(headers);

		categoryScrollPane.setViewportView(categoryTable);
		categoryTableModel.setColumnCount(1);
		PTManager ptManager=plugin.getPTManager();
		String categoryNames[]=ptManager.getPTCategoryNames(false);
		categoryTableModel.setNumRows(categoryNames.length);
		for(int i=0;i<categoryNames.length;i++){
			categoryTableModel.setValueAt(categoryNames[i],i, 0);
		}
		
		addCategoryButton=new JButton(pluginTreeTableAddButton); 
		addCategoryButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				categoryTableModel.setRowCount(categoryTableModel.getRowCount()+1);
			}			
		});
		removeCategoryButton=new JButton(pluginTreeTableRemoveButton); 
		removeCategoryButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {				
				categoryTableModel.removeRow(categoryTable.getSelectedRow());
			}			
		});				
	    
	    FormLayout layout = new FormLayout("5dlu,70dlu,30dlu,pref:grow,10dlu",
        "15dlu,default,15dlu,default,15dlu,default,90dlu,default,15dlu");
	    CellConstraints cc=new CellConstraints();
	    //layout.setColumnGroups(new int[][] {{5,7}});
	    PanelBuilder pb = new PanelBuilder(layout);
	    pb.add(fontLabel, cc.xyw(2,1,1));
	    pb.add(fontComboBox, cc.xyw(3,1,2));
	    
	    pb.add(fontHeightLabel,cc.xyw(2,2,1));
	    pb.add(fontHeightSpinner,cc.xyw(3,2,1));
	    
	    pb.add(infoLineLengthLabel,cc.xyw(2,3,1));
	    pb.add(infoLineLengthSpinner,cc.xyw(3,3,1));
	    
	    pb.add(includeInContextMenuCheckBox,cc.xyw(2,4,3));
	    
	    pb.add(pluginTreeCheckBox,cc.xyw(2,5,3));
	    
	    
	    
	    FormLayout categoryLayout = new FormLayout("5dlu,90dlu,90dlu,pref:grow,10dlu",
        "90dlu,default,15dlu,default");
	    PanelBuilder pbCategory = new PanelBuilder(categoryLayout);
	    pbCategory.add(categoryScrollPane,cc.xyw(2,1,2));
	    pbCategory.add(addCategoryButton,cc.xyw(2,2,1));
	    pbCategory.add(removeCategoryButton,cc.xyw(3,2,1));
	    pb.add(pbCategory.getPanel(),cc.xyw(2,6,4));
	    
		return pb.getPanel();
	}

    /**
     * Returns null (no icon).
     */
	public Icon getIcon() {
		return null;
	}

    /** Returns the settings title */
	public String getTitle() {		
		return titelText;
	}

    /**
     * Saves the adjusted settings.
     */
	public void saveSettings() {
		mSettings.put("Font", fontComboBox.getModel().getSelectedItem());
		mSettings.put("FontHeight", fontHeightSpinner.getModel().getValue());
		mSettings.put("IncludeInContextMenu",Boolean.toString(includeInContextMenuCheckBox.isSelected()));
		mSettings.put("InfoLineLength",infoLineLengthSpinner.getModel().getValue());
		mSettings.put("EnablePluginTree",Boolean.toString(pluginTreeCheckBox.isSelected()));
		
		plugin.getPTManager().updateCategories(categoryTableModel.getDataVector());
		
	}

}
