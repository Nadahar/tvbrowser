
package printplugin;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import javax.swing.*;
import util.exc.*;
import util.ui.*;
import devplugin.*;
import javax.print.*;
import javax.print.DocFlavor.*;
import java.io.*;
import java.awt.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.OrientationRequested;

public class PrintDialog extends JDialog implements Printable
{

   private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(PrintPlugin.class);

   private JButton mPrintBt, mCloseBt, mTestBt;
   private JTextArea mTestText;
   private JScrollPane areaScrollPane;
   private JComboBox mPrinterSelect, mOrientation;
   private JComboBox mFromDate;
   private JComboBox mUntilDate;
   private JPanel mMain;

   Channel[] mChannels;
   int mChannelsPerPage, mChannelWidth, mChannelPageIndex, mPageIndex;
   Date mEndDate, mCountDate;

   public PrintDialog(Frame parent, PrintPlugin printPlugin)
   {
      super(parent,true);

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

         tmpPanel = new JPanel(new TabLayout(2));
         mMain.add(tmpPanel);

         tmpPanel.add(new JLabel(mLocalizer.msg("printer", "Printer")));
         tmpPanel.add(new JLabel(mLocalizer.msg("orientation", "Orientation")));

         this.mPrinterSelect = new JComboBox();
         for(int i = 0; i < printPlugin.mAllServices.length; i++)
         {
            mPrinterSelect.addItem(printPlugin.mAllServices[i].getName());
         }
         tmpPanel.add(mPrinterSelect);

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
            mFromDate.addItem(fromDate.toString());
            fromDate = fromDate.addDays(1);
         }
         tmpPanel.add(mFromDate);

         this.mUntilDate = new JComboBox();
         Date utilDate = new Date(startDate);
         while (utilDate.getValue() < endDate.getValue())
         {
            mUntilDate.addItem(utilDate.toString());
            utilDate = utilDate.addDays(1);
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
      try
      {
// Funktioniert nicht... hmm
//       setCursor(Cursor.WAIT_CURSOR);
//       displayPrintMsg();
//       UiUtilities.centerAndShow(this);


         Thread thread;
         Runnable printThread = new PrintThread(this);
         thread = new Thread (printThread);
         thread.start();

         while (thread.isAlive())
         {
            Thread.currentThread().yield();
            mMain.updateUI();
            repaint();
//          parent.repaint();
            thread.join(100);
         }
      }
      catch (InterruptedException e)
      {
      }
   }

   private void displayPrintMsg()
   {
      mMain.removeAll();

      mMain.add(new JLabel(mLocalizer.msg("dataprint", "dataPrint")));
      mMain.add(new JLabel(mLocalizer.msg("dataprint", "dataPrint")));
      mMain.add(new JLabel(mLocalizer.msg("dataprint", "dataPrint")));
      mMain.add(new JLabel(mLocalizer.msg("dataprint", "dataPrint")));

      pack();

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

         mOrientation.addItem(mLocalizer.msg("landscape", "Landscape"));

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
         if (pageIndex == 0)
         {
            mChannelWidth = 100;
            mChannelsPerPage = (int) pageFormat.getImageableWidth() / mChannelWidth;
            mChannelWidth = (int) pageFormat.getImageableWidth() / mChannelsPerPage;

            String countDate = (String)mFromDate.getSelectedItem();
            mCountDate = new Date();

            while(!countDate.equals(mCountDate.toString()))
            {
               mCountDate = mCountDate.addDays(1);
            }

            String endDate = (String)mUntilDate.getSelectedItem();
            mEndDate = new Date();
            while(!endDate.equals(mEndDate.toString()))
            {
               mEndDate = mEndDate.addDays(1);
            }

            mChannelPageIndex = 0;

         }

         if (mCountDate.getValue() <= mEndDate.getValue())
         {
            int channelIndex = 0;

            Font pageHeaderFont = new Font("SansSerif", Font.BOLD, 8);
            FontMetrics metrics = g.getFontMetrics(pageHeaderFont);
            String pageHeader = mLocalizer.msg("programFrom", "program from") + mCountDate.toString();
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
      catch (Exception e)
      {
         JOptionPane.showMessageDialog(null, e.toString());
      }

      mPageIndex = pageIndex;

      return result;
   }

}
