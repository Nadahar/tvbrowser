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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2005-12-26 21:46:18 +0100 (Mo, 26 Dez 2005) $
 *   $Author: troggan $
 * $Revision: 1764 $
 */
package calendarexportplugin.exporter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import util.exc.ErrorHandler;
import util.misc.AppleScriptRunner;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import calendarexportplugin.CalendarExportPlugin;
import calendarexportplugin.utils.CalendarToolbox;
import devplugin.Program;

/**
 * Export for Apple iCal
 * 
 * @author bodum
 */
public class AppleiCalExporter extends AbstractExporter {

    /*
     * (non-Javadoc)
     * 
     * @see calendarexportplugin.exporter.ExporterIf#getName()
     */
    public String getName() {
        return "Apple iCal";
    }

    /*
     * (non-Javadoc)
     * 
     * @see calendarexportplugin.exporter.ExporterIf#exportPrograms(devplugin.Program[],
     *      java.util.Properties)
     */
    public boolean exportPrograms(Program[] programs, Properties settings, AbstractPluginProgramFormating formating) {
        System.out.println("Apple iCal!");

        AppleScriptRunner runner = new AppleScriptRunner();

        StringBuilder script = new StringBuilder();
        
        SimpleDateFormat formatDay = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        
        script.append("property myTVCalendar : \"TV-Browser\"\n");
        script.append("\n");
        script.append("tell application \"iCal\"\n");
        script.append("  if (exists (calendars whose title is myTVCalendar)) then\n");
        script.append("    set TVBrowserCalendar to first item of (calendars whose title is myTVCalendar)\n");
        script.append("  else\n");
        script.append("    set TVBrowserCalendar to make new calendar with properties {title:myTVCalendar}\n");
        script.append("  end if\n");
        script.append("\n");
        
        for (Program program : programs) {
            script.append("  set startDate to date \"").append(formatDay.format(CalendarToolbox.getStartAsCalendar(program).getTime())).append("\"\n");
            script.append("  set endDate to date \"").append(formatDay.format(CalendarToolbox.getEndAsCalendar(program).getTime())).append("\"\n");
            script.append("\n");
            script.append("  set props to {start date:startDate, end date:endDate, summary:\"");
            
            ParamParser parser = new ParamParser();
            
            String title = parser.analyse(formating.getTitleValue(), program);
            script.append(title);
               
            script.append("\", event:true, description:\"");
            
            String desc = parser.analyse(formating.getContentValue(), program);
            script.append(desc.replaceAll("\"", "\\\\\"").replace('\n', ' '));
            
            script.append("\"}\n");
            script.append("  set theEvent to make new event at end of (events of TVBrowserCalendar) with properties props\n");
            
            if (settings.getProperty(CalendarExportPlugin.PROP_ALARM, "false").equals("true")) {
                script.append("  make new display alarm at beginning of theEvent with properties {trigger interval:-");
                script.append(settings.getProperty(CalendarExportPlugin.PROP_ALARMBEFORE, "0"));
                script.append("}\n");
            }
            
        }
        
        script.append("end tell\n");
        
        try {
            runner.executeScript(script.toString());
        } catch (IOException e) {
            e.printStackTrace();
            ErrorHandler.handle("Error while execution of the applescript", e);
        }
    
        return true;
    }

}