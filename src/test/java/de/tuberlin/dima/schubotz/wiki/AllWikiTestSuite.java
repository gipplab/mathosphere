package de.tuberlin.dima.schubotz.wiki;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ProcessWikiIT.class, WikiQueryMapIT.class, WikiMainIT.class})
public class AllWikiTestSuite {
}
