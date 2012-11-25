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

	public boolean isDedicated() {
		return Boolean.parseBoolean(extra_[2]);
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
