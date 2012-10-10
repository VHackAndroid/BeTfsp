package edu.vub.at.commlib;

import java.io.IOException;


import edu.vub.at.nfcpoker.comm.PokerServer;

public class Export {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CommLibConnectionInfo clci = new CommLibConnectionInfo(
				PokerServer.class.getCanonicalName(),
				new String[] { "192.168.1.135", "1234" });
		try {
			CommLib.export(clci, "192.168.1.255");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
