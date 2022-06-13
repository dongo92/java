package com.lgcns.test;


import java.time.LocalTime;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LimitedQueue {
	LinkedList<Message> queue;
	// List<Message> queue;
	int limit;
	int msgCount;
	
	Queue<Message> dlq;
	
	int processTimeout;
	int maxFailCount;
	int waitTime;
	
//	ReentrantLock lock;
//	Queue<Condition> conds;


	private void init(int limit, int processTimeout, int maxFailCount, int waitTime) {
		this.limit = limit;
		this.msgCount = 0;
		
		this.processTimeout = processTimeout;
		this.maxFailCount = maxFailCount;
		this.waitTime = waitTime;
		
//		lock = new ReentrantLock();
//		conds = new LinkedList<Condition>();

	}
	
	public LimitedQueue(int limit) {
		this.queue = new LinkedList<Message>();
		// this.queue = Collections.synchronizedList(new LinkedList<Message>());
		init(limit, 0, 0, 0);
		this.dlq = new LinkedList<Message>();
		
	}
	
	public LimitedQueue(int limit, int processTimeout, int maxFailCount, int waitTime) {
		this.queue = new LinkedList<Message>();
		init(limit, processTimeout, maxFailCount, waitTime);
		this.dlq = new LinkedList<Message>();
	}
	
	public  boolean add(String s) {

		if (queue.size() < limit ) {
			Message m = new Message("M"+msgCount, s);
			queue.add(m);
			msgCount++;
			
			return true;
		} else
			return false;
	}
	
	public  Message poll() {
		Message m = null;

		Iterator<Message> itr = queue.iterator();
		while ( itr.hasNext() ) {
			m = itr.next();
			if ( m.isReceived == false ) {
				m.isReceived = true;
				break; 
			}
			m = null;
		}
		
		return m;
	}
	
	
	
	public  void ack(String id) {
		Iterator<Message> itr = queue.iterator();
		while ( itr.hasNext() ) {
			Message m = itr.next();
			if ( m.id.equals(id) == true ) {

				synchronized (m) {
					m.notify();
				}
				
				itr.remove();
				break; 
			}
		}
	}

	public  void fail(String id) {
		Iterator<Message> itr = queue.iterator();
		while ( itr.hasNext() ) {
			Message m = itr.next();
			if ( m.id.equals(id) == true && m.isReceived == true ) {
				m.isReceived = false;
				m.failCount++;
				
				if (m.failCount > maxFailCount ) {
					itr.remove();
					dlq.add(m);
				} else {

					synchronized (m) {
						m.notify();
					}
					
				}				
				
				break;
			}
		}
	}
	
	
	public  Message dlq() {
		return dlq.poll();
	}
	

	public void fihishReceived() {
		Iterator<Message> itr = queue.iterator();
		while ( itr.hasNext() ) {
			Message m = itr.next();
			if ( m.isReceived == true ) {
				itr.remove();
			}
		}
	}
	
	public  boolean isEmpty() {
		boolean isEmpty = true;
		Message m = null;
		Iterator<Message> itr = queue.iterator();
		while ( itr.hasNext() ) {
			m = itr.next();
			if ( m.isReceived == false ) {
				isEmpty = false;
				break; 
			}
		}
		return isEmpty;
	}
	
	
	public void processTimeout(Message m) {
		class InnerProcessTimeout implements Runnable {
			Message m;
			
			public InnerProcessTimeout (Message m) {
				this.m = m;
			}
			public void run() {
				System.out.println("Start Process Time Out: " + m.s + " " +LocalTime.now());
				try {
					synchronized (m) {
						m.wait(processTimeout*1000);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("End Process Time Out :"+m.s + " " + LocalTime.now());
				fail(m.id);
			}
		}
		
		if ( processTimeout > 0  ) {
			Thread t = new Thread(new InnerProcessTimeout(m));
			t.start();	
		}

	}

	public synchronized void waitQueue() {
		if ( waitTime > 0 ) {
//			System.out.println(Thread.currentThread().getName()+" Wait "+System.currentTimeMillis());
			try {
				wait(waitTime*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void notifyQueue() {
		notifyAll();
	}


	
//	public void awaitQueue() {
//		if ( waitTime > 0 ) {
//			
//			System.out.println(Thread.currentThread().getName()+" Wait "+System.currentTimeMillis());
//			
//			lock.lock();
//			try {
//				Condition cond = lock.newCondition();
//				cond.await(waitTime, TimeUnit.SECONDS);
//				conds.add(cond);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			lock.unlock();
//		}
//	}
//	
//	public void signalQueue() {
//		lock.lock();
//		Condition cond = conds.poll();
//		if ( cond != null ) cond.signal();
//		lock.unlock();
//	}
	


}
