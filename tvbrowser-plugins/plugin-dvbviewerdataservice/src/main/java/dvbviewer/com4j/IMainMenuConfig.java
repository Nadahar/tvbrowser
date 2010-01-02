package dvbviewer.com4j  ;

import com4j.*;

@IID("{E952AED8-6109-4228-BA5F-8ED28F313988}")
public interface IMainMenuConfig extends Com4jObject {
    @VTID(7)
    boolean active();

    @VTID(8)
    void active(
        boolean value);

    @VTID(9)
    void refresh();

    @VTID(10)
    int backColor();

    @VTID(11)
    void backColor(
        int value);

    @VTID(12)
    int disabledFontColor();

    @VTID(13)
    void disabledFontColor(
        int value);

    @VTID(14)
    int fontColor();

    @VTID(15)
    void fontColor(
        int value);

    @VTID(16)
    int iconBackColor();

    @VTID(17)
    void iconBackColor(
        int value);

    @VTID(18)
    int selectedBkColor();

    @VTID(19)
    void selectedBkColor(
        int value);

    @VTID(20)
    int separatorColor();

    @VTID(21)
    void separatorColor(
        int value);

    @VTID(22)
    int selectedFontColor();

    @VTID(23)
    void selectedFontColor(
        int value);

}
