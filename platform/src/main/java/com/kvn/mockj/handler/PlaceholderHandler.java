package com.kvn.mockj.handler;

import com.kvn.mockj.Function;
import com.kvn.mockj.Options;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author
 */
public class PlaceholderHandler {
    private static final Pattern pattern = Pattern.compile("(@\\w+)(\\(.*?\\))?");

    public static Object doGenerate(Options options) {

        String template = options.getTemplate().toString();
        String methodName = "";
        String paramStr = "";
        String paramName = "";

        Matcher matcher = pattern.matcher(options.getTemplate().toString());
        StringBuffer sbRtn = new StringBuffer();
        boolean hasFind = false;
        while (matcher.find()) {
            hasFind = true;
            int groupCount = matcher.groupCount();
            if (groupCount == 1) {
                methodName = matcher.group(1);
            } else if (groupCount == 2) {
                methodName = matcher.group(1);
                String group2 = matcher.group(2);

                //截取（xxx）之后的内容
                paramName = StringUtils.isNotBlank(group2)?
                        template.substring(template.indexOf(group2)+group2.length()):template;

                paramStr = group2 == null ? null : group2.substring(1, group2.length() - 1);
            }
            try {
                //如果没有参数，截取@xxx之后的内容
                if(StringUtils.isEmpty(paramStr)){
                    paramName = paramName.substring(template.indexOf(methodName) + methodName.length());
                }
                Object invokeRlt = Function.class.getMethod("$" + methodName.substring(1), String.class).invoke(null, paramStr);
                matcher.appendReplacement(sbRtn, invokeRlt.toString() + paramName);
            } catch (NoSuchMethodException e) {
                //如果没有找到@开头的自定义方法，返回方法名
                matcher.appendReplacement(sbRtn, methodName.substring(1));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return hasFind ? sbRtn.toString() : options.getTemplate();
    }

}
