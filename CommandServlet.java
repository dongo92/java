package com.lgcns.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CommandServlet extends HttpServlet {

	Server server;
	MessageService msgService;
	static boolean isShutdown;
	
	public CommandServlet(Server server) {
		this.server = server;
		msgService = null;
		isShutdown = false;
		try {
			Gson gson = new GsonBuilder().create();
			FileReader f = new FileReader("MessageService.txt");
			msgService = gson.fromJson(f, MessageService.class);

			
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			
		};
		if ( msgService == null ) 
			msgService = new MessageService();		
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String buffer = request.getServletPath();
		if ( buffer.equals("/favicon.ico")) return;	// for browser
		
		String elem [] = buffer.split("/");
		
		 
		if ( elem.length < 3 ) {
			System.out.println("################# Unexpected GET #################");
			return;

			// ######### RECEIVE #######
		} else if ( elem.length == 3 && elem[1].equals("RECEIVE")  ) {
			
			System.out.println("RECEIVE");
			
			String name = elem[2];
			Message m = msgService.deque(name);

			JsonObject obj = new JsonObject();
			if ( isShutdown == true ) {
				obj.addProperty("Result", "Service Unavailable");
			} else {
				if ( m == null ) {
					obj.addProperty("Result", "No Message");	
				} else {
					obj.addProperty("Result", "Ok");
					obj.addProperty("MessageID", m.id);
					obj.addProperty("Message", m.s);
				}
			}
			response.setStatus(200);
			response.getWriter().println(obj);
					
		// ######### DLQ #######	
		} else if ( elem.length == 3 && elem[1].equals("DLQ") ) {
//			System.out.println("DLQ");
			
			String name = elem[2];
			Message m = msgService.dlq(name);

			JsonObject obj = new JsonObject();
			if ( m == null ) {
				obj.addProperty("Result", "No Message");	
			} else {
				obj.addProperty("Result", "Ok");
				obj.addProperty("MessageID", m.id);
				obj.addProperty("Message", m.s);
			}
			response.setStatus(200);
			response.getWriter().println(obj);

		} 
		
	}
	

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String buffer = request.getServletPath();
		if ( buffer.equals("/favicon.ico")) return;
		String elem [] = buffer.split("/");
		
		if ( elem.length < 2 ) {
			System.out.println("################# Unexpected POST #################");
			return;
			
		// ######### CREATE #######
		} else if ( elem.length == 3 && elem[1].equals("CREATE")  ) {
//			System.out.println("CREATE");
			
			String name = elem[2];
			
			JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();
			int limit = body.get("QueueSize").getAsInt();
			int processTimeout = body.get("ProcessTimeout").getAsInt();
			int maxFailCount = body.get("MaxFailCount").getAsInt();
			int waitTime = body.get("WaitTime").getAsInt();

			boolean flag = msgService.create(name, limit, processTimeout, maxFailCount, waitTime);

			JsonObject obj = new JsonObject();
			if ( flag == false ) {
				obj.addProperty("Result", "Queue Exist");	
			} else {
				obj.addProperty("Result", "Ok");	
			}
			response.setStatus(200);
			response.getWriter().println(obj);
			
		// ######### SEND #######	
		} else if ( elem.length == 3 && elem[1].equals("SEND") ) {
			
			String name = elem[2];
			
			JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();
			String msg = body.get("Message").getAsString();

			System.out.println("SEND "+msg);
			
			
			boolean flag = msgService.enque(name, msg);

			JsonObject obj = new JsonObject();
			if ( flag == false )
				obj.addProperty("Result", "Queue Full");
			else {
				obj.addProperty("Result", "Ok");
			}
			response.setStatus(200);
			response.getWriter().println(obj);
		
		// ######### ACK #######	
		} else if ( elem.length == 4 && elem[1].equals("ACK") ) {


			String name = elem[2];
			String msgId = elem[3];
			
			System.out.println("ACK :"+msgId);

			
			msgService.ack(name, msgId);

			JsonObject obj = new JsonObject();
			obj.addProperty("Result", "Ok");
			response.setStatus(200);
			response.getWriter().println(obj);
		
		// ######### FAIL #######
		} else if ( elem.length == 4 &&  elem[1].equals("FAIL") ) {
			
			String name = elem[2];
			String msgId = elem[3];
			
			System.out.println("FAIL: "+msgId);
			
			msgService.fail(name, msgId);

			JsonObject obj = new JsonObject();
			obj.addProperty("Result", "Ok");
			response.setStatus(200);
			response.getWriter().println(obj);
		} else if ( elem.length == 2 && elem[1].equals("SHUTDOWN") ) {
//			System.out.println("SHUTDOWN");
			
			msgService.shutdown();

			JsonObject obj = new JsonObject();
			obj.addProperty("Result", "Ok");
			response.setStatus(200);
			response.getWriter().println(obj);
			
			class Shutdown implements Runnable {
				public void run() {
					try {
						server.stop();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.exit(0);;
				}
			}
			Thread t = new Thread(new Shutdown());
			t.start();
			

		}
	}
	



}
