package com.formulasearchengine.mathosphere.mlp.text;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;

/**
 * Created by Moritz on 15.12.2015.
 */
public class MathConverterTest {

	@Test
	public void testGo() throws Exception {
		 String wikiText = IOUtils.toString(getClass().getResourceAsStream("legendre_wiki.txt"));
		WikiConfig config = DefaultConfigEnWp.generate();

		// Instantiate a compiler for wiki pages
		WtEngineImpl engine = new WtEngineImpl(config);
		// Retrieve a page

		PageTitle pageTitle = PageTitle.make(config, "Ham");
		PageId pageId = new PageId(pageTitle, -1);
		// Compile the retrieved page
		//StringWriter sw = new StringWriter();
		//WtAstPrinter myPrinter = new WtAstPrinter(sw);
		final long t0 = System.nanoTime();
		EngProcessedPage cp = engine.postprocess(pageId, wikiText, null);
		System.out.println((System.nanoTime()-t0)/1000000 + "ms parsing");
		MathConverter p = new MathConverter(config, 150);
		//myPrinter.go(cp.getPage());

		String s = (String) p.go(cp.getPage());
		// (String) myPrinter.go(cp.getPage());
		System.out.println((System.nanoTime()-t0)/1000000);
		//String s = sw.toString();
		//System.out.println(s);
	}
}