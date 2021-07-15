package com.nisovin.magicspells.util.config;

import java.util.regex.Matcher;

import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.MagicConfig;

public class StringData extends ConfigData<String> {

	private String value;

	private boolean replace;

	public StringData(MagicConfig config, String path, String def) {
		value = config.getString(path, def);

		Matcher matcher = VARIABLE_PATTERN.matcher(value);
		if (matcher.find()) replace = true;
	}

	@Override
	public String get(LivingEntity caster) {
		return replace ? MagicSpells.doVariableReplacements(caster, value) : value;
	}

	@Override
	public boolean isValid() {
		return value != null;
	}

}
