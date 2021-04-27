package com.trandonsystems.tekelek.model;

import java.time.Instant;

// This object is used to get records from the alert table 
// It is the same as "AlertObject" in the API-Server application
public class Alert {

	public int id;
	public int alertType;
	public int unitId;
	public Instant alertDateTime;
	public int status;
	public int unitReadingId;
	public String comments;
	public int damageId;
	
}
