/*
 * Created on 11.04.2004
 */
package devplugin;

import java.awt.MediaTracker;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Holds all Information needed to Display the additional
 * Informations to a Program, like Black and White..
 * 
 * @author bodo
 */
public class ProgramInfoHelper {
    /**
     * The Translator
     */
    private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ProgramInfoHelper.class);

    /**
     * Logger for this class
     */
    private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(ProgramInfoHelper.class.getName());
    
    /**
     * The Bit-Array with all possibilities
     */
    public static final int[] mInfoBitArr = new int[] {
            Program.INFO_VISION_BLACK_AND_WHITE, Program.INFO_VISION_4_TO_3,
            Program.INFO_VISION_16_TO_9, Program.INFO_AUDIO_MONO,
            Program.INFO_AUDIO_STEREO, Program.INFO_AUDIO_DOLBY_SURROUND,
            Program.INFO_AUDIO_DOLBY_DIGITAL_5_1,
            Program.INFO_AUDIO_TWO_CHANNEL_TONE,
            Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED, Program.INFO_LIVE,
            Program.INFO_ORIGINAL_WITH_SUBTITLE, Program.INFO_NEW,
            Program.INFO_AUDIO_DESCRIPTION, Program.INFO_VISION_HD,
            Program.INFO_CATEGORIE_MOVIE, Program.INFO_CATEGORIE_SERIES, 
            Program.INFO_CATEGORIE_NEWS,
            Program.INFO_CATEGORIE_SHOW, Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT,
            Program.INFO_CATEGORIE_DOCUMENTARY, Program.INFO_CATEGORIE_ARTS,
            Program.INFO_CATEGORIE_SPORTS, Program.INFO_CATEGORIE_CHILDRENS,
            Program.INFO_CATEGORIE_OTHERS
    };

    /**
     * The Icons for the Bits
     */
    public static final Icon[] mInfoIconArr = new Icon[] {
            createIcon("Info_BlackAndWhite.gif"), // INFO_VISION_BLACK_AND_WHITE
            null, // INFO_VISION_4_TO_3
            createIcon("Info_16to9.gif"), // INFO_VISION_16_TO_9
            createIcon("Info_Mono.gif"), // INFO_AUDIO_MONO
            createIcon("Info_Stereo.gif"), // INFO_AUDIO_STEREO
            createIcon("Info_DolbySurround.gif"), // INFO_AUDIO_DOLBY_SURROUND
            createIcon("Info_DolbyDigital51.gif"), // INFO_AUDIO_DOLBY_DIGITAL_5_1
            createIcon("Info_TwoChannelTone.gif"), // INFO_AUDIO_TWO_CHANNEL_TONE
            createIcon(mLocalizer.msg("subtitleForAurallyHandicappedImage","Info_SubtitleForAurallyHandicapped.gif")), // INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED
            createIcon("Info_Live.png"), // INFO_LIVE
            createIcon(mLocalizer.msg("originalWithSubtitleImage","Info_OriginalWithSubtitle_EN.gif")), // INFO_ORIGINAL_WITH_SUBTITLE
            createIcon("Info_New.png"),  // INFO_NEW
            createIcon("Info_AudioDescription.png"),  // INFO_AUDIO_DESCRIPTION
            createIcon("Info_HD.png"), // High Definition Video
            createIcon("Info_Movie.png"), // INFO_CATEGORIE_MOVIE
            createIcon("Info_Series.png"), // INFO_CATEGORIE_SERIES
            createIcon("Info_News.png"), // News
            createIcon("Info_Show.png"), // Show
            createIcon("Info_Infotainment.png"), // Magazine/Infotainment
            null, //createIcon("Info_Documentary.png"), // Documentary
            null, //createIcon("Info_Arts.png"), // Arts
            null, //createIcon("Info_Sports.png"), // Sports
            null, //createIcon("Info_Childrens.png"), // Childrens
            null, //createIcon("Info_Others.png"), // Others
    };

    /**
     * The String representation of the Bits 
     */
    public static final String[] mInfoMsgArr = new String[] {
            mLocalizer.msg("blackAndWhite", "Black and white"),
            // INFO_VISION_BLACK_AND_WHITE
            mLocalizer.msg("4to3", "4:3"),
            // INFO_VISION_4_TO_3
            mLocalizer.msg("16to9", "16:9"),
            // INFO_VISION_16_TO_9
            mLocalizer.msg("mono", "Mono"),
            // INFO_AUDIO_MONO
            mLocalizer.msg("stereo", "Stereo"),
            // INFO_AUDIO_STEREO
            mLocalizer.msg("dolbySurround", "Dolby surround"),
            // INFO_AUDIO_DOLBY_SURROUND
            mLocalizer.msg("dolbyDigital5.1", "Dolby digital 5.1"),
            // INFO_AUDIO_DOLBY_DIGITAL_5_1
            mLocalizer.msg("twoChannelTone", "Two channel tone"),
            // INFO_AUDIO_TWO_CHANNEL_TONE
            mLocalizer.msg("subtitleForAurallyHandicapped",
                    "Subtitle for aurally handicapped"), // INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED
            mLocalizer.msg("live", "Live"), // INFO_LIVE
            mLocalizer.msg("originalWithSubtitle", "Original with subtitle"), // INFO_ORIGINAL_WITH_SUBTITLE
            mLocalizer.msg("new", "New"),
            mLocalizer.msg("audioDescription", "Audio Description"),
            mLocalizer.msg("hd", "high definition"),
            mLocalizer.msg("categorie_movie", "Movie"), // INFO_CATEGORIE_MOVIE
            mLocalizer.msg("categorie_series", "Series"),
            mLocalizer.msg("categorie_news", "News"),
            mLocalizer.msg("categorie_show", "Show"),
            mLocalizer.msg("categorie_magazine_infotainment", "Magazine/Infotainment"),
            mLocalizer.msg("categorie_documentary", "Documentary"),
            mLocalizer.msg("categorie_arts", "Arts/Music"),
            mLocalizer.msg("categorie_sports", "Sports"),
            mLocalizer.msg("categorie_childrens", "Children's Programming"),
            mLocalizer.msg("categorie_others", "Miscellaneous"),
    };


    /**
     * Creates the Icons
     * @param fileName Icon to create
     * @return created Icon
     */
    private static Icon createIcon(String fileName) {
        ImageIcon icon = new ImageIcon("imgs/" + fileName.trim());
        if (icon.getImageLoadStatus() == MediaTracker.ERRORED) {
          mLog.warning("Missing program info icon " + fileName);
        }
        return icon;
    }
    
    /**
     * Returns whether a bit (or combination of bits) is set in the specified
     * number.
     * @param num 
     * @param pattern 
     * @return <code>true</code>, if the bit is set
     */
    public static boolean bitSet(int num, int pattern) {
      return (num & pattern) == pattern;
    }

}