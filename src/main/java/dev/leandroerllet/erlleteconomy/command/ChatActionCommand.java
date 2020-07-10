package dev.leandroerllet.erlleteconomy.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import de.themoep.minedown.MineDown;
import dev.leandroerllet.erlleteconomy.Erlleteconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

@CommandAlias("chataction")
public class ChatActionCommand extends BaseCommand {

    private static HashMap<UUID, Runnable> actions = new HashMap<>();

    public static String createTempCommand(Runnable r, int expires) {
        final UUID uuid = UUID.randomUUID();
        actions.put(uuid, r);
        if (expires > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Erlleteconomy.getInstance(),() -> actions.remove(uuid), 60 * expires);
        }
        return "/chataction " + uuid.toString();
    }

    @Default
    @Description("see your money or someone else's")
    public void chataction(Player player, String uuids) {
        UUID uuid = null;
        try {
            uuid = UUID.fromString(uuids);
        } catch (Exception ex) {
            return;
        }
        if (!actions.containsKey(uuid)) {
            player.spigot().sendMessage(MineDown.parse("&cthe action has already been used"));
            return;
        }
        Runnable r = actions.remove(uuid);
        r.run();
    }

}
