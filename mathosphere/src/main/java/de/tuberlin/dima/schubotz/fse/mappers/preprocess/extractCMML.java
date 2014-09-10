package de.tuberlin.dima.schubotz.fse.mappers.preprocess;

import de.tuberlin.dima.schubotz.fse.mappers.DataPreprocessTemplate;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.ExtractHelper;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class extractCMML extends DataPreprocessTemplate<RawDataTuple,Tuple3<Integer,String,String>>{

    private Integer qID;

    /**
     * @param in RawDataTuple
     * @param out DataTuple of document
     */
	public void flatMap(RawDataTuple in, Collector<Tuple3<Integer,String,String>> out) {
        qID = Integer.parseInt( in.getField(0).toString().replace( "NTCIR11-Math-","" ));
        data = in.getField(1);
        if ( ! setDoc() ) return;
        mathElements = doc.select("formula, m|formula");
        if ( mathElements.isEmpty()) {
            LOG.warn("Unable to find math tags in data document(" + docID + "): ", data);
            return ;
        }
        for (Element mathElement : mathElements) {
            Tuple3<Integer, String, String> annotationXML = getElement(mathElement);
            if (annotationXML != null ){
                out.collect( annotationXML );
            }
        }

        }

    public Tuple3<Integer,String,String> getElement( Element mathElement) {
        String id = mathElement.attr("id");
        final Elements annotationXMLElements = mathElement.select("annotation-xml, m|annotation-xml");

        for (Element annotationXML : annotationXMLElements) {
            final String encoding = annotationXML.attr("encoding");
            //Add namespace information so canonicalizer can parse it
            annotationXML.attr(ExtractHelper.NAMESPACE_NAME, ExtractHelper.NAMESPACE);
            switch (encoding) {
                case "MathML-Content":
                    return new Tuple3<>(qID,id,annotationXML.toString());
                default:
                    annotationXML.remove();
                    LOG.warn("No Content MathML annotation tag found in query, formulae ",qID,id);
                    break;
            }
        }
        final Elements mathRoots = mathElement.select("math, m|math");
        for (Element root : mathRoots) {
            root.attr("xmlns", ExtractHelper.NAMESPACE);
            root.select("annotation, m|annotation").remove();
            return new Tuple3<>(qID,id,root.toString().replaceAll("<m:","<").replaceAll("</m:","</"));
        }

        return null;
    }


}


