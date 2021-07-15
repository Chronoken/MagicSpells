package com.nisovin.magicspells.util.config;

import java.util.regex.Pattern;

import org.bukkit.entity.LivingEntity;

public abstract class ConfigData<T> {

	protected static final Pattern VARIABLE_PATTERN = Pattern.compile("%var:(\\w+)(?::(\\d+))?%", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	public abstract T get(LivingEntity caster);

	public boolean isValid() {
		return true;
	}

}
