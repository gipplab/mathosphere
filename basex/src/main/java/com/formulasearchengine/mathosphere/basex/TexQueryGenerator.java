package com.formulasearchengine.mathosphere.basex;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formulasearchengine.mathmlconverters.latexml.LaTeXMLConverter;
import com.formulasearchengine.mathmlconverters.latexml.LaTeXMLServiceResponse;
import com.formulasearchengine.mathmlconverters.latexml.LateXMLConfig;
import com.google.common.collect.Lists;
import net.sf.saxon.trans.Err;
import net.xqj.basex.bin.L;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TexQueryGenerator {

    private List<NameValuePair> params = getDefaultParams();
    private LateXMLConfig lateXMLConfig = new LateXMLConfig()
            .setUrl("http://drmf-latexml.wmflabs.org")
            .setParams(updateParamFormat(this.getParams()));
    private LaTeXMLServiceResponse serviceResponse;


    List<NameValuePair> getParams() {
        return params;
    }

    @NotNull
    private ArrayList<NameValuePair> getDefaultParams() {
        ArrayList<NameValuePair> pDefault = new ArrayList<>();
        pDefault.add(new BasicNameValuePair("format", "xhtml"));
        pDefault.add(new BasicNameValuePair("whatsin", "math"));
        pDefault.add(new BasicNameValuePair("whatsout", "math"));
        pDefault.add(new BasicNameValuePair("cmml", ""));
        pDefault.add(new BasicNameValuePair("nodefaultresources", ""));
        pDefault.add(new BasicNameValuePair("preload", "LaTeX.pool"));
        pDefault.add(new BasicNameValuePair("preload", "article.cls"));
        pDefault.add(new BasicNameValuePair("preload", "amsmath.sty"));
        pDefault.add(new BasicNameValuePair("preload", "amsthm.sty"));
        pDefault.add(new BasicNameValuePair("preload", "amstext.sty"));
        pDefault.add(new BasicNameValuePair("preload", "amssymb.sty"));
        pDefault.add(new BasicNameValuePair("preload", "eucal.sty"));
        pDefault.add(new BasicNameValuePair("preload", "[dvipsnames]xcolor.sty"));
        pDefault.add(new BasicNameValuePair("preload", "url.sty"));
        pDefault.add(new BasicNameValuePair("preload", "hyperref.sty"));
        pDefault.add(new BasicNameValuePair("preload", "mws.sty"));
        pDefault.add(new BasicNameValuePair("preload", "texvc"));
        return pDefault;
    }

    void setParams(List<NameValuePair> params) {
        this.params = params;
        lateXMLConfig.setParams(updateParamFormat(params));
    }

    private Map<String, Object> updateParamFormat(List<NameValuePair> params) {
        HashMap<String, Object> map = new HashMap<>();
        for (NameValuePair pair : params) {
            if (map.containsKey(pair.getName())) {
                Object oSetting = map.get(pair.getName());
                if (oSetting instanceof List) {
                    ((List<String>) oSetting).add(pair.getValue());
                } else {
                    assert (oSetting instanceof String);
                    map.put(pair.getName(), Lists.newArrayList(pair.getValue(), oSetting));
                }
            } else {
                map.put(pair.getName(), pair.getValue());
            }
        }
        return map;
    }

    String getLaTeXMLURL() {
        return lateXMLConfig.getUrl();
    }

    Map getOb() {
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("status_code", serviceResponse.getStatusCode());
        hashMap.put("status", serviceResponse.getStatus());
        hashMap.put("log", serviceResponse.getLog());
        hashMap.put("result", serviceResponse.getResult());
        return hashMap;
    }

    void setLaTeXMLURL(String laTeXMLURL) {
        lateXMLConfig.setUrl(laTeXMLURL);
    }

    String request(String tex) throws IOException, IllegalStateException {
        LaTeXMLConverter converter = new LaTeXMLConverter(lateXMLConfig);
        try {
            serviceResponse = converter.convertLatexmlService(tex);
        } catch (Exception e) {
            throw new IOException("Tex request to MathML conversion server produced failed response.", e);
        }
        if (serviceResponse.getStatusCode() > 1) {
            throw new IOException("Tex request to MathML conversion server produced failed response.",
                    new IOException(serviceResponse.getResult()));
        }
        return serviceResponse.getResult();

    }
}
