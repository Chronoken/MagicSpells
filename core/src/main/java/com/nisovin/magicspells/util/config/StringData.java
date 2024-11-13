package com.nisovin.magicspells.util.config;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.TxtUtil;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.variables.Variable;
import com.nisovin.magicspells.variables.variabletypes.GlobalVariable;
import com.nisovin.magicspells.variables.variabletypes.GlobalStringVariable;
import com.nisovin.magicspells.variables.variabletypes.PlayerStringVariable;

public class StringData implements ConfigData<String> {

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("""
		%(?:\
		((var|castervar|targetvar):(\\w+)(?::(\\d+))?)|\
		(playervar:([^:]+):(\\w+)(?::(\\d+))?)|\
		(arg:(\\d+):([^%]+))|\
		((papi|casterpapi|targetpapi):([^%]+))|\
		(playerpapi:([^:]+):([^%]+))\
		)%|\
		(%[art])""", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	private final List<ConfigData<String>> values;
	private final List<String> fragments;

	public StringData(String value) {
		List<ConfigData<String>> values = new ArrayList<>();
		List<String> fragments = new ArrayList<>();

		Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
		int end = 0;
		while (matcher.find()) {
			ConfigData<String> data = createData(matcher);
			if (data == null) continue;

			fragments.add(value.substring(end, matcher.start()));
			values.add(data);

			end = matcher.end();
		}

		fragments.add(value.substring(end));

		this.fragments = Collections.unmodifiableList(fragments);
		this.values = Collections.unmodifiableList(values);
	}

	private static ConfigData<String> createData(Matcher matcher) {
		if (matcher.group(1) != null) {
			String owner = matcher.group(2);
			String variable = matcher.group(3);
			String placesString = matcher.group(4);

			int places = -1;
			if (placesString != null) {
				try {
					places = Integer.parseInt(placesString);
				} catch (NumberFormatException e) {
					return null;
				}
			}

			return switch (owner.toLowerCase()) {
				case "castervar" -> new CasterVariableData(matcher.group(), variable, places);
				case "targetvar" -> new TargetVariableData(matcher.group(), variable, places);
				default -> new DefaultVariableData(matcher.group(), variable, places);
			};
		}

		if (matcher.group(5) != null) {
			String player = matcher.group(6);
			String variable = matcher.group(7);
			String placesString = matcher.group(8);

			int places = -1;
			if (placesString != null) {
				try {
					places = Integer.parseInt(placesString);
				} catch (NumberFormatException e) {
					return null;
				}
			}

			return new PlayerVariableData(matcher.group(), variable, player, places);
		}

		if (matcher.group(9) != null) {
			String def = matcher.group(11);

			int index;
			try {
				index = Integer.parseInt(matcher.group(10));
			} catch (NumberFormatException e) {
				return null;
			}
			if (index == 0) return null;

			return new ArgumentData(index - 1, def);
		}

		if (matcher.group(12) != null) {
			String owner = matcher.group(13);
			String papiPlaceholder = '%' + matcher.group(14) + '%';

			return switch (owner.toLowerCase()) {
				case "casterpapi" -> new CasterPAPIData(matcher.group(), papiPlaceholder);
				case "targetpapi" -> new TargetPAPIData(matcher.group(), papiPlaceholder);
				default -> new DefaultPAPIData(matcher.group(), papiPlaceholder);
			};
		}

		if (matcher.group(15) != null) {
			String player = matcher.group(16);
			String papiPlaceholder = '%' + matcher.group(17) + '%';

			return new PlayerPAPIData(matcher.group(), papiPlaceholder, player);
		}

		return switch (matcher.group(18)) {
			case "%r" -> new DefaultNameData();
			case "%a" -> new CasterNameData();
			case "%t" -> new TargetNameData();
			default -> null;
		};
	}

	@Override
	public String get(@NotNull SpellData data) {
		if (values.isEmpty()) return fragments.get(0);

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < fragments.size() - 1; i++) {
			builder.append(fragments.get(i));
			builder.append(values.get(i).get(data));
		}
		builder.append(fragments.get(fragments.size() - 1));

		return builder.toString();
	}

	@Override
	public boolean isConstant() {
		return values.isEmpty();
	}

	public List<ConfigData<String>> getValues() {
		return values;
	}

	public List<String> getFragments() {
		return fragments;
	}

	public static abstract class PlaceholderData implements ConfigData<String> {

		protected final String placeholder;

		public PlaceholderData(String placeholder) {
			this.placeholder = placeholder;
		}

		@Override
		public boolean isConstant() {
			return false;
		}

	}

	public static class ArgumentData implements ConfigData<String> {

		private final String def;
		private final int index;

		public ArgumentData(int index, String def) {
			this.index = index;
			this.def = def;
		}

		@Override
		public String get(@NotNull SpellData data) {
			if (data.args() != null && data.args().length > index) return data.args()[index];
			else return def;
		}

		@Override
		public boolean isConstant() {
			return false;
		}

	}

	public static class DefaultVariableData extends PlaceholderData {

		private final String variable;
		private final int places;

		public DefaultVariableData(String placeholder, String variable, int places) {
			super(placeholder);

			this.variable = variable;
			this.places = places;
		}

		@Override
		public String get(@NotNull SpellData data) {
			Variable var = MagicSpells.getVariableManager().getVariable(variable);
			if (var == null) return placeholder;

			String player = data.recipient() instanceof Player p ? p.getName() : null;
			if (player == null && !(var instanceof GlobalVariable) && !(var instanceof GlobalStringVariable))
				return placeholder;

			if (places >= 0) {
				if (var instanceof PlayerStringVariable || var instanceof GlobalStringVariable)
					return TxtUtil.getStringNumber(var.getStringValue(player), places);

				return TxtUtil.getStringNumber(var.getValue(player), places);
			}

			return var.getStringValue(player);
		}

	}

	public static class CasterVariableData extends PlaceholderData {

		private final String variable;
		private final int places;

		public CasterVariableData(String placeholder, String variable, int places) {
			super(placeholder);

			this.variable = variable;
			this.places = places;
		}

		@Override
		public String get(@NotNull SpellData data) {
			Variable var = MagicSpells.getVariableManager().getVariable(variable);
			if (var == null) return placeholder;

			String player = data.caster() instanceof Player p ? p.getName() : null;
			if (player == null && !(var instanceof GlobalVariable) && !(var instanceof GlobalStringVariable))
				return placeholder;

			if (places >= 0) {
				if (var instanceof PlayerStringVariable || var instanceof GlobalStringVariable)
					return TxtUtil.getStringNumber(var.getStringValue(player), places);

				return TxtUtil.getStringNumber(var.getValue(player), places);
			}

			return var.getStringValue(player);
		}

	}

	public static class TargetVariableData extends PlaceholderData {

		private final String variable;
		private final int places;

		public TargetVariableData(String placeholder, String variable, int places) {
			super(placeholder);

			this.variable = variable;
			this.places = places;
		}

		@Override
		public String get(@NotNull SpellData data) {
			Variable var = MagicSpells.getVariableManager().getVariable(variable);
			if (var == null) return placeholder;

			String player = data.target() instanceof Player p ? p.getName() : null;
			if (player == null && !(var instanceof GlobalVariable) && !(var instanceof GlobalStringVariable))
				return placeholder;

			if (places >= 0) {
				if (var instanceof PlayerStringVariable || var instanceof GlobalStringVariable)
					return TxtUtil.getStringNumber(var.getStringValue(player), places);

				return TxtUtil.getStringNumber(var.getValue(player), places);
			}

			return var.getStringValue(player);
		}

	}

	public static class PlayerVariableData extends PlaceholderData {

		private final String variable;
		private final String player;
		private final int places;

		public PlayerVariableData(String placeholder, String variable, String player, int places) {
			super(placeholder);

			this.variable = variable;
			this.player = player;
			this.places = places;
		}

		@Override
		public String get(@NotNull SpellData data) {
			Variable var = MagicSpells.getVariableManager().getVariable(variable);
			if (var == null) return placeholder;

			if (places >= 0) {
				if (var instanceof PlayerStringVariable || var instanceof GlobalStringVariable)
					return TxtUtil.getStringNumber(var.getStringValue(player), places);

				return TxtUtil.getStringNumber(var.getValue(player), places);
			}

			return var.getStringValue(player);
		}

	}

	public static class DefaultPAPIData extends PlaceholderData {

		private final String papiPlaceholder;

		public DefaultPAPIData(String placeholder, String papiPlaceholder) {
			super(placeholder);

			this.papiPlaceholder = papiPlaceholder;
		}

		@Override
		public String get(@NotNull SpellData data) {
			if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return placeholder;
			return PlaceholderAPI.setPlaceholders(data.recipient() instanceof Player p ? p : null, papiPlaceholder);
		}

	}

	public static class CasterPAPIData extends PlaceholderData {

		private final String papiPlaceholder;

		public CasterPAPIData(String placeholder, String papiPlaceholder) {
			super(placeholder);

			this.papiPlaceholder = papiPlaceholder;
		}

		@Override
		public String get(@NotNull SpellData data) {
			if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return placeholder;
			return PlaceholderAPI.setPlaceholders(data.caster() instanceof Player p ? p : null, papiPlaceholder);
		}

	}

	public static class TargetPAPIData extends PlaceholderData {

		private final String papiPlaceholder;

		public TargetPAPIData(String placeholder, String papiPlaceholder) {
			super(placeholder);

			this.papiPlaceholder = papiPlaceholder;
		}

		@Override
		public String get(@NotNull SpellData data) {
			if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return placeholder;
			return PlaceholderAPI.setPlaceholders(data.target() instanceof Player p ? p : null, papiPlaceholder);
		}

	}

	public static class PlayerPAPIData extends PlaceholderData {

		private final String papiPlaceholder;
		private final String player;

		public PlayerPAPIData(String placeholder, String papiPlaceholder, String player) {
			super(placeholder);

			this.papiPlaceholder = papiPlaceholder;
			this.player = player;
		}

		@Override
		public String get(@NotNull SpellData data) {
			if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return placeholder;
			return PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(player), papiPlaceholder);
		}

	}

	public static class DefaultNameData extends PlaceholderData {

		public DefaultNameData() {
			super("%r");
		}

		@Override
		public String get(@NotNull SpellData data) {
			if (!data.hasRecipient()) return placeholder;
			return MagicSpells.getTargetName(data.recipient());
		}

	}

	public static class CasterNameData extends PlaceholderData {

		public CasterNameData() {
			super("%a");
		}

		@Override
		public String get(@NotNull SpellData data) {
			if (!data.hasCaster()) return placeholder;
			return MagicSpells.getTargetName(data.caster());
		}

	}

	public static class TargetNameData extends PlaceholderData {

		public TargetNameData() {
			super("%t");
		}

		@Override
		public String get(@NotNull SpellData data) {
			if (!data.hasTarget()) return placeholder;
			return MagicSpells.getTargetName(data.target());
		}

	}

}
