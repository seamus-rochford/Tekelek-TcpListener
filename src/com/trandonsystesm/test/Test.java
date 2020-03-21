package com.trandonsystesm.test;

import com.trandonsystems.tekelek.model.UnitMessage;
import com.trandonsystems.tekelek.services.Hex;
import com.trandonsystems.tekelek.services.UnitServices;

public class Test {

	private static void TestSaveUnitReading() {

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
