package com.trandonsystems.tekelek;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.trandonsystems.tekelek.database.UtilDAL;
import com.trandonsystems.tekelek.threads.ListenerThread;
import com.trandonsystems.tekelek.utils.Util;

public class TekelekTcpListener {

	static Logger log = Logger.getLogger(TekelekTcpListener.class);
	

	public static void main(String[] args) {

        log.info("Server Started ... ");
        
		int port = Util.tekelekTcpPort;
		UtilDAL.envName = System.getenv("ENV_NAME");
		
        try (ServerSocket serverSocket = new ServerSocket(port)) {
        	 
            log.info("Server is listening on port " + port);
 
            while (true) {
            	// Validate License
//            	if (License.isExpired()) {
//            		break;
//            	}
            	
                Socket socket = serverSocket.accept();
                log.info("New client connected");
 
                new ListenerThread(socket).start();
            }
 
        } catch (IOException ex) {
            log.error("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }		

        log.info("");
        log.info(" ... Server Terminated");
	}

}
