/*
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
 */
package calendarexportplugin.exporter;

import java.util.Date;
import java.util.Properties;

import javax.swing.JOptionPane;

import jp.ne.so_net.ga2.no_ji.jcom.IDispatch;
import jp.ne.so_net.ga2.no_ji.jcom.ReleaseManager;
import util.exc.ErrorHandler;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import util.ui.Localizer;
import calendarexportplugin.CalendarExportPlugin;
import calendarexportplugin.utils.CalendarToolbox;
import devplugin.Program;

/**
 * Exporter for MS Outlook
 *
 * @author uwei
 *         <p/>
 *         Uses JCom.dll to call Outlook-MAPI
 */
public class OutlookExporter extends AbstractExporter {
    /**
     * Translator
     */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(OutlookExporter.class);

    private static int olAppointmentItem = 1;

    // BusyStatus
    private static int olFree = 0;
    //private static int olTentative = 1;
    private static int olBusy = 2;
    //private static int olOutOfOffice = 3;

    // Sensitivity
    private static int olNormal = 0;
    //private static int olPersonal = 1;
    private static int olPrivate = 2;
    private static int olConfidential = 3;

    public String getName() {
        return mLocalizer.msg("name", "MS Outlook Calendar");
    }

    /*
    * correct one hour if time zone uses daylight
    *
    * Bug in JCom.dll? VARIANT.CPP jmethodID method =
    * env->GetMethodID(clsDate,"getTime", "()J"); jlong val =
    * env->CallLongMethod(obj, method); var->vt =VT_DATE; TIME_ZONE_INFORMATION
    * timeinfo; GetTimeZoneInformation(&timeinfo); var->date
    * =double(val)/(24*60*60*1000)+25569.0-(timeinfo.Bias/(60.0*24.0));
    */
    private static Date correctTimeZone(final Date date) {
       Date ret=date;
       if(java.util.TimeZone.getDefault().useDaylightTime()){
            if(java.util.TimeZone.getDefault().inDaylightTime(date))
                ret.setTime(date.getTime()+1*60*60*1000);
        }
        return ret;
    }

    // performs 100 calls in 5 seconds
    private boolean writeEvent(String subject, String body, Date start, Date end,
                               int reminderMinutesBeforeStart, boolean reminderSet, int busyStatus,
                               String categories, boolean durationIs0, int sensitivity) {

        ReleaseManager rm = null;
        IDispatch outlook;
        IDispatch item;
        try {
            System.loadLibrary("JCom");
            rm = new ReleaseManager();
            try {
                outlook = new IDispatch(rm, "Outlook.Application"); // EXCEL–{‘Ì
            } catch (jp.ne.so_net.ga2.no_ji.jcom.JComException ex) {
                JOptionPane.showMessageDialog(CalendarExportPlugin.getInstance().getBestParentFrame(),
                        mLocalizer.msg("noOutlookFound", "MS Outlook is not installed."), Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.WARNING_MESSAGE);
                return false;
            }
            item = (IDispatch) outlook.invoke("CreateItem", IDispatch.PROPERTYGET, new Integer[]{olAppointmentItem});
            item.put("Subject", subject);
            item.put("Body", body);
            item.put("Start", correctTimeZone(start));
            item.put("End", correctTimeZone(end));
            item.put("ReminderSet", reminderSet);
            if (reminderSet)
                item.put("ReminderMinutesBeforeStart", reminderMinutesBeforeStart);
            item.put("BusyStatus", busyStatus);
            item.put("Categories", categories);
            if (durationIs0)
                item.put("Duration", 0);
            item.put("Sensitivity", sensitivity);
            item.method("Save", null);
            item.release();
            outlook.release();
        } catch (Exception e) {
            e.printStackTrace();
            ErrorHandler.handle(mLocalizer.msg("exportError", "An error occured while creating an appointment in MS Outlook."), e);
            return false;
        } finally {
            if (rm != null)
                rm.release();
        }
        return true;
    }

    public boolean exportPrograms(Program[] programs, Properties settings, AbstractPluginProgramFormating formating) {
        int classification = 0;
        try {
            classification = Integer.parseInt(settings.getProperty(CalendarExportPlugin.PROP_CLASSIFICATION, "0"));
        } catch (Exception e) {
            // Empty
        }
        if (classification == 1)
            classification = olPrivate;
        else if (classification == 2)
            classification = olConfidential;
        else
            classification = olNormal;

        String categories = settings.getProperty(CalendarExportPlugin.PROP_CATEGORY, "");

        int showtime = 0;
        try {
            showtime = Integer.parseInt(settings.getProperty(CalendarExportPlugin.PROP_SHOWTIME, "0"));
        } catch (Exception e) {
            // empty
        }
        if (showtime == 1)
            showtime = olFree;
        else
            showtime = olBusy;

        int alarmBefore = 0;
        try {
            alarmBefore = Integer.parseInt(settings.getProperty(CalendarExportPlugin.PROP_ALARMBEFORE, "0"));
        } catch (Exception ex) {
            // empty
        }
        boolean useAlarm = false;
        if (settings.getProperty(CalendarExportPlugin.PROP_ALARM, "false").equals("true"))
            useAlarm = true;

        boolean nullTime = false;
        if (settings.getProperty(CalendarExportPlugin.PROP_NULLTIME, "false").equals("true"))
            nullTime = true;

        for (Program program : programs) {
            ParamParser parser = new ParamParser();
            String title = parser.analyse(formating.getTitleValue(), program);
            String desc = parser.analyse(formating.getContentValue(), program);
            Date start = CalendarToolbox.getStartAsCalendar(program).getTime();
            Date end = CalendarToolbox.getEndAsCalendar(program).getTime();

            if (!writeEvent(title, desc, start, end, alarmBefore,
                    useAlarm, showtime, categories, nullTime, classification))
                return false;
        }
        return true;
    }

  public String getIconName() {
    return "outlook.png";
  }

}
