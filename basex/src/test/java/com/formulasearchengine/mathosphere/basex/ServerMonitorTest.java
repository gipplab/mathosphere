package com.formulasearchengine.mathosphere.basex;

import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.Thread.State;

import static org.junit.Assert.*;

public class ServerMonitorTest {
	@BeforeClass
	public static void setup() throws Exception {
		BaseXTestSuite.setup();
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
		assertTrue( m.isGood() );
		try {
			Server.getInstance().shutdown();
			assertFalse( m.isGood() );
		}catch ( Exception e ){
			e.printStackTrace();
			System.out.println("Warning can not test server shutdown. Server is being used" );
		}
	}
}