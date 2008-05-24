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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;

import tvbrowser.core.Settings;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.searchplugin.SearchDialog;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;

import devplugin.Program;
import devplugin.ProgramSearcher;
import devplugin.ProgressMonitor;

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

  private DefaultListModel mListModel;
  
  private ProgressMonitor mProgressMonitor;
  
  private JProgressBar mProgressBar;
  
  /** Private Constructor */
  private SearchHelper() {
    mListModel = null;
  }

  /**
   * Search for Programs and Display a Result-Dialog.
   * This function creates a new Thread.
   * 
   * @param comp Parent-Component
   * @param settings Settings for the Search.
   * @param pictureSettings Settings for the pictures
   */
  public static void search(Component comp, SearchFormSettings settings, ProgramPanelSettings pictureSettings) {
    search(comp,settings,pictureSettings,false);
  }
  
  /**
   * Search for Programs and Display a Result-Dialog.
   * This function creates a new Thread.
   * 
   * @param comp Parent-Component
   * @param settings Settings for the Search.
   * @param pictureSettings Settings for the pictures
   */
  public static void search(Component comp, SearchFormSettings settings, ProgramPanelSettings pictureSettings, boolean showDialog) {
    if (mInstance == null) {
      mInstance = new SearchHelper();
    }
    if (pictureSettings == null) {
      pictureSettings = new ProgramPanelSettings(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE),false);
    }
    mInstance.doSearch(comp, settings, pictureSettings, showDialog);
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
  private void doSearch(final Component comp,final SearchFormSettings searcherSettings, final ProgramPanelSettings pictureSettings, final boolean showDialog) {
    JDialog dlg = null;
    
    if(showDialog) {
      dlg = createHitsDialog(comp, new Program[0], mLocalizer.msg("search","Search"), searcherSettings, pictureSettings);
    }
    
    new Thread(new Runnable() {
      public void run() {
        devplugin.Date startDate = new devplugin.Date();
        Cursor cursor = comp.getCursor();

        try {
          comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          ProgramSearcher searcher = searcherSettings.createSearcher();
          ProgressMonitor progressMonitor = null;
          if (mProgressMonitor == null && !TvDataUpdater.getInstance().isDownloading()) {
            progressMonitor = MainFrame.getInstance().getStatusBar().createProgressMonitor();
            progressMonitor.setMessage(mLocalizer.msg("searching","Searching"));
          };
          
          Program[] programArr = searcher.search(searcherSettings.getFieldTypes(), startDate, searcherSettings
              .getNrDays(), searcherSettings.getChannels(), true, mProgressMonitor != null ? mProgressMonitor : progressMonitor, mListModel);

          comp.setCursor(cursor);
          if (programArr.length == 0) {
            String msg = mLocalizer
                .msg("nothingFound", "No programs found with {0}!", searcherSettings.getSearchText());
            JOptionPane.showMessageDialog(MainFrame.getInstance(), msg);
          } else {
            if(!showDialog) {
              String title = mLocalizer.msg("hitsTitle", "Programs with {0}", searcherSettings.getSearchText());
              
              UiUtilities.centerAndShow(createHitsDialog(comp, programArr, title, searcherSettings, pictureSettings));
            }
            else if(mProgressBar != null) {
              mProgressBar.setVisible(false);
            }
          }
        } catch (TvBrowserException exc) {
          comp.setCursor(cursor);
          ErrorHandler.handle(exc);
        }
      }
    }, "Search programs").start();
    
    if(dlg != null) {
      UiUtilities.centerAndShow(dlg);
    }
  }

  /**
   * Shows a dialog containing the hits of the search.
   * 
   * @param comp Parent Component
   * @param programArr The hits.
   * @param title The dialog's title.
   * @param searchSettings
   * @param pictureSettings Picture Settings
   */
  private JDialog createHitsDialog(Component comp, final Program[] programArr, String title, final SearchFormSettings searchSettings, ProgramPanelSettings pictureSettings) {
    final JDialog dlg;
    
    final Window parentWindow = UiUtilities.getBestDialogParent(comp);
    
    if (parentWindow instanceof Frame) {
      dlg = new JDialog((Frame)parentWindow, title, true);
    } else {
      dlg = new JDialog((Dialog)parentWindow, title, true);
    }

    UiUtilities.registerForClosing(new WindowClosingIf() {

      public void close() {
        dlg.dispose();
      }

      public JRootPane getRootPane() {
        return dlg.getRootPane();
      }

    });

    JPanel main = new JPanel(new BorderLayout());
    main.setBorder(Borders.DLU4_BORDER);
    dlg.setContentPane(main);


    // Find first program that is not expired
    int curPos = -1;

    int i = 0;
    while (i < programArr.length && curPos == -1) {
        if (!programArr[i].isExpired()) {
          curPos = i;
        }
        i++;
    }

    mListModel = new DefaultListModel();
    
    if(programArr != null) {
      for(Program program : programArr) {
        mListModel.addElement(program);
      }
    }
    
    if(programArr == null || programArr.length == 0) {
      mProgressBar = new JProgressBar();
      
      mProgressMonitor = new ProgressMonitor() {

        public void setMaximum(int maximum) {
          mProgressBar.setMaximum(maximum);
        }

        public void setMessage(String msg) {
          
        }

        public void setValue(int value) {
          mProgressBar.setValue(value);
        }
      };
      
      main.add(mProgressBar, BorderLayout.NORTH);
    }
    
    final ProgramList list = new ProgramList(mListModel, pictureSettings);
    
    list.addMouseListeners(null);

    main.add(new JScrollPane(list), BorderLayout.CENTER);
    if (curPos >= 0) {
      list.setSelectedValue(programArr[curPos], true);
    }

    ButtonBarBuilder builder = ButtonBarBuilder.createLeftToRightBuilder();
    builder.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

    // send to plugins
    Icon icon = IconLoader.getInstance().getIconFromTheme("actions", "edit-copy", 16);
    JButton sendBt = new JButton(icon);
    sendBt.setToolTipText(mLocalizer.msg("send", "Send Programs to another Plugin"));
    sendBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        Program[] program = list.getSelectedPrograms();

        if (program == null) {
          program = programArr;
        }

        SendToPluginDialog send = new SendToPluginDialog(null, MainFrame.getInstance(), program);
        send.setVisible(true);
      }
    });
    builder.addFixed(sendBt);

    // change search button
    if (!(comp instanceof SearchDialog)) {
      icon = IconLoader.getInstance().getIconFromTheme("actions", "document-edit", 16);
      JButton changeBt = new JButton(icon);
      changeBt.setToolTipText(mLocalizer.msg("edit", "Change search parameters"));
      changeBt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          dlg.dispose();
          SearchDialog searchDialog;
          if (parentWindow instanceof Frame) {
            searchDialog = new SearchDialog((Frame) parentWindow);
          }
          else {
            searchDialog = new SearchDialog((Dialog) parentWindow);
          }
          searchDialog.setSearchSettings(searchSettings);
          UiUtilities.centerAndShow(searchDialog);
        }
      });
      builder.addRelatedGap();
      builder.addFixed(changeBt);
    }

    // close button
    JButton closeBt = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    closeBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dlg.dispose();
      }
    });
    
    builder.addGlue();
    builder.addFixed(closeBt);
    main.add(builder.getPanel(), BorderLayout.SOUTH);

    dlg.getRootPane().setDefaultButton(closeBt);
    
    Settings.layoutWindow("searchDlg", dlg, new Dimension(400, 400));
    
    return dlg;
  }

}