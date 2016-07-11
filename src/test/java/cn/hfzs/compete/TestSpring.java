package cn.hfzs.compete;

import javax.annotation.Resource;

import org.activiti.engine.ProcessEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
// 整合
@ContextConfiguration(locations = "classpath:testApplicationContext.xml")
// 加载配置
public class TestSpring extends AbstractJUnit4SpringContextTests {
	
	@Resource
	public ProcessEngine processEngine;

	@Autowired
	private ApplicationContext ctx;

	@Test
	public void test() {
		System.out.println(processEngine);
	}
}
