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
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(RatingIconTextFactory.class);

    /** Icons used to show the Ratings */
    private final static ImageIcon[] ICONS = new ImageIcon[] {
            new ImageIcon(RatingIconTextFactory.class.getResource("imgs/0.png")),
            new ImageIcon(RatingIconTextFactory.class.getResource("imgs/1.png")),
            new ImageIcon(RatingIconTextFactory.class.getResource("imgs/2.png")),
            new ImageIcon(RatingIconTextFactory.class.getResource("imgs/3.png")),
            new ImageIcon(RatingIconTextFactory.class.getResource("imgs/4.png")),
            new ImageIcon(RatingIconTextFactory.class.getResource("imgs/5.png")),
            new ImageIcon(RatingIconTextFactory.class.getResource("imgs/-1.png"))};

    /** The Genres */
    private static Properties mGenre = null;

    /**
     * Returns the Genre-Property
     * @return Genre-Property
     */
    public static synchronized Properties getGenres() {
        if (mGenre == null) {
            try {
                mGenre = new Properties();
                mGenre.load(RatingIconTextFactory.class.getResourceAsStream("genre_de.properties"));
                System.out.println(mGenre.size() + " Genres");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return mGenre;
    }

    /**
     * Returns an Icon for a Rating
     * @param rating Rating (0-5)
     * @return Icon for Rating
     */
    public static ImageIcon getImageIconForRating(int rating) {

        if (rating == -1) {
            return ICONS[ICONS.length - 1];
        }

        if ((rating < 0) || (rating >= ICONS.length)) {
            return new ImageIcon( new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        }

        return ICONS[rating];
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
            ratingText = mLocalizer.msg(prefix+"crap", "crap");
            break;
        case 1:
            ratingText = mLocalizer.msg(prefix+"mediocre", "mediocre");
            break;
        case 2:
            ratingText = mLocalizer.msg(prefix+"nice", "nice");
            break;
        case 3:
            ratingText = mLocalizer.msg(prefix+"enjoyable", "enjoyable");
            break;
        case 4:
            ratingText = mLocalizer.msg(prefix+"excellent", "excellent");
            break;
        case 5:
            ratingText = mLocalizer.msg(prefix+"super", "super");
            break;
        default:
            ratingText = "-";
            break;
        }
        return ratingText;
    }
}