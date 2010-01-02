package dvbviewer.com4j  ;

import com4j.*;

/**
 * Describes a EPGItem delivered by the EPGCollection/manager
 */
@IID("{340BE5BA-0B5B-490A-B0C3-FB30B88FCEF5}")
public interface IEPGItem extends Com4jObject {
    @VTID(7)
    int serviceID();

    @VTID(8)
    int transportID();

    @VTID(9)
    int charset();

    @VTID(10)
    void charset(
        int value);

    @VTID(11)
    int content();

    @VTID(12)
    void content(
        int value);

    @VTID(13)
    java.lang.String description();

    @VTID(14)
    void description(
        java.lang.String value);

    @VTID(15)
    java.util.Date duration();

    @VTID(16)
    void duration(
        java.util.Date value);

    @VTID(17)
    java.lang.String event();

    @VTID(18)
    void event(
        java.lang.String value);

    @VTID(19)
    int eventID();

    @VTID(20)
    void eventID(
        int value);

    @VTID(21)
    java.util.Date time();

    @VTID(22)
    void time(
        java.util.Date value);

    @VTID(23)
    java.lang.String title();

    @VTID(24)
    void title(
        java.lang.String value);

    @VTID(25)
    void setEPGEventID(
        int serviceID,
        int transportID);

    @VTID(26)
    java.util.Date endTime();

    @VTID(27)
    int epgEventID();

    @VTID(28)
    void epgEventID(
        int value);

}
