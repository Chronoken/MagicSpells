package com.nisovin.magicspells.castmodifiers;

import java.util.Map;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.VariableMod;
import com.nisovin.magicspells.variables.Variable;
import com.nisovin.magicspells.util.ModifierResult;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.ManaChangeEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.util.VariableMod.VariableOwner;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;
import com.nisovin.magicspells.castmodifiers.customdata.CustomData;
import com.nisovin.magicspells.events.MagicSpellsGenericPlayerEvent;
import com.nisovin.magicspells.castmodifiers.customdata.CustomDataFloat;

public enum ModifierType {

	REQUIRED(false, "required", "require") {

		@Override
		public boolean apply(SpellCastEvent event, boolean check, CustomData customData) {
			if (!check) event.setCancelled(true);
			return check;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, CustomData customData) {
			if (!check) event.setNewAmount(event.getOldAmount());
			return check;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, CustomData customData) {
			if (!check) event.setCancelled(true);
			return check;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData) {
			if (!check) event.setCancelled(true);
			return check;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData) {
			if (!check) event.setCancelled(true);
			return check;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData) {
			return result;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData) {
			return result;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData) {
			return result;
		}

	},

	DENIED(false, "denied", "deny") {

		@Override
		public boolean apply(SpellCastEvent event, boolean check, CustomData customData) {
			if (check) event.setCancelled(true);
			return !check;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, CustomData customData) {
			if (check) event.setNewAmount(event.getOldAmount());
			return !check;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, CustomData customData) {
			if (check) event.setCancelled(true);
			return !check;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData) {
			if (check) event.setCancelled(true);
			return !check;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData) {
			if (check) event.setCancelled(true);
			return !check;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData) {
			return new ModifierResult(result.data(), !result.check());
		}

		@Override
		public ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData) {
			return new ModifierResult(result.data(), !result.check());
		}

		@Override
		public ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData) {
			return new ModifierResult(result.data(), !result.check());
		}

	},

	POWER(true, "power", "empower", "multiply") {

		@Override
		public boolean apply(SpellCastEvent event, boolean check, CustomData customData) {
			if (check) event.increasePower((CustomDataFloat.from(customData, event)));
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, CustomData customData) {
			if (check) {
				int gain = event.getNewAmount() - event.getOldAmount();
				gain = Math.round(gain * CustomDataFloat.from(customData, event));
				int newAmt = event.getOldAmount() + gain;
				if (newAmt > event.getMaxMana()) newAmt = event.getMaxMana();
				event.setNewAmount(newAmt);
			}
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, CustomData customData) {
			if (check) event.increasePower(CustomDataFloat.from(customData, event));
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData) {
			if (result.check()) {
				SpellData data = result.data();
				return new ModifierResult(data.power(data.power() * CustomDataFloat.from(customData, data)), true);
			}

			return new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData) {
			if (result.check()) {
				SpellData data = result.data();
				return new ModifierResult(data.power(data.power() * CustomDataFloat.from(customData, data)), true);
			}

			return new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData) {
			if (result.check()) {
				SpellData data = result.data();
				return new ModifierResult(data.power(data.power() * CustomDataFloat.from(customData, data)), true);
			}

			return new ModifierResult(result.data(), true);
		}

		@Override
		public CustomData buildCustomActionData(String text) {
			return new CustomDataFloat(text);
		}

	},

	ADD_POWER(true, "addpower", "add") {

		@Override
		public boolean apply(SpellCastEvent event, boolean check, CustomData customData) {
			if (check) event.setPower(event.getPower() + CustomDataFloat.from(customData, event));
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, CustomData customData) {
			if (check) {
				int newAmt = event.getNewAmount() + (int) CustomDataFloat.from(customData, event);
				if (newAmt > event.getMaxMana()) newAmt = event.getMaxMana();
				if (newAmt < 0) newAmt = 0;
				event.setNewAmount(newAmt);
			}
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, CustomData customData) {
			if (check) event.setPower(event.getPower() + CustomDataFloat.from(customData, event));
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData) {
			if (result.check()) {
				SpellData data = result.data();
				return new ModifierResult(data.power(data.power() + CustomDataFloat.from(customData, data)), true);
			}

			return new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData) {
			if (result.check()) {
				SpellData data = result.data();
				return new ModifierResult(data.power(data.power() + CustomDataFloat.from(customData, data)), true);
			}

			return new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData) {
			if (result.check()) {
				SpellData data = result.data();
				return new ModifierResult(data.power(data.power() + CustomDataFloat.from(customData, data)), true);
			}

			return new ModifierResult(result.data(), true);
		}

		@Override
		public CustomData buildCustomActionData(String text) {
			return new CustomDataFloat(text);
		}

	},

	COOLDOWN(true, "cooldown") {

		@Override
		public boolean apply(SpellCastEvent event, boolean check, CustomData customData) {
			if (check) event.setCooldown(CustomDataFloat.from(customData, event));
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData) {
			return result.check() ? result : new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData) {
			return result.check() ? result : new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData) {
			return result.check() ? result : new ModifierResult(result.data(), true);
		}

		@Override
		public CustomData buildCustomActionData(String text) {
			return new CustomDataFloat(text);
		}

	},

	REAGENTS(true, "reagents") {

		@Override
		public boolean apply(SpellCastEvent event, boolean check, CustomData customData) {
			if (check) event.setReagents(event.getReagents().multiply(CustomDataFloat.from(customData, event)));
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData) {
			return result.check() ? result : new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData) {
			return result.check() ? result : new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData) {
			return result.check() ? result : new ModifierResult(result.data(), true);
		}

		@Override
		public CustomData buildCustomActionData(String text) {
			return new CustomDataFloat(text);
		}

	},

	CAST_TIME(true, "casttime") {

		@Override
		public boolean apply(SpellCastEvent event, boolean check, CustomData customData) {
			if (check) event.setCastTime((int) CustomDataFloat.from(customData, event));
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData) {
			return true;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData) {
			return result.check() ? result : new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData) {
			return result.check() ? result : new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData) {
			return result.check() ? result : new ModifierResult(result.data(), true);
		}

		@Override
		public CustomData buildCustomActionData(String text) {
			return new CustomDataFloat(text);
		}

	},

	STOP(false, "stop") {

		@Override
		public boolean apply(SpellCastEvent event, boolean check, CustomData customData) {
			return !check;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, CustomData customData) {
			return !check;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, CustomData customData) {
			return !check;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData) {
			return !check;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData) {
			return !check;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData) {
			return new ModifierResult(result.data(), !result.check());
		}

		@Override
		public ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData) {
			return new ModifierResult(result.data(), !result.check());
		}

		@Override
		public ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData) {
			return new ModifierResult(result.data(), !result.check());
		}

	},

	CONTINUE(false, "continue") {

		@Override
		public boolean apply(SpellCastEvent event, boolean check, CustomData customData) {
			return check;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, CustomData customData) {
			return check;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, CustomData customData) {
			return check;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData) {
			return check;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData) {
			return check;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData) {
			return result;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData) {
			return result;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData) {
			return result;
		}

	},

	CAST(true, "cast") {

		static class CastData extends CustomData {

			public String invalidText;

			public Subspell spell;

			@Override
			public boolean isValid() {
				return spell != null;
			}

			@Override
			public String getInvalidText() {
				return invalidText;
			}

		}

		@Override
		public boolean apply(SpellCastEvent event, boolean check, CustomData customData) {
			CastData data = (CastData) customData;
			if (check && data.isValid()) data.spell.subcast(event.getSpellData().noTargeting());
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, CustomData customData) {
			CastData data = (CastData) customData;
			if (check && data.isValid()) data.spell.subcast(new SpellData(event.getPlayer()));
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, CustomData customData) {
			CastData data = (CastData) customData;
			if (check && data.isValid()) data.spell.subcast(event.getSpellData().noLocation());
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData) {
			CastData data = (CastData) customData;
			if (check && data.isValid()) data.spell.subcast(event.getSpellData().noTarget());
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData) {
			CastData data = (CastData) customData;
			if (check && data.isValid()) data.spell.subcast(new SpellData(event.getPlayer()));
			return true;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData) {
			CastData data = (CastData) customData;
			if (result.check() && data.isValid()) data.spell.subcast(result.data().noTargeting());
			return result.check() ? result : new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData) {
			CastData data = (CastData) customData;
			if (result.check() && data.isValid()) data.spell.subcast(result.data().noLocation());
			return result.check() ? result : new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData) {
			CastData data = (CastData) customData;
			if (result.check() && data.isValid()) data.spell.subcast(result.data().noTarget());
			return result.check() ? result : new ModifierResult(result.data(), true);
		}

		@Override
		public CustomData buildCustomActionData(String text) {
			CastData data = new CastData();
			if (text == null) {
				data.invalidText = "No spell defined.";
				return data;
			}

			Subspell spell = new Subspell(text);
			if (spell.process()) data.spell = spell;
			else data.invalidText = "Spell '" + text + "' does not exist.";

			return data;
		}

	},

	CAST_INSTEAD(true, "castinstead") {

		static class CustomInsteadData extends CustomData {

			public String invalidText;

			public Subspell spell;

			@Override
			public boolean isValid() {
				return spell != null;
			}

			@Override
			public String getInvalidText() {
				return invalidText;
			}

		}

		@Override
		public boolean apply(SpellCastEvent event, boolean check, CustomData customData) {
			CustomInsteadData data = (CustomInsteadData) customData;
			if (check && data.isValid()) {
				data.spell.subcast(event.getSpellData().noTargeting());
				event.setCancelled(true);
			}
			return !check;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, CustomData customData) {
			CustomInsteadData data = (CustomInsteadData) customData;
			if (check && data.isValid()) data.spell.subcast(new SpellData(event.getPlayer()));
			return !check;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, CustomData customData) {
			CustomInsteadData data = (CustomInsteadData) customData;
			if (check && data.isValid()) {
				data.spell.subcast(event.getSpellData().noLocation());
				event.setCastCancelled(true);
			}
			return !check;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData) {
			CustomInsteadData data = (CustomInsteadData) customData;
			if (check && data.isValid()) {
				data.spell.subcast(event.getSpellData().noTarget());
				event.setCastCancelled(true);
			}
			return !check;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData) {
			CustomInsteadData data = (CustomInsteadData) customData;
			if (check && data.isValid()) data.spell.subcast(new SpellData(event.getPlayer()));
			return !check;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData) {
			CustomInsteadData data = (CustomInsteadData) customData;
			if (result.check() && data.isValid()) data.spell.subcast(result.data().noTargeting());
			return new ModifierResult(result.data(), !result.check());
		}

		@Override
		public ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData) {
			CustomInsteadData data = (CustomInsteadData) customData;
			if (result.check() && data.isValid()) data.spell.subcast(result.data().noLocation());
			return new ModifierResult(result.data(), !result.check());
		}

		@Override
		public ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData) {
			CustomInsteadData data = (CustomInsteadData) customData;
			if (result.check() && data.isValid()) data.spell.subcast(result.data().noTarget());
			return new ModifierResult(result.data(), !result.check());
		}

		@Override
		public CustomData buildCustomActionData(String text) {
			CustomInsteadData data = new CustomInsteadData();
			if (text == null) {
				data.invalidText = "No spell defined.";
				return data;
			}

			Subspell spell = new Subspell(text);
			if (spell.process()) data.spell = spell;
			else data.invalidText = "Spell '" + text + "' does not exist.";

			return data;
		}

	},

	VARIABLE_MODIFY(true, "variable") {

		static class VariableModData extends CustomData {

			private String invalidText = "Variable action is invalid.";

			public VariableOwner variableOwner;
			public Variable variable;
			public VariableMod mod;

			@Override
			public boolean isValid() {
				return variable != null;
			}

			@Override
			public String getInvalidText() {
				return invalidText;
			}

		}

		private void modifyVariable(CustomData customData, SpellData spellData) {
			if (!customData.isValid()) return;
			VariableModData data = (VariableModData) customData;

			LivingEntity owner = data.variableOwner == VariableOwner.CASTER ? spellData.caster() : spellData.target();
			if (!(owner instanceof Player playerOwner)) return;

			MagicSpells.getVariableManager().processVariableMods(data.variable, data.mod, playerOwner, spellData);
		}

		@Override
		public boolean apply(SpellCastEvent event, boolean check, CustomData customData) {
			if (check) modifyVariable(customData, event.getSpellData());
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, CustomData customData) {
			if (check) modifyVariable(customData, new SpellData(event.getPlayer()));
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, CustomData customData) {
			if (check) modifyVariable(customData, event.getSpellData());
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData) {
			if (check) modifyVariable(customData, event.getSpellData());
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData) {
			if (check) modifyVariable(customData, new SpellData(event.getPlayer()));
			return true;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData) {
			if (result.check()) {
				modifyVariable(customData, result.data());
				return result;
			}
			return new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData) {
			if (result.check()) {
				modifyVariable(customData, result.data());
				return result;
			}
			return new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData) {
			if (result.check()) {
				modifyVariable(customData, result.data());
				return result;
			}
			return new ModifierResult(result.data(), true);
		}

		@Override
		public CustomData buildCustomActionData(String text) {
			VariableModData data = new VariableModData();
			if (text == null) {
				data.invalidText = "No data action data defined.";
				return data;
			}

			if (!text.contains(";")) {
				data.invalidText = "Data is invalid.";
				return data;
			}

			String[] splits = text.split(";", 2);
			if (splits.length < 2) {
				data.invalidText = "VarMod is not defined.";
				return data;
			}

			String varData = splits[0];
			VariableOwner variableOwner = VariableOwner.CASTER;
			String variableName;
			if (varData.contains(":")) {
				String[] varDataSplits = varData.split(":");
				if (varDataSplits[0].startsWith("target")) variableOwner = VariableOwner.TARGET;
				variableName = varDataSplits[1];
			} else variableName = varData;

			data.variableOwner = variableOwner;
			data.mod = new VariableMod(splits[1]);
			data.variable = MagicSpells.getVariableManager().getVariable(variableName);
			if (data.variable == null) data.invalidText = "Variable does not exist.";
			return data;
		}

	},

	STRING(true, "string") {

		static class StringData extends CustomData {

			public String invalidText;

			public Variable variable;
			public String value;

			@Override
			public boolean isValid() {
				return variable != null && value != null;
			}

			@Override
			public String getInvalidText() {
				return invalidText;
			}

		}

		private void setVariable(CustomData customData, SpellData spellData) {
			if (!customData.isValid()) return;
			StringData data = (StringData) customData;

			if (!(spellData.caster() instanceof Player caster)) return;

			String value = MagicSpells.doReplacements(data.value, spellData);
			MagicSpells.getVariableManager().set(data.variable, caster.getName(), value);
		}

		@Override
		public boolean apply(SpellCastEvent event, boolean check, CustomData customData) {
			if (check) setVariable(customData, event.getSpellData());
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, CustomData customData) {
			if (check) setVariable(customData, new SpellData(event.getPlayer()));
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, CustomData customData) {
			if (check) setVariable(customData, event.getSpellData());
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData) {
			if (check) setVariable(customData, event.getSpellData());
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData) {
			if (check) setVariable(customData, new SpellData(event.getPlayer()));
			return true;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData) {
			if (result.check()) {
				setVariable(customData, result.data());
				return result;
			}
			return new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData) {
			if (result.check()) {
				setVariable(customData, result.data());
				return result;
			}
			return new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData) {
			if (result.check()) {
				setVariable(customData, result.data());
				return result;
			}
			return new ModifierResult(result.data(), true);
		}

		@Override
		public CustomData buildCustomActionData(String text) {
			StringData data = new StringData();
			if (text == null || text.trim().isEmpty() || !text.contains(" ")) {
				data.invalidText = "Data is invalid.";
				return data;
			}

			String[] splits = text.split(" ", 2);
			data.variable = MagicSpells.getVariableManager().getVariable(splits[0]);
			if (data.variable == null) data.invalidText = "Variable does not exist.";
			data.value = splits[1];
			return data;
		}

	},

	MESSAGE(true, "message") {

		static class MessageData extends CustomData {

			public final String message;

			MessageData(String message) {
				this.message = message;
			}

			@Override
			public boolean isValid() {
				return message != null && !message.isEmpty();
			}

			@Override
			public String getInvalidText() {
				return "No message specified.";
			}

		}

		@Override
		public boolean apply(SpellCastEvent event, boolean check, CustomData customData) {
			if (check && customData.isValid())
				MagicSpells.sendMessage(((MessageData) customData).message, event.getCaster(), event.getSpellData());

			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, CustomData customData) {
			if (check && customData.isValid())
				MagicSpells.sendMessage(event.getPlayer(), ((MessageData) customData).message);

			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, CustomData customData) {
			if (check && customData.isValid())
				MagicSpells.sendMessage(((MessageData) customData).message, event.getCaster(), event.getSpellData());

			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData) {
			if (check && customData.isValid())
				MagicSpells.sendMessage(((MessageData) customData).message, event.getCaster(), event.getSpellData());

			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData) {
			if (check && customData.isValid())
				MagicSpells.sendMessage(event.getPlayer(), ((MessageData) customData).message);

			return true;
		}

		@Override
		public ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData) {
			if (result.check() && customData.isValid())
				MagicSpells.sendMessage(((MessageData) customData).message, caster, result.data());

			return new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData) {
			if (result.check() && customData.isValid())
				MagicSpells.sendMessage(((MessageData) customData).message, caster, result.data());

			return new ModifierResult(result.data(), true);
		}

		@Override
		public ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData) {
			if (result.check() && customData.isValid())
				MagicSpells.sendMessage(((MessageData) customData).message, caster, result.data());

			return new ModifierResult(result.data(), true);
		}

		@Override
		public CustomData buildCustomActionData(String text) {
			return new MessageData(text);
		}

	}

	;

	private final String[] keys;
	private static boolean initialized = false;

	private final boolean usesCustomData;

	ModifierType(boolean usesCustomData, String... keys) {
		this.keys = keys;
		this.usesCustomData = usesCustomData;
	}

	public boolean usesCustomData() {
		return usesCustomData;
	}

	public abstract boolean apply(SpellCastEvent event, boolean check, CustomData customData);
	public abstract boolean apply(ManaChangeEvent event, boolean check, CustomData customData);
	public abstract boolean apply(SpellTargetEvent event, boolean check, CustomData customData);
	public abstract boolean apply(SpellTargetLocationEvent event, boolean check, CustomData customData);
	public abstract boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, CustomData customData);

	public abstract ModifierResult apply(LivingEntity caster, ModifierResult result, CustomData customData);
	public abstract ModifierResult apply(LivingEntity caster, LivingEntity target, ModifierResult result, CustomData customData);
	public abstract ModifierResult apply(LivingEntity caster, Location target, ModifierResult result, CustomData customData);

	public CustomData buildCustomActionData(String text) {
		return null;
	}

	static Map<String, ModifierType> nameMap;

	static void initialize() {
		nameMap = new HashMap<>();
		for (ModifierType type : ModifierType.values()) {
			for (String key : type.keys) {
				nameMap.put(key.toLowerCase(), type);
			}
		}
		initialized = true;
	}

	public static ModifierType getModifierTypeByName(String name) {
		if (!initialized) initialize();
		return nameMap.get(name.toLowerCase());
	}

}
