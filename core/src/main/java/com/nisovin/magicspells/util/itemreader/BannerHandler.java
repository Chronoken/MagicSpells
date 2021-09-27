package com.nisovin.magicspells.util.itemreader;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.handlers.DebugHandler;
import com.nisovin.magicspells.util.magicitems.MagicItemData;
import static com.nisovin.magicspells.util.magicitems.MagicItemData.MagicItemAttribute.PATTERNS;

public class BannerHandler {

	private static final String CONFIG_NAME = PATTERNS.toString();

	public static void process(ConfigurationSection config, ItemMeta meta, MagicItemData data) {
		if (!(meta instanceof BannerMeta bannerMeta)) return;
		if (!config.isList(CONFIG_NAME)) return;

		// patternType dyeColor
		List<String> strPatterns = config.getStringList(CONFIG_NAME);
		List<Pattern> patterns = new ArrayList<>();
		for (String str : strPatterns) {
			String[] args = str.split(" ");
			if (args.length < 2) continue;

			PatternType patternType = PatternType.getByIdentifier(args[0].toLowerCase());
			try {
				if (patternType == null) patternType = PatternType.valueOf(args[0].toUpperCase());
			} catch (IllegalArgumentException e) {
				DebugHandler.debugBadEnumValue(PatternType.class, args[0].toUpperCase());
				continue;
			}

			DyeColor dyeColor;
			try {
				dyeColor = DyeColor.valueOf(args[1].toUpperCase());
			} catch (IllegalArgumentException e) {
				DebugHandler.debugBadEnumValue(DyeColor.class, args[1].toUpperCase());
				continue;
			}

			Pattern pattern = new Pattern(dyeColor, patternType);
			bannerMeta.addPattern(pattern);
			patterns.add(pattern);
		}

		if (!patterns.isEmpty()) data.setAttribute(PATTERNS, patterns);
	}

	public static void processItemMeta(ItemMeta meta, MagicItemData data) {
		if (!(meta instanceof BannerMeta)) return;

		if (data.hasAttribute(PATTERNS)) ((BannerMeta) meta).setPatterns((List<Pattern>) data.getAttribute(PATTERNS));
	}

	public static void processMagicItemData(ItemMeta meta, MagicItemData data) {
		if (!(meta instanceof BannerMeta)) return;

		List<Pattern> patterns = ((BannerMeta) meta).getPatterns();
		if (!patterns.isEmpty()) data.setAttribute(PATTERNS, ((BannerMeta) meta).getPatterns());
	}
	
}
