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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.TvBrowserException;
import util.io.stream.ObjectInputStreamProcessor;
import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.SearchForm;
import util.ui.SearchFormSettings;
import util.ui.SearchHelper;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.persona.Persona;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;

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
  private SearchFormSettings mSearchFormSettings;
  /** Button for the Settings-Popup*/
  private JLabel mSearchButton, mGoOrCancelButton;

  private static final String SETTINGS_FILE = "searchfield.SearchField.dat";
  private boolean mGoButton;
  JPanel parent = new JPanel(new BorderLayout());
  final JPanel panel = new JPanel(new BorderLayout(3,0));
  
  private FocusListener mPersonaFocusListener;
  
  /**
   * Create SearchField
   */
  public SearchField() {
    readSearchFormSettings();
    createGui();
    mGoButton = true;
  }

  /**
   * Read the search form settings from the settings file.
   */
  private void readSearchFormSettings() {
    try {
      String home = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome();
      File settingsFile = new File(home,SETTINGS_FILE);
      if (!settingsFile.canRead()) {
        createDefaultSearchFormSettings();
        return;
      }
      StreamUtilities.objectInputStream(settingsFile,
          new ObjectInputStreamProcessor() {
            @Override
            public void process(final ObjectInputStream inputStream)
                throws IOException {
              try {
                mSearchFormSettings = new SearchFormSettings(inputStream);
              } catch (ClassNotFoundException e) {
                e.printStackTrace();
              }
            }
          });
    }catch(Exception e) {
      createDefaultSearchFormSettings();
    }
  }

  /**
   * Save the search form settings to the settings file.
   */
  private void saveSearchFormSettings() {
    String home = Plugin.getPluginManager().getTvBrowserSettings()
        .getTvBrowserUserHome();
    File settingsFile = new File(home, SETTINGS_FILE);
    StreamUtilities.objectOutputStreamIgnoringExceptions(settingsFile,
        new ObjectOutputStreamProcessor() {
          public void process(ObjectOutputStream outputStream)
              throws IOException {
            mSearchFormSettings.writeData(outputStream);
          }
        });
  }

  /**
   * Create the default search form settings.
   */
  private void createDefaultSearchFormSettings() {
    mSearchFormSettings = new SearchFormSettings("");
    mSearchFormSettings.setNrDays(0);
  }

  /**
   * Create the GUI
   */
  private void createGui() {
    
    
    //parent.add(panel,BorderLayout.CENTER);
   
    
    parent.setOpaque(true);

    panel.setBorder(BorderFactory.createCompoundBorder(UIManager.getBorder("TextField.border"),BorderFactory.createEmptyBorder(2,2,1,2)));
    mText = new SearchTextField(15);

    mPersonaFocusListener = new FocusListener() {
      
      @Override
      public void focusLost(FocusEvent e) {
        // TODO Auto-generated method stub
        parent.setBackground(new Color(255,255,255,210));
        parent.setOpaque(false);
        SwingUtilities.invokeLater(new Runnable() {
          
          @Override
          public void run() {
            // TODO Auto-generated method stub
          //  parent.setBackground(new Color(255,255,255,200));
            parent.setOpaque(true);
            panel.repaint();
            
          }
        });
      }
      
      @Override
      public void focusGained(FocusEvent e) {
        //panel.getRootPane().getGlassPane().setBackground(Color.white);
        // TODO Auto-generated method stub
        parent.setBackground(Color.white);
        //parent.setOpaque(true);
      //  panel.setBackground(Color.white);
      }
    };
    
    if(UIManager.getLookAndFeel().getClass().getCanonicalName().equals("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel")) {
  //    mText.setBackground(Color.white);
//      mText.setBorder(BorderFactory.createLineBorder(mText.getBackground(), 2));
      mText.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
      panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),BorderFactory.createEmptyBorder(2,2,1,2)));
    }
    if(UIManager.getLookAndFeel().getClass().getCanonicalName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
      //mText.setBackground(Color.white);
      //mText.setBorder(BorderFactory.createLineBorder(mText.getBackground(), 3));
      mText.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
    }
    else {
      mText.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
      //mText.setBorder(BorderFactory.createLineBorder(mText.getBackground()));
    }

    mText.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        mGoOrCancelButton.setVisible(mText.getText().length() != 0 && !mText.getText().equals(SearchTextField.mLocalizer.ellipsisMsg("search","Search")));
      }
    });

    mText.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {}

      public void insertUpdate(DocumentEvent e) {
        try {
          mGoOrCancelButton.setVisible(e.getDocument().getLength() > 0 && !e.getDocument().getText(0,e.getDocument().getLength()-1).equals(SearchTextField.mLocalizer.ellipsisMsg("search","Search")));
        } catch (BadLocationException e1) {
          // Ignore
        }
      }

      public void removeUpdate(DocumentEvent e) {
        try {
          mGoOrCancelButton.setVisible(e.getDocument().getLength() > 0 && !e.getDocument().getText(0,e.getDocument().getLength()-1).equals(SearchTextField.mLocalizer.ellipsisMsg("search","Search")));
        } catch (BadLocationException e1) {
          // Ignore
        }
      }
    });

    mText.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          startSearch();
        }
      }
    });

 //   panel.setBackground(mText.getBackground());

    mSearchButton = new JLabel(TVBrowserIcons.search(TVBrowserIcons.SIZE_SMALL));
    mSearchButton.setBorder(BorderFactory.createEmptyBorder());
   /* mSearchButton.setContentAreaFilled(false);
    mSearchButton.setMargin(new Insets(0, 0, 0, 0));
    mSearchButton.setFocusPainted(false);
    mSearchButton.setBorderPainted(false);*/
    mSearchButton.setFocusable(false);
    mSearchButton.setRequestFocusEnabled(false);
  //  mSearchButton.setRolloverEnabled(false);
   /* mSearchButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showConfigureDialog(mText);
      };
    });*/
    
    mSearchButton.addMouseListener(new MouseAdapter() {

      @Override
      public void mousePressed(MouseEvent e) {
        showConfigureDialog(mText);
      }
    });
    mSearchButton.setToolTipText(mLocalizer.msg("preferences.tooltip", "Click to change search preferences"));

    mGoOrCancelButton = new JLabel(IconLoader.getInstance().getIconFromTheme("action", "media-playback-start", 16));
    mGoOrCancelButton.setBorder(BorderFactory.createEmptyBorder());
  /*  mGoOrCancelButton.setContentAreaFilled(false);
    mGoOrCancelButton.setMargin(new Insets(0, 0, 0, 0));
    mGoOrCancelButton.setFocusPainted(false);*/
    mGoOrCancelButton.setVisible(false);
    mGoOrCancelButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if(mGoButton) {
          startSearch();
        }
        else {
          cancelPressed();
        }
      };
    });
    setSearchButton();
    mText.setEditable(true);
    mText.setOpaque(false);
    panel.setOpaque(false);
    updatePersona();
//panel.setBackground(new Color(255,255,255,200));
    panel.add(mSearchButton, BorderLayout.WEST);
    panel.add(mText, BorderLayout.CENTER);
    panel.add(mGoOrCancelButton, BorderLayout.EAST);
    parent.add(panel,BorderLayout.CENTER);
    setLayout(new FormLayout("80dlu, 2dlu", "fill:pref:grow, pref, fill:pref:grow"));
    add(parent, new CellConstraints().xy(1, 2));
  }

  /**
   * Start search.
   */
  private void startSearch() {
    mSearchFormSettings.setSearchText(mText.getText());

    if (mSearchFormSettings.getNrDays() == 0) {
      if (mText.getText().length() > 0) {
        try {
          SearchFilter filter = SearchFilter.getInstance();
          filter.setSearch(mSearchFormSettings);
          MainFrame.getInstance().setProgramFilter(filter);
          setCancelButton();

          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              mText.setCaretPosition(mText.getText().length());
            }
          });
        } catch (TvBrowserException e1) {
          e1.printStackTrace();
        }
      } else {
        SearchFilter.getInstance().deactivateSearch();
        MainFrame.getInstance().setProgramFilter(FilterManagerImpl.getInstance().getDefaultFilter());
        setSearchButton();
      }
    } else {
      SearchHelper.search(mText, mSearchFormSettings, new ProgramPanelSettings(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE),false),true);
    }
  }

  /**
   * Cancel the Search
   */
  protected void cancelPressed() {
    mText.setText("");
    SearchFilter.getInstance().deactivateSearch();
    MainFrame.getInstance().setProgramFilter(FilterManagerImpl.getInstance().getDefaultFilter());
    setSearchButton();
    mGoOrCancelButton.setVisible(false);
    mText.focusLost(null);
  }

  /**
   * Show the Configuration-Dialog
   * @param textField
   */
  protected void showConfigureDialog(final SearchTextField textField) {
    final SearchForm form = new SearchForm(false, false, true);
    form.setSearchFormSettings(mSearchFormSettings);

    final JDialog configure = new JDialog(MainFrame.getInstance(), mLocalizer.msg("settingsTitle","Search-Settings"), false);
    configure.setUndecorated(true);
    JPanel panel = (JPanel)configure.getContentPane();
    panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),Borders.DLU4_BORDER));
    panel.setLayout(new FormLayout("fill:pref:grow, 3dlu, pref", "pref, fill:3dlu:grow, pref"));

    form.setParentDialog(configure);

    CellConstraints cc = new CellConstraints();

    panel.add(form, cc.xyw(1, 1, 3));

    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        configure.setVisible(false);
        mSearchFormSettings = form.getSearchFormSettings();
        saveSearchFormSettings();
        textField.requestFocusInWindow();
        textField.selectAll();
        
      }
    });

    panel.add(ok, cc.xy(3,3));

    UiUtilities.registerForClosing(new WindowClosingIf() {
      public void close() {
        configure.removeWindowListener(configure.getWindowListeners()[0]);
        configure.setVisible(false);
        textField.requestFocusInWindow();
        textField.selectAll();
      }

      public JRootPane getRootPane() {
        return configure.getRootPane();
      }
    });

    configure.pack();

    configure.addWindowListener(new WindowAdapter() {
      public void windowDeactivated(WindowEvent e) {
        if(!form.isSearchFieldsSelectionDialogVisible()) {
          ((JDialog)e.getSource()).setVisible(false);
          mSearchFormSettings = form.getSearchFormSettings();
          saveSearchFormSettings();
        }
      }
    });

    Point p = mSearchButton.getLocationOnScreen();

    if(MainFrame.getInstance().getToolbar().getToolbarLocation().compareTo(BorderLayout.NORTH) == 0) {
      configure.setLocation(p.x - configure.getWidth() + mSearchButton.getWidth(),p.y + mSearchButton.getHeight());
    } else {
      configure.setLocation(p.x ,p.y - configure.getHeight());
    }
    configure.setVisible(true);
  }

  /**
   * SearchFilter was deactivated
   */
  public void deactivateSearch() {
    mText.setText("");
    setSearchButton();
    mText.focusLost(null);
    mGoOrCancelButton.setVisible(false);
  }

  /**
   * make a search button from the combined search/cancel button
   */
  private void setSearchButton() {
    mGoButton = true;
    mGoOrCancelButton.setIcon(IconLoader.getInstance().getIconFromTheme("action", "media-playback-start", 16));
    mGoOrCancelButton.setToolTipText(mLocalizer.msg("start.tooltip", "Start search"));
 }

  /**
   * make a cancel button from the combined search/cancel button
   */
  private void setCancelButton() {
    mGoButton = false;
    mGoOrCancelButton.setIcon(IconLoader.getInstance().getIconFromTheme("action", "process-stop", 16));
    mGoOrCancelButton.setToolTipText(mLocalizer.msg("cancel.tooltip", "Cancel search"));
  }

  /**
   * Updates the search field on Persona change.
   */
  public void updatePersona() {
    if(Persona.getInstance().getHeaderImage() != null) {
      parent.setBackground(new Color(255,255,255,210));
      mText.addFocusListener(mPersonaFocusListener);
    }
    else {
      parent.setBackground(Color.white);
      mText.removeFocusListener(mPersonaFocusListener);
    }
  }
}