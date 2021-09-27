package com.nisovin.magicspells.util;

import java.util.regex.Pattern;
import java.util.function.BinaryOperator;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.util.FastMath;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;

public class VariableMod {
	
	public enum VariableOwner {
		CASTER,
		TARGET
	}
	
	public enum Operation {
		
		SET((a, b) -> b),
		ADD(Double::sum),
		MULTIPLY((a, b) -> a * b),
		DIVIDE((a, b) -> a / b),
		MODULO((a, b) -> a % b),
		POWER(FastMath::pow),
		RANDOM((a, b) -> ThreadLocalRandom.current().nextDouble() * b);

		private final BinaryOperator<Double> operator;
		
		Operation(BinaryOperator<Double> operator) {
			this.operator = operator;
		}
		
		public double applyTo(double arg1, double arg2) {
			return operator.apply(arg1, arg2);
		}
		
		static Operation fromPrefix(String s) {
			char c = s.charAt(0);
			return switch (c) {
				case '=' -> SET;
				case '*' -> MULTIPLY;
				case '/' -> DIVIDE;
				case '^' -> POWER;
				case '%' -> MODULO;
				case '?' -> RANDOM;
				default -> ADD;
			};
		}
		
	}

	private static final Pattern OPERATION_MATCHER = Pattern.compile("^[=+*/^%?]");

	private VariableOwner variableOwner = VariableOwner.CASTER;
	private String modifyingVariableName = null;
	private double constantModifier;
	private boolean negate = false;
	private String value;
	private Operation op;

	public VariableMod(String data) {
		op = Operation.fromPrefix(data);
		data = OPERATION_MATCHER.matcher(data).replaceFirst("");
		if (data.startsWith("-")) {
			data = data.substring(1);
			negate = true;
		}

		value = data;

		if (RegexUtil.matches(RegexUtil.DOUBLE_PATTERN, data)) constantModifier = Double.parseDouble(data);
		else {
			// If it isn't a double, then let's match it as a variable
			String varName = data;
			if (data.contains(":")) {
				// Then there is an explicit statement of who's variable it is
				String[] dataSplits = data.split(":");
				if (dataSplits[0].equalsIgnoreCase("target")) variableOwner = VariableOwner.TARGET;
				varName = dataSplits[1];
			}
			modifyingVariableName = varName;
		}
	}
	
	public double getValue(Player caster, Player target) {
		int negationFactor = negate ? -1 : 1;
		if (modifyingVariableName != null) {
			Player variableHolder = variableOwner == VariableOwner.CASTER ? caster : target;
			return MagicSpells.getVariableManager().getValue(modifyingVariableName, variableHolder) * negationFactor;
		}
		return constantModifier * negationFactor;
	}
	
	public double getValue(Player caster, Player target, double baseValue) {
		double secondValue = getValue(caster, target);
		return getOperation().applyTo(baseValue, secondValue);
	}

	public String getStringValue(Player caster, Player target) {
		String ret = MagicSpells.doTargetedVariableReplacements(caster, target, value);
		return MagicSpells.doVariableReplacements(caster, ret);
	}

	public String getValue() {
		return value;
	}
	
	public boolean isConstantValue() {
		return modifyingVariableName == null;
	}
	
	public Operation getOperation() {
		return op;
	}
	
	public VariableOwner getVariableOwner() {
		return variableOwner;
	}
}
