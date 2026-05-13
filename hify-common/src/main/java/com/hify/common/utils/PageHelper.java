package com.hify.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hify.common.dto.PageResult;

public class PageHelper {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    public static <T> Page<T> toPage(Integer page, Integer pageSize) {
        int p = (page == null || page < 1) ? 1 : page;
        int ps = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
        if (ps < 1) {
            ps = DEFAULT_PAGE_SIZE;
        }
        if (ps > MAX_PAGE_SIZE) {
            ps = MAX_PAGE_SIZE;
        }
        return new Page<>(p, ps);
    }

    public static <T> PageResult<T> toPageResult(IPage<T> iPage) {
        return PageResult.of(iPage.getRecords(), iPage.getTotal(),
            (int) iPage.getCurrent(), (int) iPage.getSize());
    }
}