package com.formulasearchengine.mathosphere.mlp.performance;

import com.formulasearchengine.mathosphere.mlp.Main;

import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Moritz on 28.12.2015.
 */
public class PerformanceHelper {

    private static File getResources(String resourceName) throws Exception {
        PerformanceHelper instance = new PerformanceHelper();
        URL url = instance.getClass().getClassLoader().getResource(resourceName);
        File dir = null;
        try {
            assert url != null;
            dir = new File(url.toURI());
        } catch (Exception e) {
            fail("Cannot open test resource folder.");
        }
        return dir;
    }

    static void runTestCollection(String resourceName, String command) throws Exception {
        runTestCollection(getResources(resourceName), command);
    }

    private static void runTestCollection(File dir, String command) throws Exception {
        //noinspection ConstantConditions
        for (File nextFile : dir.listFiles()) {
            if (nextFile.getName().endsWith("_wiki.txt")) {
                File resultPath = new File(nextFile.getAbsolutePath().replace("_wiki.txt", "_gold.csv"));

                switch (command) {
                    case "extract":
                        if (nextFile.getName().startsWith("mean")) {
                            File expectationPath = new File(nextFile.getAbsolutePath().replace("_wiki.txt", "_definition_exp.json"));
                            runExtractionTest(nextFile, resultPath, expectationPath, command);
                        }
                        break;
                    default:
                        File expectationPath = new File(nextFile.getAbsolutePath().replace("_wiki.txt", "_exp.json"));
                        runTest(nextFile, resultPath, expectationPath, command);
                }
            }
        }
    }

    private static void runTest(File source, File gold, File expectations, String command) throws Exception {
        FileReader in = new FileReader(gold);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);
        Set<String> expected = new HashSet<>();
        for (CSVRecord record : records) {
            expected.add(record.get("identifier"));
        }
        final byte[] jsonData = Files.readAllBytes(expectations.toPath());
        JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(new String(jsonData));
        JSONObject identifiers = jsonObject.getJSONObject("identifiers");

        double expPrec = identifiers.getDouble("precision");
        double expRec = identifiers.getDouble("recall");


        String[] args = {
                command,
                "-in", source.getAbsolutePath(),
                "--tex"
        };
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        final long t0 = System.nanoTime();
        Main.main(args);
        final String standardOutput = myOut.toString();
        System.setOut(stdout);
        System.out.println((System.nanoTime() - t0) / 1000000000 + "s");
        Set<String> sysOut = new HashSet<>(Arrays.asList(standardOutput.split("\r?\n")));
        Set<String> real = new HashSet<>();
        for (String s : sysOut) {
            real.add(s.replaceAll("_\\{(.*?)\\}$", "_"));
        }
        Set<String> tp = new HashSet<>(expected);
        Set<String> fn = new HashSet<>(expected);
        Set<String> fp = new HashSet<>(real);
        fn.removeAll(real);
        fp.removeAll(expected);
        tp.retainAll(real);

        double rec = ((double) tp.size()) / (tp.size() + fn.size());
        double prec = ((double) tp.size()) / (tp.size() + fp.size());


        assertThat("precision", prec, Matchers.greaterThan(expPrec));
        assertThat("recall", rec, Matchers.greaterThan(expRec));
    }


    private static void runExtractionTest(File source, File gold, File expectations, String command) throws Exception {
        FileReader in = new FileReader(gold);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);
        Set<String> expected = new HashSet<>();
        for (CSVRecord record : records) {
            expected.add(record.get("identifier") + "," + record.get("definition"));
        }
        final byte[] jsonData = Files.readAllBytes(expectations.toPath());
        JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(new String(jsonData));
        JSONObject identifiers = jsonObject.getJSONObject("identifiers");

        double expPrec = identifiers.getDouble("precision");
        double expRec = identifiers.getDouble("recall");


        String[] args = {
                command,
                "-in", source.getAbsolutePath(),
                "--tex"
        };
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        final long t0 = System.nanoTime();
        Main.main(args);
        final String standardOutput = myOut.toString();
        System.setOut(stdout);
        System.out.println((System.nanoTime() - t0) / 1000000000 + "s");
        Set<String> sysOut = new HashSet<>(Arrays.asList(standardOutput.split("\r?\n")));
        Set<String> real = new HashSet<>();
        for (String s : sysOut) {
            real.add(sanitiseExtractionResult(s));
        }
        Set<String> tp = new HashSet<>(expected);
        Set<String> fn = new HashSet<>(expected);
        Set<String> fp = new HashSet<>(real);
        fn.removeAll(real);
        fp.removeAll(expected);
        tp.retainAll(real);

        double rec = ((double) tp.size()) / (tp.size() + fn.size());
        double prec = ((double) tp.size()) / (tp.size() + fp.size());


        assertThat("precision", prec, Matchers.greaterThan(expPrec));
        assertThat("recall", rec, Matchers.greaterThan(expRec));
    }

    @Test
    public void testSanitiseExtractionResult() {
        Assert.assertEquals("strip away score", "x, speed", PerformanceHelper.sanitiseExtractionResult("x, speed, 1"));
        Assert.assertEquals("strip away modifier", "x_, speed", PerformanceHelper.sanitiseExtractionResult("x_{}, speed, 1"));
    }

    public static String sanitiseExtractionResult(String s) {
        return s.replaceFirst("(.+?),(.+?),(.+)", "$1,$2").replaceFirst("_\\{.*\\}", "_");
    }

    @Test
    public void maximisePrecision() throws Exception {
        File gold = getResources("com/formulasearchengine/mathosphere/mlp/performance/mean_gold.csv");
        FileReader in = new FileReader(gold);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);
        Set<String> expected = new HashSet<>();
        for (CSVRecord record : records) {
            expected.add(record.get("identifier") + "," + record.get("definition"));
        }
        int precision = 100;
        for (int i = 0; i < precision; i++) {
            final String standardOutput = MEAN_DEFINIENS_EXTRACTION_OUTPUT;
            Set<String> sysOut = new HashSet<>(Arrays.asList(standardOutput.split("\r?\n")));
            Set<String> real = new HashSet<>();
            List<Relation> relations = new ArrayList<>();
            for (String s : sysOut) {
                String[] data = s.split(",");
                Relation relation = new Relation();
                relation.setIdentifier(data[0]);
                relation.setDefinition(data[1]);
                relation.setScore(Double.parseDouble(data[2]));
                relations.add(relation);
            }
            double minScore = (double) i / (double) precision;
            System.out.println("minScore:" + minScore);
            relations = relations.stream().filter(r -> r.getScore() > minScore).collect(Collectors.toList());
            real.addAll(relations.stream().map(r -> r.getIdentifier() + "," + r.getDefinition()).collect(Collectors.toList()));
            Set<String> tp = new HashSet<>(expected);
            Set<String> fn = new HashSet<>(expected);
            Set<String> fp = new HashSet<>(real);
            fn.removeAll(real);
            fp.removeAll(expected);
            tp.retainAll(real);

            double rec = ((double) tp.size()) / (tp.size() + fn.size());
            double prec = ((double) tp.size()) / (tp.size() + fp.size());

            System.out.println("rec: " + rec);
            System.out.println("prec: " + prec);
        }
    }

    private static String MEAN_DEFINIENS_EXTRACTION_OUTPUT = "A,geometric construction,0.9552325146407707\n" +
            "A,vertices,0.9199347585870997\n" +
            "A,height,0.844851838198453\n" +
            "A,[[trapezoid]],0.8277964882291066\n" +
            "A,harmonic mean,0.823758246221796\n" +
            "A,wall,0.7368405519674441\n" +
            "A,[[Pythagorean means]],0.7301587301587302\n" +
            "A,harmonic mean,0.7119167004418916\n" +
            "A,[[crossed ladders problem]],0.6039050378433966\n" +
            "A,heights,0.5809479353120643\n" +
            "A,numbers,0.572813749649663\n" +
            "A,tan,0.5643513066790318\n" +
            "A,tan,0.5643513066790318\n" +
            "A,angle,0.548478290806016\n" +
            "A,sequence,0.5071672426980666\n" +
            "A,opposite wall,0.48820430292149974\n" +
            "A,numerator,0.4722130363210386\n" +
            "A,denominator,0.4722130363210386\n" +
            "A,parallel sides,0.46555288378831433\n" +
            "A,product,0.45634002044802274\n" +
            "B,height,0.844851838198453\n" +
            "B,vertices,0.8277964882291066\n" +
            "B,distances,0.746031746031746\n" +
            "B,opposite wall,0.7368405519674441\n" +
            "B,[[crossed ladders problem]],0.7368405519674441\n" +
            "B,harmonic mean,0.7031334261520246\n" +
            "B,[[trapezoid]],0.6948609741050589\n" +
            "B,harmonic mean,0.5912918803721201\n" +
            "B,sequence,0.5791602391831622\n" +
            "B,intersection,0.572813749649663\n" +
            "B,ABC,0.572813749649663\n" +
            "B,[[equilateral triangle]],0.5424649987517378\n" +
            "B,ladders,0.541318039901861\n" +
            "B,harmonic mean,0.5238095239822985\n" +
            "B,point P,0.5079579322822297\n" +
            "B,point P,0.5079579322822297\n" +
            "B,[[circumcircle]],0.5008506398419856\n" +
            "B,distance,0.49245725383449407\n" +
            "B,[[minor arc]],0.49245725383449407\n" +
            "B,parallel sides,0.4865276407717256\n" +
            "G,side DA,0.9500119385371297\n" +
            "G,side BC,0.8948402760288094\n" +
            "G,FEG,0.6993177639632263\n" +
            "G,[[diagonal]],0.5742110304977694\n" +
            "G,intersection,0.5325966715880174\n" +
            "H,harmonic mean,0.9869785463868026\n" +
            "H,purple color,0.8909046586739348\n" +
            "H,[[real number]],0.8055775946578343\n" +
            "H,Harmonic mean,0.7940893624136869\n" +
            "x_{n},harmonic mean,0.7619047619047619\n" +
            "x_{n},dataset,0.7619047619047619\n" +
            "x_{n},[[weight function]],0.5247395833333334\n" +
            "x_{n},set,0.5239672391563718\n" +
            "M,special case,0.9869785463868026\n" +
            "M,[[power mean]],0.7619047619047619\n" +
            "Q,fourth mean,0.8948402760288094\n" +
            "Q,[[quadratic mean]],0.6993177639632263\n" +
            "a,simple example,0.9430225024307586\n" +
            "a,numbers,0.8744211421904182\n" +
            "a,harmonic mean,0.8293871184673581\n" +
            "a,fourth mean,0.816768810115425\n" +
            "a,nonempty dataset,0.7049272643355204\n" +
            "a,[[Pythagorean means]],0.6826509618950466\n" +
            "a,values,0.6679606564858476\n" +
            "a,[[quadratic mean]],0.6591773821959805\n" +
            "a,harmonic mean,0.6126939670580961\n" +
            "a,list,0.6093675346147286\n" +
            "a,set,0.543594285922011\n" +
            "a,[[mean-preserving spread]],0.5362682785960037\n" +
            "a,numbers,0.5172292642567354\n" +
            "a,set,0.5039199383281945\n" +
            "a,geometric construction,0.49495723048805434\n" +
            "a,speed,0.4887565617253917\n" +
            "a,speed,0.4887565617253917\n" +
            "a,distance,0.4814840421959133\n" +
            "a,vehicle,0.4814305543993843\n" +
            "a,speed,0.4812655116053623\n" +
            "b,[[Altitude (triangle)]],0.8909046586739348\n" +
            "b,legs,0.8357329961656145\n" +
            "b,numbers,0.8055775946578343\n" +
            "b,harmonic mean,0.7266070058510906\n" +
            "b,[[right triangle]],0.7027974820415668\n" +
            "b,real numbers,0.6881071766323678\n" +
            "b,tangent,0.6793239023425007\n" +
            "b,[[hypotenuse]],0.6402104841000313\n" +
            "b,[[Pythagorean means]],0.6223945031733412\n" +
            "b,geometric construction,0.5166262966975033\n" +
            "b,right angle,0.5151037506345745\n" +
            "b,angle,0.49614081085800765\n" +
            "b,product,0.40350821244657115\n" +
            "b,[[Trigonometric functions]],0.40350821244657115\n" +
            "c,hypotenuse,0.9869785463868026\n" +
            "c,harmonic mean,0.9147141824834585\n" +
            "c,right triangle,0.8948402760288094\n" +
            "c,inscribed squares,0.6993177639632263\n" +
            "c,sides,0.5535714285714286\n" +
            "w_{1},[[weight function]],0.9869785463868026\n" +
            "w_{1},set,0.8948402760288094\n" +
            "w_{1},dataset,0.5742110304977694\n" +
            "w_{1},harmonic mean,0.5238309481552457\n" +
            "x_{1},dataset,0.9631690225772787\n" +
            "x_{1},numbers,0.9278712665236076\n" +
            "x_{1},harmonic mean,0.7266070058510906\n" +
            "x_{1},special case,0.7027974820415668\n" +
            "x_{1},harmonic mean,0.5742110304977694\n" +
            "x_{1},[[weight function]],0.5166262966975033\n" +
            "x_{1},set,0.5043935738892468\n" +
            "h,[[Altitude (triangle)]],0.9552325146407707\n" +
            "h,right angle,0.918265906791098\n" +
            "h,[[hypotenuse]],0.8630942442827777\n" +
            "h,height,0.823758246221796\n" +
            "h,harmonic mean,0.7940893624136869\n" +
            "h,harmonic mean,0.7619047619047619\n" +
            "h,alley floor,0.7368405519674441\n" +
            "h,legs,0.6675717322171946\n" +
            "h,harmonic mean,0.5912918803721201\n" +
            "h,[[right triangle]],0.572813749649663\n" +
            "h,ladders,0.5571910557748768\n" +
            "h,height,0.4885117677558526\n" +
            "h,height,0.4885117677558526\n" +
            "h,ladders,0.4747170389700246\n" +
            "h,[[crossed ladders problem]],0.4733920327073241\n" +
            "h,distances,0.46032311524229264\n" +
            "h,alley,0.45678716035554273\n" +
            "h,feet,0.4567659392389691\n" +
            "h,base,0.456765739460699\n" +
            "h,sidewall,0.4567657360453948\n" +
            "j,term,0.9500119385371297\n" +
            "j,time,0.8293871184673581\n" +
            "j,times,0.646204026982865\n" +
            "j,numbers,0.5742110304977694\n" +
            "j,product,0.5404358205070271\n" +
            "j,arithmetic mean,0.5258878108967118\n" +
            "j,denominator,0.5238692867505265\n" +
            "n,numbers,0.9516807903331315\n" +
            "n,times,0.9199347585870997\n" +
            "n,numbers,0.8607248540714689\n" +
            "n,numbers,0.8607248540714689\n" +
            "n,product,0.8277964882291066\n" +
            "n,time,0.7623433306676551\n" +
            "n,power,0.7171372765455327\n" +
            "n,first term,0.6713873944059928\n" +
            "n,arithmetic mean,0.6481469920365392\n" +
            "n,harmonic mean,0.6126939670580961\n" +
            "n,geometric mean,0.5595458486260884\n" +
            "n,numerator,0.5595458486260884\n" +
            "n,harmonic mean,0.5247395833333334\n" +
            "n,[[real number]],0.5218253968253969\n" +
            "n,term,0.4865276407717256\n" +
            "n,denominator,0.4865276407717256\n" +
            "n,relationship,0.4722130363210386\n" +
            "n,arithmetic means,0.46032311524229264\n" +
            "n,arithmetic mean,0.44534950999497236\n" +
            "q,distances,0.9631690225772787\n" +
            "q,harmonic mean,0.9262024147276059\n" +
            "q,ABC,0.8055775946578343\n" +
            "q,[[equilateral triangle]],0.7380952380952381\n" +
            "q,[[circumcircle]],0.5807502575861709\n" +
            "q,point P,0.5404358205070271\n" +
            "q,point P,0.5247395833333334\n" +
            "q,intersection,0.5087871477784935\n" +
            "q,[[minor arc]],0.5087871477784935\n" +
            "q,distance,0.5020782870871879\n" +
            "s,sides,0.8293871184673581\n" +
            "s,harmonic mean,0.7266070058510906\n" +
            "s,inscribed squares,0.6045597813956948\n" +
            "s,right triangle,0.5404358205070271\n" +
            "s,hypotenuse,0.5282030976987706\n" +
            "t,distances,0.8710307522192856\n" +
            "t,harmonic mean,0.8293871184673581\n" +
            "t,ABC,0.6755082401537025\n" +
            "t,sides,0.6402104841000313\n" +
            "t,harmonic mean,0.6356510695894283\n" +
            "t,[[equilateral triangle]],0.6223945031733412\n" +
            "t,intersection,0.5297619047619048\n" +
            "t,[[circumcircle]],0.5297619047619048\n" +
            "t,point P,0.5282030976987706\n" +
            "t,point P,0.5239672391563718\n" +
            "t,[[minor arc]],0.5020782870871879\n" +
            "t,distance,0.5003937617710019\n" +
            "t,inscribed squares,0.4944641487082335\n" +
            "t,right triangle,0.4690958178355755\n" +
            "t,hypotenuse,0.46563230347013834\n" +
            "x_{2},harmonic mean,0.8948402760288094\n" +
            "x_{2},numbers,0.8948402760288094\n" +
            "x_{2},special case,0.646204026982865\n" +
            "x,speed,0.9516807903331315\n" +
            "x,[[real number]],0.9512642606725168\n" +
            "x,harmonic mean,0.9147141824834585\n" +
            "x,speed,0.8607248540714689\n" +
            "x,kilometres,0.7940893624136869\n" +
            "x,[[arithmetic mean]],0.7880439605075102\n" +
            "x,certain distance,0.7583750766994011\n" +
            "x,kilometres,0.7266070058510906\n" +
            "x,resistance,0.7250737844820406\n" +
            "x,average speed,0.7147022439463286\n" +
            "x,harmonic mean,0.7119167004418916\n" +
            "x,harmonic mean,0.6993177639632263\n" +
            "x,same amount,0.6674191404377388\n" +
            "x,hour,0.6640200079095552\n" +
            "x,average speed,0.6237463076846663\n" +
            "x,60LINK_4d6ecc544bc18ab12f0642e58c8ca719,0.6210307522192855\n" +
            "x,time,0.6118415457799045\n" +
            "x,hour,0.6109062709291939\n" +
            "x,vehicle,0.5870967471196701\n" +
            "x,sum,0.5769796813438103\n" +
            "y,speed,0.9869785463868026\n" +
            "y,speed,0.9516807903331315\n" +
            "y,kilometres,0.8948402760288094\n" +
            "y,resistance,0.8369153302619451\n" +
            "y,harmonic mean,0.8293871184673581\n" +
            "y,kilometres,0.8293871184673581\n" +
            "y,48Ω,0.7880439605075102\n" +
            "y,average speed,0.7821846005089249\n" +
            "y,time,0.770279838604163\n" +
            "y,hour,0.7619047619047619\n" +
            "y,[[arithmetic mean]],0.7583750766994011\n" +
            "y,same distance,0.7261904761904762\n" +
            "y,harmonic mean,0.7031334261520246\n" +
            "y,hour,0.6993177639632263\n" +
            "y,above example,0.6908927201368048\n" +
            "y,average speed,0.6342992650781031\n" +
            "y,same amount,0.6283057221952694\n" +
            "y,total resistance,0.6210307522192855\n" +
            "y,distance,0.6139462891934832\n" +
            "y,point P,0.5888844432485723\n" +
            "w_{n},dataset,0.7619047619047619\n" +
            "w_{n},[[weight function]],0.7619047619047619\n" +
            "w_{n},set,0.646204026982865\n" +
            "w_{n},harmonic mean,0.5247395833333334\n";
}
