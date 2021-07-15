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

public class DoubleData extends ConfigData<Double> {

	private Expression expression;
	private Double value;

	public DoubleData(MagicConfig config, String path, Double def) {
		if (config.isDouble(path)) {
			value = config.getDouble(path, def);
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

	public DoubleData(String equationString, Double value) {
		if (equationString != null) {
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
			this.value = null;

			ValidationResult result = expression.validate(false);
			if (!result.isValid()) {
				MagicSpells.error("Invalid equation '" + equationString + "'.");

				expression = null;
			}

			return;
		}

		expression = null;
		this.value = value;
	}

	@Override
	public Double get(LivingEntity caster) {
		if (value != null) return value;

		if (expression != null && caster instanceof Player player) {
			expression.getVariableNames().forEach(v -> expression.setVariable(v, MagicSpells.getVariableManager().getValue(v, player)));
			return expression.evaluate();
		}

		return 0d;
	}

	@Override
	public boolean isValid() {
		return value != null && expression != null;
	}

}
