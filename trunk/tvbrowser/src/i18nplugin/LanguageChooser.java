/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package i18nplugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A small Dialog that enables the User to select a Language
 *
 * @author bodum
 */
public class LanguageChooser extends JDialog implements WindowClosingIf {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(LanguageChooser.class);

  private JComboBox mLangBox;

  private Locale mSelectedLocale = null;

  /**
   * Create the Dialog
   *
   * @param parent Parent-Dialog
   * @param currentLanguages Current Languages
   */
  public LanguageChooser(JDialog parent, Vector<Locale> currentLanguages) {
    super(parent, true);

    Locale[] loc = Locale.getAvailableLocales();

    Arrays.sort(loc, new Comparator<Locale>() {
      public int compare(Locale o1, Locale o2) {
        return o1.getDisplayName().compareTo(o2.getDisplayName());
      }
    });

    Vector<Locale> newLocales = new Vector<Locale>();

    for (Locale locale : loc) {
      if ((!locale.getLanguage().equals("en")) && !currentLanguages.contains(locale)) {
				newLocales.add(locale);
			}
    }

    setTitle(mLocalizer.msg("addLanguage", "Add Language"));
    JPanel panel = (JPanel) getContentPane();

    panel.setLayout(new FormLayout("fill:pref:grow, center:pref, fill:pref:grow", "pref, 3dlu, pref, fill:3dlu:grow, pref"));
    panel.setBorder(Borders.DLU4_BORDER);
    CellConstraints cc = new CellConstraints();

    panel.add(new JLabel(mLocalizer.msg("chooseLanguage","Please choose language:")), cc.xyw(1,1,3));

    mLangBox = new JComboBox(newLocales);
    mLangBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent(list, ((Locale)value).getDisplayName(), index, isSelected, cellHasFocus);
      }
    });

    panel.add(mLangBox, cc.xy(2,3));

    ButtonBarBuilder2 builder = new ButtonBarBuilder2();

    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mSelectedLocale = (Locale) mLangBox.getSelectedItem();
        close();
      }
    });

    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    builder.addGlue();
    builder.addButton(new JButton[] {ok, cancel});

    panel.add(builder.getPanel(), cc.xyw(1,5,3));

    getRootPane().setDefaultButton(ok);

    pack();

    UiUtilities.registerForClosing(this);
  }

  /**
   * @return selected Locale of Dialog, null if cancel was pressed
   */
  public Locale getSelectedLocale() {
    return mSelectedLocale;
  }

  public void close() {
    setVisible(false);
  }

}
