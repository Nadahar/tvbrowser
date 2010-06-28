package nextviewdataservice;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import util.ui.ChannelLabel;
import util.ui.Localizer;
import util.ui.UiUtilities;

public class AlternativeChannelDialog extends JDialog {

  private static final long serialVersionUID = 1L;
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(AlternativeChannelDialog.class);
  private static final Logger mLog = java.util.logging.Logger.getLogger(AlternativeChannelDialog.class.getName());

  public JTextField [] descriptor = new JTextField[4];
  public JDialog mDialog;
  private String [] detailMix;
  private String selectedAlternative;
  public JTable selector;
  private String []selectorHeader = {mLocalizer.msg("channel", "Channel"),mLocalizer.msg("country", "Country"),mLocalizer.msg("dataPlugin", "Data Plugin"),mLocalizer.msg("group", "Channel Group")};
  private String [][] selectorData;
  private boolean okPressed;
  private String myId;
  private String myServiceName = "nextviewdataservice.NextViewDataService";

  private Properties nxtvepgAlternatives;
  private Properties sharedChannelSources;
  private Properties mixedDataSources;

  /**
   * Create the Dialog
   * @param parent Parent-Frame
   */
  public AlternativeChannelDialog(JFrame parent) {
    super(parent, true);
    mDialog=this;
  }

  /**
   * Create the Dialog
   * @param parent Parent-Dialog
   */
  public AlternativeChannelDialog(JDialog parent) {
    super(parent, true);
    mDialog=this;
  }
  public void createGui(final String origId, final String origName, final ChannelLabel channelLabel, final String[] subscribedIds, final String prevId, final String[]alternativeMix, final Properties alternativeDescs) {

    myId = origId;
    okPressed = false;
    selectedAlternative = prevId;
    detailMix = alternativeMix;
    nxtvepgAlternatives = alternativeDescs;
    mixedDataSources = new Properties();
    String mixedDataSourcesFile = NextViewDataService.getInstance().mixedChannelsDirName + "/mixedChannels.properties";
    if (new File(mixedDataSourcesFile).exists()) {
      try {
        InputStream reader = new FileInputStream(mixedDataSourcesFile);
        mixedDataSources.load(reader);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    sharedChannelSources = new Properties();
    String sharedChannelSourcesFile = NextViewDataService.getInstance().mixedChannelsDirName + "/sharedChannels.properties";
    if (new File(sharedChannelSourcesFile).exists()) {
      try {
        InputStream reader = new FileInputStream(sharedChannelSourcesFile);
        sharedChannelSources.load(reader);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    ArrayList <String> alienList = new ArrayList <String> ();
    String myServiceName = NextViewDataService.class.getName();

    for (String subscribedId : subscribedIds) {
      String [] channelId = subscribedId.split("_");
      if (!((myServiceName.equals(channelId[0])&& (myId.equals(channelId[3])|| isNxtvepgAncestor(channelId[3])))||("mixeddataservice.MixedDataService".equals(channelId[0]) && isMixedAncestor(channelId[3]))||("sharedchannelservice.SharedChannelService".equals(channelId[0]) && isSharedAncestor(channelId[3])))){
        alienList.add(subscribedId);
      }
    }
    final String[] alienIds = alienList.toArray(new String[alienList.size()]);

    setTitle(mLocalizer.msg("editChannel", "Edit Addition Data Source"));
    JPanel backgroundPanel = (JPanel) getContentPane();
    backgroundPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.PAGE_AXIS));

    JPanel titlePanel = new JPanel();
    titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.LINE_AXIS));
    titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));


    JLabel chnLabel;
    if (channelLabel != null) {
      chnLabel = channelLabel;
    }
    else{
      chnLabel = new JLabel (origName);
    }

    titlePanel.add(chnLabel);
    titlePanel.add(Box.createHorizontalGlue());
    backgroundPanel.add(titlePanel);


    if (alienIds.length>0){
      selectorData = new String [alienIds.length][4];
      for (int i = 0; i<alienIds.length;i++){
        selectorData[i]= HelperMethods.getChannelName(alienIds[i], "");
      }
      selector = new JTable( selectorData, selectorHeader ){
        private static final long serialVersionUID = 1L;
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };

      SelectorTableCellRenderer render = new SelectorTableCellRenderer();
      for (int i = 0; i < 4; i++) {
        selector.getColumnModel().getColumn(i).setCellRenderer(render);
      }
      selector.getTableHeader().setDefaultRenderer(new TableHeaderRenderer());

      selector.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent arg0) {
          selectedAlternative=alienIds[selector.getSelectedRow()];
          String [] descText= HelperMethods.getChannelName(alienIds[selector.getSelectedRow()], "");
          for (int i = 0; i < 4; i++) {
            descriptor[i].setText(descText[i]);
          }
        }
      });
      JScrollPane sScrollPane = new JScrollPane(selector);
      sizeColumnsToFit(sScrollPane, alienIds.length);

      int vScrollBarWidth = sScrollPane.getVerticalScrollBar().getPreferredSize().width;
      int headerHeight = selector.getTableHeader().getPreferredSize().height +6;
      Dimension dimTable = selector.getPreferredSize();
      dimTable.setSize(Math.min(600, dimTable.getWidth() + vScrollBarWidth), Math.min(400, dimTable.getHeight()+ headerHeight));
      sScrollPane.setPreferredSize(new Dimension(dimTable.width, dimTable.height));

      backgroundPanel.add(sScrollPane);
      backgroundPanel.add(Box.createVerticalGlue());
    }



    JPanel descPanel = new JPanel();
    descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.LINE_AXIS));
    descPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    JLabel descriptorLabel = new JLabel (mLocalizer.msg("selectedSource", "selected source:"));
    descPanel.add (descriptorLabel);
    descPanel.add(Box.createHorizontalGlue());

    JPanel tuningPanel = new JPanel();
    tuningPanel.setLayout(new BoxLayout(tuningPanel, BoxLayout.LINE_AXIS));
    tuningPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    tuningPanel.add(Box.createHorizontalGlue());

    JButton detailButton = new JButton(mLocalizer.msg("details", "Details"));
    detailButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        AlternativeFineTuningDialog detailsDialog = AlternativeFineTuningDialog.getInstance(mDialog);
         if (channelLabel==null) {
         }
        detailsDialog.showGui(origId, origName, channelLabel, detailMix);
        String [] result = detailsDialog.readSettings();
        if (result != null){
          detailMix = result;
        }
      }
    });
    Dimension dim = detailButton.getPreferredSize();
    int prefferedWidth = dim.width;
    dim.setSize(prefferedWidth, descriptorLabel.getPreferredSize().height+4);
    detailButton.setPreferredSize(new Dimension(dim.width, dim.height));
    tuningPanel.add(detailButton);

    backgroundPanel.add (tuningPanel);
    backgroundPanel.add(Box.createVerticalGlue());
    backgroundPanel.add (descPanel);
    backgroundPanel.add(Box.createVerticalGlue());


    JPanel fieldsPanel = new JPanel();
    fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.LINE_AXIS));
    fieldsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    String[]descText= HelperMethods.getChannelName(prevId, "");

    Border border = new LineBorder(Color.BLACK);
    Border margin;
    JPopupMenu popup = new JPopupMenu();
    for (int i = 0; i < 4; i++) {
      descriptor[i] = new JTextField(descText[i]);
      descriptor[i].setEditable(false);
      descriptor[i].setInheritsPopupMenu(false);
      descriptor[i].setComponentPopupMenu(popup);
      if (i==1) {
        descriptor[i].setHorizontalAlignment(SwingConstants.CENTER);
        margin = new EmptyBorder(0, 4, 0, 4);
      }
      else{
        margin = new EmptyBorder(0, 8, 0, 4);
      }
      descriptor[i].setBorder(new CompoundBorder(border, margin));
      if (alienIds.length>0) {
        prefferedWidth = selector.getColumnModel().getColumn(i).getPreferredWidth();
        dim = descriptor[i].getPreferredSize();
        dim.setSize(prefferedWidth, descriptor[i].getFont().getSize()+14);
        descriptor[i].setPreferredSize(new Dimension(dim.width, dim.height));
      }
      fieldsPanel.add(descriptor[i]);
    }
    backgroundPanel.add(fieldsPanel);

    backgroundPanel.add(Box.createVerticalGlue());

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
    JButton resetButton = new JButton(Localizer.getLocalization(Localizer.I18N_DEFAULT));
    resetButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String defaultId = "";
        String defaultsFileName = "files/alternative_sources.properties";
        final InputStream stream = getClass().getResourceAsStream(defaultsFileName);
        Properties defaults = new Properties();
        try {
          defaults.load(stream);
          defaultId = defaults.getProperty(origId,"");
          if (!defaultId.equals("")){
            defaultId=defaultId.split(";")[2];
          }
        } catch (IOException ioe) {
          mLog.warning(ioe.toString());
        }
        selectedAlternative=defaultId;
        String[]descText= HelperMethods.getChannelName(defaultId, "");
        for (int i = 0; i < 4; i++) {
          descriptor[i].setText(descText[i]);
        }
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
    UiUtilities.centerAndShow(mDialog);
  }

  public String getAlternativeChannel (){
    if (okPressed){
      return selectedAlternative;
    } else{
      return null;
    }
  }

  public String [] getDetails (){
    if (okPressed){
      return detailMix;
    } else{
      return null;
    }
  }

  private boolean isNxtvepgAncestor (String testId){
    boolean retValue = false;
    if (nxtvepgAlternatives.getProperty(testId)!=null){
      String propValue = nxtvepgAlternatives.getProperty(testId);
      if (propValue.startsWith(myServiceName) && propValue.endsWith(myId)){
        retValue = true;
      }else {
        String [] channelId = propValue.split("_");
       retValue = retValue ||("mixeddataservice.MixedDataService".equals(channelId[0])&& isMixedAncestor(channelId[3]))||("nextviewdataservice.NextViewDataService".equals(channelId[0]) && isNxtvepgAncestor(channelId[3]))||("sharedchannelservice.SharedChannelService".equals(channelId[0]) && isSharedAncestor(channelId[3]));
      }
    }
    return retValue;
  }

  private boolean isMixedAncestor (String testId){
    boolean retValue = false;
    if (mixedDataSources.getProperty(testId)!=null){
      String [] propValue = mixedDataSources.getProperty(testId).split(";");
      for (int i =1; i<3; i++){
        if (propValue[i].startsWith(myServiceName) && propValue[i].endsWith(myId)){
          retValue = true;
        } else {
          String [] channelId = propValue[i].split("_");
          retValue = retValue ||("mixeddataservice.MixedDataService".equals(channelId[0])&& isMixedAncestor(channelId[3]))||("nextviewdataservice.NextViewDataService".equals(channelId[0]) && isNxtvepgAncestor(channelId[3]))||("sharedchannelservice.SharedChannelService".equals(channelId[0]) && isSharedAncestor(channelId[3]));
        }
      }
    }
    return retValue;
  }
  

  private boolean isSharedAncestor (String testId){
    boolean retValue = false;
    if (sharedChannelSources.getProperty(testId)!=null){
      String [] propValue = sharedChannelSources.getProperty(testId).split(";");
      for (int i =1; i< propValue.length; i++){
        if (propValue[i].startsWith(myServiceName) && propValue[i].endsWith(myId)){
          retValue = true;
        }else {
          String [] channelId = propValue[i].split("_");
          if (channelId.length==4) {
            retValue = retValue || ("mixeddataservice.MixedDataService".equals(channelId[0]) && isMixedAncestor(channelId[3])) || ("nextviewdataservice.NextViewDataService".equals(channelId[0]) && isNxtvepgAncestor(channelId[3])) || ("sharedchannelservice.SharedChannelService".equals(channelId[0]) && isSharedAncestor(channelId[3]));
          }
        }
      }
    }
    return retValue;
  }

 

  private void sizeColumnsToFit(JScrollPane tScroll, int maxRows) {

    for (int i = 0; i < selector.getColumnCount(); i++) {
      TableColumn col = selector.getColumnModel().getColumn(i);
      int prefSize =  selector.getFontMetrics(selector.getFont()).stringWidth(selectorHeader[i]);
      for (int j=0; j< maxRows; j++){
        int test = selector.getFontMetrics(selector.getFont()).stringWidth(selectorData[j][i]);
        if (prefSize<test){
          prefSize = test;
        }
        col.setPreferredWidth(prefSize+24);
        col.setWidth(prefSize+24);
      }
    }
  }
}



class TableHeaderRenderer extends JPanel implements TableCellRenderer {
  private static final long serialVersionUID = 1L;
  private JLabel label = new JLabel();



  public TableHeaderRenderer(){
    super(new GridLayout(1, 1));
    this.add(label);
  }



  public Component getTableCellRendererComponent(JTable table,
      Object value,
      boolean isSelected,
      boolean hasFocus,
      int row, int column) {
    label.setText(value.toString());
    label.setFont(table.getFont());
    setBorder(new EmptyBorder(0, 0, 0, 0));
    Border border = new LineBorder(Color.BLACK);
    Border margin;
    if (column ==1){
      margin = new EmptyBorder(0, 4, 0, 4);
      label.setHorizontalAlignment(SwingConstants.CENTER);
    }
    else{
      label.setHorizontalAlignment(SwingConstants.LEFT);
      margin = new EmptyBorder(0, 7, 0, 4);
    }
    label.setBorder(new CompoundBorder(border, margin));


    setOpaque(true);

    return this;
  }
}


class SelectorTableCellRenderer extends DefaultTableCellRenderer {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  //private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(StatTableCellRenderer.class.getName());

  public SelectorTableCellRenderer(){

  }
  public java.awt.Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ){

    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column );


    if (column == 1){
      setBorder(new EmptyBorder(0,4,0,4));
      setHorizontalAlignment(SwingConstants.CENTER);

    }
    else{
      setBorder(new EmptyBorder(0,8,0,4));
      setHorizontalAlignment(SwingConstants.LEFT);
    }
    super.setValue(value);
    return this;
  }
}