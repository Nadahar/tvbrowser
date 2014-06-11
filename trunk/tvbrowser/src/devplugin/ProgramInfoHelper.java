/*
 * Created on 11.04.2004
 */
package devplugin;

import javax.swing.Icon;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.InfoThemeLoader;
import util.ui.Localizer;

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
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramInfoHelper.class);
    
    /**
     * The Bit-Array with all possibilities
     * @deprecated since 3.0, use {@link #getInfoBits()} instead
     */
    @Deprecated
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
            Program.INFO_CATEGORIE_OTHERS, Program.INFO_SIGN_LANGUAGE
    };

    /**
     * The Icons for the Bits
     * @deprecated since 3.0, use {@link #getInfoIcons()} instead
     */
    @Deprecated
    public static final Icon[] mInfoIconArr = InfoThemeLoader.getInstance().getIconThemeForIDOrDefault(Settings.propInfoIconThemeID.getString()).getInfoIcons();

    /**
     * The Icon URLs for the Bits
     * @deprecated since 3.0, use {@link #getInfoIconFilenames()} instead
     */
    @Deprecated
    public static final String[] mInfoIconFileName = InfoThemeLoader.getInstance().getIconThemeForIDOrDefault(Settings.propInfoIconThemeID.getString()).getInfoIconURLs();

    /**
     * The String representation of the Bits
     * @deprecated since 3.0, use {@link #getInfoIconMessages()} instead
     */
    @Deprecated
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
            mLocalizer.msg("categorie_show", "Show/Entertainment"),
            mLocalizer.msg("categorie_magazine_infotainment", "Magazine/Infotainment"),
            mLocalizer.msg("categorie_documentary", "Documentary/Reportage"),
            mLocalizer.msg("categorie_arts", "Theater/Concert"),
            mLocalizer.msg("categorie_sports", "Sports"),
            mLocalizer.msg("categorie_childrens", "Children's Programming"),
            mLocalizer.msg("categorie_others", "Other Program"),
            mLocalizer.msg("sign_language", "Sign language"),
    };
    
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
    
    /**
     * @since 3.0
     * @return the info bit array
     */
    public static final int[] getInfoBits() {
      return mInfoBitArr.clone();
    }
    
    /**
     * @since 3.0
     * @return the info icon array
     */
    public static final Icon[] getInfoIcons() {
      return mInfoIconArr.clone();
    }
    
    /**
     * @since 3.0
     * @return the info icon URLs array
     * @deprecated since 3.3.4
     */
    public static final String[] getInfoIconFilenames() {
      return getInfoIconURLs();
    }
    
    /**
     * @since 3.3.4
     * @return The info icon URLs array
     */
    public static final String[] getInfoIconURLs() {
      return mInfoIconFileName.clone();
    }
    
    /**
     * @since 3.0
     * @return the info message array (localized)
     */
    public static final String[] getInfoIconMessages() {
      return mInfoMsgArr.clone();
    }
    
    /**
     * 
     * @param bit The bit to get message for.
     * @return The message for the given bit.
     * @since 3.3.4
     */
    public static String getMessageForBit(int bit) {
      int[] bits = ProgramInfoHelper.getInfoBits();
      String[] names = ProgramInfoHelper.getInfoIconMessages();
      
      for(int i = 0; i < bits.length; i++) {
        if(bits[i] == bit) {
          return names[i];
        }
      }
      
      return "Unknown";
    }

    /**
     * @param bit The bit to get the index for.
     * @return The index of the bit or -1 if bit not found.
     * @since 3.3.4
     */
    public static int getIndexForBit(int bit) {
      int[] bits = ProgramInfoHelper.getInfoBits();
      
      for(int i = 0; i < bits.length; i++) {
        if(bits[i] == bit) {
          return i;
        }
      }
      
      return -1;
    }
    
    /**
     * 
     * @param index The index to get the bit for.
     * @return The bit for the index or 0 if index available.
     * @since 3.3.4
     */
    public static int getBitForIndex(int index) {
      int[] bits = ProgramInfoHelper.getInfoBits();
      
      if(index >= 0 && index < bits.length) {
        return bits[index];
      }
      
      return 0;
    }
}