package dvbviewer.com4j  ;

import com4j.IID;
import com4j.ReturnValue;
import com4j.VTID;

@IID("{D0D14E3A-EE5D-4109-804F-98AF39B2A44A}")
public interface IOSDMessage extends dvbviewer.com4j.IOSDItem {
    @VTID(15)
    void send();

    @VTID(16)
    int targetWindowId();

    @VTID(17)
    void targetWindowId(
        int value);

    @VTID(18)
    int targetControlId();

    @VTID(19)
    void targetControlId(
        int value);

    @VTID(20)
    java.lang.String subject();

    @VTID(21)
    void subject(
        java.lang.String value);

    @VTID(22)
    int senderControlID();

    @VTID(23)
    void senderControlID(
        int value);

    @VTID(24)
    int messages();

    @VTID(25)
    void messages(
        int value);

    @VTID(26)
    dvbviewer.com4j.IOSDItems list();

    @VTID(26)
    @ReturnValue(defaultPropertyThrough={dvbviewer.com4j.IOSDItems.class})
    dvbviewer.com4j.IOSDItem list(
        int index);

    @VTID(27)
    void sendToSystem();

}
