package com.formulasearchengine.mathosphere.basex;

import org.basex.query.QueryException;

import javax.xml.xquery.XQException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerMonitor extends Thread {
	private boolean run = true;

	@SuppressWarnings("RefusedBequest")
	@Override
	public final void run(){
		while( run ) {
			isGood();
			try {
				Thread.sleep(  30000 );
			} catch ( InterruptedException e ) {
				if (run ) { // abnormal termination
					e.printStackTrace();
					System.out.println( "Server health check crashed." );
				}
				return;
			}
		}
	}

	boolean isGood() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println( "Check health at " + dateFormat.format(date) );
		Client c = new Client( );
		try {
			c.directXQuery( "count(./*/*)" );
		} catch ( IOException | QueryException | XQException e ) {
			e.printStackTrace();
			System.out.println( "Server crashed!" );
			return false;
		}
		return true;

	}


	public void shutdown() {
		run = false;
		currentThread().interrupt();
	}
}
