package com.trandonsystems.tekelek;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;

import org.apache.log4j.Logger;

import com.trandonsystems.tekelek.database.UtilDAL;
import com.trandonsystems.tekelek.threads.ListenerThread;
import com.trandonsystems.tekelek.utils.Util;

public class TekelekTcpListener {

	// Set the license to date
	static final LocalDate LICENSE_TO_DATE = LocalDate.of(2020, 9, 30);
    static LocalDate dateLicenseMsgOutput = LocalDate.now().minusDays(1);
	
	static Logger log = Logger.getLogger(TekelekTcpListener.class);
	
	private static long daysLeftOnLicense() {
		
		LocalDate today = LocalDate.now();

		long daysLeft = LICENSE_TO_DATE.toEpochDay() - today.toEpochDay();
		
		return daysLeft;
	}
	
	private static boolean licenseExpired() {
    	if (daysLeftOnLicense() < 0 ) {
			log.info("******************************************************************************************************");
			log.info("******************************************************************************************************");
    		log.info("*******       Your license has expired - please contact Trandon Systems to renew license       *******");
			log.info("******************************************************************************************************");
			log.info("******************************************************************************************************");
			
			return true;
    	} else if (daysLeftOnLicense() < 60) {
    		if ((LocalDate.now().toEpochDay() -dateLicenseMsgOutput.toEpochDay()) > 0) {
    			// Only output message once a day
    			log.info("*******************************************************************************************************");
        		log.info("****      There are " + daysLeftOnLicense() + " days left on license - please contact Trandon Systems to renew License      ****");
    			log.info("*******************************************************************************************************");
        		dateLicenseMsgOutput = LocalDate.now();
        		
        		// Email me notification license is about to expire
    		}
    	}
    	return false;
	}

	public static void main(String[] args) {

        log.info("Server Started ... ");
        
		int port = Util.tekelekTcpPort;
		UtilDAL.envName = System.getenv("ENV_NAME");
		
        try (ServerSocket serverSocket = new ServerSocket(port)) {
        	 
            log.info("Server is listening on port " + port);
 
            while (true) {
            	// Validate License
            	if (licenseExpired()) {
            		break;
            	}
            	
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
