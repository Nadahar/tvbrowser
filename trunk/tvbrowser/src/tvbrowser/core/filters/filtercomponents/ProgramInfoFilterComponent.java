/*
 * Created on 08.04.2004
 */
package tvbrowser.core.filters.filtercomponents;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.core.filters.FilterComponent;
import util.ui.ImageUtilities;
import devplugin.Program;

/**
 * Filtert nach bestimmten Programm-Informationen (zum Beispiel Untertitel)
 * 
 * FIXME: Grafiken müssen noch eingecheckt werden
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
     * Gibt die Version zurück
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
     * Liest die Einstellungen für dieses Plugin aus dem Stream
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
     * Gibt einen Panel zurück, der es ermöglicht, 
     * den Filter einzustellen
     * @see tvbrowser.core.filters.FilterComponent#getPanel()
     */
    public JPanel getPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;

        GridBagConstraints b = new GridBagConstraints();
        b.fill = GridBagConstraints.NONE;
        
        _checkBox = new JCheckBox[mInfoMsgArr.length];
        
        for (int i = 0; i < mInfoMsgArr.length; i++) {
        
        	final JCheckBox box = new JCheckBox();
        	_checkBox[i] = box;
        	JLabel label = new JLabel(mInfoMsgArr[i], mInfoIconArr[i], JLabel.LEFT);
        	
        	if (bitSet(selectedBits, mInfoBitArr[i])) {
        		box.setSelected(true);
        	}
        	
        	label.addMouseListener(new MouseAdapter() {
        		 public void mouseClicked(MouseEvent e) {
        		 	box.setSelected(!box.isSelected());
        		 }
        	});
        	 
            panel.add(box,b);
            panel.add(label,c);
            System.out.println(mInfoMsgArr[i]);
        }
        
        return panel;
    }

    /**
     * Im Dialog wurde OK gedrückt, alle Einstellungen können
     * nun übernommen werden
     * @see tvbrowser.core.filters.FilterComponent#ok()
     */
    public void ok() {
    	int selectedBits = 0;
    
    	for (int i = 0; i < _checkBox.length; i++) {
    		if (_checkBox[i].isSelected()) {
    			System.out.println(mInfoMsgArr[i]);
    			selectedBits = selectedBits | mInfoBitArr[i];
    		}
        }
        this.selectedBits = selectedBits;
    }

    /**
     * Gibt den momentanen Namen des Filters zurück
     * @see tvbrowser.core.filters.FilterComponent#getName()
     */
    public String getName() {
        return _name;
    }

    /**
     * Gibt die momentane Beschreibung des Filters zurück
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
     * Gibt den Namen des Filters zurück 
     */
    public String toString() {
        return mLocalizer.msg("ProgrammInfo", "Program-Info");
    }

    /**
     * Überprüft, ob Bits gesetzt sind
     * @param num hier prüfen
     * @param pattern diese pattern prüfen
     * @return Pattern gesetzt?
     */
    private boolean bitSet(int num, int pattern) {
        return (num & pattern) == pattern;
    }

    /**
     * Erzeugt ein Icon
     * @param fileName Name der Datei
     * @return Icon
     */
    private Icon createIcon(String fileName) {
        return ImageUtilities.createImageIconFromJar("programinfo/" + fileName, getClass());
    }

    /**
     * Die gesetzten Bits
     */
    private int selectedBits = 0;
    
    /**
     * Die CheckBoxen für den Panel
     */
    private JCheckBox[] _checkBox;

    /**
     * Alle Infos
     */
    private int[] mInfoBitArr = new int[] { Program.INFO_VISION_BLACK_AND_WHITE,
            Program.INFO_VISION_4_TO_3, Program.INFO_VISION_16_TO_9,
            Program.INFO_AUDIO_MONO, Program.INFO_AUDIO_STEREO,
            Program.INFO_AUDIO_DOLBY_SURROUND,
            Program.INFO_AUDIO_DOLBY_DIGITAL_5_1,
            Program.INFO_AUDIO_TWO_CHANNEL_TONE,
            Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED, Program.INFO_LIVE,
            Program.INFO_ORIGINAL_WITH_SUBTITLE,};

    /**
     * Alle Icons
     */
    private Icon[] mInfoIconArr = new Icon[] { createIcon("Info_BlackAndWhite.gif"), // INFO_VISION_BLACK_AND_WHITE
            null, // INFO_VISION_4_TO_3
            createIcon("Info_16to9.gif"), // INFO_VISION_16_TO_9
            createIcon("Info_Mono.gif"), // INFO_AUDIO_MONO
            createIcon("Info_Stereo.gif"), // INFO_AUDIO_STEREO
            createIcon("Info_DolbySurround.gif"), // INFO_AUDIO_DOLBY_SURROUND
            createIcon("Info_DolbyDigital51.gif"), // INFO_AUDIO_DOLBY_DIGITAL_5_1
            createIcon("Info_TwoChannelTone.gif"), // INFO_AUDIO_TWO_CHANNEL_TONE
            createIcon("Info_SubtitleForAurallyHandicapped.gif"), // INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED
            null, // INFO_LIVE
            createIcon("Info_OriginalWithSubtitle.gif"), // INFO_ORIGINAL_WITH_SUBTITLE
    };

    /**
     * Alle Texte
     */
    private String[] mInfoMsgArr = new String[] {
            mLocalizer.msg("blackAndWhite", "Black and white"),
            // INFO_VISION_BLACK_AND_WHITE
            mLocalizer.msg("4to3", "4:3"),
            // INFO_VISION_4_TO_3
            mLocalizer.msg("16to9", "16:9"),
            // INFO_VISION_16_TO_9
            mLocalizer.msg("mono", "Mono"),
            // INFO_AUDIO_MONO
            mLocalizer.msg("stereo", "Stereo"),
            // INFO_AUDIO_STEREO
            mLocalizer.msg("dolbySurround", "Dolby surround"),
            // INFO_AUDIO_DOLBY_SURROUND
            mLocalizer.msg("dolbyDigital5.1", "Dolby digital 5.1"),
            // INFO_AUDIO_DOLBY_DIGITAL_5_1
            mLocalizer.msg("twoChannelTone", "Two channel tone"),
            // INFO_AUDIO_TWO_CHANNEL_TONE
            mLocalizer.msg("subtitleForAurallyHandicapped",
                    "Subtitle for aurally handicapped"), // INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED
            mLocalizer.msg("live", "Live"), // INFO_LIVE
            mLocalizer.msg("originalWithSubtitle", "Original with subtitle"), // INFO_ORIGINAL_WITH_SUBTITLE
    };

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