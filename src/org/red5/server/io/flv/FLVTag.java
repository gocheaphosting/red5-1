package org.red5.server.io.flv;

public class FLVTag {
	
	private byte tagType; //audio=8, video=9
	private byte[] dataSize;
	private byte[] timeStamp;
	private int reserved;
	private byte[] data; // audio or video data

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getDataSize() {
		return dataSize;
	}

	public void setDataSize(byte[] dataSize) {
		this.dataSize = dataSize;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(int reserved) {
		this.reserved = reserved;
	}

	public byte getTagType() {
		return tagType;
	}

	public void setTagType(byte tagType) {
		this.tagType = tagType;
	}

	public byte[] getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(byte[] timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
