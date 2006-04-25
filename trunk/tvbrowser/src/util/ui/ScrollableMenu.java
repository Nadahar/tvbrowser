/**
 * This class was found in the Thread
 * http://forum.java.sun.com/thread.jspa?forumID=57&threadID=123183
 * 
 * I tried to contact the Author, without any luck. If you are the Author and
 * don't like the Usage of your Code in this Project or want to be named, please
 * mail us!
 */
package util.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicPopupMenuUI;

// This class implements a scrollable JMenu
// This class was hacked out in a couple of hours,
// You can change maxItemsToDisplay to whatever you want, I used 25 and reduced
// it to fit into the screen
// I have NOT tested this class very much to see if it loses items, etc, ***USE
// AT YOUR OWN RISK***
// Feel free to use and modify (Please add bug fixes here).
//
// This class should only be used until SUN makes a real scrollable JMenu

/**
 * An implementation of a scrollable menu -- a popup window containing
 * <code>JMenuItem</code>s that is displayed when the user selects an item on
 * the <code>JMenuBar</code>. In addition to <code>JMenuItem</code>s, a
 * <code>JMenu</code> can also contain <code>JSeparator</code>s.
 * <p>
 * In essence, a menu is a button with an associated <code>JPopupMenu</code>.
 * When the "button" is pressed, the <code>JPopupMenu</code> appears. If the
 * "button" is on the <code>JMenuBar</code>, the menu is a top-level window.
 * If the "button" is another menu item, then the <code>JPopupMenu</code> is
 * "pull-right" menu.
 * 
 * If the menu contains more items than displayable on the screen the menu
 * becomes scrollable by hiding some of the items and adding an add and a down
 * arrow at both ends of the menu to scroll the menu with this arrows.
 * 
 * description: A popup window containing menu items displayed in a menu bar.
 * 
 * @see JPopupMenu
 */
public class ScrollableMenu extends JMenu {

  private static int maxItemsToDisplay = 35;

  private static boolean DOWN = true;

  private static boolean UP = false;

  static {
    // set max items count visible on screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    JMenuItem dummyItem = new JMenuItem("test");
    Dimension itemDim = dummyItem.getPreferredSize();
    maxItemsToDisplay = Math.min(maxItemsToDisplay, (int) (((4.0 / 5.0) * dim.height) / itemDim.height));

    // put a wrapper action between up and down selection action to scroll up or
    // down
    JPopupMenu dummy = new JPopupMenu();
    BasicPopupMenuUI ui = (BasicPopupMenuUI) BasicPopupMenuUI.createUI(dummy);
    ui.installUI(dummy); // create action map

    ActionMap map = (ActionMap) UIManager.getLookAndFeelDefaults().get("PopupMenu.actionMap");

    if (map != null) {
      Action downAction = (Action) map.get("selectNext");
      Action upAction = (Action) map.get("selectPrevious");

      map.put("selectNext", new SelectNextItemAction(DOWN, downAction));
      map.put("selectPrevious", new SelectNextItemAction(UP, upAction));
    }
  }

  private static class SelectNextItemAction extends AbstractAction {
    private boolean direction;

    private Action wrappedAction;

    SelectNextItemAction(boolean direction, Action wrappedAction) {
      this.direction = direction;
      this.wrappedAction = wrappedAction;
    }

    public void actionPerformed(ActionEvent e) {

      MenuSelectionManager msm = MenuSelectionManager.defaultManager();
      MenuElement path[] = msm.getSelectedPath();
      int len = path.length;

      if (len > 2 && path[len - 3] instanceof ScrollableMenu && path[len - 2] instanceof JPopupMenu) {

        ScrollableMenu menu = (ScrollableMenu) path[len - 3];
        MenuElement selected = path[len - 1];
        Component component = null;

        component = menu.getFirstVisibleAndEnabledComponent();
        if (direction == UP && (component == null || selected == component)) {
          if (menu.scrollUp.enableScroll) {
            // scroll up
            do {
              menu.scrollUpClicked();
              component = menu.getFirstVisibleComponent();
            } while (component != null && (!(component instanceof MenuElement)) && (component instanceof JSeparator)
                && menu.scrollUp.enableScroll);

            if (!component.isEnabled() || (!(component instanceof MenuElement))) {
              return;
            }
          } else {
            // very first - scroll to end
            for (int index = 0; index < menu.getMenuComponentCount(); index++) {
              menu.scrollDownClicked();
            }
          }
        } else if (direction == DOWN
            && (((component = menu.getLastVisibleAndEnabledComponent()) == null) || selected == component)) {
          if (menu.scrollDown.enableScroll) {
            // scroll down
            do {
              menu.scrollDownClicked();
              component = menu.getLastVisibleComponent();
            } while (component != null && (!(component instanceof MenuElement)) && (component instanceof JSeparator)
                && menu.scrollDown.enableScroll);

            if (!component.isEnabled() || (!(component instanceof MenuElement))) {
              return;
            }
          } else {
            // very last - scroll to begin
            for (int index = 0; index < menu.getMenuComponentCount(); index++) {
              menu.scrollUpClicked();
            }
          }
        }
      }

      wrappedAction.actionPerformed(e);
    }
  }

  private ScrollUpOrDownButtonItem scrollUp = new ScrollUpOrDownButtonItem(UP);

  private ScrollUpOrDownButtonItem scrollDown = new ScrollUpOrDownButtonItem(DOWN);

  private JSeparator upSeperator = new JSeparator();

  private JSeparator downSeperator = new JSeparator();

  private Vector scrollableItems = new Vector();

  private int beginIndex = 0;

  private int maxWidth = 10;

  /**
   * Constructs a new <code>JMenu</code> with no text.
   */
  public ScrollableMenu() {
    this("");
  }

  /**
   * Constructs a new <code>JMenu</code> whose properties are taken from the
   * <code>Action</code> supplied.
   * 
   * @param a an <code>Action</code>
   * 
   * @since 1.3
   */
  public ScrollableMenu(Action a) {
    this("");
    setAction(a);
  }

  /**
   * Constructs a new <code>JMenu</code> with the supplied string as its text
   * and specified as a tear-off menu or not.
   * 
   * @param s the text for the menu label
   * @param b can the menu be torn off (not yet implemented)
   */
  public ScrollableMenu(String s, boolean b) {
    this(s);
  }

  /**
   * Constructs a new <code>JMenu</code> with the supplied string as its text.
   * 
   * @param menuTitle the text for the menu label
   */
  public ScrollableMenu(String menuTitle) {
    super(menuTitle);

    super.add(scrollUp);
    super.add(upSeperator);
    super.add(downSeperator);
    super.add(scrollDown);
  }

  /**
   * Appends a menu item to the end of this menu. Returns the menu item added.
   * 
   * @param menuItem the <code>JMenuitem</code> to be added
   * @return the <code>JMenuItem</code> added
   */
  public JMenuItem add(JMenuItem menuItem) {
    addScrollableComponent(menuItem);
    return menuItem;
  }

  /**
   * Appends a component to the end of this menu. Returns the component added.
   * 
   * @param component the <code>Component</code> to add
   * @return the <code>Component</code> added
   */
  public Component add(Component component) {
    addScrollableComponent(component);
    return component;
  }

  /**
   * Adds the specified component to this container at the given position. If
   * <code>index</code> equals -1, the component will be appended to the end.
   * 
   * @param component the <code>Component</code> to add
   * @param index the position at which to insert the component
   * @return the <code>Component</code> added
   * @see #remove
   * @see java.awt.Container#add(Component, int)
   */
  public Component add(Component component, int index) {
    addScrollableComponent(component, index);
    return component;
  }

  public void insert(String s, int pos) {
    if (pos < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }

    insert(new JMenuItem(s), pos);

  }

  /**
   * Inserts the specified <code>JMenuitem</code> at a given position.
   * 
   * @param menuItem the <code>JMenuitem</code> to add
   * @param pos an integer specifying the position at which to add the new
   *          <code>JMenuitem</code>
   * @return the new menu item
   * @exception IllegalArgumentException if the value of <code>pos</code> < 0
   */
  public JMenuItem insert(JMenuItem menuItem, int pos) {
    if (pos < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }

    addScrollableComponent(menuItem, pos);
    return menuItem;
  }

  /**
   * Inserts a new menu item attached to the specified <code>Action</code>
   * object at a given position.
   * 
   * @param a the <code>Action</code> object for the menu item to add
   * @param pos an integer specifying the position at which to add the new menu
   *          item
   * @exception IllegalArgumentException if the value of <code>pos</code> < 0
   */
  public JMenuItem insert(Action a, int pos) {
    if (pos < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }

    JMenuItem menuItem = new JMenuItem((String) a.getValue(Action.NAME), (Icon) a.getValue(Action.SMALL_ICON));
    menuItem.setHorizontalTextPosition(JButton.TRAILING);
    menuItem.setVerticalTextPosition(JButton.CENTER);
    menuItem.setEnabled(a.isEnabled());
    menuItem.setAction(a);
    insert(menuItem, pos);

    return menuItem;
  }

  /**
   * Returns the <code>JMenuItem</code> at the specified position. If the
   * component at <code>pos</code> is not a menu item, <code>null</code> is
   * returned. This method is included for AWT compatibility.
   * 
   * @param pos an integer specifying the position
   * @exception IllegalArgumentException if the value of <code>pos</code> < 0
   * @return the menu item at the specified position; or <code>null</code> if
   *         the item as the specified position is not a menu item
   */
  public JMenuItem getItem(int pos) {
    if (pos < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }

    JMenuItem menuItem = null;

    Component component = getMenuComponent(pos);
    if (component instanceof JMenuItem) {
      menuItem = (JMenuItem) component;
    }

    return menuItem;
  }

  /**
   * Returns the number of items on the menu, including separators. This method
   * is included for AWT compatibility.
   * 
   * @return an integer equal to the number of items on the menu
   * @see #getMenuComponentCount
   */
  public int getItemCount() {
    return getMenuComponentCount();
  }

  /**
   * Removes the specified menu item from this menu. If there is no popup menu,
   * this method will have no effect.
   * 
   * @param menuItem the <code>JMenuItem</code> to be removed from the menu
   */
  public void remove(JMenuItem menuItem) {
    removeScrollableComponent(menuItem);
  }

  /**
   * Removes the menu item at the specified index from this menu.
   * 
   * @param pos the position of the item to be removed
   * @exception IllegalArgumentException if the value of <code>pos</code> < 0,
   *              or if <code>pos</code> is greater than the number of menu
   *              items
   */
  public void remove(int pos) {
    if (pos < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }
    if (pos > getItemCount()) {
      throw new IllegalArgumentException("index greater than the number of items.");
    }
    removeScrollableComponent((Component) scrollableItems.elementAt(pos));
  }

  /**
   * Removes the component <code>c</code> from this menu.
   * 
   * @param component the component to be removed
   */
  public void remove(Component component) {
    removeScrollableComponent(component);
  }

  /**
   * Removes all menu items from this menu.
   */
  public void removeAll() {
    while (getMenuComponentCount() > 0) {
      remove(0);
    }
  }

  /**
   * Returns the number of components on the menu.
   * 
   * @return an integer containing the number of components on the menu
   */
  public int getMenuComponentCount() {
    return scrollableItems.size();
  }

  /**
   * Returns the component at position <code>n</code>.
   * 
   * @param n the position of the component to be returned
   * @return the component requested, or <code>null</code> if there is no
   *         popup menu
   * 
   */
  public Component getMenuComponent(int n) {
    if (n >= 0 && n < scrollableItems.size()) {
      return (Component) scrollableItems.elementAt(n);
    }
    return null;
  }

  /**
   * Returns an array of <code>Component</code>s of the menu's subcomponents.
   * Note that this returns all <code>Component</code>s in the popup menu,
   * including separators.
   * 
   * @return an array of <code>Component</code>s or an empty array if there
   *         is no popup menu
   */
  public Component[] getMenuComponents() {
    Component[] components = new Component[getMenuComponentCount()];
    Iterator iterator = scrollableItems.iterator();
    int index = 0;
    while (iterator.hasNext()) {
      components[index++] = (Component) iterator.next();
    }
    return components;
  }

  /**
   * Returns true if the specified component exists in the submenu hierarchy.
   * 
   * @param component the <code>Component</code> to be tested
   * @return true if the <code>Component</code> exists, false otherwise
   */
  public boolean isMenuComponent(Component component) {
    return scrollableItems.contains(component);
  }

  /**
   * Appends a new separator to the end of the menu.
   */
  public void addSeparator() {
    add(new JPopupMenu.Separator());
  }

  /**
   * Add the specified component to this scrollable menu
   * 
   * @param component the <code>Component</code> to add
   * @param pos an integer specifying the position at which to add the new
   *          component
   */
  protected void addScrollableComponent(Component component, int pos) {

    if (pos < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }

    scrollableItems.insertElementAt(component, pos);

    if (pos >= beginIndex && pos < beginIndex + maxItemsToDisplay) {
      super.add(component, pos - beginIndex + 2);
    }

    if (super.getMenuComponentCount() > maxItemsToDisplay + 4) {
      Component comp = getMenuComponent(beginIndex + maxItemsToDisplay);
      super.remove(comp);
    }

    setPreferedSizeForMenuItems(component);
    updateScrollingComponentsVisibility();
  }

  /**
   * Add the specified component at the end of this scrollable menu
   * 
   * @param component the <code>Component</code> to add
   */
  protected void addScrollableComponent(Component component) {
    addScrollableComponent(component, scrollableItems.size());
  }

  /**
   * Remove the specified component from this scrollable menu
   * 
   * @param component the <code>Component</code> to remove
   */
  protected void removeScrollableComponent(Component component) {

    scrollableItems.remove(component);
    super.remove(component);

    if (scrollableItems.size() > maxItemsToDisplay && super.getMenuComponentCount() - 4 < maxItemsToDisplay) {

      if (beginIndex + maxItemsToDisplay <= scrollableItems.size()) {
        int end = beginIndex + maxItemsToDisplay - 1;
        Component addComponent = (Component) scrollableItems.elementAt(end);

        super.add(addComponent, maxItemsToDisplay + 1);
      } else if (beginIndex > 0 && beginIndex <= scrollableItems.size()) {

        Component addComponent = (Component) scrollableItems.elementAt(--beginIndex);

        super.add(addComponent, 2);
      }
    } else if (beginIndex > 0 && beginIndex + maxItemsToDisplay > scrollableItems.size()) {
      beginIndex--;
    }

    updateScrollingComponentsVisibility();
  }

  private Component getFirstVisibleAndEnabledComponent() {
    if (super.getMenuComponentCount() > 4) {
      for (int index = 2; index < super.getMenuComponentCount() - 2; index++) {
        Component component = super.getMenuComponent(index);
        if (component instanceof MenuElement && component.isEnabled())
          return component;
      }
    }
    return null;
  }

  private Component getLastVisibleAndEnabledComponent() {
    if (super.getMenuComponentCount() > 4) {
      for (int index = super.getMenuComponentCount() - 3; index > 1; index--) {
        Component component = super.getMenuComponent(index);

        if (component instanceof MenuElement && component.isEnabled())
          return component;
      }
    }
    return null;
  }

  private Component getFirstVisibleComponent() {
    if (super.getMenuComponentCount() > 4) {
      return super.getMenuComponent(2);
    }
    return null;
  }

  private Component getLastVisibleComponent() {
    if (super.getMenuComponentCount() > 4) {
      return super.getMenuComponent(super.getMenuComponentCount() - 3);
    }
    return null;
  }

  private void updateScrollingComponentsVisibility() {
    boolean visible = scrollableItems.size() > maxItemsToDisplay;
    scrollDown.setVisible(visible);
    scrollUp.setVisible(visible);
    upSeperator.setVisible(visible);
    downSeperator.setVisible(visible);

    if (visible) {
      scrollUp.enableScroll(beginIndex > 0);
      scrollDown.enableScroll(beginIndex + maxItemsToDisplay < scrollableItems.size());
    }

    getPopupMenu().validate();
    getPopupMenu().pack();
  }

  private void setPreferedSizeForMenuItems(Component component) {
    FontRenderContext fontrendercontext = new FontRenderContext(null, false, false);
    if (component instanceof JComponent) {
      JComponent jcomp = (JComponent) component;

      int width = jcomp.getPreferredSize().width;
      if (jcomp.getBorder() != null) {
        Insets insets = jcomp.getBorder().getBorderInsets(component);
        width += insets.left + insets.right;
      }
      if (width > maxWidth) {
        maxWidth = width;
        Iterator iterator = scrollableItems.iterator();
        while (iterator.hasNext()) {
          Object object = iterator.next();
          if (object instanceof JComponent) {
            JComponent jComponent = (JComponent) object;

            int height = jComponent.getPreferredSize().height;
            jComponent.setPreferredSize(new Dimension(maxWidth, height));
          }
        }
      } else {
        int height = jcomp.getPreferredSize().height;
        jcomp.setPreferredSize(new Dimension(maxWidth, height));
      }
    }
  }

  private void scrollUpClicked() {

    if (scrollableItems.size() <= maxItemsToDisplay || beginIndex == 0) { // no
      // need
      // to
      // scroll
      return;
    }

    super.remove(maxItemsToDisplay + 1);
    super.add((Component) scrollableItems.elementAt(--beginIndex), 2);

    updateScrollingComponentsVisibility();

    if (getLastVisibleComponent() instanceof JSeparator) {
      scrollUpClicked();
    }
  }

  private void scrollDownClicked() {

    if (scrollableItems.size() <= maxItemsToDisplay || beginIndex + maxItemsToDisplay == scrollableItems.size()) { // no
      // need
      // to
      // scroll

      return;
    }

    super.remove(2);
    Component component = (Component) scrollableItems.elementAt(beginIndex + maxItemsToDisplay);

    super.add(component, maxItemsToDisplay + 1);
    beginIndex++;

    updateScrollingComponentsVisibility();

    if (getFirstVisibleComponent() instanceof JSeparator) {
      scrollDownClicked();
    }
  }

  private class ScrollUpOrDownButtonItem extends JPanel {

    private boolean direction = UP;

    private Polygon arrow = null;

    private boolean isMouseOver = false;

    private boolean enableScroll = false;

    private MyMouseListener mouseListener;

    private MyActionListener actionListener;

    private int initialDelay = 300;

    private int repeatDelay = 50;

    private Timer timer = null;

    public ScrollUpOrDownButtonItem(boolean direction) { // direction can be UP
      // or DOWN
      this.direction = direction;
      setVisible(false);

      setPreferredSize(new Dimension(10, 10));
      setSize(new Dimension(10, 10));
      setMinimumSize(new Dimension(10, 10));

      mouseListener = new MyMouseListener();
      addMouseListener(mouseListener);

      actionListener = new MyActionListener();
      timer = new Timer(repeatDelay, actionListener);
      timer.setInitialDelay(initialDelay);
    }

    public void enableScroll(boolean enableScroll) {
      this.enableScroll = enableScroll;
      repaint();
    }

    public void paintComponent(Graphics g) {

      Color oldColor = g.getColor();

      g.setColor(ScrollableMenu.this.getBackground());
      Rectangle rect = g.getClipBounds();
      g.fillRect(rect.x, rect.y, rect.width, rect.height);

      if (isMouseOver && enableScroll) {
        g.setColor(Color.blue);
      } else if (!enableScroll) {
        g.setColor(Color.gray);
      } else {
        g.setColor(ScrollableMenu.this.getForeground());
      }

      g.fillPolygon(getArrow());
      g.setColor(oldColor);
    }

    private Polygon getArrow() {
      if (arrow == null) {
        arrow = new Polygon();
        if (direction == UP) {
          arrow.addPoint((int) (getSize().width / 2.0 - 6.0 + 0.5), (int) (getSize().height / 2.0 + 3.0 + 0.5));
          arrow.addPoint((int) (getSize().width / 2.0 + 6.0 + 0.5), (int) (getSize().height / 2.0 + 3.0 + 0.5));
          arrow.addPoint((int) (getSize().width / 2.0 + 0.5), (int) (getSize().height / 2.0 - 4.0 + 0.5));
        } else {
          arrow.addPoint((int) (getSize().width / 2.0 - 6.0 + 0.5), (int) (getSize().height / 2.0 - 3.0 + 0.5));
          arrow.addPoint((int) (getSize().width / 2.0 + 6.0 + 0.5), (int) (getSize().height / 2.0 - 3.0 + 0.5));
          arrow.addPoint((int) (getSize().width / 2.0 + 0.5), (int) (getSize().height / 2.0 + 4.0 + 0.5));
        }
      }
      return arrow;
    }

    private void scroll() {
      if (direction == UP) {
        scrollUpClicked();
      } else {
        scrollDownClicked();
      }
    }

    private void startScrollTimer() {
      if (enableScroll) {
        timer.start();
      } else {
        timer.stop();
      }
    }

    /**
     * action for timer
     */
    private class MyActionListener implements ActionListener {

      public void actionPerformed(ActionEvent actionevent) {
        scroll();
      }
    }

    private class MyMouseListener extends MouseAdapter {

      public void mouseClicked(MouseEvent me) {
        scroll();
      }

      public void mouseEntered(MouseEvent me) {
        isMouseOver = true;
        repaint();
        if ((me.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
          // moved to this panel while left button is presses
          startScrollTimer();
        }
      }

      public void mouseExited(MouseEvent me) {
        isMouseOver = false;
        timer.stop();
        repaint();
      }

      public void mousePressed(MouseEvent mouseEvent) {
        startScrollTimer();
      }

      public void mouseReleased(MouseEvent mouseevent) {
        timer.stop();
      }
    }
  }
}