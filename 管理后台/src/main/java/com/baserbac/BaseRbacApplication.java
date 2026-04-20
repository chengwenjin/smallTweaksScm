package com.baserbac;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 通用后台权限管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.baserbac.mapper")
@EnableAspectJAutoProxy
public class BaseRbacApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaseRbacApplication.class, args);
        System.out.println("========================================");
        System.out.println("   通用后台权限管理系统启动成功！");
        System.out.println("   Knife4j文档: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
