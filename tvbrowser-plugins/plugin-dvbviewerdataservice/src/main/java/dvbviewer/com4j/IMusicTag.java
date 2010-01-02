package dvbviewer.com4j  ;

import com4j.*;

/**
 * Describes a music tag of the tagreader
 */
@IID("{627BFFFA-13DE-48CA-B226-2609B952B7E3}")
public interface IMusicTag extends Com4jObject {
    @VTID(7)
    void clear();

    @VTID(8)
    java.lang.String album();

    @VTID(9)
    void album(
        java.lang.String value);

    @VTID(10)
    java.lang.String artist();

    @VTID(11)
    void artist(
        java.lang.String value);

    @VTID(12)
    java.lang.String comment();

    @VTID(13)
    void comment(
        java.lang.String value);

    @VTID(14)
    int duration();

    @VTID(15)
    void duration(
        int value);

    @VTID(16)
    java.lang.String genre();

    @VTID(17)
    void genre(
        java.lang.String value);

    @VTID(18)
    int timesPlayed();

    @VTID(19)
    void timesPlayed(
        int value);

    @VTID(20)
    java.lang.String title();

    @VTID(21)
    void title(
        java.lang.String value);

    @VTID(22)
    int track();

    @VTID(23)
    void track(
        int value);

    @VTID(24)
    int year();

    @VTID(25)
    void year(
        int value);

    @VTID(26)
    dvbviewer.com4j.IMusicTag clone();

}
