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
package tvbrowser.extras.programinfo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.core.contextmenu.ConfigMenuItem;
import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.contextmenu.SeparatorMenuItem;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.mainframe.MainFrame;
import util.browserlauncher.Launch;
import util.program.ProgramTextCreator;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.findasyoutype.TextComponentFindAction;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.ExtendedHTMLEditorKit;

import com.l2fprod.common.swing.JTaskPane;
import com.l2fprod.common.swing.JTaskPaneGroup;

import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Program;
import devplugin.SettingsItem;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */

public class ProgramInfoDialog extends JDialog implements SwingConstants, WindowClosingIf {

  private static final long serialVersionUID = 1L;

  protected static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ProgramInfoDialog.class);

  private JEditorPane mInfoEP;
  private JTaskPane mPluginsPane;
  private JTaskPaneGroup mFunctionGroup;
  private Program mProgram;
  private ExtendedHTMLDocument mDoc;
  private JScrollPane mActionsPane;
  private TextComponentFindAction mFindAsYouType;
  private ActionMenu mSearchMenu;
  private TaskMenuAction mTextSearch;
  
  private boolean mShowSettings;

  /**
   * @param parent
   *          The parent dialog.
   * @param program
   *          The program to show the info for.
   * @param pluginsSize
   *          The size of the Functions Panel.
   * @param showSettings
   *          Show the settings button.
   */
  public ProgramInfoDialog(Dialog parent, Program program,
      Dimension pluginsSize, boolean showSettings) {
    super(parent, true);
    init(program, pluginsSize, showSettings);
  }

  /**
   * @param parent
   *          The parent frame.
   * @param program
   *          The program to show the info for.
   * @param pluginsSize
   *          The size of the Functions Panel.
   * @param showSettings
   *          Show the settings button.
   */
  public ProgramInfoDialog(Frame parent, Program program,
      Dimension pluginsSize, boolean showSettings) {
    super(parent, true);
    init(program, pluginsSize, showSettings);
  }
  
  private void init(final Program program, Dimension pluginsSize,
      boolean showSettings) {
    UiUtilities.registerForClosing(this);
    
    mShowSettings = showSettings;
    mProgram = program;
    mFunctionGroup = new JTaskPaneGroup();
    mFunctionGroup.setTitle(mLocalizer.msg("functions", "Functions"));

    setTitle(mLocalizer.msg("title", "Program information"));

    JPanel main = new JPanel(new BorderLayout());
    main.setPreferredSize(new Dimension(750, 500));
    main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setContentPane(main);

    mInfoEP = new ProgramEditorPane();
    mInfoEP.setEditorKit(new ExtendedHTMLEditorKit());
    
    mDoc = (ExtendedHTMLDocument) mInfoEP.getDocument();

    mInfoEP.setText(ProgramTextCreator.createInfoText(mProgram, mDoc, ProgramInfo.getInstance().getOrder(), getFont(true), getFont(false), ProgramInfo.getInstance().getProgramPanelSettings(), true, ProgramInfo.getInstance().getProperty("zoom","false").compareTo("true") == 0 ? Integer.parseInt(ProgramInfo.getInstance().getProperty("zoomValue","100")):100));
    mInfoEP.setEditable(false);
    mInfoEP.addHyperlinkListener(new HyperlinkListener() {
      private String mTooltip;
      public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
          mInfoEP.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          mTooltip = mInfoEP.getToolTipText();
          mInfoEP.setToolTipText(evt.getURL().toExternalForm());
        }
        if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
          mInfoEP.setCursor(Cursor.getDefaultCursor());
          mInfoEP.setToolTipText(mTooltip);
        }
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          URL url = evt.getURL();
          if (url != null) {
            Launch.openURL(url.toString());
          }
        }
      }
    });
    
    mFindAsYouType = new TextComponentFindAction(mInfoEP, true);
    
    /*
     * mInfoEP.addMouseListener(new MouseAdapter(){ public void
     * mousePressed(MouseEvent evt) { if (evt.isPopupTrigger()) { showPopup(evt,
     * program); } }
     * 
     * public void mouseReleased(MouseEvent evt) { if (evt.isPopupTrigger()) {
     * showPopup(evt, program); } }
     * 
     * public void mouseClicked(MouseEvent e) { handleMouseClicked(e, program); }
     * });
     */

    final JScrollPane scrollPane = new JScrollPane(mInfoEP);
    scrollPane.getVerticalScrollBar().setUnitIncrement(30);

    // ScrollActions
    Action up = new AbstractAction() {

      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        scrollPane.getVerticalScrollBar().setValue(
            scrollPane.getVerticalScrollBar().getValue()
                - scrollPane.getVerticalScrollBar().getUnitIncrement());
      }
    };

    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
    mInfoEP.getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).put(stroke,
        "SCROLL_UP");
    mInfoEP.getInputMap(JRootPane.WHEN_FOCUSED).put(stroke, "SCROLL_UP");
    mInfoEP.getActionMap().put("SCROLL_UP", up);

    getRootPane().getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).put(stroke,
        "SCROLL_UP");
    getRootPane().getInputMap(JRootPane.WHEN_FOCUSED).put(stroke, "SCROLL_UP");
    getRootPane().getActionMap().put("SCROLL_UP", up);

    Action down = new AbstractAction() {

      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        scrollPane.getVerticalScrollBar().setValue(
          scrollPane.getVerticalScrollBar().getValue()
              + scrollPane.getVerticalScrollBar().getUnitIncrement());
      }
    };

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
    mInfoEP.getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).put(stroke,
        "SCROLL_DOWN");
    mInfoEP.getInputMap(JRootPane.WHEN_FOCUSED).put(stroke, "SCROLL_DOWN");
    mInfoEP.getActionMap().put("SCROLL_DOWN", down);

    getRootPane().getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).put(stroke,
        "SCROLL_DOWN");
    getRootPane().getInputMap(JRootPane.WHEN_FOCUSED)
        .put(stroke, "SCROLL_DOWN");
    getRootPane().getActionMap().put("SCROLL_DOWN", down);

    mPluginsPane = new JTaskPane();
    mPluginsPane.add(mFunctionGroup);
    
    mActionsPane = new JScrollPane(mPluginsPane);
    
    JButton configBtn = new JButton(mLocalizer.msg("config","Configure view"));
    configBtn.setIcon(IconLoader.getInstance().getIconFromTheme("categories",
        "preferences-desktop", 16));
    
    JPanel bottomLeft = new JPanel(new BorderLayout(3,0));    
    
    if (showSettings)
      bottomLeft.add(configBtn, BorderLayout.WEST);
    
    if (pluginsSize == null)
      mActionsPane.setPreferredSize(new Dimension(250, 500));
    else
      mActionsPane.setPreferredSize(pluginsSize);

    if(ProgramInfo.getInstance().isShowFunctions()) {
      JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      split.setDividerSize(5);
      split.setContinuousLayout(true);
      split.setDividerLocation(mActionsPane.getPreferredSize().width + 1);
      split.setLeftComponent(mActionsPane);
      split.setRightComponent(scrollPane);
      main.add(split, BorderLayout.CENTER);
      mFindAsYouType.installKeyListener(split);
      
      addComponentListener(new ComponentListener() {
        public void componentResized(ComponentEvent e) {
          mActionsPane.getVerticalScrollBar().setBlockIncrement(mActionsPane.getVisibleRect().height);
        }

        public void componentShown(ComponentEvent e) {
          mActionsPane.getVerticalScrollBar().setBlockIncrement(mActionsPane.getVisibleRect().height);
        }

        public void componentHidden(ComponentEvent e) {}
        public void componentMoved(ComponentEvent e) {}
      });
    }
    else {      
      final JButton functions = new JButton(mLocalizer.msg("functions","Functions"));
      functions.setFocusable(false);
      
      functions.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if(e.getClickCount() == 1) {
            JPopupMenu popupMenu = PluginProxyManager.createPluginContextMenu(mProgram,
                ProgramInfoProxy.getInstance());            
            popupMenu.show(functions, e.getX(), e.getY() - popupMenu.getPreferredSize().height);
          }
        }
      });
      
      if(showSettings)
        bottomLeft.add(functions, BorderLayout.EAST);
      else
        bottomLeft.add(functions, BorderLayout.WEST);
      
      main.add(scrollPane, BorderLayout.CENTER);
    }

    // buttons
    JPanel buttonPn = new JPanel(new BorderLayout(0,5));
    buttonPn.add(mFindAsYouType.getSearchBar(),BorderLayout.NORTH);
    buttonPn.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

    main.add(buttonPn, BorderLayout.SOUTH);

    configBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
        MainFrame.getInstance().showSettingsDialog(
            SettingsItem.PROGRAMINFO);
      }
    });
    
    buttonPn.add(bottomLeft, BorderLayout.WEST);

    JButton closeBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    closeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        close();
      }
    });

    buttonPn.add(closeBtn, BorderLayout.EAST);

    getRootPane().setDefaultButton(closeBtn);

    /*
     * The action for the search button in the function panel.
     */
    final Action searchAction = new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        //Open the SearchPanel
        if(mFindAsYouType.getCloseButton().isVisible()) {
          mFindAsYouType.interrupt();
          mFindAsYouType.getSearchBar().setVisible(false);
          mFindAsYouType.getCloseButton().setVisible(false);
        }
        else
          mFindAsYouType.showSearchBar();
      }
    };

    searchAction.putValue(Action.SMALL_ICON, IconLoader.getInstance()
        .getIconFromTheme("actions", "system-search", 16));
    searchAction.putValue(Action.ACTION_COMMAND_KEY, "action");
    searchAction.putValue(Action.NAME, mLocalizer.msg("search", "Search Text"));

    mSearchMenu = new ActionMenu(searchAction);
    
    mFindAsYouType.installKeyListener(scrollPane);
    mFindAsYouType.installKeyListener(main);
    mFindAsYouType.installKeyListener(configBtn);
    mFindAsYouType.installKeyListener(closeBtn);
    mFindAsYouType.installKeyListener(buttonPn);
    mFindAsYouType.installKeyListener(mPluginsPane);
    mFindAsYouType.installKeyListener(mActionsPane);
    mFindAsYouType.installKeyListener(mFunctionGroup);
    mFindAsYouType.installKeyListener(getRootPane());
    mFindAsYouType.installKeyListener(mActionsPane.getVerticalScrollBar());
    mFindAsYouType.installKeyListener(scrollPane.getVerticalScrollBar());
    
    addPluginActions(false);
    
    mFindAsYouType.getCloseButton().addComponentListener(new ComponentAdapter() {
      public void componentHidden(ComponentEvent e) {
        if(mTextSearch != null)
          mTextSearch.setText(mLocalizer.msg("search", "Search Text"));
        searchAction.putValue(Action.NAME, mLocalizer.msg("search", "Search Text"));
      }
      public void componentShown(ComponentEvent e) {
        if(mTextSearch != null)
          mTextSearch.setText(mLocalizer.msg("closeSearch", "Close search bar"));
        searchAction.putValue(Action.NAME, mLocalizer.msg("closeSearch", "Close search bar"));
      }
    });
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        close();
      }
    });

    // Scroll to the beginning
    Runnable runnable = new Runnable() {
      public void run() {
        scrollPane.getVerticalScrollBar().setValue(0);
      }
    };
    SwingUtilities.invokeLater(runnable);
    
    if(ProgramInfo.getInstance().getProperty("showSearch","false").equals("true")) {
      mFindAsYouType.showSearchBar();
      if(mTextSearch != null)
        mTextSearch.setText(mLocalizer.msg("closeSearch", "Close search bar"));
    }
  }
    
  protected void addPluginActions(boolean rebuild) {
    mFunctionGroup.removeAll();

    if(ProgramInfo.getInstance().isShowTextSearchButton())
      mTextSearch = new TaskMenuAction(mFunctionGroup, mProgram, mSearchMenu,
        this, "id_sea", mFindAsYouType);
    
    ContextMenuIf[] p = ContextMenuManager.getInstance().getAvailableContextMenuIfs(false, true);

    for (int i = 0; i < p.length; i++) {
      if(p[i].getId().compareTo(SeparatorMenuItem.SEPARATOR) == 0) {
        mFunctionGroup.add(Box.createRigidArea(new Dimension(0,2)));
        mFunctionGroup.add(new JSeparator());
        mFunctionGroup.add(Box.createRigidArea(new Dimension(0,2)));
      } else if(p[i].getId().compareTo(ConfigMenuItem.CONFIG) == 0 && mShowSettings) {
        Action action = new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            MainFrame.getInstance().showSettingsDialog(SettingsItem.CONTEXTMENU);
          }
        };

        action.putValue(Action.SMALL_ICON,IconLoader.getInstance().getIconFromTheme("categories", "preferences-desktop", 16));
        action.putValue(Action.NAME, ConfigMenuItem.getInstance().toString());

        ActionMenu configure = new ActionMenu(action);
        new TaskMenuAction(mFunctionGroup, mProgram, configure, this,
            "id_configure", mFindAsYouType);
      } else {
        ActionMenu menu = p[i].getContextMenuActions(mProgram);
        
        if (menu != null && !p[i].equals(ProgramInfo.getInstance()))
          new TaskMenuAction(mFunctionGroup, mProgram, menu, this,
              p[i].getId(), mFindAsYouType);
      }
    }

    if (rebuild) {
      mInfoEP.setText(ProgramTextCreator.createInfoText(mProgram, mDoc, ProgramInfo.getInstance().getOrder(), getFont(true), getFont(false), ProgramInfo.getInstance().getProgramPanelSettings(), true, ProgramInfo.getInstance().getProperty("zoom","false").compareTo("true") == 0 ? Integer.parseInt(ProgramInfo.getInstance().getProperty("zoomValue","100")):100));
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          mInfoEP.setCaretPosition(0);
        }
      });
    }

    mPluginsPane.revalidate();
  }
  
  private Font getFont(boolean title) {
    if(title)      
      return new Font(ProgramInfo.getInstance().getUserfont("titlefont", "Verdana"), Font.BOLD, Integer.parseInt(ProgramInfo.getInstance().getUserfont("title", "18")));
    else
      return new Font(ProgramInfo.getInstance().getUserfont("bodyfont", "Verdana"), Font.PLAIN, Integer.parseInt(ProgramInfo.getInstance().getUserfont("small", "11")));
  }

  public void close() {
    ProgramInfo.getInstance().setSettings(this, mActionsPane.getSize());
    ProgramInfo.getInstance().setExpanded("showSearch",mFindAsYouType.isAlwaysVisible());
    dispose();
  }
  
  public void setVisible(boolean value) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        ((JPanel)getContentPane()).updateUI();
      }
    });

    super.setVisible(value);
  }
}