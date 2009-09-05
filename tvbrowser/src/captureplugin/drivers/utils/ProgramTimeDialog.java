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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.ProgramPanel;
import captureplugin.CapturePlugin;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;


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
    private TimeDateSpinner mStart;
    /** End-Time */
    private TimeDateSpinner mEnd;
    /** Title-Input */
    private JTextField mTitle;
    
    /**
     * Crate the Dialog
     * @param parent Parent-Frame
     * @param prgTime ProgramTime
     * @param editTitle is the Title editable?
     */
    public ProgramTimeDialog(Window parent, ProgramTime prgTime, boolean editTitle) {
    super(parent);
    setModal(true);
        mPrgTime = prgTime;
        createGui(editTitle);
    }


    public ProgramTimeDialog(Window parent, ProgramTime time, boolean editTitle,
      String additionalText, JComponent additionalComponent) {
    super(parent);
    setModal(true);
        mPrgTime = time;
        createGui(editTitle, additionalText, additionalComponent);
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
    private void createGui(final boolean titleEditable, final String additionalText, final JComponent additionalComponent) {
        setTitle(mLocalizer.msg("SetTime","Set Time"));
        
        JPanel content = (JPanel)getContentPane();
        content.setBorder(Borders.DIALOG_BORDER);
        content.setLayout(new BorderLayout());
        
        if (mPrgTime.getProgram().getLength() <= 0) {
          mEnd.setSpinnerBackground(new Color(255, 153, 153));
        }

        EnhancedPanelBuilder panel = new EnhancedPanelBuilder("2dlu, pref:grow,pref");
        
        panel.addParagraph(Localizer.getLocalization(Localizer.I18N_PROGRAM));

        panel.addRow();
        ProgramPanel p = new ProgramPanel(mPrgTime.getProgram());
        p.setToolTipText("");
        p.addPluginContextMenuMouseListener(CapturePlugin.getInstance());
        p.setWidth(200);
        
        CellConstraints cc = new CellConstraints();
        panel.add(p, cc.xyw(2, panel.getRow(), panel.getColumnCount() - 1));

        if (titleEditable) {
          panel.addParagraph(mLocalizer.msg("Title", "Title"));
          mTitle = new JTextField(mPrgTime.getTitle());

          panel.addRow();
          panel.add(mTitle, cc.xyw(2, panel.getRow(), panel.getColumnCount() - 1));
        }

        panel.addParagraph(mLocalizer.msg("Times","Times"));
        panel.addRow();
        panel.add(new JLabel(mLocalizer.msg("StartTime","Start time")), cc.xy(2, panel.getRow()));
        mStart = new TimeDateSpinner(mPrgTime.getStart());
        panel.add(mStart, cc.xy(3, panel.getRow()));
        
        panel.addRow();
        panel.add(new JLabel(mLocalizer.msg("EndTime","End time")), cc.xy(2, panel.getRow()));
        mEnd =  new TimeDateSpinner(mPrgTime.getEnd());
        panel.add(mEnd, cc.xy(3, panel.getRow()));

        if (additionalText != null) {
            panel.addParagraph(additionalText);
            panel.addRow();
            panel.add(additionalComponent, cc.xy(2, panel.getRow()));
        }


        content.add(panel.getPanel(), BorderLayout.CENTER);
        
        ButtonBarBuilder2 btPanel = new ButtonBarBuilder2();
        btPanel.setBorder(Borders.DLU4_BORDER);
        
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
        
        btPanel.addGlue();
        
        JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
        
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mPrgTime = null;
                setVisible(false);
            }
            
        });

        
        btPanel.addButton(ok, cancel);

        getRootPane().setDefaultButton(ok);
        
        content.add(btPanel.getPanel(), BorderLayout.SOUTH);
        
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