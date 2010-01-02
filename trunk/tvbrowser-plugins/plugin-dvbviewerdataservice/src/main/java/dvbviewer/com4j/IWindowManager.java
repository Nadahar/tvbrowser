package dvbviewer.com4j  ;

import com4j.*;

@IID("{142150A3-6184-40C9-9E26-BB5F46AD33DE}")
public interface IWindowManager extends Com4jObject {
    @VTID(7)
    void showWindow(
        int windowID);

    @VTID(8)
    boolean activeWindowHasBackground();

    @VTID(9)
    int activeWindowID();

    @VTID(10)
    int activeWindowNr();

    @VTID(11)
    boolean isOverlay();

    @VTID(12)
    void previousWindow();

    @VTID(13)
    int overlayWindowID();

    @VTID(14)
    void closeOverlay();

}
