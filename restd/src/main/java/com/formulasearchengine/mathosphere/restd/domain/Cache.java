package com.formulasearchengine.mathosphere.restd.domain;

import restx.factory.AutoStartable;
import restx.factory.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This class caches results for pagination, and it also logs queries
 * Created by jjl4 on 6/23/15.
 */
@Component
public class Cache implements AutoStartable {
	//Log of all queries
	private static List<MathRequest> queryLog;

	public void start() {
		queryLog = new ArrayList<>();
		System.out.println( "Cache started" );
	}

	public static boolean addQuery( MathRequest request ) {
		return queryLog.add( new MathRequest().setQuery( request.getQuery() ).setType( request.getType() ));
	}

	public static boolean flushQueryLog() {
		queryLog.clear();
		return true;
	}

	public static ArrayList<MathRequest> getQueryLog() {
		return new ArrayList<>( queryLog );
	}
}
