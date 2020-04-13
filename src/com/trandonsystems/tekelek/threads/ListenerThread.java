package com.trandonsystems.tekelek.threads;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import org.apache.log4j.Logger;

import com.trandonsystems.tekelek.model.UnitMessage;
import com.trandonsystems.tekelek.services.UnitServices;

public class ListenerThread extends Thread {

	static Logger log = Logger.getLogger(ListenerThread.class);
    private Socket socket;
    UnitServices unitServices = new UnitServices();
    
    public ListenerThread(Socket socket) {
        this.socket = socket;
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
    		
//    		for (int i = 0; i < result.length; i++) {
//    			log.debug(i + ":" + (result[i] & 0xFF));
//    		}
    		log.debug("Recieved from client (bytes): " + data + " Byte Size: " + data.length); 
    		String inStr = Arrays.toString(data);
    		log.debug("Recieved from client (numbers): " + inStr); 
    		
    		// do NOT check msg length until after raw-data is saved
    		UnitMessage unitMsg = unitServices.saveUnitReading(data);
    		
    		// send message back to client - if there is one
    		if (unitMsg.replyMessage) {
    			// output.write sends the message
    			output.write(unitMsg.message);
    			// Mart the message as sent so it will NOT be sent again
    			unitServices.markMessageAsSent(unitMsg);
    			log.debug("Message set to unitId: " + unitMsg.unitId + " messageId: " + unitMsg.messageId + "   Message (bytes): " + unitMsg.message + "    Message(numbers): " + Arrays.toString(unitMsg.message));
    		} else {
    			// Send the default message
    			String defaultMessage = "TEK811,R3=ACTIVE,R1=80";
    			output.write(defaultMessage.getBytes(StandardCharsets.UTF_8));
    		}
	    	
    		// Close the connection and the streams
            socket.close();
            input.close();
            output.close();

        } catch (IOException exIO) {
            log.error("Server exception: " + exIO.getMessage());
    		exIO.printStackTrace();
//            log.error("Stack Trace: " + exIO.getStackTrace().toString());
    	} catch (SQLException exSQL) {
    		log.error("Server exception: " + exSQL.getMessage());
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
