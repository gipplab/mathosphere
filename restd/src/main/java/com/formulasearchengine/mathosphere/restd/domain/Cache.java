package com.formulasearchengine.mathosphere.restd.domain;

import com.formulasearchengine.mathosphere.basex.Client;
import com.formulasearchengine.mathosphere.basex.types.Results;
import restx.factory.AutoStartable;
import restx.factory.Component;

import java.util.*;

/**
 * This class caches results for pagination, and it also logs queries
 * Created by jjl4 on 6/23/15.
 */
@Component
public class Cache implements AutoStartable {
	//Least recently used cache of results that had limits/offsets
	private static LRUMap<String, Results> resultsCache;
	//Log of all queries
	private static List<MathRequest> queryLog;

	public void start() {
		resultsCache = new LRUMap<>( 30, 30 );
		queryLog = new ArrayList<>();
		System.out.println( "Cache started" );
	}

	public static boolean logQuery( MathRequest request ) {
		return queryLog.add( new MathRequest().setQuery( request.getQuery() ).setType( request.getType() ));
	}

	public static boolean cacheResults( MathRequest request ) {
		resultsCache.put( request.getQuery(), request.getResults() );
		return true;
	}

	public static Results getCachedResults( MathRequest request ) {
		return resultsCache.get( request.getQuery() );
	}

	public static boolean flushCachedResults() {
		resultsCache.clear();
		return true;
	}

	public static Map<String, Results> getAllCachedResults() {
		return Collections.unmodifiableMap( resultsCache );
	}

	public static Map<String, String> getAllCachedResultsAsStrings() {
		final Map<String, String> output = new HashMap<>();
		for ( final Map.Entry<String, Results> stringResultsEntry : resultsCache.entrySet() ) {
			output.put( stringResultsEntry.getKey(), Client.resultsToXML( stringResultsEntry.getValue() ));
		}
		return output;
	}

	public static boolean flushQueryLog() {
		queryLog.clear();
		return true;
	}

	public static ArrayList<MathRequest> getQueryLog() {
		return new ArrayList<>( queryLog );
	}

	public static class LRUMap<K,V> extends LinkedHashMap<K,V> {
		private static final long serialVersionUID = 2379739019453611804L;
		private final int maxCap;

		public LRUMap( int initialCap, int maxCap ) {
			super (initialCap, 0.75f, true);
			this.maxCap = maxCap;
		}

		@Override
		protected boolean removeEldestEntry( Map.Entry<K,V> eldest ) {
			//Make this map remove entries when it exceeds max capacity
			//Ignore super method
			return size() > this.maxCap;
		}

	}
}
