package de.tuberlin.dima.schubotz.fse;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class AllMainTestRunnerIT {

	//@org.junit.Ignore("Ignored")@Test
	public void test() {
		//TODO find a way to fix the dependency on file output between ProcessIT and MainIT, also remove hardcoded paths
		Result result = JUnitCore.runClasses(AllMainTestSuite.class);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
		System.out.println(result.wasSuccessful());
	}

}
