package com.nisovin.magicspells.util.config;

import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import de.slikey.exp4j.Expression;
import de.slikey.exp4j.ValidationResult;
import de.slikey.exp4j.ExpressionBuilder;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.MagicConfig;

public class FloatData extends ConfigData<Float> {

	private Expression expression;
	private Float value;

	public FloatData(MagicConfig config, String path, Float def) {
		if (config.isDouble(path)) {
			value = (float) config.getDouble(path, def);
			expression = null;

			return;
		}

		if (config.isString(path)) {
			String equationString = config.getString(path, "");

			if (equationString.isEmpty()) {
				value = def;
				expression = null;

				return;
			}

			Set<String> variables = new HashSet<>();

			Matcher matcher = VARIABLE_PATTERN.matcher(equationString);
			StringBuilder sb = new StringBuilder();
			while (matcher.find()) {
				String variable = matcher.group(1);

				variables.add(variable);
				matcher.appendReplacement(sb, variable);
			}

			expression = new ExpressionBuilder(matcher.appendTail(sb).toString())
				.variables(variables)
				.build();

			ValidationResult result = expression.validate(false);
			if (!result.isValid()) {
				MagicSpells.error("Invalid equation '" + equationString + "'.");

				value = def;
				expression = null;
			}

			return;
		}

		value = def;
		expression = null;
	}

	@Override
	public Float get(LivingEntity caster) {
		if (value != null) return value;

		if (expression != null && caster instanceof Player player) {
			expression.getVariableNames().forEach(v -> expression.setVariable(v, MagicSpells.getVariableManager().getValue(v, player)));
			return (float) expression.evaluate();
		}

		return 0f;
	}

}
