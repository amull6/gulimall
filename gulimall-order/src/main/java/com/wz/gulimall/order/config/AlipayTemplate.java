package com.wz.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.wz.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000121644268";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDDqXgZ5Rk9YrUlTHcgMRzci04K+N13ah3DEocDSofWGSHpK7y3PFXiYgirEvWE1ZVVeMc6HJmapW/3AHC9TIgE+pkI7UEBnayiZbvBF6X9oGeqbLemceUXBy4Ce6/gg2wSirAATXCotTvdxO+Z7b89P7wP+6vt6UAgwGj0/p1TSdil43Hu2VDv5t+EZiJJwraoaS1pZVZ+NAjbONHvAQwfHQSDu7vwplhvaQVC/FI63WTlCu3wmzF9U5TWoZLMejOaw2Uo8W7FQyzvsd0ReYeB5xdXMuCP5KVGEk1cse0CXy26dadl/xKwvLkfnW9Kc7Kb246JFMpZKKNqKHhOWrKLAgMBAAECggEAFLwDxzK4/wFT0vUMdrPdrB1zEsSWq43qQ37WIeonBPA64LvKztws+cWLx51FuWLs5VbcPfND79hySgmY6OaQT37C8ug5iFNONRN01xyLcZorAittNs1BCrSvEoJK/A7RSBG8XvvV4Xr7MiAph/Vi27nCUytHZ/nc2tupA7VrtKSiJ+Kv36vi580c8PS0REhqMmt96tzDTFsYT1kEWJyiaZYuU8vR3Vmlrl3DtxRU8T5X495fNngb66uzejhWjVa4BEAUANRMO7I3r+XVN0S4uj8tWMr1s4Se6V5an7GVrOqUZ+PnEKOh5T23+IohvNrUIxkYP8twJCNNX5NV6iZwcQKBgQDxXJj7RwMFLA2krP/XRaZfzYHIKt1vQ14WuK4X6bAnB79gkmvVxkkqO1jYmjiEBmOUcn4fs4B7Aso0dAZlIodPScihboJ8koLyWFUhZuU0kWK0Ile35euWceo5Cro3mRR/isAmVsUHvluvQdiG9Qgu5y1kgAYVs98oEBWV5s+FYwKBgQDPh1V0+9C+21QPdBJ2echtA8HlNnhXioPLfPyjeblSQ/uOaqc8REIpAWZQ01/f1xikwmGjaXki7pbTSLMcDmKax5hMojRfH3S5Kvi5j78xi2kphSDO2lKdTA5G5ExdZ87QvqUQ1Z/Cch3oIP/8UBdS+mmzT1ztWoQ4Mg2rPg/auQKBgQC2AlsG2kUHyHG9ZgxEPSy6gBHHbbOwbL/uKHR4aexBcpE59RDGk5Gm/DwCk0HdJahAUJqVs1pG6RKsvXX5HDGnc/+M5PYXlLCUqlIAL0TXAG/LYT5+2i5vNBYpz9IfjRutFmfSQYqxTTZ8kg4Dr5HtNL3BVN3BzfORlZeBhB2n+wKBgF2HQJxYYNujPV85kJiUih+Xces7gUDyzDMJQVLrqPnKabTlnDktCiQw3UvP94WKCzE16YscHdwAazkNqqsaUJYxHhJrh+7W3mQVWcNHZYOOsPZlaUPQYStC+6w3d7Mg9bReN0Y/AKFBjPjw5m1wKxLaiIinW9oc0ToCVOa4Ma0BAoGAZWhUq0w2HX6mcIuvjJ4x1WaWh+IjD8hqRUoWfp0GayaNi0fhA5eCC1d/MO41CnALnEBzxsEV3VKfzQdruUo9z+IDAAG6kTBpLDEIxwIWQmSp1gmMNa3dyNhjp7+E6wPOMX6EmTlJe1NnZK+1haKPjbqMMUPkgjThJIwJ0qJKViA=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAi54sbb7c3wx8D9pbqhuAALYTrEN7/SSc0sKW1zIyPfVWNd5axCND6WrD1lRhHJzYTy+qG+SVQxKbVZlzlrRx8QVAoXZ+DogW67GDm+cpRFVxALOq6jdQbS4lT0oTSSl8XHKyVAGCdWZceaU+RNPjFwF15Go/sNnO/dpaWuCgpFnvd6gfZnTPKQb/52Kr+4rdUorJVtTVmHmw3F877rEhm64/NUiBkBRa4nVnzCDx+pCBB4spIfCSOf6G6jhGzcaobYFeoopIPPoaRtRd37x1BjkoNdk+BF7AasB4kk/tBC1bAS5KZQHG0i4qEgSq7XvkaYeB3IVlNlolUTHlSvYZCwIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url = "https://342296y03l.zicp.fun/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
