package dvbviewer.com4j  ;

import com4j.*;

@IID("{6AF57FCE-44D9-4DC8-9276-87F8F9CFE818}")
public interface IFavoritesManager extends Com4jObject {
    @VTID(7)
    int getFavoritesList(
        java.lang.Object list);

    @VTID(8)
    dvbviewer.com4j.IFavoritesCollection getFavorites();

    @VTID(8)
    @ReturnValue(defaultPropertyThrough={dvbviewer.com4j.IFavoritesCollection.class})
    dvbviewer.com4j.IFavoritesItem getFavorites(
        int index);

    @VTID(9)
    void add(
        int channelNr,
        java.lang.String group);

    @VTID(10)
    void addbyID(
        java.lang.String channelID,
        java.lang.String group);

    @VTID(11)
    void addGroup(
        java.lang.String group);

    @VTID(12)
    boolean isFavorite(
        int sid,
        int apid,
        java.lang.String name);

    @VTID(13)
    boolean isFavoritebyEPGChannelID(
        int epgChannelID);

    @VTID(14)
    boolean isFavoritebyChannelID(
        java.lang.String channelID);

    @VTID(15)
    int getGroupsList(
        java.lang.Object list);

    @VTID(16)
    boolean removeGroup(
        int id);

    @VTID(17)
    boolean removeFavorite(
        int id);

}
