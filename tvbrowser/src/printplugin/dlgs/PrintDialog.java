/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package printplugin.dlgs;

import javax.swing.*;
import javax.swing.event.*;

import printplugin.*;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.ProgramFieldType;
import devplugin.ProgramFilter;

import util.ui.ImageUtilities;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;


public class PrintDialog extends JDialog {
  
   static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(PrintDialog.class);

  
  
  private JRadioButton mFromRb, mAllRb, mAllChannelsRb, mSelectedChannelsRb;
  private JComboBox mDateCb, mDayStartCb, mDayEndCb, mFilterCb, mSizeCb, mSortingCb;
  private JSpinner mDayCountSpinner, mNumberOfColumnsSp;
  private JCheckBox mUseFilterCB, mShowPluginMarkCB;
  private JButton mChangeSelectedChannelsBt, mProgramPanelConfigBt;
  
  private PrinterJob mPrinterJob;
  
  private Printer mPrinter;
  private PageFormat mPageFormat;
  
  private Channel[] mChannels=new Channel[0];
  private JLabel mForLabel, mDaysLabel, mProgramIconLabel;
  private ProgramIconSettings mProgramIconSettings;
  private Frame mParent;
  
  public PrintDialog(final Frame parent, PrinterJob printerJob) {
    super(parent, true);
    mParent = parent;
    setTitle(mLocalizer.msg("print","Print"));
    mPrinterJob = printerJob;
    mPageFormat = printerJob.defaultPage();
    
    JPanel content = (JPanel)getContentPane();
    
    content.setLayout(new BorderLayout());
    content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    
    JPanel eastPn = new JPanel(new BorderLayout());
    JPanel centerPn = new JPanel(new BorderLayout());
    
    JPanel btnsPn = new JPanel();
    btnsPn.setLayout(new BoxLayout(btnsPn,BoxLayout.Y_AXIS));
    
    JButton printerSetupBtn = new JButton(mLocalizer.msg("configPrinter","Configure printer")+"...",ImageUtilities.createImageIconFromJar("printplugin/imgs/PageSetup24.gif", getClass()));
    JButton pageBtn = new JButton(mLocalizer.msg("pageSetup","Page Setup")+"...", ImageUtilities.createImageIconFromJar("printplugin/imgs/Properties24.gif", getClass()));
    JButton previewBtn = new JButton(mLocalizer.msg("preview","Preview")+"...", ImageUtilities.createImageIconFromJar("printplugin/imgs/PrintPreview24.gif", getClass()));
    JButton printBtn = new JButton(mLocalizer.msg("print","Drucken"), ImageUtilities.createImageIconFromJar("printplugin/imgs/Print24.gif", getClass()));   
    JButton cancelBtn = new JButton(mLocalizer.msg("cancel","Abbrechen"), ImageUtilities.createImageIconFromJar("printplugin/imgs/Stop24.gif", getClass()));
    
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
          JOptionPane.showMessageDialog(parent,mLocalizer.msg("noPagesToPrint","There are no pages to print."));
        }
        else {
          PreviewDlg dlg = new PreviewDlg(parent, mPrinter, mPageFormat, mPrinter.getNumberOfPages());  
          util.ui.UiUtilities.centerAndShow(dlg);
        }
      }
    });
    
    pageBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Thread thread = new Thread(){
          public void run(){
            
            mPageFormat = mPrinterJob.pageDialog(mPageFormat/*mPrinterJob.defaultPage()*/);
          }
        };
        thread.start();       
      }
    });
    
    printBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        mPrinter = createPrinter();
        if (mPrinter.getNumberOfPages()==0) {
          JOptionPane.showMessageDialog(parent,mLocalizer.msg("noPagesToPrint","There are no pages to print."));
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
        
    addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
             mPrinter = null;
             hide();
            }      
    });
    
    mProgramIconSettings = PrinterProgramIconSettings.create();
    
        
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
    datePn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("period","Period")));
    
    JPanel pn1 = new JPanel();
    pn1.setLayout(new BoxLayout(pn1,BoxLayout.X_AXIS));
    pn1.add(mFromRb = new JRadioButton(mLocalizer.msg("from","from")));
    pn1.add(mDateCb = new JComboBox(createDateObjects(21)));
    pn1.add(mForLabel = new JLabel(mLocalizer.msg("numberOfDays","Programm fuer")));
    pn1.add(mDayCountSpinner = new JSpinner(new SpinnerNumberModel(5,1,28,1)));
    pn1.add(mDaysLabel = new JLabel(mLocalizer.msg("days","days")));
    
    JPanel pn2 = new JPanel(new BorderLayout());
    pn2.add(mAllRb = new JRadioButton(mLocalizer.msg("all","all")),BorderLayout.WEST);
    
    datePn.add(pn1);
    datePn.add(pn2);
    
    JPanel timePn = new JPanel(new GridLayout(2,2));
    timePn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("time","Time")));
    timePn.add(new JLabel(mLocalizer.msg("startOfDay","Start of day")+":"));
    timePn.add(mDayStartCb=new JComboBox(createIntegerArray(0,23,1)));
    timePn.add(new JLabel(mLocalizer.msg("endOfDay","End of day")+":"));
    timePn.add(mDayEndCb=new JComboBox(createIntegerArray(12,36,1)));
    
    mDayStartCb.setRenderer(new TimeListCellRenderer());
    mDayEndCb.setRenderer(new TimeListCellRenderer());
    
    mDayStartCb.setSelectedItem(new Integer(6));
    mDayEndCb.setSelectedItem(new Integer(26));
    
    
    JPanel channelPn = new JPanel();
    channelPn.setLayout(new BoxLayout(channelPn,BoxLayout.Y_AXIS));
    channelPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("channels","channels")));
    
    JPanel allChannelsPn = new JPanel(new BorderLayout());
    allChannelsPn.add(mAllChannelsRb=new JRadioButton(mLocalizer.msg("allChannels","All")));
    
    JPanel channelSelectionPn = new JPanel(new BorderLayout());    
    channelSelectionPn.add(mSelectedChannelsRb=new JRadioButton(),BorderLayout.WEST);
    channelSelectionPn.add(mChangeSelectedChannelsBt=new JButton(mLocalizer.msg("change","change")+"..."),BorderLayout.EAST);
    
    channelPn.add(allChannelsPn);
    channelPn.add(channelSelectionPn);
    
    JPanel filterPn = new JPanel(new GridLayout(1,2));
    filterPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("filter","Filter")));
    filterPn.add(mUseFilterCB=new JCheckBox(mLocalizer.msg("useFilter","use filter")+":"));
    filterPn.add(mFilterCb=new JComboBox(Plugin.getPluginManager().getAvailableFilters()));
    
    JPanel layoutPn = new JPanel(new GridLayout(2,2));
    layoutPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("layout","layout")+":"));
    
    layoutPn.add(new JLabel(mLocalizer.msg("columnsPerPage","Columns per page")+":"));
    layoutPn.add(mNumberOfColumnsSp = new JSpinner(new SpinnerNumberModel(5,1,20,1)));
    
    layoutPn.add(new JLabel(mLocalizer.msg("sortedBy","sorted by")));
    layoutPn.add(mSortingCb = new JComboBox(new String[]{mLocalizer.msg("channel",""),mLocalizer.msg("time","time")}));
       
    JPanel programSettingsPn = new JPanel(new BorderLayout());
    programSettingsPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("programItem","program item")));
    
    programSettingsPn.add(mShowPluginMarkCB = new JCheckBox(mLocalizer.msg("showPluginMarking","Show plugin marking")),BorderLayout.SOUTH);
    
    Icon ico = createDemoProgramPanel();
    System.out.println(ico.getIconWidth()+" / "+ico.getIconHeight());
    mProgramIconLabel = new JLabel(ico);
    
    JPanel pn6 = new JPanel(new BorderLayout());
    pn6.add(mProgramIconLabel,BorderLayout.NORTH);
    
    
    JPanel pn5 = new JPanel(new BorderLayout());
    pn5.add(mProgramPanelConfigBt = new JButton(mLocalizer.msg("configure","configure")+"..."),BorderLayout.NORTH);
    JScrollPane scrollPane = new JScrollPane(pn6);
    scrollPane.setPreferredSize(new Dimension(250,100));
    programSettingsPn.add(scrollPane,BorderLayout.WEST);
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
        updateDatePanel();
       }});
       
    mChangeSelectedChannelsBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){      
        ChannelChooserDlg dlg = new ChannelChooserDlg(mParent, mChannels);
        util.ui.UiUtilities.centerAndShow(dlg);
        mChannels = dlg.getChannels(mChannels);
        updateSelectedChannelsPanel();
      }
    });   
       
    mProgramPanelConfigBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        ProgramFieldType[] fieldTypes = new ProgramFieldType[]{}; 
        ProgramItemConfigDlg dlg = new ProgramItemConfigDlg(mParent,fieldTypes);
        util.ui.UiUtilities.centerAndShow(dlg);
        //mProgramIconSettings = PrinterProgramIconSettings.create();
        
        if (dlg.getResult()==ProgramItemConfigDlg.OK) {
          ProgramFieldType[] fields = dlg.getProgramItemFieldTypes(); 
          mProgramIconSettings = PrinterProgramIconSettings.create(fields, mShowPluginMarkCB.isSelected());
          mProgramIconLabel.setIcon(createDemoProgramPanel());
        }
        
        
      }
    });
       
    group = new ButtonGroup();
    group.add(mAllChannelsRb);
    group.add(mSelectedChannelsRb);
    mAllChannelsRb.setSelected(true);
    
    mSelectedChannelsRb.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent arg0) {
        updateChannelPanel();
      }
    });
    
    mUseFilterCB.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent arg0) {
        updateFilterPanel();
      }
    });
    
    
    updateFilterPanel();
    updateDatePanel();
    updateChannelPanel();
    updateSelectedChannelsPanel();
    return resultPn;
  
  }
  
  
  private void updateFilterPanel() {
    mFilterCb.setEnabled(mUseFilterCB.isSelected());
  }
  
  private void updateDatePanel() {
    boolean b = mFromRb.isSelected();
    mDayCountSpinner.setEnabled(b);
    mDaysLabel.setEnabled(b);
    mForLabel.setEnabled(b);
    mDateCb.setEnabled(b);
  }
  
  private void updateChannelPanel() {
    mChangeSelectedChannelsBt.setEnabled(mSelectedChannelsRb.isSelected());
  }
  
  private void updateSelectedChannelsPanel() {
    mSelectedChannelsRb.setText(mLocalizer.msg("selectedChannels","Selected: {0} channels selected.",""+mChannels.length));
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
      return 100;
    }
  }
  
  private int getNumberOfColumns() {
    return ((Integer)mNumberOfColumnsSp.getValue()).intValue();
  }
  
  private int getDayStartHour() {
    return ((Integer)mDayStartCb.getSelectedItem()).intValue();
  }
  
  private int getDayEndHour() {
    return ((Integer)mDayEndCb.getSelectedItem()).intValue();
  }
  
  private Channel[] getChannels() {
    if (mAllChannelsRb.isSelected()) {
      return Plugin.getPluginManager().getSubscribedChannels();    
    }
    else {
      return mChannels;
    }
  }
  
  private ProgramIconSettings getProgramIconSettings() {
    return mProgramIconSettings;
  }
  
  private ProgramFilter getProgramFilter() {
    if (mUseFilterCB.isSelected()) {
      return (ProgramFilter)mFilterCb.getSelectedItem(); 
    }
    return null;
  }
  
  
  private Printer createPrinter() {
   
   if (mSortingCb.getSelectedIndex() == 0) {
     
     return PrinterFactory.createDefaultPrinter(
                     null,
                     new ChannelPageRenderer(mPageFormat,getNumberOfColumns(),getDayStartHour(), getDayEndHour(), getProgramIconSettings()),
                     getStartDate(),
                     getNumberOfDays(),
                     getDayStartHour(),
                     getDayEndHour(),
                     getChannels(),
                     getProgramFilter());
   }
   else {
    
     return PrinterFactory.createTimeSortedPrinter(
                      null,
                      new TimePageRenderer(mPageFormat,getNumberOfColumns(), getProgramIconSettings()),
                      getStartDate(),
                      getNumberOfDays(),
                      getDayStartHour(),
                      getDayEndHour(),
                      getChannels(),
                      getProgramFilter()); 
   }
  }
   
  public Printer getPrinter() {
    return mPrinter; 
  }
  
  public PageFormat getPageFormat() {
    return mPageFormat;
  }
  
  private Icon createDemoProgramPanel() {
    
    devplugin.Program prog = Plugin.getPluginManager().getExampleProgram();
        
    ProgramIcon ico = new ProgramIcon(prog, mProgramIconSettings, 200);
    ico.setMaximumHeight(1000);
    return ico;
    
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
        label.setText((val-24)+":00 ("+mLocalizer.msg("nextDay","next day")+")");
      }
    }
                
    return label;
  }
  
}

}
