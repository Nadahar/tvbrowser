package captureplugin.drivers.thetubedriver;

import util.ui.Localizer;
import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverIf;
import captureplugin.drivers.simpledevice.SimpleDevice;

/**
 * Driver for TheTube
 */
public class TheTubeDriver implements DriverIf {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(TheTubeDriver.class);

    public String getDriverName() {
        return mLocalizer.msg("name", "TheTube Driver");
    }

    public String getDriverDesc() {
        return mLocalizer.msg("desc", "Description");
    }

    public DeviceIf createDevice(String name) {
        return new SimpleDevice(new TheTubeConnection(), this, name);
     }
}
