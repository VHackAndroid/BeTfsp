package edu.vub.at.commlib;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import edu.vub.at.nfcpoker.comm.PokerServer;

public class FoldingClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CommLibConnectionInfo clci = CommLib.discover(PokerServer.class);
			System.out.println("Discovered server at " + clci.getAddress());
			Client c = clci.connect(new Listener() {
				@Override
				public void received(Connection c, Object m) {
					super.received(c, m);
					System.out.println("Received message " + m.toString());
				}
			});
			while (true) {
				Thread.sleep(10000);
			}
		} catch (IOException e) {
			System.err.println("Could not discover server: ");
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	

}
