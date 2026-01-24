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

import org.eclipsetrader.jessx.internal.JessxActivator;
import org.eclipsetrader.jessx.server.Server;
import org.eclipsetrader.jessx.utils.Utils;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.IStatus;


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
    setDaemon(true);
  }

  private void initializeServerSocket() {
    try {
      String portStr = Utils.appsProperties.getProperty("ServerWaitingPort");
      int port = 6290;
      if (portStr != null) {
          try {
              port = Integer.parseInt(portStr);
          } catch (NumberFormatException e) {
              Utils.logger.warn("Invalid port property: " + portStr + ". Using default 6290.");
          }
      }
      this.serverSocket = new ServerSocket(port);
      Utils.logger.info("Server socket created on port " + port + ". Counting down serverReadyLatch.");
      if (JessxActivator.getDefault() != null) {
          JessxActivator.log("Server socket created on port " + port);
      }
      serverReadyLatch.countDown();
    }
    catch (Exception ex) {
        String msg = "Error initializing server socket: " + ex.toString();
        Utils.logger.error(msg, ex);
        if (JessxActivator.getDefault() != null) {
            JessxActivator.log(new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, msg, ex));
        }
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
   if (serverSocket == null) {
       initializeServerSocket();
       if (serverSocket == null) {
           Utils.logger.error("Server socket is null. Cannot accept connections.");
           if (JessxActivator.getDefault() != null) {
               JessxActivator.log(new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, "Server socket is null. Cannot accept connections."));
           }
           return;
       }
   }

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
       if (JessxActivator.getDefault() != null) JessxActivator.log("ClientConnectionPoint waiting for a client...");
       Socket clientSocket = serverSocket.accept();
       Utils.logger.info("ClientConnectionPoint accepted a new client connection. Starting handler thread.");
       if (JessxActivator.getDefault() != null) JessxActivator.log("ClientConnectionPoint accepted a new client connection.");
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
