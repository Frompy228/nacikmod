package net.artur.nacikmod.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public class SafeFakePlayer extends FakePlayer {
    static SafeFakePlayer faker = null;

    int mana = 0;

    static String NACIK_SAFE_FAKE_PLAYER_ID = "c1e6e43b-dc66-4561-8a13-cb374b2c162a";

    public SafeFakePlayer(ServerLevel world, String name) {
        super(world, new GameProfile(UUID.fromString(NACIK_SAFE_FAKE_PLAYER_ID), name));
    }

    public SafeFakePlayer(ServerLevel world, String name, UUID uuid) {
        super(world, new GameProfile(uuid, name));
    }

    public Vec3 getPosition() {
        return new Vec3(this.getX(), this.getY(), this.getZ());
    }

    public void setMana(int m) {
        this.mana = m;
    }

    public int getMana() {
        return this.mana;
    }

    public static SafeFakePlayer createFakePlayerIfNull(ServerLevel world) {
        if (faker == null)
            faker = new SafeFakePlayer(world, "faker");
        return faker;
    }
}
