package com.trandonsystems.tekelek.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trandonsystems.tekelek.model.Unit;
import com.trandonsystems.tekelek.model.TekelekMessage;
import com.trandonsystems.tekelek.model.UnitReading;

public class UnitDAL {

	static final String SOURCE = "NB-IoT Tek";
	
	static Logger log = Logger.getLogger(UnitDAL.class);
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static long saveRawData(byte[] data) throws SQLException{

		log.info("UnitDAL.saveRawData(data)");
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			log.error("ERROR: Can't create instance of driver" + ex.getMessage());
			throw new SQLException("ERROR: Can't create instance of driver" + ex.getMessage());
		}

		String spCall = "{ call SaveRawReadings(?, ?) }";
		log.info("SP Call: " + spCall);

		long id = 0;
		try (Connection conn = DriverManager.getConnection(UtilDAL.connUrl, UtilDAL.username, UtilDAL.password);
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

	public static void saveReading(long rawDataId, long unitId, UnitReading reading) throws SQLException {

		log.info("UnitDAL.saveReadingTekelek(rawDataId, reading)");
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			log.error("ERROR: Can't create instance of driver" + ex.getMessage());
		}

		String spCall = "{ call SaveReadingTekelek_V2(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";
		log.debug("SP Call: " + spCall);

		try (Connection conn = DriverManager.getConnection(UtilDAL.connUrl, UtilDAL.username, UtilDAL.password);
				CallableStatement spStmt = conn.prepareCall(spCall)) {

			spStmt.setLong(1, unitId);
			spStmt.setString(2, reading.serialNo);
			spStmt.setLong(3, rawDataId);
			spStmt.setInt(4, reading.msgType);
			spStmt.setInt(5, reading.binLevel);
			spStmt.setInt(6, reading.temperature);
			spStmt.setDouble(7, reading.rssi);
			spStmt.setInt(8, reading.src);
			spStmt.setInt(9, reading.contactReason);
			spStmt.setInt(10, reading.alarmStatus);

			// Convert java.time.Instant to java.sql.timestamp
			Timestamp ts = Timestamp.from(reading.readingDateTime);
		    spStmt.setTimestamp(11, ts);

			spStmt.setString(12, SOURCE);
		    
		    spStmt.executeQuery();

		} catch (SQLException ex) {
			log.error("UnitDAL.saveReading: " + ex.getMessage());
			throw ex;
		}
		
		return;
	}
	
	public static List<TekelekMessage> getTekelekMsgs(String serialNo) throws SQLException {
	// This gets a Tekelek Message
		log.info("UnitDAL.getUnit(conn, serialNo)");
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			log.error("ERROR: Can't create instance of driver" + ex.getMessage());
		}
 
		log.debug("SerialNo: " + serialNo);
		String spCall = "{ call GetTekelekMessages(?) }";
		log.debug("SP Call: " + spCall);

		List<TekelekMessage> unitMsgs = new ArrayList<TekelekMessage>();
		
		try (Connection conn = DriverManager.getConnection(UtilDAL.connUrl, UtilDAL.username, UtilDAL.password);
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
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			log.error("ERROR: Can't create instance of driver" + ex.getMessage());
		}

		String spCall = "{ call markTekelekMessageAsSent(?) }";
		log.info("SP Call: " + spCall);

		try (Connection conn = DriverManager.getConnection(UtilDAL.connUrl, UtilDAL.username, UtilDAL.password);
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
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			log.error("ERROR: Can't create instance of driver" + ex.getMessage());
		}

		String spCall = "{ call GetUnitBySerialNo(?, ?) }";
		log.info("SP Call: " + spCall);
		
		Unit unit = new Unit();
		
		try (Connection conn = DriverManager.getConnection(UtilDAL.connUrl, UtilDAL.username, UtilDAL.password);
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
