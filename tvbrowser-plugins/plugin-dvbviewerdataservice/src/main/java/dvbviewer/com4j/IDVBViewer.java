package dvbviewer.com4j  ;

import com4j.*;

/**
 * The Main object of the DVBViewer COM server
 */
@IID("{4CD4A1BF-7E90-4AA3-9C44-B36D78020FE8}")
public interface IDVBViewer extends Com4jObject {
    /**
     * Everything OSD related is found here.
     */
    @VTID(7)
    dvbviewer.com4j.IDVBOSD osd();

    /**
     * Sends a Commandvalue, see external List
     */
    @VTID(8)
    void sendCommand(
        int cmd);

    /**
     * The ManagerObject for the OSDWindows exposes some methods to manipulate the Windows.
     */
    @VTID(9)
    dvbviewer.com4j.IWindowManager windowManager();

    /**
     * Responsible for everything related to EPG
     */
    @VTID(10)
    dvbviewer.com4j.IEPGManager epgManager();

    /**
     * The Videotext object
     */
    @VTID(11)
    dvbviewer.com4j.IVideotext videotext();

    /**
     * Responsible for Channelmanaging
     */
    @VTID(12)
    dvbviewer.com4j.IChannelCollection channelManager();

    @VTID(12)
    @ReturnValue(defaultPropertyThrough={dvbviewer.com4j.IChannelCollection.class})
    dvbviewer.com4j.IChannelItem channelManager(
        int index);

    /**
     * Exposes the Current Channel object for convinience
     */
    @VTID(13)
    dvbviewer.com4j.IChannelItem currentChannel();

    /**
     * Utility Object with functions like Webaccess, Freedb and Tagreading
     */
    @VTID(14)
    dvbviewer.com4j.IUtils utils();

    /**
     * Manages the Recording timer
     */
    @VTID(15)
    dvbviewer.com4j.ITimerCollection timerManager();

    @VTID(15)
    @ReturnValue(defaultPropertyThrough={dvbviewer.com4j.ITimerCollection.class})
    dvbviewer.com4j.ITimerItem timerManager(
        int index);

    /**
     * writes to the OSD log. for level have a look at options -> OSD General. 0= Lowest priority, 99 Critical
     */
    @VTID(16)
    void logwrite(
        int level,
        java.lang.String key,
        java.lang.String description);

    /**
     * Delivers a value from the internal Datastate manager see external doc
     */
    @VTID(17)
    java.lang.String propGetValue(
        java.lang.String name);

    /**
     * Replaces propertytags with a value from the internal Datastate manager see external doc
     */
    @VTID(18)
    java.lang.String propParse(
        java.lang.String valu);

    /**
     * Set/Add a property in the internal Datastate Manager. Can be used to communicate data with other parts
     */
    @VTID(19)
    void propSetValue(
        java.lang.String name,
        java.lang.String value);

    /**
     * Gets a Value from the setup.xml of the DVBViewer.
     */
    @VTID(20)
    java.lang.String getSetupValue(
        java.lang.String section,
        java.lang.String name,
        java.lang.String _default);

    /**
     * Sets a Value in the setup.xml of the DVBViewer, careful, some values are proctected and can not be changed.
     */
    @VTID(21)
    void setSetupValue(
        java.lang.String section,
        java.lang.String name,
        java.lang.String value);

    /**
     * Everything about playlists here
     */
    @VTID(22)
    dvbviewer.com4j.IPlayListManager playListManager();

    /**
     * The Number of the current shown channel
     */
    @VTID(23)
    int currentChannelNr();

    /**
     * The Number of the current shown channel
     */
    @VTID(24)
    void currentChannelNr(
        int value);

    /**
     * Set the volume (0..1)
     */
    @VTID(25)
    double volume();

    /**
     * Set the volume (0..1)
     */
    @VTID(26)
    void volume(
        double value);

    /**
     * The channelnumer of the last shown channel, if any otherwise -1
     */
    @VTID(27)
    int lastChannel();

    /**
     * Plays a mediafile. Input file with full path
     */
    @VTID(28)
    void playFile(
        java.lang.String filename);

    /**
     * Clears all bookmarks for the file
     */
    @VTID(29)
    void bookmarksClear(
        java.lang.String filename);

    /**
     * Sets a bookmark for the file. Position is measured in seconds of playtime
     */
    @VTID(30)
    boolean bookmarkAdd(
        java.lang.String filename,
        double position);

    /**
     * Gets a list of bookmarks, see external doc for datadefinition of this list.
     */
    @VTID(31)
    dvbviewer.com4j.IOSDItems bookmarksGet(
        java.lang.String filename);

    /**
     * Manages the recordings from the database.
     */
    @VTID(32)
    dvbviewer.com4j.IRecordManager recordManager();

    @VTID(32)
    @ReturnValue(defaultPropertyThrough={dvbviewer.com4j.IRecordManager.class})
    dvbviewer.com4j.IRecording recordManager(
        int index);

    @VTID(33)
    dvbviewer.com4j.IMainMenuConfig mainMenuConfig();

    @VTID(34)
    dvbviewer.com4j.IFavoritesManager favoritesManager();

    @VTID(35)
    boolean isTimeshift();

    @VTID(36)
    boolean isDVD();

    @VTID(37)
    boolean isMediaplayback();

    @VTID(38)
    int audiomode();

    @VTID(39)
    java.lang.String getRDS();

    @VTID(40)
    void applyConfig();

    @VTID(41)
    int propCount();

    @VTID(42)
    java.lang.String propName(
        int index);

    @VTID(43)
    java.lang.String propValue(
        int index);

    @VTID(44)
    java.lang.String propGetAll();

    @VTID(45)
    void stop();

    @VTID(46)
    dvbviewer.com4j.IDVBViewerEventhelper events();

    @VTID(47)
    @ReturnValue(type=NativeType.VARIANT)
    java.lang.Object handle();

    @VTID(48)
    void quit();

    @VTID(49)
    void mute(
        boolean silent);

    @VTID(50)
    dvbviewer.com4j.IDVBHardware dvbHardware();

    @VTID(51)
    dvbviewer.com4j.IMessageEvents msgEvents();

    @VTID(52)
    java.lang.String currentLanguage();

    @VTID(53)
    java.lang.String getTranslation(
        java.lang.String original);

    @VTID(54)
    void loadAddLanguage(
        java.lang.String filename);

    @VTID(55)
    void sendInput(
        java.lang.String source,
        java.lang.String event);

    @VTID(56)
    dvbviewer.com4j.IDataManager dataManager();

    @VTID(57)
    void debug(
        java.lang.String param1);

}
