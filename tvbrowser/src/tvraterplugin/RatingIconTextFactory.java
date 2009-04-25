/*
 * Created on 26.01.2004
 */
package tvraterplugin;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;

import javax.swing.ImageIcon;

import util.ui.Localizer;

/**
 * The Icon and Text that is displayed in the ProgramTable
 * and various other Dialogs
 * 
 * @author bodo
 */
public class RatingIconTextFactory {

    /** Localizer */
    private static final Localizer _mLocalizer = Localizer.getLocalizerFor(RatingIconTextFactory.class);

    /** Icons used to show the Ratings */
    private final static ImageIcon _icons[] = new ImageIcon[] {
            new ImageIcon(RatingIconTextFactory.class.getResource("imgs/0.png")),
            new ImageIcon(RatingIconTextFactory.class.getResource("imgs/1.png")),
            new ImageIcon(RatingIconTextFactory.class.getResource("imgs/2.png")),
            new ImageIcon(RatingIconTextFactory.class.getResource("imgs/3.png")),
            new ImageIcon(RatingIconTextFactory.class.getResource("imgs/4.png")),
            new ImageIcon(RatingIconTextFactory.class.getResource("imgs/5.png")),
            new ImageIcon(RatingIconTextFactory.class.getResource("imgs/-1.png"))};

    /** The Genres */
    private static Properties _genre = null;
    
    /**
     * Returns the Genre-Property
     * @return Genre-Property
     */
    public static synchronized Properties getGenres() {
        if (_genre == null) {
            try {
                _genre = new Properties();
                _genre.load(RatingIconTextFactory.class.getResourceAsStream("genre_de.properties"));
                System.out.println(_genre.size() + " Genres");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return _genre;
    }
    
    /**
     * Returns an Icon for a Rating
     * @param rating Rating (0-5)
     * @return Icon for Rating
     */
    public static ImageIcon getImageIconForRating(int rating) {
        
        if (rating == -1) {
            return _icons[_icons.length - 1];
        }
        
        if ((rating < 0) || (rating >= _icons.length)) { 
            return new ImageIcon( new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)); 
        }

        return _icons[rating];
    }

    /**
     * Returns the String for a specific Rating
     * @param type Type (Rating.OVERALL ...)
     * @param rating the actual Rating (0-5)
     * @return String for the Rating
     */
    public static String getStringForRating(int type, int rating) {
        String ratingText = "-";

        String prefix = "";
        
        if (type == Rating.OVERALL_RATING_KEY) {
            prefix = "Overall.";
        } else if (type == Rating.ACTION_RATING_KEY) {
            prefix = "Action.";
        } else if (type == Rating.ENTITLEMENT_RATING_KEY) {
            prefix = "Entitlement.";
        } else if (type == Rating.EROTIC_RATING_KEY) {
            prefix = "Erotic.";
        } else if (type == Rating.FUN_RATING_KEY) {
            prefix = "Fun.";
        } else if (type == Rating.TENSION_RATING_KEY) {
            prefix = "Tension.";
        }
        
        
        switch (rating) {
        case 0:
            ratingText = _mLocalizer.msg(prefix+"crap", "crap");
            break;
        case 1:
            ratingText = _mLocalizer.msg(prefix+"mediocre", "mediocre");
            break;
        case 2:
            ratingText = _mLocalizer.msg(prefix+"nice", "nice");
            break;
        case 3:
            ratingText = _mLocalizer.msg(prefix+"enjoyable", "enjoyable");
            break;
        case 4:
            ratingText = _mLocalizer.msg(prefix+"excellent", "excellent");
            break;
        case 5:
            ratingText = _mLocalizer.msg(prefix+"super", "super");
            break;
        default:
            ratingText = "-";
            break;
        }
        return ratingText;
    }
}