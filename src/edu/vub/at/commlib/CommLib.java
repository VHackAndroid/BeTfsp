/*
 * wePoker: Play poker with your friends, wherever you are!
 * Copyright (C) 2012, The AmbientTalk team.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package edu.vub.at.commlib;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import edu.vub.at.nfcpoker.comm.Message.ClientAction;

public class CommLib {

	public static final int DISCOVERY_PORT = 54333;
	public static final int SERVER_PORT = 54334;
	
	private static final int DISCOVERY_TIMEOUT = 10000;
	private static final int EXPORT_INTERVAL = 2000;
	
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

	public static String getIpAddress(WifiManager wm) {
		return putAddress(wm.getDhcpInfo().ipAddress);
	}
	
	public static String getBroadcastAddress(WifiManager wm) {
		DhcpInfo dhcp = wm.getDhcpInfo();
		if (dhcp == null) return "localhost";
		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		return putAddress(broadcast);
	}
	
	public static String getWifiGroupName(WifiManager wm) {
		return wm.getConnectionInfo().getSSID();
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
		
		DatagramSocket ds = new DatagramSocket(DISCOVERY_PORT, InetAddress.getByName(broadcastAddress));
		ds.setBroadcast(true);
		ds.setReuseAddress(true);
		ds.setSoTimeout(DISCOVERY_TIMEOUT);
		DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);
		try {
			ds.receive(dp);
			CommLibConnectionInfo clci = k.readObject(new Input(dp.getData()), CommLibConnectionInfo.class);
			if (clci.serverType_.equals(targetClass)) {
				ds.close();
				return clci;
			}
			return null;
		} catch (InterruptedIOException e) {
			// blocked for 10 seconds without a result.
			return null;
		} finally {
			ds.close();
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
				Thread.sleep(EXPORT_INTERVAL);
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
