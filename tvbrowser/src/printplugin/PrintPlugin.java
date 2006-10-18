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

import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;

import printplugin.dlgs.DialogContent;
import printplugin.dlgs.MainPrintDialog;
import printplugin.dlgs.ProgramInfoPrintDialog;
import printplugin.dlgs.SettingsDialog;
import printplugin.dlgs.printdayprogramsdialog.PrintDayProgramsDialogContent;
import printplugin.dlgs.printfromqueuedialog.PrintFromQueueDialogContent;
import printplugin.printer.PrintJob;
import printplugin.settings.DayProgramPrinterSettings;
import printplugin.settings.DayProgramScheme;
import printplugin.settings.PrinterProgramIconSettings;
import printplugin.settings.ProgramInfoPrintSettings;
import printplugin.settings.QueuePrinterSettings;
import printplugin.settings.QueueScheme;
import printplugin.settings.Scheme;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ThemeIcon;
import devplugin.Version;


public class PrintPlugin extends Plugin {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(PrintPlugin.class);

  private static final String SCHEME_FILE_DAYPROGRAM = "printplugin.dayprog.schemes";
  private static final String SCHEME_FILE_QUEUE = "printplugin.queue.schemes";

  private static PrintPlugin mInstance;

  /** Global Settings for the PrintPlugin */
  private Properties mSettings;
  
  public PrintPlugin() {
    mInstance = this;
  }

  public static PrintPlugin getInstance() {
    return mInstance;
  }

  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("devices", "printer", 16);
  }
  
  public PluginInfo getInfo() {
    String name = mLocalizer.msg("printProgram" ,"Print program");
    String desc = mLocalizer.msg("printdescription" ,"Allows printing programs.");
    String author = "Martin Oberhauser (martin@tvbrowser.org)";

    return new PluginInfo(name, desc, author, new Version(2,1));
  }

  public void onActivation() {
    PluginTreeNode root = getRootNode();
    Program[] progs = root.getPrograms();
    for (int i=0; i<progs.length; i++) {
      progs[i].mark(this);
    }
    root.update();
    root.addAction(new EmptyQueueAction());
  }

  public ActionMenu getContextMenuActions(final Program program) {
    final Plugin thisPlugin = this;
    ContextMenuAction menu = new ContextMenuAction();
    menu.setSmallIcon(createImageIcon("devices", "printer", 16));
    menu.setText(mLocalizer.msg("printProgram","Print"));
    
    Action[] action = new AbstractAction[2];
    
    if (getRootNode().contains(program)) {
      action[0] = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          getRootNode().removeProgram(program);
          getRootNode().update();
          program.unmark(thisPlugin);
        }
      };
      action[0].putValue(Action.NAME,mLocalizer.msg("removeFromPrinterQueue","Aus der Druckerwarteschlange loeschen"));
      action[0].putValue(AbstractAction.SMALL_ICON, createImageIcon("devices", "printer", 16));
    }
    else {
      action[0] = new AbstractAction() {
        public void actionPerformed(ActionEvent event) {
          getRootNode().addProgram(program);
          getRootNode().update();
          program.mark(thisPlugin);
        }
      };
      action[0].putValue(Action.NAME,mLocalizer.msg("addToPrinterQueue","Zur Druckerwarteschlange hinzufuegen"));
      action[0].putValue(AbstractAction.SMALL_ICON, createImageIcon("devices", "printer", 16));
    }
    action[1] = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Window w = UiUtilities.getLastModalChildOf(getParentFrame());
        if(w instanceof JDialog)
          new ProgramInfoPrintDialog((JDialog)w, program);
        else if(w instanceof JFrame)
          new ProgramInfoPrintDialog((JFrame)w, program);
      }
    };
    action[1].putValue(Action.NAME, mLocalizer.msg("printProgramInfo","Print program info"));
    action[1].putValue(AbstractAction.SMALL_ICON, createImageIcon("devices", "printer", 16));
    
    return new ActionMenu(menu,action);
  }

  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        MainPrintDialog mainDialog = new MainPrintDialog(getParentFrame());
        UiUtilities.centerAndShow(mainDialog);
        int result = mainDialog.getResult();

        if (result == MainPrintDialog.PRINT_DAYPROGRAMS) {
          SettingsDialog dlg = showPrintDialog(new PrintDayProgramsDialogContent(getParentFrame()), loadDayProgramSchemes());
          storeDayProgramSchemes(dlg.getSchemes());
        }
        else if (result == MainPrintDialog.PRINT_QUEUE) {
          SettingsDialog dlg = showPrintDialog(new PrintFromQueueDialogContent(getRootNode(), getParentFrame()), loadQueueSchemes());
          storeQueueSchemes(dlg.getSchemes());
        }
      }
    };

    action.putValue(Action.NAME, mLocalizer.msg("print","Print"));
    action.putValue(Action.SMALL_ICON, createImageIcon("devices", "printer", 16));
    action.putValue(BIG_ICON, createImageIcon("devices", "printer", 22));
    action.putValue(Action.SHORT_DESCRIPTION, getInfo().getDescription());

    return new ActionMenu(action);
  }


  private SettingsDialog showPrintDialog(DialogContent content, Scheme[] schemes) {
    PrinterJob printerJob = PrinterJob.getPrinterJob();
    SettingsDialog settingsDialog = new SettingsDialog(getParentFrame(), printerJob, schemes, content);
    UiUtilities.centerAndShow(settingsDialog);

    if (settingsDialog.getResult() == SettingsDialog.OK) {
      PrintJob job = settingsDialog.getPrintJob();
      try {
        printerJob.setPrintable(job.getPrintable(), job.getPageFormat());
        printerJob.print();
        settingsDialog.printingDone();
      } catch (PrinterException e) {
        util.exc.ErrorHandler.handle("Could not print pages: "+e.getLocalizedMessage(), e);
      }
    }
    return settingsDialog;
  }


  private void storeDayProgramSchemes(Scheme[] schemes) {
    String home = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome();
    File schemeFile = new File(home,SCHEME_FILE_DAYPROGRAM);
    ObjectOutputStream out=null;
    try {
      out = new ObjectOutputStream(new FileOutputStream(schemeFile));
      out.writeInt(1);  // version
      out.writeInt(schemes.length);
      for (int i=0; i<schemes.length; i++) {
        out.writeObject(schemes[i].getName());
        ((DayProgramScheme)schemes[i]).store(out);
      }
      out.close();
    }catch(IOException e) {
      util.exc.ErrorHandler.handle("Could not store settings.",e);
    }
  }

  private void storeQueueSchemes(Scheme[] schemes) {
    String home = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome();
    File schemeFile = new File(home,SCHEME_FILE_QUEUE);
    ObjectOutputStream out=null;
    try {
      out = new ObjectOutputStream(new FileOutputStream(schemeFile));
      out.writeInt(1);  // version
      out.writeInt(schemes.length);
      for (int i=0; i<schemes.length; i++) {
        out.writeObject(schemes[i].getName());
        ((QueueScheme)schemes[i]).store(out);
      }
      out.close();
    }catch(IOException e) {
      util.exc.ErrorHandler.handle("Could not store settings.",e);
    }
  }

  public void receivePrograms(Program[] programArr) {
    PluginTreeNode rootNode = getRootNode();
    for (int i=0; i<programArr.length; i++) {
      if (!rootNode.contains(programArr[i])) {
        rootNode.addProgram(programArr[i]);
        programArr[i].mark(this);
      }
    }
    rootNode.update();
  }


  public boolean canReceivePrograms() {
    return true;
  }

  public boolean canUseProgramTree() {
    return true;
  }


  private DayProgramScheme[] readDayProgramSchemesFromStream(ObjectInputStream in) throws IOException, ClassNotFoundException {
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
    File schemeFile = new File(home,SCHEME_FILE_DAYPROGRAM);
    ObjectInputStream in=null;
    try {
      in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(schemeFile), 0x4000));
      DayProgramScheme[] schemes = readDayProgramSchemesFromStream(in);
      in.close();
      return schemes;
    }catch(Exception e) {
      if (in != null) {
        try { in.close(); } catch(IOException exc) {}
      }
      DayProgramScheme scheme = new DayProgramScheme(mLocalizer.msg("defaultScheme","DefaultScheme"));
      scheme.setSettings(new DayProgramPrinterSettings(
              new Date(),
              3,
              null,
              6,
              24+3,
              5,
              2,
              PrinterProgramIconSettings.create(
                  new ProgramFieldType[]{
                    ProgramFieldType.EPISODE_TYPE,
                    ProgramFieldType.ORIGIN_TYPE,
                    ProgramFieldType.PRODUCTION_YEAR_TYPE,
                    ProgramFieldType.SHORT_DESCRIPTION_TYPE
                  }, false), getPluginManager().getCurrentFilter()));
      return new DayProgramScheme[]{scheme};
    }
  }


  private QueueScheme[] readQueueSchemesFromStream(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt();  // read version
    int cnt = in.readInt();
    QueueScheme[] schemes = new QueueScheme[cnt];
    for (int i=0; i<cnt; i++) {
      String name = (String)in.readObject();
      schemes[i] = new QueueScheme(name);
      schemes[i].read(in);
    }
    return schemes;
  }


  private QueueScheme[] loadQueueSchemes() {
    String home = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome();
    File schemeFile = new File(home,SCHEME_FILE_QUEUE);
    ObjectInputStream in=null;
    try {
      in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(schemeFile), 0x4000));
      QueueScheme[] schemes = readQueueSchemesFromStream(in);
      in.close();
      return schemes;
    }catch(Exception e) {
      if (in != null) {
        try { in.close(); } catch(IOException exc) {}
      }
      QueueScheme scheme = new QueueScheme(mLocalizer.msg("defaultScheme","DefaultScheme"));
      scheme.setSettings(new QueuePrinterSettings(
              true,
              1,
              PrinterProgramIconSettings.create(
                  new ProgramFieldType[]{
                    ProgramFieldType.EPISODE_TYPE,
                    ProgramFieldType.ORIGIN_TYPE,
                    ProgramFieldType.PRODUCTION_YEAR_TYPE,
                    ProgramFieldType.SHORT_DESCRIPTION_TYPE
                  }, false),
              new Font("Dialog", Font.ITALIC, 12)
      ));

      return new QueueScheme[]{scheme};
    }
  }


  public ImageIcon createIcon(String fileName) {
    return super.createImageIcon(fileName);
  }

  public void loadSettings(Properties settings) {
    mSettings = settings;
  }

  public Properties storeSettings() {
    return mSettings;
  }
  
  public void readData(ObjectInputStream in) throws IOException,
  ClassNotFoundException {
    ProgramInfoPrintSettings.getInstance().readData(in);
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    storeRootNode();
    ProgramInfoPrintSettings.getInstance().writeData(out);
  }

  public Properties getSettings() {
    return mSettings;
  }
}
