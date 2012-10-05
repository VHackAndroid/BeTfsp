package edu.vub.at.commlib;

import java.util.HashMap;

public class CommLibConnectionInfo {
	public String serverType_;
	public HashMap<String, String> extra_;
	
	public CommLibConnectionInfo() {
		// Only for Kryo
	}
	
	CommLibConnectionInfo(String serverType, HashMap<String, String> extra) {
		serverType_ = serverType;
		extra_  = extra;
	}

	public String getAddress() {
		return extra_.get("address");
	}

	public String getPort() {
		return extra_.get("port");
	}
}
