package com.qorporation.msgs.server.messenger.endpoint;

import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.net.ssl.SSLEngine;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.ssl.SslHandler;

import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Serialization;

public class MessengerEndpointPipelineFactory implements ChannelPipelineFactory {

	private MessengerEndpointHandler handler = null;
	
	public MessengerEndpointPipelineFactory(MessengerEndpointHandler handler) {
		this.handler = handler;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		
        SSLEngine engine = MessengerEndpointSslContextFactory.getServerContext().createSSLEngine();
        engine.setUseClientMode(false);

        if (MessengerEndpoint.USE_SSL) {
        	pipeline.addLast("ssl", new SslHandler(engine));
        }
        
		pipeline.addLast("decoder", new Decoder());
		pipeline.addLast("encoder", new Encoder());
		
		pipeline.addLast("handler", this.handler);
		return pipeline;
	}
	
	protected class Decoder extends FrameDecoder {

		@Override
		protected Object decode(ChannelHandlerContext ctx, Channel arg, ChannelBuffer buf) throws Exception {
			if (buf.readableBytes() < 4) {
				return null;
			}

			buf.markReaderIndex();
			int length = buf.readInt();
			int readable = buf.readableBytes();
			
			if (readable < length) {
				buf.resetReaderIndex();
				return null;
			}
			
			ChannelBuffer frame = buf.readBytes(length);
			return Serialization.deserializeString(frame.array());
		}
		
		protected byte[] decompress(byte[] compressed) {
			try {
				Inflater inflater = new Inflater();
				inflater.setInput(compressed);
				
				byte[] decompressed = new byte[inflater.getTotalOut()];
				inflater.inflate(decompressed);
				
				return decompressed;
			} catch (Exception e) {
				ErrorControl.logException(e);
				return new byte[0];
			}
		}
	
	}
	
	protected class Encoder extends LengthFieldPrepender {

		public Encoder() {
			super(4);
		}
		
	    @Override
	    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
			if (!(msg instanceof String)) {
			    return msg;
			}
			
			return super.encode(ctx, channel, ChannelBuffers.wrappedBuffer(Serialization.serialize((String) msg)));
	    }
	    
	    protected byte[] compress(byte[] uncompressed) {
	    	Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
	    	deflater.setInput(uncompressed);
	    	
	    	byte[] compressed = new byte[deflater.getTotalOut()];
	    	deflater.deflate(compressed);

	    	return compressed;
	    }

	}

}
