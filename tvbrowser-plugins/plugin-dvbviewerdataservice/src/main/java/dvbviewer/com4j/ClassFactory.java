package dvbviewer.com4j  ;

import com4j.*;

/**
 * Defines methods to create COM objects
 */
public abstract class ClassFactory {
    private ClassFactory() {} // instanciation is not allowed


    /**
     * DVBViewerServer Objekt
     */
    public static dvbviewer.com4j.IDVBViewer createDVBViewer() {
        return COM4J.createInstance( dvbviewer.com4j.IDVBViewer.class, "{D0B1ACAD-1190-4E6D-BD60-41DFA6A28E30}" );
    }

    public static dvbviewer.com4j.IVideotext createTeletextmanager() {
        return COM4J.createInstance( dvbviewer.com4j.IVideotext.class, "{DF1A75B3-A9BB-4625-8893-57EB577F808C}" );
    }

    public static dvbviewer.com4j.IDVBViewerEventhelper createDVBViewerEvents() {
        return COM4J.createInstance( dvbviewer.com4j.IDVBViewerEventhelper.class, "{1937F78D-255B-4D76-9592-EA190EF2C616}" );
    }

    public static dvbviewer.com4j.IMessageEvents createMessageEvents() {
        return COM4J.createInstance( dvbviewer.com4j.IMessageEvents.class, "{447C6500-03DE-42E0-9CF5-6D5B5BEF7324}" );
    }
}
