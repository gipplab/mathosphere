package de.tuberlin.dima.schubotz.wiki;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class AllWikiTestRunnerIT {

	@Test
    @Ignore
	public void test() {
		//TODO find a way to fix the dependency on file output between ProcessIT and MainIT, also remove hardcoded paths
		Result result = JUnitCore.runClasses(AllWikiTestSuite.class);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
		System.out.println(result.wasSuccessful());
	}

}
