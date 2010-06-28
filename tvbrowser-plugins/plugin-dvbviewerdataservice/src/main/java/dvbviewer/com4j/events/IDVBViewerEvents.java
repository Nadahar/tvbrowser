package dvbviewer.com4j.events;

import com4j.DISPID;
import com4j.IID;

@IID("{B397FB16-A027-4D5B-88EB-FD9A2AA28D92}")
public abstract class IDVBViewerEvents {
    @DISPID(201)
    public void onChannelChange(
        int channelNr) {
            throw new UnsupportedOperationException();
    }

    @DISPID(202)
    public void onRDS(
        java.lang.String rds) {
            throw new UnsupportedOperationException();
    }

    @DISPID(203)
    public void onOSDWindow(
        int windowID) {
            throw new UnsupportedOperationException();
    }

    @DISPID(204)
    public void onDVBVClose() {
            throw new UnsupportedOperationException();
    }

    @DISPID(205)
    public void onStartRecord(
        int id) {
            throw new UnsupportedOperationException();
    }

    @DISPID(206)
    public void onEndRecord() {
            throw new UnsupportedOperationException();
    }

    @DISPID(207)
    public void onPlaybackstart() {
            throw new UnsupportedOperationException();
    }

    @DISPID(208)
    public void onPlaybackEnd() {
            throw new UnsupportedOperationException();
    }

    @DISPID(209)
    public void onPlaystatechange(
        dvbviewer.com4j.TRendererTyp rendererType,
        dvbviewer.com4j.TPlaystates state) {
            throw new UnsupportedOperationException();
    }

    @DISPID(210)
    public void onAction(
        int actionID) {
            throw new UnsupportedOperationException();
    }

    @DISPID(211)
    public void onPlaylist(
        java.lang.String filename) {
            throw new UnsupportedOperationException();
    }

    @DISPID(212)
    public void onAddRecord(
        int id) {
            throw new UnsupportedOperationException();
    }

    @DISPID(213)
    public void onControlChange(
        int windowID,
        int controlID) {
            throw new UnsupportedOperationException();
    }

    @DISPID(214)
    public void onSelectedItemChange() {
            throw new UnsupportedOperationException();
    }

}
