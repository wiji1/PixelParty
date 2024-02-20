package dev.wiji.pixelparty.enums;

import dev.wiji.pixelparty.sql.Constraint;
import dev.wiji.pixelparty.sql.Field;
import dev.wiji.pixelparty.sql.SQLTable;
import dev.wiji.pixelparty.sql.TableManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;

import java.sql.ResultSet;
import java.util.UUID;

public enum Group {

	ADMIN("ADMIN", ChatColor.RED, Color.RED),
	DEFAULT("", ChatColor.GRAY, Color.GRAY),
	;

	final String displayName;
	final ChatColor chatColor;
	final Color color;

	Group(String displayName, ChatColor chatColor, Color color) {
		this.displayName = displayName;
		this.chatColor = chatColor;
		this.color = color;
	}

	public String getDisplayName() {
		return displayName;
	}

	public ChatColor getChatColor() {
		return chatColor;
	}

	public Color getColor() {
		return color;
	}

	public String toString() {
		return name();
	}

	public static Group fromString(String string) {
		return Group.valueOf(string.toUpperCase());
	}

	public static Group getGroup(UUID uuid) {
		SQLTable table = TableManager.getTable("PlayerData");
		if(table == null) throw new RuntimeException("PlayerData table failed to register!");

		ResultSet rs = table.selectRow(new Constraint("uuid", uuid.toString()), new Field("userGroup"));

		Group group = DEFAULT;

		try {
			if(rs.next()) group = Group.valueOf(rs.getString("userGroup"));
			rs.close();
		} catch(Exception e) { throw new RuntimeException(e); }

		return group;
	}
}
