package org.eclipsetrader.jessx.server.net;

/***************************************************************/
/*                     SOFTWARE SECTION                        */
/***************************************************************/
/*
 * <p>Name: Jessx</p>
 * <p>Description: Financial Market Simulation Software</p>
 * <p>Licence: GNU General Public License</p>
 * <p>Organisation: EC Lille / USTL</p>
 * <p>Persons involved in the project : group T.E.A.M.</p>
 * <p>More details about this source code at :
 *    http://eleves.ec-lille.fr/~ecoxp03  </p>
 * <p>Current version: 1.0</p>
 */

/***************************************************************/
/*                      LICENCE SECTION                        */
/***************************************************************/
/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/***************************************************************/
/*                       IMPORT SECTION                        */
/***************************************************************/

import java.io.*;
import java.net.*;
import java.util.concurrent.CountDownLatch;

import org.eclipsetrader.jessx.server.Server;
import org.eclipsetrader.jessx.utils.Utils;


/***************************************************************/
/*           ClientConnectionPoint CLASS SECTION               */
/***************************************************************/
/**
 * <p>Title : ClientConnectionPoint</p>
 * <p>Description : </p>
 * @author Thierry Curtil
 * @version 1.0
 */

public class ClientConnectionPoint extends Thread {

    public static final CountDownLatch serverReadyLatch = new CountDownLatch(1);
    public ServerSocket serverSocket;
    private String AddressIP;

  public ClientConnectionPoint() {
  super("ClientConnectionPoint");
    try {
      this.serverSocket = new ServerSocket(Integer.parseInt(Utils.appsProperties.getProperty("ServerWaitingPort")));
      Utils.logger.info("Server socket created. Counting down serverReadyLatch.");
      serverReadyLatch.countDown();
    }
    catch (NumberFormatException ex) {
        Utils.logger.error("Property ServerWaitingPort is not an integer. Could not initialise SocketServer. " + ex.toString(), ex);
    }
    catch (IOException ex) {
        Utils.logger.error("An Input/output exception has occured while trying to initiate the serverSocket" + ex.toString(), ex);
    }
  }

  /**
   *
   * @return String
   */
  public String getIpAddressAndJavaVersion ()
  {
    try {
      AddressIP=serverSocket.getInetAddress().getLocalHost().getHostAddress();
    }
    catch (UnknownHostException ex) {
      Utils.logger.warn("Unabled to get host IP address. [IGNORED]");
    }
    return "\""+AddressIP+"\" / \""+(System.getProperty("java.version"))+"\"";
  }


  /**
   *
   */
  public void attenteConnexion() {

   try {
     AddressIP=serverSocket.getInetAddress().getLocalHost().getHostAddress();
     Utils.logger.info("Server hostname : " + serverSocket.getInetAddress().getLocalHost().getHostName() + ", Server IP address: " + AddressIP);
   }
   catch (UnknownHostException ex) {
     Utils.logger.warn("Unabled to get host IP address. [IGNORED]");
   }

   Utils.logger.info("ClientConnectionPoint entering listening loop. Server state is: " + Server.getServerState());
   while (Server.getServerState() == Server.SERVER_STATE_ONLINE) {
     try {
       Utils.logger.info("ClientConnectionPoint waiting for a client...");
       Socket clientSocket = serverSocket.accept();
       Utils.logger.info("ClientConnectionPoint accepted a new client connection. Starting handler thread.");
       new Thread(new PreConnectionClient(clientSocket)).start();
     }
     catch (Exception ex1) {
       if (Server.getServerState() != Server.SERVER_STATE_ONLINE && (ex1 instanceof SocketException || "Socket closed".equals(ex1.getMessage()))) {
           // Expected exception during shutdown
       } else {
           System.out.println(ex1.toString());
       }
     }

     // Waiting next client.
   }
    Utils.logger.warn("ClientConnectionPoint exited listening loop. Server state is: " + Server.getServerState());
 }

 /**
  *
  */
 public void run() {
    Utils.logger.info("ClientConnectionPoint thread started.");
    this.attenteConnexion();
    Utils.logger.info("ClientConnectionPoint thread finished.");
 }
}
