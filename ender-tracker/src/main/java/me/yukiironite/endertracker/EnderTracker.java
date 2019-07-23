package me.yukiironite.endertracker;

import java.util.ArrayList;

import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.plugin.PluginManager;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;

public class EnderTracker extends JavaPlugin implements Listener {
	private ArrayList<EnderTrackIntent> enderTrackIntents;

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		if(!event.getEntityType().equals(EntityType.ENDER_SIGNAL)) {
			return;
		}

		EnderSignal signal = (EnderSignal)event.getEntity();
		Location signalOrigin = signal.getLocation();
		EnderTrackIntent currentTrackIntent = enderTrackIntents.stream()
			.filter(trackIntent ->
				trackIntent.tracker.getWorld().getName().equals(signalOrigin.getWorld().getName())
				&& trackIntent.tracker.getLocation().distanceSquared(signalOrigin) < 1
			)
			.findAny()
			.orElse(null);
		
		if(currentTrackIntent == null) {
			return;
		}

		enderTrackIntents.remove(currentTrackIntent);

		Vector vec = currentTrackIntent.getTrackingVector();
		Location destination = signalOrigin.add(vec);

		signal.setTargetLocation(destination);
		
		getLogger().info(currentTrackIntent.tracker.getName() + " is tracking " + currentTrackIntent.target.getName());
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		// exit if the player is not using an eye of ender
		boolean itemIsNull = event.getItem() == null;
		boolean worldNotNormal = !event.getPlayer().getWorld().getEnvironment().equals(Environment.NORMAL);
		boolean itemNotEnderEye = itemIsNull || !event.getItem().getType().equals(Material.ENDER_EYE);
		if(itemIsNull || worldNotNormal || itemNotEnderEye) {
			return;
		}

		String name = event.getItem().getItemMeta().getDisplayName();

		// exit if the eye of ender does not have a name
		if(name == null || name.isEmpty()) {
			return;
		}

		Player tracker = event.getPlayer();
		Player target = Bukkit.getPlayer(name);
		EnderTrackIntent trackIntent = new EnderTrackIntent(tracker, target);

		if(trackIntent.processErrors(event)) {
			return;
		}

		getLogger().info(tracker.getName() + " is targeting " + target.getName());
		enderTrackIntents.add(trackIntent);
	}

	@Override
	public void onEnable() {
		enderTrackIntents = new ArrayList<>();
		getServer().getPluginManager().registerEvents(this, this);
	}
}
