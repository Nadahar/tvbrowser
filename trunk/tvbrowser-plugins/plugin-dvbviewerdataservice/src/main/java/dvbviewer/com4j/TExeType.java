package dvbviewer.com4j  ;

import com4j.*;

/**
 * The Execute type describes the type of command send to the executor Object of the DVBViewer
 */
public enum TExeType {
    texScript, // 0
    texApp, // 1
    texCMD, // 2
    texFunct, // 3
    texHyperlink, // 4
}
