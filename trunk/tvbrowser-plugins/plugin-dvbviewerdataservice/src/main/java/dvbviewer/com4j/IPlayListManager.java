package dvbviewer.com4j  ;

import com4j.*;

@IID("{64EBF69D-6D41-4D3E-A229-4F11640C7D4B}")
public interface IPlayListManager extends Com4jObject {
    @VTID(7)
    java.lang.String getNext();

    @VTID(8)
    dvbviewer.com4j.IPlaylist getPlaylist(
        int playlist);

    @VTID(9)
    boolean hasChanged();

    @VTID(10)
    dvbviewer.com4j.IPlaylist loadPlayList(
        java.lang.String filename);

    @VTID(11)
    dvbviewer.com4j.IPlaylistItem newPlaylistItem();

    @VTID(12)
    dvbviewer.com4j.IPlaylist newPlayList();

    @VTID(12)
    @ReturnValue(defaultPropertyThrough={dvbviewer.com4j.IPlaylist.class})
    dvbviewer.com4j.IPlaylistItem newPlayList(
        int index);

    @VTID(13)
    void play(
        int titleNr,
        double position);

    @VTID(14)
    void playFile(
        java.lang.String filename);

    @VTID(15)
    void playNext(
        boolean autostart,
        double position);

    @VTID(16)
    void playPrevious();

    @VTID(17)
    void remove(
        int typ,
        java.lang.String filename);

    @VTID(18)
    void reset();

    @VTID(19)
    int currentPlaylist();

    @VTID(20)
    void currentPlaylist(
        int value);

    @VTID(21)
    int currentTitle();

    @VTID(22)
    void currentTitle(
        int value);

    @VTID(23)
    int entriesNotPresent();

    @VTID(24)
    boolean isPlaying();

}
