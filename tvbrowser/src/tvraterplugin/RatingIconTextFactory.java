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
    private static final Localizer _mLocalizer = Localizer.getLocalizerFor(RatingIconTextFactory.class);

    /** Icons used to show the Ratings */
    final static ImageIcon _icons[] = new ImageIcon[] {
            new ImageIcon(RatingIconTextFactory.class.getResource("0.gif")),
            new ImageIcon(RatingIconTextFactory.class.getResource("1.gif")),
            new ImageIcon(RatingIconTextFactory.class.getResource("2.gif")),
            new ImageIcon(RatingIconTextFactory.class.getResource("3.gif")),
            new ImageIcon(RatingIconTextFactory.class.getResource("4.gif")),
            new ImageIcon(RatingIconTextFactory.class.getResource("5.gif"))};

    /**
     * Returns the Icon for a specific Rating
     */
    public static ImageIcon getImageIconForRating(int rating) {
        if ((rating < 0) || (rating > _icons.length)) { return new ImageIcon(); }

        return _icons[rating];
    }

    public static String getStringForRating(Object type, int rateing) {
        String ratingText;

        String prefix = "";
        
        if (type == Rating.OVERALL) {
            prefix = "Overall.";
        } else if (type == Rating.ACTION) {
            prefix = "Action.";
        } else if (type == Rating.ENTITLEMENT) {
            prefix = "Entitlement.";
        } else if (type == Rating.EROTIC) {
            prefix = "Erotic.";
        } else if (type == Rating.FUN) {
            prefix = "Fun.";
        } else if (type == Rating.TENSION) {
            prefix = "Tension.";
        }
        
        
        switch (rateing) {
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