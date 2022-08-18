package com.iflytek.integrated.platform.common;

import com.iflytek.integrated.platform.dto.CacheDeleteDto;
import com.iflytek.integrated.platform.entity.TInterface;
import com.iflytek.integrated.platform.entity.TSys;
import com.iflytek.integrated.platform.service.InterfaceService;
import com.iflytek.integrated.platform.service.SysService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CacheDeleteService {
    @Autowired
    private SysService sysService;

    @Autowired
    private InterfaceService interfaceService;

    @Autowired
    private RedisService redisService;

    /**
     * 缓存只用到系统编码的组装
     *
     * @param dto
     * @return
     */
    private List<String> sysKey(CacheDeleteDto dto) {
        List list = new ArrayList();
        List<String> codes = getSys(dto);

        if (CollectionUtils.isNotEmpty(codes)) {
            for (String code : codes) {
                if (dto.getCacheTypeList().contains(Constant.CACHE_KEY_PREFIX.DRIVERS_TYPE)) {
                    //得到IntegratedPlatform:Configs:WS:drivers:_productcode 的缓存key
                    list.add(Constant.CACHE_KEY_PREFIX.DRIVERS + "_" + code);
                } else if (dto.getCacheTypeList().contains(Constant.CACHE_KEY_PREFIX.AUTHENTICATION_TYPE)) {
                    //得到IntegratedPlatform:Configs:authentication:_productcode 的缓存key
                    list.add(Constant.CACHE_KEY_PREFIX.AUTHENTICATION + "_" + code);
                }
            }

        }
        return list;
    }

    private List<String> getSys(CacheDeleteDto dto) {
        List<String> codes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dto.getSysCodes())) {
            codes = dto.getSysCodes();
        } else if (CollectionUtils.isNotEmpty(dto.getSysIds())) {
            //查询系统编码
            List<TSys> sysList = sysService.getBySysIds(dto.getSysIds());
            if (CollectionUtils.isNotEmpty(sysList)) {
                codes = sysList.stream().filter(e -> StringUtils.isNotEmpty(e.getSysCode())).map(TSys::getSysCode).collect(Collectors.toList());
            }
        } else {
            //查询所有的系统
            List<TSys> all = sysService.getAll();
            if (CollectionUtils.isNotEmpty(all)) {
                codes = all.stream().filter(e -> StringUtils.isNotEmpty(e.getSysCode())).map(TSys::getSysCode).collect(Collectors.toList());
            }
        }
        return codes;
    }

    /**
     * 缓存用到系统和funcode的组装
     *
     * @param dto
     * @return
     */
    private List<String> sysFunKey(CacheDeleteDto dto) {
        List list = new ArrayList();

        //系统编码
        List<String> productCodes = getSys(dto);

        //服务编码编码
        List<String> funCodes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dto.getInterfaceCodes())) {
            funCodes = dto.getInterfaceCodes();
        } else if (CollectionUtils.isNotEmpty(dto.getInterfaceIds())) {
            //查询全部编码
            List<TInterface> sysList = interfaceService.getByIdList(dto.getInterfaceIds());
            if (CollectionUtils.isNotEmpty(sysList)) {
                funCodes = sysList.stream().filter(e -> StringUtils.isNotEmpty(e.getInterfaceUrl()))
                        .map(TInterface::getInterfaceUrl).collect(Collectors.toList());
            }
        } else {
            //查询全部的服务编码
            List<TInterface> sysList = interfaceService.getAll();
            if (CollectionUtils.isNotEmpty(sysList)) {
                funCodes = sysList.stream().filter(e -> StringUtils.isNotEmpty(e.getInterfaceUrl()))
                        .map(TInterface::getInterfaceUrl).collect(Collectors.toList());
            }
        }

        if (CollectionUtils.isNotEmpty(productCodes) || CollectionUtils.isNotEmpty(funCodes)) {
            for (String productCode : productCodes) {
                for (String funcCode : funCodes) {
                    if (dto.getCacheTypeList().contains(Constant.CACHE_KEY_PREFIX.SCHEMA_TYPE)) {
                        //得到IntegratedPlatform:Configs:WS:schema:_funcode_productcode 的缓存key
                        list.add(Constant.CACHE_KEY_PREFIX.SCHEMA + "_" + funcCode + "_" + productCode);
                    } else if (dto.getCacheTypeList().contains(Constant.CACHE_KEY_PREFIX.COMMON_TYPE)) {
                        //得到IntegratedPlatform:Configs:_productcode_funcode 的缓存key
                        list.add(Constant.CACHE_KEY_PREFIX.COMMON + "_" + productCode + "_" + funcCode);
                    }
                }
            }
        }
        return list;
    }

    /**
     * 服务告警的键
     *
     * @return
     */
    private List<String> alertKey() {
        return Arrays.asList(Constant.CACHE_KEY_PREFIX.ALERT);
    }

    public void cacheKeyDelete(CacheDeleteDto dto) {
        List<String> list = new ArrayList<>();
        List<Integer> cacheTypeList = dto.getCacheTypeList();
        //如果是生成只含系统编码的
        if (cacheTypeList.contains(Constant.CACHE_KEY_PREFIX.DRIVERS_TYPE) || cacheTypeList.contains(Constant.CACHE_KEY_PREFIX.AUTHENTICATION_TYPE)
        ) {
            list.addAll(sysKey(dto));
        }

        if (cacheTypeList.contains(Constant.CACHE_KEY_PREFIX.SCHEMA_TYPE) || cacheTypeList.contains(Constant.CACHE_KEY_PREFIX.COMMON_TYPE)) {
            list.addAll(sysFunKey(dto));
        }

        if (cacheTypeList.contains(Constant.CACHE_KEY_PREFIX.ALERT_TYPE)) {
            list.addAll(alertKey());
        }
        //删除缓存
        redisService.delCacheByKeyList(list);
    }

}
