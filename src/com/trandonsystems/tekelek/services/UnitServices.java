package com.trandonsystems.tekelek.services;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trandonsystems.tekelek.database.UnitDAL;
import com.trandonsystems.tekelek.model.Unit;
import com.trandonsystems.tekelek.model.TekelekMessage;
import com.trandonsystems.tekelek.model.UnitReading;

public class UnitServices {

	static Logger log = Logger.getLogger(UnitServices.class);
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	// This considerabily different from BriteBin reading processing
	// Tekelek send multiple messages in a single tcp communication
	private List<TekelekMessage> processTekelekData(long rawDataId, byte[] data) throws Exception {
		
		log.info("processTekelekData - start");
		List<TekelekMessage> unitMsgs = new ArrayList<TekelekMessage>();
		UnitReading reading = new UnitReading();

		int productType = data[0] & 0xff;

        // serialNo is IMEI - each of the half bytes in bytes 7 to 14 have the digits of the IMEI - converting each byte to it HEX will give each of the 2 digits in that byte
        reading.serialNo = Hex.ByteToHex(data[7]) + Hex.ByteToHex(data[8]) + Hex.ByteToHex(data[9]) + Hex.ByteToHex(data[10]) 
                        + Hex.ByteToHex(data[11]) + Hex.ByteToHex(data[12]) + Hex.ByteToHex(data[13]) + Hex.ByteToHex(data[14]);
        Unit unit = UnitDAL.getUnitBySerialNo(1, reading.serialNo);
		
        reading.msgType = (int)data[15];
        log.debug("MsgType: " + reading.msgType);

        int sampleInterval = 0;
        int loggerSpeed = (int)data[23];

        if (reading.msgType == 8) {
        	// MsgType = 8 (This is an alarm or a manual send (using magnet))
            sampleInterval = ((loggerSpeed & 128) == 128) ? 15 : 1;
        } else if (reading.msgType == 4) {
        	// This is a regular server communication
            int noIntervals = loggerSpeed & 127;  // lower 7 bits define the number of intervals between logging samples

            sampleInterval = 15 * noIntervals;
        }

        // These will only be used for msgType = 8, but set once here for the message received
    	reading.contactReason = (int)data[3];   // byte 3 is contact reason
    	log.debug("Contact Reason: " + reading.contactReason);

    	reading.alarmStatus = (int)data[4];
    	log.debug("AlarmStats: " + reading.alarmStatus);

    	boolean manualReading = ((reading.contactReason & 8) == 8);  	// if bit 4 = 1 then it is a manual triggered reading
    	boolean alarmReading = ((reading.contactReason & 2) == 2);		// if bit 2 = 1 then it is an alarm reading

    	log.info("Manual Reading: " + manualReading);
    	log.info("Alarm Reading: " + alarmReading);
    	
    	if (alarmReading) {
    		// Check if static alarms fired
    		boolean s4Alarm = ((reading.alarmStatus & 1) == 1);
    		boolean s5Alarm = ((reading.alarmStatus & 2) == 2);
    		boolean s6Alarm = ((reading.alarmStatus & 4) == 4);
    		
    		// check if dynamic alarms fired
    		boolean s7Alarm = ((reading.contactReason & 64) == 64);
    		boolean s8Alarm = ((reading.contactReason & 128) == 128);
    		
    		if (s4Alarm) log.debug("S4 Alarm triggered");
    		if (s5Alarm) log.debug("S5 Alarm triggered");
    		if (s6Alarm) log.debug("S6 Alarm triggered");
    		if (s7Alarm) log.debug("S7 Alarm triggered");
    		if (s8Alarm) log.debug("S8 Alarm triggered");
    	}

    	
        log.debug("Product Type: " + productType);
        log.debug("IMEI: " + reading.serialNo);
        log.debug("Message Type: " + reading.msgType);
        log.debug("Logger Interval: " + sampleInterval);

        // Only interested in message type 4 & 8 - ignore all other message types
        if (reading.msgType != 4 && reading.msgType != 8) {
            log.info("Message Type: " + reading.msgType + " >>> Not supported");
        } else {
        	
	        Instant readingTime = Instant.now();
	
	        int index = 26;
	        int readingsCount = 0;
	        boolean finished = false;
	        while (!finished) {
	            // Process the data 
	            reading.rssi = (int)data[index] & 15;
	            reading.temperature = ((int)(data[index + 1] & 0xff) >> 1) - 30;
	            reading.src = ((int)data[index + 2] >> 2) & 15;
	            reading.binLevel = (((int)data[index + 2] & 3) << 8) + (int)(data[index + 3] & 0xff);
	
	    		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
	    			                     						.withLocale(Locale.UK)
	    			                     						.withZone(ZoneId.systemDefault());
	    		String readingTime_HHMM = formatter.format( readingTime );		
	            log.debug("RTC: " + readingTime_HHMM + "   cms: " + reading.binLevel + "   src: " + reading.src + "   temp: " + reading.temperature + "   rssi: " + reading.rssi);

				reading.readingDateTime = readingTime;
	            log.info(reading);
				

	            if (reading.msgType == 8) {
	            	// Manual reading or an alarm
	            	if (manualReading) {
	            		if (reading.src >= 6) {
	            			// Good reading - save to database
	        				UnitDAL.saveReading(rawDataId, unit.id, reading);
	        				// Only save one valid manual reading - (Note, if no reading of src = 9 or 10 then no manual reading will be saved)
	        				finished = true;
	            		}
	            	} else if (alarmReading) {
	            		// only save one reading to the database
	        			UnitDAL.saveReading(rawDataId, unit.id, reading);
	        			finished = true;
	            	}
	            } else {
	            	// Regular interval reading communication - Only save readings where src >= 0
            		if (reading.src >= 0) {
        	            // Save data to database
        				UnitDAL.saveReading(rawDataId, unit.id, reading);
            		}
	            }
	
	            readingsCount++;
	
	            if (readingsCount == 28) {
	                finished = true;
	            } else {
	                index += 4;
	
	                // Do NOT increment readingDateTime for a manual Reading - we will only save the first reading that has src = 9 or 10
	                if (reading.msgType != 8 || !manualReading) {
	                	readingTime = readingTime.minus(sampleInterval, ChronoUnit.MINUTES);
	                }
	
	                if (data[index] + data[index+1] + data[index+2] + data[index+3] == 0) {
	                    finished = true;
	                }
	            }
	        }
	        
			unitMsgs = UnitDAL.getTekelekMsgs(reading.serialNo);
			log.debug("unitMsgs: " + gson.toJson(unitMsgs));		
	        
        }
		log.info("processTekelekData - end");

		return unitMsgs;
	}

	
	public List<TekelekMessage> saveUnitReading(byte[] data) throws Exception {
		try {
			log.info("UnitServices.saveUnitReading");
			
			List<TekelekMessage> unitMsgs = new ArrayList<TekelekMessage>();
			
			// Save the raw data to the DB
			long rawDataId = UnitDAL.saveRawData(data);
			
			// Tekelek sensor must be 140 bytes
    		if (data.length == 140) {
				unitMsgs = processTekelekData(rawDataId, data);
    		} else {
    			throw new Exception("Tekelek messages must be 140 bytes");
    		}
						
			return unitMsgs;
		}
		catch(SQLException ex) {
			log.error(ex.getMessage());
			throw ex;
		}
	}
    
	public void markMessageAsSent(TekelekMessage unitMsg) throws SQLException {
		UnitDAL.markTekelekMessageAsSent(unitMsg);
	}
}
