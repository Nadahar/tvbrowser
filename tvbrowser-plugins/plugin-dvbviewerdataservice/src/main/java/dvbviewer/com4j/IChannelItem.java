package dvbviewer.com4j  ;

import com4j.*;

/**
 * Describes a  IChannelItem delivered by the ChannelCollection/manager
 */
@IID("{76148341-0A9F-436B-BEDE-50FFFED47A31}")
public interface IChannelItem extends Com4jObject {
    @VTID(7)
    java.lang.String root();

    @VTID(8)
    java.lang.String name();

    @VTID(9)
    java.lang.String category();

    @VTID(10)
    byte encrypted();

    @VTID(11)
    dvbviewer.com4j.ITuner tuner();

    @VTID(12)
    java.lang.String channelID();

    @VTID(13)
    int epgChannelID();

}
