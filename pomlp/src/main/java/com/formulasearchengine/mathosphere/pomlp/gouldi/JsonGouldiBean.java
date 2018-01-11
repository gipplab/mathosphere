package com.formulasearchengine.mathosphere.pomlp.gouldi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties( ignoreUnknown = true )
@JsonPropertyOrder({
        "definitions",
        "constraints",
        "math_inputtex",
        "math_inputtex_semantic",
        "correct_tex",
        "correct_mml",
        "uri",
        "title",
        "comment",
        "type",
        "check"
})
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude( JsonInclude.Include.NON_NULL )
public class JsonGouldiBean {
    @JsonProperty("math_inputtex")
    private String mathTex;

    @JsonProperty("math_inputtex_semantic")
    private String mathTexSemantic;

    @JsonProperty("title")
    private String title;

    @JsonProperty("correct_tex")
    private String correctTex;

    @JsonProperty("uri")
    private String uri;

    @JsonProperty("correct_mml")
    private String mml;

    @JsonProperty("constraints")
    private String[] constraints;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("type")
    private String type;

    @JsonProperty("check")
    private JsonGouldiCheckBean check;

    @JsonIgnore
    private JsonGouldiDefinitionsBean definitionsBean;

    public JsonGouldiBean(){}

    @JsonProperty("definitions")
    private void unpackNested( Map<String, Object> defs ){
        definitionsBean = new JsonGouldiDefinitionsBean();
        LinkedList<JsonGouldiIdentifierDefinienBean> list = new LinkedList<>();
        definitionsBean.setIdentifierDefiniens(list);

        for ( String key : defs.keySet() ){
            JsonGouldiIdentifierDefinienBean bean = new JsonGouldiIdentifierDefinienBean();
            ArrayList<JsonGouldiWikidataDefinienBean> arrList = new ArrayList<>();
            bean.setName( key );

            ArrayList<Object> identifierList = (ArrayList<Object>)defs.get(key);
            for ( Object obj : identifierList ){
                if ( obj instanceof String ){
                    JsonGouldiTextDefinienBean textBean = new JsonGouldiTextDefinienBean();
                    textBean.setDiscription( (String)obj );
                    arrList.add( textBean );
                } else {
                    Map<String, String> qidMappings = (Map<String, String>)obj;
                    for ( String qID : qidMappings.keySet() ){
                        JsonGouldiWikidataDefinienBean wikidefbean = new JsonGouldiWikidataDefinienBean();
                        wikidefbean.setWikiID( qID );
                        wikidefbean.setDiscription( qidMappings.get(qID) );
                        arrList.add( wikidefbean );
                    }
                }
            }

            JsonGouldiWikidataDefinienBean[] arr = new JsonGouldiWikidataDefinienBean[arrList.size()];
            arr = arrList.toArray(arr);
            bean.setDefiniens( arr );
            list.add(bean);
        }
    }

    @JsonGetter("definitions")
    @JsonSerialize( using = JsonGouldiDefinitionSerializer.class )
    public JsonGouldiDefinitionsBean getDefinitions(){
        return definitionsBean;
    }

    public void setDefinitions( JsonGouldiDefinitionsBean definitionsBean ){
        this.definitionsBean = definitionsBean;
    }

    public String getOriginalTex() {
        return mathTex;
    }

    public void setMathTex(String mathTex) {
        this.mathTex = mathTex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCorrectTex() {
        return correctTex;
    }

    public void setCorrectTex(String correctTex) {
        this.correctTex = correctTex;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMml() {
        return mml;
    }

    public void setMml(String mml) {
        this.mml = mml;
    }

    public String getMathTex() {
        return mathTex;
    }

    public String[] getConstraints() {
        return constraints;
    }

    public void setConstraints(String[] constraints) {
        this.constraints = constraints;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonGouldiCheckBean getCheck() {
        return check;
    }

    public void setCheck(JsonGouldiCheckBean check) {
        this.check = check;
    }


    public String getMathTexSemantic() {
        return mathTexSemantic;
    }

    public void setMathTexSemantic(String mathTexSemantic) {
        this.mathTexSemantic = mathTexSemantic;
    }

    @Override
    public String toString(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return mapper.writeValueAsString(this);
        } catch ( Exception e ){
            return e.getMessage();
        }
    }

    @JsonIgnore
    private Map<String, Object> other = new HashMap<>();
    @JsonAnyGetter
    public Map<String, Object> any() {
        return other;
    }

    @JsonAnySetter
    public void set(String name, Object value) {
        other.put(name, value);
    }
    public boolean hasUnknowProperties() {
        return !other.isEmpty();
    }
}
