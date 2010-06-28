package dvbviewer.com4j  ;

import com4j.Com4jObject;
import com4j.IID;
import com4j.VTID;

@IID("{663FF9A7-E59F-4700-8076-375200A1A122}")
public interface IRecording extends Com4jObject {
    @VTID(7)
    java.lang.String channel();

    @VTID(8)
    int played();

    @VTID(9)
    java.lang.String filename();

    @VTID(10)
    java.lang.String title();

    @VTID(11)
    java.lang.String description();

    @VTID(12)
    java.util.Date date();

    @VTID(13)
    java.util.Date duration();

    @VTID(14)
    int recID();

}
