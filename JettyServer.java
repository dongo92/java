
package com.lgcns.test;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class JettyServer {
	Server server;
	
	
	public void start() throws Exception {
		
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMaxThreads(5000);
		
		//server = new Server(threadPool);
		server = new Server();
		
		// connector
		ServerConnector connector = new ServerConnector(server);
		connector.setHost("127.0.0.1");
		connector.setPort(8080);

	//	System.out.println(connector.getAcceptors());
		
		server.addConnector(connector);
		
//		server.setConnectors(new Connector[] { connector } );
		
		// servlet context
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");	
		context.addServlet(new ServletHolder(new CommandServlet(server)), "/");
		
		server.setHandler(context);
		
		// run
		server.start();
		//server.join();
		
	}

}
