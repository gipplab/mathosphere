package com.formulasearchengine.mathmltools.mml.elements;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiBean;
import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiCheckBean;
import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiDefinitionsBean;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class Goldener extends JsonGouldiBean {

    private JsonGouldiBean bean;
    private boolean augmented = false;

    public Goldener(JsonGouldiBean value) {
        bean = value;
    }

    public void augment() {
        try {
            final MathDoc math = new MathDoc(MathDoc.tryFixHeader(getMml()));
            math.fixGoldCd();
            math.changeTeXAnnotation(getOriginalTex());
            setMml(math.toString());
            augmented = true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            augmented = false;
        }
    }


    @Override
    public JsonGouldiDefinitionsBean getDefinitions() {
        return bean.getDefinitions();
    }

    @Override
    public void setDefinitions(JsonGouldiDefinitionsBean definitionsBean) {
        bean.setDefinitions(definitionsBean);
    }

    @Override
    public String getOriginalTex() {
        return bean.getOriginalTex();
    }

    @Override
    public void setMathTex(String mathTex) {
        bean.setMathTex(mathTex);
    }

    @Override
    public String getTitle() {
        return bean.getTitle();
    }

    @Override
    public void setTitle(String title) {
        bean.setTitle(title);
    }

    @Override
    public String getCorrectTex() {
        return bean.getCorrectTex();
    }

    @Override
    public void setCorrectTex(String correctTex) {
        bean.setCorrectTex(correctTex);
    }

    @Override
    public String getUri() {
        return bean.getUri();
    }

    @Override
    public void setUri(String uri) {
        bean.setUri(uri);
    }

    @Override
    public String getMml() {
        return bean.getMml();
    }

    @Override
    public void setMml(String mml) {
        bean.setMml(mml);
    }

    @Override
    public String getMathTex() {
        return bean.getMathTex();
    }

    @Override
    public List<String> getConstraints() {
        return bean.getConstraints();
    }

    @Override
    public void setConstraints(List<String> constraints) {
        bean.setConstraints(constraints);
    }

    @Override
    public String getComment() {
        return bean.getComment();
    }

    @Override
    public void setComment(String comment) {
        bean.setComment(comment);
    }

    @Override
    public String getType() {
        return bean.getType();
    }

    @Override
    public void setType(String type) {
        bean.setType(type);
    }

    @Override
    public JsonGouldiCheckBean getCheck() {
        return bean.getCheck();
    }

    @Override
    public void setCheck(JsonGouldiCheckBean check) {
        bean.setCheck(check);
    }

    @Override
    public String getMathTexSemantic() {
        return bean.getMathTexSemantic();
    }

    @Override
    public void setMathTexSemantic(String mathTexSemantic) {
        bean.setMathTexSemantic(mathTexSemantic);
    }

    @Override
    public String toString() {
        return bean.toString();
    }

    @Override
    public Map<String, Object> any() {
        return bean.any();
    }

    @Override
    public void set(String name, Object value) {
        bean.set(name, value);
    }

    @Override
    public boolean hasUnknowProperties() {
        return bean.hasUnknowProperties();
    }
}
