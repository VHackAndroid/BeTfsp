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

import java.util.UUID;

import android.util.Log;

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
		if (listener != null) {
			listener.resolve(value);
			listener = null;
		}
	}
	
	public boolean isResolved() {
		return value != null;
	}
	
	public synchronized T get() throws InterruptedException {
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
			Log.d("wePoker - Future", "Future was interrupted in get()");
			throw e;
		}
		return value;
	}
	
	public void setFutureListener(FutureListener<T> fl) {
		listener = fl;
	}
	
	public UUID getFutureId() {
		return id;
	}

	public T unsafeGet() {
		return value;
	}
}
