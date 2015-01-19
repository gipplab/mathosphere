package com.formulasearchengine.backend.basex;

import java.util.LinkedList;

/**
 * Class with inner classes for generating xml trees
 * 
 * @author Tobias Uhlich
 * @author Thanh Phuong Luu
 */
public class Results {

	private LinkedList<Run> runs = new LinkedList<Run>();

	public void addRun(String runtag, Long ms, String type) {
		runs.add(new Run(runtag, ms, type));
	}

	public void addRun(Run run) {
		runs.add(run);
	}

	public void clear() {
		runs.clear();
	}

	public String toXML() {
		String runsXML = "";
		for (Run run : runs) {
			runsXML += run.toXML();
		}

		return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<results xmlns=\"http://ntcir-math.nii.ac.jp/\">\n"
				+ runsXML + "</results>\n";

	}
	public String toCSV(){
			String runsCSV = "queryId,formulaId\n";
			for (Run run : runs) {
				runsCSV += run.toCSV();
			}

			return  runsCSV;

		}

	public class Run {

		private String runtag;
		private Long ms;
		private String type;

		private LinkedList<Result> results = new LinkedList<Result>();

		public Run(String runtag, Long ms, String type) {
			this.runtag = runtag;
			this.ms = ms;
			this.type = type;
		}

		public Run(String runtag, String type) {
			this.runtag = runtag;
			this.type = type;
		}

		public void setTime(Long ms) {
			this.ms = ms;
		}

		public void addResult(String num, Long ms) {
			results.add(new Result(num, ms));
		}

		public void addResult(Result result) {
			results.add(result);
		}

		public String toXML() {
			String resultXML = "";
			for (Result result : results) {
				resultXML += result.toXML();
			}

			return "  <run runtag=\"" + runtag + "\" runtime=\"" + ms
					+ "\" runtype=\"" + type + "\">\n" + resultXML
					+ "  </run>\n";
		}

		public String toCSV () {
			String CSV = "";
			for (Result result : results) {
				CSV += result.toCSV();
			}
			return  CSV;
		}

		public class Result {

			private Long ms;
			private String num;

			private LinkedList<Hit> hits = new LinkedList<Hit>();

			public Result(String num, Long ms) {
				this.ms = ms;
				this.num = num;
			}
			public int size(){
				return hits.size();
			}
			public Result(String num) {
				this.num = num;
			}

			public void setTime(Long ms) {
				this.ms = ms;
			}

			public Long getTime() {
				return this.ms;
			}

			public void addHit(String id, String filename, String score,
					String rank) {
				hits.add(new Hit(id, filename, score, rank));
			}

			public void addHit(Hit hit) {
				hits.add(hit);
			}

			public String toXML() {
				String hitXML = "";
				for (Hit hit : hits) {
					hitXML += hit.toXML();
				}

				return "    <result for=\"NTCIR11-Math-" + num
						+ "\" runtime=\"" + ms + "\">\n" + hitXML
						+ "    </result>\n";
			}

			public String toCSV () {
				String CSV = "";
				String lastHit ="";
				for (Hit hit : hits) {
					//Note, that there is no reason to output duplicates in csv output format.
					if (! hit.toCSV().equals( lastHit ) ) {
						CSV += num.replace( "NTCIR11-Math-", "" ) + "," + hit.toCSV() + "\n";
						lastHit = hit.toCSV();
					}
				}
				return  CSV;
			}

			public  void addHit (String item, String filename, int score, int rank) {
				addHit( item, filename, Integer.toString( score ), Integer.toString( rank ) );
			}

			public class Hit {
				private String id;
				private String filename;
				private String score;
				private String rank;

				public Hit(String id, String filename, String score, String rank) {
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

				public String toCSV () {
					return id;
				}
			}

		}

	}
}
