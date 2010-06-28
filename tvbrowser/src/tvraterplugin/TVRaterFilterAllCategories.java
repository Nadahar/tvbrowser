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
 * to filter for all ratings of shows. The different
 * categories of the rating are and-linked together, ie
 * a show must be better (or worse) in _all_ categories
 * to be accepted.
 *
 * @author uzi
 */
public class TVRaterFilterAllCategories extends PluginsFilterComponent {
    /**
     * Localizer for messages, labels etc
     */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(TVRaterFilterAllCategories.class);

    /**
     * the values which will be accepted for the different categories: overall - action - fun - erotic - tension - entitlement.
     * the initial values will be replaced by the values from the different combo boxes. the indices are defined as constants.
     */
    private byte[] mAcceptValues = {3, 3, 3, 3, 3, 3};

    private static final byte OVERALL_INDEX = 0;
    private static final byte ACTION_INDEX = 1;
    private static final byte FUN_INDEX = 2;
    private static final byte EROTIC_INDEX = 3;
    private static final byte TENSION_INDEX = 4;
    private static final byte ENTITLEMENT_INDEX = 5;

    /**
     * Show programs that are better or worse than the <code>_acceptValues</code>.
     */
    private boolean mBest = true;

    /**
     * Settings: show only good or bad programs. linked to <code>_best</code>.
     */
    private JComboBox mBetterCombo;

    /**
     * Settings: show only shows, that are better/worse than or equal this overall rating. linked to <code>_acceptValues[0]</code>.
     */
    private JComboBox mOverallRatingBox;

    /**
     * Settings: show only shows, that are better/worse than or equal this action rating. linked to <code>_acceptValues[1]</code>.
     */
    private JComboBox mActionRatingBox;

    /**
     * Settings: show only shows, that are better/worse than or equal this fun rating. linked to <code>_acceptValues[2]</code>.
     */
    private JComboBox mFunRatingBox;

    /**
     * Settings: show only shows, that are better/worse than or equal this erotic rating. linked to <code>_acceptValues[3]</code>.
     */
    private JComboBox mEroticRatingBox;

    /**
     * Settings: show only shows, that are better/worse than or equal this tension rating. linked to <code>_acceptValues[4]</code>.
     */
    private JComboBox mTensionRatingBox;

    /**
     * Settings: show only shows, that are better/worse than or equal this entitlement rating. linked to <code>_acceptValues[5]</code>.
     */
    private JComboBox mEntitlementRatingBox;





    /**
     * @return Name of the Filter
     */
    public String getUserPresentableClassName()
    {
        return mLocalizer.msg("PresentableName", "Filter ratings");
    }

    /**
     * @return Version
     */
    public int getVersion()
    {
        return 1;
    }



    /**
     * Accept Programs if rating fits
     *
     * @param program check this program
     * @return true, if rating fits
     */
    public boolean accept(Program program)
    {
        Rating rating = TVRaterPlugin.getInstance().getRating(program);

        if (rating == null)
        {
            return false;
        }

        if (mBest &&
            rating.getOverallRating() >= mAcceptValues[TVRaterFilterAllCategories.OVERALL_INDEX] &&
            rating.getActionRating() >= mAcceptValues[TVRaterFilterAllCategories.ACTION_INDEX] &&
            rating.getFunRating() >= mAcceptValues[TVRaterFilterAllCategories.FUN_INDEX] &&
            rating.getEroticRating() >= mAcceptValues[TVRaterFilterAllCategories.EROTIC_INDEX] &&
            rating.getTensionRating() >= mAcceptValues[TVRaterFilterAllCategories.TENSION_INDEX] &&
            rating.getEntitlementRating() >= mAcceptValues[TVRaterFilterAllCategories.ENTITLEMENT_INDEX])
        {
            return true;
        }
        else if (!mBest &&
            rating.getOverallRating() <= mAcceptValues[TVRaterFilterAllCategories.OVERALL_INDEX] &&
            rating.getActionRating() <= mAcceptValues[TVRaterFilterAllCategories.ACTION_INDEX] &&
            rating.getFunRating() <= mAcceptValues[TVRaterFilterAllCategories.FUN_INDEX] &&
            rating.getEroticRating() <= mAcceptValues[TVRaterFilterAllCategories.EROTIC_INDEX] &&
            rating.getTensionRating() <= mAcceptValues[TVRaterFilterAllCategories.TENSION_INDEX] &&
            rating.getEntitlementRating() <= mAcceptValues[TVRaterFilterAllCategories.ENTITLEMENT_INDEX])
        {
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
    public void read(ObjectInputStream in, int version)
    throws IOException, ClassNotFoundException
    {
        if (version >= 1)
        {
            mAcceptValues = (byte[]) in.readObject();
            mBest = in.readBoolean();
        }
    }

    /**
     * Store Settings
     * @param out store settings here
     * @throws IOException Exception while storing the data
     */
    public void write(ObjectOutputStream out)
    throws IOException
    {
        out.writeObject(mAcceptValues);
        out.writeBoolean(mBest);
    }

    @Override
    public JPanel getSettingsPanel()
    {
        JPanel panel = new JPanel(new FormLayout("pref, 3dlu, pref", "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));
        CellConstraints cc = new CellConstraints();

        //better-or-worse-combobox + label
        panel.add(new JLabel(mLocalizer.msg("programsThatAre", "Programs that are")), cc.xy(1,1));
        mBetterCombo = new JComboBox(new String[]{
            mLocalizer.msg("betterOrEqual", "better than or equal to"),
            mLocalizer.msg("worseOrEqual", "inferior or equal to")
        });
        if (mBest)
        {
            mBetterCombo.setSelectedIndex(0);
        }
        else
        {
            mBetterCombo.setSelectedIndex(1);
        }
        panel.add(mBetterCombo, cc.xy(3,1));

        //the rating for the RatingComboBox
        Rating filterRating = new Rating("");
        filterRating.setOverallRating(mAcceptValues[TVRaterFilterAllCategories.OVERALL_INDEX]);
        filterRating.setActionRating(mAcceptValues[TVRaterFilterAllCategories.ACTION_INDEX]);
        filterRating.setFunRating(mAcceptValues[TVRaterFilterAllCategories.FUN_INDEX]);
        filterRating.setEroticRating(mAcceptValues[TVRaterFilterAllCategories.EROTIC_INDEX]);
        filterRating.setTensionRating(mAcceptValues[TVRaterFilterAllCategories.TENSION_INDEX]);
        filterRating.setEntitlementRating(mAcceptValues[TVRaterFilterAllCategories.ENTITLEMENT_INDEX]);

        //overall rating box + label
        panel.add(new JLabel(mLocalizer.msg("overall", "Overall") + ":"), cc.xy(1,3));
        mOverallRatingBox = new RatingComboBox(filterRating, Rating.OVERALL_RATING_KEY);
        panel.add(mOverallRatingBox, cc.xy(3,3));

        //action rating box + label
        panel.add(new JLabel(mLocalizer.msg("action", "Action") + ":"), cc.xy(1,5));
        mActionRatingBox = new RatingComboBox(filterRating, Rating.ACTION_RATING_KEY);
        panel.add(mActionRatingBox, cc.xy(3,5));

        //fun rating box + label
        panel.add(new JLabel(mLocalizer.msg("fun", "Fun") + ":"), cc.xy(1,7));
        mFunRatingBox = new RatingComboBox(filterRating, Rating.FUN_RATING_KEY);
        panel.add(mFunRatingBox, cc.xy(3,7));

        //erotic rating box + label
        panel.add(new JLabel(mLocalizer.msg("erotic", "Erotic") + ":"), cc.xy(1,9));
        mEroticRatingBox = new RatingComboBox(filterRating, Rating.EROTIC_RATING_KEY);
        panel.add(mEroticRatingBox, cc.xy(3,9));

        //tension rating box + label
        panel.add(new JLabel(mLocalizer.msg("tension", "Tension") + ":"), cc.xy(1,11));
        mTensionRatingBox = new RatingComboBox(filterRating, Rating.TENSION_RATING_KEY);
        panel.add(mTensionRatingBox, cc.xy(3,11));

        //entitlement rating box + label
        panel.add(new JLabel(mLocalizer.msg("entitlement", "Level") + ":"), cc.xy(1,13));
        mEntitlementRatingBox = new RatingComboBox(filterRating, Rating.ENTITLEMENT_RATING_KEY);
        panel.add(mEntitlementRatingBox, cc.xy(3,13));

        return panel;
    }


    @Override
    public void saveSettings()
    {
        mBest = mBetterCombo.getSelectedIndex() < 1;
        mAcceptValues[TVRaterFilterAllCategories.OVERALL_INDEX] = (byte) mOverallRatingBox.getSelectedIndex();
        mAcceptValues[TVRaterFilterAllCategories.ACTION_INDEX] = (byte) mActionRatingBox.getSelectedIndex();
        mAcceptValues[TVRaterFilterAllCategories.FUN_INDEX] = (byte) mFunRatingBox.getSelectedIndex();
        mAcceptValues[TVRaterFilterAllCategories.EROTIC_INDEX] = (byte) mEroticRatingBox.getSelectedIndex();
        mAcceptValues[TVRaterFilterAllCategories.TENSION_INDEX] = (byte) mTensionRatingBox.getSelectedIndex();
        mAcceptValues[TVRaterFilterAllCategories.ENTITLEMENT_INDEX] = (byte) mEntitlementRatingBox.getSelectedIndex();
    }
}