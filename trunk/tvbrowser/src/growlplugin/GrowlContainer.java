package growlplugin;

import java.io.File;

import util.exc.ErrorHandler;
import util.ui.Localizer;

import com.apple.cocoa.application.NSImage;

import devplugin.Program;

public class GrowlContainer {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GrowlPlugin.class);

  /** Nofiction-Name */
  private final static String NOTIFICATION = "TVBrowserSendProgram";
  
  /** Growl-Instance */
  private Growl mGrowl;
  
  /**
   * Create the Growl-Container and register the NOTIFICATION
   * 
   * @throws Exception
   */
  public GrowlContainer() throws Exception {
    File img = new File("./imgs/TVBrowser32.gif");

    mGrowl = new Growl("TV-Browser", new NSImage(img.toURL()));
    
    String[] notes = {NOTIFICATION};
    
    mGrowl.setAllowedNotifications(notes);
    mGrowl.setDefaultNotifications(notes);
    mGrowl.register();

  }
  
  /**
   * Notifies Growl
   * 
   * @param prg Program to use
   */
  public void notifyGrowl(Program prg) {
    try {
      mGrowl.notifyGrowlOf(NOTIFICATION, prg.getTitle(), "bla desc");
    } catch (Exception e) {
      ErrorHandler.handle("Error while Sending Program to Growl", e);
    }
  }
  
}