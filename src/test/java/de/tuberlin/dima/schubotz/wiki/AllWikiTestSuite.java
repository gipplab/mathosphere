package de.tuberlin.dima.schubotz.wiki;

import de.tuberlin.dima.schubotz.fse.mappers.WikiQueryMapTest;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@Ignore
@SuiteClasses({ProcessWikiIT.class, WikiQueryMapTest.class, WikiMainIT.class})
public class AllWikiTestSuite {
}
