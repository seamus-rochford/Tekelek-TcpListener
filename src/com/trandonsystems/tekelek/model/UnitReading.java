package com.trandonsystems.tekelek.model;

import java.time.Instant;

public class UnitReading {

	public long snitId;
	
	public String serialNo;
	
	// data
	public int msgType;
	
	public int binLevel;		// unsigned
	public int binLevelBC;		// unsigned
	public int noFlapOpening;	// unsigned
	public int batteryVoltage;	// unsigned
	public int temperature;		// *** signed ***
	public int noCompactions;	// unsigned
	
	// Signal strengths
	public int nbIoTSignalStrength;
	
	//flags
	public boolean batteryUVLO;
	public boolean binEmptiedLastPeriod;
	public boolean batteryOverTempLO;
	public boolean binLocked;
	public boolean binFull;
	public boolean binTilted;
	public boolean serviceDoorOpen;
	public boolean flapStuckOpen;

	public double rssi;
	public int src;
	public double snr;
	public int ber;
	
	public int contactReason;   
	// Contact Reason has 8 bits - will be set as follows
	//	1 = Scheduled reading, 
	//	2 = Alarm (triggered by alarms (S4, S5, S6, S7 or S8), 
	//	3 = Server Request (not using this), 
	//	4 = Manual (trigger using magnet next to unit)
	//	5 = Reboot (not using this)
	//	6 = TSP Requested (not using this)
	//	7 = dynLim Status (set if S7 fired an alarm)
	//	8 = dynLim2 Status (set if S8 fired an alarm)
	
	public int alarmStatus;
	// AlarmStatus has 8 bits - they are set as follows:
	//	1 = Limit1 (set if S4 fired alarm)
	//	2 = Limit2 (set if S5 fired alarm)
	//	3 = Limit3 (set if S6 fired alarm)
	//	bits 4 - 8  (not using)
	
	public Instant readingDateTime;
	
}
