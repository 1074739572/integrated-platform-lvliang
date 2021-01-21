package com.iflytek.mock.handler;

import com.iflytek.mock.Function;
import com.iflytek.mock.Options;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author
 */
public class PlaceholderHandler {
    private static final Pattern PATTERN = Pattern.compile("(@\\w+)(\\(.*?\\))?");

    public static Object doGenerate(Options options) {

        String template = options.getTemplate().toString();
        String paramName = template;

        Matcher matcher = PATTERN.matcher(options.getTemplate().toString());
        StringBuffer sbRtn = new StringBuffer();
        boolean hasFind = false;
        while (matcher.find()) {

            String methodName = "";
            String paramStr = "";

            hasFind = true;
            int groupCount = matcher.groupCount();
            if (groupCount == 1) {
                methodName = matcher.group(1);
            } else if (groupCount == 2) {
                methodName = StringUtils.isNotBlank(matcher.group(1))?matcher.group(1):"";
                String group2 = StringUtils.isNotBlank(matcher.group(2))?matcher.group(2):"";

                paramStr = group2.length() > 2 ? group2.substring(1, group2.length() - 1) : "";

                String templateStr = methodName + group2;
                //截取 mock 之后的内容
                paramName = paramName.substring(paramName.indexOf(templateStr) + templateStr.length());
            }
            //如果后续还有mock，无需附加单位
            String unit = paramName.indexOf('@') >=0 ? "" : paramName;
            try {
                Object invokeRlt = Function.class.getMethod("$" + methodName.substring(1), String.class).invoke(null, paramStr);

                matcher.appendReplacement(sbRtn, invokeRlt.toString() + unit);
            } catch (NoSuchMethodException e) {
                //如果没有找到@开头的自定义方法，返回原Mock内容（取出@）
                matcher.appendReplacement(sbRtn, methodName.substring(1) + unit);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return hasFind ? sbRtn.toString() : options.getTemplate();
    }

}
