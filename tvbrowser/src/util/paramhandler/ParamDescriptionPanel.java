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
package util.paramhandler;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import util.ui.Localizer;

/**
 * A Panel with a Description for the Parameters
 */
public class ParamDescriptionPanel extends JPanel {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ParamDescriptionPanel.class);

  /**
   * Constructor
   */
  public ParamDescriptionPanel() {
    this(new ParamLibrary());
  }

  /**
   * Constructor
   * 
   */
  public ParamDescriptionPanel(ParamLibrary lib) {
    createGui(lib);
  }

  /**
   * Creates the GUI
   */
  private void createGui(ParamLibrary lib) {
    setLayout(new BorderLayout());

    JEditorPane helpPanel = new JEditorPane();
    helpPanel.setContentType("text/html");
    helpPanel.setEditable(false);
    StringBuilder text = new StringBuilder("<html>" + 
        " <head>" + 
        "<style type=\"text/css\" media=\"screen\">" + 
        "<!--" + 
        "body {font-family:Dialog;}" +
        "-->" + 
        "</style>" + 
        "  </head>" + 
        "<body>" + "<b>" +
        mLocalizer.msg("possibleParameters", "Possible Parameters") +
        ":</b>\n\n" + 
        "<table>");
    
    List<String> params = Arrays.asList(lib.getPossibleKeys());
    Collections.sort(params);

    for (String param : params) {
      text.append("<tr><td valign='top'>{");
      text.append(param);
      text.append("}</td><td>");
      text.append(lib.getDescriptionForKey(param));
      text.append("</td></tr>");
    }
    
    text.append("</table> <br>\n\n<b>" +
        mLocalizer.msg("possibleFunctions", "Possible Functions") +
        ":</b>\n\n<table>" );
    
    List<String> functions = Arrays.asList(lib.getPossibleFunctions());
    Collections.sort(functions);
    
    for (String function : functions) {
      text.append("<tr><td valign='top'>");
      text.append(function);
      text.append("</td><td>");
      text.append(lib.getDescriptionForFunctions(function));
      text.append("</td></tr>");
    }
    
    text.append("</table></body></html>");

    helpPanel.setText(text.toString());
    final JScrollPane spane = new JScrollPane(helpPanel);

    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        spane.getVerticalScrollBar().setValue(0);
      }

    });

    add(spane, BorderLayout.CENTER);
  }
}
