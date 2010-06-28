package dvbviewer.com4j  ;

import com4j.Com4jObject;
import com4j.DefaultMethod;
import com4j.IID;
import com4j.VTID;

/**
 * Listobject for timeritems.
 */
@IID("{54E78EB2-B31A-441A-AECE-5EE201E0CEF8}")
public interface ITimerCollection extends Com4jObject,Iterable<Com4jObject> {
    @VTID(7)
    int count();

    @VTID(8)
    @DefaultMethod
    dvbviewer.com4j.ITimerItem item(
        int index);

    @VTID(9)
    void add(
        dvbviewer.com4j.ITimerItem value);

    @VTID(10)
    void remove(
        int index);

    @VTID(11)
    boolean recording();

    @VTID(12)
    void stopRecording(
        int value);

    @VTID(13)
    boolean instantRecord();

    @VTID(14)
    java.util.Date nextRecordingTime();

    @VTID(15)
    boolean isRecording(
        dvbviewer.com4j.ITuner value);

    @VTID(16)
    int isTimerAt(
        java.util.Date atTime);

    @VTID(17)
    dvbviewer.com4j.ITimerItem addItem(
        java.lang.String channelID,
        java.util.Date date,
        java.util.Date startTime,
        java.util.Date endTime,
        java.lang.String description,
        boolean disableAV,
        boolean enabled,
        int recAction,
        int afterRec,
        java.lang.String days);

    @VTID(18)
    dvbviewer.com4j.ITimerItem newItem();

    @VTID(19)
    java.util.Iterator<Com4jObject> iterator();

    @VTID(20)
    dvbviewer.com4j.ITimerItem overlap(
        dvbviewer.com4j.ITimerItem timer);

    @VTID(21)
    int getTimerList(
        java.lang.Object list);

    @VTID(22)
    int addRecordingFolder(
        java.lang.String folder);

    @VTID(23)
    int getRecordingFolders(
        java.lang.Object list);

}
