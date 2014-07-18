package de.tuberlin.dima.schubotz.wiki;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ProcessWikiIT.class, WikiQueryIT.class, WikiMainIT.class})
public class AllWikiTestSuite {
}
