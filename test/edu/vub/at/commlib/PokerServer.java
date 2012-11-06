package edu.vub.at.commlib;

import edu.vub.at.nfcpoker.comm.GameServer;
import edu.vub.at.nfcpoker.ui.DummServerView;

public class PokerServer {
	public static void main(String[] args) {
		String ipAddress = "127.0.0.1";
		if (args.length > 0) ipAddress = args[0];
		
		GameServer cps = new GameServer(new DummServerView(), true, ipAddress, ipAddress);
		cps.start();
		while (true) {
			try { Thread.sleep(100000);
			} catch (InterruptedException e) { }
		}
	}
}
