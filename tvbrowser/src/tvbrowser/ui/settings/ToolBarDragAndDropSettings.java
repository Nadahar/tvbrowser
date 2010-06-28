package tvbrowser.ui.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;

import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.toolbar.ContextMenu;
import tvbrowser.ui.mainframe.toolbar.DefaultToolBarModel;
import tvbrowser.ui.mainframe.toolbar.ToolBar;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;

/**
 * A class to support Drag'n'Drop in the toolbar.
 * 
 * @author René Mach
 */
public class ToolBarDragAndDropSettings extends JDialog implements
    WindowListener, DragGestureListener, DropTargetListener, ActionListener,
    MouseListener, WindowClosingIf {

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ToolBarDragAndDropSettings.class);

  private static final Logger mLog = java.util.logging.Logger
  .getLogger(ToolBarDragAndDropSettings.class.getName());

  private static final long serialVersionUID = 1L;
  /** Actions that are visible in the ToolBar */
  private Vector<Action> mCurrentActions = new Vector<Action>();
  /** Actions the user can add to the ToolBar */
  private Vector<Action> mAvailableActions = new Vector<Action>();
  private JComboBox mShowCB, mLocationCB;
  private JCheckBox mShowToolbarCb, mUseBigIconsCb, mShowSearchFieldCb;
  private JPanel mButtonPanel;
  private boolean mWest;

  private Rectangle2D mCueLine = new Rectangle2D.Float();

  private static ToolBarDragAndDropSettings mInstance = null;

  /**
   * The default constructor.
   * 
   */
  public ToolBarDragAndDropSettings() {
    super(MainFrame.getInstance());
    mInstance = this;

    UiUtilities.registerForClosing(this);

    this.getContentPane().setLayout(new FormLayout("fill:pref:grow", "pref, 3dlu, fill:min:grow, 3dlu, pref, 3dlu, pref"));
    ((JPanel) this.getContentPane()).setBorder(BorderFactory.createEmptyBorder(
        0, 6, 2, 6));

    // Initialize the buttonPanel and fill the vectors with the actions.
    final DefaultToolBarModel toolbarModel = DefaultToolBarModel.getInstance();
    Action[] currentActions = toolbarModel.getActions();

    ArrayList<Action> notSelectedActionsList = new ArrayList<Action>(Arrays.asList(toolbarModel
        .getAvailableActions()));

    for (Action a : currentActions) {
      mCurrentActions.addElement(a);
    }

    for (Action a : currentActions) {
      if (notSelectedActionsList.contains(a)) {
        notSelectedActionsList.remove(a);
      }
    }

    Action[] availableActions = new Action[notSelectedActionsList.size()];
    notSelectedActionsList.toArray(availableActions);

    for (Action a : availableActions) {
      mAvailableActions.addElement(a);
    }

    mAvailableActions.insertElementAt(toolbarModel.getSeparatorAction(), 0);
    mAvailableActions.insertElementAt(toolbarModel.getGlueAction(), 1);
    mAvailableActions.insertElementAt(toolbarModel.getSpaceAction(), 2);

    // Initialize the Panel with the available Buttons
    mButtonPanel = new JPanel();
    mButtonPanel.setLayout(new GridLayout(0, 4, 2, 2));
    mButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    
    // Make the buttonPanel scrollable
    JScrollPane pane = new JScrollPane(mButtonPanel);
    pane.setAlignmentX(Component.LEFT_ALIGNMENT);
    pane.getVerticalScrollBar().setUnitIncrement(73);
    pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    addMouseAdapterForHandCursorToComponent(pane);
    
    // Initialize the Panel for selecting toolBars visibility
    final JPanel tVisPanel = new JPanel();
    tVisPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    tVisPanel.setLayout(new BoxLayout(tVisPanel, BoxLayout.X_AXIS));
    tVisPanel.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0,
        0, 1, 0, Color.GRAY), BorderFactory.createEmptyBorder(10, 5, 9, 5)));

    mShowToolbarCb = new JCheckBox(mLocalizer
        .msg("showToolbar", "Show toolbar"));
    mShowToolbarCb.setSelected(Settings.propIsToolbarVisible.getBoolean());

    tVisPanel.add(mShowToolbarCb);
    tVisPanel.add(Box.createHorizontalGlue());

    mShowSearchFieldCb = new JCheckBox(mLocalizer.msg("showSearchField",
        "Show Search field"));
    mShowSearchFieldCb.setSelected(Settings.propIsSearchFieldVisible
        .getBoolean());

    tVisPanel.add(mShowSearchFieldCb);

    // Initialize the panel for the ToolBar settings
    JPanel tSetPanel = new JPanel(new FormLayout("default,5dlu,default,0dlu:grow,default,5dlu,default,5dlu,default","default"));
    tSetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    mLocationCB = new JComboBox(new String[] { mLocalizer.msg("top", "top"),
        Localizer.getLocalization(Localizer.I18N_LEFT), });
    
    if ("west".equals(Settings.propToolbarLocation.getString())) {
      mLocationCB.setSelectedIndex(1);
      mWest = true;
    } else {
      mWest = false;
    }

    mShowCB = new JComboBox(new String[] {
        ContextMenu.mLocalizer.msg("text.and.icon", "text and icon"),
        ContextMenu.mLocalizer.msg("text", "text"),
        ContextMenu.mLocalizer.msg("icon", "icon") });

    String style = Settings.propToolbarButtonStyle.getString();
    if ("text".equals(style)) {
      mShowCB.setSelectedIndex(1);
    } else if ("icon".equals(style)) {
      mShowCB.setSelectedIndex(2);
    }
    mShowCB.setAlignmentX(Component.CENTER_ALIGNMENT);
    mShowCB.setMaximumSize(mShowCB.getPreferredSize());

    mUseBigIconsCb = new JCheckBox(ContextMenu.mLocalizer.msg("bigIcons",
        "Use big icons"));
    mUseBigIconsCb.setSelected(Settings.propToolbarUseBigIcons.getBoolean());

    CellConstraints cc = new CellConstraints();
    
    tSetPanel.add(new JLabel(mLocalizer.msg("location", "Location")),cc.xy(1,1));
    tSetPanel.add(mLocationCB,cc.xy(3,1));
    tSetPanel.add(new JLabel(mLocalizer.msg("icons", "Icons")),cc.xy(5,1));
    tSetPanel.add(mShowCB,cc.xy(7,1));
    tSetPanel.add(mUseBigIconsCb,cc.xy(9,1));

    tSetPanel.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(1,
        0, 1, 0, Color.GRAY), BorderFactory.createEmptyBorder(10, 5, 9, 5)));

    // The panel for the OK button
    JPanel okButtonPanel = new JPanel();
    okButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    okButtonPanel.setLayout(new BoxLayout(okButtonPanel, BoxLayout.X_AXIS));
    okButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));

    JButton ok = new JButton("OK");
    ok.addActionListener(this);

    okButtonPanel.add(Box.createHorizontalGlue());
    okButtonPanel.add(ok);

    // Register ActionListener to the settings
    mShowToolbarCb.addActionListener(this);
    mShowSearchFieldCb.addActionListener(this);
    mShowCB.addActionListener(this);
    mUseBigIconsCb.addActionListener(this);
    mLocationCB.addActionListener(this);

    // Add the components to the window
    this.getContentPane().add(tVisPanel, cc.xy(1,1));
    this.getContentPane().add(pane, cc.xy(1,3));
    this.getContentPane().add(tSetPanel, cc.xy(1,5));
    this.getContentPane().add(okButtonPanel, cc.xy(1,7));

    // Set up the windows attributes
    this.setSize(630, 400);
    this.addWindowListener(this);
    this.setTitle(mLocalizer.msg("modifyToolbar", "Modify Toolbar"));
    this.setLocationRelativeTo(MainFrame.getInstance());
    this.setVisible(true);

    buildButtonPanel();
    setMainframeMenusEnabled(false);
    init();
  }
  
  private void addMouseAdapterForHandCursorToComponent(final JComponent c) {
    c.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }

      public void mouseExited(MouseEvent e) {
        getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });
  }

  /**
   * @return An Instance of this or null.
   */
  public static ToolBarDragAndDropSettings getInstance() {
    return mInstance;
  }

  private void buildButtonPanel() {
    mButtonPanel.removeAll();

    // Calculate the size of the ButtonPanel
    int n = this.mAvailableActions.size() / 4;
    if (this.mAvailableActions.size() % 4 != 0) {
      n++;
    }
    mButtonPanel.setPreferredSize(new Dimension(570, n * 73));

    // Add all availableActions to the buttonPanel
    for (Action action : mAvailableActions) {
      // <html> is needed to have a black color of the letters of a button,
      // because the Buttons have to be disabled for Drag'n'Drop
      JButton button = new JButton("<html><div style=\"text-align:center\">" + (String) action.getValue(Action.NAME)
          + "</div></html>");
      button.setBorder(new CompoundBorder(BorderFactory
          .createEmptyBorder(1, 1, 1, 1), BorderFactory.createEmptyBorder(1, 1,
          1, 1)));
      
      Icon icon = (Icon) action.getValue(Plugin.BIG_ICON);
      if (icon == null) {
        mLog.warning("Big icon missing for action " + action.getValue(Action.NAME));
        icon = (Icon) action.getValue(Action.SMALL_ICON);
      }
      if ((icon != null)
          && ((icon.getIconHeight() != TVBrowserIcons.SIZE_LARGE) || (icon.getIconWidth() != TVBrowserIcons.SIZE_LARGE))) {
        icon = UiUtilities.scaleIcon(icon, TVBrowserIcons.SIZE_LARGE, TVBrowserIcons.SIZE_LARGE);
      }
      button.setIcon(icon);
      button.setDisabledIcon(icon);
      button.setVerticalTextPosition(SwingConstants.BOTTOM);
      button.setHorizontalTextPosition(SwingConstants.CENTER);
      button.setFont(new Font("Dialog", Font.PLAIN, 10));
      button.addMouseListener(this);
      addMouseAdapterForHandCursorToComponent(button);
      button.setContentAreaFilled(false);
      
      // Set up the available ActionButtons for dragging
      DragSource d = new DragSource();
      d.createDefaultDragGestureRecognizer(button, DnDConstants.ACTION_MOVE, this);

      button.setEnabled(false);
      mButtonPanel.add(button);
    }
    mButtonPanel.updateUI();
  }

  private void init() {
    // set the drop targets of the Actions
    new DropTarget(MainFrame.getInstance().getToolbar(), this);
    new DropTarget(MainFrame.getInstance().getToolBarPanel(), this);
    new DropTarget(mButtonPanel, this);

    // set up the ActionButtons in the ToolBar for dragging
    MainFrame.getInstance().getToolbar().disableForDragAndDrop(this, mWest);

    if (mShowToolbarCb.isSelected()) {
      mShowSearchFieldCb.setEnabled(true);
    } else {
      mShowSearchFieldCb.setEnabled(false);
    }
  }

  private void setMainframeMenusEnabled(boolean enabled) {
    JMenuBar bar = MainFrame.getInstance().getJMenuBar();

    for (int i = 0; i < bar.getMenuCount(); i++) {
      bar.getMenu(i).setEnabled(enabled);
    }
  }

  /**
   * The class to save the action in that is transfered with Drag'n'Drop.
   * 
   * @author René Mach
   */
  private static class TransferAction implements Transferable {
    /** The Action.NAME */
    private String mName;

    /** The index of a ActionButton in the ToolBar */
    private Integer mIndex;

    /**
     * The DataFlavors to recognize this as an ActionButton.
     */
    private DataFlavor mNF;
    private DataFlavor mIF;

    /**
     * The constructor for this class.
     * 
     * @param name
     *          A String that contains the Action.NAME.
     * @param index
     *          The index of the drag source.
     */
    public TransferAction(String name, int index) {
      mName = name;
      mNF = new DataFlavor(Action.class, "Action");
      mIndex = index;
      mIF = new DataFlavor(Integer.class, "Integer");
    }

    public DataFlavor[] getTransferDataFlavors() {
      DataFlavor[] f = { mNF, mIF };
      return (f);
    }

    public boolean isDataFlavorSupported(DataFlavor e) {
      if (e.equals(mNF) || e.equals(mIF)) {
        return true;
      } else {
        return false;
      }
    }

    public Object getTransferData(DataFlavor e)
        throws UnsupportedFlavorException, IOException {
      if (e.equals(mNF)) {
        return mName;
      } else if (e.equals(mIF)) {
        return mIndex;
      } else {
        return null;
      }
    }
  }

  public void windowClosing(WindowEvent e) {
    close();
  }

  public void dragEnter(DropTargetDragEvent e) {
    /*
     * Start drag only if the drag source has the correct DataFlavors.
     */
    DataFlavor[] flavors = e.getCurrentDataFlavors();
    if (flavors.length < 2) {
      e.rejectDrag();
      return;
    }
    if (flavors[0].getHumanPresentableName().equals("Action")
        && flavors[1].getHumanPresentableName().equals("Integer")) {
      e.acceptDrag(e.getDropAction());
    } else {
      e.rejectDrag();
    }
  }

  public void drop(DropTargetDropEvent e) {
    
    // Drop the TransferAction
    e.acceptDrop(e.getDropAction());
    Transferable tr = e.getTransferable();
    DataFlavor[] flavors = tr.getTransferDataFlavors();
    try {
      String name = (String) tr.getTransferData(flavors[0]);
      int index = ((Integer) tr.getTransferData(flavors[1])).intValue();
      Action separator = DefaultToolBarModel.getInstance().getSeparatorAction();
      Action glue = DefaultToolBarModel.getInstance().getGlueAction();
      Action space = DefaultToolBarModel.getInstance().getSpaceAction();

      JComponent target = (JComponent) ((DropTarget) e.getSource())
          .getComponent();
      
      if (target.equals(mButtonPanel)) {
        if (index == -1 && !separator.getValue(Action.NAME).equals(name)
            && !glue.getValue(Action.NAME).equals(name)
            && !space.getValue(Action.NAME).equals(name)) {
          for (Action a : mCurrentActions) {
            if (a.getValue(Action.NAME).equals(name)) {
              mCurrentActions.removeElement(a);
              mAvailableActions.addElement(a);
              break;
            }
          }
        } else if (index != -1) {
          Action a = mCurrentActions.elementAt(index);
          mCurrentActions.removeElementAt(index);
          if (!separator.getValue(Action.NAME).equals(name)
              && !glue.getValue(Action.NAME).equals(name)
              && !space.getValue(Action.NAME).equals(name)
              ) {
            mAvailableActions.addElement(a);
          }
        }
        saveSettings();
      } else if (target.equals(MainFrame.getInstance().getToolbar())
          || ((DropTarget) e.getSource()).getComponent().equals(
              MainFrame.getInstance().getToolBarPanel())) {
        
        Point location = e.getLocation();

        if (mWest) {
          location.setLocation(10, location.y);
        } else {
          location.setLocation(location.x, MainFrame.getInstance().getToolbar()
              .getHeight() / 2);
        }

        JComponent c = (JComponent) MainFrame.getInstance().getToolbar()
            .getComponentAt(location);

        if ((c == null || c instanceof JToolBar) && MainFrame.getInstance().getToolbar().getComponentCount() > 0) {
          c = (JComponent) MainFrame.getInstance().getToolbar().getComponent(
              MainFrame.getInstance().getToolbar().getComponentCount() - 1);

          if (c != null) {
            location.setLocation(c.getLocation().x + c.getWidth() - 1, c
                .getLocation().y
                + c.getHeight() - 1);
          }
        }

        int n = 0;
        
        if (c != null) {
          Point p = SwingUtilities.convertPoint(MainFrame.getInstance().getToolBarPanel(), location, c);

          n = MainFrame.getInstance().getToolbar().getComponentIndex(c);

          if (!((mWest && (p.y < c.getHeight() / 2)) || (!mWest && (p.x < c
              .getWidth() / 2)))) {
            n++;
          }
        }

        if (index != -1) {
          Action a = mCurrentActions.remove(index);

          if (index < n) {
            n--;
          }

          if (n > MainFrame.getInstance().getToolbar().getComponentCount() - 1) {
            mCurrentActions.insertElementAt(a, n - 1);
          } else {
            mCurrentActions.insertElementAt(a, n);
          }
        } else {
          if(n < 0) {
            n = 0;
          }
            
          for (Action a : mAvailableActions) {
            if (a.getValue(Action.NAME).equals(name)) {
              if (!separator.getValue(Action.NAME).equals(name)
                  && !glue.getValue(Action.NAME).equals(name)
                  && !space.getValue(Action.NAME).equals(name)) {
                mAvailableActions.removeElement(a);
              }
              mCurrentActions.insertElementAt(a, n);
              break;
            }
          }
        }
        
        saveSettings();
      }
      e.dropComplete(true);
    } catch (Exception ee) {ee.printStackTrace();
      e.dropComplete(false);
    }
    mCueLine.setRect(0,0,0,0);
  }

  public void dragGestureRecognized(DragGestureEvent e) {
    /* Start drag of an ActionButton */
    Action separator = DefaultToolBarModel.getInstance().getSeparatorAction();
    Action glue = DefaultToolBarModel.getInstance().getGlueAction();
    Action space = DefaultToolBarModel.getInstance().getSpaceAction();
    Action[] actions = DefaultToolBarModel.getInstance().getAvailableActions();

    JComponent c = (JComponent) e.getComponent();
    
    for (Action action : actions) {
      String text;
      if (c instanceof JToolBar.Separator) {
        text = (String) separator.getValue(Action.NAME);
      } else if(c instanceof JPanel) {
        text = (String) glue.getValue(Action.NAME);
      } else {
        if (((AbstractButton) c).getText() == null) {
          text = "notext";
        } else if (!((AbstractButton) c).getText().startsWith("<html>")) {
          text = ((AbstractButton) c).getText();
        } else {
          text = ((AbstractButton) c).getText().substring(37,
              ((AbstractButton) c).getText().length() - 13);
        }
      }
      if (action.getValue(Action.NAME).equals(text)
          || separator.getValue(Action.NAME).equals(text)
          || glue.getValue(Action.NAME).equals(text)
          || space.getValue(Action.NAME).equals(text)
          || text.equals("notext")) {
        c.setBackground(Color.WHITE);

        e.startDrag(null, new TransferAction(text, MainFrame.getInstance()
            .getToolbar().getComponentIndex(e.getComponent())));
        break;
      }
    }
  }

  /**
   * Save the new ToolBar settings
   */
  private void saveSettings() {
    int size = mCurrentActions.size();
    String[] ids = new String[size];
    for (int i = 0; i < size; i++) {
      Action action = mCurrentActions.elementAt(i);
      ids[i] = (String) action.getValue(ToolBar.ACTION_ID_KEY);
    }
    DefaultToolBarModel.getInstance().setButtonIds(ids);
    Settings.propToolbarButtons.setStringArray(ids);

    ToolBar toolbar = MainFrame.getInstance().getToolbar();
    int inx = mShowCB.getSelectedIndex();
    if (inx == 0) {
      toolbar.setStyle(ToolBar.STYLE_ICON | ToolBar.STYLE_TEXT);
    } else if (inx == 1) {
      toolbar.setStyle(ToolBar.STYLE_TEXT);
    } else if (inx == 2) {
      toolbar.setStyle(ToolBar.STYLE_ICON);
    }

    if (mLocationCB.getSelectedIndex() == 1) {
      toolbar.setToolbarLocation(BorderLayout.WEST);
      mWest = true;
    } else {
      toolbar.setToolbarLocation(BorderLayout.NORTH);
      mWest = false;
    }

    MainFrame.getInstance().setShowToolbar(mShowToolbarCb.isSelected());
    MainFrame.getInstance().setShowSearchField(mShowSearchFieldCb.isSelected());

    toolbar.setUseBigIcons(mUseBigIconsCb.isSelected());
    toolbar.storeSettings();
    MainFrame.getInstance().updateToolbar();

    buildButtonPanel();
    init();
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() instanceof JButton && e.getActionCommand().equals("OK")) {
      windowClosing(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    } else if (e.getSource() == mShowToolbarCb) {

      MainFrame.getInstance().setShowToolbar(mShowToolbarCb.isSelected());
      init();
    } else {
      saveSettings();
    }
  }

  public void dragOver(DropTargetDragEvent e) {
    /*
     * Paint the cue line to show the user where the ActionButton will be placed
     * in the ToolBar.
     */
    JComponent target = (JComponent) ((DropTarget) e.getSource())
        .getComponent();

    if (!target.equals(mButtonPanel)) {
      Point location = e.getLocation();

      if (mWest) {
        location.setLocation(10,
            location.y);
      } else {
        location.setLocation(location.x, MainFrame.getInstance().getToolbar()
            .getHeight() / 2);
      }

      JComponent c = (JComponent) MainFrame.getInstance().getToolbar()
          .getComponentAt(location);
      
      if ((c == null || c instanceof JToolBar) && MainFrame.getInstance().getToolbar().getComponentCount()>0) {
        c = (JComponent) MainFrame.getInstance().getToolbar().getComponent(
            MainFrame.getInstance().getToolbar().getComponentCount() - 1);

        if (c != null) {
          location.setLocation(c.getLocation().x + c.getWidth() - 1, c
              .getLocation().y
              + c.getHeight() - 1);
        }
      }
      
      if (c != null) {
        JPanel toolBarPanel = MainFrame.getInstance().getToolBarPanel();
        
        Point p = SwingUtilities.convertPoint(toolBarPanel, location, c);
        
        Rectangle oldCueLineBounds = mCueLine.getBounds();
        
        if (mWest) {
          mCueLine.setRect(1,
              (p.y < c.getHeight() / 2) ? (location.y - p.y) : (location.y
                  + c.getHeight() - p.y), toolBarPanel.getWidth() - 1, 2);
        } else {
          mCueLine.setRect((p.x < c.getWidth() / 2) ? (location.x - p.x )
              : (location.x + c.getWidth() - p.x ), 1, 2,
              toolBarPanel.getHeight() - 1);
        }

        if (!oldCueLineBounds.equals(mCueLine.getBounds())) {
          Graphics2D g2d = (Graphics2D) toolBarPanel.getGraphics();
          toolBarPanel.paintImmediately(oldCueLineBounds);

          Color color = new Color(255, 0, 0, 180);
          g2d.setColor(color);
          g2d.fill(mCueLine);

          g2d.dispose();
        }
      }
    }
  }

  public void windowOpened(WindowEvent e) {}

  public void windowClosed(WindowEvent e) {}

  public void windowIconified(WindowEvent e) {}

  public void windowDeiconified(WindowEvent e) {}

  public void windowActivated(WindowEvent e) {}

  public void windowDeactivated(WindowEvent e) {}

  public void dropActionChanged(DropTargetDragEvent e) {}

  public void dragExit(DropTargetEvent e) {
    MainFrame.getInstance().getToolBarPanel().paintImmediately(mCueLine.getBounds());
    mCueLine.setRect(0, 0, 0, 0);
  }

  public void mouseClicked(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
      int index = MainFrame.getInstance().getToolbar().getComponentIndex(
          e.getComponent());
      int n = MainFrame.getInstance().getToolbar().getComponentCount();
      Action separator = DefaultToolBarModel.getInstance().getSeparatorAction();
      Action glue = DefaultToolBarModel.getInstance().getGlueAction();
      Action space = DefaultToolBarModel.getInstance().getSpaceAction();

      if (index != -1) {
        Action a = mCurrentActions.elementAt(index);
        mCurrentActions.removeElementAt(index);
        if (!a.equals(separator) && !a.equals(glue) && !a.equals(space)) {
          mAvailableActions.addElement(a);
        }
      } else {
        String name = ((AbstractButton) e.getComponent()).getText().substring(
            37, ((AbstractButton) e.getComponent()).getText().length() - 13);

        for (int i = 0; i < mAvailableActions.size(); i++) {
          Action a = mAvailableActions.elementAt(i);
          if (a.getValue(Action.NAME).equals(name)) {
            if (!separator.getValue(Action.NAME).equals(name)
                && !glue.getValue(Action.NAME).equals(name)
                && !space.getValue(Action.NAME).equals(name)) {
              mAvailableActions.removeElement(a);
            }
            mCurrentActions.insertElementAt(a, n);
          }
        }
      }
      saveSettings();
    }
  }

  public void mousePressed(MouseEvent e) {}

  public void mouseReleased(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void close() {
    mInstance = null;
    setMainframeMenusEnabled(true);
    MainFrame.getInstance().updateToolbar();
    dispose();
  }
}
