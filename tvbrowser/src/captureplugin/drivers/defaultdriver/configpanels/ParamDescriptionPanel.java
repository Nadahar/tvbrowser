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
package captureplugin.drivers.defaultdriver.configpanels;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;


/**
 * A Panel with a Description for the Parameters 
 */
public class ParamDescriptionPanel extends JPanel {

    /**
     * Constructor
     *
     */
    public ParamDescriptionPanel() {
        createGui();
    }

    /**
     * Creates the GUI 
     */
    private void createGui() {
        setLayout(new BorderLayout());
        
        JEditorPane helpPanel = new JEditorPane();
        helpPanel.setContentType("text/html");
        helpPanel.setEditable(false);
        helpPanel.setText("<html>" + 
                "  <head>" + 
                "<style type=\"text/css\" media=\"screen\">" + 
                "<!--" + 
                "body {font-family:Dialog;}" +
                "-->" + 
                "</style>" + 
                "  </head>" + 
                "<body>" + 
                "<b>Possible Parameters are</b>:\n\n" + 
                "<table>" +
                "<tr><td>%SD</td><td>start day</td></tr>" + 
                "<tr><td>%SMO</td><td>start month</td></tr>" +
                "<tr><td>%SY</td><td>start year</td></tr>" + 
                "<tr><td colspan=2></td></tr>" + 
                "<tr><td>%ED</td><td>end day</td></tr>" +
                "<tr><td>%EMO</td><td>end month</td></tr>" + 
                "<tr><td>%EY</td><td>end year</td></tr>" + 
                "<tr><td colspan=2></td></tr>" + 
                "<tr><td>%SH</td><td>start hour</td></tr>" + 
                "<tr><td>%SMI</td><td>start minute</td></tr>" + 
                "<tr><td colspan=2></td></tr>" + 
                "<tr><td>%EH</td><td>end hour</td></tr>" + 
                "<tr><td>%EMI</td><td>end minute</td></tr>" + 
                "<tr><td colspan=2></td></tr>" + 
                "<tr><td>%CNA</td><td>internal channel name</td></tr>"+
                "<tr><td>%CNB</td><td>%CNA without special chars and spaces</td></tr>"+
                "<tr><td>%CNC</td><td>%CNA URL-Encoded</td></tr>"+
                "<tr><td colspan=2></td></tr>" +
                "<tr><td>%CNU</td><td>external channel name</td></tr>" +
                "<tr><td>%CNV</td><td>%CNU without special chars and spaces</td></tr>" +
                "<tr><td>%CNW</td><td>%CNU URL-Encoded</td></tr>"+
                "<tr><td colspan=2></td></tr>" +
                "<tr><td>%CNF</td><td>%CNU if set, if not %CNA</td></tr>" +
                "<tr><td>%CNG</td><td>%CNV if set, if not %CNB</td></tr>" +
                "<tr><td>%CNH</td><td>%CNW if set, if not %CNC</td></tr>"+
                "<tr><td colspan=2></td></tr>" +
                "<tr><td>%T1</td><td>Title</td></tr>" + 
                "<tr><td>%T2</td><td>%T1 without special chars and spaces</td></tr>" +
                "<tr><td>%T3</td><td>URL-Encoded Title</td></tr>" + 
                "<tr><td colspan=2></td></tr>" + 
                "<tr><td>%LM</td><td>length (min)</td></tr>" +
                "<tr><td>%LS</td><td>length (sec)</td></tr>" + 
                "<tr><td colspan=2></td></tr>" + 
                "<tr><td>%UN</td><td>Username</td></tr>" +
                "<tr><td>%UP</td><td>Password</td></tr>" +
                "<tr><td colspan=2></td></tr>" + 
                "<tr><td>%D</td><td>description</td></tr>" +
                "<tr><td>%I</td><td>short Info</td></tr>" + 
                "<tr><td colspan=2></td></tr>" + 
                "<tr><td>%%</td><td>%</td></tr>" + 
                "</table>" + 
                "</body>");

        final JScrollPane spane = new JScrollPane(helpPanel); 
        
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                spane.getVerticalScrollBar().setValue(0);   
            }
            
        });
        
        add(spane, BorderLayout.CENTER);
    }
    
}
