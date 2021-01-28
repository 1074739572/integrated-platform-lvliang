package com.iflytek.integrated.platform.common;

import com.iflytek.integrated.common.utils.PinYinUtil;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 基础service，不展示swagger
 * @author czzhan
 * @version 1.0
 * @date 2021/1/27 11:06
 */
public class BaseService <E, I extends Comparable, ID extends ComparableExpressionBase<I> & Path<I> >{

    protected ID qId;
    protected RelationalPathBase<E> qEntity;

    @Autowired
    protected SQLQueryFactory sqlQueryFactory;

    public BaseService(RelationalPathBase<E> qEntity, ID qId) {
        this.qEntity = qEntity;
        this.qId = qId;
    }

    /**
     * 获取一条
     * @param id
     * @return
     */
    public E getOne(I id) {
        return (E) ((SQLQuery)this.sqlQueryFactory.selectFrom(this.qEntity).where(this.qId.eq(id))).fetchOne();
    }
    public long delete(I id) {
        return (this.sqlQueryFactory.delete(this.qEntity).where(this.qId.eq(id))).execute();
    }

    public I post(E entity) {
        return (I) (this.sqlQueryFactory.insert(this.qEntity).populate(entity)).executeWithKey((Path)this.qId);
    }

    public long put(I id, E entity) {
        return ((this.sqlQueryFactory.update(this.qEntity).populate(entity)).where(this.qId.eq(id))).execute();
    }

    /**
     * 生成编码
     * @param codePath
     * @param name
     * @return
     */
    public String generateCode(StringPath codePath, String name){
        //校验类型是否为空
        if(StringUtils.isEmpty(name)){
            throw new RuntimeException("名称不能为空");
        }
        //名称中中文转拼音首字母小写
        String code = PinYinUtil.getFirstSpell(name);
        //查询编码已存在次数，递增
        Long count = sqlQueryFactory.select().from(this.qEntity).where(codePath.eq(code)).fetchCount();
        if(count > 0){
            code += count;
        }
        return code;
    }
}