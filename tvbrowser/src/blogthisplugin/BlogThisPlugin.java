package blogthisplugin;

import java.awt.event.ActionEvent;
import java.net.URLEncoder;

import javax.swing.AbstractAction;
import javax.swing.Action;

import util.paramhandler.ParamParser;
import util.ui.BrowserLauncher;
import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.Version;

public class BlogThisPlugin extends Plugin {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(BlogThisPlugin.class);
  
  private static final String DEFAULT_TITLE = "{title} ({channel_name})";
  private static final String DEFAULT_CONTENT = "<blockquote><strong>{title}</strong>\n\n<em>{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")} {channel_name}</em>\n\n{short_info}</blockquote>\n";
  
  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#getInfo()
   */
  public PluginInfo getInfo() {
    String name = mLocalizer.msg("pluginName", "BlogThis");
    String desc = mLocalizer.msg("description", "Creates a new Blog-Entry");
    String author = "Bodo Tasche";
    return new PluginInfo(name, desc, author, new Version(0, 1));
  }
  
  
  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#getContextMenuActions(devplugin.Program)
   */
  public ActionMenu getContextMenuActions(final Program program) {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        blogThis(program);
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("contextMenuText", "Create a new Blog-Entry"));
    action.putValue(Action.SMALL_ICON, createImageIcon("apps", "internet-web-browser", 16)); 
    return new ActionMenu(action);
  }  

  /**
   * Creates a new Blog-Entry
   * @param program Program to use for the Entry
   */
  private void blogThis(Program program) {
    ParamParser parser = new ParamParser();
    
    String title = parser.analyse(DEFAULT_TITLE, program);
    String content = parser.analyse(DEFAULT_CONTENT, program);
    String url = program.getChannel().getWebpage();
    
    try {
      BrowserLauncher.openURL("http://blog.wannawork.de/admin/b2bookmarklet.php?" +
          "post_title="+URLEncoder.encode(title, "ISO-8859-1")+"&"+
          "content="+URLEncoder.encode(content, "ISO-8859-1") + "&"+
          "post_url="+URLEncoder.encode(url, "ISO-8859-1")+ "&" +
          "sourceid="+URLEncoder.encode("BlogThis v0.1", "UTF-8"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
 
}