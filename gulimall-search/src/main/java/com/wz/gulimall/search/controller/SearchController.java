package com.wz.gulimall.search.controller;

import com.wz.gulimall.search.service.MallSearchSerice;
import com.wz.gulimall.search.vo.SearchParam;
import com.wz.gulimall.search.vo.SearchResult;
import jdk.nashorn.internal.ir.RuntimeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {
    @Autowired
    MallSearchSerice mallSearchSerice;

    @RequestMapping("/list.html")
    public String listPage(HttpServletRequest httpRequest, SearchParam searchParam, Model model) {
        searchParam.set_queryString(httpRequest.getQueryString());
        SearchResult searchResult = mallSearchSerice.search(searchParam);
        model.addAttribute("result", searchResult);
        return "list";
    }
}
