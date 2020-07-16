package dev.leandroerllet.erlleteconomy.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import de.themoep.minedown.MineDown;
import dev.leandroerllet.erlleteconomy.Erlleteconomy;
import dev.leandroerllet.erlleteconomy.util.StringUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@CommandAlias("economy|money")
public class EconomyCommand extends BaseCommand {


    @Default
    @Description("see your money or someone else's")
    @Subcommand("see")
    @CommandCompletion("@players @nothing")
    public static void seeMoney(Player player, @Optional String target) {
        if (target == null) {
            String money = StringUtils.moneyFormat(Erlleteconomy.getEcon().getBalance(player));
            player.spigot().sendMessage(MineDown.parse(String.format("&2Your money: %s"
                    , money)));
            return;
        }
        String money = StringUtils.moneyFormat(Erlleteconomy.getEcon().getBalance(target));
        player.spigot().sendMessage(MineDown.parse(String.format("&2%s's money: %s", target, money)));
    }

    @Description("give money to a player")
    @Subcommand("give")
    @CommandPermission("money.give")
    @CommandCompletion("@players @nothing")
    public static void giveMoney(Player player, String target, Integer value) {
        if (value <= 0) {
            player.spigot().sendMessage(MineDown.parse("&cthe value cannot be less than 0"));
            return;
        }
        String money = StringUtils.moneyFormat(value);
        Erlleteconomy.getEcon().depositPlayer(target, value);
        player.spigot().sendMessage(MineDown.parse(String.format("&ethe amount of %s was deposited in %s's account", money, target)));
    }

    @Description("take money of a player")
    @Subcommand("take")
    @CommandPermission("money.take")
    @CommandCompletion("@players @nothing")
    public static void takeMoney(Player player, String target, Integer value) {
        if (value <= 0) {
            player.spigot().sendMessage(MineDown.parse("&cthe value cannot be less than 0"));
            return;
        }
        String money = StringUtils.moneyFormat(value);
        Erlleteconomy.getEcon().withdrawPlayer(target, value);
        player.spigot().sendMessage(MineDown.parse(String.format("&cthe value of %s was taken from %s's account", money, target)));
    }

    @Description("set money of a player")
    @Subcommand("set")
    @CommandPermission("money.set")
    @CommandCompletion("@players @nothing")
    public static void setMoney(Player player, String target, Integer value) {
        if (value < 0) {
            player.spigot().sendMessage(MineDown.parse("&cthe value cannot be less than 0"));
            return;
        }
        String money = StringUtils.moneyFormat(value);
        double has = Erlleteconomy.getEcon().getBalance(target);
        Erlleteconomy.getEcon().withdrawPlayer(target, has);
        Erlleteconomy.getEcon().depositPlayer(target, value);
        player.spigot().sendMessage(MineDown.parse(String.format("&ethe amount of %s was set in %s's account", money, target)));
    }

    @Description("pay money to a player")
    @Subcommand("pay")
    @CommandCompletion("@players @nothing")
    public static void payMoney(Player player, OnlinePlayer onlinePlayer, Integer value) {
        if (value <= 0) {
            player.spigot().sendMessage(MineDown.parse("&cthe value cannot be less than 0!"));
            return;
        }
        Player target = onlinePlayer.getPlayer();
        String money = StringUtils.moneyFormat(value);
        if (!Erlleteconomy.getEcon().has(player, value)) {
            player.spigot().sendMessage(MineDown.parse(String.format("&cyou don't have %s to send to %s!", money, target.getName())));
            return;
        }
        if (target.getName().equalsIgnoreCase(player.getName())) {
            player.spigot().sendMessage(MineDown.parse("&cyou cannot send money to yourself!"));
            return;
        }
        int limit = Erlleteconomy.getInstance().getConfig().getInt("limitwithoutalert");
        if (value <= limit) {
            sendMoney(player, value, target, money);
            return;
        }
        String cmd = ChatActionCommand.createTempCommand(() -> {
            if (!Erlleteconomy.getEcon().has(player, value)) {
                player.spigot().sendMessage(MineDown.parse(String.format("&cyou don't have %s to send to %s!", money, target.getName())));
                return;
            }
            sendMoney(player, value, target, money);
            BaseComponent[] alertMessage = MineDown.parse(String.format("&c[ALERT] %s deposited %s in %s's account", player.getName(), money, target.getName()));
            Bukkit.getOnlinePlayers().forEach(online -> online.spigot().sendMessage(alertMessage));
        }, 5);

        player.spigot().sendMessage(MineDown.parse(String.format("&ayou are about to deposit %s in %s's account, click below to confirm", money, target.getName())));
        player.spigot().sendMessage(MineDown.parse(String.format("[&6[I want to send %s to %s's account]](%s)", money, target.getName(), cmd)));
    }

    private static void sendMoney(Player player, Integer value, Player target, String money) {
        Erlleteconomy.getEcon().withdrawPlayer(player, value);
        Erlleteconomy.getEcon().depositPlayer(target, value);
        player.spigot().sendMessage(MineDown.parse(String.format("&eYou deposited %s in %s's account", money, target.getName())));
        target.spigot().sendMessage(MineDown.parse(String.format("&e%s deposited %s in your account", target.getName(), money)));
    }

    @Description("see The top 10 richest players on the server")
    @Subcommand("top")
    @CommandCompletion("@players @nothing")
    public static void topMoney(Player player) {
        try {
            Connection db = Erlleteconomy.getDb();
            Statement stmt;
            stmt = db.createStatement();
            stmt.execute("SELECT * FROM economy ORDER BY balance DESC limit 10;");
            ResultSet rs = stmt.getResultSet();
            int i = 1;
            player.spigot().sendMessage(MineDown.parse("&eThe top 10 richest players on the server!"));
            while (rs.next()) {
                player.spigot().sendMessage(MineDown.parse(String.format("&e#%d %s - %s", i, rs.getString("username"),
                        StringUtils.moneyFormat(rs.getDouble("balance")
                        ))));
                i++;
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            player.spigot().sendMessage(MineDown.parse("&cFailed to get the top players!"));
        }
    }
}
