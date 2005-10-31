package org.red5.server.rtmp.codec;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.protocol.ProtocolDecoderOutput;
import org.apache.mina.protocol.ProtocolSession;
import org.apache.mina.protocol.ProtocolViolationException;
import org.apache.mina.protocol.codec.CumulativeProtocolDecoder;
import org.red5.server.io.BufferUtils;
import org.red5.server.io.Deserializer;
import org.red5.server.io.amf.Input;
import org.red5.server.protocol.rtmp.RTMPUtils;
import org.red5.server.rtmp.Channel;
import org.red5.server.rtmp.Connection;
import org.red5.server.rtmp.message.AudioData;
import org.red5.server.rtmp.message.Constants;
import org.red5.server.rtmp.message.Handshake;
import org.red5.server.rtmp.message.InPacket;
import org.red5.server.rtmp.message.Invoke;
import org.red5.server.rtmp.message.Message;
import org.red5.server.rtmp.message.Notify;
import org.red5.server.rtmp.message.PacketHeader;
import org.red5.server.rtmp.message.Ping;
import org.red5.server.rtmp.message.StreamBytesRead;
import org.red5.server.rtmp.message.VideoData;
import org.red5.server.service.Call;
import org.red5.server.utils.BufferLogUtils;

public class ProtocolDecoder extends CumulativeProtocolDecoder implements Constants {

	protected static Log log =
        LogFactory.getLog(ProtocolDecoder.class.getName());
	
	private Deserializer deserializer = null;

	public ProtocolDecoder(){
		super(1024); // default capacity, auto expands
	}
	
	public void setDeserializer(Deserializer deserializer) {
		this.deserializer = deserializer;
	}
	
	protected boolean doDecode(ProtocolSession session, ByteBuffer in,
			 ProtocolDecoderOutput out) throws ProtocolViolationException {
		
		try {
			
			log.debug("doDecode: "+in.remaining());
		
			if(in.remaining() < 1) {
				log.debug("Empty read, buffering");
				return false;
			}
			
			final int startPosition = in.position();
			final Connection conn = (Connection) session.getAttachment();
			final byte headerByte = in.get();
			final byte channelId = RTMPUtils.decodeChannelId(headerByte);
			final Channel channel = conn.getChannel(channelId);
			final byte headerSize = (byte) RTMPUtils.decodeHeaderSize(headerByte);
			int headerLength = RTMPUtils.getHeaderLength(headerSize);
			
			if(headerLength > in.remaining()) {
				log.debug("Header too small, buffering");
				in.position(startPosition);
				return false;
			}
			
			PacketHeader header = null;
			
			if(conn.getState()==Connection.STATE_CONNECT){
				header = new PacketHeader();
				header.setChannelId(channelId);
				header.setSize(HANDSHAKE_SIZE);
				header.setDataType(TYPE_HANDSHAKE);
			} else if(conn.getState()==Connection.STATE_HANDSHAKE){
				if(in.remaining() < HANDSHAKE_SIZE){ 
					log.debug("Handshake reply too small, buffering");
					return false;
				}
				in.position(in.position()-1);
				in.skip(HANDSHAKE_SIZE-1);
				conn.setState(Connection.STATE_CONNECTED);
				return true;
			} else {
				in.position(in.position()-1);
				BufferLogUtils.debug(log,"buf",in);
				header = decodeHeader(in,channel.getLastReadHeader());
			}
				
			if(header==null) 
				throw new ProtocolViolationException("Header is null, check for error");
			
			log.debug(header);
			
			channel.setLastReadHeader(header);
			
			InPacket packet = channel.getInPacket();
			
			if(packet==null){
				packet = newPacket(header);
				channel.setInPacket(packet);
			}
			
			
			
			final ByteBuffer buf = packet.getMessage().getData();
			
			final int readRemaining = header.getSize() - buf.position();
			
			final int readAmount = (readRemaining > header.getChunkSize()) ? header.getChunkSize() : readRemaining;
			
			log.debug("Read amount: "+readAmount);
			
			if(in.remaining() < readAmount) {
				log.debug("Chunk too small, buffering");
				log.debug("Remaining: "+in.remaining());
				in.position(startPosition);
				return false;
			}
			
			BufferUtils.put(buf, in, readAmount);
			
			log.debug("Position" + buf.position());
			
			if(buf.position() >= header.getSize()){
				log.debug("Finished read, decode packet");
				buf.flip();
				decodeMessage(packet.getMessage());
				if(header.getDataType()==TYPE_HANDSHAKE){
					if(conn.getState()==Connection.STATE_CONNECT){
						conn.setState(Connection.STATE_HANDSHAKE);
					} 
				}
				out.write(packet);
				channel.setInPacket(null);
			} 
			return true;

		} catch (RuntimeException e) {
			log.error("Exception", e);
		}
		
		in.skip(in.remaining());
		return true;
	}
	
	public PacketHeader decodeHeader(ByteBuffer in, PacketHeader lastHeader){
		
		final byte headerByte = in.get();
		final byte channelId = RTMPUtils.decodeChannelId(headerByte);
		final byte headerSize = (byte) RTMPUtils.decodeHeaderSize(headerByte);
		PacketHeader header = new PacketHeader();
		header.setChannelId(channelId);
		
		switch(headerSize){
		
		case HEADER_NEW:
			if(log.isDebugEnabled())
				log.debug("0: Full headers");			
			header.setTimer(RTMPUtils.readMediumInt(in));
			header.setSize(RTMPUtils.readMediumInt(in));
			header.setDataType(in.get());
			header.setSource(in.getInt());
			break;
			
		case HEADER_SAME_SOURCE:
			if(log.isDebugEnabled())
				log.debug("1: Same source as last time");
			header.setTimer(RTMPUtils.readMediumInt(in));
			header.setSize(RTMPUtils.readMediumInt(in));
			header.setDataType(in.get());
			header.setSource(lastHeader.getSource());
			break;
			
		case HEADER_TIMER_CHANGE:
			if(log.isDebugEnabled())
				log.debug("2: Only the timer changed");
			header.setTimer(RTMPUtils.readMediumInt(in));
			header.setSize(lastHeader.getSize());
			header.setDataType(lastHeader.getDataType());
			header.setSource(lastHeader.getSource());
			break;
			
		case HEADER_CONTINUE:
			header = lastHeader;
			break;
			
		default:
			log.error("Unexpected header size: "+headerSize);
			return null;
		
		}
		return header;
	}
	
	public InPacket newPacket(PacketHeader header){
		final InPacket packet = new InPacket();
		packet.setSource(header);
		switch(header.getDataType()){
		case TYPE_HANDSHAKE:
			packet.setMessage(new Handshake());
			break;
		case TYPE_INVOKE:
			packet.setMessage(new Invoke());
			break;
		case TYPE_NOTIFY:
			packet.setMessage(new Notify());
			break;
		case TYPE_PING:
			packet.setMessage(new Ping());
			break;
		case TYPE_STREAM_BYTES_READ:
			packet.setMessage(new StreamBytesRead());
			break;
		case TYPE_AUDIO_DATA:
			packet.setMessage(new AudioData());
			break;
		case TYPE_VIDEO_DATA:
			packet.setMessage(new VideoData());
			break;
		}
		return packet;
	}
	
	public void decodeMessage(Message message) {
		switch(message.getDataType()){
		case TYPE_HANDSHAKE:
			// none needed
			break;
		case TYPE_INVOKE:
			decodeInvoke((Invoke) message);
			break;
		case TYPE_NOTIFY:
			decodeNotify((Notify) message);
			break;
		case TYPE_PING:
			decodePing((Ping) message);
			break;
		case TYPE_STREAM_BYTES_READ:
			decodeStreamBytesRead((StreamBytesRead) message);
			break;
		case TYPE_AUDIO_DATA:
			decodeAudioData((AudioData) message);
			break;
		case TYPE_VIDEO_DATA:
			decodeVideoData((VideoData) message);
			break;
		}
	}
	
	public void decodeInvoke(Invoke invoke){
		
		Input input = new Input(invoke.getData());
		
		String action = (String) deserializer.deserialize(input);
		int invokeId = ((Number) deserializer.deserialize(input)).intValue();
		invoke.setInvokeId(invokeId);
				
		Object[] params = new Object[]{};

		if(invoke.getData().hasRemaining()){
			ArrayList paramList = new ArrayList();
			
			// Often the first param is null, for connect its not
			// So if its null just ignore it and move on
			Object firstParam = deserializer.deserialize(input);
			if(firstParam!=null) paramList.add(firstParam);
			
			while(invoke.getData().hasRemaining()){
				paramList.add(deserializer.deserialize(input));
			}
			params = paramList.toArray();
			if(log.isDebugEnabled()){
				log.debug("Num params: "+paramList.size()); 
				for(int i=0; i<params.length; i++){
					log.debug(" > "+i+": "+params[i]);
				}
			}
		} 
		
		final int dotIndex = action.indexOf(".");
		String serviceName = (dotIndex==-1) ? null : action.substring(0,dotIndex);
		String serviceMethod = (dotIndex==-1) ? action : action.substring(dotIndex+1, action.length());
		
		Call call = new Call(serviceName,serviceMethod,params);
		
		invoke.setCall(call);
	}
	
	public void decodeNotify(Notify notify){

	}
	
	public void decodePing(Ping ping){

	}
	
	public void decodeStreamBytesRead(StreamBytesRead streamBytesRead){

	}
	
	public void decodeAudioData(AudioData audioData){

	}
	
	public void decodeVideoData(VideoData videoData){

	}
	
}
