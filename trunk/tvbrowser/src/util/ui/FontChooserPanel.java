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

package util.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class FontChooserPanel extends JPanel {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(FontChooserPanel.class);

  private JComboBox mFontCB, mStyleCB;

  private JSpinner mSizeSpinner;

  private JLabel mTitle;

  private static java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();

  private static final String[] FONTNAMES = ge.getAvailableFontFamilyNames();

  private static final String[] FONTSTYLES = { mLocalizer.msg("plain", "plain"), mLocalizer.msg("bold", "bold"),
      mLocalizer.msg("italic", "italic") };

  private static final int FONTSIZE_MIN = 8;

  private static final int FONTSIZE_MAX = 40;

  /**
   * create a new font chooser with optional style selection
   * @param title title or <code>null</code>
   * @param font selected font
   * @param style enable style selection
   */
  public FontChooserPanel(String title, Font font, boolean style) {
    setLayout(new BorderLayout());
    if (title != null) {
      mTitle = new JLabel(title);
      add(mTitle, BorderLayout.NORTH);
    }
    JPanel innerPanel = new JPanel(new FlowLayout());

    mFontCB = new JComboBox(FONTNAMES);
    mStyleCB = new JComboBox(FONTSTYLES);
    mSizeSpinner = new JSpinner(new SpinnerNumberModel(FONTSIZE_MIN, FONTSIZE_MIN, FONTSIZE_MAX, 1));

    innerPanel.add(mFontCB);

    if (style) {
      innerPanel.add(mStyleCB);
    }

    innerPanel.add(mSizeSpinner);
    
    add(innerPanel, BorderLayout.CENTER);

    if (font != null) {
      selectFont(font);
    }
  }

  /**
   * create a font chooser with style selection
   * @param title title or <code>null</code>
   * @param font
   */
  public FontChooserPanel(String title, Font font) {
    this(title, font, true);
  }

  /**
   * Creates a Font-Chooser without Title-Bar, but with style selection
   * 
   * @param font
   * @since 2.2
   */
  public FontChooserPanel(Font font) {
    this(null, font, true);
  }
  
  /**
   * create a font chooser without title bar
   * @param font
   * @param enableStyleSelection show style selection or not
   * @since 3.0
   */
  public FontChooserPanel(final Font font, boolean enableStyleSelection) {
    this(null, font, enableStyleSelection);
  }
  
  public FontChooserPanel(String title) {
    this(title, null);
  }

  public void selectFont(Font font) {
    for (int i = 0; i < mFontCB.getItemCount(); i++) {
      String item = (String) mFontCB.getItemAt(i);
      if (item.equals(font.getName())) {
        mFontCB.setSelectedIndex(i);
        break;
      }
    }

    mSizeSpinner.setValue(font.getSize());

    if (font.getStyle() == Font.BOLD) {
      mStyleCB.setSelectedIndex(1);
    } else if (font.getStyle() == Font.ITALIC) {
      mStyleCB.setSelectedIndex(2);
    }

  }

  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    mFontCB.setEnabled(enabled);
    mStyleCB.setEnabled(enabled);
    mSizeSpinner.setEnabled(enabled);
    if (mTitle != null) {
      mTitle.setEnabled(enabled);
    }
  }

  public Font getChosenFont() {
    Font result;
    int style;
    int inx = mStyleCB.getSelectedIndex();
    if (inx == 0) {
      style = Font.PLAIN;
    } else if (inx == 1) {
      style = Font.BOLD;
    } else {
      style = Font.ITALIC;
    }
    result = new Font((String) mFontCB.getSelectedItem(), style, ((Integer) mSizeSpinner.getValue()).intValue());

    return result;
  }
}
