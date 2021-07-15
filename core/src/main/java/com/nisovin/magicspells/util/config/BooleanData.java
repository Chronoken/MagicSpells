package com.nisovin.magicspells.util.config;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.variables.Variable;

public class BooleanData extends ConfigData<Boolean> {

	private Boolean value;

	private Variable variable;

	public BooleanData(MagicConfig config, String path, Boolean def) {
		if (config.isBoolean(path)) {
			value = config.getBoolean(path, def);
			variable = null;

			return;
		}

		if (config.isString(path)) {
			value = null;
			variable = MagicSpells.getVariableManager().getVariable(config.getString(path, ""));

			return;
		}

		value = def;
		variable = null;
	}

	@Override
	public Boolean get(LivingEntity caster) {
		if (value != null) return value;

		if (variable != null && caster instanceof Player player)
			return Boolean.parseBoolean(variable.getStringValue(player));

		return false;
	}

	@Override
	public boolean isValid() {
		return value != null && variable != null;
	}

}
