/*
 * Created on 26.01.2004
 */
package tvraterplugin;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * The Icon that is displayed in the ProgramTable
 * 
 * @author bodo
 */
public class RatingIcon implements Icon {

    /** This is Icon for an Overallrating */
    public final static int OVERALLRATING = 1;

    /** This is Icon for an Personalrating */
    public final static int PERSONALRATING = 2;

	/** Icons used to show the Ratings */
   	final static ImageIcon _icons[] = new ImageIcon[] {
            new ImageIcon(RatingIcon.class.getResource("0.gif")),
            new ImageIcon(RatingIcon.class.getResource("1.gif")),
            new ImageIcon(RatingIcon.class.getResource("2.gif")),
            new ImageIcon(RatingIcon.class.getResource("3.gif")),
            new ImageIcon(RatingIcon.class.getResource("4.gif")),
            new ImageIcon(RatingIcon.class.getResource("5.gif"))
    };

    /** The Rating used in this Icon */
    private Rating _rating;

    /** The Rating-Type */
    private int _ratingType;


    /**
     * Creates the Icon for a specific Rating
     * 
     * @param program
     */
    public RatingIcon(Rating rating, int ratingType) {
        _rating = rating;
        _ratingType = ratingType;
    }

	/**
	 *	Returns the Icon for a specific Rating
     */
	public static ImageIcon getImageIconForRating(int rateing) {
		return _icons[rateing];
	}

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.Icon#getIconHeight()
     */
    public int getIconHeight() {
        return 13;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.Icon#getIconWidth()
     */
    public int getIconWidth() {
        return 13;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics,
     *      int, int)
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.drawImage(getImageIconForRating(_rating.getIntValue(Rating.OVERALL)).getImage(), x, y, 13, 13, null);
    }

}
