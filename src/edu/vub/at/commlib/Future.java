package edu.vub.at.commlib;

import java.util.UUID;

public class Future<T> {
	public interface FutureListener<T> {
		void resolve(T value);
	}
	
	public Future(FutureListener<T> fl) {
		listener = fl;
	}
	
	UUID id = UUID.randomUUID();
	FutureListener<T> listener;
	T value;
	
	void resolve(T value) {
		this.value = value;
		if (listener != null)
			listener.resolve(value);
	}
	
	synchronized T get() {
		setFutureListener(new FutureListener<T>() {
			@Override
			public void resolve(T value) {
				synchronized (Future.this) {
					Future.this.notify();
				}
			}
		});
		try {
			this.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	void setFutureListener(FutureListener<T> fl) {
		listener = fl;
	}
	
	UUID getFutureId() {
		return id;
	}
}
