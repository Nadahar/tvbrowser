package com.gc.systray;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * This is the manager class. It may bee seen as a wrapper for the native code.
 * But also provides support for the rigth-click popup throught a JPoupupMenu.
 *
 * @author <a href="mail:gciubotaru@yahoo.com">George Ciubotaru</a>
 * @version 1.0
 */
public class SystemTrayIconManager {

    /**
     * Defines the mouse left click
     */
    public static final int mouseLeftClick = 0;
    /**
     * Defines the mouse right click
     */
    public static final int mouseRightClick = 1;
    /**
     * Defines the mouse double left click
     */
    public static final int mouseLeftDoubleClick = 2;
    /**
     * Defines the mouse double right click
     */
    public static final int mouseRightDoubleClick = 3;

    private int image;
    private String tooltip;
    private LinkedList<SystemTrayIconListener> listeners = new LinkedList<SystemTrayIconListener>();
    private int handler = 0;
    private Component leftClickView = null;
    private Component rightClickView = null;
    private boolean mouseOnPopup = false;

    private static String dllName = new String("DesktopIndicator");

    private JDialog d = new JDialog();
    private JWindow w = new JWindow(d);

    // this will contain the component if is not popup or window
    private JDialog wrapper = new JDialog();

    private final int DISTANCE = 1000; // a positive value


    /**
     * The class doesn't have a default constructor because the need for the icon.
     *
     * @param image an int that represent the icon resource
     * @param tooltip the tooltip string
     */
    public SystemTrayIconManager(int image, String tooltip) {
        this.image = image;
        this.tooltip = tooltip;

        d.setSize(0, 0);
        d.setLocation(-DISTANCE, -DISTANCE); //
        d.setVisible(true);
        String title = generateUniqueTitle();
        d.setTitle(title);
        nativeMoveToFront(title);
        d.setVisible(false);

        w.setSize(0, 0);
        w.setLocation(-DISTANCE, -DISTANCE); // place the dialog/invoker out of visible area
        w.setVisible(false);
    }

    /**
     * Add sys tray icon listener
     *
     * @param obj the listener to be added
     */
    public void addSystemTrayIconListener(SystemTrayIconListener obj) {
        listeners.add(obj);
    }

    /**
     * Remove sys tray icon listener
     *
     * @param obj the listener to be removed
     */
    public void removeSystemTrayIconListener(SystemTrayIconListener obj) {
        listeners.remove(obj);
    }

    /**
     * Should be called only once sice this method loads the dll.
     *
     * @return true if the loadin succeds, false otherwise
     */
    public static boolean initializeSystemDependent() {
        // Load JNI library
        try {
          System.loadLibrary(dllName);
        }
        catch( UnsatisfiedLinkError x ) {
                return false;
        }

        return true;
    }


    /**
     * Internal use - will be called from the native code
     */
    private void fireClicked(int buttonType, int x, int y) {
        for (int i = 0; i < listeners.size(); i++) {
            SystemTrayIconListener listener =
                (SystemTrayIconListener)listeners.get(i);
            /**
             * depending on button type call listener's
             *  mouseClickLeftButton
             *  ...
             */
            switch (buttonType) {
                case (mouseLeftClick):
                    listener.mouseClickedLeftButton(new Point(x, y), this);
                    break;
                case (mouseRightClick):
                    listener.mouseClickedRightButton(new Point(x, y), this);
                    break;
                case (mouseLeftDoubleClick):
                    listener.mouseLeftDoubleClicked(new Point(x, y), this);
                    break;
                case (mouseRightDoubleClick):
                    listener.mouseRightDoubleClicked(new Point(x, y), this);
                    break;
            }
        }


        Component comp = null;

        switch (buttonType) {
            case (mouseLeftClick):
                comp = leftClickView;
                break;
            case (mouseRightClick):
                comp = rightClickView;
                break;
        }
        if (comp == null) return;

        if (comp instanceof JPopupMenu) {
            JPopupMenu tmpPopupMenu = (JPopupMenu)comp;

            d.addWindowListener(new WindowAdapter() {
                public final void windowClosing(WindowEvent e) {
                    //System.out.println("windowClosing");
                    e.getWindow().removeWindowListener(this);
                }
                public final void windowDeactivated(WindowEvent e) {
                    //System.out.println("windowDeactivated");
                    // avoid losing dialog focus when press on popup and release outside popup
                    if (!mouseOnPopup) {
                        d.setVisible(false);
                        e.getWindow().removeWindowListener(this);
                    }
                    else {
                        e.getWindow().requestFocus();
                    }
                }
            });
            tmpPopupMenu.addPopupMenuListener(new PopupMenuListener() {
                public final void popupMenuCanceled(PopupMenuEvent e) {
                    //System.out.println("popupMenuCanceled");
                    ((JPopupMenu)e.getSource()).removePopupMenuListener(this);
                }
                public final void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    //System.out.println("popupMenuWillBecomeInvisible");
                    d.setVisible(false);
                    ((JPopupMenu)e.getSource()).removePopupMenuListener(this);
                }
                public final void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }
            });

            w.setVisible(true); // keep this order
            d.setVisible(true);

            Point p = computeDisplayPoint(x, y, tmpPopupMenu.getPreferredSize());
            tmpPopupMenu.show(w, p.x + DISTANCE, p.y + DISTANCE);
            tmpPopupMenu.repaint();
            d.toFront();
        }
        else
        if (comp instanceof Window) {
            Window window = (Window)comp;
            if (!window.isVisible()) {
                window.setLocation(comuteDisplayPointCentre(window.getSize()));
                window.setVisible(true);
                window.toFront();
            }
        }
        else
        if (comp instanceof Component) {
            if (!wrapper.isVisible()) {
                wrapper.getContentPane().removeAll();
                wrapper.getContentPane().add(comp);
                wrapper.pack();
                wrapper.setLocation(comuteDisplayPointCentre(wrapper.getSize()));
                wrapper.setVisible(true);
                wrapper.toFront();
            }
        }

    }

    private String generateUniqueTitle() {
        return "ThisIsATitle_UNIQUE_" + String.valueOf(new Date().getTime());
    }

    /**
     * Compute the proper position for a centered window
     */
    private Point comuteDisplayPointCentre(Dimension dim) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - dim.width) / 2;
        int y = (screen.height - dim.height) / 2;
        return new Point(x, y);
    }

    /**
     * Compute the proper position for a popup
     */
    private Point computeDisplayPoint(int x, int y, Dimension dim) {
        if (x - dim.width > 0) x -= dim.width;
        if (y - dim.height > 0) y -= dim.height;
        return new Point(x, y);
    }

    /**
     * This static methos is used to load an icon in the
     * native code and returns an identifier of this image
     *
     * @param filename the file that contains the icon (in ico file usualy)
     * @return -1 in case of error
     */
    public static int loadImage(String filename) {
        try	{
                return nativeLoadImage( filename );
        }
        catch( UnsatisfiedLinkError x )	{
                return -1;
        }
    }

    /**
     * Load an image form resource
     *
     * @param resourceNo rsource number
     * @return -1 in case of error
     */
    public static int loadImageFromResource(int resourceNo) {
        try	{
                return nativeLoadImageFromResource( resourceNo );
        }
        catch ( UnsatisfiedLinkError x ) {
                return -1;
        }
    }

    /**
     * Free the memore ocupied by the icon image
     *
     * @param image the image ot me removed
     */
    public static void freeImage(int image) {
        try {
                nativeFreeImage( image );
        }
        catch( UnsatisfiedLinkError x )	{}
    }

    /**
     * Hide the icon sys tray
     *
     * @deprecated replace by the new setVisible(false)
     */
    public void hide() {
        try	{
                nativeHide();
        }
        catch( UnsatisfiedLinkError x )	{}
    }

    /**
     * Show the icon sys tray
     *
     * @deprecated replace by the new setVisible(true)
     */
    public void show() {
      Thread t = new Thread() {
        public void run() {
          try {
            nativeEnable( image, tooltip );
          }catch( UnsatisfiedLinkError x )  {}
        }
      };
      t.setPriority(Thread.MIN_PRIORITY);
      t.start();
    }

    /**
     * Change the icon and tooltip
     *
     * @param image the new image
     * @param tooltip thenew tooltip
     */
    public void update(final int image,final String tooltip) {
      this.image = image;
      this.tooltip = tooltip;

      Thread t = new Thread(){
        public void run() {
          try {
            nativeEnable( image, tooltip );
          } catch( UnsatisfiedLinkError x ) {}
        }
      };
      t.setPriority(Thread.MIN_PRIORITY);
      t.start();
    }

    /**
     * The new method used to show or hide the icon
     *
     * @param status true - show icon
     */
    public void setVisible(boolean status) {
        if (status)
            show();
        else
            hide();
    }


    /**
     * internal use
     * with jdk1.3 when a JPopupMenu 'get the focus' by pressing one of its
     * JMenuItems the invoker (in this case the JWindow) loses the focus
     * so the message of hiding the invoker comes first the one of sending
     * the click button to the JPopupMenu
     * So when click on popup do not hide the invoker :)
     */
    private void addListenersToAllSubcomps(Component component, MouseListener listener) {
        if (component == null) return;
        component.addMouseListener(listener);
        if (!(component instanceof Container)) return;
        Component[] childs;
        if (component instanceof JMenu)
            childs = ((JMenu)component).getMenuComponents();
        else
            childs = ((Container)component).getComponents();
        for (int i = 0; i < childs.length; i++)
            addListenersToAllSubcomps(childs[i], listener);
    }

    /**
     * Set the Component to be prompted when mouse right click
     *
     * @param component the component to be shown
     */
    public void setRightClickView(Component component) {
        rightClickView = component;
        if (!(rightClickView instanceof JPopupMenu)) return;
        MouseAdapter listener = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                mouseOnPopup = true;
            }
            public void mouseExited(MouseEvent e) {
                mouseOnPopup = false;
            }
        };
        addListenersToAllSubcomps(rightClickView, listener);
    }

    /**
     * Removes the actual component (if any) that shows when right click
     */
    public void removeRightClickView() {
        rightClickView = null;
    }

    /**
     * Set the Component to be prompted when mouse left click
     *
     * @param component the component to be shown
     */
    public void setLeftClickView(Component component) {
        leftClickView = component;
        if (!(leftClickView instanceof JPopupMenu)) return;
        MouseAdapter listener = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                mouseOnPopup = true;
            }
            public void mouseExited(MouseEvent e) {
                mouseOnPopup = false;
            }
        };
        addListenersToAllSubcomps(leftClickView, listener);
    }

    /**
     * Removes the actual component (if any) that shows when left click
     */
    public void removeLeftClickView() {
        leftClickView = null;
    }

    /**
     * finaalize method
     */
    public void finalize() {
        nativeDisable();
    }

    /**
     * native methods
     */
    private synchronized native void nativeDisable() throws UnsatisfiedLinkError;
    private synchronized native void nativeEnable( int image, String tooltip ) throws UnsatisfiedLinkError;
    private synchronized static native void nativeFreeImage( int image ) throws UnsatisfiedLinkError;
    private synchronized static native int nativeLoadImage( String filename ) throws UnsatisfiedLinkError;
    private synchronized static native int nativeLoadImageFromResource( int inResource ) throws UnsatisfiedLinkError;
    private synchronized native void nativeHide() throws UnsatisfiedLinkError;
    private synchronized native void nativeMoveToFront(String title) throws UnsatisfiedLinkError;
    private synchronized static native void nativeRemoveTitleBar(String title) throws UnsatisfiedLinkError; // not used
    private synchronized static native void nativeGetMousePos(Point p) throws UnsatisfiedLinkError; // not used
}
