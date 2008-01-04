package devplugin;

/**
 * Extends the SettingsTab-Interface to be able to detect the cancelation the input of a user
 *
 * @since 2.7
 */
public interface CancelableSettingsTab extends SettingsTab {

    /**
     * This function gets called if a user presses cancel in the settings dialog
     */
    public void cancel();
}
