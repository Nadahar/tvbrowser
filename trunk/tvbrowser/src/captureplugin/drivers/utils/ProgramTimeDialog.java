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
 *     $Date: 2007-01-13 22:08:02 +0100 (Sa, 13 Jan 2007) $
 *   $Author: ds10 $
 * $Revision: 3016 $
 */
package captureplugin.drivers.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import util.ui.Localizer;
import util.ui.ProgramPanel;
import captureplugin.CapturePlugin;


/**
 * A Dialog that lets the user choose start/end time of the
 * recording
 */
public class ProgramTimeDialog extends JDialog {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramTimeDialog.class);

    /** ProgramTime*/
    private ProgramTime mPrgTime;
    /** Start-Time*/
    private TimeDateChooserPanel mStart;
    /** End-Time */
    private TimeDateChooserPanel mEnd;
    /** Title-Input */
    private JTextField mTitle;
    
    /**
     * Crate the Dialog
     * @param parent Parent-Frame
     * @param prgTime ProgramTime
     * @param editTitle is the Title editable?
     */
    public ProgramTimeDialog(JDialog parent, ProgramTime prgTime, boolean editTitle) {
        super(parent, true);
        mPrgTime = prgTime;
        createGui(editTitle);
    }


    public ProgramTimeDialog(JDialog parent, ProgramTime time, boolean editTitle, String additonalText, JComponent additionalComponent) {
        super(parent, true);
        mPrgTime = time;
        createGui(editTitle, additonalText, additionalComponent);
    }


    /**
     * Crate the Dialog
     * @param parent Parent-Frame
     * @param prgTime ProgramTime
     * @param editTitle is the Title editable?
     */
    public ProgramTimeDialog(JFrame parent, ProgramTime prgTime, boolean editTitle) {
        super(parent, true);
        mPrgTime = prgTime;
        createGui(editTitle);
    }

    public ProgramTimeDialog(JFrame parent, ProgramTime time, boolean editTitle, String additonalText, JComponent additionalComponent) {
        super(parent, true);
        mPrgTime = time;
        createGui(editTitle, additonalText, additionalComponent);
    }

    /**
     * Create the GUI
     * @param titleEditable is the Title editable ?
     */
    private void createGui(final boolean titleEditable) {
        createGui(titleEditable, null, null);
    }


    /**
     * Create the GUI
     * @param titleEditable is the Title editable ?
     */
    private void createGui(final boolean titleEditable, final String additonalText, final JComponent additionalComponent) {
        setTitle(mLocalizer.msg("SetTime","Set Time"));
        
        JPanel panel = (JPanel)getContentPane();
        panel.setLayout(new BorderLayout());
        
        mStart = new TimeDateChooserPanel(mPrgTime.getStart());
        mStart.setBorder(BorderFactory.createTitledBorder(
                mLocalizer.msg("StartTime","Start-Time")));
        
        mEnd =  new TimeDateChooserPanel(mPrgTime.getEnd());
        mEnd.setBorder(BorderFactory.createTitledBorder(
                mLocalizer.msg("EndTime","End-Time")));
        
        if (mPrgTime.getProgram().getLength() <= 0) {
          mEnd.setSpinnerBackground(new Color(255, 153, 153));
        }

        JPanel center = new JPanel();
        center.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.weightx = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 0.5;
        
        JPanel namePanel = new JPanel();
        namePanel.setBorder(BorderFactory.createTitledBorder(Localizer.getLocalization(Localizer.I18N_PROGRAM)));
        namePanel.setLayout(new BorderLayout());

        ProgramPanel p = new ProgramPanel(mPrgTime.getProgram());
        p.setToolTipText("");
        p.addPluginContextMenuMouseListener(CapturePlugin.getInstance());
        
        namePanel.add(p);
        
        center.add(namePanel, c);

        if (titleEditable) {
            JPanel titlePanel = new JPanel(new BorderLayout());

            mTitle = new JTextField(mPrgTime.getTitle());

            titlePanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("Title", "Title")));
            titlePanel.add(mTitle, BorderLayout.CENTER);

            center.add(titlePanel, c);
        }

        center.add(mStart, c);
        center.add(mEnd, c);

        if (additonalText != null) {
            JPanel additionalPanel = new JPanel();
            additionalPanel.setBorder(BorderFactory.createTitledBorder(additonalText));
            additionalPanel.add(additionalComponent);
            center.add(additionalPanel, c);
        }


        panel.add(center, BorderLayout.CENTER);
        
        JPanel btPanel = new JPanel();
        
        btPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
       
        JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
        
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mPrgTime.setStart(mStart.getDate());
                mPrgTime.setEnd(mEnd.getDate());
                if (titleEditable) {
                    mPrgTime.setTitle(mTitle.getText());
                }
                setVisible(false);
            }
        });
        
        btPanel.add(ok);
        
        JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
        
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mPrgTime = null;
                setVisible(false);
            }
            
        });

        
        btPanel.add(cancel);

        getRootPane().setDefaultButton(ok);
        
        panel.add(btPanel, BorderLayout.SOUTH);
        
        pack();
    }
    
    
    /**
     * Get the ProgramTime
     * @return ProgramTime
     */
    public ProgramTime getPrgTime() {
        return mPrgTime;
    }

}