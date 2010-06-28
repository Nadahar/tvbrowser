package util.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import devplugin.Program;

/**
 * A class for Drag'n'Drop of Programs.
 * @author René Mach
 */
public class TransferProgram implements Transferable {
  private Program mProgram;
  private DataFlavor mPF;
  
  /**
   * Set up the transferEntries.
   * @param program The selected program.
   */
  public TransferProgram(Program program) {
    mProgram = program;
    mPF = new DataFlavor(Program.class,"Program");
  }
  
  public DataFlavor[] getTransferDataFlavors() {
    DataFlavor[] f = {mPF};
    return f;
  }

  public boolean isDataFlavorSupported(DataFlavor e) {
    if(e.equals(mPF)) {
      return true;
    } else {
      return false;
    }
  }
  
  public Object getTransferData(DataFlavor e) throws UnsupportedFlavorException, IOException {
    if(e.equals(mPF)) {
      return mProgram;
    } else {
      return null;
    }
  }

}
