package widgetplugin;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.SwingUtilities;

import tvbrowser.extras.programinfo.ProgramInfo;
import util.ui.Localizer;
import util.ui.html.HTMLTextHelper;
import widgetplugin.nanohttpd.NanoHTTPD;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginManager;
import devplugin.Program;

public class WidgetServer extends NanoHTTPD {
	
	private static final String PARAM_ID = "id";

	private static final String PARAM_ACTION = "action";

	private static final String CMD_SHOWPROGRAMINFO = "showprograminfo";

	private static final Localizer mLocalizer = Localizer
	.getLocalizerFor(WidgetServer.class);

  private WidgetSettings mSettings;

  protected WidgetServer(WidgetSettings settings) throws IOException {
    super(settings.getPortNumber());
    mSettings = settings;
	}

	@Override
	public Response serve(String uri, String method, Properties header,
			Properties parms) {
		// check for localhost
		String host = header.getProperty("host");
		if (host.contains(":")) {
			host = host.substring(0, host.indexOf(':'));
		}
		host = host.toLowerCase();
		if (!host.equals("localhost")) {
			return new Response(HTTP_FORBIDDEN, MIME_HTML, mLocalizer.msg("localHostOnly", "Widgets are only available on localhost"));
		}
		// find command to handle
		String action = parms.getProperty(PARAM_ACTION);
		if (action != null) {
			action = action.toLowerCase();
			if (action.equals(CMD_SHOWPROGRAMINFO)) {
				String id = parms.getProperty(PARAM_ID);
				if (id != null && id.length() > 0) {
					final PluginManager pluginManager = Plugin.getPluginManager();
					final Program program = pluginManager.getProgram(pluginManager.getCurrentDate(), id);
					if (program != null) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								pluginManager.scrollToProgram(program);
								ProgramInfo.getInstance().showProgramInformation(program);
							}
						});
					}
				}
			}
		}
		// build the widget code
		return new Response(HTTP_OK, MIME_HTML, currentProgramList());
	}

	private String currentProgramList() {
		StringBuffer result = new StringBuffer(1024);
		Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();
		if (channels.length == 0) {
			return mLocalizer.msg("noSubscriptions", "No channels subscribed");
		}
		Date date = Date.getCurrentDate();
		for (int i = 0; i < channels.length; i++) {
			Iterator<Program> dayProgram = Plugin.getPluginManager().getChannelDayProgram(date, channels[i]);
			if (dayProgram != null) {
				while (dayProgram.hasNext()) {
					Program program = dayProgram.next();
					if (program.isOnAir()) {
						result.append(convert(channels[i].getName())).append(" - ").append(
								program.getTimeString()).append(" <a href=\"").append(
								getProgramLink(program)).append("\"");
						String shortInfo = program.getShortInfo();
						if (shortInfo != null) {
							result.append(" title=\"").append(convert(shortInfo))
									.append("\"");
						}
						result.append(">").append(convert(program.getTitle())).append(
								"</a><br>");
					}
				}
			}
		}
		if (result.length() == 0) {
			return mLocalizer.msg("noOnAir", "No programs on air");
		}
		result
        .insert(0,
            "</head><body><span style=\"font-family:sans-serif; font-size:x-small\">");
		if (mSettings.getRefresh()) {
      result.insert(0, "<meta http-equiv=\"refresh\" content=\"60\"/>");
    }
		result
				.insert(
						0,
						"<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />");
		result.append("</span></body></html>");
		return result.toString();
	}

	private String convert(String in) {
		in = HTMLTextHelper.convertTextToHtml(in.replaceAll("\n", " "), false);
		StringBuffer buf = new StringBuffer();
		int len = in.length();
		for (int i = 0; i < len; i++) {
			char c = in.charAt(i);
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9') {
				buf.append(c);
			} else {
				buf.append("&#" + (int) c + ";");
			}
		}
		return buf.toString();
	}
	
	private String getProgramLink(Program program) {
		return "?" + PARAM_ACTION + "=" + CMD_SHOWPROGRAMINFO + "&" + PARAM_ID + "=" + program.getID();
	}

}
