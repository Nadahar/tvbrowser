/*
 * Created on 11.04.2004
 */
package listviewplugin;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.VerticalToolBar;
import util.ui.ImageUtilities;
import util.ui.UiUtilities;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * Creates a Dialog with a List of Programs
 * 
 * @author bodo
 */
public class ListViewDialog extends JDialog {

    /** The localizer for the time-phrases. */
    public static final util.ui.Localizer mTimeLocalizer = util.ui.Localizer
            .getLocalizerFor(VerticalToolBar.class);

    /** The localizer used by this class. */
    private static final util.ui.Localizer mLocalizer = util.ui.Localizer
            .getLocalizerFor(ListViewDialog.class);

    /** JList component */
    private JList mProgramJList;

    /** Vector with Programs */
    private Vector mProgramList;
    
    /** Runs at ... */
    private JRadioButton mRuns = new JRadioButton(mLocalizer.msg("runs", "Running"));
    /** Runs on ... */
    private JRadioButton mOn = new JRadioButton(mLocalizer.msg("on", "On"));
    /** Date-Select for mOn */
    private JComboBox mDate;
    /** Time-Spinner for mOn */
    private JSpinner mTimeSpinner = new JSpinner(new SpinnerDateModel());
    /** Text for mRuns */
    final static String[] TIMETEXT = { mTimeLocalizer.msg("button.now", "Now"),
            mLocalizer.msg("15min", "in 15 minutes"),
            mLocalizer.msg("30min", "in 30 minutes"),
            mTimeLocalizer.msg("button.early", "Early"),
            mTimeLocalizer.msg("button.midday", "Midday"),
            mTimeLocalizer.msg("button.afternoon", "Afternoon"),
            mTimeLocalizer.msg("button.evening", "Evening") };
    /** Select for mRuns */
    private JComboBox mBox = new JComboBox(TIMETEXT);
    
    /** Plugin that created the Dialog */
    private Plugin mPlugin;

    /**
     * Creates the Dialog
     * 
     * @param frame Frame for modal
     * @param plugin Plugin for reference
     */
    public ListViewDialog(Frame frame, Plugin plugin) {
        super(frame, true);
        setTitle(mLocalizer.msg("viewList", "View List:"));
        mPlugin = plugin;
        generateList(new Date(), getCurrentTime());
        createGUI();
    }

    /**
     * Returns the current Time in minutes
     * 
     * @return Time in minutes
     */
    private int getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    }

    /**
     * Genereates the List of Programs
     */
    private void generateList(Date date, int time) {
        mProgramList = new Vector();

        // If Time > 24 try next Day
        if (time > 60 * 24) {
            date = date.addDays(1);
            time -= 60 * 24;
        }

        Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();

        for (int i = 0; i < channels.length; i++) {
            if (!fillList(date, time, channels[i])) {
                // No Program added, try yesterday
                fillList(date.addDays(-1), time + 60 * 24, channels[i]);
            }
        }

    }

    /**
     * Fills a List
     * 
     * @param date Date
     * @param channels Channel
     * @return added a Program
     */
    private boolean fillList(Date date, int time, Channel channel) {
        boolean added = false;

        Iterator it = Plugin.getPluginManager().getChannelDayProgram(date,
                channel);
        while ((it != null) && (it.hasNext())) {
            Program program = (Program) it.next();
            
            int start = program.getStartTime();
            int ende = program.getStartTime() + program.getLength();
            
            if ((start <= time) && (ende > time)) {
                mProgramList.add(program);
                added = true;
            }
        }
        return added;
    }

    /**
     * Updates the List
     */
    private void updateList(Date date, int time) {
        generateList(date, time);
        mProgramJList.setListData(mProgramList);
    }

    /**
     * Calculates the Time based on the current selection
     * 
     * @param selectedIndex # of selection
     * @return calculated Time
     */
    private int calcTimeForSelection(int selectedIndex) {
        int time = getCurrentTime();

        switch (selectedIndex) {
        case 1:
            time += 15;
            break;

        case 2:
            time += 30;
            break;

        case 3:
            time = Settings.propEarlyTime.getInt();
            break;

        case 4:
            time = Settings.propMiddayTime.getInt();
            break;

        case 5:
            time = Settings.propAfternoonTime.getInt();
            break;

        case 6:
            time = Settings.propEveningTime.getInt();
            break;

        default:
            break;
        }

        return time;
    }

    /**
     * Creates the GUI
     * 
     * @param plugin
     */
    private void createGUI() {
        JPanel content = (JPanel) this.getContentPane();
        content.setLayout(new BorderLayout());
        content.setBorder(UiUtilities.DIALOG_BORDER);

        mBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int time = calcTimeForSelection(mBox.getSelectedIndex());
                updateList(new Date(), time);
            }

        });
        
        ButtonGroup group = new ButtonGroup();
        group.add(mRuns);
        group.add(mOn);

        JButton refreshButton = new JButton(new ImageIcon(ImageUtilities
                .createImage("imgs/Refresh16.gif")));

        JPanel datetimeselect = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0));

        Vector dates = new Vector();

        for (int i = 0; i < 14; i++) {
            dates.add(Date.getCurrentDate().addDays(i));
        }
        mDate = new JComboBox(dates);

        datetimeselect.add(mDate);

        datetimeselect.add(new JLabel(" " + mLocalizer.msg("at", "at") + " "));

        String timePattern = mLocalizer.msg("timePattern", "HH:mm");

        mTimeSpinner
                .setEditor(new JSpinner.DateEditor(mTimeSpinner, timePattern));

        datetimeselect.add(mTimeSpinner);

        // Event-Handler

        mRuns.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                refreshView();
            }

        });

        mOn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                refreshView();
            }

        });

        mDate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                refreshView();

            }
        });

        mTimeSpinner.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent arg0) {
                refreshView();

            }
        });

        refreshButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                refreshView();

            }
        });

        mRuns.setSelected(true);
        mDate.setEnabled(false);
        mTimeSpinner.setEnabled(false);


        // Upper Panel
        
        JPanel topPanel = new JPanel(new GridBagLayout());
        
        topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridheight = 2;
        c.weighty = 1.0;
        
        c.insets = new Insets(0, 0, 5, 0);

        topPanel.add(refreshButton, c);

        c.insets = new Insets(0, 5, 5, 0);
        c.gridheight = 1;
        c.weighty = 0;

        topPanel.add(mBox, c);
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        
        topPanel.add(mRuns, c);

        c.gridwidth = 1;
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        
        topPanel.add(datetimeselect, c);
        
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        topPanel.add(mOn, c);
        
        content.add(topPanel, BorderLayout.NORTH);

        // Rest of the GUI
        
        mProgramJList = new JList(mProgramList);

        mProgramJList.setCellRenderer(new ProgramCellRenderer());

        mProgramJList.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                mouseClickedOnList(e);
            }
        });

        JScrollPane scroll = new JScrollPane(mProgramJList);

        content.add(scroll, BorderLayout.CENTER);

        JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 5));
        content.add(buttonPn, BorderLayout.SOUTH);

        JButton closeButton = new JButton(mLocalizer.msg("close", "Close"));
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
        buttonPn.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

    }

    /**
     * Refresh the List with current settings
     */
    private void refreshView() {

        mBox.setEnabled(mRuns.isSelected());
        mDate.setEnabled(mOn.isSelected());
        mTimeSpinner.setEnabled(mOn.isSelected());

        if (mRuns.isSelected()) {
            int time = calcTimeForSelection(mBox.getSelectedIndex());
            updateList(new Date(), time);
        } else {
            java.util.Date startTime = (java.util.Date) mTimeSpinner.getValue();
            Calendar cal = Calendar.getInstance();
            cal.setTime(startTime);
            int minutes = cal.get(Calendar.HOUR_OF_DAY) * 60
                    + cal.get(Calendar.MINUTE);
            updateList((Date) mDate.getSelectedItem(), minutes);
        }
    }

    /**
     * Called when a Mouse-Event occurs
     * 
     * @param e Event
     */
    private void mouseClickedOnList(MouseEvent e) {

        if (SwingUtilities.isRightMouseButton(e)) {
            int inx = mProgramJList.locationToIndex(e.getPoint());
            Program p = (Program) mProgramJList.getModel().getElementAt(inx);
            mProgramJList.setSelectedIndex(inx);
            JPopupMenu menu = devplugin.Plugin.getPluginManager()
                    .createPluginContextMenu(p);
            menu.show(mProgramJList, e.getX() - 15, e.getY() - 15);
        } else if (SwingUtilities.isLeftMouseButton(e)
                && (e.getClickCount() == 2)) {
            int inx = mProgramJList.locationToIndex(e.getPoint());
            Program p = (Program) mProgramJList.getModel().getElementAt(inx);

            devplugin.Plugin.getPluginManager().handleProgramDoubleClick(p);
        }
    }
}