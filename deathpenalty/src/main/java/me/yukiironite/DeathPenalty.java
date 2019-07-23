package me.yukiironite;

import org.bukkit.plugin.java.JavaPlugin;

public class DeathPenalty extends JavaPlugin {
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		getServer().getPluginManager().registerEvents(new DeathListener(this), this);
	}

}
