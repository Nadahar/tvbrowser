package imdbplugin;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ImdbIcon implements Icon {
  private ImdbRating mRating;

  private static Icon mImdbIcon;
  int mLength = 0;
  public ImdbIcon(ImdbRating rating) {
    mRating = rating;
    mLength = 15 * rating.getRating() / 100;
  }

  public int getIconWidth() {
    return 16;
  }

  public int getIconHeight() {
    return 16;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    if (mImdbIcon == null) {
      mImdbIcon = new ImageIcon(getClass().getResource("rating.png"));
    }
    mImdbIcon.paintIcon(c, g, x, y);
    Color oc = g.getColor();
    g.setColor(Color.yellow);
    g.fill3DRect(x, y+7, mLength, 5, true);
    g.setColor(oc);
  }
}
