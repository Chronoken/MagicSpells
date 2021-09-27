package com.nisovin.magicspells.variables.meta;

import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

import com.nisovin.magicspells.util.PlayerNameUtils;
import com.nisovin.magicspells.variables.variabletypes.MetaVariable;

public class AttributeVariable extends MetaVariable {

	private final Attribute attribute;

	public AttributeVariable(Attribute attribute) {
		super();

		this.attribute = attribute;
	}

	@Override
	public double getValue(String player) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p == null) return 0D;

		AttributeInstance inst = p.getAttribute(attribute);
		if (inst == null) return 0D;

		return inst.getValue();
	}

}
