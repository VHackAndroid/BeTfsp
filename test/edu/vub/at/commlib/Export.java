package edu.vub.at.commlib;

import java.io.IOException;
import java.util.HashMap;

import edu.vub.at.nfcpoker.comm.PokerServer;

public class Export {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HashMap<String, String> extra = new HashMap<String, String>();
		extra.put("address", "192.168.1.135");
		extra.put("port", "1234");
		CommLibConnectionInfo clci = new CommLibConnectionInfo(PokerServer.class.getCanonicalName(), extra);
		try {
			CommLib.export(clci);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
