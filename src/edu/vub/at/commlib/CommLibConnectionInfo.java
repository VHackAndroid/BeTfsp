package edu.vub.at.commlib;

import java.util.Map;

public class CommLibConnectionInfo {
	public String serverType_;
	public String[] extra_;
	public CommLibConnectionInfo() {
		// Only for Kryo
	}
	
	CommLibConnectionInfo(String serverType, String[] extra) {
		serverType_ = serverType;
		extra_  = extra;
	}

	public String getAddress() {
		return extra_[0];
	}

	public String getPort() {
		return extra_[1];
	}
}
