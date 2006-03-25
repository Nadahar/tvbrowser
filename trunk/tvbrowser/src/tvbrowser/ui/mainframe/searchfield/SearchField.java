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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.ProgramList;
import util.ui.SearchForm;
import util.ui.SearchFormSettings;
import util.ui.SendToPluginDialog;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramSearcher;

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
  private JToggleButton mSearchButton;
  
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
        if (e.getKeyCode() == KeyEvent.VK_ENTER) 
          doSearch();
      }
    });
    
    CellConstraints cc = new CellConstraints();

    mSearchButton = new JToggleButton(IconLoader.getInstance().getIconFromTheme("action", "system-search", 16)); 
    mSearchButton.setBorder(BorderFactory.createEmptyBorder());
    mSearchButton.setOpaque(false);
    mSearchButton.setMargin(new Insets(0, 0, 0, 0));
    mSearchButton.setFocusPainted(false);    
    mSearchButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showPopUp();
      };
    });
    
    panel.add(mSearchButton, cc.xy(2,1));
    panel.add(mText, cc.xy(4,1));
    
    panel.setMaximumSize(new Dimension(Sizes.dialogUnitXAsPixel(55, this), 30));
    
    setLayout(new BorderLayout());
    add(panel, BorderLayout.EAST);
  }

  /**
   * Show Popup if Settings-Button is pressed
   */
  protected void showPopUp() {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem item = new JMenuItem("Configure");
    
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showConfigureDialog();
      };
    });
    
    menu.add(item);
    
    menu.addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuCanceled(PopupMenuEvent e) {}
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        mSearchButton.setSelected(false);
      }
    });
    
    menu.show(mSearchButton, 0, mSearchButton.getHeight());
  }

  /**
   * Show the Configuration-Dialog
   */
  protected void showConfigureDialog() {
    SearchForm form = new SearchForm(false, false, true);
    form.setSearchFormSettings(mSearchFormSettings);
    
    JDialog configure = new JDialog(MainFrame.getInstance(), true);
    
    JPanel panel = (JPanel)configure.getContentPane();
    
    panel.setLayout(new BorderLayout());
    
    panel.add(form);
    
    configure.pack();
    UiUtilities.centerAndShow(configure);
    
    mSearchFormSettings = form.getSearchFormSettings();
  }

  /**
   * Starts the search.
   */  
  private void doSearch() {
    new Thread(new Runnable() {
      public void run() {
        mSearchFormSettings.setSearchText(mText.getText());
        ProgramFieldType[] fieldArr = mSearchFormSettings.getFieldTypes();
        devplugin.Date startDate = new devplugin.Date();
        
        try {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          ProgramSearcher searcher = mSearchFormSettings.createSearcher();
          Program[] programArr = searcher.search(fieldArr, startDate, mSearchFormSettings.getNrDays(), null, true);

          if (programArr.length == 0) {
            String msg = mLocalizer.msg("nothingFound",
              "No programs found with {0}!", mSearchFormSettings.getSearchText());
            JOptionPane.showMessageDialog(MainFrame.getInstance(), msg);
          } else {
            String title = mLocalizer.msg("hitsTitle",
              "Sendungen mit {0}", mSearchFormSettings.getSearchText());
            showHitsDialog(programArr, title);
          }
        }
        catch (TvBrowserException exc) {
          ErrorHandler.handle(exc);
        }finally{
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      };
    }).start();

  }
  
  /**
   * Shows a dialog containing the hits of the search.
   *
   * @param programArr The hits.
   * @param title The dialog's title.
   */  
  private void showHitsDialog(final Program[] programArr, String title) {
    final JDialog dlg = new JDialog(MainFrame.getInstance(), title, true);
    
    UiUtilities.registerForClosing(new WindowClosingIf() {

      public void close() {
        dlg.dispose();
      }

      public JRootPane getRootPane() {
        return dlg.getRootPane();
      }
      
    });
    
    JPanel main = new JPanel(new BorderLayout());
    main.setBorder(UiUtilities.DIALOG_BORDER);
    dlg.setContentPane(main);
    
    final ProgramList list = new ProgramList(programArr);
    list.addMouseListeners(null);
    
    main.add(new JScrollPane(list), BorderLayout.CENTER);
    
    JPanel buttonPn = new JPanel(new BorderLayout());
    buttonPn.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
    main.add(buttonPn, BorderLayout.SOUTH);
    
    Icon icon = IconLoader.getInstance().getIconFromTheme("actions", "edit-copy", 16);
    JButton sendBt = new JButton(icon);
    sendBt.setToolTipText(mLocalizer.msg("send", "send Programs to another Plugin"));
    sendBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
          Program[] program = list.getSelectedPrograms();

          if(program == null)
            program = programArr;
          
          SendToPluginDialog send = new SendToPluginDialog(null, MainFrame.getInstance(), program);
          send.setVisible(true);
      }
    });
    buttonPn.add(sendBt, BorderLayout.WEST);
    
    JButton closeBt = new JButton(mLocalizer.msg("close", "Close"));
    closeBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dlg.dispose();
      }
    });
    buttonPn.add(closeBt, BorderLayout.EAST);
    
    dlg.getRootPane().setDefaultButton(closeBt);
    
    dlg.setSize(400, 400);
      UiUtilities.centerAndShow(dlg);
  }
  
}