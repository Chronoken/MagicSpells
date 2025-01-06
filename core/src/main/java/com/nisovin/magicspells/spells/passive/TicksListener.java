package com.nisovin.magicspells.spells.passive;

import java.util.Set;
import java.util.HashSet;

import org.jetbrains.annotations.NotNull;

import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;
import com.nisovin.magicspells.events.SpellLearnEvent;
import com.nisovin.magicspells.events.SpellForgetEvent;
import com.nisovin.magicspells.spells.passive.util.PassiveListener;

// Trigger argument is required
// Must be an integer.
// The value reflects how often the trigger runs
// Where the value of the trigger variable is x
// The trigger will activate every x ticks
@Name("ticks")
public class TicksListener extends PassiveListener {

	private Ticker ticker;

	@Override
	public void initialize(@NotNull String var) {
		if (var.isEmpty()) return;
		try {
			int interval = Integer.parseInt(var);
			ticker = new Ticker(passiveSpell, interval);
		} catch (NumberFormatException e) {
			// ignored
		}

		for (World world : Bukkit.getWorlds()) {
			for (LivingEntity livingEntity : world.getLivingEntities()) {
				if (!livingEntity.isValid()) continue;
				if (!canTrigger(livingEntity)) continue;
				ticker.add(livingEntity);
			}
		}
	}

	@Override
	public void turnOff() {
		if (ticker != null) ticker.turnOff();
	}

	@OverridePriority
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		if (ticker == null) return;

		for (Entity entity : event.getChunk().getEntities()) {
			if (!(entity instanceof LivingEntity le) || !canTrigger(le)) continue;
			ticker.add(le);
		}
	}

	@OverridePriority
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (ticker == null) return;

		for (Entity entity : event.getChunk().getEntities()) {
			if (!(entity instanceof LivingEntity le) || !canTrigger(le)) continue;
			ticker.remove(le);
		}
	}

	@OverridePriority
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		if (ticker == null) return;

		Entity entity = event.getEntity();
		if (entity instanceof Player || !(entity instanceof LivingEntity le) || !canTrigger(le)) return;

		ticker.add(le);
	}

	@OverridePriority
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (ticker == null) return;

		Player player = event.getPlayer();
		if (!canTrigger(player)) return;

		ticker.add(player);
	}

	@OverridePriority
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		if (ticker == null) return;

		Player player = event.getPlayer();
		if (!canTrigger(player)) return;

		ticker.remove(player);
	}

	@OverridePriority
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		if (ticker == null) return;

		Player player = event.getEntity();
		if (!canTrigger(player)) return;

		ticker.remove(player);
	}

	@OverridePriority
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		if (ticker == null) return;

		Player player = event.getPlayer();
		if (!canTrigger(player)) return;

		ticker.add(player);
	}

	@OverridePriority
	@EventHandler
	public void onLearn(SpellLearnEvent event) {
		if (ticker == null) return;

		Spell spell = event.getSpell();
		if (!(spell instanceof PassiveSpell passive) || !passive.equals(passiveSpell)) return;

		ticker.add(event.getLearner());
	}

	@OverridePriority
	@EventHandler
	public void onForget(SpellForgetEvent event) {
		if (ticker == null) return;

		Spell spell = event.getSpell();
		if (!(spell instanceof PassiveSpell passive) || !passive.equals(passiveSpell)) return;

		ticker.remove(event.getForgetter());
	}

	private static class Ticker implements Runnable {

		private final Set<LivingEntity> entities;

		private final PassiveSpell passiveSpell;

		private final int taskId;
		private final String profilingKey;

		public Ticker(PassiveSpell passiveSpell, int interval) {
			this.passiveSpell = passiveSpell;
			taskId = MagicSpells.scheduleRepeatingTask(this, interval, interval);
			profilingKey = MagicSpells.profilingEnabled() ? "PassiveTick:" + interval : null;
			entities = new HashSet<>();
		}

		public void add(LivingEntity livingEntity) {
			entities.add(livingEntity);
		}

		public void remove(LivingEntity livingEntity) {
			entities.remove(livingEntity);
		}

		@Override
		public void run() {
			long start = System.nanoTime();

			for (LivingEntity entity : new HashSet<>(entities)) {
				if (entity == null || !entity.isValid()) {
					entities.remove(entity);
					continue;
				}
				passiveSpell.activate(entity);
			}

			if (profilingKey != null) MagicSpells.addProfile(profilingKey, System.nanoTime() - start);
		}

		public void turnOff() {
			MagicSpells.cancelTask(taskId);
		}

	}

}
