package edu.vub.at.commlib;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import edu.vub.at.nfcpoker.comm.Message.ClientAction;

public class CommLib {

	public static final int DISCOVERY_PORT = 54333;
	public static final int SERVER_PORT = 54334;
	
	private static final int TIMEOUT_EXPORT = 10000;
	
	@SuppressWarnings("rawtypes")
	public static Map<UUID, Future> futures = new HashMap<UUID, Future>();
	
	private static Map<String,String> wifiConnections = new HashMap<String, String>();
	static {
		wifiConnections.put("androidVHack", "android55");
	}

	private static String putAddress(int addr) {
		StringBuffer buf = new StringBuffer();
		buf.append(addr  & 0xff).append('.').
		append((addr >>>= 8) & 0xff).append('.').
 		append((addr >>>= 8) & 0xff).append('.').
 		append((addr >>>= 8) & 0xff);
		return buf.toString();
	}

	public static String getIpAddress(Context ctx) {
		WifiManager m = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
		return putAddress(m.getDhcpInfo().ipAddress);
	}
	
	public static String getBroadcastAddress(Context ctx) {
		WifiManager m = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = m.getDhcpInfo();
		if (dhcp == null) return "localhost";
		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		return putAddress(broadcast);
	}

	public static String getWifiPassword(String ssid) {
		if (wifiConnections.containsKey(ssid)) {
			return wifiConnections.get(ssid);
		} else {
			return "********";
		}
	}
	
	public static CommLibConnectionInfo discover(Class<?> klass, String broadcastAddress) throws IOException {
		final String targetClass = klass.getCanonicalName();
		Kryo k = new Kryo();
		k.setRegistrationRequired(false);
		k.register(CommLibConnectionInfo.class);
		k.register(UUID.class, new UUIDSerializer());
//		Log.d("wePoker - CommLib", "Discovering on broadcast: "+InetAddress.getByName(broadcastAddress));
		DatagramSocket ds = new DatagramSocket(DISCOVERY_PORT, InetAddress.getByName(broadcastAddress));
		ds.setBroadcast(true);
		ds.setReuseAddress(true);
		DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);
		while (true) {
			ds.receive(dp);
			CommLibConnectionInfo clci = k.readObject(new Input(dp.getData()), CommLibConnectionInfo.class);
			if (clci.serverType_.equals(targetClass)) {
				ds.close();
				return clci;
			}
		}
	}
	
	public static void export(CommLibConnectionInfo clci, String broadcastAddress) throws IOException {
		Kryo k = new Kryo();
		k.setRegistrationRequired(false);
		k.register(CommLibConnectionInfo.class);
		k.register(UUID.class, new UUIDSerializer());
		Output o = new Output(1024);
		k.writeObject(o, clci);
		final byte[] buf = o.toBytes();

//		Log.d("wePoker - CommLib", "Exporting on broadcast: "+InetAddress.getByName(broadcastAddress));
		DatagramSocket ds = new DatagramSocket();
		ds.setBroadcast(true);
		ds.setReuseAddress(true);
		DatagramPacket dp = new DatagramPacket(buf, buf.length);
		ds.connect(new InetSocketAddress(InetAddress.getByName(broadcastAddress), DISCOVERY_PORT));
		while (true) {
			ds.send(dp);
			try {
				Thread.sleep(TIMEOUT_EXPORT);
			} catch (InterruptedException e) { }
		}
	}
	
	public static Future<ClientAction> createFuture() {
		Future<ClientAction> f = new Future<ClientAction>(null);
		futures.put(f.getFutureId(), f);
		return f;
	}

	public static void resolveFuture(UUID futureId, Object futureValue) {
		@SuppressWarnings("unchecked")
		Future<Object> f = futures.remove(futureId);
		if (f == null) {
//			Log.w("wePoker - CommLib", "Future null!");
			return;
		}
		f.resolve(futureValue);	
	}
}
