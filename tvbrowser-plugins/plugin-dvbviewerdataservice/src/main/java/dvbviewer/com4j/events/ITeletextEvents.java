package dvbviewer.com4j.events;

import com4j.DISPID;
import com4j.IID;

@IID("{CB23390F-A678-4E20-9FFB-9A1C6F741601}")
public abstract class ITeletextEvents {
    @DISPID(201)
    public void onDataArrive(
        int pageNr,
        int subPageNr) {
            throw new UnsupportedOperationException();
    }

}
