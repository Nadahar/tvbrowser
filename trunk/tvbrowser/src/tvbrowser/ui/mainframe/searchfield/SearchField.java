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
package tvbrowser.ui.mainframe.searchfield;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.UIManager;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.SearchForm;
import util.ui.SearchFormSettings;
import util.ui.SearchHelper;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A SearchField for the Toolbar
 * 
 * @author bodum
 */
public class SearchField extends JPanel {
  /** The localizer of this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SearchField.class);
  /** TextField */
  private SearchTextField mText;
  /** Settings for the Search */
  private SearchFormSettings mSearchFormSettings = new SearchFormSettings("");
  /** Button for the Settings-Popup*/
  private JButton mSearchButton;
  
  /**
   * Create SearchField
   */
  public SearchField() {
    createGui();
  }

  /**
   * Create the GUI
   */
  private void createGui() {
    JPanel panel = new JPanel();
    panel.setLayout(new FormLayout("1dlu,pref, 2dlu, 50dlu", "fill:pref:grow"));
    Color background = new Color(UIManager.getColor("TextField.background").getRGB());
    
    panel.setBackground(background);
    panel.setBorder(UIManager.getBorder("TextField.border"));
    
    mText = new SearchTextField(15);
    mText.setBorder(BorderFactory.createEmptyBorder());
    mText.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          mSearchFormSettings.setSearchText(mText.getText());
          SearchHelper.search(mText, mSearchFormSettings);
        }
      }
    });
    
    CellConstraints cc = new CellConstraints();

    mSearchButton = new JButton(IconLoader.getInstance().getIconFromTheme("action", "system-search", 16)); 
    mSearchButton.setBorder(BorderFactory.createEmptyBorder());
    mSearchButton.setOpaque(false);
    mSearchButton.setMargin(new Insets(0, 0, 0, 0));
    mSearchButton.setFocusPainted(false);    
    mSearchButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showConfigureDialog();
      };
    });
    
    panel.add(mSearchButton, cc.xy(2,1));
    panel.add(mText, cc.xy(4,1));
    
    setLayout(new FormLayout("fill:pref:grow, pref", "fill:pref:grow, pref, fill:pref:grow"));
    add(panel, new CellConstraints().xy(2, 2));
  }

  /**
   * Show the Configuration-Dialog
   */
  protected void showConfigureDialog() {
    SearchForm form = new SearchForm(false, false, true);
    form.setSearchFormSettings(mSearchFormSettings);
    
    final JDialog configure = new JDialog(MainFrame.getInstance(), mLocalizer.msg("settingsTitle","Search-Settings"), true);
    
    JPanel panel = (JPanel)configure.getContentPane();
    panel.setBorder(Borders.DLU4_BORDER);
    panel.setLayout(new FormLayout("fill:pref:grow, 3dlu, pref", "pref, 3dlu, pref"));
    
    CellConstraints cc = new CellConstraints();
    
    panel.add(form, cc.xyw(1, 1, 3));
    
    JButton ok = new JButton(mLocalizer.msg("ok", "OK"));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        configure.setVisible(false);
      }
    });
    
    panel.add(ok, cc.xy(3,3));

    UiUtilities.registerForClosing(new WindowClosingIf() {
      public void close() {
        configure.setVisible(false);
      }

      public JRootPane getRootPane() {
        return configure.getRootPane();
      }
    });

    configure.pack();
    UiUtilities.centerAndShow(configure);
    
    mSearchFormSettings = form.getSearchFormSettings();
  }

  
}