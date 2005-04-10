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
import printplugin.printer.JobFactory;
import printplugin.printer.PrintJob;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;


import devplugin.Channel;
import devplugin.Date;
import util.ui.ImageUtilities;
import util.ui.UiUtilities;


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


  public PrintDayProgramsDialog(final Frame parent, final PrinterJob printerJob, DayProgramScheme[] schemes) {
    super(parent, true);
    setTitle(mLocalizer.msg("dialogTitle","Tagesprogramme drucken"));
    JPanel contentPane = (JPanel)getContentPane();
    contentPane.setBorder(BorderFactory.createEmptyBorder(4,4,5,5));
    contentPane.setLayout(new BorderLayout());


    JTabbedPane tab = new JTabbedPane();

    JPanel listingsTab = new JPanel();
    tab.add(mLocalizer.msg("listings", "Daten"), listingsTab);
    tab.add(mLocalizer.msg("layout","Layout"), new JPanel());
    tab.add(mLocalizer.msg("misc","Extras"), new JPanel());

    listingsTab.setLayout(new BorderLayout());

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
    JButton deleteSchemeBtn = new JButton(ImageUtilities.createImageIconFromJar("printplugin/imgs/Delete16.gif", getClass()));
    newSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    deleteSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);


    JPanel schemePanel = new JPanel();
    schemePanel.add(new JLabel(mLocalizer.msg("scheme","Vorlage:")));
    schemePanel.add(new JComboBox(schemes));
    schemePanel.add(newSchemeBtn);
    schemePanel.add(deleteSchemeBtn);
    southPanel.add(schemePanel, BorderLayout.WEST);

    contentPane.add(tab, BorderLayout.CENTER);
    contentPane.add(eastPanel, BorderLayout.EAST);
    contentPane.add(southPanel, BorderLayout.SOUTH);



    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    mDatePanel = new DateRangePanel();
    mTimePanel = new TimeRangePanel();
    mChannelPanel = new ChannelSelectionPanel(parent, new Channel[]{});

    content.add(mDatePanel);
    content.add(mTimePanel);
    content.add(mChannelPanel);

    listingsTab.add(content, BorderLayout.NORTH);

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

  }

  public int getResult() {
    return mResult;
  }

  public PrintJob getPrintJob() {
    return JobFactory.createPrintJob(getSettings());
  }

  private DayProgramPrinterSettings getSettings() {
    //DayProgramScheme scheme = (DayProgramScheme)mSchemeCB.getSelectedItem();
    return new DayProgramPrinterSettings(){
      public Date getFromDay() {
        return mDatePanel.getFromDate();
      }

      public int getNumberOfDays() {
        return mDatePanel.getNumberOfDays();
      }

      public Channel[] getChannelList() {
        return mChannelPanel.getChannels();
      }

      public int getDayStartHour() {
        return mTimePanel.getFromTime();
      }

      public int getDayEndHour() {
        return mTimePanel.getToTime();
      }

      public PageFormat getPageFormat() {
        return mPageFormat;
      }

      public int getColumnCount() {
        return 5;
      }

      public int getChannelsPerColumn() {
        return 2;
      }
    };
  }




}
