package com.toomda.parasitusfix.techguns;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import techguns.entities.npcs.ArmySoldier;
import techguns.entities.npcs.Commando;
import techguns.entities.npcs.StormTrooper;
import techguns.entities.npcs.ZombieFarmer;
import techguns.entities.npcs.ZombieMiner;
import techguns.entities.npcs.ZombiePigmanSoldier;
import techguns.entities.npcs.ZombiePoliceman;
import techguns.entities.npcs.ZombieSoldier;

public class TechgunsZombieSoldierTargetFix {
    private static final String NBT_KEY = "parasitusfix:tgsoldierTarget";
    private static final int TARGET_PRIORITY = 3;
    private static final int TARGET_CHANCE = 10;

    private static final Predicate<EntityLivingBase> TECHGUN_SOLDIER_TARGET = target ->
            target != null && isTechgunSoldier(target);

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof EntityCreature)) return;
        if (!isZombieSource(entity)) return;

        NBTTagCompound data = entity.getEntityData();
        if (data.getBoolean(NBT_KEY)) return;
        data.setBoolean(NBT_KEY, true);

        EntityCreature creature = (EntityCreature) entity;
        creature.targetTasks.addTask(
                TARGET_PRIORITY,
                new EntityAINearestAttackableTarget<>(creature, EntityLivingBase.class, TARGET_CHANCE, true, false, TECHGUN_SOLDIER_TARGET)
        );
    }

    private static boolean isZombieSource(Entity entity) {
        if (entity instanceof EntityZombie && !(entity instanceof EntityPigZombie)) {
            return true;
        }
        return isTechgunZombie(entity);
    }

    private static boolean isTechgunZombie(Entity entity) {
        return entity instanceof ZombieSoldier
                || entity instanceof ZombiePoliceman
                || entity instanceof ZombieFarmer
                || entity instanceof ZombieMiner
                || entity instanceof ZombiePigmanSoldier;
    }

    private static boolean isTechgunSoldier(EntityLivingBase entity) {
        return entity instanceof ArmySoldier
                || entity instanceof Commando
                || entity instanceof StormTrooper;
    }
}
