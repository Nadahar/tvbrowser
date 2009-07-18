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

package util.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A Color chooser with Alpha-Selection
 */
public class AlphaColorChooser extends JDialog implements ChangeListener {
    /** The localizer for this class. */
    private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(AlphaColorChooser.class);
    
    /**
     * The Sliders
     */
    private JSlider mRedSl, mGreenSl, mBlueSl, mAlphaSl;

    /**
     * The Spinners
     */
    private JSpinner mRedSp, mGreenSp, mBlueSp, mAlphaSp;

    /**
     * The Panel that shows the current color
     */
    private PaintColor mColorPanel;

    /**
     * The current color
     */
    private Color mCurrentColor;

    /**
     * The default color
     */
    private Color mDefaultColor;
    
    /**
     * The standard color
     */
    private Color mStandardColor;

    /**
     * Return-Value (JOptionPane.CANCEL_OPTION / OK_OPTION)
     */
    private int mReturnValue = JOptionPane.CANCEL_OPTION;

  /**
   * Creates the Dialog
   * 
   * @param parent
   *          Parent-Dialog
   * @param title
   *          Title
   * @param color
   *          Color to start with
   * @param stdColor
   *          The standard color
   * @since 3.0
   */
  public AlphaColorChooser(Window parent, String title, Color color,
      Color stdColor) {
    super(parent, title);
    setModal(true);
    mStandardColor = stdColor;
    createGui();
    mDefaultColor = color;
    setColor(color);
  }

  /**
   * Creates the Dialog
   * 
   * @param parent
   *          Parent-Dialog
   * @param title
   *          Title
   * @param color
   *          Color to start with
   * @param stdColor
   *          The standard color
   * @deprecated since 3.0
   */
  public AlphaColorChooser(JDialog parent, String title, Color color,
      Color stdColor) {
    this((Window) parent, title, color, stdColor);
  }

  /**
   * Creates the Dialog
   * 
   * @param parent
   *          Parent-Frame
   * @param title
   *          Title
   * @param color
   *          Color to start with
   * @param stdColor
   *          The standard color
   * @deprecated since 3.0
   */
    public AlphaColorChooser(JFrame parent, String title, Color color, Color stdColor) {
      this((Window) parent, title, color, stdColor);
    }
    
    /**
     * Create the GUI
     */
    public void createGui() {

        JPanel panel = (JPanel) getContentPane();
        panel.setBorder(Borders.DLU4_BORDER);
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 0.5;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;

        JPanel values = new JPanel(new TabLayout(3));
        values.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("Values","Values")+":"));

        values.add(new JLabel(mLocalizer.msg("red","Red")+":"));
        values.add(mRedSl = createSlider());
        values.add(mRedSp = createSpinner());

        values.add(new JLabel(mLocalizer.msg("green","Green")+":"));
        values.add(mGreenSl = createSlider());
        values.add(mGreenSp = createSpinner());

        values.add(new JLabel(mLocalizer.msg("blue","Blue")+":"));
        values.add(mBlueSl = createSlider());
        values.add(mBlueSp = createSpinner());

        values.add(new JLabel(mLocalizer.msg("alpha","Alpha")+":"));
        values.add(mAlphaSl = createSlider());
        values.add(mAlphaSp = createSpinner());

        mRedSl.addChangeListener(this);
        mRedSp.addChangeListener(this);
        mGreenSl.addChangeListener(this);
        mGreenSp.addChangeListener(this);
        mBlueSl.addChangeListener(this);
        mBlueSp.addChangeListener(this);
        mAlphaSl.addChangeListener(this);
        mAlphaSp.addChangeListener(this);

        panel.add(values, c);

        JPanel color = new JPanel(new BorderLayout());
        color.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("Color","Color")+":"));

        mColorPanel = new PaintColor();
        color.add(mColorPanel, BorderLayout.CENTER);
        color.setMinimumSize(new Dimension(100, 100));
        color.setPreferredSize(new Dimension(100, 100));

        c.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(color, c);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0;
        c.insets = new Insets(5, 0, 0, 0);
        c.gridwidth = GridBagConstraints.REMAINDER;

        
        FormLayout layout = new FormLayout("pref, fill:pref:grow, pref, 3dlu, pref", "pref");
        layout.setColumnGroups(new int[][]{{1,3,5}});
        JPanel buttonPanel = new JPanel(layout);
        
        JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
        JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
        ok.addActionListener(new ActionListener() {

          public void actionPerformed(ActionEvent e) {
                stopEditing();
                updateColorPanel();
                mReturnValue = JOptionPane.OK_OPTION;
                setVisible(false);
            }

        });

        cancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                mCurrentColor = mDefaultColor;
                updateColorPanel();
                mReturnValue = JOptionPane.CANCEL_OPTION;
                setVisible(false);
            }

        });
        
        CellConstraints cc = new CellConstraints();
        
        if(mStandardColor != null) {
          JButton def = new JButton(Localizer
          .getLocalization(Localizer.I18N_DEFAULT));
          def.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              setColor(mStandardColor);
              mCurrentColor = mStandardColor;
            }        
          });        
          buttonPanel.add(def, cc.xy(1,1));         
        }
        
        buttonPanel.add(ok, cc.xy(3,1));
        buttonPanel.add(cancel, cc.xy(5,1));

        panel.add(buttonPanel, c);

        updateColorPanel();
        pack();
    }

    /**
     * Creates one Spinner
     * 
     * @return Spinner
     */
    private JSpinner createSpinner() {
        SpinnerModel model = new SpinnerNumberModel(100, 0, 255, 1);

        JSpinner ret = new JSpinner(model);
        ret.setMinimumSize(new Dimension(50, 10));
        ret.setPreferredSize(new Dimension(50, 10));
        return ret;
    }

    /**
     * Creates a Slider
     * 
     * @return Slider
     */
    private JSlider createSlider() {
        JSlider ret = new JSlider(0, 255, 100);
        ret.setMinorTickSpacing(25);
        ret.setPaintTicks(true);
        return ret;
    }

    /**
     * Shows the ColorChooser-Dialog and returns the selected Color. If Cancel
     * is pressed, the default Color is returned
     * 
     * @param parent Parent-Dialog or Frame
     * @param title Title of the Dialog
     * @param color Color to start with
     * @param stdColor The standard color.
     * @return Selected Color
     */
    public static Color showDialog(Component parent, String title, Color color,
      Color stdColor) {

    AlphaColorChooser chooser;

    if (parent instanceof Window) {
      chooser = new AlphaColorChooser((Window) parent, title, color, stdColor);
    } else {
      chooser = new AlphaColorChooser((Window) null, title, color, stdColor);
    }

    UiUtilities.centerAndShow(chooser);

    if (chooser.getReturnValue() == JOptionPane.OK_OPTION) {
      return chooser.getColor();
    }

    return color;
  }

    /**
     * Get the Return Value (JOptionPane.OK_OPTION or CANCEL_OPTION)
     * 
     * @return return value (JOptionPane.OK_OPTION or CANCEL_OPTION)
     */
    public int getReturnValue() {
        return mReturnValue;
    }

    /**
     * Update the ColorPanel
     */
    private void updateColorPanel() {
        int red = mRedSl.getValue();
        int green = mGreenSl.getValue();
        int blue = mBlueSl.getValue();
        int alpha = mAlphaSl.getValue();

        mCurrentColor = new Color(red, green, blue, alpha);
        mColorPanel.setBackground(mCurrentColor);
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == mRedSp) {
            mRedSl.setValue(((Integer) mRedSp.getValue()).intValue());
        } else if (e.getSource() == mGreenSp) {
            mGreenSl.setValue(((Integer) mGreenSp.getValue()).intValue());
        } else if (e.getSource() == mBlueSp) {
            mBlueSl.setValue(((Integer) mBlueSp.getValue()).intValue());
        } else if (e.getSource() == mAlphaSp) {
            mAlphaSl.setValue(((Integer) mAlphaSp.getValue()).intValue());
        } else if (e.getSource() == mRedSl) {
            mRedSp.setValue(mRedSl.getValue());
        } else if (e.getSource() == mGreenSl) {
            mGreenSp.setValue(mGreenSl.getValue());
        } else if (e.getSource() == mBlueSl) {
            mBlueSp.setValue(mBlueSl.getValue());
        } else if (e.getSource() == mAlphaSl) {
            mAlphaSp.setValue(mAlphaSl.getValue());
        }
        updateColorPanel();
    }

    /**
     * Quick Work-Around. Give Focus to Slider, all Spinner loose Focus and
     * try to store their values
     */
    private void stopEditing() {
        mRedSl.grabFocus();
    }
    
    /**
     * Sets the Color in this Dialog
     * 
     * @param color Color
     */
    public void setColor(Color color) {
        if (color != null) {
            mRedSl.setValue(color.getRed());
            mGreenSl.setValue(color.getGreen());
            mBlueSl.setValue(color.getBlue());
            mAlphaSl.setValue(color.getAlpha());
        }
    }

    
    /**
     * Returns the current selected Color
     * 
     * @return current selected Color
     */
    public Color getColor() {
        stopEditing();
        updateColorPanel();
        return mCurrentColor;
    }

    /**
     * Inner Class for the Color-Preview
     */
    private static class PaintColor extends JPanel {

        protected void paintComponent(Graphics g) {

            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.GRAY);
            for (int y = 0; y < getHeight(); y += 15) {

                int start = 0;
                if (y % 2 == 1) {
                    start = 15;
                }

                for (int x = start; x < getWidth(); x += 30) {
                    g.fillRect(x, y, 15, 15);
                }
            }

            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());

        }
    }
}