/*
 * Created on 26.01.2004
 */
package tvraterplugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

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

	/** The Rating used in this Icon */
	private Rating _rating;
	
	/** The Rating-Type */
	private int _ratingType;

	/**
	 * Creates the Icon for a specific Rating
	 * @param program
	 */
	public RatingIcon(Rating rating, int ratingType) {
		_rating = rating;
		_ratingType = ratingType;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Icon#getIconHeight()
	 */
	public int getIconHeight() {
		return 16;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Icon#getIconWidth()
	 */
	public int getIconWidth() {
		return 16;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	public void paintIcon(Component c, Graphics g, int x, int y) {
		// TODO Auto-generated method stub
		g.setColor(Color.RED);
		g.drawRect(x,y,16,16);
	}

}
