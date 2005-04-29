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

package printplugin;

import devplugin.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.util.Properties;
import java.io.*;
import javax.swing.*;
import printplugin.dlgs.MainPrintDialog;
import printplugin.dlgs.SettingsDialog;
import printplugin.dlgs.DialogContent;
import printplugin.dlgs.printfromqueuedialog.PrintFromQueueDialog;
import printplugin.dlgs.printdayprogramsdialog.PrintDayProgramsDialogContent;
import printplugin.settings.DayProgramScheme;
import printplugin.settings.DayProgramPrinterSettingsImpl;
import printplugin.printer.PrintJob;
import util.ui.UiUtilities;


public class PrintPlugin extends Plugin {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(PrintPlugin.class);


  private static final String SCHEME_FILE = "printplugin.schemes";

  public PrintPlugin() {
  }

  
  public String getMarkIconName() {
    return "printplugin/imgs/Print16.gif";
  }

  public PluginInfo getInfo() {
    String name = mLocalizer.msg("printProgram" ,"Print program");
    String desc = mLocalizer.msg("printdescription" ,"Allows printing programs.");
    String author = "Martin Oberhauser (martin@tvbrowser.org)";

    return new PluginInfo(name, desc, author, new Version(0, 9));
  }


  public ActionMenu getContextMenuActions(final Program program) {
    ContextMenuAction action = new ContextMenuAction();
    action.setText(mLocalizer.msg("addToPrinterQueue","Zur Druckerwarteschlange hinzufuegen"));
    action.setSmallIcon(createImageIcon("printplugin/imgs/Print16.gif"));
    action.setActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        getRootNode().addProgram(program);
        getRootNode().update();
      }
    });
    return new ActionMenu(action);
  }


  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        MainPrintDialog dlg = new MainPrintDialog(getParentFrame());
        UiUtilities.centerAndShow(dlg);
        int result = dlg.getResult();

        if (result == MainPrintDialog.PRINT_DAYPROGRAMS) {
          showPrintDialog(new PrintDayProgramsDialogContent(getParentFrame()));
          //storeSchemes(settingsDialog.getSchemes());
        }
        else if (result == MainPrintDialog.PRINT_QUEUE) {
          showPrintDialog(new PrintFromQueueDialog());
        }
      }
    };

    action.putValue(Action.NAME, mLocalizer.msg("print","Print"));
    action.putValue(Action.SMALL_ICON, createImageIcon("printplugin/imgs/Print16.gif"));
    action.putValue(BIG_ICON, createImageIcon("printplugin/imgs/Print24.gif"));
    action.putValue(Action.SHORT_DESCRIPTION, getInfo().getDescription());

    return new ActionMenu(action);
  }


  private void showPrintDialog(DialogContent content) {
    PrinterJob printerJob = PrinterJob.getPrinterJob();
    SettingsDialog settingsDialog = new SettingsDialog(getParentFrame(), printerJob, loadDayProgramSchemes(), content);
    UiUtilities.centerAndShow(settingsDialog);

    if (settingsDialog.getResult() == SettingsDialog.OK) {
      PrintJob job = settingsDialog.getPrintJob();
      try {
        printerJob.setPrintable(job.getPrintable());
        printerJob.print();
      } catch (PrinterException e) {
        util.exc.ErrorHandler.handle("Could not print pages: "+e.getLocalizedMessage(), e);
      }
    }
  }


  private void storeSchemes(DayProgramScheme[] schemes) {
    String home = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome();
    File schemeFile = new File(home,SCHEME_FILE);
    ObjectOutputStream out=null;
    try {
      out = new ObjectOutputStream(new FileOutputStream(schemeFile));
      out.writeInt(1);  // version
      out.writeInt(schemes.length);
      for (int i=0; i<schemes.length; i++) {
        out.writeObject(schemes[i].getName());
        schemes[i].store(out);
      }
      out.close();
    }catch(IOException e) {

    }
  }

  public void receivePrograms(Program[] programArr) {
    PluginTreeNode rootNode = getRootNode();
    for (int i=0; i<programArr.length; i++) {
      rootNode.addProgram(programArr[i]);
    }
    rootNode.update();
  }


  public boolean canReceivePrograms() {
    return true;
  }

  public boolean canUseProgramTree() {
    return true;
  }


  private DayProgramScheme[] readSchemes(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt();  // read version
    int cnt = in.readInt();
    DayProgramScheme[] schemes = new DayProgramScheme[cnt];
    for (int i=0; i<cnt; i++) {
      String name = (String)in.readObject();
      schemes[i] = new DayProgramScheme(name);
      schemes[i].read(in);
    }
    return schemes;
  }

  private DayProgramScheme[] loadDayProgramSchemes() {
    String home = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome();
    File schemeFile = new File(home,SCHEME_FILE);
    ObjectInputStream in=null;
    try {
      in = new ObjectInputStream(new FileInputStream(schemeFile));
      DayProgramScheme[] schemes = readSchemes(in);
      in.close();
      return schemes;
    }catch(Exception e) {
      if (in != null) {
        try { in.close(); } catch(IOException exc) {}
      }
      DayProgramScheme scheme = new DayProgramScheme(mLocalizer.msg("defaultScheme","DefaultScheme"));
      scheme.setSettings(new DayProgramPrinterSettingsImpl(new Date(), 3, null, 6, 24+3, 5, 2));
      return new DayProgramScheme[]{scheme};
    }

    /*
    DayProgramScheme scheme = new DayProgramScheme(mLocalizer.msg("defaultScheme","DefaultScheme"));

    scheme.setSettings(new DayProgramPrinterSettings(){
      public Date getFromDay() {
        return new Date();
      }

      public int getNumberOfDays() {
        return 7;
      }

      public Channel[] getChannelList() {
        return null;
      }

      public int getDayStartHour() {
        return 6;
      }

      public int getDayEndHour() {
        return 24+3;
      }

      public PageFormat getPageFormat() {
        return null;
      }

      public int getColumnCount() {
        return 5;
      }

      public int getChannelsPerColumn() {
        return 2;
      }
    });
    return new DayProgramScheme[]{scheme};   */
  }


  public void loadSettings(Properties settings) {

  }


}
