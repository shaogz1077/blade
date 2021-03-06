package com.blade.embedd;

import static com.blade.Blade.$;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.Const;
import com.blade.kit.Environment;
import com.blade.web.DispatcherServlet;

public class EmbedJettyServer implements EmbedServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmbedJettyServer.class);
	
    private int port = Const.DEFAULT_PORT;
	
	private org.eclipse.jetty.server.Server server;
	
	private WebAppContext webAppContext;
	
	private Environment environment = null;
    
	public EmbedJettyServer() {
		System.setProperty("org.apache.jasper.compiler.disablejsr199", "true");
		$().loadAppConf("jetty.properties");
		environment = $().environment();
		$().enableServer(true);
	}
	
	@Override
	public void startup(int port) throws Exception {
		this.startup(port, Const.DEFAULT_CONTEXTPATH, null);
	}

	@Override
	public void startup(int port, String contextPath) throws Exception {
		this.startup(port, contextPath, null);
	}
	
	@Override
	public void setWebRoot(String webRoot) {
		webAppContext.setResourceBase(webRoot);
	}
	
	@Override
	public void startup(int port, String contextPath, String webRoot) throws Exception {
		this.port = port;
		
		// Setup Threadpool
        QueuedThreadPool threadPool = new QueuedThreadPool();
        
        int minThreads = environment.getInt("server.jetty.min-threads", 100);
        int maxThreads = environment.getInt("server.jetty.max-threads", 500);
        
        threadPool.setMinThreads(minThreads);
        threadPool.setMaxThreads(maxThreads);
        
		server = new org.eclipse.jetty.server.Server(threadPool);
		
		// 设置在JVM退出时关闭Jetty的钩子。
        server.setStopAtShutdown(true);
        
        webAppContext = new WebAppContext();
        webAppContext.setContextPath(contextPath);
        webAppContext.setResourceBase("");
        
	    int securePort = environment.getInt("server.jetty.http.secure-port", 8443);
	    int outputBufferSize = environment.getInt("server.jetty.http.output-buffersize", 32768);
	    int requestHeaderSize = environment.getInt("server.jetty.http.request-headersize", 8192);
	    int responseHeaderSize = environment.getInt("server.jetty.http.response-headersize", 8192);
	    
	    // HTTP Configuration
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecurePort(securePort);
        http_config.setOutputBufferSize(outputBufferSize);
        http_config.setRequestHeaderSize(requestHeaderSize);
        http_config.setResponseHeaderSize(responseHeaderSize);
        http_config.setSendServerVersion(true);
        http_config.setSendDateHeader(false);
        
        long idleTimeout = environment.getLong("server.jetty.http.idle-timeout", 30000L);
        
        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
        http.setPort(this.port);
        http.setIdleTimeout(idleTimeout);
        server.addConnector(http);
	    
	    ServletHolder servletHolder = new ServletHolder(DispatcherServlet.class);
	    servletHolder.setAsyncSupported(false);
	    servletHolder.setInitOrder(1);
	    
	    webAppContext.addServlet(servletHolder, "/");
	    
	    HandlerList handlers = new HandlerList();
	    handlers.setHandlers(new Handler[] { webAppContext, new DefaultHandler() });
	    server.setHandler(handlers);
        
	    server.start();
	    LOGGER.info("Blade Server Listen on 0.0.0.0:{}", this.port);
	    server.join();
	}
	
    public void stop() throws Exception {
        server.stop();
    }
    
}