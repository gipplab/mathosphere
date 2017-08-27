package com.formulasearchengine.mathosphere.mlp.pojos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

import static com.formulasearchengine.mathosphere.mlp.text.WikiTextUtilsTest.getTestResource;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * Created by Moritz on 30.09.2015.
 */
public class MathTagTest {

    @Test
    public void testGetContentHash() throws Exception {
        MathTag t = new MathTag(1, "<math><mrow><mo>x</mo><mrow></math>", WikiTextUtils.MathMarkUpType.MATHML);
        assertEquals("2cf05995ef521b456aa419201d160406", t.getContentHash());
    }

    @Test
    public void testGetIdentifier() throws Exception {
        MathTag tagX = new MathTag(1, "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mrow><mi>x</mi></mrow></math>", WikiTextUtils.MathMarkUpType.MATHML);
        assertEquals(ImmutableSet.of("x"), tagX.getIdentifier(true, true).elementSet());
        assertEquals(ImmutableSet.of("x"), tagX.getIdentifier(false, true).elementSet());
        MathTag schrödinger = new MathTag(1, getTestResource("com/formulasearchengine/mathosphere/mlp/schrödinger_eq.xml"), WikiTextUtils.MathMarkUpType.MATHML);
        //MathTag schrödingerTex = new MathTag(1,"i\\hbar\\frac{\\partial}{\\partial t}\\Psi(\\mathbb{r},\\,t)=-\\frac{\\hbar^{2}}{2m}" +
        //  "\\nabla^{2}\\Psi(\\mathbb{r},\\,t)+V(\\mathbb{r})\\Psi(\\mathbb{r},\\,t).", WikiTextUtils.MathMarkUpType.LATEX);
        ImmutableMultiset<String> lIds = ImmutableMultiset.of("i",
                "\\hbar", "\\hbar",
                "t", "t", "t", "t",
                "\\Psi", "\\Psi", "\\Psi",
                "\\mathbb{r}", "\\mathbb{r}", "\\mathbb{r}", "\\mathbb{r}",
                "V",
                "m");
        assertEquals(lIds, schrödinger.getIdentifier(true, false));
    }

    @Test
    public void testGetJson() throws JsonProcessingException {
        MathTag tag = new MathTag(1, "a+b", WikiTextUtils.MathMarkUpType.LATEX);
        Assert.assertThat(tag.toJson(), containsString("\"inputhash\""));
        Assert.assertThat(tag.toJson(), containsString("\"input\""));
        Assert.assertThat(tag.toJson(), containsString("\"type\""));
    }

    @Test
    public void testGetJsonValidation() throws Exception {
        MathTag tag = new MathTag(1, "a+b", WikiTextUtils.MathMarkUpType.LATEX);
        final String real = "[" + tag.toJson() + "]";
        final JsonNode jsonSchema = JsonLoader.fromResource("/formula_list_schema.json");
        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        final JsonSchema schema = factory.getJsonSchema(jsonSchema);
        ProcessingReport report = schema.validate(JsonLoader.fromString(real));
        assertTrue(report.isSuccess());
    }

    @Test
    public void testGetMarkUpType() throws Exception {

    }

    @Test
    public void testPlaceholder() throws Exception {

    }
}
