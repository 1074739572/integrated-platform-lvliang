package com.iflytek.integrated.platform.common;

import com.iflytek.integrated.common.utils.PinYinUtil;
import com.iflytek.integrated.common.utils.SensitiveUtils;
import com.iflytek.integrated.common.utils.ase.AesUtil;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BaseService，包含公共方法
 * @author czzhan
 * @version 1.0
 * @date 2021/1/27 11:06
 */
public class BaseService <E, I extends Comparable, ID extends ComparableExpressionBase<I> & Path<I> >{

    private ID qId;

    private RelationalPathBase<E> qEntity;

    @Autowired
    protected SQLQueryFactory sqlQueryFactory;

    public BaseService(RelationalPathBase<E> qEntity, ID qId) {
        this.qEntity = qEntity;
        this.qId = qId;
    }

    public E getOne(I id) {
        return (this.sqlQueryFactory.selectFrom(this.qEntity).where(this.qId.eq(id))).fetchOne();
    }

    public long delete(I id) {
        return (this.sqlQueryFactory.delete(this.qEntity).where(this.qId.eq(id))).execute();
    }

    public long post(E entity) {
        return this.sqlQueryFactory.insert(this.qEntity).populate(entity).execute();
    }

    public long put(I id, E entity) {
        return ((this.sqlQueryFactory.update(this.qEntity).populate(entity)).where(this.qId.eq(id))).execute();
    }

    /**
     * 生成编码
     * @param codePath  编码Path
     * @param name      名称
     * @return          code
     */
    protected String generateCode(StringPath codePath, RelationalPath<?> path, String name){
        //校验类型是否为空
        name = name.replaceAll("_","");
        if(StringUtils.isEmpty(name)){
            throw new RuntimeException("名称不能为空");
        }
        //名称中中文转拼音首字母小写
        String code = PinYinUtil.getFirstSpell(name);
        //查询编码已存在次数，递增
        String maxCode = sqlQueryFactory.select(codePath).from(path).where(codePath.eq(code)
                .or(codePath.like(PlatformUtil.rightCreateFuzzyText(code + "_")))).orderBy(codePath.desc()).fetchFirst();
        if(StringUtils.isNotBlank(maxCode)){
            String max = maxCode.contains("_") ? maxCode.substring(maxCode.indexOf("_")+1) : "0";
            int count = Integer.parseInt(max) + 1;
            code += "_" + count;
        }
        return code;
    }


    /**
     * 先解密，再脱敏处理
     *
     * @param aes
     * @return
     */
    public String decryptAndFilterSensitive(String aes) {
        try {
            return SensitiveUtils.filterSensitive(AesUtil.decrypt(aes));
        } catch (Exception e) {
            return "";
        }
    }
}
