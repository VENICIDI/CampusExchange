package org.campusmarket.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页查询结果
 * @param <T> 数据项类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    
    /**
     * 当前页码
     */
    private Integer current;
    
    /**
     * 每页记录数
     */
    private Integer size;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Long pages;
    
    /**
     * 数据记录
     */
    private List<T> records;
    
    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;
    
    /**
     * 是否有下一页
     */
    private Boolean hasNext;
    
    /**
     * 从MyBatisPlus的IPage转换为PageResult
     */
    public static <T> PageResult<T> fromIPage(com.baomidou.mybatisplus.core.metadata.IPage<T> page) {
        return new PageResult<>(
                (int)page.getCurrent(),
                (int)page.getSize(),
                page.getTotal(),
                page.getPages(),
                page.getRecords(),
                page.getCurrent() > 1,
                page.getCurrent() < page.getPages()
        );
    }
} 