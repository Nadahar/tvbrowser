package calendarexportplugin.exporter;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import util.exc.ErrorHandler;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import calendarexportplugin.CalendarExportPlugin;
import calendarexportplugin.CalendarExportSettings;

import com.google.gdata.client.GoogleService;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

/**
 * Settings for the Google Exporter
 */
public class GoogleSettingsDialog extends JDialog  implements WindowClosingIf {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GoogleSettingsDialog.class);
  /**
   * Translator
   */
  private static final Localizer mErrorLocalizer = Localizer.getLocalizerFor(GoogleExporter.class);

  /** Which Button was pressed ?*/
  private int mReturnValue = JOptionPane.CANCEL_OPTION;

  private JButton mOkButton;
  private JComboBox mCalendarChooser;
  private JCheckBox mUseSMS;
  private JCheckBox mUseEMail;
  private JCheckBox mUseAlert;
  private JComboBox mRemindMinutes;
  private JCheckBox mReminderCheckBox;
  private JLabel mReminderText;
  private JCheckBox mReminderStore;

  public GoogleSettingsDialog(Window owner, CalendarExportSettings settings, String password) {
    super(owner);
    setModal(true);
    createGui(settings, password);
  }

  private void createGui(final CalendarExportSettings settings, final String password) {
    setTitle(mLocalizer.msg("title", "Google Calendar Settings"));

    UiUtilities.registerForClosing(this);

    JPanel content = (JPanel)getContentPane();
    content.setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();

    content.setLayout(new FormLayout("5dlu, 15dlu, fill:pref:grow, 3dlu, 100dlu",
            "pref, 3dlu,pref, 3dlu,pref, 3dlu,pref, 3dlu,pref, 3dlu,pref, 3dlu,pref, 3dlu,pref, fill:3dlu:grow ,pref, 3dlu,pref"));

    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("calendar", "Calendar")), cc.xyw(1,1,5));

    content.add(new JLabel(mLocalizer.msg("select", "Select Calendar:")), cc.xyw(2,3,2));
    mCalendarChooser = new JComboBox(new String[] {mLocalizer.msg("loading", "Loading list...")});
    mCalendarChooser.setEnabled(false);
    content.add(mCalendarChooser, cc.xy(5,3));

    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("reminder", "Reminder")), cc.xyw(1,5,5));

    mReminderCheckBox = new JCheckBox(mLocalizer.msg("reminderCheckbox", "Reminder"));
    mReminderCheckBox.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        refreshEnabledElements();
      }
    });

    content.add(mReminderCheckBox, cc.xyw(2,7,4));

    mReminderText = new JLabel(mLocalizer.msg("minutesBefore", "Remind how many minutes before?"));
    content.add(mReminderText, cc.xy(3,9));

    Vector<GoogleComboboxItem> v = new Vector<GoogleComboboxItem>();
    v.add(new GoogleComboboxItem(    5, mLocalizer.msg("5_minutes", "5 minutes")));
    v.add(new GoogleComboboxItem(   10, mLocalizer.msg("10_minutes","10 minutes")));
    v.add(new GoogleComboboxItem(   15, mLocalizer.msg("15_minutes","15 minutes")));
    v.add(new GoogleComboboxItem(   20, mLocalizer.msg("20_minutes","20 minutes")));
    v.add(new GoogleComboboxItem(   25, mLocalizer.msg("25_minutes","25 minutes")));
    v.add(new GoogleComboboxItem(   30, mLocalizer.msg("30_minutes","30 minutes")));
    v.add(new GoogleComboboxItem(   45, mLocalizer.msg("45_minutes", "45 minutes")));
    v.add(new GoogleComboboxItem(   60, mLocalizer.msg("60_minutes","1 hour")));
    v.add(new GoogleComboboxItem(  120, mLocalizer.msg("120_minutes","2 hours")));
    v.add(new GoogleComboboxItem(  180, mLocalizer.msg("180_minutes","3 hours")));
    v.add(new GoogleComboboxItem(  720, mLocalizer.msg("720_minutes","12 hours")));
    v.add(new GoogleComboboxItem( 1440, mLocalizer.msg("1440_minutes","1 day")));
    v.add(new GoogleComboboxItem( 2880, mLocalizer.msg("2880_minutes","2 days")));
    v.add(new GoogleComboboxItem(10080, mLocalizer.msg("10080_minutes","1 week")));
    mRemindMinutes = new JComboBox(v);

    String minutes = settings.getExporterProperty(GoogleExporter.REMINDER_MINUTES);
    for (GoogleComboboxItem item : v) {
      if (item.getKey().equals(minutes)) {
        mRemindMinutes.setSelectedItem(item);
      }
    }

    content.add(mRemindMinutes, cc.xy(5, 9));

    mUseSMS   = new JCheckBox(mLocalizer.msg("useSMS", "Use SMS"));
    mUseEMail = new JCheckBox(mLocalizer.msg("useEMail", "Use EMail"));
    mUseAlert = new JCheckBox(mLocalizer.msg("useAlert", "Use Alert"));

    content.add(mUseSMS,   cc.xyw(3,11,3));
    content.add(mUseEMail, cc.xyw(3,13,3));
    content.add(mUseAlert, cc.xyw(3,15,3));

    mReminderStore = new JCheckBox(mLocalizer.msg("storeSettings", "Use these settings and don't ask again"));
    content.add(mReminderStore, cc.xyw(2,17,4));


    ButtonBarBuilder2 builder = new ButtonBarBuilder2();
    builder.addGlue();

    mOkButton = new JButton(Localizer.getLocalization(Localizer.I18N_OK));

    mOkButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed(settings);
      }
    });

    getRootPane().setDefaultButton(mOkButton);

    JButton cancel = new JButton (Localizer.getLocalization(Localizer.I18N_CANCEL));

    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    builder.addButton(new JButton[] {mOkButton, cancel});

    content.add(builder.getPanel(), cc.xyw(1, 19, 5));

    mOkButton.setEnabled(false);

    loadValues(settings);
    refreshEnabledElements();

    new Thread(new Runnable() {
      public void run() {
        try {
          fetchCalendarList(settings, password);
        } catch (AuthenticationException e) {
          loadingFailed();
          ErrorHandler.handle(mErrorLocalizer.msg("loginFailure", "Problems during login to service.\nMaybe bad username or password?"), e);
          settings.setExporterProperty(GoogleExporter.STORE_PASSWORD, false);
        } catch (Exception e) {
          loadingFailed();
          ErrorHandler.handle(mErrorLocalizer.msg("commError", "Error while communicating with Google!"), e);
        }
      }
    }, "Fetch calendar list").start();

    setSize(Sizes.dialogUnitXAsPixel(240, this), Sizes.dialogUnitYAsPixel(200, this));
  }

  private void loadValues(CalendarExportSettings settings) {
    mUseAlert.setSelected(settings.getExporterProperty(GoogleExporter.REMINDER_ALERT, false));
    mUseEMail.setSelected(settings.getExporterProperty(GoogleExporter.REMINDER_EMAIL, false));
    mUseSMS.setSelected(settings.getExporterProperty(GoogleExporter.REMINDER_SMS, false));

    mReminderCheckBox.setSelected(settings.getExporterProperty(GoogleExporter.REMINDER, false));
    mReminderStore.setSelected(settings.getExporterProperty(GoogleExporter.STORE_SETTINGS, false));
  }

  private void okPressed(CalendarExportSettings settings) {
    mReturnValue = JOptionPane.OK_OPTION;
    setVisible(false);

    settings.setExporterProperty(GoogleExporter.SELECTED_CALENDAR, ((GoogleComboboxItem)mCalendarChooser.getSelectedItem()).getKey());
    settings.setExporterProperty(GoogleExporter.REMINDER, mReminderCheckBox.isSelected());
    settings.setExporterProperty(GoogleExporter.REMINDER_ALERT, mUseAlert.isSelected());
    settings.setExporterProperty(GoogleExporter.REMINDER_EMAIL, mUseEMail.isSelected());
    settings.setExporterProperty(GoogleExporter.REMINDER_SMS,   mUseSMS.isSelected());
    settings.setExporterProperty(GoogleExporter.REMINDER_MINUTES, ((GoogleComboboxItem)mRemindMinutes.getSelectedItem()).getKey());

    settings.setExporterProperty(GoogleExporter.STORE_SETTINGS,   mReminderStore.isSelected());
  }

  private void loadingFailed() {
    DefaultComboBoxModel model = (DefaultComboBoxModel) mCalendarChooser.getModel();
    model.removeAllElements();
    model.addElement(mLocalizer.msg("errorLoading", "Error while loading calendars"));
  }

  private void refreshEnabledElements() {
    mRemindMinutes.setEnabled(mReminderCheckBox.isSelected());
    mUseAlert.setEnabled(mReminderCheckBox.isSelected());
    mUseSMS.setEnabled(mReminderCheckBox.isSelected());
    mUseEMail.setEnabled(mReminderCheckBox.isSelected());
    mReminderText.setEnabled(mReminderCheckBox.isSelected());
  }

  private void fetchCalendarList(CalendarExportSettings settings, String password) throws IOException, ServiceException {
    GoogleService myService = new GoogleService("cl", "tvbrowser-tvbrowsercalenderplugin-" + CalendarExportPlugin.getInstance().getInfo().getVersion().toString());
    myService.setUserCredentials(settings.getExporterProperty(GoogleExporter.USERNAME).trim(), password);

    // Send the request and print the response
    URL feedUrl = new URL("http://www.google.com/calendar/feeds/default/owncalendars/full");
    CalendarFeed resultFeed = myService.getFeed(feedUrl, CalendarFeed.class);

    DefaultComboBoxModel model = (DefaultComboBoxModel) mCalendarChooser.getModel();
    model.removeAllElements();

    for (int i = 0; i < resultFeed.getEntries().size(); i++) {
      CalendarEntry entry = resultFeed.getEntries().get(i);

      String id = entry.getId();
      id = StringUtils.substringAfterLast(id, "/");

      model.addElement(new GoogleComboboxItem(id, entry.getTitle().getPlainText()));

      if (id.equals(settings.getExporterProperty(GoogleExporter.SELECTED_CALENDAR))) {
        mCalendarChooser.setSelectedIndex(model.getSize() - 1);
      }
    }

    mCalendarChooser.setEnabled(true);

    mOkButton.setEnabled(true);
  }

  /**
   * Show the Dialog
   * @return Which Button was pressed ? (JOptionpane.OK_OPTION / CANCEL_OPTION)
   */
  public int showDialog() {
    UiUtilities.centerAndShow(this);
    return mReturnValue;
  }

  public void close() {
    mReturnValue = JOptionPane.CANCEL_OPTION;
    setVisible(false);
  }

  private static class GoogleComboboxItem {
    private String mKey;
    private String mText;

    public GoogleComboboxItem(String key, String text) {
      mKey = key;
      mText = text;
    }

    public GoogleComboboxItem(int key, String text) {
      mKey = Integer.toString(key);
      mText = text;
    }

    public String getText() {
      return mText;
    }

    public String getKey() {
      return mKey;
    }

    public String toString() {
      return getText();
    }
  }

}