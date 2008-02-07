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
package util.ui;

import devplugin.Program;
import devplugin.ProgramSearcher;
import devplugin.ProgressMonitor;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This Class helps to search for a Program. 
 * 
 * It creates a new Thread, searches with a SearchFormSettings and Displays the Result
 * @author bodum
 * @since 2.2
 */
public class SearchHelper {
  /** The localizer of this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SearchHelper.class);
  /** Instance of the Helper */
  private static SearchHelper mInstance;

  /** Private Constructor */
  private SearchHelper() {}  

  /**
   * Search for Programs and Display a Result-Dialog.
   * This function creates a new Thread.
   * 
   * @param comp Parent-Component
   * @param settings Settings for the Search.
   * @param pictureSettings Settings for the pictures
   */
  public static void search(Component comp, SearchFormSettings settings, ProgramPanelSettings pictureSettings) {
    if (mInstance == null) {
      mInstance = new SearchHelper();
    }

    mInstance.doSearch(comp, settings, pictureSettings);
  }
  
  /**
   * Search for Programs and Display a Result-Dialog.
   * This function creates a new Thread.
   * 
   * @param comp Parent-Component
   * @param pictureSettings Settings for the pictures
   * @param settings Settings for the Search.
   * @since 2.6
   */
  public static void search(Component comp, PluginPictureSettings pictureSettings, SearchFormSettings settings) {
    search(comp,settings,new ProgramPanelSettings(pictureSettings,false));
  }

  /**
   * Search for Programs and Display a Result-Dialog.
   * This function creates a new Thread.
   * 
   * @param comp Parent-Component
   * @param settings Settings for the Search.
   */
  public static void search(Component comp, SearchFormSettings settings) {
    search(comp,settings,null);
  }

  /**
   * Starts the search.
   * @param comp Parent-Component
   * @param searcherSettings Settings for the Search.
   * @param pictureSettings Settings for Pictures
   */
  private void doSearch(final Component comp,final SearchFormSettings searcherSettings, final ProgramPanelSettings pictureSettings) {
    new Thread(new Runnable() {
      public void run() {
        devplugin.Date startDate = new devplugin.Date();
        Cursor cursor = comp.getCursor();

        try {
          comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          ProgramSearcher searcher = searcherSettings.createSearcher();
          ProgressMonitor progressMonitor = null;
          if (!TvDataUpdater.getInstance().isDownloading()) {
            progressMonitor = MainFrame.getInstance().getStatusBar().createProgressMonitor();
            progressMonitor.setMessage(mLocalizer.msg("searching","Searching"));
          };
          Program[] programArr = searcher.search(searcherSettings.getFieldTypes(), startDate, searcherSettings
              .getNrDays(), searcherSettings.getChannels(), true, progressMonitor);

          comp.setCursor(cursor);
          if (programArr.length == 0) {
            String msg = mLocalizer
                .msg("nothingFound", "No programs found with {0}!", searcherSettings.getSearchText());
            JOptionPane.showMessageDialog(MainFrame.getInstance(), msg);
          } else {
            String title = mLocalizer.msg("hitsTitle", "Sendungen mit {0}", searcherSettings.getSearchText());
            showHitsDialog(comp, programArr, title, pictureSettings);
          }
        } catch (TvBrowserException exc) {
          comp.setCursor(cursor);
          ErrorHandler.handle(exc);
        }
      }
    }, "Search programs").start();

  }

  /**
   * Shows a dialog containing the hits of the search.
   * 
   * @param comp Parent Component
   * @param programArr The hits.
   * @param title The dialog's title.
   * @param pictureSettings Picture Settings
   */
  private void showHitsDialog(Component comp, final Program[] programArr, String title, ProgramPanelSettings pictureSettings) {
    final JDialog dlg;
    
    Window w = UiUtilities.getBestDialogParent(comp);
    
    if (w instanceof Frame)
      dlg = new JDialog((Frame)w, title, true);
    else
      dlg = new JDialog((Dialog)w, title, true);

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


    // Find first program that is not expired
    int curPos = -1;

    int i = 0;
    while (i < programArr.length && curPos == -1) {
        if (!programArr[i].isExpired())
            curPos = i;
        i++;
    }

    final ProgramList list = new ProgramList(programArr, pictureSettings);
    list.addMouseListeners(null);

    main.add(new JScrollPane(list), BorderLayout.CENTER);
    if (curPos >= 0)
        list.setSelectedValue(programArr[curPos], true);

    JPanel buttonPn = new JPanel(new BorderLayout());
    buttonPn.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
    main.add(buttonPn, BorderLayout.SOUTH);

    Icon icon = IconLoader.getInstance().getIconFromTheme("actions", "edit-copy", 16);
    JButton sendBt = new JButton(icon);
    sendBt.setToolTipText(mLocalizer.msg("send", "send Programs to another Plugin"));
    sendBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        Program[] program = list.getSelectedPrograms();

        if (program == null)
          program = programArr;

        SendToPluginDialog send = new SendToPluginDialog(null, MainFrame.getInstance(), program);
        send.setVisible(true);
      }
    });
    buttonPn.add(sendBt, BorderLayout.WEST);

    JButton closeBt = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
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