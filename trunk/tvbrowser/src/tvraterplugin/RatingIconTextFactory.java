/*
 * Created on 26.01.2004
 */
package tvraterplugin;

import javax.swing.ImageIcon;

import util.ui.Localizer;

/**
 * The Icon that is displayed in the ProgramTable
 * 
 * @author bodo
 */
public class RatingIconTextFactory {

    /** Localizer */
    private static final Localizer _mLocalizer = Localizer
            .getLocalizerFor(RatingIconTextFactory.class);

	/** Icons used to show the Ratings */
   	final static ImageIcon _icons[] = new ImageIcon[] {
            new ImageIcon(RatingIconTextFactory.class.getResource("0.gif")),
            new ImageIcon(RatingIconTextFactory.class.getResource("1.gif")),
            new ImageIcon(RatingIconTextFactory.class.getResource("2.gif")),
            new ImageIcon(RatingIconTextFactory.class.getResource("3.gif")),
            new ImageIcon(RatingIconTextFactory.class.getResource("4.gif")),
            new ImageIcon(RatingIconTextFactory.class.getResource("5.gif"))
    };

	/**
	 *	Returns the Icon for a specific Rating
     */
	public static ImageIcon getImageIconForRating(int rating) {
	    if ((rating < 0) || (rating > _icons.length)) {
	        return new ImageIcon();
	    }
	            
	    return _icons[rating];
	}

	
	public static String getStringForRating(int rateing) {
        String ratingText;

        switch (rateing) {
        case 0:
            ratingText = _mLocalizer.msg("crap", "crap");
            break;
        case 1:
            ratingText = _mLocalizer.msg("mediocre", "mediocre");
            break;
        case 2:
            ratingText = _mLocalizer.msg("nice", "nice");
            break;
        case 3:
            ratingText = _mLocalizer.msg("enjoyable", "enjoyable");
            break;
        case 4:
            ratingText = _mLocalizer.msg("excellent", "excellent");
            break;
        default:
            ratingText = _mLocalizer.msg("super", "super");
            break;
        }
        return ratingText;
	}
}
