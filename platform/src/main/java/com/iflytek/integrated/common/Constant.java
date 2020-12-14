package com.iflytek.integrated.common;

/**
 * @author czzhan
 * 公共部分
 */
public class Constant {

    /**
     * 状态编码
     */
    public interface Status {
        public static final String YES = "1";

        public static final String NO = "0";
        /**
         * 启用
         */
        public static final String START = "1";
        /**
         * 停用
         */
        public static final String STOP = "2";
    }

    /**
     * 00
     */
    public static final String NN_CODE = "00";

    /**
     * 模糊查询
     */
    public interface Fuzzy {
        /**
         * 模糊查询
         */
        public static final String FUZZY_SEARCH = "%%%s%%";

        /**
         * 右模糊查询
         */
        public static final String RIGHT_FUZZY_SEARCH = "%s%%";
    }


    /**
     * 接口返回状态Code
     */
    public interface ResultCode {
        /**
         * 返回成功
         */
        public static final int SUCCESS_CODE = 200;
        /**
         * 返回失败
         */
        public static final int ERROR_CODE = 500;

    }

    /**
     * 有效状态
     */
    public interface IsValid {
        /**
         * 有效
         */
        public static final String ON = "1";

        /**
         * 无效
         */
        public static final String OFF = "2";
    }

    /**
     * 管理编码前缀
     */
    public interface AppCode {

        /**
         * 产品管理
         */
        public static final String PRODUCT = "PR_";
        /**
         * 功能管理
         */
        public static final String FUNCTION = "FU_";

        /**
         * 项目
         */
        public static final String PROJECT = "PJ";
        /**
         * 平台
         */
        public static final String PLATFORM = "PF";
        /**
         * 厂商
         */
        public static final String VENDOR = "V";

    }

    /**
     * 区域层级
     */
    public interface Area {
        /**
         * 省级
         */
        public static final Integer PROVINCE = 1;

        /**
         * 市级
         */
        public static final Integer CITY = 2;

        /**
         * 区（县）级
         */
        public static final Integer COUNTY = 3;
    }
}
