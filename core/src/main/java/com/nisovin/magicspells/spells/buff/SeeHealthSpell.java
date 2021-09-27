package com.nisovin.magicspells.spells.buff;

import java.util.Set;
import java.util.UUID;
import java.util.Random;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class SeeHealthSpell extends BuffSpell {

	private final static String COLORS = "01234567890abcdef";

	private final Set<UUID> entities;

	private final Random random = ThreadLocalRandom.current();

	private int barSize;
	private int interval;

	private String symbol;

	private Updater updater;

	public SeeHealthSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		barSize = getConfigInt("bar-size", 20);
		interval = getConfigInt("update-interval", 5);
		symbol = getConfigString("symbol", "=");

		entities = new HashSet<>();
	}

	@Override
	public boolean castBuff(LivingEntity entity, float power, String[] args) {
		if (!(entity instanceof Player)) return true;
		entities.add(entity.getUniqueId());
		updater = new Updater();
		return true;
	}

	@Override
	public boolean isActive(LivingEntity entity) {
		return entities.contains(entity.getUniqueId());
	}

	@Override
	public void turnOffBuff(LivingEntity entity) {
		entities.remove(entity.getUniqueId());

		if (updater != null && entities.isEmpty()) {
			updater.stop();
			updater = null;
		}
	}

	@Override
	protected void turnOff() {
		for (UUID id : entities) {
			Player player = Bukkit.getPlayer(id);
			if (player == null) continue;
			if (!player.isValid()) continue;
			player.updateInventory();
		}

		entities.clear();
		if (updater != null) {
			updater.stop();
			updater = null;
		}
	}

	private ChatColor getRandomColor() {
		return ChatColor.getByChar(COLORS.charAt(random.nextInt(COLORS.length())));
	}
	
	private void showHealthBar(Player player, LivingEntity entity) {
		double pct = entity.getHealth() / Util.getMaxHealth(entity);

		ChatColor color = ChatColor.GREEN;
		if (pct <= 0.2) color = ChatColor.DARK_RED;
		else if (pct <= 0.4) color = ChatColor.RED;
		else if (pct <= 0.6) color = ChatColor.GOLD;
		else if (pct <= 0.8) color = ChatColor.YELLOW;

		StringBuilder sb = new StringBuilder(barSize);
		sb.append(getRandomColor().toString());
		int remain = (int) Math.round(barSize * pct);
		sb.append(color);
		sb.append(symbol.repeat(remain));

		if (remain < barSize) {
			sb.append(ChatColor.DARK_GRAY);
			sb.append(symbol.repeat(barSize - remain));
		}

		player.sendActionBar(Util.getMiniMessage(sb.toString()));
	}

	public static String getColors() {
		return COLORS;
	}

	public Set<UUID> getEntities() {
		return entities;
	}

	public int getBarSize() {
		return barSize;
	}

	public void setBarSize(int barSize) {
		this.barSize = barSize;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	private class Updater implements Runnable {

		private final int taskId;

		private Updater() {
			taskId = MagicSpells.scheduleRepeatingTask(this, 0, interval);
		}
		
		@Override
		public void run() {
			for (UUID id : entities) {
				Player player = Bukkit.getPlayer(id);
				if (player == null) continue;
				if (!player.isValid()) continue;
				TargetInfo<LivingEntity> target = getTargetedEntity(player, 1F);
				if (target != null) showHealthBar(player, target.getTarget());
			}
		}
		
		public void stop() {
			MagicSpells.cancelTask(taskId);
		}
		
	}

}
