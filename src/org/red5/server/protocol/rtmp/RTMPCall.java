package org.red5.server.protocol.rtmp;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright � 2006 by respective authors. All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 2.1 of the License, or (at your option) any later 
 * version. 
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this library; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Luke Hubbard, Codegent Ltd (luke@codegent.com)
 */

import org.red5.server.service.Call;

public class RTMPCall extends Call {

	protected int packetId = -1;
	protected int packetSource = -1;
	protected Channel channel;

	public RTMPCall(String serviceName, String serviceMethod, Object[] args, int packetId, int packetSource, Channel channel){
		super(serviceName, serviceMethod, args);
		this.packetId = packetId;
		this.packetSource = packetSource;
		this.channel = channel;
	}

	public Channel getChannel() {
		return channel;
	}

	public int getPacketId(){
		return packetId;
	}

	public int getPacketSource() {
		return packetSource;
	}
	
}
