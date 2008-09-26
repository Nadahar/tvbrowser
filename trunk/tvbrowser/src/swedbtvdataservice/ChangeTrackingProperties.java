package swedbtvdataservice;

import java.util.Properties;

public class ChangeTrackingProperties extends Properties {

  private boolean mChanged = false;

  @Override
  public synchronized Object setProperty(String key, String value) {
    mChanged = true;
    return super.setProperty(key, value);
  }

  public boolean changed() {
    return mChanged;
  }

}
