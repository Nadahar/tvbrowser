package dvbviewer.com4j  ;

import com4j.*;

/**
 * A single tunerstructure used by the dvbviewer
 */
@IID("{17D7A163-BCAE-4F69-8014-316503CC03F8}")
public interface ITuner extends Com4jObject {
    @VTID(7)
    dvbviewer.com4j.TDVBVTunerType tunerType();

    @VTID(8)
    int frequency();

    @VTID(9)
    int symbolRate();

    @VTID(10)
    int lnb();

    @VTID(11)
    int pmt();

    @VTID(12)
    int ecM_0();

    @VTID(13)
    int unused2();

    @VTID(14)
    int avFormat();

    @VTID(15)
    int fec();

    @VTID(16)
    int caiD_0();

    @VTID(17)
    int polarity();

    @VTID(18)
    int ecM_1();

    @VTID(19)
    int lnbSelection();

    @VTID(20)
    int caiD_1();

    @VTID(21)
    int diseqc();

    @VTID(22)
    int ecM_2();

    @VTID(23)
    int audioPID();

    @VTID(24)
    int caiD_2();

    @VTID(25)
    int videoPID();

    @VTID(26)
    int transportStreamID();

    @VTID(27)
    int telePID();

    @VTID(28)
    int networkID();

    @VTID(29)
    int sid();

    @VTID(30)
    int pcrpid();

    @VTID(31)
    int group();

}
