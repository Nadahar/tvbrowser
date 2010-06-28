package dvbviewer.com4j  ;

import com4j.Com4jObject;
import com4j.IID;
import com4j.VTID;

/**
 * A Timer object, it describes a recording timer
 */
@IID("{C243EAA6-9638-4EF1-9934-92F31990C0FC}")
public interface ITimerItem extends Com4jObject {
    @VTID(7)
    boolean cancelled();

    @VTID(8)
    java.lang.String channelName();

    @VTID(9)
    int channelNr();

    @VTID(10)
    boolean done();

    @VTID(11)
    java.lang.String filename();

    @VTID(12)
    void filename(
        java.lang.String value);

    @VTID(13)
    int id();

    @VTID(14)
    boolean instant();

    @VTID(15)
    boolean isPlugin();

    @VTID(16)
    boolean recording();

    @VTID(17)
    dvbviewer.com4j.ITuner tuner();

    @VTID(18)
    java.lang.String channelID();

    @VTID(19)
    void channelID(
        java.lang.String value);

    @VTID(20)
    java.util.Date date();

    @VTID(21)
    void date(
        java.util.Date value);

    @VTID(22)
    java.lang.String days();

    @VTID(23)
    void days(
        java.lang.String value);

    @VTID(24)
    java.lang.String description();

    @VTID(25)
    void description(
        java.lang.String value);

    @VTID(26)
    boolean disableAV();

    @VTID(27)
    void disableAV(
        boolean value);

    @VTID(28)
    boolean enabled();

    @VTID(29)
    void enabled(
        boolean value);

    @VTID(30)
    java.util.Date endTime();

    @VTID(31)
    void endTime(
        java.util.Date value);

    @VTID(32)
    dvbviewer.com4j.TDVBVShutdown shutdown();

    @VTID(33)
    void shutdown(
        dvbviewer.com4j.TDVBVShutdown value);

    @VTID(34)
    java.util.Date startTime();

    @VTID(35)
    void startTime(
        java.util.Date value);

    @VTID(36)
    dvbviewer.com4j.TDVBVTimerAction timerAction();

    @VTID(37)
    void timerAction(
        dvbviewer.com4j.TDVBVTimerAction value);

    @VTID(38)
    boolean executeable();

}
