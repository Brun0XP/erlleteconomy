package dev.leandroerllet.erlleteconomy.model;


import dev.leandroerllet.erlleteconomy.Erlleteconomy;
import dev.leandroerllet.erlleteconomy.util.StringUtils;
import lombok.SneakyThrows;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CustomEconomy implements Economy {

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "Dollar";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double d) {
        return StringUtils.moneyFormat(d);
    }

    @Override
    public String currencyNamePlural() {
        return "Dollars";
    }

    @Override
    public String currencyNameSingular() {
        return "dollar";
    }

    private final List<String> cache = new ArrayList<>();

    @SneakyThrows
    @Override
    public boolean hasAccount(String nick) {
        if (cache.contains(nick.toLowerCase())) {
            return true;
        }
        Connection db = Erlleteconomy.getDb();
        Statement stmt;
        stmt = db.createStatement();
        stmt.execute(String.format("SELECT * FROM economy WHERE username = '%s'", nick.toLowerCase()));
        ResultSet rs = stmt.getResultSet();
        boolean b = rs.next();
        rs.close();
        stmt.close();
        if (b) {
            cache.add(nick.toLowerCase());
        }
        return b;
    }

    @Override
    public boolean hasAccount(OfflinePlayer op) {
        return hasAccount(Objects.requireNonNull(op.getName()));
    }

    @Override
    public boolean hasAccount(String string, String string1) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer op, String string) {
        return false;
    }

    @SneakyThrows
    @Override
    public double getBalance(String nick) {
        Connection db = Erlleteconomy.getDb();
        Statement stmt;
        stmt = db.createStatement();
        stmt.execute(String.format("SELECT * FROM economy WHERE username = '%s'", nick.toLowerCase()));
        ResultSet rs = stmt.getResultSet();
        double b = rs.next() ? rs.getDouble("balance") : 0.00;
        rs.close();
        stmt.close();
        return b;
    }

    @Override
    public double getBalance(OfflinePlayer op) {
        return getBalance(Objects.requireNonNull(op.getName()));
    }

    @Override
    public double getBalance(String nick, String string1) {
        return getBalance(nick);
    }

    @Override
    public double getBalance(OfflinePlayer op, String string) {
        return getBalance(Objects.requireNonNull(op.getName()));
    }

    @Override
    public boolean has(String nick, double d) {
        return getBalance(nick) >= d;
    }

    @Override
    public boolean has(OfflinePlayer op, double d) {
        return has(op.getName(), d);
    }

    @Override
    public boolean has(String nick, String string1, double quantia) {
        return has(nick, quantia);
    }

    @Override
    public boolean has(OfflinePlayer op, String string, double d) {
        return has(op.getName(), d);
    }

    @SneakyThrows
    @Override
    public EconomyResponse withdrawPlayer(String nick, double d) {
        if (!hasAccount(nick)) {
            createPlayerAccount(nick);
        }
        if (!has(nick, d)) {
            return new EconomyResponse(0, d, EconomyResponse.ResponseType.FAILURE, "");
        }
        Connection db = Erlleteconomy.getDb();
        Statement stmt;
        stmt = db.createStatement();
        stmt.executeUpdate(String.format("UPDATE economy SET balance=balance-%s WHERE username = '%s';", d, nick.toLowerCase()));
        stmt.close();
        return new EconomyResponse(d, getBalance(nick) - d, EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer op, double d) {
        return withdrawPlayer(op.getName(), d);
    }

    @Override
    public EconomyResponse withdrawPlayer(String nick, String string1, double d) {
        return withdrawPlayer(nick, d);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer op, String string, double d) {
        return withdrawPlayer(op.getName(), d);
    }

    @SneakyThrows
    @Override
    public EconomyResponse depositPlayer(String nick, double d) {
        Connection db = Erlleteconomy.getDb();
        Statement stmt;
        stmt = db.createStatement();
        stmt.executeUpdate(String.format("UPDATE economy SET balance=balance+%s WHERE username = '%s';", d, nick.toLowerCase()));
        stmt.close();
        return new EconomyResponse(d, getBalance(nick) + d, EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer op, double d) {
        return depositPlayer(Objects.requireNonNull(op.getName()), d);
    }

    @Override
    public EconomyResponse depositPlayer(String nick, String string1, double d) {
        return depositPlayer(nick, d);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer op, String string, double d) {
        return depositPlayer(Objects.requireNonNull(op.getName()), d);
    }


    @Override
    public boolean createPlayerAccount(String nick) {
        if (!hasAccount(nick)) {
            Optional<Player> optionalPlayer = Optional.ofNullable(Bukkit.getPlayer(nick));
            optionalPlayer.ifPresent(player -> {
                Connection db = Erlleteconomy.getDb();
                Statement stmt;
                try {
                    stmt = db.createStatement();
                    stmt.executeUpdate(String.format("INSERT INTO economy (username, balance) VALUES('%s', %s);", player.getName().toLowerCase(), 0));
                    stmt.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            });
        }
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer op) {
        return createPlayerAccount(op.getName());
    }

    @Override
    public boolean createPlayerAccount(String string, String string1) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer op, String string) {
        return false;
    }

    @Override
    public EconomyResponse createBank(String string, String string1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse createBank(String string, OfflinePlayer op) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse deleteBank(String string) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse bankBalance(String string) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse bankHas(String string, double d) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse bankWithdraw(String string, double d) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse bankDeposit(String string, double d) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse isBankOwner(String string, String string1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse isBankOwner(String string, OfflinePlayer op) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse isBankMember(String string, String string1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse isBankMember(String string, OfflinePlayer op) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public List<String> getBanks() {
        return new ArrayList<>();
    }
}
