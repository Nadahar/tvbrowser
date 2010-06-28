package dvbviewer.com4j  ;


/**
 * Describes the differnt Tag-audioformats supported by the Tagreader
 */
public enum Tagtype {
    /**
     * The APE Tag-Audioformat
     */
    tpApe, // 0
    /**
     * The FLAC Tag-Audioformat
     */
    tpFlac, // 1
    /**
     * The ID3V2 Tag-Audioformat
     */
    tpID3v2, // 2
    /**
     * The ID3V1 Tag-Audioformat
     */
    tpID3v1, // 3
    /**
     * The ogg-Vorbis Tag-Audioformat
     */
    tpVorbis, // 4
}
