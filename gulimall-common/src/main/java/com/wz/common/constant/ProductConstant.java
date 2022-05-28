package com.wz.common.constant;

public class ProductConstant {
    public enum AttrType{
        ATTR_TYPE_BASE(1,"基本属性"),
        ATTR_TYPE_SALE(0,"销售属性");
        private Integer code;
        private String msg;

        AttrType(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
    public enum PublishStatus{
        CREATED(0,"新建"),
        UP(1,"已上架"),
        DOWN(2,"已下架");
        private Integer code;
        private String msg;

        PublishStatus(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
