package dev.leandroerllet.erlleteconomy;

import co.aikar.commands.BukkitCommandManager;
import dev.leandroerllet.erlleteconomy.command.ChatActionCommand;
import dev.leandroerllet.erlleteconomy.command.EconomyCommand;
import dev.leandroerllet.erlleteconomy.listener.CreateAccountListener;
import dev.leandroerllet.erlleteconomy.model.CustomEconomy;
import lombok.Getter;
import lombok.SneakyThrows;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Objects;


public final class Erlleteconomy extends JavaPlugin {


    @Getter
    private static Connection db;

    @Getter
    private static Erlleteconomy instance;

    @Getter
    private static Economy econ;

    @SneakyThrows
    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        econ = new CustomEconomy();
        Bukkit.getServicesManager().register(Economy.class, econ,
                Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Vault")), ServicePriority.Highest
        );

        Statement stmt;
        Class.forName("org.postgresql.Driver");
        db = DriverManager
                .getConnection(Objects.requireNonNull(instance.getConfig().getString("database.host"))
                        , instance.getConfig().getString("database.username")
                        , instance.getConfig().getString("database.password"));

        stmt = db.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS economy ( username varchar(16) NOT NULL,  balance int  NOT NULL,  PRIMARY KEY (username))");
        stmt.close();
    }

    @Override
    public void onEnable() {
       Bukkit.getPluginManager().registerEvents(new CreateAccountListener(), this);
        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.registerCommand(new EconomyCommand());
        manager.registerCommand(new ChatActionCommand());
    }

    @SneakyThrows
    @Override
    public void onDisable() {
       db.close();
    }


}
