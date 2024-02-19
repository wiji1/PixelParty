package dev.wiji.pixelparty.util;

import org.bukkit.ChatColor;

public enum Color {

	WHITE((byte) 0, ChatColor.WHITE),
	ORANGE((byte) 1, ChatColor.GOLD),
	MAGENTA((byte) 2, ChatColor.LIGHT_PURPLE),
	LIGHT_BLUE((byte) 3, ChatColor.AQUA),
	YELLOW((byte) 4, ChatColor.YELLOW),
	LIME((byte) 5, ChatColor.GREEN),
	PINK((byte) 6, ChatColor.RED),
	DARK_GRAY((byte) 7, ChatColor.DARK_GRAY),
	LIGHT_GRAY((byte) 8, ChatColor.GRAY),
	CYAN((byte) 9, ChatColor.DARK_AQUA),
	PURPLE((byte) 10, ChatColor.DARK_PURPLE),
	BLUE((byte) 11, ChatColor.BLUE),
	BROWN((byte) 12, ChatColor.BLACK),
	GREEN((byte) 13, ChatColor.DARK_GREEN),
	RED((byte) 14, ChatColor.DARK_RED),
	BLACK((byte) 15, ChatColor.BLACK);


	final Byte data;
	final ChatColor chatColor;

	Color(Byte data, ChatColor chatColor) {
		this.data = data;
		this.chatColor = chatColor;
	}

	public Byte getData() {
		return data;
	}

	public ChatColor getChatColor() {
		return chatColor;
	}

	public static Color fromData(Byte data) {
		for(Color color : Color.values()) {
			if(color.getData().equals(data)) {
				return color;
			}
		}
		return null;
	}

	public String getName() {
		StringBuilder colorText = new StringBuilder(name().replace("_", " "));
		String[] split = colorText.toString().split(" ");
		colorText = new StringBuilder();

		for(int i = 0; i < split.length; i++) {
			String s = split[i];
			colorText.append(s.substring(0, 1).toUpperCase()).append(s.substring(1).toLowerCase());
			if(i != split.length - 1) colorText.append(" ");
		}

		return colorText.toString();
	}
}
