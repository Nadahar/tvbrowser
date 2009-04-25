/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 */
package tvraterplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.Localizer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.PluginsFilterComponent;
import devplugin.Program;

/**
 * This FilterComponent gives the users the opportunity
 * to filter for ratings of shows.
 */
public class TVRaterFilter extends PluginsFilterComponent {
    /** Localizer */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(TVRaterFilter.class);
    
    /** Accept-Value*/
    private int _value = 3;
    /** Show programs that are better or worse than the <code>_value</code>*/
    private boolean _best = true;

    /** Settings: show only good or bad programs*/
    private JComboBox _betterCombo;
    /** Settings: show only ratings that are better/worse than or equal this value*/
    private RatingComboBox _ratingBox;

    /**
     * @return Name of the Filter
     */
    public String getUserPresentableClassName() {
        return mLocalizer.msg("PresentableName", "Filter ratings");
    }

    /**
     * @return Version
     */
    public int getVersion() {
        return 1;
    }

    /**
     * Accept Programs if rating fits
     *
     * @param program check this program
     * @return true, if rating fits
     */
    public boolean accept(Program program) {
        Rating rating = TVRaterPlugin.getInstance().getRating(program);

        if (rating == null)
            return false;

        if (_best && (rating.getOverallRating() >= _value)) {
            return true;
        } else if (!_best && (rating.getOverallRating() <= _value)) {
            return true;
        }

        return false;
    }

    /**
     * Load settings
     * @param in load settings from this stream
     * @param version Version of the stream
     * @throws IOException Problems reading the Data
     * @throws ClassNotFoundException Problems creating classes while reading
     */
    public void read(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
        if (version >= 1) {
            _value = in.readInt();
            _best = in.readBoolean();
        }
    }

    /**
     * Store Settigns
     * @param out store settings here
     * @throws IOException Exception while storing the data
     */
    public void write(ObjectOutputStream out) throws IOException {
        out.writeInt(_value);
        out.writeBoolean(_best);
    }

    @Override
    public JPanel getSettingsPanel() {
        JPanel panel = new JPanel(new FormLayout("right:pref, 3dlu, pref", "pref, 3dlu, pref"));

        CellConstraints cc = new CellConstraints();

        panel.add(new JLabel(mLocalizer.msg("programsThatAre", "Programs that are")), cc.xy(1,1));
        _betterCombo = new JComboBox(new String[] {
            mLocalizer.msg("betterOrEqual", "better than or equal to"),
            mLocalizer.msg("worseOrEqual", "inferior or equal to")
        });

        if (_best)
            _betterCombo.setSelectedIndex(0);
        else
            _betterCombo.setSelectedIndex(1);

        panel.add(_betterCombo, cc.xy(3,1));

        panel.add(new JLabel(mLocalizer.msg("thisRating", "this rating") + ":"), cc.xy(1,3));

        Rating rating = new Rating("");
        rating.setOverallRating(_value);
        _ratingBox = new RatingComboBox(rating, Rating.OVERALL_RATING_KEY);
        panel.add(_ratingBox, cc.xy(3,3));

        return panel;
    }

    @Override
    public void saveSettings() {
        _best = _betterCombo.getSelectedIndex() < 1;
        _value = _ratingBox.getSelectedIndex();
    }
}