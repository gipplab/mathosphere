package com.formulasearchengine.mathosphere.basex;

import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * Translates hits into different NTCIR result formats including XML and CSV.
 *
 * @author Tobias Uhlich
 * @author Thanh Phuong Luu
 */
public class Results {

	private final LinkedList<Run> runs = new LinkedList<Run>();
	private boolean showTime = true;

	public void addRun( String runtag, Long ms, String type ) {
		runs.add( new Run( runtag, ms, type ) );
	}

	public void addRun( Run run ) {
		runs.add( run );
	}

	public void clear() {
		runs.clear();
	}

	public String toXML() {
		final StringBuilder runsXMLBuilder = new StringBuilder();
		for (final Run run : runs ) {
			runsXMLBuilder.append(run.toXML());
		}

		return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
			+ "<results xmlns=\"http://ntcir-math.nii.ac.jp/\">\n"
			+ runsXMLBuilder.toString() + "</results>\n";

	}

	public String toCSV() {
		final StringBuilder runsCSVBuilder = new StringBuilder().append("queryId,formulaId\n");
		for (final Run run : runs ) {
			runsCSVBuilder.append(run.toCSV());
		}

		return runsCSVBuilder.toString();

	}

	public void setShowTime (boolean showTime) {
		this.showTime = showTime;
	}

	public class Run {

		private final String runtag;
		private Long ms;
		private final String type;

		private final LinkedList<Result> results = new LinkedList<Result>();

		public Run( String runtag, Long ms, String type ) {
			this.runtag = runtag;
			this.ms = ms;
			this.type = type;
		}

		public Run( String runtag, String type ) {
			this.runtag = runtag;
			this.type = type;
		}

		public void setTime( Long ms ) {
			this.ms = ms;
		}

		public void addResult( String num, Long ms ) {
			results.add(new Result(num, ms));
		}

		public void addResult( Result result ) {
			results.add( result );
		}

		public String toXML () {
			final StringBuilder resultXMLBuilder = new StringBuilder();
			for ( final Result result : results ) {
				resultXMLBuilder.append(result.toXML());
			}

			final StringBuilder s = new StringBuilder().append( "  <run runtag=\"" ).append( runtag );
			if ( showTime ) {
				s.append( "\" runtime=\"" ).append( ms );
			}
			s.append( "\" runtype=\"" ).append( type ).append( "\">\n" ).
				append(resultXMLBuilder.toString()).append("  </run>\n");
			return s.toString();
		}

		public String toCSV() {
			final StringBuilder csvBuilder = new StringBuilder();
			for (final Result result : results ) {
				csvBuilder.append(result.toCSV());
			}
			return csvBuilder.toString();
		}
		

		public class Result {
			private final Pattern NTCIR_MATH_PATTERN = Pattern.compile("NTCIR11-Math-", Pattern.LITERAL);
			private Long ms;
			private final String num;

			private final LinkedList<Hit> hits = new LinkedList<Hit>();

			public Result( String num, Long ms ) {
				this.ms = ms;
				this.num = num;
			}

			public Result( String num ) {
				this.num = num;
			}

			public int size() {
				return hits.size();
			}

			public Long getTime() {
				return ms;
			}

			public void setTime( Long ms ) {
				this.ms = ms;
			}

			public void addHit( Hit hit ) {
				hits.add( hit );
			}

			public String toXML() {
				final StringBuilder hitXMLBuilder = new StringBuilder();
				for (final Hit hit : hits ) {
					hitXMLBuilder.append(hit.toXML());
				}

				final StringBuilder s = new StringBuilder().append( "    <result for=\"NTCIR11-Math-" ).append( num );
				if ( showTime ){
					s.append( "\" runtime=\"" ).append( ms );
				}
				s.append( "\">\n" ).append(hitXMLBuilder.toString() ).append("    </result>\n").toString();
                return s.toString();
			}

			public String toCSV() {
				final StringBuilder csvBuilder = new StringBuilder();
				String lastHit = "";
				for (final Hit hit : hits ) {
					//Note, that there is no reason to output duplicates in csv output format.
					if ( !hit.toCSV().equals( lastHit ) ) {
						csvBuilder.append(NTCIR_MATH_PATTERN.matcher(num).replaceAll("")).append(',').append(hit.toCSV()).append('\n');
						lastHit = hit.toCSV();
					}
				}
				return csvBuilder.toString();
			}

			public void addHit( String item, String filename, int score, int rank ) {
				addHit( item, filename, Integer.toString( score ), Integer.toString( rank ) );
			}

			public void addHit( String id, String filename, String score,
			                    String rank ) {
				hits.add( new Hit( id, filename, score, rank ) );
			}

			public class Hit {
				private final String id;
				private final String filename;
				private final String score;
				private final String rank;

				public Hit( String id, String filename, String score, String rank ) {
					this.id = id;
					this.filename = "math000000000000.xml";
					this.score = score;
					this.rank = rank;
				}

				public String toXML() {
					return "      <hit id=\"" + id + "\" xref=\"" + filename
						+ "\" score=\"" + score + "\" rank=\"" + rank
						+ "\"/>\n";
				}

				public String toCSV() {
					return id;
				}
			}

		}

	}
}
