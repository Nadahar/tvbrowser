/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package tvraterplugin;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * This class contains the communication with the server
 * @author bodo tasche
 */
public class Updater {

	private static String LOCATION = "http://localhost/test/test.php"; 

	private Database _tvdatabase;

	public Updater(Frame parent, Database tvdatabase) {
		_tvdatabase = tvdatabase;
	}

	public void doUpdate() throws Exception {
		System.out.println("Update!!");

		URL url = new URL(LOCATION);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);

		OutputStream out = connection.getOutputStream();
		
		GZIPOutputStream outZipped = new GZIPOutputStream(out);

		PrintWriter writer = new PrintWriter(outZipped);
		writeData(writer);
		writer.close();

		String data = "";

		data = readURLConnection(connection);
		System.out.println(data);

		out.close();
	}

	private void writeData(PrintWriter writer) {
		Hashtable table = createUpdateList();

		writer.println("<tvrater>");
		writer.println("<user>");
		writer.println("<name>");
		writer.println("</name>");
		writer.println("<password>");
		writer.println("</password>");
		writer.println("</user>");

		writer.println("<setratings>");
		writer.println("</setratings>");
		
		writer.println("<getratings>");
		
		Enumeration enum = table.elements();
		
		while (enum.hasMoreElements()) {
			writer.println("<program>");
			Program prog = (Program) enum.nextElement();
			writer.println(prog.getTitle());
			writer.println("</program>");
		}
		
		writer.println("</getratings>");
		
		writer.println("</tvrater>");
	}

	private static String readURLConnection(URLConnection uc) throws Exception {
		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String line = null;
			int letter = 0;
			while ((letter = reader.read()) != -1)
				buffer.append((char) letter);
		} catch (Exception e) {
			System.out.println("Cannot read from URL" + e.toString());
			throw e;
		} finally {
			try {
				reader.close();
			} catch (IOException io) {
				System.out.println("Error closing URLReader!");
				throw io;
			}
		}
		return buffer.toString();
	}

	private Hashtable createUpdateList() {
		Hashtable table = new Hashtable();

		Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();

		Date date = new Date();
		for (int d = 0; d < 31; d++) {
			for (int i = 0; i < channels.length; i++) {
				Iterator it = Plugin.getPluginManager().getChannelDayProgram(date, channels[i]);
				while ((it != null) && (it.hasNext())) {
					Program program = (Program) it.next();
					if (program.getLength() >= 75) {

						if (!table.containsKey(program.getTitle())) {
							table.put(program.getTitle(), program);
						}
					}
				}
			}

			date = date.addDays(1);
		}

		return table;
	}

}