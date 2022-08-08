package com.wz.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class MyRabbitConfig {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @PostConstruct //在MyRabbitConfig对象创建完成之后执行
    public void initRabbitTemplate() {
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
//                已到达
                System.out.println("confirm:" + correlationData + "--" + b + "--" + s);
            }
        });
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 触发时机：只要消息没有投递给指定的队列，就触发这个失败回调
             * @param message 投递失败的消息详细信息;
             * @param i 回复的状态码;
             * @param s 回复的文本内容;
             * @param s1    当时这个消息发送给哪个交换机
             * @param s2    当时这叫消息用哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int i, String s, String s1, String s2) {
//                错误
                System.out.println("Fail Message["+message+"]==>i["+i+"]==>s["+s+"]==>s1["+s1+"]==>s2["+s2+"]");
            }
        });
    }
}
