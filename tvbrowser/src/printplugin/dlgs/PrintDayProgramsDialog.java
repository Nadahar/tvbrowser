/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

import printplugin.dlgs.components.DateRangePanel;
import printplugin.dlgs.components.TimeRangePanel;
import printplugin.dlgs.components.ChannelSelectionPanel;
import printplugin.settings.DayProgramPrinterSettings;
import printplugin.settings.DayProgramScheme;
import printplugin.settings.DayProgramPrinterSettingsImpl;
import printplugin.printer.JobFactory;
import printplugin.printer.PrintJob;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.ArrayList;


import devplugin.Channel;
import devplugin.Date;
import util.ui.ImageUtilities;
import util.ui.UiUtilities;
import util.ui.TabLayout;


public class PrintDayProgramsDialog extends JDialog {

  /** The localizer for this class. */
    private static final util.ui.Localizer mLocalizer
        = util.ui.Localizer.getLocalizerFor(PrintDayProgramsDialog.class);


  public static final int OK = 1;
  public static final int CANCEL = 0;

  private ChannelSelectionPanel mChannelPanel;
  private TimeRangePanel mTimePanel;
  private DateRangePanel mDatePanel;
  private PageFormat mPageFormat;

  private int mResult;

  private LayoutTab mLayoutTab;
  private DefaultComboBoxModel mSchemeCBModel;
  private JComboBox mSchemeCB;
  private JButton mDeleteSchemeBtn, mSaveSchemeBtn, mEditSchemeBtn;


  public PrintDayProgramsDialog(final Frame parent, final PrinterJob printerJob, DayProgramScheme[] schemes) {
    super(parent, true);
    setTitle(mLocalizer.msg("dialogTitle","Tagesprogramme drucken"));
    JPanel contentPane = (JPanel)getContentPane();
    contentPane.setBorder(BorderFactory.createEmptyBorder(4,4,5,5));
    contentPane.setLayout(new BorderLayout());


    JTabbedPane tab = new JTabbedPane();

    JPanel listingsTab = new ListingsTab(parent);
    mLayoutTab = new LayoutTab();
    tab.add(mLocalizer.msg("listingsTab", "Daten"), listingsTab);
    tab.add(mLocalizer.msg("layoutTab","Layout"), mLayoutTab);
    tab.add(mLocalizer.msg("miscTab","Extras"), new JPanel());

    JPanel eastPanel = new JPanel(new BorderLayout());
    JPanel eastBtnPanel = new JPanel();
    eastBtnPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
    eastBtnPanel.setLayout(new GridLayout(-1, 1));

    JButton printerSetupBtn = new JButton(mLocalizer.msg("printer","Drucker")+"...",ImageUtilities.createImageIconFromJar("printplugin/imgs/PageSetup16.gif", getClass()));
    JButton pageBtn = new JButton(mLocalizer.msg("page","Seite")+"...", ImageUtilities.createImageIconFromJar("printplugin/imgs/Properties16.gif", getClass()));
    JButton previewBtn = new JButton(mLocalizer.msg("preview","Vorschau")+"...", ImageUtilities.createImageIconFromJar("printplugin/imgs/PrintPreview16.gif", getClass()));

    printerSetupBtn.setHorizontalAlignment(SwingConstants.LEFT);
    pageBtn.setHorizontalAlignment(SwingConstants.LEFT);
    previewBtn.setHorizontalAlignment(SwingConstants.LEFT);



    eastBtnPanel.add(printerSetupBtn);
    eastBtnPanel.add(pageBtn);
    eastBtnPanel.add(previewBtn);
    eastPanel.add(eastBtnPanel, BorderLayout.NORTH);


    JPanel southPanel = new JPanel(new BorderLayout());
    JPanel okCancelBtnPanel = new JPanel(new FlowLayout());
    JButton printBt = new JButton(mLocalizer.msg("print","Drucken"));
    JButton cancelBt = new JButton(mLocalizer.msg("cancel","Abbrechen"));
    okCancelBtnPanel.add(printBt);
    okCancelBtnPanel.add(cancelBt);
    southPanel.add(okCancelBtnPanel, BorderLayout.EAST);

    printerSetupBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        Thread thread = new Thread(){
          public void run(){
            printerJob.printDialog();
          }
        };
        thread.start();
      }
    });

    pageBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Thread thread = new Thread(){
          public void run(){
            if (mPageFormat == null) {
              mPageFormat = printerJob.defaultPage();
            }
            mPageFormat = printerJob.pageDialog(mPageFormat);
          }
        };
        thread.start();
      }
    });

    previewBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
            PrintJob job = getPrintJob();
            if (job.getNumOfPages()==0) {
              JOptionPane.showMessageDialog(parent, mLocalizer.msg("noPagesToPrint","Es sind keine Seiten zu drucken."));
            }
            else {
              PreviewDlg dlg = new PreviewDlg(parent, job.getPrintable(), mPageFormat, job.getNumOfPages());
              util.ui.UiUtilities.centerAndShow(dlg);
            }
          }
        });


    printBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mResult = OK;
        hide();
      }
    });

    cancelBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mResult = CANCEL;
        hide();
      }
    });



    JButton newSchemeBtn = new JButton(ImageUtilities.createImageIconFromJar("printplugin/imgs/New16.gif", getClass()));
    mEditSchemeBtn = new JButton(ImageUtilities.createImageIconFromJar("printplugin/imgs/Edit16.gif", getClass()));
    mDeleteSchemeBtn = new JButton(ImageUtilities.createImageIconFromJar("printplugin/imgs/Delete16.gif", getClass()));
    mSaveSchemeBtn = new JButton(ImageUtilities.createImageIconFromJar("printplugin/imgs/Save16.gif", getClass()));
    newSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    mDeleteSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    mEditSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    mSaveSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    mDeleteSchemeBtn.setEnabled(false);
    mEditSchemeBtn.setEnabled(false);
    newSchemeBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String newSchemeName = JOptionPane.showInputDialog(parent, "Enter new name for scheme:", "New Scheme", JOptionPane.PLAIN_MESSAGE);
        if (newSchemeName != null) {
          DayProgramScheme newScheme = new DayProgramScheme(newSchemeName);
          newScheme.setSettings(getSettings());
          mSchemeCBModel.addElement(newScheme);
          mSchemeCB.setSelectedItem(newScheme);
          setScheme(newScheme);
        }
      }
    });

    mSaveSchemeBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        DayProgramScheme scheme = (DayProgramScheme)mSchemeCB.getSelectedItem();
        scheme.setSettings(getSettings());

      }
    });

    mEditSchemeBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        DayProgramScheme scheme = (DayProgramScheme)mSchemeCB.getSelectedItem();
        Object newSchemeName = JOptionPane.showInputDialog(parent, "Enter new name for scheme:", "New Scheme", JOptionPane.PLAIN_MESSAGE, null, null, scheme.getName());
        if (newSchemeName != null) {
          scheme.setName(newSchemeName.toString());
          mSchemeCB.updateUI();
        }
      }
    });

    mSchemeCBModel = new DefaultComboBoxModel(schemes);
    mSchemeCB = new JComboBox(mSchemeCBModel);
    JPanel schemePanel = new JPanel();
    //schemePanel.add(new JLabel(mLocalizer.msg("scheme","Vorlage:")));
    schemePanel.add(mSchemeCB);
    schemePanel.add(newSchemeBtn);
    schemePanel.add(mEditSchemeBtn);
    schemePanel.add(mSaveSchemeBtn);
    schemePanel.add(mDeleteSchemeBtn);
    southPanel.add(schemePanel, BorderLayout.WEST);
    mSchemeCB.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        mDeleteSchemeBtn.setEnabled(mSchemeCB.getSelectedIndex()!=0);
        mEditSchemeBtn.setEnabled(mSchemeCB.getSelectedIndex()!=0);
        DayProgramScheme scheme = (DayProgramScheme)mSchemeCB.getSelectedItem();
        setScheme(scheme);
      }
    });

    contentPane.add(tab, BorderLayout.CENTER);
    contentPane.add(eastPanel, BorderLayout.EAST);
    contentPane.add(southPanel, BorderLayout.SOUTH);

    setScheme(schemes[0]);
    mPageFormat = schemes[0].getSettings().getPageFormat();
    if (mPageFormat == null) {
      mPageFormat = printerJob.defaultPage();
    }

    setSize(450,400);

    mResult = CANCEL;
  }


  private void setScheme(DayProgramScheme scheme) {
    DayProgramPrinterSettings settings = scheme.getSettings();
    Channel[] ch = settings.getChannelList();
    mChannelPanel.setChannels(ch);
    int start = settings.getDayStartHour();
    int end = settings.getDayEndHour();
    mTimePanel.setRange(start, end);
    Date from = settings.getFromDay();
    mDatePanel.setFromDate(from);
    mDatePanel.setNumberOfDays(settings.getNumberOfDays());
    mLayoutTab.setColumnLayout(settings.getColumnCount(), settings.getChannelsPerColumn());
  }




  public int getResult() {
    return mResult;
  }

  public PrintJob getPrintJob() {
    return JobFactory.createPrintJob(getSettings());
  }

  public DayProgramScheme[] getSchemes() {
    DayProgramScheme[] result = new DayProgramScheme[mSchemeCBModel.getSize()];
    for (int i=0; i<result.length; i++) {
      result[i] = (DayProgramScheme)mSchemeCBModel.getElementAt(i);
    }
    return result;
  }

  private DayProgramPrinterSettings getSettings() {
    return new DayProgramPrinterSettingsImpl(mDatePanel.getFromDate(),
        mDatePanel.getNumberOfDays(),
        mChannelPanel.getChannels(),
        mTimePanel.getFromTime(),
        mTimePanel.getToTime(),
        mPageFormat,
        mLayoutTab.getColumnsPerPage(),
        mLayoutTab.getChannelsPerColumn()
        );
  }



  class ListingsTab extends JPanel {

    public ListingsTab(Frame parentFrame) {
      super();
      setLayout(new BorderLayout());

      JPanel content = new JPanel();
      content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
      mDatePanel = new DateRangePanel();
      mTimePanel = new TimeRangePanel();
      mChannelPanel = new ChannelSelectionPanel(parentFrame, new Channel[]{});

      content.add(mDatePanel);
      content.add(mTimePanel);
      content.add(mChannelPanel);

      add(content, BorderLayout.NORTH);
    }

  }

  class LayoutTab extends JPanel {

    private JComboBox mChannelsPerPageCB;
    private JComboBox mLayoutCB;
    private DefaultComboBoxModel mLayoutCBModel;

    public LayoutTab() {
      super();
      setLayout(new BorderLayout());

      JPanel content = new JPanel();
      content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

      mChannelsPerPageCB = new JComboBox(createIntegerArray(2,22));
      mLayoutCBModel = new DefaultComboBoxModel();
      mLayoutCB = new JComboBox(mLayoutCBModel);
      JPanel columnsPanel = new JPanel(new TabLayout(2));
      columnsPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("channelsAndColumns","Channels and columns")));
      columnsPanel.add(new JLabel(mLocalizer.msg("channelsPerPage","Channels per page")+":"));
      columnsPanel.add(mChannelsPerPageCB);
      columnsPanel.add(new JLabel(mLocalizer.msg("columnsPerPage","columns")+":"));
      columnsPanel.add(mLayoutCB);

      mChannelsPerPageCB.addItemListener(new ItemListener(){
        public void itemStateChanged(ItemEvent e) {
          int val = ((Integer)mChannelsPerPageCB.getSelectedItem()).intValue();
          updateLayoutCombobox(val);
        }
      });

      content.add(columnsPanel);
      add(content, BorderLayout.NORTH);
    }


    public void setColumnLayout(int columnsPerPage, int channelsPerColumn) {
      int channelsPerPage = columnsPerPage*channelsPerColumn;
      mChannelsPerPageCB.setSelectedItem(new Integer(channelsPerPage));
      for (int i=0; i<mLayoutCBModel.getSize(); i++) {
        LayoutOption option = (LayoutOption)mLayoutCBModel.getElementAt(i);
        if (channelsPerColumn == option.getChannelsPerColumn()) {
          mLayoutCB.setSelectedItem(option);
          break;
        }
      }
    }

    public int getColumnsPerPage() {
      LayoutOption option = (LayoutOption)mLayoutCB.getSelectedItem();
      return option.getChannelsPerPage() / option.getChannelsPerColumn();
    }

    public int getChannelsPerColumn() {
      LayoutOption option = (LayoutOption)mLayoutCB.getSelectedItem();
      return option.getChannelsPerColumn();
    }
    private Integer[] createIntegerArray(int from, int cnt) {
      Integer[] result = new Integer[cnt];
      for (int i=0; i<result.length; i++) {
        result[i] = new Integer(i+from);
      }
      return result;
    }

    private void updateLayoutCombobox(int val) {
      mLayoutCBModel.removeAllElements();
      int[] primes = getPrimes(val);
      for (int i=0; i<primes.length; i++) {
        mLayoutCBModel.addElement(new LayoutOption(val, primes[i]));
      }

    }

    private int[] getPrimes(int val) {
      ArrayList list = new ArrayList();
      for (int i=1; i<=val/2; i++) {
        if (val%i==0) {
          list.add(new Integer(i));
        }
      }
      int[] result = new int[list.size()];
      for (int i=0; i<list.size(); i++) {
        result[i] = ((Integer)list.get(i)).intValue();
      }
      return result;
    }

  }


  class LayoutOption {

    private int mChannelsPerPage, mChannelsPerColumn;

    public LayoutOption(int channelsPerPage, int channelsPerColumn) {
      mChannelsPerPage = channelsPerPage;
      mChannelsPerColumn = channelsPerColumn;
    }

    public int getChannelsPerColumn() {
      return mChannelsPerColumn;
    }

    public int getChannelsPerPage() {
      return mChannelsPerPage;
    }

    public String toString() {
      int columns = mChannelsPerPage/mChannelsPerColumn;
      String s = mLocalizer.msg("layoutString", "{0} ({1} channels per column))", new Integer(columns), new Integer(mChannelsPerColumn));
      return s;
    }
  }




}
