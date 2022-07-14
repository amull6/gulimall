package com.wz.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

public class Cast {
    List<CastItem> castItems;
    private Integer countNum;
    private Integer countType;
    private BigDecimal totalAmount;
    private BigDecimal reduce = new BigDecimal("0.00");

    public List<CastItem> getCastItems() {
        return castItems;
    }

    public void setCastItems(List<CastItem> castItems) {
        this.castItems = castItems;
    }

    public Integer getCountNum() {
        Integer count = 0;
        if (castItems != null && castItems.size() > 0) {
            for (CastItem castItem : castItems) {
                count += castItem.getCount();
            }
        }
        return count;
    }

    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }

    public Integer getCountType() {
        int count = 0;
        if (castItems != null && castItems.size() > 0) {
            for (CastItem castItem : castItems) {
                count += 1;
            }
        }
        return count;
    }

    public void setCountType(Integer countType) {
        this.countType = countType;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal bigDecimal = new BigDecimal("0.00");
        if (castItems.size() > 0) {
            for (CastItem castItem : castItems) {
                bigDecimal = bigDecimal.add(castItem.getTotalPrice());
            }
        }
        return bigDecimal.subtract(getReduce());
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
