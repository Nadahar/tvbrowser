/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * PrintPlugin
 * Copyright (C) 08-2003 Robert Inzinger (yxterwd@users.sourceforge.net)
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

package printplugin;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.Enumeration;
import javax.swing.*;
import util.ui.*;
import devplugin.*;
import javax.print.*;
import java.awt.print.*;
import javax.print.attribute.*;
import tvbrowser.ui.programtable.*;
//tvbrowser.ui.programtable
import javax.print.attribute.standard.OrientationRequested;

/**
 * Print Dialog
 *
 * @author Robert Inzinger
 */

public class PrintDialog extends JDialog implements Printable
{

   private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(PrintPlugin.class);

   private JButton mPrintBt, mCloseBt, mTestBt;
   private JTextArea mTestText;
   private JScrollPane areaScrollPane;
   private JComboBox mPrinterSelect, mOrientation, mFilter;
   private JComboBox mFromDate;
   private JComboBox mUntilDate;
   private JPanel mMain;
   private static PrintDialog mInstance;
   private ProgramFilter mSelectedFilter;
   private PrintFilter mPrintFilter;
   private int mFilterCount, mFilterCounter;


   Channel[] mChannels;
   int mChannelsPerPage, mChannelWidth, mChannelPageIndex, mPageIndex;
   Date mEndDate, mCountDate;

   public PrintDialog(Frame parent, PrintPlugin printPlugin)
   {
      super(parent,true);

      mInstance = this;

      JPanel tmpPanel, subtmpPanel;

      try
      {
         setTitle(mLocalizer.msg("printProgram", "Print program"));

   //    PrintService[] allServices = PrintServiceLookup.lookupPrintServices(null, null);

         mChannels = Plugin.getPluginManager().getSubscribedChannels();

         Date startDate, endDate;

         startDate = new Date();
         endDate = new Date();

         Iterator programIter = Plugin.getPluginManager().getChannelDayProgram(endDate, mChannels[0]);

         while(programIter != null)
         {
            endDate = endDate.addDays(1);
            programIter = Plugin.getPluginManager().getChannelDayProgram(endDate, mChannels[0]);
         }

         mMain = new JPanel(new TabLayout(1));
         mMain.setBorder(UiUtilities.DIALOG_BORDER);
         setContentPane(mMain);

         mMain.add(new JLabel(mLocalizer.msg("printer", "Printer")));
         this.mPrinterSelect = new JComboBox();
         for(int i = 0; i < printPlugin.mAllServices.length; i++)
         {
            mPrinterSelect.addItem(printPlugin.mAllServices[i].getName());
         }
         mMain.add(mPrinterSelect);


         tmpPanel = new JPanel(new TabLayout(2));
         mMain.add(tmpPanel);

         tmpPanel.add(new JLabel(mLocalizer.msg("filter", "Filter")));
         tmpPanel.add(new JLabel(mLocalizer.msg("orientation", "Orientation")));

         this.mFilter = new JComboBox();

         ProgramFilter allProgs = null;

         for (Enumeration e = tvbrowser.ui.filter.FilterListModel.getInstance().elements() ; e.hasMoreElements() ;)
         {
            ProgramFilter pf = (ProgramFilter) e.nextElement();
            mFilter.addItem(pf);

            if (pf instanceof tvbrowser.ui.filter.ShowAllFilter)
            {
               allProgs = pf;;
            }
         }

         if (allProgs != null)
            mFilter.setSelectedItem(allProgs);

         tmpPanel.add(mFilter);

         this.mOrientation = new JComboBox();
         mOrientation.addItem(mLocalizer.msg("portrait", "Portrait"));
         mOrientation.addItem(mLocalizer.msg("landscape", "Landscape"));
         tmpPanel.add(mOrientation);

         tmpPanel = new JPanel(new TabLayout(2));
         mMain.add(tmpPanel);

         tmpPanel.add(new JLabel(mLocalizer.msg("from", "printing from date")));
         tmpPanel.add(new JLabel(mLocalizer.msg("until", "printing until date")));

         this.mFromDate = new JComboBox();
         Date fromDate = new Date(startDate);
         while (fromDate.getValue() < endDate.getValue())
         {
            mFromDate.addItem(fromDate);
            fromDate = fromDate.addDays(1);
         }
         tmpPanel.add(mFromDate);

         this.mUntilDate = new JComboBox();
         Date untilDate = new Date(startDate);
         while (untilDate.getValue() < endDate.getValue())
         {
            mUntilDate.addItem(untilDate);
            untilDate = untilDate.addDays(1);
         }
         tmpPanel.add(mUntilDate);


/*
   //    Only for Test!
         JTextArea mTestText = new JTextArea(10, 50);
         mTestText.setLineWrap(true);
         tmpPanel.add(mTestText);

         JScrollPane areaScrollPane = new JScrollPane(mTestText);
         areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         areaScrollPane.setPreferredSize(new Dimension(250, 250));

         tmpPanel.add(areaScrollPane);

         for (Enumeration e = tvbrowser.ui.filter.FilterListModel.getInstance().elements() ; e.hasMoreElements() ;)
         {
            ProgramFilter pf = (ProgramFilter) e.nextElement();

            mTestText.append(pf.toString());

            if (pf instanceof tvbrowser.ui.filter.ShowAllFilter)
            {
               mTestText.append(" (Alle Daten)");
            }
            else
            {
            }

            mTestText.append("\n");
         }
*/

         JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
         mMain.add(buttonPn);

         mPrintBt = new JButton(mLocalizer.msg("print", "print"));
         mPrintBt.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               mChannels = Plugin.getPluginManager().getSubscribedChannels();
               mPageIndex = -1;

               startPrintThread();
//             print();

               dispose();
            }
         });
         buttonPn.add(mPrintBt);
         getRootPane().setDefaultButton(mPrintBt);


         mCloseBt = new JButton(mLocalizer.msg("cancel", "cancel"));
         mCloseBt.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               dispose();
            }
         });

         buttonPn.add(mCloseBt);

   /*
   // Only for Test
         mTestBt = new JButton("Test");
         mTestBt.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               displayPrintMsg();
            }
         });
         buttonPn.add(mTestBt);
   */

         pack();
      }
      catch (Exception e)
      {
         JOptionPane.showMessageDialog(null, e.toString());
      }
   }

   private void startPrintThread()
   {
      Thread printThread = new Thread()
      {
         public void run()
         {
            PrintDialog.getInstance().displayPrintMsg();
            PrintDialog.getInstance().print();
         }
      };
      printThread.start();
   }

   private void displayPrintMsg()
   {
      mMain.removeAll();

      mMain.add(new JLabel(mLocalizer.msg("dataprint", "dataPrint")));

      pack();

   }

   public static PrintDialog getInstance()
   {
      return mInstance;
   }

   public void print()
   {
      try
      {
         String printerName = (String)mPrinterSelect.getSelectedItem();
         int i;

         PrintService[] allServices = PrintServiceLookup.lookupPrintServices(null, null);
         for (i = 0; i <allServices.length; i++)
         {
            if (allServices[i].getName() == printerName)
            {
               break;
            }
         }

         DocPrintJob job = allServices[i].createPrintJob();

         mSelectedFilter = (ProgramFilter) mFilter.getSelectedItem();

         if (!(mSelectedFilter instanceof tvbrowser.ui.filter.ShowAllFilter))
         {
            mPrintFilter = new PrintFilter("SansSerif", 8, 6, mSelectedFilter);

            mFilterCount = mPrintFilter.readPrograms(new Date((Date) mFromDate.getSelectedItem()),
                                                     new Date((Date) mUntilDate.getSelectedItem()));
            mFilterCounter = 0;

            if (mFilterCount == 0)
            {
               JOptionPane.showMessageDialog(null,
                                             mLocalizer.msg("nodata" ,"No data"),
                                             mLocalizer.msg("printProgram" ,"Print program"),
                                             JOptionPane.ERROR_MESSAGE);
               return;

            }

         }


//         mOrientation.addItem(mLocalizer.msg("landscape", "Landscape"));

         Doc doc;

         if ((String)mOrientation.getSelectedItem() == mLocalizer.msg("landscape", "Landscape"))
         {
            DocAttributeSet docAttributes = new HashDocAttributeSet();
            docAttributes.add(OrientationRequested.LANDSCAPE);
            doc = new SimpleDoc(this, DocFlavor.SERVICE_FORMATTED.PRINTABLE , docAttributes);
         }
         else
            doc = new SimpleDoc(this, DocFlavor.SERVICE_FORMATTED.PRINTABLE , null);

         job.print(doc, null);
      }
      catch (PrintException e)
      {
         JOptionPane.showMessageDialog(null, e.toString());
      }

   }

   public int print(Graphics g, PageFormat pageFormat, int pageIndex)   throws PrinterException
   {
      int result = NO_SUCH_PAGE;

      try
      {
         if (mSelectedFilter instanceof tvbrowser.ui.filter.ShowAllFilter)
         {
            if (pageIndex == 0)
            {
               mChannelWidth = 100;
               mChannelsPerPage = (int) pageFormat.getImageableWidth() / mChannelWidth;
               mChannelWidth = (int) pageFormat.getImageableWidth() / mChannelsPerPage;

               mCountDate = new Date((Date) mFromDate.getSelectedItem());
               mEndDate = new Date((Date) mUntilDate.getSelectedItem());

               mChannelPageIndex = 0;

            }

            if (mCountDate.getValue() <= mEndDate.getValue())
            {
               int channelIndex = 0;

               Font pageHeaderFont = new Font("SansSerif", Font.BOLD, 8);
               FontMetrics metrics = g.getFontMetrics(pageHeaderFont);
               String pageHeader = mLocalizer.msg("programFrom", "program from") + " " + mCountDate.toString();
               int pageWidth = metrics.stringWidth(pageHeader);
               int pageX = (int) pageFormat.getImageableX() + ((int) pageFormat.getImageableWidth() / 2) - (pageWidth /2 );

               for (int i = 0; i < mChannelsPerPage; i++)
               {

                  channelIndex = (mChannelPageIndex *  mChannelsPerPage) + i;
                  if (channelIndex < mChannels.length)
                  {

                     g.setFont(pageHeaderFont);
                     g.drawString(pageHeader, pageX, (int) pageFormat.getImageableY() + pageHeaderFont.getSize());

                     PrintChannel printChannel = new PrintChannel(mChannels[channelIndex], "SansSerif", 8, 6, g);
                     printChannel.draw((int) (pageFormat.getImageableX() + (i * mChannelWidth)),
                                       (int) (pageFormat.getImageableY() + (pageHeaderFont.getSize() + pageHeaderFont.getSize() * 0.4)),
                                       mChannelWidth, (int) (pageFormat.getImageableHeight() - (pageHeaderFont.getSize() + pageHeaderFont.getSize() * 0.4)), mCountDate, true, false);
                  }
               }

               if (pageIndex == mPageIndex)
               {
                  if (channelIndex + 1 >= mChannels.length)
                  {
                     mChannelPageIndex = 0;
                     mCountDate = mCountDate.addDays(1);
   //                JOptionPane.showMessageDialog(null, "Date change to "  + mCountDate.toString());
                  }
                  else
                  {
                     mChannelPageIndex++;
                  }
               }

               result = PAGE_EXISTS;
            }
         }
         else
         {

            if (mFilterCounter < mFilterCount)
            {

               int fc;

               fc = mFilterCounter;

               fc = mPrintFilter.draw((int) pageFormat.getImageableX(),
                                      (int) pageFormat.getImageableY(),
                                      (int) pageFormat.getImageableWidth(),
                                      (int) pageFormat.getImageableHeight(),
                                      g,
                                      fc);

               if (pageIndex == mPageIndex)
               {
                  mFilterCounter = fc;
               }

               result = PAGE_EXISTS;
            }

//            JOptionPane.showMessageDialog(null, "Printing of Filter is not implented.");
         }
      }
      catch (Exception e)
      {
         JOptionPane.showMessageDialog(null, e.toString());
      }

      mPageIndex = pageIndex;

      return result;
   }

}
