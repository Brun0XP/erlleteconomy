package dev.leandroerllet.erlleteconomy.service;

import dev.leandroerllet.erlleteconomy.Erlleteconomy;
import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

public class MoneyTopService {


    @Getter
    private static HashMap<String, Double> moneyTop = new HashMap<>();



    public static void load() {
        try {
            Connection db = Erlleteconomy.getDb();
            Statement stmt;
            stmt = db.createStatement();
            stmt.execute("SELECT * FROM economy ORDER BY balance DESC limit 10;");
            ResultSet rs = stmt.getResultSet();
            moneyTop.clear();
            while (rs.next()) {
                moneyTop.put(rs.getString("username"), rs.getDouble("balance"));
            }
            rs.close();
            stmt.close();
        } catch (Exception ignored) {
        }
    }
}
