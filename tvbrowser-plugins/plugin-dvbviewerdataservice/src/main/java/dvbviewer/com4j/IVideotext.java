package dvbviewer.com4j  ;

import com4j.Com4jObject;
import com4j.Holder;
import com4j.IID;
import com4j.VTID;

/**
 * The Videotext-Objekt enables you to get videotext from the DVBViewer
 */
@IID("{3696EC85-4824-4265-8882-E35290113C1C}")
public interface IVideotext extends Com4jObject {
    @VTID(7)
    java.lang.String getPage(
        int page,
        int subpage);

    @VTID(8)
    java.lang.String getPageAsHTML(
        int pageNr,
        int subpage);

    @VTID(9)
    boolean nextAvailable(
        Holder<Integer> page,
        Holder<Integer> subpage);

    @VTID(10)
    boolean prevAvailable(
        Holder<Integer> page,
        Holder<Integer> subpage);

    @VTID(11)
    int getPageAsRaw(
        int page,
        int sub,
        java.lang.Object list);

    @VTID(12)
    boolean findPage(
        int page,
        int sub);

}
