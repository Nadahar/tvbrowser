package tvpearlplugin;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.icontheme.IconLoader;
import util.programkeyevent.ProgramKeyAndContextMenuListener;
import util.programkeyevent.ProgramKeyEventHandler;
import util.programmouseevent.ProgramMouseAndContextMenuListener;
import util.programmouseevent.ProgramMouseEventHandler;
import util.ui.Localizer;
import util.ui.SearchFormSettings;
import util.ui.SearchHelper;
import util.ui.SendToPluginDialog;
import util.ui.UiUtilities;
import util.ui.menu.MenuUtil;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import devplugin.Plugin;
import devplugin.PluginManager;
import devplugin.Program;

public class PearlPanel extends JPanel {
  private static final Localizer mLocalizer = PearlDialog.mLocalizer;
  
  private JScrollPane mScrollPane;
  private JList mDataList;
  private JButton mSendBn;
  private JButton mCloseBn;
  private JButton mUpdateBn;
  private DefaultListModel mProgramList;
  
  public PearlPanel(PearlDialog dialog) {
    createGUI(dialog);
  }
  
  private void createGUI(final PearlDialog dialog)
  {
    setOpaque(false);
    setLayout(new BorderLayout());
    setBorder(UiUtilities.DIALOG_BORDER);
    
    mProgramList = new DefaultListModel();
    mDataList = new JList(mProgramList)
    {
      private static final long serialVersionUID = 1L;

      public JToolTip createToolTip()
      {
        return new TVPearlToolTip();
      }

      public String getToolTipText(final MouseEvent evt)
      {
        final int index = locationToIndex(evt.getPoint());

        final Object item = getModel().getElementAt(index);
        if (item instanceof TVPProgram)
        {
          final TVPProgram p = (TVPProgram) item;
          final HTTPConverter converter = new HTTPConverter();
          final String info = converter.convertToString(p.getInfo());
          if (info.length() == 0) {
            return mLocalizer.msg("noInfo", "(no information)");
          }
          return info;
        }
        return null;
      }
    };
    mDataList.setCellRenderer(new TVPearlListCellRenderer());
    mDataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    ProgramMouseEventHandler handler = new ProgramMouseEventHandler(new ProgramMouseAndContextMenuListener() {
      
      @Override
      public void showContextMenu(MouseEvent e) {
        checkPopup(e);
      }
      
      @Override
      public void mouseEventActionFinished() {
        
      }
      
      @Override
      public Program getProgramForMouseEvent(MouseEvent e) {
        final int index = mDataList.locationToIndex(e.getPoint());
        
        if (mDataList.getModel().getElementAt(index) instanceof TVPProgram) {
          final TVPProgram pearl = (TVPProgram) mDataList.getModel()
              .getElementAt(
              index);
          final Program prog = pearl.getProgram();
          
          if(prog != null) {
            return prog;
          }
          else {
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
              TVPearlPlugin.getInstance().showPearlInfo(pearl);
            }
          }
        }
        
        return null;
      }
    }, null);
    
    ProgramKeyEventHandler keyHandler = new ProgramKeyEventHandler(new ProgramKeyAndContextMenuListener() {
      @Override
      public void showContextMenu(Program prog) {
        JPopupMenu popup = Plugin.getPluginManager().createPluginContextMenu(prog,
            null);
        
        Point p = mDataList.indexToLocation(mDataList.getSelectedIndex());
        
        popup.show(mDataList, p.x, p.y);
      }
      
      @Override
      public void keyEventActionFinished() {}
      
      @Override
      public Program getProgramForKeyEvent(KeyEvent e) {
        final int index = mDataList.getSelectedIndex();
        
        if(index >= 0 && mDataList.getModel().getElementAt(index) instanceof TVPProgram) {
          final TVPProgram pearl = (TVPProgram) mDataList.getModel()
              .getElementAt(
              index);
          final Program prog = pearl.getProgram();
          
          if(prog != null) {
            return prog;
          }
          else if(e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
            Point p = mDataList.indexToLocation(index);
            
            createNoProgramInstanceMenu(pearl).show(mDataList, p.x, p.y);
          }
          else if(e.getKeyCode() == KeyEvent.VK_D) {
            TVPearlPlugin.getInstance().showPearlInfo(pearl);
          }
        }
        
        return null;
      }
    }, null);
    
    mDataList.addMouseListener(handler);
    mDataList.addKeyListener(keyHandler);
    
    mDataList.addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(final ListSelectionEvent e)
      {
        if (!mDataList.getValueIsAdjusting())
        {
          boolean isEnable = false;
          final Object[] selectedValues = mDataList.getSelectedValues();
          if (selectedValues.length == 1
              && (selectedValues[0] instanceof TVPProgram)) {
            final TVPProgram p = (TVPProgram) selectedValues[0];
            if (p.wasFound()) {
              isEnable = true;
            }
          }
          mSendBn.setEnabled(isEnable);
        }
      }
    });

    mScrollPane = new JScrollPane(mDataList);
    add(mScrollPane, BorderLayout.CENTER);

    final ButtonBarBuilder builderButton = new ButtonBarBuilder();
    builderButton.setLeftToRight(true);
    builderButton.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
    builderButton.getPanel().setOpaque(false);

    final JButton configBn = new JButton(IconLoader.getInstance()
        .getIconFromTheme("categories", "preferences-system", 16));
    configBn.setToolTipText(mLocalizer.msg("config", "Configure TV Pearl"));
    configBn.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent e)
      {
        if(dialog != null) {
          dialog.close();
        }
        
        Plugin.getPluginManager().showSettings(TVPearlPlugin.getInstance());
      }
    });
    builderButton.addFixed(configBn);
    builderButton.addRelatedGap();

    mSendBn = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "edit-copy", 16));
    mSendBn.setToolTipText(mLocalizer.msg("send", "Send to other Plugins"));
    mSendBn.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent e)
      {
        showSendDialog();
      }
    });
    builderButton.addFixed(mSendBn);
    builderButton.addRelatedGap();

    builderButton.addGlue();

    mUpdateBn = new JButton(mLocalizer.msg("update", "Update"));
    mUpdateBn.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent evt)
      {
        try
        {
          mUpdateBn.setEnabled(false);
          getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          // TVPearlPlugin.getInstance().update();
          TVPearlPlugin.getInstance().run();
        }
        catch (Exception e)
        {
          mUpdateBn.setEnabled(true);
        }
        finally
        {
          getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      }
    });
    mUpdateBn.setVisible(TVPearlPlugin.getSettings().getUpdatePearlsManually());
    builderButton.addFixed(mUpdateBn);
    
    mCloseBn = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    
    if(dialog != null) {
      builderButton.addRelatedGap();
      
      mCloseBn.addActionListener(new ActionListener()
      {
        public void actionPerformed(final ActionEvent evt)
        {
          dialog.dispose();
        }
      });
      
      builderButton.addFixed(mCloseBn);
    }

    add(builderButton.getPanel(), BorderLayout.SOUTH);

    if(dialog != null) {
      dialog.getRootPane().setDefaultButton(mCloseBn);
    }

    updateProgramList();
  }
  
  private JPopupMenu createNoProgramInstanceMenu(final TVPProgram popupProgram) {
    JPopupMenu popup = new JPopupMenu();
    JMenuItem item = new JMenuItem(mLocalizer.msg("comment", "TV Pearl comment"));
    item.setIcon(TVPearlPlugin.getInstance().getSmallIcon());
    item.setFont(MenuUtil.CONTEXT_MENU_BOLDFONT);
    item.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent arg0)
      {
        if (popupProgram != null)
        {
          TVPearlPlugin.getInstance().showPearlInfo(popupProgram);
        }
      }
    });
    popup.add(item);
    item = new JMenuItem(mLocalizer.msg("menu.repetitions",
        "Search repetitions"), Plugin.getPluginManager()
        .getIconFromTheme(TVPearlPlugin.getInstance(), "action",
            "system-search", 16));
    item.addActionListener(new ActionListener() {

      public void actionPerformed(final ActionEvent event) {
        final SearchFormSettings searchSettings = new SearchFormSettings(
            popupProgram.getTitle());
        searchSettings
            .setSearcherType(PluginManager.SEARCHER_TYPE_EXACTLY);
        SearchHelper.search(UiUtilities.getLastModalChildOf(TVPearlPlugin.getInstance().getSuperFrame()), searchSettings);
      }
    });
    popup.add(item);
    
    return popup;
  }
  
  private void checkPopup(final MouseEvent e)
  {
    if (e.isPopupTrigger())
    {
      final int index = mDataList.locationToIndex(e.getPoint());

      if (mDataList.getModel().getElementAt(index) instanceof TVPProgram)
      {
        mDataList.setSelectedIndex(index);
        JPopupMenu popup;
        final TVPProgram pearlProgram = (TVPProgram) mDataList.getModel()
            .getElementAt(index);
        final Program program = pearlProgram.getProgram();
        if (program != null)
        {
          popup = Plugin.getPluginManager().createPluginContextMenu(program,
              null);
        }
        else
        {
          popup = createNoProgramInstanceMenu(pearlProgram);
        }

        final JPopupMenu openPopup = popup;
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            openPopup.show(mDataList, e.getX() - 15, e.getY() - 15);
          }
        });
      }
    }
  }

  /**
   * show send to plugin dialog
   */
  private void showSendDialog()
  {
    final Object[] selectedValues = mDataList.getSelectedValues();
    if (selectedValues.length == 1)
    {
      if (selectedValues[0] instanceof TVPProgram)
      {
        final TVPProgram p = (TVPProgram) selectedValues[0];
        if (p.wasFound())
        {
          final Program[] programArr = { p.getProgram() };

          final SendToPluginDialog send = new SendToPluginDialog(TVPearlPlugin
              .getInstance(), UiUtilities.getLastModalChildOf(TVPearlPlugin.getInstance().getSuperFrame()), programArr);
          send.setVisible(true);
        }
      }
    }
  }

  public void updateProgramList()
  {
    final Calendar now = Calendar.getInstance();
    int index = -1;

    mProgramList.clear();
    for (TVPProgram item : TVPearlPlugin.getInstance().getProgramList())
    {
      mProgramList.addElement(item);
      if (index == -1)
      {
        final Program program = item.getProgram();
        // find next pearl or first still running program
        if ((now.compareTo(item.getStart()) < 0) || (program != null && !program.isExpired()))
        {
          index = mProgramList.size() - 1;
        }
      }
    }
    if (mUpdateBn != null)
    {
      mUpdateBn.setEnabled(true);
    }
    mDataList.revalidate();
    mDataList.repaint();
    if (mProgramList.getSize() > 0)
    {
      mDataList.setSelectedIndex(0);
      if (mProgramList.getSize() > index)
      {
        mDataList.setSelectedIndex(index);
        mDataList.ensureIndexIsVisible(index);
      }
    }
  }
  
  public void update()
  {
    mDataList.repaint();
  }
}
