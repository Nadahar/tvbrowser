/*
 * Created on 11.04.2004
 */
package devplugin;

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
     * The Bit-Array with all Posibilities
     */
    public static int[] mInfoBitArr = new int[] {
            Program.INFO_VISION_BLACK_AND_WHITE, Program.INFO_VISION_4_TO_3,
            Program.INFO_VISION_16_TO_9, Program.INFO_AUDIO_MONO,
            Program.INFO_AUDIO_STEREO, Program.INFO_AUDIO_DOLBY_SURROUND,
            Program.INFO_AUDIO_DOLBY_DIGITAL_5_1,
            Program.INFO_AUDIO_TWO_CHANNEL_TONE,
            Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED, Program.INFO_LIVE,
            Program.INFO_ORIGINAL_WITH_SUBTITLE,};

    /**
     * The Icons for the Bits
     */
    public static Icon[] mInfoIconArr = new Icon[] {
            createIcon("Info_BlackAndWhite.gif"), // INFO_VISION_BLACK_AND_WHITE
            null, // INFO_VISION_4_TO_3
            createIcon("Info_16to9.gif"), // INFO_VISION_16_TO_9
            createIcon("Info_Mono.gif"), // INFO_AUDIO_MONO
            createIcon("Info_Stereo.gif"), // INFO_AUDIO_STEREO
            createIcon("Info_DolbySurround.gif"), // INFO_AUDIO_DOLBY_SURROUND
            createIcon("Info_DolbyDigital51.gif"), // INFO_AUDIO_DOLBY_DIGITAL_5_1
            createIcon("Info_TwoChannelTone.gif"), // INFO_AUDIO_TWO_CHANNEL_TONE
            createIcon("Info_SubtitleForAurallyHandicapped.gif"), // INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED
            null, // INFO_LIVE
            createIcon("Info_OriginalWithSubtitle.gif"), // INFO_ORIGINAL_WITH_SUBTITLE
    };

    /**
     * The String representation of the Bits 
     */
    public static String[] mInfoMsgArr = new String[] {
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
    };


    /**
     * Creates the Icons
     * @param fileName Icon to create
     * @return created Icon
     */
    private static Icon createIcon(String fileName) {
        return new ImageIcon("imgs/" + fileName);
    }

}