package net.goo.brutality.entity.custom.trident.physics_projectile;

import net.goo.brutality.client.entity.ArmaGeoEntity;
import net.goo.brutality.entity.base.BrutalityAbstractPhysicsProjectile;
import net.goo.brutality.entity.base.BrutalityAbstractTrident;
import net.goo.brutality.registry.ModItems;
import net.goo.brutality.registry.ModParticles;
import net.goo.brutality.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ThrownWintermelon extends BrutalityAbstractPhysicsProjectile implements ArmaGeoEntity {
    public ThrownWintermelon(Level pLevel, LivingEntity pShooter, ItemStack pStack, EntityType<? extends AbstractArrow> trident) {
        super(pLevel, pShooter, pStack, trident);
    }

    public ThrownWintermelon(EntityType<? extends BrutalityAbstractTrident> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public String geoIdentifier() {
        return "thrown_wintermelon";
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(ModItems.WINTERMELON_TRIDENT.get());
    }

    @Override
    protected @NotNull SoundEvent getDefaultHitGroundSoundEvent() {
        return ModSounds.SQUELCH.get();
    }

    @Override
    protected SoundEvent getHitEntitySoundEvent() {
        return ModSounds.SQUELCH.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        Entity hitEntity = pResult.getEntity();
        Vec3 location = pResult.getLocation();
        for (int i = 0; i < 16; i++) {
            this.level().addParticle(ModParticles.WINTERMELON_PARTICLE.get(),
                    location.x, location.y + hitEntity.getBbHeight() / 2, location.z,
                    (random.nextDouble() - 0.5) * 0.3,
                    random.nextDouble() * 0.2,
                    (random.nextDouble() - 0.5) * 0.3);
        }

        if (hitEntity instanceof LivingEntity)
            ((LivingEntity) hitEntity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, true));

    }

    @Override
    protected float getBounciness() {
        return 0.15F;
    }

    @Override
    public float getModelHeight() {
        return 16;
    }
}
