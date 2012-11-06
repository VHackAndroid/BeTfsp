package edu.vub.at.commlib;

import java.io.IOException;

import edu.vub.at.nfcpoker.comm.PokerServer;

public class Discover {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String ipAddress = "127.0.0.1";
			if (args.length > 0) ipAddress = args[0];
			CommLibConnectionInfo clci = CommLib.discover(PokerServer.class, ipAddress);
			System.out.println(clci);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
