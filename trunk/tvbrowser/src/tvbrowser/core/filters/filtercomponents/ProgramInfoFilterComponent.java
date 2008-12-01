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

package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import tvbrowser.core.filters.FilterComponent;
import util.ui.ScrollableJPanel;
import devplugin.Program;
import devplugin.ProgramInfoHelper;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.factories.Borders;

/**
 * Filtert nach bestimmten Programm-Informationen (zum Beispiel Untertitel)
 * 
 * @author bodo
 */
public class ProgramInfoFilterComponent implements FilterComponent {

	/**
	 * Erzeugt einen leeren Filter
     */
    public ProgramInfoFilterComponent() {
        this("", "");
    }

    /**
     * Erzeugt einen Filter
     * @param name Name 
     * @param description Beschreibung
     */
    public ProgramInfoFilterComponent(String name, String description) {
        _name = name;
        _desc = description;
    }

    /** 
     * Gibt die Version zurueck
     * @see tvbrowser.core.filters.FilterComponent#getVersion()
     */
    public int getVersion() {
        return 1;
    }

    /**
     * Wird dieses Programm akzeptiert von diesem Filter ?
     * @see tvbrowser.core.filters.FilterComponent#accept(devplugin.Program)
     */
    public boolean accept(Program program) {
        int info = program.getInfo();
        if (info < 1) { return false; }

        return bitSet(info, selectedBits);
    }

    /**
     * Liest die Einstellungen f�r dieses Plugin aus dem Stream
     * @see tvbrowser.core.filters.FilterComponent#read(java.io.ObjectInputStream,
     *      int)
     */
    public void read(ObjectInputStream in, int version) throws IOException,
            ClassNotFoundException {
		selectedBits = in.readInt();
    }

    /**
     * Schreibt die Einstellungen dieses Plugins in den Stream
     * @see tvbrowser.core.filters.FilterComponent#write(java.io.ObjectOutputStream)
     */
    public void write(ObjectOutputStream out) throws IOException {
		out.writeInt(selectedBits);
    }

    /**
     * Gibt einen Panel zurueck, der es ermoeglicht,
     * den Filter einzustellen
     * @see tvbrowser.core.filters.FilterComponent#getSettingsPanel()
     */
    public JPanel getSettingsPanel() {
        FormLayout layout = new FormLayout("pref, 3dlu, pref", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, new ScrollableJPanel());
        builder.setDefaultDialogBorder();
        builder.setBorder(Borders.EMPTY_BORDER);

        _checkBox = new JCheckBox[ProgramInfoHelper.mInfoMsgArr.length];
        
        for (int i = 0; i < ProgramInfoHelper.mInfoMsgArr.length; i++) {
        
        	final JCheckBox box = new JCheckBox();
        	_checkBox[i] = box;
        	JLabel label = new JLabel(" " + ProgramInfoHelper.mInfoMsgArr[i], ProgramInfoHelper.mInfoIconArr[i], JLabel.LEFT);
        	label.setBorder(new EmptyBorder(0,5,0,0));
        	
        	if (bitSet(selectedBits, ProgramInfoHelper.mInfoBitArr[i])) {
        		box.setSelected(true);
        	}
        	
        	label.addMouseListener(new MouseAdapter() {
        		 public void mouseClicked(MouseEvent e) {
        		 	box.setSelected(!box.isSelected());
        		 }
        	});
        	 
          builder.append(box);
          builder.append(label);
          builder.nextLine();
        }

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(builder.getPanel()), BorderLayout.CENTER);
        return centerPanel;
    }

    /**
     * Im Dialog wurde OK gedrueckt, alle Einstellungen koennen
     * nun uebernommen werden
     * @see tvbrowser.core.filters.FilterComponent#saveSettings()
     */
    public void saveSettings() {
    	int selectedBits = 0;
    
    	for (int i = 0; i < _checkBox.length; i++) {
    		if (_checkBox[i].isSelected()) {
    			selectedBits = selectedBits | ProgramInfoHelper.mInfoBitArr[i];
    		}
        }
        this.selectedBits = selectedBits;
    }

    /**
     * Gibt den momentanen Namen des Filters zurueck
     * @see tvbrowser.core.filters.FilterComponent#getName()
     */
    public String getName() {
        return _name;
    }

    /**
     * Gibt die momentane Beschreibung des Filters zurueck
     * @see tvbrowser.core.filters.FilterComponent#getDescription()
     */
    public String getDescription() {
        return _desc;
    }

    /**
     * Setzt den Namen des Filters
     * @see tvbrowser.core.filters.FilterComponent#setName(java.lang.String)
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Setzt die Beschreibung des Filters
     * @see tvbrowser.core.filters.FilterComponent#setDescription(java.lang.String)
     */
    public void setDescription(String desc) {
        _desc = desc;
    }

    /**
     * Gibt den Namen des Filters zurueck
     */
    public String toString() {
        return mLocalizer.msg("ProgrammInfo", "Program-Info");
    }

    /**
     * Ueberprueft, ob Bits gesetzt sind
     * @param num hier pruefen
     * @param pattern diese pattern pruefen
     * @return Pattern gesetzt?
     */
    private boolean bitSet(int num, int pattern) {
        return (num & pattern) == pattern;
    }

    /**
     * Die gesetzten Bits
     */
    private int selectedBits = 0;
    
    /**
     * Die CheckBoxen fuer den Panel
     */
    private JCheckBox[] _checkBox;

    /**
     * Der Lokalizer
     */
    private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ProgramInfoFilterComponent.class);    
    
    /**
     * Name des Filters
     */
    private String _name;

    /**
     * Beschreibung des Filters
     */
    private String _desc;
}