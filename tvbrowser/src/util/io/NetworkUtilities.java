/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *
 * CVS information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.io;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.logging.Logger;


/**
 * Network Helper Class with some utility functions
 *
 * @author bodum
 * @since 2.2
 */
public class NetworkUtilities {

  private static final Logger mLog = java.util.logging.Logger
    .getLogger(NetworkUtilities.class.getName());

  /**
   * Checks if a Internet connection can be established
   *
   * @return true, if a connection can be established
   */
  public static boolean checkConnection() {
    return new CheckNetworkConnection().checkConnection();
  }

  /**
   * Checks if a Internet connection to a specific Server can be established
   *
   * @param url check this Server
   * @return true, if a connection can be established
   */
  public static boolean checkConnection(URL url) {
    return new CheckNetworkConnection().checkConnection(url);
  }


  /**
   * get the time difference from a NTP server
   *
   * @param serverName
   * @return time difference in seconds
   * @since 2.6
   */
  public static int getTimeDifferenceSeconds(String serverName) {
    // don't access the net if there is no connection
    if (!checkConnection()) {
      return 0;
    }
    // Send request
    try {
      DatagramSocket socket = new DatagramSocket();
      socket.setSoTimeout(10000);
      InetAddress address = InetAddress.getByName(serverName);
      byte[] buf = new NtpMessage().toByteArray();
      DatagramPacket packet =
        new DatagramPacket(buf, buf.length, address, 123);

      // Set the transmit timestamp *just* before sending the packet
      // ToDo: Does this actually improve performance or not?
      NtpMessage.encodeTimestamp(packet.getData(), 40,
        (System.currentTimeMillis()/1000.0) + 2208988800.0);

      socket.send(packet);


      // Get response
      mLog.info("NTP request sent, waiting for response...");
      packet = new DatagramPacket(buf, buf.length);
      socket.receive(packet);

      // Immediately record the incoming timestamp
      double destinationTimestamp =
        (System.currentTimeMillis()/1000.0) + 2208988800.0;


      // Process response
      NtpMessage msg = new NtpMessage(packet.getData());

      // Corrected, according to RFC2030 errata
      double roundTripDelay = (destinationTimestamp-msg.originateTimestamp) -
        (msg.transmitTimestamp-msg.receiveTimestamp);

      double localClockOffset =
        ((msg.receiveTimestamp - msg.originateTimestamp) +
        (msg.transmitTimestamp - destinationTimestamp)) / 2;


      // Display response
      mLog.info("NTP server: " + serverName);
      mLog.info(msg.toString());

      mLog.info("Dest. timestamp:     " +
        NtpMessage.timestampToString(destinationTimestamp));

      mLog.info("Round-trip delay: " +
        new DecimalFormat("0.00").format(roundTripDelay*1000) + " ms");

      mLog.info("Local clock offset: " +
        new DecimalFormat("0.00").format(localClockOffset*1000) + " ms");

      socket.close();
      return (int) localClockOffset;
    } catch (SocketException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return 0;
  }

  public static String[] getConnectionCheckUrls() {
    return CheckNetworkConnection.getUrls();
  }

}