package com.trandonsystesm.test;

import com.trandonsystems.tekelek.model.UnitMessage;
import com.trandonsystems.tekelek.services.Hex;
import com.trandonsystems.tekelek.services.UnitServices;

public class Test {

	private static void TestSaveUnitReading() {
	
//		byte[] data = {(byte)0x01, // Message Type
//				(byte)0x3D, // Bin Level
//				(byte)0xA4, // Bin Level BC
//				(byte)0x12, // Flap openings 
//				(byte)0xCA,  
//				(byte)0x21, // Battery Voltage
//				(byte)0x12, // Temp.
//				(byte)0x86, // NoCompactions
//				(byte)0xFF, // Flags All set
//				(byte)0x9B, // Signal Strengths
//				(byte)0x00, 
//				(byte)0x00, 
//				(byte)0x04, // Serial No length
//				(byte)0xAC, // Serial Number
//				(byte)0x2B, 
//				(byte)0xE7, 
//				(byte)0xB6, 
//				(byte)0x00, 
//				(byte)0x00, 
//				(byte)0x00, 
//				(byte)0x00,
//				(byte)0x00, 
//				(byte)0x00, 
//				(byte)0x00, 
//				(byte)0x00, 
//				(byte)0x00, 
//				(byte)0x00,
//				(byte)0x00, 
//				(byte)0x00};
//		byte[] data = {(byte)0x01, // Message Type
//				(byte)0x28, // Bin Level
//				(byte)0x48, // Bin Level BC
//				(byte)0x00, // Flap openings 
//				(byte)0x55,  
//				(byte)0x44, // Battery Voltage
//				(byte)0x66, // Temp.
//				(byte)0x34, // NoCompactions
//				(byte)0x5A, // Flags All set
//				(byte)0x76, // Signal Strengths
//				(byte)0x00, 
//				(byte)0x00, 
//				(byte)0x04, // Serial No length
//				(byte)0xac, // Serial Number
//				(byte)0x2b, 
//				(byte)0xe7, 
//				(byte)0xb6, 
//				(byte)0x00, 
//				(byte)0x00, 
//				(byte)0x00, 
//				(byte)0x00,
//				(byte)0x00, 
//				(byte)0x00, 
//				(byte)0x00, 
//				(byte)0x00, 
//				(byte)0x00, 
//				(byte)0x00,
//				(byte)0x00, 
//				(byte)0x00};
		
		// Tekelek sample
		// msg = 0509E30187047D0863835023729515047B005D8A00000084020C0A7C2999097C2998087E2997077E2996067E2995057E29940480299303802992028029910180299000800000008000000080299A0082299A0082299A008429990086299800882999008829990088299900862998008629990086299802842853000000000000000000000000000000002358
		String hexStr = "0509E30187047D0863835023729515047B005D8A00000084020C0A7C2999097C2998087E2997077E2996067E2995057E29940480299303802992028029910180299000800000008000000080299A0082299A0082299A008429990086299800882999008829990088299900862998008629990086299802842853000000000000000000000000000000002358";
		byte[] data = Hex.hexStringToByteArray(hexStr);

		UnitServices unitServices = new UnitServices();
		
		try {
			UnitMessage unitMsg = unitServices.saveUnitReading(data);
			System.out.println(unitMsg);
		}
		catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		TestSaveUnitReading();
	}

}
