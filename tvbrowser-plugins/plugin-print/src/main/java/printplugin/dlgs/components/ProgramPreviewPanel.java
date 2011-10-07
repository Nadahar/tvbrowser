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
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mrz 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */

package printplugin.dlgs.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import printplugin.printer.ProgramIcon;
import printplugin.settings.MutableProgramIconSettings;
import printplugin.settings.ProgramIconSettings;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.ProgramFieldType;


public class ProgramPreviewPanel extends JPanel {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(ProgramPreviewPanel.class);


  private MutableProgramIconSettings mProgramIconSettings;
  private JLabel mProgramIconLabel;
  private Font mDateFont;
  private JLabel mDateLabel;
  private JPanel mIconPanel;

  public ProgramPreviewPanel(final Frame dlgParent, ProgramIconSettings programIconSettings, Font dateFont) {
    super();
    mDateFont = dateFont;
    if (programIconSettings != null) {
      setProgramIconSettings(programIconSettings);
    }
    setLayout(new BorderLayout(3,3));

    JButton fontsButton = new JButton(mLocalizer.msg("fonts", "Fonts.."));
    JButton fieldsButton = new JButton(mLocalizer.msg("fields", "Fields.."));

    mDateLabel = new JLabel(new Date().getLongDateString());
    mProgramIconLabel = new JLabel(createDemoProgramPanel());

    mIconPanel = new JPanel(new BorderLayout());
    if (mDateFont != null) {
      mIconPanel.add(mDateLabel, BorderLayout.NORTH);
    }
    mIconPanel.add(mProgramIconLabel, BorderLayout.CENTER);

    JPanel eastPn = new JPanel(new BorderLayout());
    JPanel buttonPn = new JPanel(new GridLayout(-1, 1,3,3));
    eastPn.add(buttonPn, BorderLayout.NORTH);
    buttonPn.add(fontsButton);
    buttonPn.add(fieldsButton);

    JScrollPane scrollPane = new JScrollPane(mIconPanel);
    scrollPane.setPreferredSize(new Dimension(0,90));
    add(scrollPane, BorderLayout.CENTER);
    add(eastPn, BorderLayout.EAST);


    fontsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (mProgramIconSettings == null) {
          return;
        }
        FontsDialog dlg = new FontsDialog(dlgParent, mProgramIconSettings.getTitleFont(), mProgramIconSettings.getTextFont(), mDateFont);
        util.ui.UiUtilities.centerAndShow(dlg);
        if (dlg.getResult() == FontsDialog.OK) {
          Font titleFont = dlg.getTitleFont();
          Font descFont = dlg.getDescriptionFont();
          mDateFont = dlg.getDateFont();
          mProgramIconSettings.setTextFont(descFont);
          mProgramIconSettings.setTimeFont(titleFont);
          mProgramIconSettings.setTitleFont(titleFont);
          updatePreviewPanel();
        }
      }
    });

    fieldsButton.addActionListener(new ActionListener(){
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
    this(dlgParent, null, null);
  }

  public void updatePreviewPanel() {
    mProgramIconLabel.setIcon(createDemoProgramPanel());
    mDateLabel.setFont(mDateFont);
    if (mDateFont != null) {
      mIconPanel.add(mDateLabel, BorderLayout.NORTH);
    }
  }

  public void setProgramIconSettings(ProgramIconSettings settings) {
    mProgramIconSettings = new MutableProgramIconSettings(settings);
    updatePreviewPanel();
  }

  public void setDateFont(Font f) {
    mDateFont = f;
    updatePreviewPanel();
  }

  public void setShowPluginMarking(boolean show) {
    mProgramIconSettings.setPaintPluginMarks(show);
  }

  public boolean getShowPluginMarking() {
    return mProgramIconSettings.getPaintPluginMarks();
  }

  public ProgramIconSettings getProgramIconSettings() {
    return mProgramIconSettings;
  }

  public Font getDateFont() {
    return mDateFont;
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


