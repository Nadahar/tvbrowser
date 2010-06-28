package dvbviewer.com4j  ;

import com4j.Com4jObject;
import com4j.DefaultMethod;
import com4j.IID;
import com4j.VTID;

@IID("{83AEB9FB-6FF3-49D6-A5F7-3CA4F0554743}")
public interface IFavoritesCollection extends Com4jObject,Iterable<Com4jObject> {
    @VTID(7)
    int count();

    @VTID(8)
    @DefaultMethod
    dvbviewer.com4j.IFavoritesItem item(
        int index);

    @VTID(9)
    java.util.Iterator<Com4jObject> iterator();

}
