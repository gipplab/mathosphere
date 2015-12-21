package com.formulasearchengine.mathosphere.mlp.text;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Moritz on 15.12.2015.
 */
public class MathConverterTest {

	@Test
	public void testGo() throws Exception {
		 String wikiText = IOUtils.toString(getClass().getResourceAsStream("legendre_wiki.txt"));
		final MathConverter mathConverter = new MathConverter(wikiText);
		final String real = mathConverter.getStrippedOutput();
		assertThat(real, containsString("Let FORMULA_"));
	}

	@Test
	public void testGo2() throws Exception {
		String wikiText = IOUtils.toString(getClass().getResourceAsStream("../hamiltonian_wiki.txt"));
		final MathConverter mathConverter = new MathConverter(wikiText);
		final String real = mathConverter.getOutput();
		assertThat(real, containsString("denoted by <math>H</math>, also \"Ȟ\" or <math>\\hat{H}</math>. "));
	}
}