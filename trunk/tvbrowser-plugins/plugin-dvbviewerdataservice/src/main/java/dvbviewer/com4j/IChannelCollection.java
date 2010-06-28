package dvbviewer.com4j  ;

import com4j.Com4jObject;
import com4j.DefaultMethod;
import com4j.Holder;
import com4j.IID;
import com4j.VTID;

/**
 * A list of Channelitems optained from the Channelmanager.
 */
@IID("{4B39BDAF-65A1-4AA7-8C8F-F0753F9454F9}")
public interface IChannelCollection extends Com4jObject,Iterable<Com4jObject> {
    @VTID(7)
    int count();

    @VTID(8)
    @DefaultMethod
    dvbviewer.com4j.IChannelItem item(
        int index);

    @VTID(9)
    int getNr(
        java.lang.String id);

    @VTID(10)
    java.lang.String getChannelname(
        int sid);

    @VTID(11)
    java.lang.String getChannelID(
        int sid);

    @VTID(12)
    dvbviewer.com4j.IChannelItem getChannel(
        java.lang.String id,
        Holder<Integer> nr);

    @VTID(13)
    int getbyChannelname(
        java.lang.String name);

    @VTID(14)
    int getChannelList(
        java.lang.Object list);

    @VTID(15)
    dvbviewer.com4j.IChannelItem getByEPGChannelID(
        int epgChannelID);

    @VTID(16)
    java.util.Iterator<Com4jObject> iterator();

    @VTID(17)
    dvbviewer.com4j.IChannelItem getChannelByTID(
        int sid,
        int tid);

}
