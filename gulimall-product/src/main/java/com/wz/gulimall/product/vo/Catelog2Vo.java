package com.wz.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class Catelog2Vo {
    private String catelog1Id;
    private List<Catelog3Vo> catelog3List;
    private String id;
    private String name;

    public static class Catelog3Vo{
        private String catelog2Id;
        private String id;
        private String name;
    }
}
