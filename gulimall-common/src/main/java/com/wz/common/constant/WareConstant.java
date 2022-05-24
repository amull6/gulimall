package com.wz.common.constant;

/**
 * @Author: 2014015130
 * @Date: 2022/5/24 11:12
 * @Description:
 */
public class WareConstant {
    public enum PurchaseEntityStatus {
        CREATED(0, "新建"),
        ASSIGNED(1, "已分配"),
        RECEIVE(2, "已领取"),
        FINISH(3, "已完成"),
        HASERROR(4, "有异常");

        private Integer code;
        private String msg;

        PurchaseEntityStatus(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    public enum PurchaseDetailStatus {
        //        0新建，1已分配，2正在采购，3已完成，4采购失败]
        CREATED(0, "新建"),
        ASSIGNED(1, "已分配"),
        RECEIVE(2, "正在采购"),
        FINISH(3, "已完成"),
        HASERROR(4, "采购失败");

        private Integer code;
        private String msg;

        PurchaseDetailStatus(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
