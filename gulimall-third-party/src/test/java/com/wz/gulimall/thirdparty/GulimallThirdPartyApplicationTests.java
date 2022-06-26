package com.wz.gulimall.thirdparty;

import com.wz.gulimall.thirdparty.component.SmsComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallThirdPartyApplicationTests {
    @Autowired
    SmsComponent smsComponent;


    @Test
    public void contextLoads() {
    }

    @Test
    public void testSendSms() throws Exception {
        smsComponent.sendSmsCode("15762352663","7781");
    }

}
