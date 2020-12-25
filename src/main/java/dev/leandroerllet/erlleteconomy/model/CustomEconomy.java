package dev.leandroerllet.erlleteconomy.model;


import dev.leandroerllet.erlleteconomy.Erlleteconomy;
import dev.leandroerllet.erlleteconomy.util.StringUtils;
import lombok.SneakyThrows;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
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
    public boolean hasAccount(String playerName) {
        if (cache.contains(playerName.toLowerCase())) {
            return true;
        }
        Connection connection = Erlleteconomy.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM economy WHERE username = ?;");
        preparedStatement.setString(1, playerName.toLowerCase());
        ResultSet resultSet = preparedStatement.executeQuery();
        boolean hasAccount = resultSet.next();
        resultSet.close();
        preparedStatement.close();
        if (hasAccount) {
            cache.add(playerName.toLowerCase());
        }
        return hasAccount;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return hasAccount(Objects.requireNonNull(offlinePlayer.getName()));
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String worldName) {
        return hasAccount(offlinePlayer);
    }

    @SneakyThrows
    @Override
    public double getBalance(String playerName) {
        Connection connection = Erlleteconomy.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM economy WHERE username = ?;");
        preparedStatement.setString(1, playerName.toLowerCase());
        ResultSet resultSet = preparedStatement.executeQuery();
        double balance = resultSet.next() ? resultSet.getDouble("balance") : 0.00;
        resultSet.close();
        preparedStatement.close();
        return balance;
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return getBalance(Objects.requireNonNull(offlinePlayer.getName()));
    }

    @Override
    public double getBalance(String playerName, String worldName) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String worldName) {
        return getBalance(Objects.requireNonNull(offlinePlayer.getName()));
    }

    @Override
    public boolean has(String playerName, double balance) {
        return getBalance(playerName) >= balance;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double balance) {
        return has(offlinePlayer.getName(), balance);
    }

    @Override
    public boolean has(String playerName, String worldName, double balance) {
        return has(playerName, balance);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String worldName, double balance) {
        return has(offlinePlayer.getName(), balance);
    }

    @SneakyThrows
    @Override
    public EconomyResponse withdrawPlayer(String playerName, double balance) {
        if (!hasAccount(playerName)) {
            createPlayerAccount(playerName);
        }
        if (!has(playerName, balance)) {
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "");
        }
        Connection connection = Erlleteconomy.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE economy SET balance=balance-? WHERE username = ?;");
        preparedStatement.setDouble(1, balance);
        preparedStatement.setString(2, playerName.toLowerCase());
        preparedStatement.executeUpdate();
        preparedStatement.close();
        return new EconomyResponse(balance, getBalance(playerName) - balance, EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double balance) {
        return withdrawPlayer(offlinePlayer.getName(), balance);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double balance) {
        return withdrawPlayer(playerName, balance);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String worldName, double balance) {
        return withdrawPlayer(offlinePlayer.getName(), balance);
    }

    @SneakyThrows
    @Override
    public EconomyResponse depositPlayer(String playerName, double balance) {
        Connection connection = Erlleteconomy.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE economy SET balance=balance+? WHERE username = ?;");
        preparedStatement.setDouble(1, balance);
        preparedStatement.setString(2, playerName.toLowerCase());
        preparedStatement.executeUpdate();

        preparedStatement.close();
        return new EconomyResponse(balance, getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double balance) {
        return depositPlayer(Objects.requireNonNull(offlinePlayer.getName()), balance);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double balance) {
        return depositPlayer(playerName, balance);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String worldName, double balance) {
        return depositPlayer(Objects.requireNonNull(offlinePlayer.getName()), balance);
    }


    @Override
    public boolean createPlayerAccount(String playerName) {
        if (!hasAccount(playerName)) {
            Optional<Player> optionalPlayer = Optional.ofNullable(Bukkit.getPlayer(playerName));
            optionalPlayer.ifPresent(player -> {
                Connection connection = Erlleteconomy.getConnection();
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO economy (username, balance) VALUES(?, ?);");
                    preparedStatement.setString(1, playerName.toLowerCase());
                    preparedStatement.setDouble(2, 0);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }

            });
        }
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return createPlayerAccount(offlinePlayer.getName());
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String worldName) {
        return createPlayerAccount(offlinePlayer);
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
