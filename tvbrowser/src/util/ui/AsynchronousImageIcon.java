/*
 * Copyright Michael Keppler
 * 
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
package util.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * this class starts image loading on creation, but does not block
 * 
 * @author Bananeweizen
 * 
 */
public class AsynchronousImageIcon implements Icon {

  private ImageIcon mIcon;
  private Image mImage;

  public AsynchronousImageIcon(final String fileName) {
    mImage = ImageUtilities.createImageAsynchronous(fileName);
  }

  public AsynchronousImageIcon(final File file) {
    this(file.getAbsolutePath());
  }

  @Override
  public int getIconHeight() {
    return getIcon().getIconHeight();
  }

  @Override
  public int getIconWidth() {
    return getIcon().getIconWidth();
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    getIcon().paintIcon(c, g, x, y);
  }

  private Icon getIcon() {
    if (mIcon == null) {
      mIcon = new ImageIcon(mImage);
    }
    return mIcon;
  }

  /**
   * get an image icon from this icon
   * 
   * @return
   */
  public Icon getImageIcon() {
    return getIcon();
  }

}
