/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package captureplugin.drivers.defaultdriver;

import java.awt.Component;
import java.net.URLEncoder;
import java.util.Calendar;

import javax.swing.JOptionPane;

import util.ui.Localizer;
import devplugin.Program;


/**
 * This Class analyses the Parameters
 */
public class CaptureParamAnalyser {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CaptureParamAnalyser.class);
    /** Config */
    private DeviceConfig mData;
    /** Config-Dialog */
    private DefaultKonfigurator mDialog;
    /** Parent */
    private Component mParentFrame;
    
    /**
     * Create the Param-Anaylser
     * @param parentFrame Parent
     * @param data Settings
     * @param dialog Dialog for Settings
     */
    public CaptureParamAnalyser(Component parentFrame, DeviceConfig data, DefaultKonfigurator dialog) {
        mData = data;
        mDialog = dialog;
        mParentFrame = parentFrame;
    }
    
    /**
     * Returns the Commandline that is called
     * 
     * @param program Program to use
     * @return Commandline for program
     */
    public String getParamLine(ProgramTime programTime, String param) {

        try {

            boolean error = false;
            int nStart, nStop;

            String sDay, sMonth, sYear, sHour, sMinute;
            String eDay, eMonth, eYear, eHour, eMinute;
            int length;

            String line = new String();

            Calendar start = Calendar.getInstance();
            start.setTime(programTime.getStart());

            sDay = convertToString(start.get(java.util.Calendar.DAY_OF_MONTH), 2);
            sMonth = convertToString((start.get(java.util.Calendar.MONTH) + 1), 2);
            sYear = Integer.toString(start.get(java.util.Calendar.YEAR));
            sHour = convertToString(start.get(java.util.Calendar.HOUR_OF_DAY), 2);
            sMinute = convertToString(start.get(java.util.Calendar.MINUTE), 2);

            Calendar end = Calendar.getInstance();
            end.setTime(programTime.getEnd());

            eDay = convertToString(end.get(java.util.Calendar.DAY_OF_MONTH), 2);
            eMonth = convertToString((end.get(java.util.Calendar.MONTH) + 1), 2);
            eYear = Integer.toString(end.get(java.util.Calendar.YEAR));
            eHour = convertToString(end.get(java.util.Calendar.HOUR_OF_DAY), 2);
            eMinute = convertToString(end.get(java.util.Calendar.MINUTE), 2);
            
            length = (int) (end.getTimeInMillis() - start.getTimeInMillis()) / (60*1000);

            while ((nStop = param.indexOf("%")) != -1) {
                line += param.substring(0, nStop);
                switch (param.charAt(nStop + 1)) {
                case 'S':
                    switch (param.charAt(nStop + 2)) {
                    case 'D':
                        line += sDay;
                        param = param.substring(nStop + 3);
                        break;
                    case 'M':
                        switch (param.charAt(nStop + 3)) {
                        case 'O':
                            line += sMonth;
                            param = param.substring(nStop + 4);
                            break;
                        case 'I':
                            line += sMinute;
                            param = param.substring(nStop + 4);
                            break;
                        default:
                            error = true;
                        }
                        break;
                    case 'Y':
                        line += sYear;
                        param = param.substring(nStop + 3);
                        break;
                    case 'H':
                        line += sHour;
                        param = param.substring(nStop + 3);
                        break;
                    default:
                        error = true;
                    }
                    break;
                case 'E':
                    switch (param.charAt(nStop + 2)) {
                    case 'D':
                        line += eDay;
                        param = param.substring(nStop + 3);
                        break;
                    case 'M':
                        switch (param.charAt(nStop + 3)) {
                        case 'O':
                            line += eMonth;
                            param = param.substring(nStop + 4);
                            break;
                        case 'I':
                            line += eMinute;
                            param = param.substring(nStop + 4);
                            break;
                        default:
                            error = true;
                        }
                        break;
                    case 'Y':
                        line += eYear;
                        param = param.substring(nStop + 3);
                        break;
                    case 'H':
                        line += eHour;
                        param = param.substring(nStop + 3);
                        break;
                    default:
                        error = true;
                    }
                    break;
                case 'C':
                    switch (param.charAt(nStop + 2)) {
                    case 'N':
                        String chn =  getChannelName(programTime, param.charAt(nStop + 3), true);
                        if (chn == null) {
                            return null;
                        }
                        line+=chn;
                        param = param.substring(nStop + 4);
                        break;
                    default:
                        error = true;
                    }
                    break;
                case 'D':
                    line += programTime.getProgram().getDescription();
                    param = param.substring(nStop + 2);
                    break;
                case 'L':
                    if (param.charAt(nStop + 2) == 'S') {
                        line += length * 60;
                        param = param.substring(nStop + 3);
                    } else if (param.charAt(nStop + 2) == 'M') {
                        line += length;
                        param = param.substring(nStop + 3);
                    }
                    break;
                case 'I':
                    line += programTime.getProgram().getShortInfo();
                    param = param.substring(nStop + 2);
                    break;
                case 'T':
                    switch (param.charAt(nStop + 2)) {
                    case '1':
                        line += programTime.getProgram().getTitle();
                        param = param.substring(nStop + 3);
                        break;
                    case '2':
                        String title = stripSpecialChars(programTime.getProgram().getTitle());

                        line += title;
                        param = param.substring(nStop + 3);
                        break;
                    case '3':
                        line += URLEncoder.encode(programTime.getProgram().getTitle(), "UTF-8");
                        param = param.substring(nStop + 3);
                        break;
                    }
                    break;
                case 'U':
                    switch (param.charAt(nStop + 2)) {
                    case 'N':
                        String name = getUsername();
                        if (name == null) { return null;};
                        line += name;
                        param = param.substring(nStop + 3);
                        break;
                    case 'P':
                        String pwd = getPassword();
                        if (pwd == null) { return null;};
                        line += pwd;
                        param = param.substring(nStop + 3);
                        break;
                    }
                    break;
                case '%':
                    line += "%";
                    param = param.substring(nStop + 2);
                    break;
                default:
                    error = true;
                }// main switch

                if (error) {
                    JOptionPane.showMessageDialog(mParentFrame, mLocalizer.msg("WrongParam", "Parameters are not valid!"), mLocalizer.msg(
                            "Error", "Error"), JOptionPane.OK_OPTION);
                    mDialog.show(DefaultKonfigurator.TAB_PARAMETER);
                    return null;
                }
            }

            line += param;

            return line;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mParentFrame, mLocalizer.msg("WrongParam", "Parameters are not valid!"), mLocalizer.msg(
                    "Error", "Error"), JOptionPane.OK_OPTION);
            mDialog.show(DefaultKonfigurator.TAB_PARAMETER);
        }
        
        return null;
    }

    /**
     * Returns the ChannelName
     * 
     * @param programTime Program to use
     * @param c Param
     * @param showerror Show Errors
     * @return ChannelName
     */
    private String getChannelName(ProgramTime programTime, char c, boolean showerror) throws Exception {
        
        String line = "";
        
        if (c == 'A') {
            line = programTime.getProgram().getChannel().getName();
        } else if (c == 'B') {
            line = stripSpecialChars(programTime.getProgram().getChannel().getName());
        } else if (c == 'C') {
            line = URLEncoder.encode(programTime.getProgram().getChannel().getName(), "UTF-8");
        } else if (c == 'U') {
            String channelNumber = getChannelNumber(programTime.getProgram(), showerror);
            if (channelNumber == null) { return null; }
            line = channelNumber;
        } else if (c == 'V') {
            String channelNmber = getChannelNumber(programTime.getProgram(), showerror);
            if (channelNmber == null) { return null; }
            line = stripSpecialChars(channelNmber);
        } else if (c == 'W') {
            String channelNumber = getChannelNumber(programTime.getProgram(), showerror);
            if (channelNumber == null) { return null; }
            line = URLEncoder.encode(channelNumber, "UTF-8");
        } else if (c == 'F') {
            line = getChannelName(programTime, 'U', false);
            if (line == null) {
                line = getChannelName(programTime, 'A', false);
            }
        } else if (c == 'G') {
            line = getChannelName(programTime, 'V', false);
            if (line == null) {
                line = getChannelName(programTime, 'B', false);
            }
        } else if (c == 'H') {
            line = getChannelName(programTime, 'W', false);
            if (line == null) {
                line = getChannelName(programTime, 'C', false);
            }
        }
            
        return line;
    }

    /**
     * Strip all special Characters from the Text
     * 
     * @param text replace special Chars with _ in this String
     * @return text
     */
    private String stripSpecialChars(String text) {
        boolean foundSpecial = true;

        while (foundSpecial) {

            int pos = getSpecialChar(text);

            if (pos > -1) {
                if (pos >= text.length()) {
                    text = text.substring(0, pos);
                } else {
                    text = text.substring(0, pos) + text.substring(pos + 1);
                }
            } else {
                foundSpecial = false;
            }

        }

        return text.replaceAll("\\s", "_").replaceAll("__", "_");
    }

    /**
     * Gets the position of the first special character (aka no letter and no
     * digit)
     * 
     * @param text
     * @return Position of fist special char, -1 if no one found
     */
    private int getSpecialChar(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isLetterOrDigit(text.charAt(i)) && !Character.isWhitespace(text.charAt(i))) { return i; }
        }

        return -1;
    }


    /**
     * Returns the Username
     * @return Username
     */
    private String getUsername() {
        String uname = mData.getUserName();
        
        if ((uname == null) || (uname.trim().length() == 0)) {
            JOptionPane.showMessageDialog(mParentFrame,
                    mLocalizer.msg("NoUser", "Please specify Username!"), mLocalizer.msg("Errorn",
                            "Error"), JOptionPane.OK_OPTION);
            mDialog.show(DefaultKonfigurator.TAB_SETTINGS);
            return null;
        }
        
        return uname;
    }
    
    /**
     * Returns the Password
     * @return Password
     */
    private String getPassword() {
        String pwd = mData.getPassword();
        
        if ((pwd == null) || (pwd.trim().length() == 0)) {
            JOptionPane.showMessageDialog(mParentFrame,
                    mLocalizer.msg("NoPwd", "Please specify Password!"), mLocalizer.msg("Error",
                            "Error"), JOptionPane.OK_OPTION);
            mDialog.show(DefaultKonfigurator.TAB_SETTINGS);
            return null;
        }
        
        return pwd;
    }
    
    /**
     * Returns the ChannelNumber for a Channel
     * 
     * @param program Program to extract the Channel
     * @return ChannelNumber as String
     */
    private String getChannelNumber(Program program, boolean showerror) {
        String channelName = program.getChannel().getName();
        String channelNumber = "";

        if (((String) mData.getChannels().get(channelName)).trim().length() == 0) {
            if (showerror) {
                JOptionPane.showMessageDialog(mParentFrame,
                        mLocalizer.msg("NoNumber", "No Programnumber exists for channel {0}.", channelName), mLocalizer.msg("Error",
                                "Error"), JOptionPane.OK_OPTION);
                mDialog.show(DefaultKonfigurator.TAB_CHANNELS);
            }
            return null;
        } else {
            channelNumber = (String) mData.getChannels().get(channelName);
        }

        return channelNumber;
    }

    /**
     * Converts an Integer to String and adds 0 at the Begining
     * 
     * @param num Number to convert
     * @param length min. Lenght
     * @return String
     */
    private String convertToString(int num, int length) {
        String str = Integer.toString(num);

        while (str.length() < length) {
            str = "0" + str;
        }
        return str;
    }

}
