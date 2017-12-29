package com.formulasearchengine.mathosphere.pomlp.comparison;

import com.formulasearchengine.mathosphere.pomlp.convertor.Converters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

import static com.formulasearchengine.mathosphere.pomlp.util.Constants.NL;


/**
 * @author Andre Greiner-Petter
 */
public class CSVResultWriter {
    private static final Logger LOG = LogManager.getLogger( CSVResultWriter.class.getName() );

    public static final String CSV_SEP = ",";

    private Info[] results;
    private LinkedList<ComparisonError> errors;

    public static final Path RESULT_PATH = Paths.get("results");

    public CSVResultWriter( int max ){
        results = new Info[max];
        for ( int i = 0; i < max; i++ )
            results[i] = new Info();
        errors = new LinkedList<>();
    }

    public void addResult( ComparisonResult result ){
        results[result.getIndex()-1].all[result.getConverter().getPosition()] = result;
    }

    public void addError( ComparisonError error ){
        errors.addLast( error );
    }

    public void writeToFile( Path output ) throws IOException {
        LocalDateTime localDateTime = LocalDateTime.now();
        Path filePath = RESULT_PATH.resolve("latest-results.csv");
        LOG.info("Write results to " + filePath.toAbsolutePath());
        if (!Files.exists(filePath)) Files.createFile(filePath);

        String header = "GOLD" + CSV_SEP;
        for ( Converters c : Converters.values() )
            header += c.name() + CSV_SEP;
        header = header.substring( 0, header.length()-CSV_SEP.length() );

        try ( BufferedWriter w = Files.newBufferedWriter(filePath) ){
            w.write( header + NL );

            for ( int i = 0; i < results.length; i++ ){
                w.write( (i+1) + CSV_SEP );
                w.write( results[i].toString() + NL );
            }
        } catch ( IOException ioe ){
            LOG.error("Cannot write result file.", ioe);
        }

        Path fileErrorPath = RESULT_PATH.resolve("latest-errors.txt");
        LOG.info("Write errors to " + fileErrorPath.toAbsolutePath());
        if ( !Files.exists(fileErrorPath) ) Files.createFile(fileErrorPath);

        try (BufferedWriter w = Files.newBufferedWriter(fileErrorPath)){
            w.write( localDateTime.toString() + NL );
            for ( ComparisonError error : errors ){
                w.write( error.toString() + NL);
            }
        } catch ( IOException ioe ){
            LOG.error("Cannot write error file.", ioe);
        }

        analyzeResults();
        Path fileAnalizationPath = RESULT_PATH.resolve("latest-analization.txt");
        LOG.info("Write all info to " + fileAnalizationPath.toAbsolutePath());
        if ( !Files.exists(fileAnalizationPath) ) Files.createFile(fileAnalizationPath);

        try (BufferedWriter w = Files.newBufferedWriter(fileAnalizationPath)){
            w.write( localDateTime.toString() + NL );
            w.write( analizationToString() );
        } catch ( IOException ioe ){
            LOG.error("Cannot write error file.", ioe);
        }
    }

    private double[] averagesCont, averagesPres;
    private double[] totalCont, totalPres;
    private HighLow[] highestCont, highestPres, lowestCont, lowestPres;
    private int[] nullContentCounter, nullPresCounter;
    private int[] numberOfConst, numberOfPres;

    public void analyzeResults(){
        Converters[] convs = Converters.values();
        averagesCont = new double[convs.length+1];
        averagesPres = new double[convs.length+1];
        totalCont = new double[convs.length+1];
        totalPres = new double[convs.length+1];
        numberOfConst = new int[convs.length+1];
        numberOfPres = new int[convs.length+1];
        nullContentCounter = new int[convs.length+1];
        nullPresCounter = new int[convs.length+1];
        highestCont = new HighLow[convs.length+1];
        highestPres = new HighLow[convs.length+1];
        lowestCont = new HighLow[convs.length+1];
        lowestPres = new HighLow[convs.length+1];

        for ( int i = 0; i < convs.length+1; i++ ){
            highestCont[i] = new HighLow();
            lowestCont[i] = new HighLow();
            highestPres[i] = new HighLow();
            lowestPres[i] = new HighLow();
            highestCont[i].distance = Double.MIN_VALUE;
            highestPres[i].distance = Double.MIN_VALUE;
            lowestCont[i].distance = Double.MAX_VALUE;
            lowestPres[i].distance = Double.MAX_VALUE;
        }

        int allIndex = convs.length;

        for ( Info i : results ){
            i.analyzeDists();
            for ( int idx = 0; idx < convs.length; idx++ ){
                if ( i.all[idx] == null ){
                    LOG.warn("Strange, just error here.");
                    continue;
                }
                if ( i.all[idx].getContentDistance() != null ){
                    double curr = i.all[idx].getContentDistance();
                    averagesCont[idx] += curr;
                    numberOfConst[idx]++;
                    if ( curr > highestCont[idx].distance ){
                        highestCont[idx].distance = curr;
                        highestCont[idx].index = idx;
                    }
                    if ( curr < lowestCont[idx].distance ){
                        lowestCont[idx].distance = curr;
                        lowestCont[idx].index = idx;
                    }
                } else nullContentCounter[idx]++;
                if ( i.all[idx].getPresentationDistance() != null ){
                    double curr = i.all[idx].getPresentationDistance();
                    averagesPres[idx] += curr;
                    numberOfPres[idx]++;
                    if ( curr > highestPres[idx].distance ){
                        highestPres[idx].distance = curr;
                        highestPres[idx].index = idx;
                    }
                    if ( curr < lowestPres[idx].distance ){
                        lowestPres[idx].distance = curr;
                        lowestPres[idx].index = idx;
                    }
                } else nullPresCounter[idx]++;
            }
            averagesCont[allIndex] += i.averagePresDist;
            averagesPres[allIndex] += i.averageContDist;
            numberOfConst[allIndex] += i.totalContDist;
            numberOfPres[allIndex] += i.totalPresDist;
        }

        HighLow tmpLC = new HighLow(Double.MAX_VALUE);
        HighLow tmpLP = new HighLow(Double.MAX_VALUE);
        HighLow tmpHC = new HighLow(Double.MIN_VALUE);
        HighLow tmpHP = new HighLow(Double.MIN_VALUE);
        for ( int i = 0; i < convs.length; i++ ){
            totalCont[i] = averagesCont[i];
            totalPres[i] = averagesPres[i];
            averagesCont[i] /= numberOfConst[i];
            averagesPres[i] /= numberOfPres[i];
            if ( highestCont[i].distance > tmpHC.distance )
                tmpHC.copy( highestCont[i], convs[i] );
            if ( highestPres[i].distance > tmpHP.distance )
                tmpHP.copy( highestPres[i], convs[i] );
            if ( lowestCont[i].distance < tmpLC.distance )
                tmpLC.copy( lowestCont[i], convs[i] );
            if ( lowestPres[i].distance < tmpLP.distance )
                tmpLP.copy( lowestPres[i], convs[i] );
        }

        totalCont[allIndex] = averagesCont[allIndex];
        totalPres[allIndex] = averagesPres[allIndex];
        averagesCont[allIndex] /= numberOfConst[allIndex];
        averagesPres[allIndex] /= numberOfPres[allIndex];
        highestCont[allIndex].copy( tmpHC );
        highestPres[allIndex].copy( tmpHP );
        lowestCont[allIndex].copy( tmpLC );
        lowestPres[allIndex].copy( tmpLP );
    }

    private String analizationToString(){
        int last = Converters.values().length;
        String NL = System.lineSeparator();
        String TAB = "   ";
        String str = "Final Analization:" + NL;
        str += "Converters: " + Arrays.toString( Converters.values() ) + NL;
        str += NL;
        str += "       Total Content Distance: " + totalCont[last] + NL;
        str += "  Total Presentation Distance: " + totalPres[last] + NL;
        str += "     Maximum Content Distance: " + highestCont[last] + NL;
        str += "      Lowest Content Distance: " + lowestCont[last] + NL;
        str += "Maximum Presentation Distance: " + highestPres[last] + NL;
        str += " Lowest Presentation Distance: " + lowestPres[last] + NL;
        str += "     Average Content Distance: " + averagesCont[last] + NL;
        str += "Average Presentation Distance: " + averagesPres[last] + NL;
        str += "        # Content Comparisons: " + numberOfConst[last] + NL;
        str += "   # Presentation Comparisons: " + numberOfPres[last] + NL;
        str += "    # Not Content Comparisons: " + nullContentCounter[last] + NL;
        str += "#Not Presentation Comparisons: " + nullPresCounter[last] + NL;
        str += NL;
        str += "Different Presentation: " + NL;
        for ( Converters conv : Converters.values() ){
            str += conv.name() + ":" + NL;
            str += TAB + "       Total Content Distance: " + totalCont[conv.getPosition()] + NL;
            str += TAB + "  Total Presentation Distance: " + totalPres[conv.getPosition()] + NL;
            str += TAB + "     Maximum Content Distance: " + highestCont[conv.getPosition()] + NL;
            str += TAB + "      Lowest Content Distance: " + lowestCont[conv.getPosition()] + NL;
            str += TAB + "Maximum Presentation Distance: " + highestPres[conv.getPosition()] + NL;
            str += TAB + " Lowest Presentation Distance: " + lowestPres[conv.getPosition()] + NL;
            str += TAB + "     Average Content Distance: " + averagesCont[conv.getPosition()] + NL;
            str += TAB + "Average Presentation Distance: " + averagesPres[conv.getPosition()] + NL;
            str += TAB + "        # Content Comparisons: " + numberOfConst[conv.getPosition()] + NL;
            str += TAB + "   # Presentation Comparisons: " + numberOfPres[conv.getPosition()] + NL;
            str += TAB + "    # Not Content Comparisons: " + nullContentCounter[conv.getPosition()] + NL;
            str += TAB + "#Not Presentation Comparisons: " + nullPresCounter[conv.getPosition()] + NL;
            str += NL;
        }

        str += NL;
        str += "Number of Errors: " + errors.size() + NL;
        for ( ComparisonError e : errors ){
            str += e.simpleToString() + NL;
        }
        str += NL;

        return str;
    }

    private class HighLow {
        private int index;
        private double distance;
        private Converters converter = null;

        public HighLow(){}

        public HighLow(double maxmin){
            distance = maxmin;
        }

        public void copy( HighLow c ){
            this.index = c.index;
            this.distance = c.distance;
            this.converter = c.converter;
        }

        public void copy( HighLow c, Converters conv ){
            this.index = c.index;
            this.distance = c.distance;
            this.converter = conv;
        }

        @Override
        public String toString(){
            if ( converter == null )
                return "[ Idx: " + index + " / Distance: " + distance + "]";
            return "[" + converter.name() + " - " + index + ": " + distance + "]";
        }
    }

    private class Info {
        private ComparisonResult[] all = new ComparisonResult[Converters.values().length];

        private double averageContDist, averagePresDist;
        private double totalContDist, totalPresDist;

        public void analyzeDists(){
            int cont = 0, pres = 0;
            averageContDist = 0;
            averagePresDist = 0;
            for ( ComparisonResult r : all ){
                if ( r == null ) {
                    //LOG.warn("Strange, no comparison results: " + );
                    continue;
                }
                if ( r.getContentDistance() != null ){
                    averageContDist += r.getContentDistance();
                    cont++;
                }
                if ( r.getPresentationDistance() != null ){
                    averagePresDist += r.getPresentationDistance();
                    pres++;
                }
            }
            totalContDist = averageContDist;
            totalPresDist = averagePresDist;

            averageContDist = averageContDist/cont;
            averagePresDist = averagePresDist/pres;
        }

        @Override
        public String toString(){
            String str = "";
            for ( int i = 0; i < all.length; i++ ){
                str += all[i] != null ? all[i].resultToString() : " ";
                if ( i < all.length-1 ) str += CSV_SEP;
            }
            return str;
        }
    }
}
