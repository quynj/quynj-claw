package com.github.quynj.agentconsole.common;

import java.util.List;

public class PageResult<T> {
    public List<T> items;
    public long total;
    public int page;
    public int pageSize;

    public PageResult(List<T> items, long total, int page, int pageSize) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }
}
