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

import printplugin.printer.PrintJob;
import printplugin.settings.Scheme;
import util.ui.ImageUtilities;
import util.ui.UiUtilities;


public class SettingsDialog extends JDialog {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(SettingsDialog.class);

  public static final int OK = 1;
  public static final int CANCEL = 0;


  private PageFormat mPageFormat;

  private int mResult;


  private DefaultComboBoxModel mSchemeCBModel;
  private JComboBox mSchemeCB;
  private JButton mDeleteSchemeBtn, mSaveSchemeBtn, mEditSchemeBtn;

  private DialogContent mDialogContent;

  public SettingsDialog(final Frame parent, final PrinterJob printerJob, Scheme[] schemes, DialogContent content) {
    super(parent, true);

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
        printplugin.printer.PrintJob job = getPrintJob();
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
        String newSchemeName = JOptionPane.showInputDialog(parent, mLocalizer.msg("enterNewSchemeName","Enter new name for scheme:"), mLocalizer.msg("newScheme","New scheme"), JOptionPane.PLAIN_MESSAGE);
        if (newSchemeName != null) {
          if (newSchemeName.toString().trim().length()>0) {
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
            mSchemeCB.updateUI();
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

    setSize(450,400);

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

}
