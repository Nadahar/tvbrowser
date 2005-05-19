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

package printplugin.dlgs.components;

import devplugin.Plugin;
import devplugin.ProgramFieldType;

import javax.swing.*;

import printplugin.printer.ProgramIcon;
import printplugin.settings.ProgramIconSettings;
import printplugin.settings.MutableProgramIconSettings;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import util.ui.FontChooserPanel;


public class ProgramPreviewPanel extends JPanel {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(ProgramPreviewPanel.class);


  private MutableProgramIconSettings mProgramIconSettings;
  private JLabel mProgramIconLabel;
  private JButton mFieldsButton, mFontsButton;

  public ProgramPreviewPanel(final Frame dlgParent, ProgramIconSettings programIconSettings) {
    super();
    if (programIconSettings != null) {
      setProgramIconSettings(programIconSettings);
    }
    setLayout(new BorderLayout(3,3));

    mFontsButton = new JButton(mLocalizer.msg("fonts","Fonts.."));
    mFieldsButton = new JButton(mLocalizer.msg("fields","Fields.."));


    mProgramIconLabel = new JLabel(createDemoProgramPanel());

    JPanel iconPanel = new JPanel();
    iconPanel.add(mProgramIconLabel);

    JPanel eastPn = new JPanel(new BorderLayout());
    JPanel buttonPn = new JPanel(new GridLayout(-1, 1,3,3));
    eastPn.add(buttonPn, BorderLayout.NORTH);
    buttonPn.add(mFontsButton);
    buttonPn.add(mFieldsButton);

    JScrollPane scrollPane = new JScrollPane(iconPanel);
    scrollPane.setPreferredSize(new Dimension(0,90));
    add(scrollPane, BorderLayout.CENTER);
    add(eastPn, BorderLayout.EAST);


    mFontsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (mProgramIconSettings == null) {
          return;
        }
        FontsDialog dlg = new FontsDialog(dlgParent, mProgramIconSettings.getTitleFont(), mProgramIconSettings.getTextFont());
        util.ui.UiUtilities.centerAndShow(dlg);
        if (dlg.getResult() == FontsDialog.OK) {
          Font titleFont = dlg.getTitleFont();
          Font descFont = dlg.getDescriptionFont();
          mProgramIconSettings.setTextFont(descFont);
          mProgramIconSettings.setTimeFont(titleFont);
          mProgramIconSettings.setTitleFont(titleFont);
          updatePreviewPanel();
        }
      }
    });

    mFieldsButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (mProgramIconSettings == null) {
          return;
        }
        ProgramItemFieldsConfigDlg dlg = new ProgramItemFieldsConfigDlg(dlgParent, mProgramIconSettings.getProgramInfoFields());
        util.ui.UiUtilities.centerAndShow(dlg);

        if (dlg.getResult()==ProgramItemFieldsConfigDlg.OK) {
          ProgramFieldType[] fieldTypes = dlg.getProgramItemFieldTypes();
          mProgramIconSettings.setProgramInfoFields(fieldTypes);
          updatePreviewPanel();
        }
      }
    });

    updatePreviewPanel();
  }


  public ProgramPreviewPanel(Frame dlgParent) {
    this(dlgParent, null);
  }

  private void updatePreviewPanel() {
    mProgramIconLabel.setIcon(createDemoProgramPanel());
  }

  public void setProgramIconSettings(ProgramIconSettings settings) {
    mProgramIconSettings = new MutableProgramIconSettings(settings);
    updatePreviewPanel();
  }

  public ProgramIconSettings getProgramIconSettings() {
    return mProgramIconSettings;
  }

  private Icon createDemoProgramPanel() {
    devplugin.Program prog = Plugin.getPluginManager().getExampleProgram();
    if (mProgramIconSettings != null) {
      mProgramIconSettings.setTimeFieldWidth(util.ui.UiUtilities.getStringWidth(mProgramIconSettings.getTimeFont(),"55:55X"));
    }
    ProgramIcon ico = new ProgramIcon(prog, mProgramIconSettings, 200, false);
    ico.setMaximumHeight(Integer.MAX_VALUE);
    return ico;

  }

}


class FontsDialog extends JDialog {

  public static int CANCEL = 0;
  public static int OK = 1;

  private FontChooserPanel mTitleFontPanel;
  private FontChooserPanel mDescriptionFontPanel;
  private int mResult;

  public FontsDialog(Frame parent, Font titleFont, Font descriptionFont) {
    super(parent, true);
    setTitle("Fonts");
    JPanel content = (JPanel)getContentPane();

    mTitleFontPanel=new FontChooserPanel("Title", titleFont);
    mDescriptionFontPanel=new FontChooserPanel("Description", descriptionFont);

    content.setLayout(new BorderLayout());

    JPanel fontPanel=new JPanel(new GridLayout(2,1));
    fontPanel.setBorder(BorderFactory.createTitledBorder("Fonts"));

    fontPanel.add(mTitleFontPanel);
    fontPanel.add(mDescriptionFontPanel);

    JPanel btnPn = new JPanel(new FlowLayout());

    JButton okBt = new JButton("OK");
    JButton cancelBt = new JButton("Cancel");

    btnPn.add(okBt);
    btnPn.add(cancelBt);

    okBt.addActionListener(new ActionListener(){
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

    content.add(fontPanel, BorderLayout.CENTER);
    content.add(btnPn, BorderLayout.SOUTH);

    mResult = CANCEL;
    pack();
  }

  public int getResult() {
    return mResult;
  }

  public Font getTitleFont() {
    return mTitleFontPanel.getChosenFont();
  }

  public Font getDescriptionFont() {
    return mDescriptionFontPanel.getChosenFont();
  }

}
