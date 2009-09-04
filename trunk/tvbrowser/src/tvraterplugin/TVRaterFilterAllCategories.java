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
     * the values wich will be accepted for the different categories: overall - action - fun - erotic - tension - entitlement.
     * the initial values will be replaced by the values from the different combo boxes. the indices are defined as constants.
     */
    private byte[] _acceptValues = {3, 3, 3, 3, 3, 3};
    
    private static final byte OVERALL_INDEX = 0;
    private static final byte ACTION_INDEX = 1;
    private static final byte FUN_INDEX = 2;
    private static final byte EROTIC_INDEX = 3;
    private static final byte TENSION_INDEX = 4;
    private static final byte ENTITLEMENT_INDEX = 5;
    
    /** 
     * Show programs that are better or worse than the <code>_acceptValues</code>.
     */
    private boolean _best = true;

    /** 
     * Settings: show only good or bad programs. linked to <code>_best</code>.
     */
    private JComboBox _betterCombo;
    
    /**
     * Settings: show only shows, that are better/worse than or equal this overall rating. linked to <code>_acceptValues[0]</code>.
     */
    private JComboBox _overallRatingBox;
    
    /**
     * Settings: show only shows, that are better/worse than or equal this action rating. linked to <code>_acceptValues[1]</code>.
     */
    private JComboBox _actionRatingBox;
    
    /**
     * Settings: show only shows, that are better/worse than or equal this fun rating. linked to <code>_acceptValues[2]</code>.
     */
    private JComboBox _funRatingBox;
    
    /**
     * Settings: show only shows, that are better/worse than or equal this erotic rating. linked to <code>_acceptValues[3]</code>.
     */
    private JComboBox _eroticRatingBox;
    
    /**
     * Settings: show only shows, that are better/worse than or equal this tension rating. linked to <code>_acceptValues[4]</code>.
     */
    private JComboBox _tensionRatingBox;
    
    /**
     * Settings: show only shows, that are better/worse than or equal this entitlement rating. linked to <code>_acceptValues[5]</code>.
     */
    private JComboBox _entitlementRatingBox;
    
    
    
    

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

        if (_best && 
            rating.getOverallRating() >= _acceptValues[TVRaterFilterAllCategories.OVERALL_INDEX] &&
            rating.getActionRating() >= _acceptValues[TVRaterFilterAllCategories.ACTION_INDEX] &&
            rating.getFunRating() >= _acceptValues[TVRaterFilterAllCategories.FUN_INDEX] &&
            rating.getEroticRating() >= _acceptValues[TVRaterFilterAllCategories.EROTIC_INDEX] &&
            rating.getTensionRating() >= _acceptValues[TVRaterFilterAllCategories.TENSION_INDEX] &&
            rating.getEntitlementRating() >= _acceptValues[TVRaterFilterAllCategories.ENTITLEMENT_INDEX]) 
        {
            return true;
        } 
        else if (!_best && 
            rating.getOverallRating() <= _acceptValues[TVRaterFilterAllCategories.OVERALL_INDEX] &&
            rating.getActionRating() <= _acceptValues[TVRaterFilterAllCategories.ACTION_INDEX] &&
            rating.getFunRating() <= _acceptValues[TVRaterFilterAllCategories.FUN_INDEX] &&
            rating.getEroticRating() <= _acceptValues[TVRaterFilterAllCategories.EROTIC_INDEX] &&
            rating.getTensionRating() <= _acceptValues[TVRaterFilterAllCategories.TENSION_INDEX] &&
            rating.getEntitlementRating() <= _acceptValues[TVRaterFilterAllCategories.ENTITLEMENT_INDEX]) 
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
            _acceptValues = (byte[]) in.readObject();
            _best = in.readBoolean();
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
        out.writeObject(_acceptValues);
        out.writeBoolean(_best);
    }

    @Override
    public JPanel getSettingsPanel() 
    {
        JPanel panel = new JPanel(new FormLayout("pref, 3dlu, pref", "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));
        CellConstraints cc = new CellConstraints();

        //better-or-worse-combobox + label
        panel.add(new JLabel(mLocalizer.msg("programsThatAre", "Programs that are")), cc.xy(1,1));
        _betterCombo = new JComboBox(new String[]{
            mLocalizer.msg("betterOrEqual", "better than or equal to"),
            mLocalizer.msg("worseOrEqual", "inferior or equal to")
        });
        if (_best)
        {
            _betterCombo.setSelectedIndex(0);
        }
        else
        {
            _betterCombo.setSelectedIndex(1);
        }
        panel.add(_betterCombo, cc.xy(3,1));

        //the rating for the RatingComboBox
        Rating filterRating = new Rating("");
        filterRating.setOverallRating(_acceptValues[TVRaterFilterAllCategories.OVERALL_INDEX]);
        filterRating.setActionRating(_acceptValues[TVRaterFilterAllCategories.ACTION_INDEX]);
        filterRating.setFunRating(_acceptValues[TVRaterFilterAllCategories.FUN_INDEX]);
        filterRating.setEroticRating(_acceptValues[TVRaterFilterAllCategories.EROTIC_INDEX]);
        filterRating.setTensionRating(_acceptValues[TVRaterFilterAllCategories.TENSION_INDEX]);
        filterRating.setEntitlementRating(_acceptValues[TVRaterFilterAllCategories.ENTITLEMENT_INDEX]);
        
        //overall rating box + label
        panel.add(new JLabel(mLocalizer.msg("overall", "Overall") + ":"), cc.xy(1,3));
        _overallRatingBox = new RatingComboBox(filterRating, Rating.OVERALL_RATING_KEY);
        panel.add(_overallRatingBox, cc.xy(3,3));
        
        //action rating box + label
        panel.add(new JLabel(mLocalizer.msg("action", "Action") + ":"), cc.xy(1,5));
        _actionRatingBox = new RatingComboBox(filterRating, Rating.ACTION_RATING_KEY);
        panel.add(_actionRatingBox, cc.xy(3,5));
        
        //fun rating box + label
        panel.add(new JLabel(mLocalizer.msg("fun", "Fun") + ":"), cc.xy(1,7));
        _funRatingBox = new RatingComboBox(filterRating, Rating.FUN_RATING_KEY);
        panel.add(_funRatingBox, cc.xy(3,7));
        
        //erotic rating box + label
        panel.add(new JLabel(mLocalizer.msg("erotic", "Erotic") + ":"), cc.xy(1,9));
        _eroticRatingBox = new RatingComboBox(filterRating, Rating.EROTIC_RATING_KEY);
        panel.add(_eroticRatingBox, cc.xy(3,9));
        
        //tension rating box + label
        panel.add(new JLabel(mLocalizer.msg("tension", "Tension") + ":"), cc.xy(1,11));
        _tensionRatingBox = new RatingComboBox(filterRating, Rating.TENSION_RATING_KEY);
        panel.add(_tensionRatingBox, cc.xy(3,11));
        
        //entitlement rating box + label
        panel.add(new JLabel(mLocalizer.msg("entitlement", "Level") + ":"), cc.xy(1,13));
        _entitlementRatingBox = new RatingComboBox(filterRating, Rating.ENTITLEMENT_RATING_KEY);
        panel.add(_entitlementRatingBox, cc.xy(3,13));

        return panel;
    }

    
    @Override
    public void saveSettings()
    {
        _best = _betterCombo.getSelectedIndex() < 1;
        _acceptValues[TVRaterFilterAllCategories.OVERALL_INDEX] = (byte) _overallRatingBox.getSelectedIndex();
        _acceptValues[TVRaterFilterAllCategories.ACTION_INDEX] = (byte) _actionRatingBox.getSelectedIndex();
        _acceptValues[TVRaterFilterAllCategories.FUN_INDEX] = (byte) _funRatingBox.getSelectedIndex();
        _acceptValues[TVRaterFilterAllCategories.EROTIC_INDEX] = (byte) _eroticRatingBox.getSelectedIndex();
        _acceptValues[TVRaterFilterAllCategories.TENSION_INDEX] = (byte) _tensionRatingBox.getSelectedIndex();
        _acceptValues[TVRaterFilterAllCategories.ENTITLEMENT_INDEX] = (byte) _entitlementRatingBox.getSelectedIndex();
    }
}