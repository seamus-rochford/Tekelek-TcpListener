package com.trandonsystems.tekelek.threads;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.trandonsystems.tekelek.model.TekelekMessage;
import com.trandonsystems.tekelek.services.Hex;
import com.trandonsystems.tekelek.services.UnitServices;

public class ListenerThread extends Thread {

	static Logger log = Logger.getLogger(ListenerThread.class);
    private Socket socket;
    UnitServices unitServices = new UnitServices();
    
    public ListenerThread(Socket socket) {
        this.socket = socket;
    }
 
    private String buildMessage(List<TekelekMessage> tekelekMsgs) {
    	
    	// Start the message with password
    	String msg = "3&x!yz";
    	
    	for (int i = 0; i < tekelekMsgs.size(); i++) {
    		msg += ',' + tekelekMsgs.get(i).message;
    	}
    	
    	// Set Unit to "ACTIVE" state 
    	msg += ",R3=ACTIVE";
    	
    	// Check if "R1" msg included already - if yes do nothing otherwise add ",R1=80"
    	int index = msg.indexOf(",R1=");
    	
    	if (index < 0) {
    		// Tell unit Shut down and sleep
    		msg += ",R1=80";
    	}

    	return msg;
    }
    
    public void run() {
        try {
        	log.debug("Start communication - Message received - processing");
        	
        	// Takes input from the client socket
        	DataInputStream input = new DataInputStream(socket.getInputStream());
        	// Writes on client socket
        	DataOutputStream output = new DataOutputStream(socket.getOutputStream());

    		// Receiving data from client
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		byte buffer[] = new byte[1024];
    		baos.write(buffer, 0, input.read(buffer));
    		
    		byte data[] = baos.toByteArray();
    		log.debug("Recieved from client (Hex): " + Hex.ByteArrayToHex(data) + " Byte Size: " + data.length); 
    		
    		// do NOT check msg length until after raw-data is saved
    		List<TekelekMessage> unitMsgs = unitServices.saveUnitReading(data);

    		// build message to send back to client - if there is one
    		String replyMessage = buildMessage(unitMsgs);
    		log.debug("Msg to Sensor: " + replyMessage);
//    		log.debug("Msg to Sensor (Hex): " + Hex.ByteArrayToHex(replyMessage.getBytes(StandardCharsets.UTF_8)));
    		
    		// output.write sends the message
			output.write(replyMessage.getBytes(StandardCharsets.UTF_8));
			
			// Mark messages as sent
			for (int i = 0; i < unitMsgs.size(); i++) {
				unitServices.markMessageAsSent(unitMsgs.get(i));
			}

			log.debug("Message sent to unit");
			
    		// Close the connection and the streams
            socket.close();
            input.close();
            output.close();

        } catch (IOException exIO) {
            log.error("Server IO exception: " + exIO.getMessage());
    		exIO.printStackTrace();
//            log.error("Stack Trace: " + exIO.getStackTrace().toString());
    	} catch (SQLException exSQL) {
    		log.error("Server SQL exception: " + exSQL.getMessage());
    		exSQL.printStackTrace();
//            log.error("Stack Trace: " + exSQL.getStackTrace().toString());
        } catch (Exception ex) {
            log.error("Server exception: " + ex.getMessage());
            ex.printStackTrace();
//            log.error("Stack Trace: " + ex.getStackTrace().toString());
    	} finally {
            log.info("Communication ended");            
    	}
    }
    
}
