package com.lgcns.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Command {
	
	public void run(MessageService msgService){
		
		BufferedReader reader = null;
		
		try { 
			reader = new BufferedReader(new InputStreamReader(System.in));
			while ( true ) {
				String buffer = reader.readLine();
				String [] elem = buffer.split(" ");
				
				if ( elem[0].equals("CREATE") ) {
//					if ( msgService.create(elem[1], Integer.parseInt(elem[2])) == false )
						System.out.println("Queue Exist");
					
				} else if ( elem[0].equals("SEND") ) {
					if ( msgService.enque(elem[1], elem[2]) == false )
						System.out.println("Queue Full");
					
				} else if ( elem[0].equals("RECEIVE") ) {
//					String s = msgService.deque(elem[1]);
//					if ( s != null ) System.out.println(s);
				} else {
					System.out.println("Unknown Command");
				}
				
			}			
		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
