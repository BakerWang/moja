package com.song.moja.server;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.song.moja.netty.NettyServer;
import com.song.moja.util.Constance;
import com.song.moja.zk.ServiceProvider;

/**
 * 服务器类，负责启动各种Netty监听服务，各种系统线程
 * @author Administrator
 * @param <T>
 *
 */
public class Server<T> {
	private final Logger LOG = Logger.getLogger(Server.class);

	final String CLEAN_SHUTDOWN_FILE = Constance.CLEAN_SHUTDOWN_FILE;

	private final CountDownLatch shutdownLatch = new CountDownLatch(1);

	final ServerConfig config;
	
	//线程管理类各种mq消费线程，监控线程，持久化线程等
	private ThreadManager<T> threadManager;
	
	private NettyServer nettyServer;

	private final File logDir;

	private int port = -1;
	final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

	public Server(ServerConfig config) {
		this.config = config;
		logDir = new File(config.getLogDir());
		if (!logDir.exists()) {
			LOG.warn("没有创建存放日志持久化的文件夹!!!系统正在创建");
			logDir.mkdirs();
		}
		port = config.getPort();
	}

	public void startup() {
		try {
			final long start = System.currentTimeMillis();
			LOG.info("启动日志服务器"+ port);
			// 1.本地是否存在.cleanshutdown文件，如果存在，表明上一次是正常关闭
			boolean needRecovery = true;
			File cleanShutDownFile = new File(new File(config.getLogDir()),
					CLEAN_SHUTDOWN_FILE);
			if (cleanShutDownFile.exists()) {
				needRecovery = false;
				cleanShutDownFile.delete();
			}
			//3.启动各种线程
			threadManager = new ThreadManager<T>(config, needRecovery);
			threadManager.start();
			
			// 2.启动Netty服务器
			nettyServer = new NettyServer(threadManager,config);
			nettyServer.start();
			
			// 4.启动服务端之后去zookeeper注册ServerSocket地址
			if(config.getEnableZookeeper()){
				ServiceProvider provider = new ServiceProvider();
				provider.publish("localhost", config.getPort());
			}
			
			
			final long cost = (System.currentTimeMillis() - start) / 1000;
			LOG.info("PHP日志服务器启动花费了:" + cost);

		} catch (Exception ex) {
			LOG.error("========================================");
			LOG.error("Fatal error during startup.", ex);
			LOG.error("========================================");
			close();
		}
	}

	public ThreadManager getThreadManager() {
		return threadManager;
	}
	public void awaitShutdown() throws InterruptedException {
		shutdownLatch.await();
	}
	
	public void close() {
		boolean canShutdown = isShuttingDown.compareAndSet(false, true);
		if (!canShutdown)
			return;// CLOSED

		LOG.info("关闭日志服务器!!!");
		try {
			if(nettyServer!=null){
				nettyServer.close();
			}
			if(threadManager!=null){
				threadManager.close();
			}
			
			//正常关闭，生成文件
			File cleanShutDownFile = new File(new File(config.getLogDir()),
					CLEAN_SHUTDOWN_FILE);
			cleanShutDownFile.createNewFile();
			
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
		}
		shutdownLatch.countDown();
		LOG.info("关闭日志系统完毕!!!");
	}
}
