package dvbviewer.com4j  ;

import com4j.Com4jObject;
import com4j.IID;
import com4j.VTID;

/**
 * The FreeDB object allows you to download informations about a AudioCD from freedb
 */
@IID("{9FAB3578-0E3E-4BB5-A2AF-3B9D48794D35}")
public interface IFreeDB_HTTP extends Com4jObject {
    @VTID(7)
    void readCDData(
        java.lang.String drive);

    @VTID(8)
    boolean verifyDiscID();

    @VTID(9)
    boolean queryCategories();

    @VTID(10)
    int matchCount();

    @VTID(11)
    boolean doQuery();

    @VTID(12)
    boolean doRead(
        int matchIndex);

    @VTID(13)
    java.lang.String lastError();

    @VTID(14)
    int lastResponseCode();

    @VTID(15)
    int categoryCount();

    @VTID(16)
    java.lang.String category(
        int index);

    @VTID(17)
    void setHostParam(
        java.lang.String freedb_host,
        int freedb_port,
        java.lang.String freedb_cgi);

    @VTID(18)
    dvbviewer.com4j.IMusicCD returnDiscInfo();

    @VTID(20)
    int getMatchlist(
        java.lang.Object value);

}
