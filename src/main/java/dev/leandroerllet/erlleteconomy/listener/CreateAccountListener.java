package dev.leandroerllet.erlleteconomy.listener;

import dev.leandroerllet.erlleteconomy.Erlleteconomy;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class CreateAccountListener implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(Erlleteconomy.getInstance()
                ,() -> Erlleteconomy.getEcon().createPlayerAccount(e.getPlayer()));
    }


}
