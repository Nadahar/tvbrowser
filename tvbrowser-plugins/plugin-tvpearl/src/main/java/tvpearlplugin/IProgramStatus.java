package tvpearlplugin;

public interface IProgramStatus {

  /**
   * not found in TVB
   */
  public final static int STATUS_NOT_FOUND = 0;
  /**
   * only channel found
   */
  public final static int STATUS_FOUND_CHANNEL = 1;
  /**
   * program found in TVB
   */
  public final static int STATUS_FOUND_PROGRAM = 2;

}
