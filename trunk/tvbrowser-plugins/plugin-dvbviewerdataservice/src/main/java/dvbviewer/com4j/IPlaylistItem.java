package dvbviewer.com4j  ;

import com4j.*;

@IID("{40659892-1CBF-48C1-8B41-1B2BECDFB2BA}")
public interface IPlaylistItem extends Com4jObject {
    @VTID(7)
    int typ();

    @VTID(8)
    void typ(
        int value);

    @VTID(9)
    boolean played();

    @VTID(10)
    void played(
        boolean value);

    @VTID(11)
    java.lang.String filename();

    @VTID(12)
    void filename(
        java.lang.String value);

    @VTID(13)
    int duration();

    @VTID(14)
    void duration(
        int value);

    @VTID(15)
    java.lang.String description();

    @VTID(16)
    void description(
        java.lang.String value);

}
