package edu.vub.at.commlib;

import java.io.IOException;


import edu.vub.at.nfcpoker.comm.PokerServer;

public class Export {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String ipAddress = "127.0.0.1";
		if (args.length > 0) ipAddress = args[0];
		CommLibConnectionInfo clci = new CommLibConnectionInfo(
				PokerServer.class.getCanonicalName(),
				new String[] { ipAddress, "1234" });
		try {
			CommLib.export(clci, ipAddress);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
