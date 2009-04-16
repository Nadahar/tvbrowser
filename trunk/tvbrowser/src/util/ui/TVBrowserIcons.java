package util.ui;

import javax.swing.ImageIcon;

import tvbrowser.core.icontheme.IconLoader;

/**
 * common icons in TV-Browser
 * 
 * @author Bananeweizen
 *
 */
public class TVBrowserIcons {
  /**
   * size of large icons
   */
  public static final int SIZE_LARGE = 22;
  /**
   * size of small icons
   */
  public static final int SIZE_SMALL = 16;
  
  public static final ImageIcon plugin(int size) {
    return getIcon("actions", "view-plugins", size);
  }
  
  public static final ImageIcon webBrowser(int size) {
    return getIcon("apps", "internet-web-browser", size);
  }

  private static ImageIcon getIcon(String category, String name, int size) {
    return IconLoader.getInstance().getIconFromTheme(category, name, size);
  }
}
