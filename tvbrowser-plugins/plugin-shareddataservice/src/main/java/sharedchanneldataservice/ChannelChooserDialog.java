package sharedchanneldataservice;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
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


import tvbrowser.core.Settings;
import util.ui.CaretPositionCorrector;
import util.ui.Localizer;
import util.ui.UiUtilities;

public class ChannelChooserDialog extends JDialog {

  private static final long serialVersionUID = 1L;
  
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannelChooserDialog.class);
//  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(ChannelChooserDialog.class.getName());
  
  public JTextField [] descriptor = new JTextField[4];
  private JSpinner mTime;

  public JDialog mDialog;
  private String selectedChannel;
  public JTable selector;
  private String []selectorHeader = {mLocalizer.msg("channel", "Channel"),mLocalizer.msg("country", "Country"),mLocalizer.msg("dataPlugin", "Data Plugin"),mLocalizer.msg("group", "Channel Group")};
  private String [][] selectorData;
  private boolean okPressed;

  /**
   * Create the Dialog
   * @param parent Parent-Frame
   */
  public ChannelChooserDialog(JFrame parent) {
    super(parent, true);
    mDialog=this;
  }

  /**
   * Create the Dialog
   * @param parent Parent-Dialog
   */
  public ChannelChooserDialog(JDialog parent) {
    super(parent, true);
    mDialog=this;
  }

  public void createGui(final String myName, final Icon myIcon,  final String[] alienIds, final String prevId, final String prevStart) {
    
    okPressed=false;
    selectedChannel = prevId;

    setTitle(mLocalizer.msg("selectChannel", "Select Data Source"));

    JPanel backgroundPanel = (JPanel) getContentPane();
    backgroundPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.PAGE_AXIS)); 

    JPanel titlePanel = new JPanel();
    titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.LINE_AXIS));
    titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

    JLabel chnLabel  = new JLabel (myName);
    chnLabel.setIconTextGap(20);
    if (myIcon != null) {
      chnLabel.setIcon(UiUtilities.scaleIcon(myIcon, 40));
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
          selectedChannel=alienIds[selector.getSelectedRow()];
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
      dimTable.setSize(Math.min(400, dimTable.getWidth() + vScrollBarWidth), Math.min(300, dimTable.getHeight()+ headerHeight));
      sScrollPane.setPreferredSize(new Dimension(dimTable.width, dimTable.height));

      backgroundPanel.add(sScrollPane);
      backgroundPanel.add(Box.createVerticalGlue());
   }
   
    JPanel descPanel = new JPanel();
    descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.LINE_AXIS));
    descPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

    JLabel descriptorLabel = new JLabel (mLocalizer.msg("selectedSource", "selected source:"));
    descPanel.add (descriptorLabel);
    descPanel.add(Box.createHorizontalGlue());
  
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
         descriptor[i].setHorizontalAlignment(JLabel.CENTER);
         margin = new EmptyBorder(0, 4, 0, 4);
       }
       else{
         margin = new EmptyBorder(0, 8, 0, 4);
       }
       descriptor[i].setBorder(new CompoundBorder(border, margin));
       if (alienIds.length>0) {
         int prefferedWidth = selector.getColumnModel().getColumn(i).getPreferredWidth();
         Dimension dim = descriptor[i].getPreferredSize();
         dim.setSize(prefferedWidth, descriptor[i].getFont().getSize()+14);
         descriptor[i].setPreferredSize(new Dimension(dim.width, dim.height));
       }
       fieldsPanel.add(descriptor[i]);
     }
     backgroundPanel.add(fieldsPanel);
     
     backgroundPanel.add(Box.createVerticalGlue());

     JPanel timePanel = new JPanel();
     timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.LINE_AXIS));
     timePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
     JLabel timeLabel = new JLabel(mLocalizer.msg("starttime", "The Channel starts at:"));
     timePanel.add (timeLabel);
     
     timePanel.add(Box.createRigidArea(new Dimension(8, 0)));

     mTime = new JSpinner(new SpinnerDateModel());
      JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(mTime, Settings.getTimePattern());
    mTime.setEditor(dateEditor);
    CaretPositionCorrector.createCorrector(dateEditor.getTextField(), new char[] { ':' }, -1);
     
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt((String)prevStart.split(":")[0]));
    cal.set(Calendar.MINUTE, Integer.parseInt((String)prevStart.split(":")[1]));
    mTime.setValue(cal.getTime());
    
     timePanel.add (mTime);

     timePanel.add(Box.createHorizontalGlue());
     backgroundPanel.add (timePanel);
    backgroundPanel.add(Box.createVerticalGlue());

     JPanel buttonPanel = new JPanel();
     buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
     buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
  
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
         okPressed=true;
         setVisible(false);
       }
     });    
     buttonPanel.add(okButton);


     backgroundPanel.add (buttonPanel);
     pack();
     UiUtilities.centerAndShow(mDialog);
  }

  public String getSelectedTime (){
    if (okPressed) {
      Calendar timeStart = Calendar.getInstance();
      timeStart.setTime((Date) mTime.getValue());
      int hour = timeStart.get(Calendar.HOUR_OF_DAY);
      int min = timeStart.get(Calendar.MINUTE);
      String retValue = "";
      if (hour > 9) {
        retValue = retValue + hour + ":";
      } else {
        retValue = retValue + "0" + hour + ":";
      }
      if (min > 9) {
        retValue = retValue + min;
      } else {
        retValue = retValue + "0" + min;
      }
      return retValue;
    } else {
      return null;
    }
  }
  
  
  public String getSelectedChannel (){
    if (okPressed) {
      return selectedChannel;
    } else {
      return null;
    }

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
      setHorizontalAlignment(JLabel.CENTER);

    }
    else{
      setBorder(new EmptyBorder(0,8,0,4));
      setHorizontalAlignment(JLabel.LEFT);
    }
    super.setValue(value);
    return this;
  }
}