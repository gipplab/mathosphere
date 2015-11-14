package com.formulasearchengine.mathosphere.restd.rest;


import com.formulasearchengine.mathosphere.mlp.FlinkMlpRelationFinder;
import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mathosphere.restd.domain.WikiTextRequest;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

@Component
@RestxResource
public class MlpResource {


	private final FlinkMlpCommandConfig config = FlinkMlpCommandConfig.test();
	private final FlinkMlpRelationFinder finder = new FlinkMlpRelationFinder();

	@POST("/AnalyzeWikiText")
	@PermitAll
	public WikiDocumentOutput AnalyeWikiText(WikiTextRequest input) {
		try {
			return finder.outDocFromText(config, input.wikitext);
		} catch (Exception e) {
			e.printStackTrace();
			return new WikiDocumentOutput(false);
		}
	}
}
