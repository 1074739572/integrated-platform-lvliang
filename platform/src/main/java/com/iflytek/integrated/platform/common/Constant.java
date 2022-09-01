package com.iflytek.integrated.platform.common;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysConfig.qTSysConfig;
import static com.iflytek.integrated.platform.entity.QTSysDriveLink.qTSysDriveLink;

import com.querydsl.core.types.dsl.StringPath;

/**
 * @author czzhan 公共部分
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

	/**
	 * 出入参：1，入参 2、出参
	 */
	public interface ParmInOut {

		public static String IN = "1";

		public static String OUT = "2";
	}

	/**
	 * 操作标识
	 */
	public interface Operation {
		/**
		 * 新增
		 */
		String ADD = "1";
		/**
		 * 编辑
		 */
		String UPDATE = "2";

		/**
		 * 当前
		 */
		String CURRENT = "1";
		/**
		 * 非当前
		 */
		String NOTCURRENT = "2";
	}

	/**
	 * 分类类型
	 */
	public interface CACHE_KEY_PREFIX {
		/**
		 * 驱动
		 */
		public static String DRIVERS = "IntegratedPlatform:Configs:WS:drivers:";
		public static Integer DRIVERS_TYPE = 1;
		/**
		 * schema
		 */
		public static String SCHEMA = "IntegratedPlatform:Configs:WS:schema:";
		public static Integer SCHEMA_TYPE = 2;
		/**
		 * 认证
		 */
		public static String AUTHENTICATION = "IntegratedPlatform:Configs:authentication:";
		public static Integer AUTHENTICATION_TYPE = 3;

		/**
		 * 告警
		 */
		public static String ALERT = "IntegratedPlatform:Configs:alert";
		public static Integer ALERT_TYPE = 4;
		/**
		 * 通用
		 */
		public static String COMMON = "IntegratedPlatform:Configs:";
		public static Integer COMMON_TYPE = 5;

		/**
		 * mq
		 */
		public static String MQ = "IntegratedPlatform:Configs:asyncresponse:";
		public static Integer MQ_TYPE = 6;
	}

	/**
	 * 分类类型
	 */
	public interface TypeStatus {
		/**
		 * 接口分类
		 */
		public static Integer INTERFACE = 1;
		/**
		 * 驱动分类
		 */
		public static Integer DRIVE = 2;
		/**
		 * 插件分类
		 */
		public static Integer PLUGIN = 3;
	}

	/**
	 * 参数格式类型枚举
	 */
	public enum ParamFormatType {

		// 无格式
		NONE("1", "无"),

		// xml格式
		XML("2", "xml"),

		// json格式
		JSON("3", "json");

		private String code;
		private String type;

		ParamFormatType(String code, String type) {
			this.code = code;
			this.type = type;
		}

		public String getCode() {
			return code;
		}

		public String getType() {
			return type;
		}

		public static String getByType(String code) {
			for (ParamFormatType formatType : values()) {
				if (formatType.getCode().equals(code)) {
					return formatType.getType();
				}
			}
			return null;
		}
	}

	/**
	 * 参数格式类型枚举
	 */
	public enum ConnectionType {

		// webservice
		WEBSERVICE("1", "webservice"),

		// http
		HTTP("2", "http"),

		// 数据库视图
		VIEW("3", "数据库视图");

		private String code;
		private String type;

		ConnectionType(String code, String type) {
			this.code = code;
			this.type = type;
		}

		public String getCode() {
			return code;
		}

		public String getType() {
			return type;
		}

		public static String getByType(String code) {
			for (ConnectionType formatType : values()) {
				if (formatType.getCode().equals(code)) {
					return formatType.getType();
				}
			}
			return null;
		}
	}

	/**
	 * redis缓存匹配枚举
	 */
	public enum RedisKeyEnum {
		/**
		 * DRIVE
		 */
		DRIVE("drive", qTSysDriveLink.driveId),

		PLUGIN("plugin", qTBusinessInterface.pluginId),

		SYSCONFIG("sysconfig", qTSysConfig.sysId),

		INTERFACE("interface", qTInterface.id),

		SYS("sys", qTSys.id),

		HOSPITAL("hospital", qTHospital.id),

		PROJECT("project", qTProject.id),

		PLATFORM("platform", qTPlatform.id),

		BUSINESS_INTERFACE("businessInterface", qTBusinessInterface.id);

		private String key;
		private StringPath id;

		RedisKeyEnum(String key, StringPath id) {
			this.id = id;
			this.key = key;
		}

		public String getKey() {
			return key;
		}

		public StringPath getId() {
			return id;
		}

		public static StringPath idByKey(String key) {
			for (RedisKeyEnum redisKeyEnum : values()) {
				if (redisKeyEnum.getKey().equals(key)) {
					return redisKeyEnum.getId();
				}
			}
			return null;
		}
	}

}
