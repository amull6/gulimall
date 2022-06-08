package com.wz.gulimall.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SearchController {
    @RequestMapping("/list.html")
    public String listPage(){
        return "list";
    }
}
