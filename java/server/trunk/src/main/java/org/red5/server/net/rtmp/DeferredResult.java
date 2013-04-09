/*
 * RED5 Open Source Flash Server - http://code.google.com/p/red5/
 * 
 * Copyright 2006-2012 by respective authors (see below). All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.red5.server.net.rtmp;

import java.lang.ref.WeakReference;

import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.net.rtmp.event.Invoke;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Can be returned to delay returning the result of invoked methods.
 * 
 * @author The Red5 Project
 * @author Joachim Bauch (jojo@struktur.de)
 */
public class DeferredResult {
	/**
	 * Logger
	 */
	protected static Logger log = LoggerFactory.getLogger(DeferredResult.class);

	/**
	 * Weak reference to used channel
	 */
	private WeakReference<Channel> channel;

	/**
	 * Pending call object
	 */
	private IPendingServiceCall call;

	/**
	 * Invocation id
	 */
	private int invokeId;

	/**
	 * Results sent flag
	 */
	private boolean resultSent = false;

	/**
	 * Set the result of a method call and send to the caller.
	 * 
	 * @param result deferred result of the method call
	 */
	public void setResult(Object result) {
		if (resultSent) {
			throw new RuntimeException("You can only set the result once.");
		}
		this.resultSent = true;
		Channel channel = this.channel.get();
		if (channel == null) {
			log.warn("The client is no longer connected.");
			return;
		}
		Invoke reply = new Invoke();
		call.setResult(result);
		reply.setCall(call);
		reply.setInvokeId(invokeId);
		channel.write(reply);
		channel.getConnection().unregisterDeferredResult(this);
	}

	/**
	 * Check if the result has been sent to the client.
	 * 
	 * @return <code>true</code> if the result has been sent, otherwise <code>false</code> 
	 */
	public boolean wasSent() {
		return resultSent;
	}

	/**
	 * Setter for invoke Id.
	 *
	 * @param id  Invocation object identifier
	 */
	public void setInvokeId(int id) {
		this.invokeId = id;
	}

	/**
	 * Setter for service call.
	 *
	 * @param call  Service call
	 */
	public void setServiceCall(IPendingServiceCall call) {
		this.call = call;
	}

	/**
	 * Setter for channel.
	 *
	 * @param channel  Channel
	 */
	public void setChannel(Channel channel) {
		this.channel = new WeakReference<Channel>(channel);
	}
	
}
