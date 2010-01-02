package dvbviewer.com4j  ;

import com4j.*;

/**
 * The Utils-Object offers you some helperfunktion
 */
@IID("{47C32284-00FF-4367-8A8C-1830E27FF107}")
public interface IUtils extends Com4jObject {
    /**
     * The function filters all html tags
     */
    @VTID(7)
    java.lang.String htmL2Text(
        java.lang.String inText);

    /**
     * the function disconnect a dial-up Connection
     */
    @VTID(8)
    void netDisconnect();

    /**
     * the function connects a dial-up Connection
     */
    @VTID(9)
    boolean netConnect();

    /**
     * the function download a file from a URL
     */
    @VTID(10)
    boolean downloadFileFromURL(
        java.lang.String sourceURL,
        java.lang.String destination,
        java.lang.String username,
        java.lang.String password);

    /**
     * the function downloads a htmlfile as a string
     */
    @VTID(11)
    java.lang.String getHTML(
        java.lang.String aUrl);

    /**
     * Helperfunction to break up a URL
     */
    @VTID(12)
    void parseURL(
        java.lang.String url,
        Holder<java.lang.String> hostname,
        Holder<java.lang.String> filename);

    /**
     * Helperfunction to retrieve a unique CD-Serial as used by the Windowsmediaplayer
     */
    @VTID(13)
    java.lang.String getCDSerialNo(
        java.lang.String drive);

    /**
     * The Tagreaderobject offers you reading and writing of various tag formats
     */
    @VTID(14)
    dvbviewer.com4j.ITagreader tagreader();

    /**
     * The FreeDB object allows you to download informations about a AudioCD from freedb
     */
    @VTID(15)
    dvbviewer.com4j.IFreeDB_HTTP freeDB();

}
