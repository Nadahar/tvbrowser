/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package tvbrowser.ui.settings.looksSettings;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import tvbrowser.ui.BrownSugarDark;
import tvbrowser.ui.DarkStarDark;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticTheme;

/**
 * Settings for the JGoodies LnF
 * 
 * @author bodum
 */
public class JGoodiesLNFSettings extends JDialog implements WindowClosingIf {
  /** Translation */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(JGoodiesLNFSettings.class);

  /** Color-Schema */
  private JComboBox mColorScheme;
  /** Drop-Shadow */
  private JCheckBox mShadow;
  
  /**
   * Create the Dialog
   * 
   * @param parent Parent-Dialog
   */
  public JGoodiesLNFSettings(JDialog parent) {
    super(parent, true);
    setTitle(mLocalizer.msg("title", "Title"));
    createGui();
  }

  /**
   * Create the GUI
   */
  private void createGui() {
    List themesList = PlasticLookAndFeel.getInstalledThemes();
    themesList.add(2,new DarkStarDark(true));
    themesList.add(1,new BrownSugarDark(true));
    PlasticTheme[] themes = (PlasticTheme[]) themesList
        .toArray(new PlasticTheme[themesList.size()]);
    
    JPanel content = (JPanel) getContentPane();
    
    content.setLayout(new FormLayout("pref, 3dlu, fill:pref:grow", "pref, 3dlu, pref, fill:3dlu:grow, pref"));
    content.setBorder(Borders.DLU4_BORDER);
    
    CellConstraints cc = new CellConstraints();
    
    content.add(new JLabel(mLocalizer.msg("colorTheme", "Color-Theme") +  ":"), cc.xy(1,1));
    
    mColorScheme = new JComboBox(themes);
    mColorScheme.setRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setText(((PlasticTheme)value).getName());
        return label;
      }
    });
    
    String theme = Settings.propJGoodiesTheme.getString();
    if (theme == null) {
      theme = PlasticLookAndFeel.createMyDefaultTheme().getClass().getName();
    }

    for (int i = 0;i< themes.length;i++) {
      if (themes[i].getClass().getName().equals(theme)) {
        mColorScheme.setSelectedIndex(i);
      }
    }
    
    content.add(mColorScheme, cc.xy(3,1));
    
    mShadow = new JCheckBox(mLocalizer.msg("dropShadow", "Drop Shadow on Menus"));
    mShadow.setSelected(Settings.propJGoodiesShadow.getBoolean());
    content.add(mShadow, cc.xyw(1,3,3));
    
    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed();
      }
    });
    
    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelPressed();
      }
    });

    ButtonBarBuilder2 bar = new ButtonBarBuilder2();
    bar.addButton(new JButton[] {ok, cancel});

    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    panel.add(bar.getPanel());
    content.add(panel, cc.xyw(1,5,3));
   
    UiUtilities.registerForClosing(this);
    
    pack();
  }

  /**
   * Cancel was pressed
   */
  protected void cancelPressed() {
    setVisible(false);
  }

  /**
   * OK was pressed
   */
  protected void okPressed() {
    Settings.propJGoodiesTheme.setString(mColorScheme.getSelectedItem().getClass().getName());
    Settings.propJGoodiesShadow.setBoolean(mShadow.isSelected());
    setVisible(false);
  }

  /**
   * Close the Dialog
   */
  public void close() {
    cancelPressed();
  }
}