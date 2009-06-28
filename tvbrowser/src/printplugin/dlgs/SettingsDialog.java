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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import printplugin.PrintPlugin;
import printplugin.printer.PrintJob;
import printplugin.settings.Scheme;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;


public class SettingsDialog extends JDialog implements WindowClosingIf {

  /** The localizer for this class. */
  protected static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(SettingsDialog.class);

  public static final int OK = 1;
  private static final int CANCEL = 0;


  private PageFormat mPageFormat;

  private int mResult;


  private DefaultComboBoxModel mSchemeCBModel;
  private JComboBox mSchemeCB;
  private JButton mDeleteSchemeBtn, mSaveSchemeBtn, mEditSchemeBtn;

  private DialogContent mDialogContent;

  public SettingsDialog(final Frame parent, final PrinterJob printerJob, Scheme[] schemes, DialogContent content) {
    super(parent,true);
    UiUtilities.registerForClosing(this);
    
    mDialogContent = content;
    mPageFormat = printerJob.defaultPage();
    setTitle(content.getDialogTitle());
    JPanel contentPane = (JPanel)getContentPane();
    contentPane.setBorder(BorderFactory.createEmptyBorder(4,4,5,5));
    contentPane.setLayout(new BorderLayout());

    JPanel eastPanel = new JPanel(new BorderLayout());
    JPanel eastBtnPanel = new JPanel();
    eastBtnPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
    eastBtnPanel.setLayout(new GridLayout(-1, 1));

    JButton printerSetupBtn = new JButton(mLocalizer.ellipsisMsg("printer","Printer"),PrintPlugin.getInstance().createImageIcon("devices", "printer", 16));
    JButton pageBtn = new JButton(mLocalizer.ellipsisMsg("page","Page"), PrintPlugin.getInstance().createImageIcon("actions", "document-properties", 16));
    JButton previewBtn = new JButton(mLocalizer.ellipsisMsg("preview","Preview"), PrintPlugin.getInstance().createImageIcon("actions", "document-print-preview", 16));

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
    JButton cancelBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    okCancelBtnPanel.add(printBt);
    okCancelBtnPanel.add(cancelBt);
    southPanel.add(okCancelBtnPanel, BorderLayout.EAST);

    printerSetupBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        Thread thread = new Thread("Printer setup"){
          public void run(){
            printerJob.printDialog();
          }
        };
        thread.start();
      }
    });

    pageBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Thread thread = new Thread("Document properties (printing)"){
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
        printplugin.printer.PrintJob job = getPrintJob();
        if (job.getNumOfPages()==0) {
          JOptionPane.showMessageDialog(parent, mLocalizer.msg("noPagesToPrint","Es sind keine Seiten zu drucken."));
        }
        else {
          PreviewDlg dlg = new PreviewDlg(parent, job.getPrintable(), mPageFormat, job.getNumOfPages());
          
          PrintPlugin.getInstance().layoutWindow("previewDlg",dlg);
          
          dlg.setVisible(true);
        }
      }
    });


    printBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mResult = OK;
        setVisible(false);
      }
    });

    cancelBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });



    JButton newSchemeBtn = new JButton(TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
    newSchemeBtn.setToolTipText(mLocalizer.msg("newScheme", "New scheme"));
    mEditSchemeBtn = new JButton(TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
    mEditSchemeBtn.setToolTipText(mLocalizer.msg("editScheme", "Edit scheme"));
    mDeleteSchemeBtn = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mDeleteSchemeBtn.setToolTipText(mLocalizer.msg("deleteScheme", "Delete scheme"));
    mSaveSchemeBtn = new JButton(PrintPlugin.getInstance().createImageIcon("actions", "document-save", 16));
    mSaveSchemeBtn.setToolTipText(mLocalizer.msg("saveScheme", "Save scheme"));
    newSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    mDeleteSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    mEditSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    mSaveSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    mDeleteSchemeBtn.setEnabled(false);
    mEditSchemeBtn.setEnabled(false);


    newSchemeBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String newSchemeName = JOptionPane.showInputDialog(parent, mLocalizer.msg("enterNewSchemeName","Enter new name for scheme:"), mLocalizer.msg("newScheme","New scheme"), JOptionPane.PLAIN_MESSAGE);
        if (newSchemeName != null) {
          if (newSchemeName.trim().length() > 0) {
            Scheme newScheme = mDialogContent.createNewScheme(newSchemeName);
            newScheme.setSettings(mDialogContent.getSettings());
            mSchemeCBModel.addElement(newScheme);
            mSchemeCB.setSelectedItem(newScheme);
            mDialogContent.setSettings(newScheme.getSettings());
          }
          else {
            JOptionPane.showMessageDialog(parent, mLocalizer.msg("invalidSchemeMsg","Invalid scheme name"), mLocalizer.msg("invalidInput","Invalid input"), JOptionPane.INFORMATION_MESSAGE);
          }
        }
      }
    });

    mSaveSchemeBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        Scheme scheme = (Scheme)mSchemeCB.getSelectedItem();
        scheme.setSettings(mDialogContent.getSettings());

      }
    });

    mEditSchemeBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        Scheme scheme = (Scheme)mSchemeCB.getSelectedItem();
        Object newSchemeName = JOptionPane.showInputDialog(parent, mLocalizer.msg("enterNewSchemeName","Enter new name for scheme:"), mLocalizer.msg("editScheme","Edit scheme"), JOptionPane.PLAIN_MESSAGE, null, null, scheme.getName());
        if (newSchemeName != null) {
          if (newSchemeName.toString().trim().length()>0) {
            scheme.setName(newSchemeName.toString());
            mSchemeCB.repaint();
          }
          else {
            JOptionPane.showMessageDialog(parent, mLocalizer.msg("invalidSchemeMsg","Invalid scheme name"), mLocalizer.msg("invalidInput","Invalid input"), JOptionPane.INFORMATION_MESSAGE);
          }
        }
      }
    });

    mDeleteSchemeBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        Scheme scheme = (Scheme)mSchemeCB.getSelectedItem();
        if (scheme != null) {
          if (JOptionPane.showConfirmDialog(parent, mLocalizer.msg("deleteSchemeMsg","Do you want to delete the selected scheme?"), mLocalizer.msg("deleteScheme","Delete Scheme"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            mSchemeCBModel.removeElement(scheme);
            mSchemeCB.setSelectedIndex(0);
          }
        }
      }
    });

    mSchemeCBModel = new DefaultComboBoxModel(schemes);
    mSchemeCB = new JComboBox(mSchemeCBModel);
    JPanel schemePanel = new JPanel();
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
        Scheme scheme = (Scheme)mSchemeCB.getSelectedItem();
        mDialogContent.setSettings(scheme.getSettings());
      }
    });


    contentPane.add(eastPanel, BorderLayout.EAST);
    contentPane.add(southPanel, BorderLayout.SOUTH);
    contentPane.add(content.getContent(), BorderLayout.CENTER);

    content.setSettings(schemes[0].getSettings());

    if (mPageFormat == null) {
      mPageFormat = printerJob.defaultPage();
    }

    PrintPlugin.getInstance().layoutWindow("settingsDlg",this,new Dimension(450, 400));

    mResult = CANCEL;
  }



  public Scheme[] getSchemes() {
    Scheme[] result = new Scheme[mSchemeCBModel.getSize()];
    for (int i=0; i<result.length; i++) {
      result[i] = (Scheme)mSchemeCBModel.getElementAt(i);
    }
    return result;
  }

  public int getResult() {
    return mResult;
  }

  public PrintJob getPrintJob() {
    return mDialogContent.createPrintJob(mPageFormat);
  }

  public void printingDone() {
    mDialogContent.printingDone();
  }
  
  public void close() {
    mResult = CANCEL;
    setVisible(false);
  }

}
