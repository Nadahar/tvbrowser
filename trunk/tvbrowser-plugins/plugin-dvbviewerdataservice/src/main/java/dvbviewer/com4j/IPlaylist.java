package dvbviewer.com4j  ;

import com4j.*;

@IID("{43242D96-8407-4100-918C-D76D41FC1B2A}")
public interface IPlaylist extends Com4jObject {
    @VTID(7)
    java.lang.String name();

    @VTID(8)
    void name(
        java.lang.String value);

    @VTID(9)
    @DefaultMethod
    dvbviewer.com4j.IPlaylistItem item(
        int index);

    @VTID(10)
    int count();

    @VTID(11)
    void shuffle();

    @VTID(12)
    void resetStatus();

    @VTID(13)
    void save(
        java.lang.String filename);

    @VTID(14)
    int remove(
        java.lang.String filename);

    @VTID(15)
    boolean load(
        java.lang.String filename);

    @VTID(16)
    void clear();

    @VTID(17)
    boolean allplayed();

    @VTID(18)
    void add(
        dvbviewer.com4j.IPlaylistItem item);

}
