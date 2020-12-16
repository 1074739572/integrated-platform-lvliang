package com.iflytek.integrated.common.utils;

import java.io.IOException;
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

import org.json.XML;


/**
 * @author xml,json相互转换
 */
public class XmlJsonUtils {

    /**
     * 传入字符串格式的xml
     * 将xml格式<a/>装换成<a></a>
     * 再将xml装换成属性没有带"@"的JSONObject格式
     * @author
     * */
    public static String convertXmlIntoJSONObject (String xml){
        try {
            Document xmlDocument = DocumentHelper.parseText(xml);
            OutputFormat format = new OutputFormat();
            format.setEncoding("UTF-8");
            format.setExpandEmptyElements(true);
            StringWriter out = new StringWriter();
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(xmlDocument);
            writer.flush();
            return XML.toJSONObject(out.toString()).toString();
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
                    JSONObject jsonobject =  jsonArray.getJSONObject(i);
                    jsonToXmlStr(jsonobject,buffer);
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
}

