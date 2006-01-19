package org.red5.io.flv;

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
 * @author Dominick Accattato (daccattato@gmail.com)
 * @author Luke Hubbard, Codegent Ltd (luke@codegent.com)
 */

import org.apache.mina.common.ByteBuffer;

/**
 * A Tag represents the contents or payload of a FLV file
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Dominick Accattato (daccattato@gmail.com)
 * @author Luke Hubbard, Codegent Ltd (luke@codegent.com)
 * @version 0.3
 */
public interface Tag {

	public static final byte TYPE_VIDEO = 0x09;
	public static final byte TYPE_AUDIO = 0x08;
	public static final byte TYPE_METADATA = 0x12;
	
	
	/**
	 * Return the body ByteBuffer
	 * @return ByteBuffer
	 */
	public ByteBuffer getBody();
	
	/**
	 * Return the size of the body
	 * @return int
	 */
	public int getBodySize();
	
	/**
	 * Get the data type
	 * @return byte
	 */
	public byte getDataType();
	
	/**
	 * Return the timestamp
	 * @return int
	 */
	public int getTimestamp();
	

	/**
	 * Returns the data as a ByteBuffer
	 * @return ByteBuffer buf
	 */
	public ByteBuffer getData();
	
	/**
	 * Returns the data as a ByteBuffer
	 * @return ByteBuffer buf
	 */
	public int getPreviousTagSize();
	
	/**
	 * Return the body ByteBuffer
	 * @return ByteBuffer
	 */
	public void setBody(ByteBuffer body);
	
	/**
	 * Return the size of the body
	 * @return int
	 */
	public void setBodySize(int size);
	
	/**
	 * Get the data type
	 * @return byte
	 */
	public void setDataType(byte datatype);
	
	/**
	 * Return the timestamp
	 * @return int
	 */
	public void setTimestamp(int timestamp);
	

	/**
	 * Returns the data as a ByteBuffer
	 * @return ByteBuffer buf
	 */
	public void setPreviousTagSize(int size);

}
