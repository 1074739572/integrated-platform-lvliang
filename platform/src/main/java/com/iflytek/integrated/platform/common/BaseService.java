package com.iflytek.integrated.platform.common;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 基础service，不展示swagger
 * @author czzhan
 * @version 1.0
 * @date 2021/1/27 11:06
 */
@ApiIgnore
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
    public E getOne(@PathVariable I id) {
        return (E) ((SQLQuery)this.sqlQueryFactory.selectFrom(this.qEntity).where(this.qId.eq(id))).fetchOne();
    }
    public long delete(@PathVariable I id) {
        return (this.sqlQueryFactory.delete(this.qEntity).where(this.qId.eq(id))).execute();
    }

    public I post(@RequestBody E entity) {
        return (I) (this.sqlQueryFactory.insert(this.qEntity).populate(entity)).executeWithKey((Path)this.qId);
    }

    public long put(@PathVariable I id, @RequestBody E entity) {
        return ((this.sqlQueryFactory.update(this.qEntity).populate(entity)).where(this.qId.eq(id))).execute();
    }
}
