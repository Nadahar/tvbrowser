package util.misc;

public class JavaVersion {

  public static final int VERSION_NOTFOUND = 0;

  public static final int VERSION_1_0 = 1;

  public static final int VERSION_1_1 = 2;

  public static final int VERSION_1_2 = 3;

  public static final int VERSION_1_3 = 4;

  public static final int VERSION_1_4 = 5;

  public static final int VERSION_1_5 = 6;

  public static final int VERSION_1_6 = 7;

  public static final int VERSION_1_7 = 8;

  public static int getVersion() {

    String[] ver = System.getProperty("java.version").split("\\.");

    if (ver.length < 2) {
      return -1;
    }

    try {

      int major = Integer.parseInt(ver[0]);
      int minor = Integer.parseInt(ver[1]);

      if (major == 1) {

        switch (minor) {
        case 0:
          return VERSION_1_0;
        case 1:
          return VERSION_1_1;
        case 2:
          return VERSION_1_2;
        case 3:
          return VERSION_1_3;
        case 4:
          return VERSION_1_4;
        case 5:
          return VERSION_1_5;
        case 6:
          return VERSION_1_6;
        case 7:
          return VERSION_1_7;
        default:
          break;
        }

        if (minor > 7) {
          return VERSION_1_7;
        }
      }

    } catch (Exception e) {
      // TODO: handle exception
    }

    return -1;
  }

  public static void main(String[] args) {
    System.out.println(getVersion());
  }
}
