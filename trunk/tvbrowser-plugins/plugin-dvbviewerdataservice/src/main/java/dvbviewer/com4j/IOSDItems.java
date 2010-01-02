package dvbviewer.com4j  ;

import com4j.*;

@IID("{41E45161-06B8-4DF0-AB5C-3FAC75B51403}")
public interface IOSDItems extends Com4jObject {
    @VTID(7)
    void add(
        dvbviewer.com4j.IOSDItem item);

    @VTID(8)
    void addFromList(
        dvbviewer.com4j.IOSDItems sourceList);

    @VTID(9)
    dvbviewer.com4j.IOSDItem addNew();

    @VTID(9)
    @ReturnValue(type=NativeType.VARIANT,defaultPropertyThrough={dvbviewer.com4j.IOSDItem.class})
    java.lang.Object addNew(
        @MarshalAs(NativeType.VARIANT) java.lang.Object index);

    @VTID(10)
    void clear();

    @VTID(11)
    dvbviewer.com4j.IOSDItems clone();

    @VTID(11)
    @ReturnValue(defaultPropertyThrough={dvbviewer.com4j.IOSDItems.class})
    dvbviewer.com4j.IOSDItem clone(
        int index);

    @VTID(12)
    dvbviewer.com4j.IOSDItem findItem(
        java.lang.String key,
        @MarshalAs(NativeType.VARIANT) java.lang.Object value);

    @VTID(13)
    int findItemIndex(
        java.lang.String key,
        @MarshalAs(NativeType.VARIANT) java.lang.Object value);

    @VTID(14)
    void remove(
        int index);

    @VTID(15)
    void exchange(
        int oldIndex,
        int newIndex);

    @VTID(16)
    int count();

    @VTID(17)
    @DefaultMethod
    dvbviewer.com4j.IOSDItem items(
        int index);

}
