package com.formulasearchengine.mathosphere.mlp.cli;

import com.beust.jcommander.Parameters;

import java.io.Serializable;

@Parameters(commandDescription = "Extracts tags from wikidump")
public class TagsCommandConfig extends FlinkMlpCommandConfig implements Serializable {

}
