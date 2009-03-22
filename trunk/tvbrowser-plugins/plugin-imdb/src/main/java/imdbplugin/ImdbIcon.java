/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package imdbplugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public final class ImdbIcon implements Icon {

  private static Icon mEmptyImdbIcon;
  private int mLength = 0;
  
  public ImdbIcon(final ImdbRating rating) {
    mLength = (getIconWidth() - 1) * rating.getRating()
        / ImdbRating.MAX_RATING_NORMALIZATION;
  }

  public int getIconWidth() {
    return 16;
  }

  public int getIconHeight() {
    return 16;
  }

  public void paintIcon(final Component c, final Graphics g, final int x,
      final int y) {
    if (mEmptyImdbIcon == null) {
      mEmptyImdbIcon = new ImageIcon(getClass().getResource("rating.png"));
    }
    mEmptyImdbIcon.paintIcon(c, g, x, y);
    final Color oc = g.getColor();
    g.setColor(Color.yellow);
    g.fill3DRect(x, y+7, mLength, 5, true);
    g.setColor(oc);
  }
}
