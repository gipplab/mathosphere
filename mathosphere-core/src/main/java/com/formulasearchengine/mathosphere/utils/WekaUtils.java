package com.formulasearchengine.mathosphere.utils;

import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import com.formulasearchengine.mathosphere.mlp.text.MyPatternMatcher;
import weka.core.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.formulasearchengine.mathosphere.mlp.text.MyPatternMatcher.match;

/**
 * Created by Leo on 21.12.2016.
 */
public class WekaUtils {
  public static final String MATCH = "match";
  public static final String NO_MATCH = "no match";
  public static final int ATTRIBUTE_SENTENCE = 36;

  public static Instances createInstances(String title) {
    ArrayList<Attribute> atts = new ArrayList<>();
    //meta information
    atts.add(new Attribute("title", (FastVector) null));//0
    atts.add(new Attribute("identifier", (FastVector) null));//1
    atts.add(new Attribute("definiens", (FastVector) null));//2
    atts.add(new Attribute("identifierPos"));//3
    atts.add(new Attribute("definiensPos"));//4
    atts.add(new Attribute("wordDistance"));//5
    atts.add(new Attribute("wordPositioning"));//6
    atts.add(new Attribute("definiensLength"));//7
    atts.add(new Attribute("pattern1"));//8
    atts.add(new Attribute("pattern2"));//9
    atts.add(new Attribute("pattern3"));//10
    atts.add(new Attribute("pattern4"));//11
    atts.add(new Attribute("pattern5"));//12
    atts.add(new Attribute("pattern6"));//13
    atts.add(new Attribute("pattern7"));//14
    atts.add(new Attribute("pattern8"));//15
    atts.add(new Attribute("pattern9"));//16
    atts.add(new Attribute("pattern10"));//17
    atts.add(new Attribute("pattern11"));//18
    atts.add(new Attribute("pattern12"));//19
    atts.add(new Attribute("pattern13"));//20
    atts.add(new Attribute("pattern14"));//21
    atts.add(new Attribute("pattern15"));//22
    atts.add(new Attribute("pattern16"));//23
    atts.add(new Attribute("null0"));//23
    atts.add(new Attribute("null1"));//24
    atts.add(new Attribute("null2"));//26
    atts.add(new Attribute("null3"));//27
    atts.add(new Attribute("null4"));//28
    atts.add(new Attribute("null5"));//29
    atts.add(new Attribute("null6"));//30
    atts.add(new Attribute("null7"));//31
    atts.add(new Attribute("null8"));//32
    atts.add(new Attribute("null9"));//33
    atts.add(new Attribute("null10"));//34
    atts.add(new Attribute("null11"));//35
    //this is where the real attrs begin
    atts.add(new Attribute("sentence", (FastVector) null));//36
    //TODO expand
    //classification
    ArrayList nominal = new ArrayList();
    nominal.add(MATCH);
    nominal.add(NO_MATCH);
    atts.add(new Attribute("classification", nominal));//36
    Instances result = new Instances(title, atts, 0);
    result.setClassIndex(result.numAttributes() - 1);
    return result;
  }

  public static Instances addRelationsToInstances(List<Relation> relations, String title, Instances instances) {
    List nominal = new ArrayList();
    nominal.add(MATCH);
    nominal.add(NO_MATCH);
    for (Relation relation : relations) {
      int[] patternMatches = match(relation.getSentence(), relation.getIdentifier(), relation.getDefinition(), relation.getIdentifierPosition(), relation.getWordPosition());
      double[] values = new double[instances.numAttributes()];
      values[0] = instances.attribute(0).addStringValue(title);
      values[1] = instances.attribute(1).addStringValue(relation.getIdentifier());
      values[2] = instances.attribute(2).addStringValue(relation.getDefinition());
      values[3] = relation.getIdentifierPosition();
      values[4] = relation.getWordPosition();
      //distance between identifier and definiens candidate
      values[5] = Math.abs(relation.getIdentifierPosition() - relation.getWordPosition());
      //weather or not the definiens is before or after the identifier
      values[6] = Math.signum(relation.getIdentifierPosition() - relation.getWordPosition());
      values[7] = 0;
      for (int i = 0; i < patternMatches.length; i++) {
        values[8 + i] = patternMatches[i];
      }
      if (relation.getIdentifierPosition() > relation.getWordPosition()) {
        values[ATTRIBUTE_SENTENCE] = instances.attribute(ATTRIBUTE_SENTENCE).addStringValue(
          relation.getSentence().getWords()
            .subList(relation.getWordPosition(), relation.getIdentifierPosition() + 1)
            .toString());
      } else {
        values[ATTRIBUTE_SENTENCE] = instances.attribute(ATTRIBUTE_SENTENCE).addStringValue(
          relation.getSentence().getWords()
            .subList(relation.getIdentifierPosition(), relation.getWordPosition() + 1)
            .toString());
      }
      values[values.length - 1] = relation.getRelevance() > 1 ? nominal.indexOf(MATCH) : nominal.indexOf(NO_MATCH);
      instances.add(new DenseInstance(1.0, values));
    }
    return instances;
  }
}
