package me.yukiironite;

import java.util.HashSet;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class DeathListener implements Listener {
  static HashSet<String> penaltyWorlds = new HashSet<>();
  static HashMap<UUID, Long> deadPlayers = new HashMap<>();
  int deathTimeout;
  JavaPlugin plugin;

  DeathListener(JavaPlugin plugin) {
    this.plugin = plugin;
    this.deathTimeout = plugin.getConfig().getInt("deathTimeout");
    plugin.getConfig().getStringList("penaltyWorlds")
      .forEach(world -> penaltyWorlds.add(world));
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    String worldName = event.getEntity().getWorld().getName();

    if(penaltyWorlds.contains(worldName)) {
      BaseComponent[] message = new ComponentBuilder( "Death has you feeling drained.\n" ).color( ChatColor.RED ).bold( true )
        .append("Your strength will return soon... ").color( ChatColor.GOLD ).bold(false)
        .append("("+deathTimeout+"m, 0s)").color(ChatColor.YELLOW).bold(false)
        .create();
      Player deadPlayer = event.getEntity();
      Long startTime = java.lang.System.currentTimeMillis();

      

      PenaltyTimeout timeout = new PenaltyTimeout(deadPlayer.getUniqueId());
      timeout.runTaskLater(this.plugin, deathTimeout * 60 * 20);
      
      deadPlayers.put(deadPlayer.getUniqueId(), startTime);
      deadPlayer.spigot().sendMessage(message);
    }
  }

  @EventHandler
  public void onTeleport(PlayerTeleportEvent event) {
    Player player = event.getPlayer();
    Location dest = event.getTo();
    boolean deadPlayerIsTryingToTeleport =
      deadPlayers.containsKey(player.getUniqueId())
      && penaltyWorlds.contains(dest.getWorld().getName());
    
    if(deadPlayerIsTryingToTeleport) {
      long startTime = deadPlayers.get(player.getUniqueId());
      long currentTime = java.lang.System.currentTimeMillis();
      long remainingMillis = (deathTimeout * 60 * 1000) - (currentTime - startTime);
      long remainingMinutes = remainingMillis / (1000 * 60);
      long remainingSeconds = (remainingMillis % (1000 * 60)) / 1000;

      BaseComponent[] message = new ComponentBuilder("You still feel drained from dying.\n").color( ChatColor.RED ).bold( true )
        .append("Your strength will return soon... ").color( ChatColor.GOLD ).bold(false)
        .append("("+remainingMinutes+"m, "+remainingSeconds+"s)").color(ChatColor.YELLOW).bold(false)
        .create();

      event.setCancelled(true);
      player.spigot().sendMessage(message);
    }
  }

  public class PenaltyTimeout extends BukkitRunnable {
    public UUID playerId;
    PenaltyTimeout(UUID playerId) {
      this.playerId = playerId;
    }
    public void run() {
      BaseComponent[] message = new ComponentBuilder("You feel refreshed.\n").color(ChatColor.GREEN).bold( true )
        .append("You can now return to the overworld!").color( ChatColor.YELLOW ).bold(false)
        .create();

      deadPlayers.remove(playerId);
      Bukkit.getServer().getPlayer(playerId).spigot().sendMessage(message);
    }
  }

}