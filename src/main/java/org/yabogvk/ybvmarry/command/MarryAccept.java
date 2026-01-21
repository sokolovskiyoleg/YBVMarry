package org.yabogvk.ybvmarry.command;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.manager.MarriageManager;
import java.util.UUID;

public class MarryAccept implements SubCommand {
    private final YBVMarry plugin;

    public MarryAccept(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        MarriageManager manager = plugin.getMarriageManager();
        UUID acceptorUUID = player.getUniqueId();

        if (manager.isMarried(acceptorUUID)) {
            player.sendMessage(plugin.getConfigManager().getMessage("already-married"));
            return;
        }

        UUID proposerUUID = manager.getProposer(acceptorUUID);
        if (proposerUUID == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-proposals"));
            return;
        }

        if (manager.isMarried(proposerUUID)) {
            player.sendMessage(plugin.getConfigManager().getMessage("target-already-married"));
            manager.removeProposal(acceptorUUID);
            return;
        }

        manager.acceptProposal(proposerUUID, acceptorUUID);

        Player proposer = Bukkit.getPlayer(proposerUUID);
        String proposerName = (proposer != null) ? proposer.getName() : Bukkit.getOfflinePlayer(proposerUUID).getName();
        if (proposerName == null) proposerName = "Unknown";

        String broadcastMsg = plugin.getConfigManager().getMessage("broadcast-marry", proposerName, player.getName());
        Bukkit.broadcastMessage(broadcastMsg);

        playWeddingEffects(player);
        if (proposer != null && proposer.isOnline()) {
            playWeddingEffects(proposer);
        }
    }

    private void playWeddingEffects(Player player) {
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 15, 0.5, 0.5, 0.5);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
    }
}