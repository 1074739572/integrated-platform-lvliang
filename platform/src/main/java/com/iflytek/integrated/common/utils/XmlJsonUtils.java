package com.iflytek.integrated.common.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import org.json.JSONException;
import org.json.XML;
import org.json.XMLTokener;


/**
 * @author
 * xml,json相互转换
 */
public class XmlJsonUtils {

    /**
     * 传入字符串格式的xml
     * 将xml格式<a/>装换成<a></a>
     * 再将xml装换成属性没有带"@"的JSONObject格式
     * @author
     * */
    public static String convertXmlToJsonObject(String xml){
        try {
            Document xmlDocument = DocumentHelper.parseText(xml);
            OutputFormat format = new OutputFormat();
            format.setEncoding("UTF-8");
            format.setExpandEmptyElements(true);
            StringWriter out = new StringWriter();
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(xmlDocument);
            writer.flush();
            return toJsonObject(out.toString()).toString();
        } catch (DocumentException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("解析XML失败，格式有错误");
    }

    /**
     * Json to xml string.
     * @param json the json
     * @return the string
     */
    public static String jsonToXml(String json){
        StringBuffer buffer = new StringBuffer();
        JSONObject jObj = JSON.parseObject(json);
        jsonToXmlStr(jObj,buffer);
        return buffer.toString();
    }

    /**
     * Json to xmlStr string.
     * @param jObj   the j obj
     * @param buffer the buffer
     * @return the string
     */
    private static String jsonToXmlStr(JSONObject jObj, StringBuffer buffer ){
        Set<Map.Entry<String, Object>> se = jObj.entrySet();
        for(Iterator<Map.Entry<String, Object>> it = se.iterator(); it.hasNext(); )
        {
            Map.Entry<String, Object> en = it.next();
            if(en.getValue() instanceof JSONObject){
                buffer.append("<"+en.getKey()+">");
                JSONObject jo = jObj.getJSONObject(en.getKey());
                jsonToXmlStr(jo,buffer);
                buffer.append("</"+en.getKey()+">");
            }
            else if(en.getValue() instanceof JSONArray){
                JSONArray jsonArray = jObj.getJSONArray(en.getKey());
                for (int i = 0; i < jsonArray.size(); i++) {
                    buffer.append("<"+en.getKey()+">");
                    if(jsonArray.get(i) instanceof JSONObject){
                        jsonToXmlStr(jsonArray.getJSONObject(i),buffer);
                    }
                    else {
                        buffer.append(jsonArray.get(i));
                    }
                    buffer.append("</"+en.getKey()+">");
                }
            }
            else{
                buffer.append("<"+en.getKey()+">"
                        +en.getValue());
                buffer.append("</"+en.getKey()+">");
            }

        }
        return buffer.toString();
    }

    private static org.json.JSONObject toJsonObject(String string) throws JSONException {
        return toJsonObject(string, false);
    }

    private static org.json.JSONObject toJsonObject(String string, boolean keepStrings) throws JSONException {
        return toJsonObject((Reader)(new StringReader(string)), keepStrings);
    }

    private static org.json.JSONObject toJsonObject(Reader reader, boolean keepStrings) throws JSONException {
        org.json.JSONObject jo = new org.json.JSONObject();
        XMLTokener x = new XMLTokener(reader);

        while(x.more()) {
            x.skipPast("<");
            if (x.more()) {
                parse(x, jo, (String)null, keepStrings);
            }
        }

        return jo;
    }

    private static boolean parse(XMLTokener x, org.json.JSONObject context, String name, boolean keepStrings) throws JSONException {
        org.json.JSONObject jsonobject = null;
        Object token = x.nextToken();
        String string;
        if (token == XML.BANG) {
            char c = x.next();
            if (c == '-') {
                if (x.next() == '-') {
                    x.skipPast("-->");
                    return false;
                }

                x.back();
            } else if (c == '[') {
                token = x.nextToken();
                if ("CDATA".equals(token) && x.next() == '[') {
                    string = x.nextCDATA();
                    if (string.length() > 0) {
                        context.accumulate("_value_", string);
                    }

                    return false;
                }

                throw x.syntaxError("Expected 'CDATA['");
            }

            int i = 1;

            do {
                token = x.nextMeta();
                if (token == null) {
                    throw x.syntaxError("Missing '>' after '<!'.");
                }

                if (token == XML.LT) {
                    ++i;
                } else if (token == XML.GT) {
                    --i;
                }
            } while(i > 0);

            return false;
        } else if (token == XML.QUEST) {
            x.skipPast("?>");
            return false;
        } else if (token == XML.SLASH) {
            token = x.nextToken();
            if (name == null) {
                throw x.syntaxError("Mismatched close tag " + token);
            } else if (!token.equals(name)) {
                throw x.syntaxError("Mismatched " + name + " and " + token);
            } else if (x.nextToken() != XML.GT) {
                throw x.syntaxError("Misshaped close tag");
            } else {
                return true;
            }
        } else if (token instanceof Character) {
            throw x.syntaxError("Misshaped tag");
        } else {
            String tagName = (String)token;
            token = null;
            jsonobject = new org.json.JSONObject();

            while(true) {
                if (token == null) {
                    token = x.nextToken();
                }

                if (!(token instanceof String)) {
                    if (token == XML.SLASH) {
                        if (x.nextToken() != XML.GT) {
                            throw x.syntaxError("Misshaped tag");
                        }

                        if (jsonobject.length() > 0) {
                            context.accumulate(tagName, jsonobject);
                        } else {
                            context.accumulate(tagName, "");
                        }

                        return false;
                    }

                    if (token != XML.GT) {
                        throw x.syntaxError("Misshaped tag");
                    }

                    while(true) {
                        token = x.nextContent();
                        if (token == null) {
                            if (tagName != null) {
                                throw x.syntaxError("Unclosed tag " + tagName);
                            }

                            return false;
                        }

                        if (token instanceof String) {
                            string = (String)token;
                            if (string.length() > 0) {
                                jsonobject.accumulate("_value_", keepStrings ? string : XML.stringToValue(string));
                            }
                        } else if (token == XML.LT && parse(x, jsonobject, tagName, keepStrings)) {
                            if (jsonobject.length() == 0) {
                                context.accumulate(tagName, "");
                            } else if (jsonobject.length() == 1 && jsonobject.opt("_value_") != null) {
                                context.accumulate(tagName, jsonobject.opt("_value_"));
                            } else {
                                context.accumulate(tagName, jsonobject);
                            }

                            return false;
                        }
                    }
                }

                string = (String)token;
                token = x.nextToken();
                if (token == XML.EQ) {
                    token = x.nextToken();
                    if (!(token instanceof String)) {
                        throw x.syntaxError("Missing value");
                    }

                    jsonobject.accumulate(string, keepStrings ? (String)token : XML.stringToValue((String)token));
                    token = null;
                } else {
                    jsonobject.accumulate(string, "");
                }
            }
        }
    }
}