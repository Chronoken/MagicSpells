package com.nisovin.magicspells.spells.command;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.command.CommandSender;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.CastItem;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.spelleffects.EffectPosition;

public class UnbindSpell extends CommandSpell {

	private Set<Spell> allowedSpells;

	private String strUsage;
	private String strNoSpell;
	private String strNotBound;
	private String strUnbindAll;
	private String strCantUnbind;
	private String strCantBindSpell;

	public UnbindSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		List<String> allowedSpellsNames = getConfigStringList("allowed-spells", null);
		if (allowedSpellsNames != null && !allowedSpellsNames.isEmpty()) {
			allowedSpells = new HashSet<>();
			for (String n: allowedSpellsNames) {
				Spell s = MagicSpells.getSpellByInternalName(n);
				if (s != null) allowedSpells.add(s);
				else MagicSpells.plugin.getLogger().warning("Invalid spell defined: " + n);
			}
		}

		strUsage = getConfigString("str-usage", "You must specify a spell name.");
		strNoSpell = getConfigString("str-no-spell", "You do not know a spell by that name.");
		strNotBound = getConfigString("str-not-bound", "That spell is not bound to that item.");
		strUnbindAll = getConfigString("str-unbind-all", "All spells from your item were cleared.");
		strCantUnbind = getConfigString("str-cant-unbind", "You cannot unbind this spell");
		strCantBindSpell = getConfigString("str-cant-bind-spell", "That spell cannot be bound to an item.");
	}

	@Override
	public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL && caster instanceof Player player) {
			if (args == null || args.length == 0) {
				sendMessage(strUsage, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}

			CastItem item = new CastItem(player.getEquipment().getItemInMainHand());
			Spellbook spellbook = MagicSpells.getSpellbook(player);

			if (args[0] != null && args[0].equalsIgnoreCase("*")) {
				List<Spell> spells = new ArrayList<>();

				for (CastItem i : spellbook.getItemSpells().keySet()) {
					if (!i.equals(item)) continue;
					spells.addAll(spellbook.getItemSpells().get(i));
				}

				for (Spell s : spells) {
					spellbook.removeCastItem(s, item);
				}

				spellbook.save();
				spellbook.reload();
				sendMessage(strUnbindAll, player, args);
				playSpellEffects(EffectPosition.CASTER, player);
				return PostCastAction.NO_MESSAGES;
			}

			Spell spell = MagicSpells.getSpellByInGameName(Util.arrayJoin(args, ' '));
			if (spell == null) {
				sendMessage(strNoSpell, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}

			if (!spellbook.hasSpell(spell)) {
				sendMessage(strNoSpell, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}

			if (!spell.canCastWithItem()) {
				sendMessage(strCantBindSpell, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}

			if (allowedSpells != null && !allowedSpells.contains(spell)) {
				sendMessage(strCantUnbind, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}

			boolean removed = spellbook.removeCastItem(spell, item);
			if (!removed) {
				sendMessage(strNotBound, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}

			spellbook.save();
			sendMessage(strCastSelf, player, args, "%s", spell.getName());
			playSpellEffects(EffectPosition.CASTER, player);
			return PostCastAction.NO_MESSAGES;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String partial) {
		if (sender instanceof Player && !partial.contains(" ")) return tabCompleteSpellName(sender, partial);
		return null;
	}

	public Set<Spell> getAllowedSpells() {
		return allowedSpells;
	}

	public String getStrUsage() {
		return strUsage;
	}

	public void setStrUsage(String strUsage) {
		this.strUsage = strUsage;
	}

	public String getStrNoSpell() {
		return strNoSpell;
	}

	public void setStrNoSpell(String strNoSpell) {
		this.strNoSpell = strNoSpell;
	}

	public String getStrNotBound() {
		return strNotBound;
	}

	public void setStrNotBound(String strNotBound) {
		this.strNotBound = strNotBound;
	}

	public String getStrUnbindAll() {
		return strUnbindAll;
	}

	public void setStrUnbindAll(String strUnbindAll) {
		this.strUnbindAll = strUnbindAll;
	}

	public String getStrCantUnbind() {
		return strCantUnbind;
	}

	public void setStrCantUnbind(String strCantUnbind) {
		this.strCantUnbind = strCantUnbind;
	}

	public String getStrCantBindSpell() {
		return strCantBindSpell;
	}

	public void setStrCantBindSpell(String strCantBindSpell) {
		this.strCantBindSpell = strCantBindSpell;
	}

}
