package dev.wiji.pixelparty.powerups;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.controllers.FloorManager;
import dev.wiji.pixelparty.enums.GameSound;
import dev.wiji.pixelparty.objects.PixelMooshroom;
import dev.wiji.pixelparty.objects.PowerUp;
import dev.wiji.pixelparty.util.Misc;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ColorCow extends PowerUp {

	FloorManager floorManager = PixelParty.gameManager.floorManager;

	public ColorCow() {
		Misc.registerEntity("ColorCow", 96, PixelMooshroom.class);
	}

	@Override
	public void onActivate(Player player, Location location) {
		ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
		stand.setVisible(false);
		stand.setGravity(false);
		stand.setCustomName(Misc.color("&a&lColor &b&lCow"));
		stand.setCustomNameVisible(true);
		stand.setSmall(true);
		stand.setMarker(true);

		Slime slime = location.getWorld().spawn(location, Slime.class);
		slime.setSize(1);
		slime.setPassenger(stand);
		slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 0, false, false));

		World nmsWorld = ((CraftWorld) player.getWorld()).getHandle();
		PixelMooshroom colorCow = new PixelMooshroom(nmsWorld);
		colorCow.getBukkitEntity().setPassenger(slime);

		colorCow.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		nmsWorld.addEntity(colorCow);

		new BukkitRunnable() {
			@Override
			public void run() {
				slime.remove();
				colorCow.remove();
				stand.remove();

				if(colorCow.getBukkitEntity().getLocation().getY() < 0) return;
				explode(colorCow.getBukkitEntity().getLocation().clone());
			}
		}.runTaskLater(PixelParty.INSTANCE, 20 * 3);

		Bukkit.broadcastMessage(Misc.color("&aA color cow has spawned! &7It'll explode in &f3 &7seconds..."));
	}

	public void explode(Location location) {
		location.setY(0);

		if(!floorManager.isOnFloor(location)) return;

		location.getWorld().playEffect(location.clone().add(0, 1, 0), Effect.EXPLOSION_HUGE, 1);
		GameSound.COLOR_COW.play(location);

		floorManager.paint(location, floorManager.chosenColor);
	}
}
