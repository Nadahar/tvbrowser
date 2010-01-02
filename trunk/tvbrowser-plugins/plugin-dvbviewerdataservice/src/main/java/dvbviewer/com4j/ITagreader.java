package dvbviewer.com4j  ;

import com4j.*;

/**
 * The Tagreaderobject offers you reading and writing of various tag formats
 */
@IID("{2B361414-EDCF-48AD-BABB-5104348432ED}")
public interface ITagreader extends Com4jObject {
    @VTID(7)
    void saveTag(
        java.lang.String filename,
        java.lang.String title,
        java.lang.String artist,
        java.lang.String album,
        java.lang.String track,
        java.lang.String genre,
        java.lang.String year,
        java.lang.String comment,
        dvbviewer.com4j.Tagtype typ);

    @VTID(8)
    dvbviewer.com4j.IMusicTag readTag(
        java.lang.String filename);

}
