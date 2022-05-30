package com.wz.gulimall.product.web;

import com.wz.gulimall.product.entity.CategoryEntity;
import com.wz.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @RequestMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        List<CategoryEntity> categories = categoryService.getLevel1Categories();
        model.addAttribute("categories", categories);
        return "index";
    }

//    @RequestMapping("/index/json/catalog.json")
//    public

}
