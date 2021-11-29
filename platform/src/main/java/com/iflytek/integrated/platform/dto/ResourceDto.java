package com.iflytek.integrated.platform.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 资源下载入参
 */
public class ResourceDto implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String type;

    private String sysId;
    private List<String> sysDriveIds;
    private List<String> sysIntfIds;
    
    private String platformId;
    
    private List<String> ids;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSysId() {
		return sysId;
	}

	public void setSysId(String sysId) {
		this.sysId = sysId;
	}

	public String getPlatformId() {
		return platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public List<String> getSysDriveIds() {
		return sysDriveIds;
	}

	public void setSysDriveIds(List<String> sysDriveIds) {
		this.sysDriveIds = sysDriveIds;
	}

	public List<String> getSysIntfIds() {
		return sysIntfIds;
	}

	public void setSysIntfIds(List<String> sysIntfIds) {
		this.sysIntfIds = sysIntfIds;
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}
	
}

