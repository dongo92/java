package com.lgcns.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

public class MessageService {
	Map<String, LimitedQueue> queue;
	
	public MessageService() {
		this.queue = new HashMap<String, LimitedQueue>();
	}

	public boolean create(String name, int limit, int processTimeout, int maxFailCount, int waitTime) {
		if ( queue.containsKey(name) ) {
			return false;
		} else {
			LimitedQueue q = new LimitedQueue(limit, processTimeout, maxFailCount, waitTime);
			queue.put(name, q);
			return true;
		}
	}
	
	public boolean enque(String name, String s) {
		LimitedQueue q = queue.get(name);
		boolean result = q.add(s);
		if ( result == true ) q.notifyQueue();
		return result;
	}
	
	public Message deque(String name) {
		LimitedQueue q = queue.get(name);
		
		if ( q.isEmpty() == true ) {
			System.out.println(name + " : Start Wait: " + LocalTime.now());
			q.waitQueue();
			System.out.println(name + " : End Wait: " + LocalTime.now());
		}
		
		Message m = q.poll();
		
		if ( m != null ) q.processTimeout(m);
		return m;
	
	}
	
	public void shutdown() {
		
		synchronized (this) {
			notifyAll();
		}
		
		for( LimitedQueue q : queue.values() ) {
			q.fihishReceived();
		}

		Gson gson = new GsonBuilder().create();

		FileWriter f = null;
		
		try {
			f = new FileWriter("MessageService.txt");
			gson.toJson(this, f);
			f.close();
		} catch (JsonIOException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
//		String str = gson.toJson(this);
//		System.out.println(str);
		
		
		
	}
	
	public void ack(String name, String msgId) {
		LimitedQueue q = queue.get(name);
		q.ack(msgId);
	}
	
	public void fail(String name, String msgId) {
		LimitedQueue q = queue.get(name);
		q.fail(msgId);
		q.notifyQueue();
	}
	
	public Message dlq(String name) {
		LimitedQueue q = queue.get(name);
		return q.dlq();
	}
	
	public boolean isEmpty(String name) {
		LimitedQueue q = queue.get(name);
		return q.isEmpty();
	}
	
	public LimitedQueue getQueue(String name) {
		return queue.get(name);
	}
	
	public int getWaitTime(String name) {
		LimitedQueue q = queue.get(name);
		return q.waitTime;
	}
	
	public int getProcessTimeout(String name) {
		LimitedQueue q = queue.get(name);
		return q.processTimeout;
	}

//	public void processTimeout(String name, String msgId) {
//		LimitedQueue q = queue.get(name);
//		
//		class ProcessTimeout implements Runnable {
//			
//			public void run() {
//				try {
//					Thread.sleep(q.processTimeout*1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				q.fail(msgId);
//			
//			}
//		}
//		
//		if ( q.processTimeout > 0 ) {
//			Thread t = new Thread(new ProcessTimeout());
//			t.start();		
//		}
//	}
	
}
