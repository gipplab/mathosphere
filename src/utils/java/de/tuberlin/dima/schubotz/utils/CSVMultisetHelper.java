package de.tuberlin.dima.schubotz.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.google.common.collect.HashMultiset;

public class CSVMultisetHelper {
	public static HashMultiset<String> csvToMultiset(String in) throws FileNotFoundException, IOException {
		HashMultiset<String> out = HashMultiset.create();
		BufferedReader br = new BufferedReader(new FileReader(in));
        String line = "";
        while ((line = br.readLine()) != null) {
        	String parts[] = line.split(" ");
        	out.add(parts[0], Integer.valueOf(parts[1]));
        }
        br.close();
        return out;
	}
}
