package dev.wiji.pixelparty.enums;

import net.luckperms.api.model.group.Group;
import org.bukkit.Color;

public enum BootColor {

	ADMIN(Color.RED),
	MVP(Color.AQUA),
	VIP(Color.LIME),
	DEFAULT(Color.GRAY);

	final Color color;

	BootColor(Color color) {
		this.color = color;
	}

	public static BootColor fromGroup(Group group) {
		for(BootColor value : values()) {
			if(value.name().equalsIgnoreCase(group.getName())) {
				return value;
			}
		}

		return DEFAULT;
	}

	public Color getColor() {
		return color;
	}
}
