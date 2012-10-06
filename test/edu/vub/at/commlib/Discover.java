package edu.vub.at.commlib;

import java.io.IOException;

import edu.vub.at.nfcpoker.comm.PokerServer;

public class Discover {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CommLibConnectionInfo clci = CommLib.discover(PokerServer.class);
			System.out.println(clci);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
