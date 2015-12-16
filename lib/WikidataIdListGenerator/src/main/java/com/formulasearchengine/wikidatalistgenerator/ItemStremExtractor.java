package com.formulasearchengine.wikidatalistgenerator;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moritz on 16.12.2015.
 */
public class ItemStremExtractor {
	final JsonFactory factory = new JsonFactory();
	final String language;
	final boolean useAlias;
	String ItemId;


	public ItemStremExtractor(String language, boolean useAlias) {
		this.language = language;
		this.useAlias = useAlias;
	}

	public void extract(InputStream in, OutputStream out ) throws IOException {
		JsonParser jParser = factory.createParser( in );
		if (jParser.nextToken() != JsonToken.START_ARRAY) {
			emitError( "Expected a JSON array. Unexpected token " + jParser.getText() );
		}
		while (jParser.nextToken() != JsonToken.END_ARRAY) {
			if (jParser.getCurrentToken() == JsonToken.START_OBJECT) {
				processItem( jParser, out );
			} else {
				emitError( "Unexpected token " + jParser.getText() );
			}
		}
	}

	private void processItem(JsonParser jParser, OutputStream out) throws IOException {
		String id = null;
		// We can not expect that the id attribute comes first, so we have to store all titles
		// until the whole entry is read
		List<String> titles = new ArrayList<>(  );
		while ( jParser.nextToken() != JsonToken.END_OBJECT ) {
			if ( jParser.getCurrentToken() != JsonToken.FIELD_NAME ){
				emitError( "expects field name on top level object" );
			}
			switch ( jParser.getCurrentName() ){
				case "id":
					jParser.nextToken();
					id = jParser.getValueAsString();
					break;
				case "labels":
					jParser.nextToken();
					if (jParser.getCurrentToken() == JsonToken.START_OBJECT ){
						jParser.nextToken();
						extractLabel( jParser, titles );
					} else {
						emitError( "expects object start after labels" );
					}
					break;
				case "aliases":
					if ( useAlias ){
						break;
					}
				default:
					jParser.nextToken();
					if ( jParser.getCurrentToken().isStructStart() ){
						skipBlock( jParser );
					}
					// Skip value
			}
		}
		if ( id != null ){
			out.write( id.getBytes() );
		}
	}

	private void extractLabel(JsonParser jParser, List<String> titles) throws IOException {
		if (jParser.getCurrentToken() == JsonToken.END_OBJECT) {
			return;
		}
		if ( jParser.getCurrentToken() != JsonToken.FIELD_NAME ){
			emitError( "expects field name on top level object" );
		}
		if (language.equals( jParser.getCurrentName() )) {
			jParser.nextToken();
			if (jParser.getCurrentToken() == JsonToken.START_OBJECT) {
				while (jParser.nextToken() != JsonToken.END_OBJECT) {
					if (jParser.getCurrentName().equals( "value" )) {
						jParser.nextToken();
						titles.add( jParser.getValueAsString() );
					}
				}
			} else {
				emitError( "language is expected to be an array" );
			}
		} else {
			jParser.nextToken();
			skipStruct( jParser, JsonToken.START_OBJECT );
		}
		jParser.nextToken();
		extractLabel( jParser, titles );
	}

	private void skipBlock(JsonParser jParser) throws IOException {
		if ( jParser.getCurrentToken() == JsonToken.START_OBJECT ) {
			skipStruct( jParser, JsonToken.START_OBJECT );
		} else if (jParser.getCurrentToken() == JsonToken.START_ARRAY) {
			skipStruct( jParser, JsonToken.START_ARRAY );
		} else {
			emitError( "Unexpected token " + jParser.getText() );
		}
	}

	private void skipStruct(JsonParser jParser, JsonToken type) throws IOException {
		JsonToken endToken;
		if (  jParser.getCurrentToken() != type ){
			emitError( "skip struct does not match current struct" );
		}
		if ( type == JsonToken.START_ARRAY ){
			endToken = JsonToken.END_ARRAY;
		} else {
			endToken = JsonToken.END_OBJECT;
		}
		while ( jParser.nextToken() != endToken ) {
			if ( jParser.getCurrentToken() == JsonToken.START_OBJECT ){
				skipStruct( jParser, JsonToken.START_OBJECT );
			}
			if ( jParser.getCurrentToken() == JsonToken.START_ARRAY ){
				skipStruct( jParser, JsonToken.START_ARRAY );
			}
		}
	}

	private void emitError(String message) throws IOException {
		throw new IOException(message);
	}
}
