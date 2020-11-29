package com.trandonsystems.tekelek.services;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trandonsystems.tekelek.database.UnitDAL;
import com.trandonsystems.tekelek.model.Unit;
import com.trandonsystems.tekelek.model.UnitMessage;
import com.trandonsystems.tekelek.model.UnitReading;

public class UnitServices {

	static Logger log = Logger.getLogger(UnitServices.class);
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	// This considerabily different from BriteBin reading processing
	// Tekelek send multiple messages in a single tcp communication
	private UnitMessage processTekelekData(long rawDataId, byte[] data) throws Exception {
		
		log.info("processTekelekData - start");
		UnitMessage unitMsg = new UnitMessage();
		UnitReading reading = new UnitReading();

		int productType = data[0] & 0xff;

        // serialNo is IMEI - each of the half bytes in bytes 7 to 14 have the digits of the IMEI - converting each byte to it HEX will give each of the 2 digits in that byte
        reading.serialNo = Hex.ByteToHex(data[7]) + Hex.ByteToHex(data[8]) + Hex.ByteToHex(data[9]) + Hex.ByteToHex(data[10]) 
                        + Hex.ByteToHex(data[11]) + Hex.ByteToHex(data[12]) + Hex.ByteToHex(data[13]) + Hex.ByteToHex(data[14]);
        Unit unit = UnitDAL.getUnitBySerialNo(1, reading.serialNo);
		        
        reading.msgType = (int)data[15];

        int sampleInterval = 0;
        int loggerSpeed = (int)data[23];

        if (reading.msgType == 8) {
            sampleInterval = ((loggerSpeed & 128) == 128) ? 15 : 1;
        } else if (reading.msgType == 4) {
            int noIntervals = loggerSpeed & 127;  // lower 7 bits define the number of intervals between logging samples

            sampleInterval = 15 * noIntervals;
        }
        
        log.debug("Product Type:" + productType);
        log.debug("IMEI:" + reading.serialNo);
        log.debug("Message Type:" + reading.msgType);
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
	            log.debug("RTC: " + readingTime_HHMM + "   cms: " + reading.binLevel);
	
	            // Put default values into all other fields - NOT used by Tekelek units
				reading.binLevelBC = 0;
				reading.noFlapOpening = 0;
				reading.batteryVoltage = 0;
				reading.noCompactions = 0;
				
				reading.batteryUVLO = false;
				reading.binEmptiedLastPeriod = false;
				reading.batteryOverTempLO = false;
				reading.binLocked = false;
				reading.binFull = false;
				reading.binTilted = false;
				reading.serviceDoorOpen = false;
				reading.flapStuckOpen = false;
				
				reading.nbIoTSignalStrength = 0;
				reading.snr = 0;
				reading.ber = 0;

				reading.readingDateTime = readingTime;
	            log.info(reading);
				
	            // Save data to database
				UnitDAL.saveReading(rawDataId, unit.id, reading);
	
	            readingsCount++;
	
	            if (readingsCount == 28) {
	                finished = true;
	            } else {
	                index += 4;
	
	                readingTime = readingTime.minus(sampleInterval, ChronoUnit.MINUTES);;
	
	                if (data[index] + data[index+1] + data[index+2] + data[index+3] == 0) {
	                    finished = true;
	                }
	            }
	        }
	        
			unitMsg = UnitDAL.getUnitMsg(reading.serialNo);
			log.debug("unitMsg: " + gson.toJson(unitMsg));		
	        
        }
		log.info("processTekelekData - end");

		return unitMsg;
	}
	

	public UnitMessage saveUnitReading(byte[] data) throws Exception {
		try {
			log.info("UnitServices.saveUnitReading");
			
			UnitMessage unitMsg = new UnitMessage();
			
			// Save the raw data to the DB
			long rawDataId = UnitDAL.saveRawData(data);
			
			// Tekelek sensor must be 140 bytes
    		if (data.length == 140) {
				unitMsg = processTekelekData(rawDataId, data);
    		} else {
    			throw new Exception("Tekelek messages must be 140 bytes");
    		}
						
			return unitMsg;
		}
		catch(SQLException ex) {
			log.error(ex.getMessage());
			throw ex;
		}
	}
    
	public void markMessageAsSent(UnitMessage unitMsg) throws SQLException {
		UnitDAL.markMessageAsSent(unitMsg);
	}
}
