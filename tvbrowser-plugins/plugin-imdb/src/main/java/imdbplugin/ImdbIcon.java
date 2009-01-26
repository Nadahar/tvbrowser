package imdbplugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public final class ImdbIcon implements Icon {

  private static Icon mEmptyImdbIcon;
  private int mLength = 0;
  
  public ImdbIcon(ImdbRating rating) {
    mLength = (getIconWidth() - 1) * rating.getRating()
        / ImdbRating.MAX_RATING_NORMALIZATION;
  }

  public int getIconWidth() {
    return 16;
  }

  public int getIconHeight() {
    return 16;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    if (mEmptyImdbIcon == null) {
      mEmptyImdbIcon = new ImageIcon(getClass().getResource("rating.png"));
    }
    mEmptyImdbIcon.paintIcon(c, g, x, y);
    Color oc = g.getColor();
    g.setColor(Color.yellow);
    g.fill3DRect(x, y+7, mLength, 5, true);
    g.setColor(oc);
  }
}
