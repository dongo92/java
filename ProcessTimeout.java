package com.lgcns.test;

public class ProcessTimeout implements Runnable {
	String id;
	LimitedQueue q;
	
	public ProcessTimeout(String id, LimitedQueue q) {
		this.id = id;
		this.q = q;
	}
	public void run() {
//		System.out.println("##### " + id + " Start of ProcessTimeout #####");
//		try {
//			Thread.sleep(q.processTimeout*1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		try {
			synchronized (q) {
				q.wait(q.processTimeout*1000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		q.fail(id);

		
//		System.out.println("##### " + id + " End of ProcessTimeout #####");
	}
}
