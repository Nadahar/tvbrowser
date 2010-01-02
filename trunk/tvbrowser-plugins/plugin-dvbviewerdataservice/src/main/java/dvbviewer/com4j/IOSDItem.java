package dvbviewer.com4j  ;

import com4j.*;

@IID("{1A2F98E3-535D-43BE-AA0F-24AB83EF1C51}")
public interface IOSDItem extends Com4jObject {
    @VTID(7)
    @DefaultMethod
    @ReturnValue(type=NativeType.VARIANT)
    java.lang.Object values(
        @MarshalAs(NativeType.VARIANT) java.lang.Object index);

    @VTID(8)
    @DefaultMethod
    void values(
        @MarshalAs(NativeType.VARIANT) java.lang.Object index,
        @MarshalAs(NativeType.VARIANT) java.lang.Object value);

    @VTID(9)
    java.lang.String keys(
        int index);

    @VTID(10)
    int count();

    @VTID(11)
    void clear();

    @VTID(12)
    boolean contains(
        java.lang.String key);

    @VTID(13)
    void copyForm(
        dvbviewer.com4j.IOSDItem sourceItem);

    @VTID(14)
    @ReturnValue(type=NativeType.VARIANT)
    java.lang.Object get(
        java.lang.String key,
        @MarshalAs(NativeType.VARIANT) java.lang.Object _default);

}
