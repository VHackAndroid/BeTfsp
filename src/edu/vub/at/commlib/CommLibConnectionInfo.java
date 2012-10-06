package edu.vub.at.commlib;

import java.io.IOException;
import java.util.UUID;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;

public class CommLibConnectionInfo {
	public String serverType_;
	public String[] extra_;
	public CommLibConnectionInfo() {
		// Only for Kryo
	}
	
	public CommLibConnectionInfo(String serverType, String[] extra) {
		serverType_ = serverType;
		extra_  = extra;
	}

	public String getAddress() {
		return extra_[0];
	}

	public String getPort() {
		return extra_[1];
	}

	public Client connect(Listener listener) throws IOException {
		return connect(getAddress(), Integer.parseInt(getPort()), listener);
	}
	
	public static Client connect(String ipAddress, int port, Listener listener) throws IOException {
		Client ret = new Client();
		ret.start();
		Kryo k = ret.getKryo();
		k.setRegistrationRequired(false);
		k.register(UUID.class, new UUIDSerializer());
		if (listener != null)
			ret.addListener(listener);
		ret.connect(5000, ipAddress, port);

		return ret;
	}
}
