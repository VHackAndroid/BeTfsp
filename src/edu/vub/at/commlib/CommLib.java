package edu.vub.at.commlib;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class CommLib {

	public static CommLibConnectionInfo discover(Class<?> klass) throws IOException {
		final String targetClass = klass.getCanonicalName();
		Kryo k = new Kryo();
		k.setRegistrationRequired(false);
		k.register(String.class);
		k.register(HashMap.class);
		k.register(CommLibConnectionInfo.class);
		
		DatagramSocket ds = new DatagramSocket(54333);
		DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);
		while (true) {
			ds.receive(dp);
			CommLibConnectionInfo clci = k.readObject(new Input(dp.getData()), CommLibConnectionInfo.class);
			if (clci.serverType_.equals(targetClass)) {
				return clci;
			}
		}
	}
	
	public static void export(CommLibConnectionInfo clci) throws IOException {
		Kryo k = new Kryo();
		k.setRegistrationRequired(false);
		k.register(String.class);
		k.register(HashMap.class);
		k.register(CommLibConnectionInfo.class);
		Output o = new Output(1024);
		k.writeObject(o, clci);
		final byte[] buf = o.toBytes();
		
		DatagramSocket ds = new DatagramSocket();
		DatagramPacket dp = new DatagramPacket(buf, buf.length);
		ds.connect(new InetSocketAddress(InetAddress.getByName("192.168.1.255"), 54333));
		while (true) {
			ds.send(dp);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}
	}
}
