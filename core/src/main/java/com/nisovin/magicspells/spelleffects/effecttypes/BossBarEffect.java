package com.nisovin.magicspells.spelleffects.effecttypes;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.variables.Variable;
import com.nisovin.magicspells.spelleffects.SpellEffect;
import com.nisovin.magicspells.util.managers.BossBarManager.Bar;

public class BossBarEffect extends SpellEffect {

	private String namespaceKey;
	private String title;
	private String color;
	private String style;

	private String strVar;
	private Variable variable;
	private String maxVar;
	private Variable maxVariable;
	private double maxValue;

	private BarColor barColor;
	private BarStyle barStyle;

	private int duration;
	private double progress;

	private boolean remove;
	private boolean visible;
	private boolean broadcast;

	@Override
	protected void loadFromConfig(ConfigurationSection config) {
		namespaceKey = config.getString("namespace-key");
		if (namespaceKey != null && !MagicSpells.getBossBarManager().isNamespaceKey(namespaceKey)) {
			MagicSpells.error("Wrong namespace-key defined! '" + namespaceKey + "'");
		}

		broadcast = config.getBoolean("broadcast", false);

		remove = config.getBoolean("remove", false);
		if (remove) return;

		title = config.getString("title", "");
		color = config.getString("color", "red");
		style = config.getString("style", "solid");
		strVar = config.getString("variable", "");
		maxValue = config.getDouble("max-value", 100);
		maxVar = config.getString("max-variable", "");
		visible = config.getBoolean("visible", true);

		try {
			barColor = BarColor.valueOf(color.toUpperCase());
		} catch (IllegalArgumentException ignored) {
			barColor = BarColor.WHITE;
			MagicSpells.error("Wrong bar color defined! '" + color + "'");
		}

		try {
			barStyle = BarStyle.valueOf(style.toUpperCase());
		} catch (IllegalArgumentException ignored) {
			barStyle = BarStyle.SOLID;
			MagicSpells.error("Wrong bar style defined! '" + style + "'");
		}

		duration = config.getInt("duration", 60);
		progress = config.getDouble("progress", 1);
		if (progress > 1) progress = 1;
		if (progress < 0) progress = 0;
	}

	@Override
	public void initializeModifiers(Spell spell) {
		super.initializeModifiers(spell);

		if (remove) return;

		variable = MagicSpells.getVariableManager().getVariable(strVar);
		if (variable == null && !strVar.isEmpty()) {
			MagicSpells.error("Wrong variable defined! '" + strVar + "'");
		}

		maxVariable = MagicSpells.getVariableManager().getVariable(maxVar);
		if (maxVariable == null && !maxVar.isEmpty()) {
			MagicSpells.error("Wrong variable defined! '" + maxVar + "'");
		}
	}

	@Override
	protected Runnable playEffectEntity(Entity entity) {
		if (!remove && (barStyle == null || barColor == null)) return null;
		if (broadcast) Util.forEachPlayerOnline(this::createBar);
		else if (entity instanceof Player) createBar((Player) entity);
		return null;
	}

	private void createBar(Player player) {
		Bar bar = MagicSpells.getBossBarManager().getBar(player, namespaceKey, !remove);
		if (remove) {
			if (bar != null) bar.remove();
			return;
		}

		double progress = this.progress;
		if (variable != null) {
			progress = variable.getValue(player) / (maxVariable == null ? maxValue : maxVariable.getValue(player));

			if (progress < 0d) progress = 0d;
			if (progress > 1d) progress = 1d;
		}

		String title = Util.doVarReplacementAndColorize(player, this.title);
		bar.set(title, progress, barStyle, barColor, visible);

		if (duration > 0) MagicSpells.scheduleDelayedTask(bar::remove, duration);
	}

}
