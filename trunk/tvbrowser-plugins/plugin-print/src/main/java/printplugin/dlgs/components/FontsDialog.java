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
 *     $Date: 2009-04-25 09:58:28 +0200 (Sa, 25 Apr 2009) $
 *   $Author: Bananeweizen $
 * $Revision: 5670 $
 */

package printplugin.dlgs.components;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import util.ui.FontChooserPanel;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class FontsDialog extends JDialog implements WindowClosingIf {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(FontsDialog.class);


  private static final int CANCEL = 0;
  protected static final int OK = 1;

  private FontChooserPanel mTitleFontPanel;
  private FontChooserPanel mDescriptionFontPanel;
  private FontChooserPanel mDateFontPanel;
  private int mResult;

  public FontsDialog(Frame parent, Font titleFont, Font descriptionFont, Font dateFont) {
    super(parent, true);
    setTitle(mLocalizer.msg("dialog.title","Fonts"));
    JPanel content = (JPanel)getContentPane();
    
    UiUtilities.registerForClosing(this);

    CellConstraints cc = new CellConstraints();
    FormLayout layout = new FormLayout("5dlu,pref:grow",
        "pref,5dlu,pref,2dlu,pref,5dlu,pref");
    PanelBuilder pb = new PanelBuilder(layout, content);
    pb.setBorder(Borders.DLU4_BORDER);
    
    pb.addSeparator(mLocalizer.msg("fonts","Fonts"), cc.xyw(1,1,2));
    
    mTitleFontPanel=new FontChooserPanel(mLocalizer.msg("title","Title"), titleFont);
    mDescriptionFontPanel=new FontChooserPanel(mLocalizer.msg("description","Description"), descriptionFont);
    if (dateFont != null) {
      mDateFontPanel = new FontChooserPanel(mLocalizer.msg("date","Date"), dateFont);
    }
    
    int y = 3;
    
    pb.add(mTitleFontPanel, cc.xy(2,y++));
    pb.add(mDescriptionFontPanel, cc.xy(2,++y));
    
    if(dateFont != null) {
      layout.insertRow(++y, RowSpec.decode("2dlu"));
      layout.insertRow(++y, RowSpec.decode("pref"));
      
      content.add(mDateFontPanel, cc.xy(2,y));
    }
    y++;
    
    JPanel btnPn = new JPanel(new FlowLayout());

    JButton okBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    JButton cancelBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));

    btnPn.add(okBt);
    btnPn.add(cancelBt);

    okBt.addActionListener(new ActionListener(){
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
    
    content.add(btnPn, cc.xyw(1,++y,2));

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
    if (mDateFontPanel != null) {
      return mDateFontPanel.getChosenFont();
    }
    return null;
  }

  public void close() {
    mResult = CANCEL;
    setVisible(false);
  }

}

