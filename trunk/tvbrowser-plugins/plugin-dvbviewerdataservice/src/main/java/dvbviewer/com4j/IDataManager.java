package dvbviewer.com4j  ;

import com4j.Com4jObject;
import com4j.IID;
import com4j.VTID;

@IID("{297E2B6F-79B4-4C9F-91D2-7B5AEBA5D151}")
public interface IDataManager extends Com4jObject {
    @VTID(7)
    java.lang.String keys();

    @VTID(8)
    java.lang.String value(
        java.lang.String key);

    @VTID(9)
    void value(
        java.lang.String key,
        java.lang.String value);

    @VTID(10)
    int count();

    @VTID(11)
    java.lang.String parse(
        java.lang.String valu);

    @VTID(12)
    java.lang.String getAll();

}
