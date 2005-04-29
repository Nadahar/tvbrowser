package printplugin.dlgs.printfromqueuedialog;

import printplugin.dlgs.DialogContent;
import printplugin.settings.Settings;
import printplugin.printer.*;
import printplugin.printer.PrintJob;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 29.04.2005
 * Time: 21:30:22
 */
public class PrintFromQueueDialog implements DialogContent {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(PrintFromQueueDialog.class);

  private GeneralTab mGeneralTab;
  private LayoutTab mLayoutTab;

  public PrintFromQueueDialog() {


  }

  public Component getContent() {
     JTabbedPane tab = new JTabbedPane();

    mGeneralTab = new GeneralTab();
    mLayoutTab = new LayoutTab();
    tab.add(mLocalizer.msg("listingsTab", "Daten"), mGeneralTab);
    tab.add(mLocalizer.msg("layoutTab","Layout"), mLayoutTab);
    tab.add(mLocalizer.msg("miscTab","Extras"), new JPanel());
    return tab;

  }

  public String getDialogTitle() {
    return mLocalizer.msg("dialogTitle","Print from queue");
  }

  public Settings getSettings() {
    return null;
  }

  public void setSettings(Settings settings) {

  }

  public PrintJob createPrintJob(PageFormat format) {
    return null;
  }
}
