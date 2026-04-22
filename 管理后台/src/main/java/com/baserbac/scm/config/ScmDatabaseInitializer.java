package com.baserbac.scm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * SCM数据库初始化器
 * 在应用启动时执行数据库表结构初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScmDatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化SCM数据库表结构...");
        
        // 执行SCM完整表结构初始化
        executeSqlScript("db/scm_full_schema.sql");
        
        log.info("SCM数据库表结构初始化完成");
    }

    /**
     * 执行SQL脚本文件
     */
    private void executeSqlScript(String scriptPath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(scriptPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            if (inputStream == null) {
                log.warn("SQL脚本文件不存在: {}", scriptPath);
                return;
            }
            
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                // 跳过注释和空行
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                
                sqlBuilder.append(line);
                
                // 执行完整的SQL语句
                if (line.endsWith(";")) {
                    String sql = sqlBuilder.toString();
                    try {
                        jdbcTemplate.execute(sql);
                        log.debug("执行SQL: {}", sql);
                    } catch (Exception e) {
                        // 忽略表已存在的错误
                        if (!e.getMessage().contains("already exists")) {
                            log.error("执行SQL失败: {}", sql, e);
                        }
                    }
                    sqlBuilder.setLength(0);
                }
            }
            
        } catch (Exception e) {
            log.error("执行SQL脚本失败: {}", scriptPath, e);
        }
    }

    /**
     * 检查表是否存在
     */
    private boolean tableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查表是否存在失败: {}", tableName, e);
            return false;
        }
    }
}