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
    private int mValue = 3;
    /** Show programs that are better or worse than the <code>_value</code>*/
    private boolean mBest = true;

    /** Settings: show only good or bad programs*/
    private JComboBox mBetterCombo;
    /** Settings: show only ratings that are better/worse than or equal this value*/
    private RatingComboBox mRatingBox;

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

        if (rating == null) {
          return false;
        }

        if (mBest && (rating.getOverallRating() >= mValue)) {
            return true;
        } else if (!mBest && (rating.getOverallRating() <= mValue)) {
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
            mValue = in.readInt();
            mBest = in.readBoolean();
        }
    }

    /**
     * Store Settings
     * @param out store settings here
     * @throws IOException Exception while storing the data
     */
    public void write(ObjectOutputStream out) throws IOException {
        out.writeInt(mValue);
        out.writeBoolean(mBest);
    }

    @Override
    public JPanel getSettingsPanel() {
        JPanel panel = new JPanel(new FormLayout("pref, 3dlu, pref", "pref, 3dlu, pref"));

        CellConstraints cc = new CellConstraints();

        panel.add(new JLabel(mLocalizer.msg("programsThatAre", "Programs that are")), cc.xy(1,1));
        mBetterCombo = new JComboBox(new String[] {
            mLocalizer.msg("betterOrEqual", "better than or equal to"),
            mLocalizer.msg("worseOrEqual", "inferior or equal to")
        });

        if (mBest) {
          mBetterCombo.setSelectedIndex(0);
        } else {
          mBetterCombo.setSelectedIndex(1);
        }

        panel.add(mBetterCombo, cc.xy(3,1));

        panel.add(new JLabel(mLocalizer.msg("thisRating", "this rating") + ":"), cc.xy(1,3));

        Rating rating = new Rating("");
        rating.setOverallRating(mValue);
        mRatingBox = new RatingComboBox(rating, Rating.OVERALL_RATING_KEY);
        panel.add(mRatingBox, cc.xy(3,3));

        return panel;
    }

    @Override
    public void saveSettings() {
        mBest = mBetterCombo.getSelectedIndex() < 1;
        mValue = mRatingBox.getSelectedIndex();
    }
}