package util.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.plaf.UIResource;

public class LazyImageIcon extends ImageIcon implements UIResource {
    private URL location;

    public LazyImageIcon(URL location) {
        super();
        this.location = location;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (getImage() != null) {
            super.paintIcon(c, g, x, y);
        }
    }

    public int getIconWidth() {
        if (getImage() != null) {
            return super.getIconWidth();
        }
        return 0;
    }

    public int getIconHeight() {
        if (getImage() != null) {
            return super.getIconHeight();
        }
        return 0;
    }

    public Image getImage() {
        if (location != null) {
            setImage(Toolkit.getDefaultToolkit().getImage(location));
            location = null;
        }
        return super.getImage();
    }
}
