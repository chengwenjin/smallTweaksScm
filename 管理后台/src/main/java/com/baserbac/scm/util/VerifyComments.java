package com.baserbac.scm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class VerifyComments {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/small_tweaks_scm?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   验证SCM表及字段中文注释");
        System.out.println("========================================");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            System.out.println("\n========== 表注释 ==========\n");
            
            String tableSql = "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES " +
                    "WHERE TABLE_SCHEMA = 'small_tweaks_scm' AND TABLE_NAME LIKE 'scm_%' ORDER BY TABLE_NAME";
            
            try (ResultSet rs = stmt.executeQuery(tableSql)) {
                System.out.printf("%-45s | %s\n", "表名", "表注释");
                System.out.println("--------------------------------------------------------------------------------");
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    String tableComment = rs.getString("TABLE_COMMENT");
                    System.out.printf("%-45s | %s\n", tableName, tableComment);
                }
            }

            System.out.println("\n========== 字段注释 ==========\n");
            
            String columnSql = "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_COMMENT FROM information_schema.COLUMNS " +
                    "WHERE TABLE_SCHEMA = 'small_tweaks_scm' AND TABLE_NAME LIKE 'scm_%' ORDER BY TABLE_NAME, ORDINAL_POSITION";
            
            String currentTable = "";
            try (ResultSet rs = stmt.executeQuery(columnSql)) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    String columnName = rs.getString("COLUMN_NAME");
                    String columnComment = rs.getString("COLUMN_COMMENT");
                    
                    if (!tableName.equals(currentTable)) {
                        currentTable = tableName;
                        System.out.println("\n【" + tableName + "】");
                        System.out.printf("  %-25s | %s\n", "字段名", "字段注释");
                        System.out.println("  ---------------------------------------------------------");
                    }
                    System.out.printf("  %-25s | %s\n", columnName, columnComment);
                }
            }

            System.out.println("\n========================================");
            System.out.println("验证完成！如果看到中文注释正常显示，说明修复成功。");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("验证失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
