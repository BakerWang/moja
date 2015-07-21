package com.song.moja.netty;

import com.google.protobuf.MessageLite;
import com.song.moja.protobuf.LogProbuf.Log;
import com.song.moja.server.ServerConfig;
import com.song.moja.server.ThreadManager;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.codec.string.StringEncoder;

//处理protobuf的编解码
public class ProtobufNettyServerInitializer extends ChannelInitializer<SocketChannel> {
	final ThreadManager threadManager;
	final ServerConfig config;

	public ProtobufNettyServerInitializer(ThreadManager threadManager,
			ServerConfig config) {
		this.threadManager = threadManager;
		this.config = config;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {

		ChannelPipeline pipeline = ch.pipeline();

		// 解码
		pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(
				1048576, 0, 4, 0, 4));

		// 这个地方从配置文件中读出类。。。
//		String pbLogClassName = config.getPBLogClassName();
//		Class pbLogClass = Class.forName(pbLogClassName);
//		MessageToMessageDecoder msg = MessageToMessageDecoder;
//		
//		MessageLite ml = (MessageLite)pbLogClass;
		// 构造函数传入要解码成的类实例，null替换成日志对应的protobuf日志对象
		pipeline.addLast("protobufDecoder", new ProtobufDecoder(Log.getDefaultInstance()));

		// 编码
		// pipeline.addLast("frameEncoder", new
		// ProtobufVarint32LengthFieldPrepender());
		// pipeline.addLast("protobufEncoder", new ProtobufEncoder());
		// 业务逻辑处理类
		pipeline.addLast("handler", new NettyServerHandler(threadManager,
				config));

	}
}