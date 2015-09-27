package mlp;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by Moritz on 27.09.2015.
 */
public class MainTest extends TestCase {

	@Test
	public void testMain() throws Exception {
		String[] args = new String[ 2 ];
		final ClassLoader classLoader = getClass().getClassLoader();
		args[ 0 ] = "-d";
		args[ 1 ] = classLoader.getResource( "wikienmathsample.xml" ).getFile();
		Main.main( args );
	}

}