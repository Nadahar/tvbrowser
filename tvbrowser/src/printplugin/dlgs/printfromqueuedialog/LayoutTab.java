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

package printplugin.dlgs.printfromqueuedialog;

import util.ui.FontChooserPanel;

import javax.swing.*;
import java.awt.*;

public class LayoutTab extends JPanel {
  private Font mTitleFont, mDescriptionFont;
  private FontChooserPanel mTitleFontPanel, mDescriptionFontPanel;
  private JComboBox mColumnsPerPageCB;

  public LayoutTab() {

    super();
    setLayout(new BorderLayout());

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    JPanel columnPanel = new JPanel(new BorderLayout());
    columnPanel.setBorder(BorderFactory.createTitledBorder("Columns"));
    JPanel pn1 = new JPanel();
    pn1.setLayout(new BoxLayout(pn1, BoxLayout.X_AXIS));

    JLabel columnLabel = new JLabel("Columns per page:");
    mColumnsPerPageCB = new JComboBox(createIntegerArray(1,4));

    pn1.add(columnLabel);
    pn1.add(mColumnsPerPageCB);
    columnPanel.add(pn1, BorderLayout.WEST);

    JPanel fontPanel=new JPanel(new GridLayout(2,1));
    fontPanel.setBorder(BorderFactory.createTitledBorder("Fonts"));

    mTitleFontPanel=new FontChooserPanel("Title", getTitleFont());
    mDescriptionFontPanel=new FontChooserPanel("Description", getDescriptionFont());

    fontPanel.add(mTitleFontPanel);
    fontPanel.add(mDescriptionFontPanel);

    content.add(columnPanel);
    content.add(fontPanel);

    add(content, BorderLayout.NORTH);
  }

  public Font getTitleFont() {
    return mTitleFont;
  }

  public void setTitleFont(Font newFont) {
    mTitleFont = newFont;
  }

  public Font getDescriptionFont() {
    return mDescriptionFont;
  }

  public void setDescriptionFont(Font newFont) {
    mDescriptionFont = newFont;
  }

  private Integer[] createIntegerArray(int from, int cnt) {
    Integer[] result = new Integer[cnt];
    for (int i=0; i<result.length; i++) {
      result[i] = new Integer(i+from);
    }
    return result;
  }
}