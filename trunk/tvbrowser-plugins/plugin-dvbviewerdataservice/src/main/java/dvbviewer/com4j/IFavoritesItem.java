package dvbviewer.com4j  ;

import com4j.Com4jObject;
import com4j.IID;
import com4j.VTID;

@IID("{AB38A523-A294-4CD1-B72E-75E614096620}")
public interface IFavoritesItem extends Com4jObject {
    @VTID(7)
    java.lang.String group();

    @VTID(8)
    java.lang.String name();

    @VTID(9)
    java.lang.String channelID();

    @VTID(10)
    int nr();

}
