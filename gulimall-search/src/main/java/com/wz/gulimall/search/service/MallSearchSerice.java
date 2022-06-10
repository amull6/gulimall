package com.wz.gulimall.search.service;

import com.wz.gulimall.search.vo.SearchParam;
import com.wz.gulimall.search.vo.SearchResult;

public interface MallSearchSerice {
    SearchResult search(SearchParam searchParam);
}
