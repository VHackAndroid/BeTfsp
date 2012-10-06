package edu.vub.at.commlib;

import java.util.UUID;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class UUIDSerializer extends Serializer<UUID> {

	@Override
	public void write(Kryo kryo, Output output, UUID u) {
		output.writeLong(u.getMostSignificantBits());
		output.writeLong(u.getLeastSignificantBits());
	}

	@Override
	public UUID read(Kryo kryo, Input input, Class<UUID> k) {
		long msb = input.readLong();
		long lsb = input.readLong();
		return new UUID(msb, lsb);
	}
}