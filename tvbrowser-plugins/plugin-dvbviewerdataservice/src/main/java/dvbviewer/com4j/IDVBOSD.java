package dvbviewer.com4j  ;

import com4j.*;

/**
 * The OSD-Object
 */
@IID("{CD78F42E-9DBA-459E-B1B9-8747CCE9318D}")
public interface IDVBOSD extends Com4jObject {
    /**
     * Gets the current Channelname
     */
    @VTID(7)
    java.lang.String channelName();

    /**
     * Shows the input dialog and returns the input
     */
    @VTID(8)
    java.lang.String getText(
        java.lang.String msg,
        java.lang.String strline,
        boolean numbersOnly);

    /**
     * Closes the OSD
     */
    @VTID(9)
    void goHome();

    /**
     * set muted
     */
    @VTID(10)
    void setMute(
        boolean mute);

    /**
     * signal the OSD to redraw itself
     */
    @VTID(11)
    void setRefresh(
        boolean value);

    /**
     * true if the OSD is global activated
     */
    @VTID(12)
    boolean running();

    /**
     * shows a Message on the TV screen
     */
    @VTID(13)
    void showInfoinTVPic(
        java.lang.String info,
        @DefaultValue("5000")int timeout);

    /**
     * shows a YES/No dialog and returns the selection: True for Yes
     */
    @VTID(14)
    boolean showYesNo(
        java.lang.String heading,
        java.lang.String line1,
        java.lang.String line2,
        java.lang.String line3,
        @DefaultValue("0")boolean selected);

    @VTID(15)
    java.lang.String getfile(
        java.lang.String heading,
        java.lang.String startDir,
        java.lang.String ext);

    @VTID(16)
    int showPopUp(
        java.lang.String heading,
        java.lang.String options,
        @DefaultValue("-1")boolean centered);

    @VTID(17)
    boolean execProc(
        java.lang.String myProcStr);

    @VTID(18)
    java.lang.String appVersion();

    @VTID(19)
    java.lang.String appDir();

    @VTID(20)
    boolean isPlaying();

    @VTID(21)
    boolean isFullscreen();

    @VTID(22)
    java.lang.String settingDir();

    @VTID(23)
    boolean isPlayingVideo();

    @VTID(24)
    boolean isVisible();

    @VTID(25)
    boolean showInfo(
        java.lang.String heading,
        java.lang.String line1,
        java.lang.String line2,
        int secTimeout,
        java.lang.String buttonCaption);

    @VTID(26)
    dvbviewer.com4j.IOSDMessage newOSDMessage(
        int msgID,
        int windowID,
        int senderId);

    @VTID(27)
    void showImage(
        java.lang.String filename,
        int timeout);

    @VTID(28)
    java.lang.String createAlarm(
        java.util.Date interval,
        dvbviewer.com4j.TAlarmMode mode,
        java.util.Date startTime,
        boolean save,
        dvbviewer.com4j.IOSDMessage data);

    @VTID(29)
    boolean closeAlarm(
        java.lang.String id);

    @VTID(30)
    void addButton(
        int x,
        int y,
        int width,
        int height,
        int windowID,
        int actionID,
        int hyperlink,
        java.lang.String labeltext,
        java.lang.String funktion,
        java.lang.String app,
        java.lang.String arg);

    @VTID(31)
    void execNewProcess(
        java.lang.String programName,
        java.lang.String params,
        boolean wait,
        boolean lowPriority);

    @VTID(32)
    void showInfoHelp(
        java.lang.String heading,
        java.lang.String line1);

    @VTID(33)
    void addMenuButton(
        int windowID,
        int hyperlink,
        java.lang.String buttonText,
        java.lang.String pictureImage,
        java.lang.String programm,
        java.lang.String argument,
        java.lang.String dvbvFunction);

    @VTID(34)
    java.lang.String skinpath();

}
