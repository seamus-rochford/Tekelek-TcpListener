package com.trandonsystems.tekelek.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trandonsystems.tekelek.model.Unit;
import com.trandonsystems.tekelek.model.Alert;
import com.trandonsystems.tekelek.model.TekelekMessage;
import com.trandonsystems.tekelek.model.UnitReading;

public class UnitDAL {

	static final String SOURCE = "NB-IoT Tek";
	
	static Logger log = Logger.getLogger(UnitDAL.class);
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static long saveRawData(byte[] data) throws SQLException{
		log.info("UnitDAL.saveRawData(data)");

		String spCall = "{ call SaveRawReadings(?, ?) }";
		log.info("SP Call: " + spCall);

		long id = 0;
		try (Connection conn = UtilDAL.getConnection();
				CallableStatement spStmt = conn.prepareCall(spCall)) {

			spStmt.setBytes(1, data);
			spStmt.setString(2, SOURCE);
			ResultSet rs = spStmt.executeQuery();
			
			if (rs.next()) {
				id = rs.getInt("id");
			}

		} catch (SQLException ex) {
			log.error(ex.getMessage());
			throw ex;
		}
		
		return id;
	}

	public static List<Alert> getAlerts(int unitReadingId) throws SQLException {
		log.info("UnitDAL.getAlerts(" + unitReadingId + ")");
 
		String spCall = "{ call GetAlertsByUnitReadingId(?) }";
		log.debug("SP Call: " + spCall);

		List<Alert> alerts = new ArrayList<Alert>();
		
		try (Connection conn = UtilDAL.getConnection();
				CallableStatement spStmt = conn.prepareCall(spCall)) {
			spStmt.setLong(1, unitReadingId);
			ResultSet rs = spStmt.executeQuery();	
			
			while (rs.next()) {
				Alert alert = new Alert();

				alert.id = rs.getInt("id");
				alert.alertType = rs.getInt("alertType");
				alert.unitId = rs.getInt("unitId");
				alert.status = rs.getInt("status");
				alert.unitReadingId = unitReadingId;
				alert.comments = rs.getString("comments");
				alert.damageId = rs.getInt("damageId");
				
				// Convert database timestamp(UTC date) to local time instant
				Timestamp alertDateTime = rs.getTimestamp("alertDateTime");
				if (alertDateTime == null) {
					alert.alertDateTime = null;
				}
				else {
					java.time.Instant alertDateTimeInstant = alertDateTime.toInstant();
					alert.alertDateTime = alertDateTimeInstant;
				}	
				
				alerts.add(alert);
			}
			
		} catch (SQLException ex) {
			log.error(ex.getMessage());
			throw ex;
		}
		
		return alerts;
	}
	
	public static void setBinEmptiedFlag(int unitReadingId) throws SQLException {
		log.info("UnitDAL.setBinEmptiedFlag(unitReadingId)");

		String spCall = "{ call SetBinEmptiedFlag(?) }";
		log.debug("SP Call: " + spCall);

		try (Connection conn = UtilDAL.getConnection();
				CallableStatement spStmt = conn.prepareCall(spCall)) {

			spStmt.setInt(1, unitReadingId);

		    spStmt.executeQuery();

			
		} catch (SQLException ex) {
			log.error(ex.getMessage());
			throw ex;
		}
		
		return;	
	}
	
	public static void setBinFullFlag(int unitReadingId) throws SQLException {
		log.info("UnitDAL.setBinFullFlag(unitReadingId)");

		String spCall = "{ call SetBinFullFlag(?) }";
		log.debug("SP Call: " + spCall);

		try (Connection conn = UtilDAL.getConnection();
				CallableStatement spStmt = conn.prepareCall(spCall)) {

			spStmt.setInt(1, unitReadingId);

		    spStmt.executeQuery();

			
		} catch (SQLException ex) {
			log.error(ex.getMessage());
			throw ex;
		}
		
		return;	
	}
	
	public static void saveReading(long rawDataId, long unitId, UnitReading reading) throws SQLException {
		log.info("UnitDAL.saveReading(rawDataId, unitId, reading)");

		String spCall = "{ call SaveReadingTekelek_V3(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";
		log.debug("SP Call: " + spCall);

		int id = 0;
		
		try (Connection conn = UtilDAL.getConnection();
				CallableStatement spStmt = conn.prepareCall(spCall)) {

			spStmt.setLong(1, id);
			spStmt.setLong(2, unitId);
			spStmt.setString(3, reading.serialNo);
			spStmt.setLong(4, rawDataId);
			spStmt.setInt(5, reading.msgType);
			spStmt.setInt(6, reading.binLevel);
			spStmt.setInt(7, reading.temperature);
			spStmt.setDouble(8, reading.rssi);
			spStmt.setInt(9, reading.src);
			spStmt.setInt(10, reading.contactReason);
			spStmt.setInt(11, reading.alarmStatus);

			// Convert java.time.Instant to java.sql.timestamp
			Timestamp ts = Timestamp.from(reading.readingDateTime);
		    spStmt.setTimestamp(12, ts);

			spStmt.setString(13, SOURCE);
		    
		    spStmt.executeQuery();

			id = spStmt.getInt(1);

			// If it is and alram reading or a manual reading (magnet triggered)
			if (reading.msgType == 8) {
				// Check if alert generated for this reading
				List<Alert> alerts = getAlerts(id);
				for(int i = 0; i < alerts.size(); i++) {
					Alert alert = alerts.get(i);
					switch (alert.alertType) {
					case 2:
						setBinEmptiedFlag(alert.unitReadingId);
						log.debug("Bin Emptied Flag set");
						break;
					case 10:
						setBinFullFlag(alert.unitReadingId);
						log.debug("Bin Full Flag set");
						break;
					default:
						log.debug("Unset flag for unitReading: " + alert.unitReadingId + "  Flag(alertType): " + alert.alertType);
					}
				}
			}
			
		} catch (SQLException ex) {
			log.error("UnitDAL.saveReading: " + ex.getMessage());
			throw ex;
		}
		
		return;
	}
	
	public static List<TekelekMessage> getTekelekMsgs(String serialNo) throws SQLException {
	// This gets a Tekelek Message
		log.info("UnitDAL.getUnit(conn, serialNo)");
 
		log.debug("SerialNo: " + serialNo);
		String spCall = "{ call GetTekelekMessages(?) }";
		log.debug("SP Call: " + spCall);

		List<TekelekMessage> unitMsgs = new ArrayList<TekelekMessage>();
		
		try (Connection conn = UtilDAL.getConnection();
				CallableStatement spStmt = conn.prepareCall(spCall)) {
			spStmt.setString(1, serialNo);
			ResultSet rs = spStmt.executeQuery();

			while (rs.next()) {
				TekelekMessage unitMsg = new TekelekMessage();
				unitMsg.id = rs.getInt("id");
				unitMsg.unitId = rs.getInt("unitId");
				unitMsg.message = rs.getString("message");
				
				unitMsgs.add(unitMsg);
			}
		} catch (SQLException ex) {
			log.error(ex.getMessage());
			throw ex;
		}

		return unitMsgs;
	}
	
	public static void markTekelekMessageAsSent(TekelekMessage unitMsg) throws SQLException{
		log.info("UnitDAL.markTekelekMessageAsSent(unitMsg)");

		String spCall = "{ call markTekelekMessageAsSent(?) }";
		log.info("SP Call: " + spCall);

		try (Connection conn = UtilDAL.getConnection();
				CallableStatement spStmt = conn.prepareCall(spCall)) {

			spStmt.setInt(1, unitMsg.id);
			spStmt.executeUpdate();

		} catch (SQLException ex) {
			log.error("UnitDAL.getUnit" + ex.getMessage());
			throw ex;
		}
	}

	public static Unit getUnitBySerialNo(int userFilterId, String serialNo) {
		log.info("UnitDAL.get(serialNo)");

		String spCall = "{ call GetUnitBySerialNo(?, ?) }";
		log.info("SP Call: " + spCall);
		
		Unit unit = new Unit();
		
		try (Connection conn = UtilDAL.getConnection();
				CallableStatement spStmt = conn.prepareCall(spCall)) {

			spStmt.setInt(1, userFilterId);
			spStmt.setString(2, serialNo);
			ResultSet rs = spStmt.executeQuery();

			unit.serialNo = serialNo;
			if (rs.next()) {
				unit.id = rs.getInt("id");
				unit.location = rs.getString("location");
			}

		} catch (SQLException ex) {
			log.error(ex.getMessage());
		}

		return unit;
	}
}
