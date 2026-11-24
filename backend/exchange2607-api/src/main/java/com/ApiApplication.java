package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

import com.exchange.constants.Constant;
import com.zengtengpeng.annotation.EnableCache;
import com.zengtengpeng.annotation.EnableMQ;

@SpringBootApplication
@ComponentScan(value = { "com.exchange" }, nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
@EnableCache(value = { Constant.当前项目 + "dictItem", Constant.当前项目 + "dictItems", Constant.当前项目 + "chainNetwork" })
@EnableMQ
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
