package printplugin;

import javax.swing.*;
import javax.swing.event.*;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.ProgramFilter;
import devplugin.ProgressMonitor;

import tvdataservice.MutableProgram;
import util.ui.ImageUtilities;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterJob;
import java.util.TimeZone;


public class PrintDialog extends JDialog {
  
  private JRadioButton mFromRb, mAllRb, mAllChannelsRb, mSelectedChannelsRb;
  private JComboBox mDateCb, mDayStartCb, mDayEndCb, mFilterCb, mSizeCb;
  private JSpinner mDayCountSpinner;
  private JCheckBox mUseFilterCB;
  private JButton mChangeSelectedChannelsBt;
  
  private PrinterJob mPrinterJob;
  
  private Printer mPrinter;
  
  private JLabel mForLabel, mDaysLabel;
  
  public PrintDialog(final Frame parent, PrinterJob printerJob) {
    super(parent, true);
    mPrinterJob = printerJob;
    
    JPanel content = (JPanel)getContentPane();
    
    content.setLayout(new BorderLayout());
    content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    
    JPanel eastPn = new JPanel(new BorderLayout());
    JPanel centerPn = new JPanel(new BorderLayout());
    
    JPanel btnsPn = new JPanel();
    btnsPn.setLayout(new BoxLayout(btnsPn,BoxLayout.Y_AXIS));
    
    JButton printerSetupBtn = new JButton("Drucker einrichten...",ImageUtilities.createImageIconFromJar("printplugin/PageSetup24.gif", getClass()));
    JButton pageBtn = new JButton("Seite einrichten...", ImageUtilities.createImageIconFromJar("printplugin/Properties24.gif", getClass()));
    JButton previewBtn = new JButton("Vorschau...", ImageUtilities.createImageIconFromJar("printplugin/PrintPreview24.gif", getClass()));
    JButton printBtn = new JButton("Drucken", ImageUtilities.createImageIconFromJar("printplugin/Print24.gif", getClass()));   
    JButton cancelBtn = new JButton("Abbrechen", ImageUtilities.createImageIconFromJar("printplugin/Stop24.gif", getClass()));
    
    printerSetupBtn.setHorizontalAlignment(SwingConstants.LEFT);
    pageBtn.setHorizontalAlignment(SwingConstants.LEFT);
    previewBtn.setHorizontalAlignment(SwingConstants.LEFT);
    printBtn.setHorizontalAlignment(SwingConstants.LEFT);
    cancelBtn.setHorizontalAlignment(SwingConstants.LEFT);
    
    btnsPn.add(createButtonPanel(printerSetupBtn,BorderLayout.CENTER));
    btnsPn.add(createButtonPanel(pageBtn,BorderLayout.CENTER));
    btnsPn.add(createButtonPanel(previewBtn,BorderLayout.CENTER));
    
    eastPn.add(btnsPn,BorderLayout.NORTH);
    eastPn.add(printBtn,BorderLayout.SOUTH);
    
    JPanel settingsContentPn = createSettingsContent();
    
    centerPn.add(settingsContentPn,BorderLayout.CENTER);    
    centerPn.add(createButtonPanel(cancelBtn,BorderLayout.WEST),BorderLayout.SOUTH);
    
    
    content.add(eastPn,BorderLayout.EAST);
    content.add(centerPn,BorderLayout.CENTER);
        
        
    printerSetupBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        Thread thread = new Thread(){
          public void run(){
            mPrinterJob.printDialog();
          }
        };
        thread.start();        
      }      
    });    
    
   
    
    previewBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        mPrinter = createPrinter();
        if (mPrinter.getNumberOfPages()==0) {
          JOptionPane.showMessageDialog(parent,"Es sind keine Seiten zu drucken.");
        }
        else {
          PreviewDlg dlg = new PreviewDlg(parent, mPrinter, mPrinterJob.defaultPage(), mPrinter.getNumberOfPages());  
          util.ui.UiUtilities.centerAndShow(dlg);
        }
      }
    });
    
    pageBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Thread thread = new Thread(){
          public void run(){
            mPrinterJob.pageDialog(mPrinterJob.defaultPage());
          }
        };
        thread.start();       
      }
    });
    
    printBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        mPrinter = createPrinter();
        if (mPrinter.getNumberOfPages()==0) {
          JOptionPane.showMessageDialog(parent,"Es sind keine Seiten zu drucken.");
        }
        else {
          hide();  
        }        
      }
    });
    
    cancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {   
        mPrinter = null;     
        hide();  
      }
    });
        
    pack();
  }
  
  private JPanel createButtonPanel(JButton btn, String location) {
    JPanel pn = new JPanel(new BorderLayout());
    pn.add(btn,location);
    return pn;
  }
  
  private JPanel createSettingsContent() {
    JPanel resultPn = new JPanel(new BorderLayout());  
    resultPn.setBorder(BorderFactory.createEmptyBorder(0,0,50,20));
    
    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
    resultPn.add(content,BorderLayout.NORTH);
    
    JPanel datePn = new JPanel();
    datePn.setLayout(new BoxLayout(datePn,BoxLayout.Y_AXIS));
    datePn.setBorder(BorderFactory.createTitledBorder("Zeitraum"));
    
    JPanel pn1 = new JPanel();
    pn1.setLayout(new BoxLayout(pn1,BoxLayout.X_AXIS));
    pn1.add(mFromRb = new JRadioButton("Ab"));
    pn1.add(mDateCb = new JComboBox(createDateObjects(21)));
    pn1.add(mForLabel = new JLabel("Programm fuer"));
    pn1.add(mDayCountSpinner = new JSpinner(new SpinnerNumberModel(5,1,28,1)));
    pn1.add(mDaysLabel = new JLabel("Tage"));
    
    JPanel pn2 = new JPanel(new BorderLayout());
    pn2.add(mAllRb = new JRadioButton("Alles"),BorderLayout.WEST);
    
    datePn.add(pn1);
    datePn.add(pn2);
    
    JPanel timePn = new JPanel(new GridLayout(2,2));
    timePn.setBorder(BorderFactory.createTitledBorder("Uhrzeit"));
    timePn.add(new JLabel("Tagesbeginn:"));
    timePn.add(mDayStartCb=new JComboBox(createIntegerArray(0,23,1)));
    timePn.add(new JLabel("Tagesende:"));
    timePn.add(mDayEndCb=new JComboBox(createIntegerArray(12,36,1)));
    
    mDayStartCb.setRenderer(new TimeListCellRenderer());
    mDayEndCb.setRenderer(new TimeListCellRenderer());
    
    mDayStartCb.setSelectedItem(new Integer(6));
    mDayEndCb.setSelectedItem(new Integer(26));
    
    
    JPanel channelPn = new JPanel();
    channelPn.setLayout(new BoxLayout(channelPn,BoxLayout.Y_AXIS));
    channelPn.setBorder(BorderFactory.createTitledBorder("Sender"));
    
    JPanel allChannelsPn = new JPanel(new BorderLayout());
    allChannelsPn.add(mAllChannelsRb=new JRadioButton("Alle"));
    
    JPanel channelSelectionPn = new JPanel(new BorderLayout());    
    channelSelectionPn.add(mSelectedChannelsRb=new JRadioButton("Ausgewaehlte: 10 von 23 Sender ausgewaehlt"),BorderLayout.WEST);
    channelSelectionPn.add(mChangeSelectedChannelsBt=new JButton("aendern"),BorderLayout.EAST);
    
    channelPn.add(allChannelsPn);
    channelPn.add(channelSelectionPn);
    
    JPanel filterPn = new JPanel();
    filterPn.setLayout(new BoxLayout(filterPn,BoxLayout.X_AXIS));
    filterPn.setBorder(BorderFactory.createTitledBorder("Filter"));
    filterPn.add(mUseFilterCB=new JCheckBox("Filter verwenden:"));
    filterPn.add(mFilterCb=new JComboBox(Plugin.getPluginManager().getAvailableFilters()));
    
    JPanel layoutPn = new JPanel(new GridLayout(3,2));
    layoutPn.setBorder(BorderFactory.createTitledBorder("Layout:"));;
    
    layoutPn.add(new JLabel("Spalten pro Seite:"));
    layoutPn.add(new JSpinner(new SpinnerNumberModel(5,1,20,1)));
    
    layoutPn.add(new JLabel("Sortiert nach"));
    layoutPn.add(new JComboBox(new String[]{"Sender","Beginnzeit"}));
    
    layoutPn.add(new JLabel("Groesse:"));
    layoutPn.add(mSizeCb = new JComboBox(createIntegerArray(20,150,10)));
    mSizeCb.setRenderer(new PercentListCellRenderer());
    
    JPanel programSettingsPn = new JPanel(new BorderLayout());
    programSettingsPn.setBorder(BorderFactory.createTitledBorder("Programmelement"));
    
    Icon ico = createDemoProgramPanel();
    System.out.println(ico.getIconWidth()+" / "+ico.getIconHeight());
    JLabel icoLabel = new JLabel(ico);
    
    
    
    JPanel pn5 = new JPanel(new BorderLayout());
    pn5.add(new JButton("anpassen"),BorderLayout.NORTH);
    
    programSettingsPn.add(icoLabel,BorderLayout.WEST);
    programSettingsPn.add(pn5,BorderLayout.EAST);
    
    content.add(datePn);
    content.add(timePn);
    content.add(channelPn);
    content.add(filterPn);
    content.add(layoutPn);
    content.add(programSettingsPn);
    
    
    ButtonGroup group = new ButtonGroup();
    group.add(mFromRb);
    group.add(mAllRb);
    mFromRb.setSelected(true);
    mFromRb.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent arg0) {
        boolean b = mFromRb.isSelected();
        mDayCountSpinner.setEnabled(b);
        mDaysLabel.setEnabled(b);
        mForLabel.setEnabled(b);
        mDateCb.setEnabled(b);
       }});
       
       
    group = new ButtonGroup();
    group.add(mAllChannelsRb);
    group.add(mSelectedChannelsRb);
    mAllChannelsRb.setSelected(true);
    mSelectedChannelsRb.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent arg0) {
        mChangeSelectedChannelsBt.setEnabled(mSelectedChannelsRb.isSelected());
      }
    });
    
    mUseFilterCB.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent arg0) {
        mFilterCb.setEnabled(mUseFilterCB.isSelected());
      }
    });
    
    return resultPn;
  
  }
  
  private Date[] createDateObjects(int days) {
    Date[] result = new Date[days];
    Date today = Date.getCurrentDate();
    for (int i=0;i<result.length;i++) {
      result[i]=today.addDays(i);
    }
    return result;
  }
  
  private Integer[] createIntegerArray(int from, int to, int step) {
    Integer[] result = new Integer[(to-from)/step+1];
    int cur=from;
    for (int i=0;i<result.length;i++) {
      result[i]=new Integer(cur);
      cur+=step;
    }
    return result;
  }
  
  
  
  private Date getStartDate() {
    
    if (mFromRb.isSelected()) {
      return (Date)mDateCb.getSelectedItem();
    }
    else {
      return Date.getCurrentDate();
    }
  }
  
  private int getNumberOfDays() {
    if (mFromRb.isSelected()) {
      return ((Integer)mDayCountSpinner.getValue()).intValue(); 
    }
    else {
      return -1;
    }
  }
  
  private int getDayStartHour() {
    return ((Integer)mDayStartCb.getSelectedItem()).intValue();
  }
  
  private int getDayEndHour() {
    return ((Integer)mDayEndCb.getSelectedItem()).intValue();
  }
  
  private Channel[] getChannels() {    
    return Plugin.getPluginManager().getSubscribedChannels();
  }
  
  private ProgramFilter getProgramFilter() {
    if (mUseFilterCB.isSelected()) {
      return (ProgramFilter)mFilterCb.getSelectedItem(); 
    }
    return null;
  }
  
  
  private Printer createPrinter() {
   
   
   return PrinterFactory.createDefaultPrinter(
                     null,
                     new DefaultPageRenderer(mPrinterJob.defaultPage(),0.4),
                     getStartDate(),
                     getNumberOfDays(),
                     getDayStartHour(),
                     getDayEndHour(),
                     getChannels(),
                     getProgramFilter());
  }
   
  public Printer getPrinter() {
    return mPrinter; 
  }
  
  private Icon createDemoProgramPanel() {
    MutableProgram prog = new MutableProgram(
               new Channel(null, "Channel 1", TimeZone.getDefault(), "de", ""),
               Date.getCurrentDate(),
               14,
               45);
    prog.setTitle("TV-Browser");
    prog.setShortInfo("TV-Browser is a free EPG");
    prog.setDescription("TV-Browser is a java-based TV guide which is easily extensible using plugins. It is designed to look like a paper based tv guide.");
    
    return new ProgramIcon(prog, null, 200);
    
  }
  
}


class PercentListCellRenderer extends DefaultListCellRenderer {
  public PercentListCellRenderer() {
      
  }
  
  
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                  index, isSelected, cellHasFocus);
      
    if (value instanceof Integer) {
      int val = ((Integer)value).intValue();
      label.setText(val+"%");            
    }
                
    return label;
  }
}

class TimeListCellRenderer extends DefaultListCellRenderer {

  public TimeListCellRenderer() {
      
  }
  
  
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                index, isSelected, cellHasFocus);
      
    if (value instanceof Integer) {
      int val = ((Integer)value).intValue();
      if (val<24) {
        label.setText(val+":00");            
      }
      else {
        label.setText((val-24)+":00 (naechster Tag)");
      }
    }
                
    return label;
  }
  
}


