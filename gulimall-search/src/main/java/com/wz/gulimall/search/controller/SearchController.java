package com.wz.gulimall.search.controller;

import com.wz.gulimall.search.service.MallSearchSerice;
import com.wz.gulimall.search.vo.SearchParam;
import com.wz.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchController {
    @Autowired
    MallSearchSerice mallSearchSerice;
    @RequestMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model) {
        SearchResult searchResult = mallSearchSerice.search(searchParam);
        model.addAttribute("result", searchResult);
        return "list";
    }
}
