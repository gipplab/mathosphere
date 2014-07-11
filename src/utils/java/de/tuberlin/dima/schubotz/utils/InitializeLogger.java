package de.tuberlin.dima.schubotz.utils;

import java.io.File;

import org.apache.log4j.Level;

public class InitializeLogger {
	public static void init(String name, Level level) {
		System.setProperty("log4j.configuration", new File(".", "resources"+File.separatorChar+"log4j.properties").toURI().toString());
	}
}
