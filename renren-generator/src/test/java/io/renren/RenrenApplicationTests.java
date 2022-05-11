package io.renren;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RenrenApplicationTests {

	@Test
	public void contextLoads() {
		String chinese =  "[\u4e00-\u9fa5]";
		Pattern pattern = Pattern.compile(chinese);
		Matcher matcher = pattern.matcher("你好213");
		System.out.println(matcher.replaceAll(""));
	}

}
