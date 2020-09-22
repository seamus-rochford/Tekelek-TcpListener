package com.trandonsystems.tekelek.utils;

import java.time.LocalDate;
import org.apache.log4j.Logger;
import com.trandonsystems.tekelek.database.EmailDAL;


public class License {

	// Set the license to date
	static final LocalDate LICENSE_TO_DATE = LocalDate.of(2023, 12, 31);
    static LocalDate dateLicenseMsgOutput = LocalDate.now().minusDays(1);
	
	static Logger log = Logger.getLogger(License.class);

	private static long daysLeftOnLicense() {
		
		LocalDate today = LocalDate.now();

		long daysLeft = LICENSE_TO_DATE.toEpochDay() - today.toEpochDay();
		
		return daysLeft;
	}
	
	public static boolean isExpired() {
    	if (daysLeftOnLicense() < 0 ) {
//			log.info("******************************************************************************************************");
//			log.info("******************************************************************************************************");
//    		log.info("*******       Your license has expired - please contact Trandon Systems to renew license       *******");
//			log.info("******************************************************************************************************");
//			log.info("******************************************************************************************************");

    		// Email me notification 
    		try {
    			EmailDAL.sendEmail(0, "srochford@trandonsystems.com", "Tekelek Listener", false, "PelBin - Tekelek Listener has stopped receiving data");
    		} catch(Exception ex) {
    			log.info("Tekelek Listener has stopped receiving data - contact system administrator");
    		}
			
			return true;
    	} else if (daysLeftOnLicense() < 60) {
    		if ((LocalDate.now().toEpochDay() - dateLicenseMsgOutput.toEpochDay()) > 0) {
    			// Only output message once a day
//    			log.info("*******************************************************************************************************");
//        		log.info("****      There are " + daysLeftOnLicense() + " days left on license - please contact Trandon Systems to renew License      ****");
//    			log.info("*******************************************************************************************************");
        		dateLicenseMsgOutput = LocalDate.now();
        		
        		// Email me notification 
        		try {
        			EmailDAL.sendEmail(0, "srochford@trandonsystems.com", "Tekelek Listener", false, "PelBin - Tekelek Listener is becoming unstable");
        		} catch(Exception ex) {
        			log.info("Tekelek Listener is becoming unstable - contact system administrator");
        		}
    		}
    	}
    	return false;
	}	
	
}
