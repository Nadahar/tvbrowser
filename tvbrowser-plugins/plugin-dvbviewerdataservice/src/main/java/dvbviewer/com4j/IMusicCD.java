package dvbviewer.com4j  ;

import com4j.Com4jObject;
import com4j.IID;
import com4j.VTID;

/**
 * Used by the freedb reader for returning MusicCDData
 */
@IID("{5F2B520D-2CCF-4E55-8138-B231EDD4FF2E}")
public interface IMusicCD extends Com4jObject {
    @VTID(7)
    int trackCount();

    @VTID(8)
    void trackCount(
        int value);

    @VTID(9)
    java.lang.String playorder();

    @VTID(10)
    void playorder(
        java.lang.String value);

    @VTID(11)
    java.lang.String discID();

    @VTID(12)
    void discID(
        java.lang.String value);

    @VTID(13)
    java.lang.String submitted_Via();

    @VTID(14)
    void submitted_Via(
        java.lang.String value);

    @VTID(15)
    int freedb_Revision();

    @VTID(16)
    void freedb_Revision(
        int value);

    @VTID(17)
    java.lang.String extendedData();

    @VTID(18)
    void extendedData(
        java.lang.String value);

    @VTID(19)
    int disc_Length();

    @VTID(20)
    void disc_Length(
        int value);

    @VTID(21)
    java.lang.String discYear();

    @VTID(22)
    void discYear(
        java.lang.String value);

    @VTID(23)
    java.lang.String discTitle();

    @VTID(24)
    void discTitle(
        java.lang.String value);

    @VTID(25)
    java.lang.String discGenre();

    @VTID(26)
    void discGenre(
        java.lang.String value);

    @VTID(27)
    java.lang.String discArtist();

    @VTID(28)
    void discArtist(
        java.lang.String value);

        @VTID(31)
        int getTrackList(
            java.lang.Object value);

    }
