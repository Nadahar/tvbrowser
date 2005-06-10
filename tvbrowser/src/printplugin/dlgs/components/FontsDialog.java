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

import util.ui.FontChooserPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class FontsDialog extends JDialog {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(FontsDialog.class);


  public static int CANCEL = 0;
  public static int OK = 1;

  private FontChooserPanel mTitleFontPanel;
  private FontChooserPanel mDescriptionFontPanel;
  private FontChooserPanel mDateFontPanel;
  private int mResult;

  public FontsDialog(Frame parent, Font titleFont, Font descriptionFont, Font dateFont) {
    super(parent, true);
    setTitle(mLocalizer.msg("dialog.title","Fonts"));
    JPanel content = (JPanel)getContentPane();

    mTitleFontPanel=new FontChooserPanel(mLocalizer.msg("title","Title"), titleFont);
    mDescriptionFontPanel=new FontChooserPanel(mLocalizer.msg("description","Description"), descriptionFont);
    if (dateFont != null) {
      mDateFontPanel = new FontChooserPanel(mLocalizer.msg("date","Date"), dateFont);
    }
    content.setLayout(new BorderLayout());

    JPanel fontPanel=new JPanel(new GridLayout(-1,1));
    fontPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("fonts","Fonts")));

    fontPanel.add(mTitleFontPanel);
    fontPanel.add(mDescriptionFontPanel);
    if (dateFont != null) {
      fontPanel.add(mDateFontPanel);
    }

    JPanel btnPn = new JPanel(new FlowLayout());

    JButton okBt = new JButton(mLocalizer.msg("ok","OK"));
    JButton cancelBt = new JButton(mLocalizer.msg("cancel","Cancel"));

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

  public Font getDateFont() {
    return mDateFontPanel.getChosenFont();
  }

}

