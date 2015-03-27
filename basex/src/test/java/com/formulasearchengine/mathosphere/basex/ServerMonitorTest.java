package com.formulasearchengine.mathosphere.basex;

import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.Thread.State;

import static org.junit.Assert.*;

public class ServerMonitorTest {
	@BeforeClass
	public static void setup() throws Exception {
		if ( !Server.isEmpty() ){
			Server.getInstance().shutdown();
		}
	}

	@Test(timeout = 10)
	public void testRun() throws Exception {
		ServerMonitor m = new ServerMonitor();
		m.start();
		assertTrue( "Thread was not started", m.isAlive() );
		m.shutdown();
		assertEquals( "Thread was not stopped", State.TERMINATED, m.getState() );
	}

	@Test
	public void testIsGood() throws Exception {
		ServerMonitor m = new ServerMonitor();
		assertFalse( m.isGood() );
		(new ServerTest()).testImportData();
		assertTrue( m.isGood() );
	}
}