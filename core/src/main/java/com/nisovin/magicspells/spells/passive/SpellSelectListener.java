package com.nisovin.magicspells.spells.passive;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.nisovin.magicspells.util.SpellFilter;
import com.nisovin.magicspells.util.OverridePriority;
import com.nisovin.magicspells.events.SpellSelectionChangedEvent;
import com.nisovin.magicspells.spells.passive.util.PassiveListener;

public class SpellSelectListener extends PassiveListener {

	private SpellFilter filter;

	@Override
	public void initialize(String var) {
		if (var == null || var.isEmpty()) return;

		List<String> spells = new ArrayList<>();
		List<String> deniedSpells = new ArrayList<>();
		List<String> tagList = new ArrayList<>();
		List<String> deniedTagList = new ArrayList<>();

		String[] split = var.split(",");
		for (String s : split) {
			boolean denied = false;
			s = s.trim();

			if (s.startsWith("!")) {
				s = s.substring(1);
				denied = true;
			}

			if (s.toLowerCase().startsWith("tag:")) {
				if (denied) {
					deniedTagList.add(s.substring(4));
				} else {
					tagList.add(s.substring(4));
				}
			} else {
				if (denied) {
					deniedSpells.add(s);
				} else {
					spells.add(s);
				}
			}
		}

		filter = new SpellFilter(spells, deniedSpells, tagList, deniedTagList);
	}

	@OverridePriority
	@EventHandler
	public void onSpellSelect(SpellSelectionChangedEvent event) {
		if (!(event.getCaster() instanceof Player caster)) return;
		if (!hasSpell(caster) || !canTrigger(caster)) return;
		if (filter != null && !filter.check(event.getSpell())) return;
		passiveSpell.activate(caster);
	}

}
