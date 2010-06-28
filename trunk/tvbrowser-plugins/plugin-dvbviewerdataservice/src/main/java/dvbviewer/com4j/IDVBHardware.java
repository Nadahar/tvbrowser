package dvbviewer.com4j  ;

import com4j.Com4jObject;
import com4j.IID;
import com4j.NativeType;
import com4j.ReturnValue;
import com4j.VTID;

@IID("{B5E7BC35-DDBD-412C-8B01-0501131897FB}")
public interface IDVBHardware extends Com4jObject {
    @VTID(7)
    @ReturnValue(type=NativeType.VARIANT)
    java.lang.Object signalErrorRate(
        int card);

    @VTID(8)
    @ReturnValue(type=NativeType.VARIANT)
    java.lang.Object signalQuality(
        int card);

    @VTID(9)
    @ReturnValue(type=NativeType.VARIANT)
    java.lang.Object signalStrength(
        int card);

    @VTID(10)
    java.lang.String hardwareName(
        int card);

    @VTID(11)
    int cardCount();

}
