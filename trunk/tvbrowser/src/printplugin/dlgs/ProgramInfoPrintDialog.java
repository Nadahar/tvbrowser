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

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import printplugin.PrintPlugin;
import printplugin.printer.singleprogramprinter.DocumentRenderer;
import printplugin.settings.ProgramInfoPrintSettings;
import util.program.ProgramTextCreator;
import util.settings.ProgramPanelSettings;
import util.ui.FontChooserPanel;
import util.ui.Localizer;
import util.ui.OrderChooser;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.ExtendedHTMLEditorKit;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * A class that creates a dialog for setting up the
 * printing of the Program infos.
 *  
 * @author René Mach
 *
 */
public class ProgramInfoPrintDialog implements WindowClosingIf{

  private static Localizer mLocalizer = Localizer.getLocalizerFor(ProgramInfoPrintDialog.class);
  
  private JDialog mDialog;
  private PageFormat mPageFormat;

  protected Object[] mFieldTypes;
  
  /**
   * Creates a instance of this class
   * 
   * @param parent The parent frame.
   * @param program Program to Print
   */
  public ProgramInfoPrintDialog(Window parent, Program program) {
    mDialog = new JDialog(parent);
    mDialog.setModal(true);
    createGUI(parent, program);
  }

  private void createGUI(final Window parent, final Program program) {
    UiUtilities.registerForClosing(this);
    mDialog.setTitle(mLocalizer.msg("title","Print program info"));

    final OrderChooser fieldChooser = new OrderChooser(ProgramInfoPrintSettings.getInstance().getFieldTypes(),ProgramTextCreator.getDefaultOrder(),true);
    final FontChooserPanel fontChooser = new FontChooserPanel("",ProgramInfoPrintSettings.getInstance().getFont(), false);    
    final JCheckBox printImage = new JCheckBox(mLocalizer.msg("printImage","Print image"), ProgramInfoPrintSettings.getInstance().isPrintImage());
    final JCheckBox printPluginIcons = new JCheckBox(mLocalizer.msg("printPluginIcons","Print plugin icons"), ProgramInfoPrintSettings.getInstance().isPrintPluginIcons());
    final PrinterJob printerJob = PrinterJob.getPrinterJob();
    mPageFormat = printerJob.defaultPage();
    
    JButton printerSetupBtn = new JButton(SettingsDialog.mLocalizer.msg("printer","Drucker")+"...",PrintPlugin.getInstance().createImageIcon("devices", "printer", 16));
    printerSetupBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        new Thread("Printer setup") {
          public void run() {
            printerJob.printDialog();
          }
        }.start();
      }
    });
    
    JButton pageBtn = new JButton(SettingsDialog.mLocalizer.msg("page","Seite")+"...", PrintPlugin.getInstance().createImageIcon("actions", "document-properties", 16));
    pageBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        new Thread("Document setup (printing)"){
          public void run(){
            if (mPageFormat == null) {
              mPageFormat = printerJob.defaultPage();
            }
            mPageFormat = printerJob.pageDialog(mPageFormat);
          }
        }.start();
      }
    });
    
    JButton previewBtn = new JButton(SettingsDialog.mLocalizer.msg("preview","Vorschau")+"...", PrintPlugin.getInstance().createImageIcon("actions", "document-print-preview", 16));
    previewBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (mPageFormat == null) {
          mPageFormat = printerJob.defaultPage();
        }
        
        mFieldTypes = fieldChooser.getOrder();
        
        DocumentRenderer printJob = createPrintjob(program, fontChooser, printImage, printPluginIcons);
        
        PreviewDlg dlg = new PreviewDlg(mDialog, printJob, mPageFormat, printJob.getPageCount());
        
        PrintPlugin.getInstance().layoutWindow("previewDlg",dlg);
        
        dlg.setVisible(true);
      }
    });
    
    JButton print = new JButton(SettingsDialog.mLocalizer.msg("print","Drucken"));
    print.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (mPageFormat == null) {
          mPageFormat = printerJob.defaultPage();
        }
        
        mFieldTypes = fieldChooser.getOrder();
        
        ProgramInfoPrintSettings.getInstance().setFont(fontChooser.getChosenFont());
        ProgramInfoPrintSettings.getInstance().setFieldTypes(mFieldTypes);
        ProgramInfoPrintSettings.getInstance().setPrintImage(printImage.isSelected());
        ProgramInfoPrintSettings.getInstance().setPrintPluginIcons(printPluginIcons.isSelected());
        close();
        
        DocumentRenderer printable = createPrintjob(program, fontChooser, printImage, printPluginIcons);

        printerJob.setPrintable(printable, mPageFormat);
        try {
          printerJob.print();
        } catch (PrinterException pe) {
          util.exc.ErrorHandler.handle("Could not print pages: "+pe.getLocalizedMessage(), pe);
        }
        
      }
    });
    
    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });
    
    boolean hasImage = program.hasFieldValue(ProgramFieldType.PICTURE_TYPE);
    boolean hasIcons = program.getMarkerArr().length > 0;
    
    if(!hasIcons) {
      PluginAccess[] plugins = Plugin.getPluginManager().getActivatedPlugins();
        
      for (PluginAccess pluginAccess : plugins) {
        Icon[] ico = pluginAccess.getProgramTableIcons(program);

        if (ico != null && ico.length > 0) {
          hasIcons = true;
          break;
        }
      }
    }
    
    PanelBuilder b1 = new PanelBuilder(new FormLayout("5dlu,pref:grow","pref,5dlu,pref,10dlu,pref,5dlu,fill:default:grow" + (hasIcons || hasImage ? ",3dlu" : "") + (hasIcons ? ",pref" : "") + (hasImage ? ",pref" : "")));
    b1.setDefaultDialogBorder();

    PanelBuilder b2 = new PanelBuilder(new FormLayout("pref,10dlu,pref","pref,2dlu,pref,2dlu,pref,default:grow,pref,5dlu,pref"));
    b2.setDefaultDialogBorder();

    CellConstraints cc = new CellConstraints();
    
    b1.addSeparator(mLocalizer.msg("font","Font"),cc.xyw(1,1,2));
    b1.add(fontChooser, cc.xyw(1,3,2));
    b1.addSeparator(mLocalizer.msg("order","Info fields and order"), cc.xyw(1,5,2));
    b1.add(fieldChooser, cc.xyw(1,7,2));
    
    if(hasIcons) {
      b1.add(printPluginIcons, cc.xy(2,9));
    }
    
    if(hasImage) {
      if(hasIcons) {
        b1.add(printImage, cc.xy(2,10));
      } else {
        b1.add(printImage, cc.xy(2,9));
      }
    }
    
    b2.add(printerSetupBtn, cc.xy(3,1));
    b2.add(pageBtn, cc.xy(3,3));
    b2.add(previewBtn, cc.xy(3,5));
    b2.add(print,cc.xy(3,7));
    b2.add(cancel,cc.xy(3,9));
    b2.add(new JSeparator(SwingConstants.VERTICAL), cc.xywh(1,1,1,9));
    
    JPanel main = new JPanel(new BorderLayout());
    main.add(b1.getPanel(), BorderLayout.CENTER);
    main.add(b2.getPanel(), BorderLayout.EAST);
    
    mDialog.getRootPane().setDefaultButton(print);
    mDialog.setContentPane(main);
    
    PrintPlugin.getInstance().layoutWindow("programInfoPrintDialog", mDialog);
    
    mDialog.setVisible(true);
  }

  /*
   * (non-Javadoc)
   * @see util.ui.WindowClosingIf#close()
   */
  public void close() {
    mDialog.dispose();
  }

  /*
   * (non-Javadoc)
   * @see util.ui.WindowClosingIf#getRootPane()
   */
  public JRootPane getRootPane() {
    return mDialog.getRootPane();
  }

  /**
   * Create the PrintJob
   * 
   * @param program Program to Print
   * @param fontChooser Font to use
   * @param printImage Print Image, if available ?
   * @return PrintJob
   */
  private DocumentRenderer createPrintjob(final Program program, final FontChooserPanel fontChooser, final JCheckBox printImage, final JCheckBox printIcons) {
    JEditorPane pane = new JEditorPane();
    pane.setEditorKit(new ExtendedHTMLEditorKit());
    ExtendedHTMLDocument doc = (ExtendedHTMLDocument) pane.getDocument();
    
    String html = /*ProgramTextCreator.createInfoText(program, doc, 
          mFieldTypes, null, fontChooser.getChosenFont(), printImage.isSelected(), false);*/ 
    
    ProgramTextCreator.createInfoText(program, doc, mFieldTypes, null, fontChooser.getChosenFont(), new ProgramPanelSettings(printImage.isSelected() ? ProgramPanelSettings.SHOW_PICTURES_EVER : ProgramPanelSettings.SHOW_PICTURES_NEVER, -1, -1, false, true, 10), false, 100, printIcons.isSelected());
    
    pane.setText(html);
    
    DocumentRenderer printJob = new DocumentRenderer(mPageFormat);
    printJob.setEditorPane(pane);
    return printJob;
  }
}
