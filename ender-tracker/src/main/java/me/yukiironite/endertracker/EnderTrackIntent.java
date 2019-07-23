package me.yukiironite.endertracker;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.Material;

public class EnderTrackIntent {
  public Player tracker;
  public Player target;

  public EnderTrackIntent (Player tracker, Player target) {
    this.tracker = tracker;
    this.target = target;
  }

  public Vector getTrackingVector() {
    if(tracker == null || target == null) {
      return new Vector();
    }
    Location fromLocation = tracker.getLocation();
		Location toLocation = target.getLocation();

		double x = toLocation.getX() - fromLocation.getX();
		double z = toLocation.getZ() - fromLocation.getZ();

		Vector vec = new Vector(x, 0, z);
		vec.normalize();
		vec.add(new Vector(0, 0.25, 0));
		vec.normalize();
    vec.multiply(5);

    return vec;
  }

  public boolean isTargetOnline() {
    if(target == null) {
      return false;
    }

    return target.isOnline();
  }

  public boolean isTargetInSameWorld() {
    if(target == null) {
      return false;
    }

    return tracker.getWorld().getName().equals(target.getWorld().getName());
  }

  public boolean isTargetTracker() {
    if(target == null) {
      return false;
    }

    return tracker.getUniqueId().equals(target.getUniqueId());
  }

  public boolean isTargetProtected() {
    if(target == null) {
      return false;
    }

    ItemStack helmet = target.getEquipment().getHelmet();

    return helmet != null && helmet.getType().equals(Material.CARVED_PUMPKIN);
  }

  public boolean processErrors(PlayerInteractEvent event) {
    return !isTargetOnline() && cancelEvent(event, "Your target is not online!")
      || !isTargetInSameWorld() && cancelEvent(event, target.getName() + " is not in the same world!")
      || isTargetTracker() && cancelEvent(event, "You can't target yourself!")
      || isTargetProtected() && cancelEvent(event, target.getName() + " is protected!");    
  }

  private boolean cancelEvent(PlayerInteractEvent event, String message) {
    tracker.sendMessage(message);
    event.setCancelled(true);
    return true;
  }
}