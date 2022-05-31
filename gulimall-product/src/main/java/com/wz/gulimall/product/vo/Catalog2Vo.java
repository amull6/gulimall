package com.wz.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class Catalog2Vo {
    private String catalog1Id;
    private List<catalog3Vo> catalog3List;
    private String id;
    private String name;

    @Data
    @AllArgsConstructor
    public static class catalog3Vo {
        private String catalog2Id;
        private String id;
        private String name;
    }
}
