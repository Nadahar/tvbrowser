/*
 * Created on 18.06.2004
 */
package calendarexportplugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import util.exc.ErrorHandler;
import util.ui.Localizer;
import devplugin.Program;

/**
 * This Class handles the Export of the Files
 * 
 * @author bodo
 */
public class CalendarExporter {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CalendarExporter.class);
    /** DateFormat */
    private SimpleDateFormat mDate = new SimpleDateFormat("yyyyMMdd");
    /** TimeFormat */
    private SimpleDateFormat mTime = new SimpleDateFormat("HHmmss");
    /** TimeFormat */
    private SimpleDateFormat mExtTime = new SimpleDateFormat("HH:mm");
    
    /**
     * Exports a list of Programs into a vCal-File 
     * @param intothis into this File
     * @param list List to export
     * @param nulltime Lenght of Programs = 0?
     */
    public void exportVCal(File intothis, Program[] list, Properties settings) {
        try {
            boolean nulltime = settings.getProperty("nulltime", "false").equals("true");
            
            mTime.setTimeZone(TimeZone.getTimeZone("GMT"));
            mDate.setTimeZone(TimeZone.getTimeZone("GMT"));
            PrintStream out = new PrintStream(new FileOutputStream(intothis));

        	out.println("BEGIN:VCALENDAR");
        	out.println("PRODID:-//TV Browser//Calendar Exporter");
        	out.println("VERSION:1.0");

        	for (int i = 0; i < list.length; i++) {
                Program p = list[i];
                
                out.println();
                out.println("BEGIN:VEVENT");
                
                Calendar c = Calendar.getInstance();
                
                out.println("DCREATED:" + mDate.format(c.getTime())+"T"+mTime.format(c.getTime()));
                out.println("SEQUENZ:" + i);
                
                int classification = 0;
                
                try {
                    classification = Integer.parseInt(settings.getProperty("Classification", "0"));
                } catch(Exception e) {
                    
                }
                
                switch (classification) {
                case 0:
                    out.println("CLASS:PUBLIC");
                    break;
                case 1:
                    out.println("CLASS:PRIVATE");
                    break;
                case 2:
                    out.println("CLASS:CONFIDENTIAL");
                    break;

                default:
                    break;
                }
                
                
                out.println("PRIORITY:3");

                if (settings.getProperty("Categorie", "").trim().length() > 0) {
                    out.println("CATEGORIES:" + settings.getProperty("Categorie", ""));
                }
                
                
                
                c = getStartAsCalendar(p);
                
                out.println("UID:" + mDate.format(c.getTime()) + "-" + p.getID());
                out.println("SUMMARY:" + p.getChannel().getName() + " - " + noBreaks(p.getTitle()));
                
                out.println("DTSTART:"+mDate.format(c.getTime())+"T"+mTime.format(c.getTime()) + "Z");

                if (nulltime) {
                    out.println("DESCRIPTION:" + p.getChannel().getName() + " - " + noBreaks(p.getTitle()) + 
                                   "(-" + mExtTime.format(getEndAsCalendar(p).getTime()) + ")");
                } else {
                    out.println("DESCRIPTION:" + p.getChannel().getName() + " - " + noBreaks(p.getTitle()));

                    c = getEndAsCalendar(p);
                }
                
                int showtime = 0;
                
                try {
                    showtime = Integer.parseInt(settings.getProperty("ShowTime", "0"));
                } catch (Exception e) {
                    
                }

                switch (showtime) {
                case 0:
                    out.println("TRANSP:0");
                    break;
                case 1:
                    out.println("TRANSP:1");
                    break;
                default:
                    break;
                }                
                
                out.println("DTEND:"+mDate.format(c.getTime())+"T"+mTime.format(c.getTime()) + "Z");
                
                out.println("END:VEVENT");
        	}

        	out.println();
            out.println("END:VCALENDAR");
            out.close();
        } catch (Exception e) {
        	ErrorHandler.handle(mLocalizer.msg("saveError", "An error occured while saving the Calendar-File"), e);
        	e.printStackTrace();
    	}
    }

    /**
     * Exports a list of Programs into a iCal-File 
     * @param intothis into this File
     * @param list List to export
     * @param nulltime Lenght of Programs = 0?
     */
    public void exportICal(File intothis, Program[] list, Properties settings) {

        try {
            boolean nulltime = settings.getProperty("nulltime", "false").equals("true");

            mTime.setTimeZone(TimeZone.getTimeZone("GMT"));
            mDate.setTimeZone(TimeZone.getTimeZone("GMT"));

            PrintStream out = new PrintStream(new FileOutputStream(intothis));

            out.println("BEGIN:VCALENDAR");
            out.println("PRODID:-//TV Browser//Calendar Exporter");
            out.println("VERSION:2.0");

            for (int i = 0; i < list.length; i++) {
                
                Program p = list[i];
                
                out.println();
                out.println("BEGIN:VEVENT");
                
                Calendar c = Calendar.getInstance();
                
                out.println("CREATED:" + mDate.format(c.getTime())+"T"+mTime.format(c.getTime()));
                out.println("SEQUENZ:" + i);
                
                int classification = 0;
                
                try {
                    classification = Integer.parseInt(settings.getProperty("Classification", "0"));
                } catch(Exception e) {
                    
                }
                
                switch (classification) {
                case 0:
                    out.println("CLASS:PUBLIC");
                    break;
                case 1:
                    out.println("CLASS:PRIVATE");
                    break;
                case 2:
                    out.println("CLASS:CONFIDENTIAL");
                    break;

                default:
                    break;
                }

                out.println("PRIORITY:3");

                if (settings.getProperty("Categorie", "").trim().length() > 0) {
                    out.println("CATEGORIES:" + settings.getProperty("Categorie", ""));
                }

                c = getStartAsCalendar(p);
                
                out.println("UID:" + mDate.format(c.getTime()) + "-" + p.getID());
                out.println("SUMMARY:" + p.getChannel().getName() + " - " + noBreaks(p.getTitle()));
                
                out.println("DTSTART:"+mDate.format(c.getTime())+"T"+mTime.format(c.getTime()) + "Z");
                
                if (nulltime) {
                    out.println("DESCRIPTION:" + p.getChannel().getName() + " - " + noBreaks(p.getTitle()) + 
                                   "(-" + mExtTime.format(getEndAsCalendar(p).getTime()) + ")");
                } else {
                    out.println("DESCRIPTION:" + p.getChannel().getName() + " - " + noBreaks(p.getTitle()));

                    c = getEndAsCalendar(p);
                }
                
                int showtime = 0;
                
                try {
                    showtime = Integer.parseInt(settings.getProperty("ShowTime", "0"));
                } catch (Exception e) {
                    
                }

                switch (showtime) {
                case 0:
                    out.println("TRANSP:OPAQUE");
                    break;
                case 1:
                    out.println("TRANSP:TRANSPARENT");
                    break;
                default:
                    break;
                }
                
                out.println("DTEND:"+mDate.format(c.getTime())+"T"+mTime.format(c.getTime()) + "Z");
                
                out.println("END:VEVENT");
            }

            out.println();
            out.println("END:VCALENDAR");
            out.close();
        } catch (Exception e) {
            ErrorHandler.handle(mLocalizer.msg("saveError", "An error occured while saving the Calendar-File"), e);
            e.printStackTrace();
        }
        
    }

    /**
     * Replaces Newline-Characters with ' '
     * @param b replace here
     * @return String without Newline
     */
    public String noBreaks(String b) {
        b.replace('\n', ' ');
        return b;
    }

    
    /**
     * Gets the Start-Time as Calendar
     * @param p Program
     * @return Start-Time
     */
    public Calendar getStartAsCalendar(Program p) {
        Calendar cal = p.getDate().getCalendar();
        
        int min = p.getStartTime();
        
        int hour = min % 60;
        
        min = min - (hour * 60);
        
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, 0);
        
        return cal;
    }
    
    
    /**
     * Gets the End-Time as Calendar 
     * @param p Program
     * @return End-Time
     */
    public Calendar getEndAsCalendar(Program p) {
        Calendar cal = getStartAsCalendar(p);

        int leng = p.getLength();
        
        cal.add(Calendar.MINUTE, leng);
        
        return cal;
    }  
}