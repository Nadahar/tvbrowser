package dvbviewer.com4j  ;

import com4j.Com4jObject;
import com4j.IID;
import com4j.VTID;

@IID("{8BD0691B-7A1A-4F21-AAD0-44396FFFA821}")
public interface IEPGAddBuffer extends Com4jObject {
    @VTID(7)
    dvbviewer.com4j.IEPGItem newItem();

    @VTID(8)
    void add(
        dvbviewer.com4j.IEPGItem entry);

    @VTID(9)
    void commit();

    @VTID(10)
    void clear();

}
