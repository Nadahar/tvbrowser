/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package tvbrowser.ui.settings;

import javax.swing.*;
import java.awt.*;

public class DataServiceSettingsTab implements devplugin.SettingsTab {
 
  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(DataServiceSettingsTab.class);
 
 
  public JPanel createSettingsPanel() {
    
    JPanel mainPanel=new JPanel(new BorderLayout());
    
    mainPanel.setBorder(BorderFactory.createEmptyBorder(7,7,7,7));
    /*
    String text="TV-Daten-Services sind externe Komponenten, die " +
      "TV-Daten (z.B. aus dem Internet) laden und sie dann " +
      "TV-Browser zur Verfügung stellen.\n\n" +
      "Damit TV-Browser Daten darstellen kann, muß mindestens ein " +
      "TV-Daten-Service installiert sein.\n\n"+
      "Wählen Sie ein TV-Daten-Service aus, um Änderungen an der " +
      "Konfiguration vorzunehmen.\n\n" +
      "Anmerkung: Nicht jedes TV-Daten-Service unterstützt diese " +
      "Funktion.\n" +
      "Bei Problemen wenden Sie sich bitte an den Autor des "+
      "jeweiligen TV-Daten-Services.";
      */
      /*
       * 
       * 
       * Die TV-Daten werden nicht direkt mit TV-Browser 
       * heruntergeladen. Diese Aufgabe übernehmen eigene 
       * Plugins, sogenannte TVDataServices. Ein solches 
       * TVDataService lädt die TV-Daten (üblicherweise) 
       * aus dem Internet und übergibt sie an TV-Browser, 
       * der sie dann darstellt und speichert (Im Verzeichnis 
       * tvdataservice/ befinden sich diese TVDataServices in 
       * Form von .jar-Dateien).
Es existiert kein einheitliches Protokoll oder Format, mit 
dem die TV-Daten übertragen werden. PremiereDataService lädt
 die TV-Daten beispielsweise von premiere.de in Form einer 
 .txt-Datei, XMLDataService lädt Daten im XMLTV-Format.
Auf diese Weise sind wir sehr flexibel, wenn es darum geht, 
neue Sender aufzunehmen.
Wir sind allerdings auf frei verfügbare TV-Datenquellen 
angewiesen.
       */
    
    JTextArea ta=new JTextArea(mLocalizer.msg("description", "tv data"));
    ta.setWrapStyleWord(true);
    ta.setLineWrap(true);
    ta.setOpaque(false);
    ta.setEditable(false);
    ta.setFocusable(false);
    
    mainPanel.add(ta,BorderLayout.NORTH);
    
    return mainPanel;
  }

  
    /**
     * Called by the host-application, if the user wants to save the settings.
     */
    public void saveSettings() {
      
    }

  
    /**
     * Returns the name of the tab-sheet.
     */
    public Icon getIcon() {
      return null;
    }
  
  
    /**
     * Returns the title of the tab-sheet.
     */
    public String getTitle() {
      return "TVDataServices";
    }
  
}