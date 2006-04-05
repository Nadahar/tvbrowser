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
import java.awt.print.PrinterJob;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;

import printplugin.PrintPlugin;
import printplugin.settings.ProgramInfoPrintSettings;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import util.ui.FontChooserPanel;
import util.ui.Localizer;
import util.ui.OrderChooser;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

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
  
  /**
   * Creates a instance of this class
   * 
   * @param parent The parent frame.
   */
  public ProgramInfoPrintDialog(JFrame parent) {
    mDialog = new JDialog(parent ,true);  
    createGUI(parent);
  }

  /**
   * Creates a instance of this class
   * 
   * @param parent The parent dialog.
   */
  public ProgramInfoPrintDialog(JDialog parent) {
    mDialog = new JDialog(parent ,true);
    createGUI(parent);
  }

  private void createGUI(Window parent) {
    UiUtilities.registerForClosing(this);
    mDialog.setTitle(mLocalizer.msg("title","Print program info"));

    final OrderChooser fieldChooser = new OrderChooser(ProgramInfoPrintSettings.getInstance().getFieldTypes(),ProgramInfoPrintSettings.getInstance().getAllFieldTypes(),true);
    final FontChooserPanel fontChooser = new FontChooserPanel("",ProgramInfoPrintSettings.getInstance().getFont());
    final PrinterJob printerJob = PrinterJob.getPrinterJob();
    mPageFormat = printerJob.defaultPage();
    
    JButton printerSetupBtn = new JButton(SettingsDialog.mLocalizer.msg("printer","Drucker")+"...",PrintPlugin.getInstance().createImageIcon("devices", "printer", 16));
    printerSetupBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        new Thread() {
          public void run() {
            printerJob.printDialog();
          }
        }.start();
      }
    });
    
    JButton pageBtn = new JButton(SettingsDialog.mLocalizer.msg("page","Seite")+"...", PrintPlugin.getInstance().createImageIcon("actions", "document-properties", 16));
    pageBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        new Thread(){
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
        // TODO Add print preview.
        
      }
    });
    
    JButton print = new JButton(SettingsDialog.mLocalizer.msg("print","Drucken"));
    print.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ProgramInfoPrintSettings.getInstance().setFont(fontChooser.getChosenFont());
        ProgramInfoPrintSettings.getInstance().setFieldTypes(fieldChooser.getOrder());
        close();
        //TODO Add print action.
        
      }
    });
    
    JButton cancel = new JButton(SettingsDialog.mLocalizer.msg("cancel","Abbrechen"));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });
    
    PanelBuilder b1 = new PanelBuilder(new FormLayout("pref:grow","pref,5dlu,pref,10dlu,pref,5dlu,fill:default:grow"));
    b1.setDefaultDialogBorder();

    PanelBuilder b2 = new PanelBuilder(new FormLayout("pref,10dlu,pref","pref,2dlu,pref,2dlu,pref,default:grow,pref,2dlu,pref"));
    b2.setDefaultDialogBorder();

    CellConstraints cc = new CellConstraints();
    
    b1.addSeparator(mLocalizer.msg("font","Font"),cc.xy(1,1));
    b1.add(fieldChooser, cc.xy(1,7));
    b1.addSeparator(mLocalizer.msg("order","Info fields and order"), cc.xy(1,5));
    b1.add(fontChooser, cc.xy(1,3));
    
    b2.add(printerSetupBtn, cc.xy(3,1));
    b2.add(pageBtn, cc.xy(3,3));
    b2.add(previewBtn, cc.xy(3,5));
    b2.add(print,cc.xy(3,7));
    b2.add(cancel,cc.xy(3,9));
    b2.add(new JSeparator(JSeparator.VERTICAL), cc.xywh(1,1,1,9));
    
    JPanel main = new JPanel(new BorderLayout());
    main.add(b1.getPanel(), BorderLayout.CENTER);
    main.add(b2.getPanel(), BorderLayout.EAST);
    
    mDialog.getRootPane().setDefaultButton(print);
    mDialog.setContentPane(main);
    mDialog.pack();    
    mDialog.setLocationRelativeTo(parent);
    mDialog.setVisible(true);
  }

  public void close() {
    mDialog.dispose();
  }

  public JRootPane getRootPane() {
    return mDialog.getRootPane();
  }
}
