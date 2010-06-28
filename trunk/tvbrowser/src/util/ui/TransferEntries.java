package util.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JList;

/**
 * The TransferEntries for JList Drag'n'Drop.
 * @author René Mach
 *
 */
public class TransferEntries implements Transferable {
  private int[] mIndices;
  private DataFlavor mSF;
  private DataFlavor mIF;
  private String mSource;
  
  /**
   * Set up the transferEntries.
   * @param indices The selected indices of the source list.
   * @param source The source list name.
   * @param type The type of the list entries.
   */
  public TransferEntries(int[] indices, String source, String type) {
    mIndices = indices;
    mIF = new DataFlavor(Integer.class,type);
    mSource = source;
    mSF = new DataFlavor(JList.class,"Source");
  }
  
  public DataFlavor[] getTransferDataFlavors() {
    DataFlavor[] f = {mIF,mSF};
    return f;
  }

  public boolean isDataFlavorSupported(DataFlavor e) {
    if(e.equals(mIF)) {
      return true;
    }
    if(e.equals(mSF)) {
      return true;
    } else {
      return false;
    }
  }
  
  public Object getTransferData(DataFlavor e) throws UnsupportedFlavorException, IOException {
    if(e.equals(mIF)) {
      return mIndices;
    }
    if(e.equals(mSF)) {
      return mSource;
    } else {
      return null;
    }
  }
  
}
