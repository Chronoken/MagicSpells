package com.nisovin.magicspells.spells.command;

import java.util.List;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import net.kyori.adventure.text.Component;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.util.*;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.config.ConfigData;

// Advanced perm is for listing other player's spells

public class ListSpell extends CommandSpell {

	private final List<String> spellsToHide;

	private final ConfigData<Boolean> reloadGrantedSpells;
	private final ConfigData<Boolean> onlyShowCastableSpells;

	private String strPrefix;
	private String strNoSpells;

	private SpellFilter filter;

	public ListSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		spellsToHide = getConfigStringList("spells-to-hide", null);

		reloadGrantedSpells = getConfigDataBoolean("reload-granted-spells", true);
		onlyShowCastableSpells = getConfigDataBoolean("only-show-castable-spells", false);

		strPrefix = getConfigString("str-prefix", "Known spells:");
		strNoSpells = getConfigString("str-no-spells", "You do not know any spells.");
	}

	@Override
	protected void initialize() {
		super.initialize();

		filter = getConfigSpellFilter("filter");
	}

	@Override
	public CastResult cast(SpellData data) {
		if (!(data.caster() instanceof Player caster)) return new CastResult(PostCastAction.ALREADY_HANDLED, data);

		Spellbook spellbook = MagicSpells.getSpellbook(caster);
		String extra = "";
		if (data.hasArgs() && spellbook.hasAdvancedPerm("list")) {
			Player p = Bukkit.getPlayer(data.args()[0]);
			if (p != null) {
				spellbook = MagicSpells.getSpellbook(p);
				extra = '(' + Util.getStringFromComponent(p.displayName()) + ") ";
			}
		}

		if (reloadGrantedSpells.get(data)) spellbook.addGrantedSpells();

		boolean onlyShowCastableSpells = this.onlyShowCastableSpells.get(data);
		Spellbook finalSpellbook = spellbook;

		List<Spell> spells = MagicSpells.getSpellsOrdered()
			.stream()
			.filter(spell -> shouldListSpell(spell, finalSpellbook, onlyShowCastableSpells))
			.sorted()
			.toList();

		if (spells.isEmpty()) {
			sendMessage(strNoSpells, caster, data);
			return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
		}

		Component message = Util.getMiniMessage(MagicSpells.getTextColor() + strPrefix + " " + extra);

		boolean prev = false;
		for (Spell spell : spells) {
			if (shouldListSpell(spell, spellbook, onlyShowCastableSpells)) {
				if (prev) message = message.append(Component.text(", "));

				message = message.append(Util.getMiniMessage(spell.getName()));
				prev = true;
			}
		}

		caster.sendMessage(message);
		playSpellEffects(data);

		return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
	}

	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		Component message;

		Collection<Spell> spells = MagicSpells.spells();
		if (args != null && args.length > 0) {
			Player player = Bukkit.getPlayer(args[0]);
			if (player == null) {
				sender.sendPlainMessage("No such player.");
				return true;
			}

			spells = MagicSpells.getSpellbook(player).getSpells();
			message = Component.text(player.getName() + "'s spells: ");
		} else message = Component.text("All spells: ");

		boolean prev = false;
		for (Spell spell : spells) {
			if (prev) message = message.append(Component.text(", "));

			message = message.append(Util.getMiniMessage(spell.getName()));
			prev = true;
		}

		sender.sendMessage(message);

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length != 1) return null;

		if (sender instanceof Player player) {
			if (!MagicSpells.getSpellbook(player).hasAdvancedPerm("list")) return null;
		} else if (!(sender instanceof ConsoleCommandSender)) return null;

		return TxtUtil.tabCompletePlayerName(sender);
	}

	private boolean shouldListSpell(Spell spell, Spellbook spellbook, boolean onlyShowCastableSpells) {
		if (spell.isHelperSpell()) return false;
		if (!spellbook.hasSpell(spell, false)) return false;
		if (onlyShowCastableSpells && (!spellbook.canCast(spell) || spell instanceof PassiveSpell)) return false;
		if (spellsToHide != null && spellsToHide.contains(spell.getInternalName())) return false;
		return filter.check(spell);
	}

	public List<String> getSpellsToHide() {
		return spellsToHide;
	}

	public String getStrPrefix() {
		return strPrefix;
	}

	public void setStrPrefix(String strPrefix) {
		this.strPrefix = strPrefix;
	}

	public String getStrNoSpells() {
		return strNoSpells;
	}

	public void setStrNoSpells(String strNoSpells) {
		this.strNoSpells = strNoSpells;
	}

	public SpellFilter getFilter() {
		return filter;
	}

	public void setFilter(SpellFilter filter) {
		this.filter = filter;
	}

}
