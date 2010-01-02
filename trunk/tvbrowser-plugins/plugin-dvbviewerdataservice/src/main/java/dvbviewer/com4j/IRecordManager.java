package dvbviewer.com4j  ;

import com4j.*;

@IID("{F614DFEE-06E8-4AC8-B78A-0F5FF4EAA3C3}")
public interface IRecordManager extends Com4jObject,Iterable<Com4jObject> {
    @VTID(7)
    int count();

    @VTID(8)
    @DefaultMethod
    dvbviewer.com4j.IRecording items(
        int index);

    @VTID(9)
    java.lang.String filterby();

    @VTID(10)
    void filterby(
        java.lang.String value);

    @VTID(11)
    void deleteEntry(
        int id);

    @VTID(12)
    java.util.Iterator<Com4jObject> iterator();

}
