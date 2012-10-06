package edu.vub.at.commlib;

import java.util.UUID;

public class Future<T> {
	public interface FutureListener<T> {
		void resolve(T value);
	}
	
	public Future() {
		
	}
	
	public Future(FutureListener<T> fl) {
		listener = fl;
	}
	
	UUID id = UUID.randomUUID();
	FutureListener<T> listener;
	T value;
	
	public void resolve(T value) {
		this.value = value;
		if (listener != null)
			listener.resolve(value);
	}
	
	public synchronized T get() {
		if (value != null)
			return value;
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
	
	public void setFutureListener(FutureListener<T> fl) {
		listener = fl;
	}
	
	public UUID getFutureId() {
		return id;
	}
}
