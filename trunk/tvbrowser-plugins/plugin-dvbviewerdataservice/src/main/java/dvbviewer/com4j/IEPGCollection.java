package dvbviewer.com4j  ;

import com4j.*;

/**
 * The EPGCollection is a list of EPGEntries
 */
@IID("{605E3A97-B64F-47DF-8E7A-7DC832694F96}")
public interface IEPGCollection extends Com4jObject,Iterable<Com4jObject> {
    @VTID(7)
    @DefaultMethod
    dvbviewer.com4j.IEPGItem item(
        int index);

    @VTID(8)
    int count();

    @VTID(9)
    java.util.Iterator<Com4jObject> iterator();

}
