package tvbrowser.ui.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.CompoundBorder;
import devplugin.Plugin;

import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.toolbar.ContextMenu;
import tvbrowser.ui.mainframe.toolbar.DefaultToolBarModel;
import tvbrowser.ui.mainframe.toolbar.ToolBar;

/**
 * A class to support Drag'n'Drop in the toolbar.
 * 
 * @author René Mach
 */
public class ToolBarDragAndDropSettings extends JFrame implements
    WindowListener, MouseMotionListener,
    DragGestureListener, DropTargetListener, ActionListener {

  private static final long serialVersionUID = 1L;
  /**Actions that are visible in the ToolBar*/
  private Vector mCurrentActions = new Vector();
  /**Actions the user can add to the ToolBar*/
  private Vector mAvailableActions = new Vector();
  private JComboBox mShowCB, mLocationCB;
  private JCheckBox mShowToolbarCb, mUseBigIconsCb;
  private JPanel mButtonPanel;
  private boolean mWest;
  
  private static ToolBarDragAndDropSettings mInstance = null;

  /**
   * The default constructor.
   * 
   */
  public ToolBarDragAndDropSettings() {
    mInstance = this;
    
    // Set this window to AlwaysOnTop if
    // the JVM has the needed method.
    Class c = this.getClass();
    Method[] m = c.getMethods();
    for(int i = 0; i < m.length; i++) {
      if(m[i].getName().equals("setAlwaysOnTop")) {
        boolean bb = true;
        Boolean B = new Boolean(bb);
        
        Object[] b = {B}; 
        try {
        m[i].invoke(this,b);
        }catch(Exception ee){}
        break;
      }
    }

    this.getContentPane().setLayout(
        new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
    ((JPanel) this.getContentPane()).setBorder(BorderFactory.createEmptyBorder(
        0, 6, 2, 6));
     
    // Initalize the buttonPanel and fill the vectors with the actions.
    final DefaultToolBarModel toolbarModel = DefaultToolBarModel.getInstance();
    Action[] currentActions = toolbarModel.getActions();

    ArrayList notSelectedActionsList = new ArrayList(Arrays.asList(toolbarModel
        .getAvailableActions()));

    for (int i = 0; i < currentActions.length; i++)
      mCurrentActions.addElement(currentActions[i]);

    for (int i = 0; i < currentActions.length; i++)
      if (notSelectedActionsList.contains(currentActions[i]))
        notSelectedActionsList.remove(currentActions[i]);

    Action[] availableActions = new Action[notSelectedActionsList.size()];
    notSelectedActionsList.toArray(availableActions);

    for (int i = 0; i < availableActions.length; i++)
      mAvailableActions.addElement(availableActions[i]);

    mAvailableActions.insertElementAt(toolbarModel.getSeparatorAction(), 0);

      // Initialize the Panel with the available Buttons
    mButtonPanel = new JPanel();
    mButtonPanel.setLayout(new GridLayout(0, 4, 2, 2));
    mButtonPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

      // Make the buttonPanel scrollable
    JScrollPane pane = new JScrollPane(mButtonPanel);
    pane.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
    pane.getVerticalScrollBar().setUnitIncrement(73);
    pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
      // Initialize the Panel for selecting toolBars visibility
    JPanel tVisPanel = new JPanel();
    tVisPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    tVisPanel.setLayout(new BoxLayout(tVisPanel, BoxLayout.X_AXIS));
    tVisPanel.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
        Color.GRAY), BorderFactory.createEmptyBorder(10, 5, 9, 5)));
    
    mShowToolbarCb = new JCheckBox(ToolbarSettingsTab.mLocalizer.msg(
        "showToolbar", "Show toolbar"));
    mShowToolbarCb.setSelected(Settings.propIsTooolbarVisible.getBoolean());    

    tVisPanel.add(mShowToolbarCb);
    tVisPanel.add(Box.createHorizontalGlue());
   
      // Initialize the panel for the ToolBar settings
    JPanel tSetPanel = new JPanel();
    tSetPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    tSetPanel.setLayout(new BoxLayout(tSetPanel, BoxLayout.X_AXIS));
    
    mLocationCB = new JComboBox(new String[] {
        ToolbarSettingsTab.mLocalizer.msg("top", "top"),
        ToolbarSettingsTab.mLocalizer.msg("left", "left"), });
    mLocationCB.setMaximumSize(mLocationCB.getPreferredSize());
    
    if ("west".equals(Settings.propToolbarLocation.getString())) {
      mLocationCB.setSelectedIndex(1);
      mWest = true;
    } else
      mWest = false;   

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
    mShowCB.setAlignmentX(JComboBox.CENTER_ALIGNMENT);
    mShowCB.setMaximumSize(mShowCB.getPreferredSize());

    mUseBigIconsCb = new JCheckBox(ContextMenu.mLocalizer.msg("bigIcons","Use big icons"));
    mUseBigIconsCb.setSelected(Settings.propToolbarUseBigIcons.getBoolean());
    
      // add the components to the settingsPanel
    tSetPanel.add(new JLabel(ToolbarSettingsTab.mLocalizer.msg("location","Location")));
    tSetPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    tSetPanel.add(mLocationCB);
    tSetPanel.add(Box.createHorizontalGlue());
    tSetPanel.add(new JLabel(ToolbarSettingsTab.mLocalizer.msg("icons", "Icons")));
    tSetPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    tSetPanel.add(mShowCB);
    tSetPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    tSetPanel.add(mUseBigIconsCb);

    tSetPanel.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(1, 0, 1, 0,
        Color.GRAY), BorderFactory.createEmptyBorder(10, 5, 9, 5)));
    
      // The panel for the OK button
    JPanel okButtonPanel = new JPanel();
    okButtonPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    okButtonPanel.setLayout(new BoxLayout(okButtonPanel, BoxLayout.X_AXIS));
    okButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
    
    JButton ok = new JButton("OK");
    ok.addActionListener(this);

    okButtonPanel.add(Box.createHorizontalGlue());
    okButtonPanel.add(ok);
        
      // Register ActionListener to the settings
    mShowToolbarCb.addActionListener(this);
    mShowCB.addActionListener(this);
    mUseBigIconsCb.addActionListener(this);
    mLocationCB.addActionListener(this);

      // Add the components to the window
    this.getContentPane().add(tVisPanel);    
    this.getContentPane().add(pane);
    this.getContentPane().add(tSetPanel);
    this.getContentPane().add(okButtonPanel);

      // Set up the windows attributes
    this.setSize(630, 400);
    this.addWindowListener(this);
    this.setTitle("Werkzeugleiste anpassen");
    this.setLocationRelativeTo(MainFrame.getInstance());
    this.setVisible(true);
    
    buildButtonPanel();    
    ini();
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
    if (this.mAvailableActions.size() % 4 != 0)
      n++;
    mButtonPanel.setPreferredSize(new Dimension(570, n * 73));

     // Add all availableActions to the buttonPanel
    for (int i = 0; i < this.mAvailableActions.size(); i++) {
      Action a = (Action) this.mAvailableActions.elementAt(i);

        // <html> is needed to have a black color of the letters of a button,
        // because the Buttons have to be disabled for Drag'n'Drop
      JButton b = new JButton("<html>" + (String) a.getValue(Action.NAME)
          + "</html>");
      b.setBorder(new CompoundBorder(BorderFactory
          .createEmptyBorder(1, 1, 1, 1), BorderFactory.createEmptyBorder(1, 1,
          1, 1)));
      b.setIcon((Icon) a.getValue(Plugin.BIG_ICON));
      b.setDisabledIcon((Icon) a.getValue(Plugin.BIG_ICON));
      b.setVerticalTextPosition(JButton.BOTTOM);
      b.setHorizontalTextPosition(JButton.CENTER);
      b.setFont(new Font("Dialog", Font.PLAIN, 10));
      
        // Set up the available ActionButtons for dragging
      DragSource d = new DragSource();
      d.createDefaultDragGestureRecognizer(b, DnDConstants.ACTION_MOVE, this);

      b.setEnabled(false);
      mButtonPanel.add(b);
    }
    mButtonPanel.updateUI();
  }

  private void ini() {
     // set the drop targets of the Actions
    new DropTarget(MainFrame.getInstance().getToolbar(), this);
    new DropTarget(mButtonPanel, this);

     // set up the ActionButtons in the ToolBar for dragging
    MainFrame.getInstance().getToolbar().disableForDragAndDrop(this,
        mWest);
  }

    /**
     * The class to save the action in that is transfered
     * with Drag'n'Drop.
     * 
     * @author René Mach
     */
  class TransferAction implements Transferable {
     /**The Action.NAME*/
    private String mName;
    
     /**The index of a ActionButton in the ToolBar*/
    private Integer mIndex;
    
     /**The DataFlavors to recognize this as an
      * ActionButton.
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
      mIndex = new Integer(index);
      mIF = new DataFlavor(Integer.class, "Integer");
    }

    public DataFlavor[] getTransferDataFlavors() {
      DataFlavor[] f = { mNF, mIF };
      return (f);
    }

    public boolean isDataFlavorSupported(DataFlavor e) {
      if (e.equals(mNF) || e.equals(mIF))
        return true;
      else
        return false;
    }

    public Object getTransferData(DataFlavor e)
        throws UnsupportedFlavorException, IOException {
      if (e.equals(mNF))
        return mName;
      else if (e.equals(mIF))
        return mIndex;
      else
        return null;
    }
  }

  public void windowClosing(WindowEvent e) {
    MainFrame.getInstance().updateToolbar();
    mInstance = null;
    this.dispose();
  }

  public void mouseMoved(MouseEvent e) {
    /* Paints the border of an ActionButton
     * if the Mouse is over it.*/
    
    int n = MainFrame.getInstance().getToolbar().getComponentIndex(
        e.getComponent());

    for (int i = 0; i < MainFrame.getInstance().getToolbar()
        .getComponentCount(); i++) {
      if (mWest)
        ((JComponent) MainFrame.getInstance().getToolbar().getComponent(i))
            .setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(1, 0,
                0, 0), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
      else
        ((JComponent) MainFrame.getInstance().getToolbar().getComponent(i))
            .setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 1,
                0, 0), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
    }
    if (!mWest)
    ((JComponent) MainFrame.getInstance().getToolbar().getComponent(n))
        .setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 1, 0,
            0), BorderFactory.createLineBorder(Color.BLACK)));
    else
      ((JComponent) MainFrame.getInstance().getToolbar().getComponent(n))
      .setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(1, 0, 0,
          0), BorderFactory.createLineBorder(Color.BLACK)));
  }

  public void dragEnter(DropTargetDragEvent e) {
    /* Start drag only if the drag source has
     * the correct DataFlavors.
     * */
    DataFlavor[] flavors = e.getCurrentDataFlavors();
    if (flavors.length < 2) {
      e.rejectDrag();
      return;
    }
    if (flavors[0].getHumanPresentableName().equals("Action")
        && flavors[1].getHumanPresentableName().equals("Integer"))
      e.acceptDrag(e.getDropAction());
    else      
      e.rejectDrag();
  }

  public void drop(DropTargetDropEvent e) {
    
    // Drop the TransferAction
    e.acceptDrop(e.getDropAction());
    Transferable tr = e.getTransferable();
    DataFlavor[] flavors = tr.getTransferDataFlavors();    
    try {
      String name = (String) tr.getTransferData(flavors[0]);
      int index = ((Integer) tr.getTransferData(flavors[1])).intValue();
      Action s = DefaultToolBarModel.getInstance().getSeparatorAction();

      if (((DropTarget) e.getSource()).getComponent().equals(mButtonPanel)) {
        if (index == -1 && !s.getValue(Action.NAME).equals(name)) {
          for (int i = 0; i < mCurrentActions.size(); i++) {
            Action a = (Action) mCurrentActions.elementAt(i);
            if (a.getValue(Action.NAME).equals(name)) {
              mCurrentActions.removeElement(a);
              mAvailableActions.addElement(a);
            }
          }
        } else if (index != -1){
          Action a = (Action) mCurrentActions.elementAt(index);
          mCurrentActions.removeElementAt(index);
          if (!s.getValue(Action.NAME).equals(name))
            mAvailableActions.addElement(a);
        }
        saveSettings();
      } else if (((DropTarget) e.getSource()).getComponent().equals(
          MainFrame.getInstance().getToolbar())) {
        Point p = e.getLocation();
        if (!mWest)
          p.setLocation(p.x,
              MainFrame.getInstance().getToolbar().getHeight() / 2);
        else
          p.setLocation(10, p.y);

        int n = MainFrame.getInstance().getToolbar().getComponentIndex(
            MainFrame.getInstance().getToolbar()
                .getComponentAt(e.getLocation()));

        if (index < n && index != -1)
          n--;
        if (n == -1) {
          if (!mWest) {
            int width = MainFrame.getInstance().getToolbar().getWidth();
            while (p.x < width) {
              p.setLocation(p.x + 1, p.y);
              n = MainFrame.getInstance().getToolbar().getComponentIndex(
                  MainFrame.getInstance().getToolbar().getComponentAt(
                      e.getLocation()));
              if (n != -1)
                break;
            }
            if (p.x >= width) {
              n = MainFrame.getInstance().getToolbar().getComponentCount();
            }
          } else {
            int height = MainFrame.getInstance().getToolbar().getHeight();
            while (p.y < height) {
              p.setLocation(p.x, p.y + 1);
              n = MainFrame.getInstance().getToolbar().getComponentIndex(
                  MainFrame.getInstance().getToolbar().getComponentAt(
                      e.getLocation()));
              if (n != -1)
                break;
            }
            if (p.y >= height) {
              n = MainFrame.getInstance().getToolbar().getComponentCount();
            }
          }
        }
        if (index != -1) {
          Action a = (Action) mCurrentActions.elementAt(index);
          mCurrentActions.removeElementAt(index);
          if (n > MainFrame.getInstance().getToolbar().getComponentCount() - 1)
            mCurrentActions.insertElementAt(a, n - 1);
          else
            mCurrentActions.insertElementAt(a, n);
        } else
          for (int i = 0; i < mAvailableActions.size(); i++) {
            Action a = (Action) mAvailableActions.elementAt(i);
            if (a.getValue(Action.NAME).equals(name)) {
              if (!s.getValue(Action.NAME).equals(name))
                mAvailableActions.removeElement(a);
              mCurrentActions.insertElementAt(a, n);
            }
          }
        saveSettings();
      }
    } catch (Exception ee) {
      ee.printStackTrace();
    }
  }

  public void dragGestureRecognized(DragGestureEvent e) {
    
    /* Start drag of an ActionButton*/
    Action s = DefaultToolBarModel.getInstance().getSeparatorAction();
    Action[] a = DefaultToolBarModel.getInstance().getAvailableActions();
    for (int i = 0; i < a.length; i++) {
      String text;
      if (e.getComponent() instanceof JToolBar.Separator)
        text = (String) s.getValue(Action.NAME);
      else {
        if (((AbstractButton) e.getComponent()).getText() == null)
          text = "notext";
        else if (!((AbstractButton) e.getComponent()).getText().startsWith(
            "<html>"))
          text = ((AbstractButton) e.getComponent()).getText();
        else
          text = ((AbstractButton) e.getComponent()).getText().substring(6,
              ((AbstractButton) e.getComponent()).getText().length() - 7);
      }
      if (a[i].getValue(Action.NAME).equals(text)
          || s.getValue(Action.NAME).equals(text) || text.equals("notext")) {
        ((JComponent) e.getComponent()).setBackground(Color.WHITE);

        if (mWest)
          ((JComponent) e.getComponent()).setBorder(new CompoundBorder(
              BorderFactory.createEmptyBorder(1, 0, 0, 0), BorderFactory
                  .createEmptyBorder(1, 1, 1, 1)));
        else
          ((JComponent) e.getComponent()).setBorder(new CompoundBorder(
              BorderFactory.createEmptyBorder(0, 1, 0, 0), BorderFactory
                  .createEmptyBorder(1, 1, 1, 1)));
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
      Action action = (Action) mCurrentActions.elementAt(i);
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

    Settings.propIsTooolbarVisible.setBoolean(mShowToolbarCb.isSelected());

    toolbar.setUseBigIcons(mUseBigIconsCb.isSelected());
    toolbar.storeSettings();
    MainFrame.getInstance().updateToolbar();

    buildButtonPanel();
    ini();
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getSource() instanceof JButton && e.getActionCommand().equals("OK")) {
      windowClosing(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
    else
      saveSettings();
  }
  
  public void dragOver(DropTargetDragEvent e) {
    /* Paint the border to show the user where
     * the ActionButton will be placed in the ToolBar.*/
    Point p = e.getLocation();
    
    if (((DropTarget) e.getSource()).getComponent() instanceof JPanel) {
      for (int i = 0; i < MainFrame.getInstance().getToolbar()
          .getComponentCount(); i++) {
        if (mWest)
          ((JComponent) MainFrame.getInstance().getToolbar().getComponent(i))
              .setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(1,
                  0, 0, 0), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        else
          ((JComponent) MainFrame.getInstance().getToolbar().getComponent(i))
              .setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0,
                  1, 0, 0), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
      }
      return;
    }

    if (!mWest)
      p.setLocation(p.x, MainFrame.getInstance().getToolbar().getHeight() / 2);
    else
      p.setLocation(10, p.y);

    int n = MainFrame.getInstance().getToolbar().getComponentIndex(
        MainFrame.getInstance().getToolbar().getComponentAt(e.getLocation()));
    boolean greater = false;

    if (n == -1) {
      if (!mWest) {
        int width = MainFrame.getInstance().getToolbar().getWidth();
        while (p.x < width) {
          p.setLocation(p.x + 1, p.y);
          n = MainFrame.getInstance().getToolbar().getComponentIndex(
              MainFrame.getInstance().getToolbar().getComponentAt(
                  e.getLocation()));
          if (n != -1)
            break;
        }
        if (p.x >= width) {
          n = MainFrame.getInstance().getToolbar().getComponentCount() - 1;
          greater = true;
        }
      } else {
        int height = MainFrame.getInstance().getToolbar().getHeight();
        while (p.y < height) {
          p.setLocation(p.x, p.y + 1);
          n = MainFrame.getInstance().getToolbar().getComponentIndex(
              MainFrame.getInstance().getToolbar().getComponentAt(
                  e.getLocation()));
          if (n != -1)
            break;
        }
        if (p.y >= height) {
          n = MainFrame.getInstance().getToolbar().getComponentCount() - 1;
          greater = true;
        }
      }
    }
    if(n == -1)
      return;
    
    JComponent c = (JComponent) MainFrame.getInstance().getToolbar()
        .getComponent(n);

    for (int i = 0; i < MainFrame.getInstance().getToolbar()
        .getComponentCount(); i++) {
      if (i == n && c instanceof JToolBar.Separator) {
        if (mWest && !greater)
          c.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(1, 0,
              0, 0, Color.BLACK), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        else if (!mWest && !greater)
          c.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 1,
              0, 0, Color.BLACK), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        else if (mWest && greater)
          c.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 0,
              1, 0, Color.BLACK), BorderFactory.createEmptyBorder(2, 1, 1, 1)));
        else
          c.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 0,
              0, 1, Color.BLACK), BorderFactory.createEmptyBorder(1, 2, 1, 1)));
      } else if (i == n && c instanceof AbstractButton) {
        AbstractButton b = (AbstractButton) c;
        if (mWest && !greater)
          b.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(1, 0,
              0, 0, Color.BLACK), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        else if (!mWest && !greater)
          b.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 1,
              0, 0, Color.BLACK), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        else if (mWest && greater)
          b.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 0,
              1, 0, Color.BLACK), BorderFactory.createEmptyBorder(2, 1, 1, 1)));
        else
          b.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 0,
              0, 1, Color.BLACK), BorderFactory.createEmptyBorder(1, 2, 1, 1)));
        b.setBorderPainted(true);
      } else if (mWest)
        ((JComponent) MainFrame.getInstance().getToolbar().getComponent(i))
            .setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(1, 0,
                0, 0), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
      else
        ((JComponent) MainFrame.getInstance().getToolbar().getComponent(i))
            .setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 1,
                0, 0), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
    }

  }

  public void mouseDragged(MouseEvent e) {}
  public void windowOpened(WindowEvent e) {}
  public void windowClosed(WindowEvent e) {}
  public void windowIconified(WindowEvent e) {}
  public void windowDeiconified(WindowEvent e) {}
  public void windowActivated(WindowEvent e) {}
  public void windowDeactivated(WindowEvent e) {}
  public void dropActionChanged(DropTargetDragEvent e) {}
  public void dragExit(DropTargetEvent e) {}
}
