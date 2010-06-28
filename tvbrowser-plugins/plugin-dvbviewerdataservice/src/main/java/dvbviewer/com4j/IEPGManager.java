package dvbviewer.com4j  ;

import com4j.Com4jObject;
import com4j.Holder;
import com4j.IID;
import com4j.VTID;

/**
 * Responsible for EPG
 */
@IID("{E06CE24F-EA08-48C5-B661-EC639E3D26B5}")
public interface IEPGManager extends Com4jObject {
    @VTID(7)
    dvbviewer.com4j.IEPGItem epgNow();

    @VTID(8)
    dvbviewer.com4j.IEPGItem epgNext();

    @VTID(9)
    dvbviewer.com4j.IEPGCollection get(
        int sid,
        int tid,
        java.util.Date startTime,
        java.util.Date endTime);

    @VTID(10)
    dvbviewer.com4j.IEPGItem newEPGItem();

    @VTID(11)
    void clear();

    @VTID(12)
    void addItem(
        dvbviewer.com4j.IEPGItem entry);

    @VTID(13)
    void commit();

    @VTID(14)
    int getAsArray(
        int channelID,
        java.util.Date startTime,
        java.util.Date endTime,
        java.lang.Object list);

    @VTID(15)
    boolean hasEPG(
        int epgChannelID);

    @VTID(16)
    void splitEPGChannelID(
        int epgChannelID,
        Holder<Integer> sid,
        Holder<Integer> tid);

    @VTID(17)
    dvbviewer.com4j.IEPGAddBuffer addEPG();

}
