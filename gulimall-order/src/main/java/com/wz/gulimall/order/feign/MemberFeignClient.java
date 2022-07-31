package com.wz.gulimall.order.feign;

import com.wz.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("gulimall-member")
public interface MemberFeignClient {
    @RequestMapping("member/memberreceiveaddress/{memberId}/addresses")
    List<MemberAddressVo> getMemberReceiveAddressByMemberId(@PathVariable Long memberId);

    @RequestMapping("member/memberreceiveaddress//address/{id}")
    MemberAddressVo getMemberReceiveAddressById(@PathVariable Long id);
}
