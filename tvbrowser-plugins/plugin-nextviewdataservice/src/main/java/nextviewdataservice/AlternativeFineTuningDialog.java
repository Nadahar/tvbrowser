package nextviewdataservice;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;

import util.ui.ChannelLabel;
import util.ui.Localizer;
import util.ui.UiUtilities;


public class AlternativeFineTuningDialog extends JDialog{

   private static final long serialVersionUID = 1L;

   public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(AlternativeFineTuningDialog.class);
   private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(AlternativeFineTuningDialog.class.getName());
 
   private static AlternativeFineTuningDialog mInstance;

   private JLabel[]fieldLabel;
   private String[] fieldName;
   private int[] fieldIndex;
   private JComboBox[] fieldSetting;
   private int numberOfFields;
   private String[] detailMix;
   private String origId;
   private JPanel titlePanel;
   private boolean okPressed;
   
   /**
    * Create the Dialog
    * @param parent Parent-Frame
    */
   public AlternativeFineTuningDialog(JFrame parent) {
       super(parent, true);
       createGui();
   }

   /**
    * Create the Dialog
    * @param parent Parent-Dialog
    */
   public AlternativeFineTuningDialog(JDialog parent) {
       super(parent, true);
       createGui();
   }
   
   private void createGui(){
     
       JPanel backgroundPanel = (JPanel) getContentPane();
       backgroundPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
       backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.PAGE_AXIS));    

       titlePanel = new JPanel();
       titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
      
       backgroundPanel.add(titlePanel);
       
       JPanel tuningPanel = new JPanel();
       GridBagLayout tuningLayout = new GridBagLayout();
       GridBagConstraints tuningConstraints = new GridBagConstraints();
       tuningPanel.setLayout(tuningLayout);
       
       String fieldsFileName = "files/alternative_fields.properties";
       final InputStream stream = getClass().getResourceAsStream(fieldsFileName);
       Properties fieldsProp = new Properties();
       try {
         fieldsProp.load(stream);
      } catch (IOException ioe) {
         mLog.warning(ioe.toString());
       }
      
      numberOfFields=fieldsProp.size();
      fieldLabel = new JLabel[numberOfFields];
      fieldName = new String [numberOfFields];
      fieldSetting = new JComboBox[numberOfFields];
      fieldIndex = new int[numberOfFields];
      

      for (int i=0; i< numberOfFields; i++) {
        String fieldDesc[]= fieldsProp.getProperty(Integer.toString(i), "-1").split(";");
        fieldIndex [i] = Integer.parseInt((String) fieldDesc[0]);
        fieldName [i]= mLocalizer.msg(fieldDesc[1], fieldDesc[1]);
        fieldLabel[i]= new JLabel(fieldName[i]);
        tuningConstraints = makegbc(0, i, 1, 1);
        tuningConstraints.anchor = GridBagConstraints.WEST;
        tuningLayout.setConstraints(fieldLabel[i], tuningConstraints);
        tuningPanel.add(fieldLabel[i]);
        
        String []items = new String[]{mLocalizer.msg("unchanged", "unchanged"), mLocalizer.msg("replaced", "replaced")};
        if (fieldIndex[i]==13){
          items = new String[]{mLocalizer.msg("unchanged", "unchanged"), mLocalizer.msg("replaced", "replaced"), mLocalizer.msg("added", "added")};
        }
        if (fieldIndex[i]==7||fieldIndex[i]==8||fieldIndex[i]==12||fieldIndex[i]==16){
          items = new String[]{mLocalizer.msg("unchanged", "unchanged"), mLocalizer.msg("replaced", "replaced"), mLocalizer.msg("before", "before"), mLocalizer.msg("after", "after")};
        }
       
        fieldSetting[i]= new JComboBox(items);
       tuningConstraints = makegbc(1, i, 1, 1);
        tuningConstraints.fill = GridBagConstraints.HORIZONTAL;
        tuningConstraints.gridwidth = GridBagConstraints.REMAINDER;
        tuningLayout.setConstraints(fieldSetting[i], tuningConstraints);
        fieldSetting[i].setEditable(false);
        tuningPanel.add(fieldSetting[i]);
        
        
      }
     
     
      JScrollPane tScroller = new JScrollPane(tuningPanel);
      tScroller.setPreferredSize(new Dimension(400, 400));
       backgroundPanel.add(tScroller);
       
       JPanel buttonPanel = new JPanel();
       buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
       buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

       JButton resetButton = new JButton(Localizer.getLocalization(Localizer.I18N_DEFAULT));
       resetButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
             String defaultValue = "";
             String defaultsFileName = "files/alternative_sources.properties";
             final InputStream stream = getClass().getResourceAsStream(defaultsFileName);
             Properties defaults = new Properties();
             try {
               defaults.load(stream);
               defaultValue = defaults.getProperty(origId,"");
               if (!defaultValue.equals("")){
                 defaultValue=defaultValue.split(";",4)[3];
                 detailMix = defaultValue.split(";");
               }
               else{
                 detailMix = "replace;replace;replace;replace;replace;before;replace;replace;before;mix;replace;replace;before;replace;replace;replace;replace;replace;replace;replace;replace;replace;replace;replace;replace;replace;replace;replace;replace;replace".split(";");
               }
             } catch (IOException ioe) {
               mLog.warning(ioe.toString());
             }
             
             setBoxes();

           }
        });    
      buttonPanel.add(resetButton);
     

      buttonPanel.add(Box.createHorizontalGlue());
    
       JButton cancelButton = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
       cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            setVisible(false);
          }
         
     });    
       buttonPanel.add(cancelButton);
    
       JButton okButton = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
       okButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           okPressed = true;
           setVisible(false);
        }
       });    
      buttonPanel.add(okButton);
       
       backgroundPanel.add (buttonPanel);
     
      pack();
       
   }
   
   /**
    * Returns the current instance of this panel. If no instance is given, create a new one.
     * @return instance of setting panel
    */
   public static AlternativeFineTuningDialog getInstance(JDialog parent) {
     if (mInstance == null || mInstance.getParent()instanceof JFrame) {
       mInstance = new AlternativeFineTuningDialog(parent);
     }
     return mInstance;
   } 
   /**
    * Returns the current instance of this panel. If no instance is given, create a new one.
     * @return instance of setting panel
    */
   public static AlternativeFineTuningDialog getInstance(JFrame parent) {
     if (mInstance == null || mInstance.getParent()instanceof JDialog) {
       mInstance = new AlternativeFineTuningDialog(parent);
     }
     return mInstance;
   } 
   
   public void showGui (String id, String origName, ChannelLabel channelLabel, String[]alternativeMix){
     okPressed= false;
    origId =id;
     detailMix = alternativeMix;
     setTitle(mLocalizer.msg("editChannel", "Edit Details"));

     titlePanel.removeAll();
     GridBagLayout titleLayout = new GridBagLayout();
     GridBagConstraints titleConstraints = new GridBagConstraints();
     titlePanel.setLayout(titleLayout);
     
     
     JLabel chnLabel;
     if (channelLabel != null) {
       chnLabel = channelLabel;
    }
     else{
       if (origName.equals("All Channels")){
         chnLabel = new JLabel (UIManager.getIcon("OptionPane.warningIcon"));
         chnLabel.setText(mLocalizer.msg("allChannels", "All Channels!"));
         titleConstraints = makegbc(1, 0, 1, 1);
         titleConstraints.insets = new Insets(10, 10, 10, 10);
         titleConstraints.fill = GridBagConstraints.HORIZONTAL;
         titleConstraints.gridwidth = GridBagConstraints.REMAINDER;
         JTextArea warningText = UiUtilities.createHelpTextArea(mLocalizer.msg("warningText", "Warning: Changing detail information for all channels will delete all channel specific settings done before!"));
         titleLayout.setConstraints(warningText, titleConstraints);
         titlePanel.add(warningText);       
       }
       else{
       chnLabel = new JLabel (origName);
       }
     }

     titleConstraints = makegbc(0, 0, 1, 1);
     titleConstraints.insets = new Insets(10, 10, 10, 10);
     titleLayout.setConstraints(chnLabel, titleConstraints);
     titlePanel.add(chnLabel);

     
     titlePanel.repaint();
     setBoxes ();
     UiUtilities.centerAndShow(mInstance);
   }
   
   
   private void setBoxes (){
     int maxCounter = Math.min(detailMix.length, numberOfFields);
     for (int i=0; i< maxCounter; i++) {
       if (detailMix[i].equals("replace")){
         fieldSetting[i].setSelectedIndex(1);
       }
       else{
         fieldSetting[i].setSelectedIndex(0);
       }
       if (fieldIndex[i]==13){
         if (detailMix[i].equals("mix")){
           fieldSetting[i].setSelectedIndex(2);
         }
       }
       if (fieldIndex[i]==7||fieldIndex[i]==8||fieldIndex[i]==12||fieldIndex[i]==16){
         if (detailMix[i].equals("before")){
           fieldSetting[i].setSelectedIndex(2);
         }
         if (detailMix[i].equals("after")){
           fieldSetting[i].setSelectedIndex(3);
         }
       }
     }
   }
   
   public String[]readSettings(){
 
     if (!okPressed){
       return null;
     }
     
     String []settings = new String[30];
     for (int i = 0; i<30; i++){
       if (i>=numberOfFields || fieldSetting[i].getSelectedIndex()==0){
         settings[i] = "none";
       }
       else {
         settings[i]= "replace";
       }
       if (fieldIndex[i]==13){
         if (fieldSetting[i].getSelectedIndex()==2){
           settings[i] = "mix";
         }
       }
       if (fieldIndex[i]==7||fieldIndex[i]==8||fieldIndex[i]==12||fieldIndex[i]==16){
         if (fieldSetting[i].getSelectedIndex()==2){
           settings[i] = "before";
         }
         if (fieldSetting[i].getSelectedIndex()==3){
           settings[i] = "after";
         }
       }   
     }
     return settings;
   }
   
   
   
   /* makegbc
    * helper function for the GridBagLayout */
   private static GridBagConstraints makegbc(
       int x, int y, int width, int height) {
     GridBagConstraints gbc = new GridBagConstraints();
     gbc.gridx = x;
     gbc.gridy = y;
     gbc.gridwidth = width;
     gbc.gridheight = height;
     gbc.insets = new Insets(10, 10, 10, 10);
     gbc.weightx = 1.0;
     gbc.weighty = 1.0;
     gbc.fill = GridBagConstraints.NONE;
     gbc.anchor = GridBagConstraints.NORTHWEST;
     return gbc;
   } 
}
