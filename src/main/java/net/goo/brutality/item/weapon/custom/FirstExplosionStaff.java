package net.goo.brutality.item.weapon.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.goo.brutality.Brutality;
import net.goo.brutality.entity.custom.ExplosionRay;
import net.goo.brutality.item.base.BrutalityGenericItem;
import net.goo.brutality.network.PacketHandler;
import net.goo.brutality.network.s2cEnhancedExactParticlePacket;
import net.goo.brutality.particle.custom.ExplosionAmbientParticle;
import net.goo.brutality.particle.custom.flat.ExplosionMagicCircleParticle;
import net.goo.brutality.registry.ModEntities;
import net.goo.brutality.util.ModResources;
import net.goo.brutality.util.helpers.CameraHelper;
import net.goo.brutality.util.helpers.ModExplosionHelper;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.List;

import static net.goo.brutality.util.ModUtils.lookingAtBlock;

@Mod.EventBusSubscriber(modid = Brutality.MOD_ID)
public class FirstExplosionStaff extends BrutalityGenericItem {
    private int useDuration, randYaw, randPitch, circleCount = 0;
    private BlockPos targetBlockPos;
    private static String SUCCESSFUL = "Successful";
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;

    public FirstExplosionStaff(Properties pProperties, String identifier, Rarity rarity, List<BrutalityTooltipHelper.DescriptionComponent> descriptionComponents) {
        super(pProperties, identifier, rarity, descriptionComponents);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        this.attributeModifiers = ImmutableMultimap.<Attribute, AttributeModifier>builder()
                .put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -2, AttributeModifier.Operation.ADDITION))
                .build();
    }


    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        stack.getOrCreateTag().putBoolean(SUCCESSFUL, true);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {


        int[][] colors = BrutalityTooltipHelper.getLoreColors(this.getClass());
        pTooltipComponents.add(Component.translatable("rarity." + Brutality.MOD_ID + "." + rarity).withStyle(Style.EMPTY.withFont(ModResources.RARITY_FONT)));
        for (int i = 1; i <= 3; i++) {
            pTooltipComponents.add(BrutalityTooltipHelper.tooltipHelper("item." + Brutality.MOD_ID + "." + identifier + ".lore." + i, false, null, colors[1]));
        }

        assert Minecraft.getInstance().level != null;
        pTooltipComponents.add(BrutalityTooltipHelper.tooltipHelper("item." + Brutality.MOD_ID + "." + identifier + ".lore.4", false, ALAGARD_LARGE, 0.25F, 4, new int[][]{{203,53,61}, {237,98,64}, {249,182,78}}));

        pTooltipComponents.add(Component.literal(""));
        if (pStack.isEnchanted()) pTooltipComponents.add(Component.literal(""));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot, ItemStack stack) {
        return equipmentSlot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(equipmentSlot, stack);
    }

    @SubscribeEvent
    public static void onFOVModifier(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();

        if (player.isUsingItem()) {
            ItemStack itemStack = player.getUseItem();
            if (itemStack.getItem() instanceof FirstExplosionStaff) {
                event.setNewFovModifier(event.getFovModifier() * 1.5F);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack pStack = pPlayer.getItemInHand(pUsedHand);
        pPlayer.startUsingItem(pUsedHand);
        if (pLevel instanceof ServerLevel serverLevel) {
            triggerAnim(pPlayer, GeoItem.getOrAssignId(pStack, serverLevel), "controller", "use");
            pStack.getOrCreateTag().putBoolean(SUCCESSFUL, true);
            targetBlockPos = lookingAtBlock(pPlayer, false, 300);
            randPitch = pLevel.random.nextIntBetweenInclusive(-15, 16);
            randYaw = pLevel.random.nextIntBetweenInclusive(0, 361);

            for (int i = 0; i < 2; i++) {
                PacketHandler.sendToAllClients(new s2cEnhancedExactParticlePacket(
                        pPlayer.getX(),
                        pPlayer.getY() + 0.1,
                        pPlayer.getZ(),
                        0, 0, 0,
                        new ExplosionMagicCircleParticle.ParticleData(
                                pPlayer.getId(),
                                1,
                                0,
                                0,
                                0, i == 1, true
                        )
                ));

            }
        }


        return InteractionResultHolder.consume(pPlayer.getItemInHand(pUsedHand));
    }

    private void spawnExplosionSpellMagicCircles(Level level, Player player, int step) {
        if (targetBlockPos == null) return;

        double pitchRad = Math.toRadians(randPitch);
        double yawRad = Math.toRadians(randYaw);

        Vec3 startPos = Vec3.atCenterOf(targetBlockPos);
        int baseHeight = 14;
        int circleStep = 8;

        int height = baseHeight + circleStep * step;
        double radius = height * Math.tan(pitchRad);
        double xOffset = radius * Math.sin(yawRad);
        double zOffset = radius * Math.cos(yawRad);

        Vec3 position = new Vec3(startPos.x + xOffset, startPos.y + height, startPos.z + zOffset);

        PacketHandler.sendToAllClients(new s2cEnhancedExactParticlePacket(
                position.x,
                position.y,
                position.z,
                0, 0, 0,
                new ExplosionMagicCircleParticle.ParticleData(
                        player.getId(),
                        level.random.nextIntBetweenInclusive(5, 50),
                        randPitch,
                        randYaw,
                        0, level.random.nextBoolean(), false
                )
        ));

    }


    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        useDuration = getUseDuration(pStack) - pRemainingUseDuration;
        if (pLevel.isClientSide())
            CameraHelper.lockCamera();
        if (!pLevel.isClientSide()) {

            if (useDuration % 20 == 0) {
                if (circleCount < 10) {
                    circleCount++;
                    spawnExplosionSpellMagicCircles(pLevel, ((Player) pLivingEntity), circleCount);
                } else if (pLivingEntity instanceof Player player) {
                    player.displayClientMessage(Component.translatable("item." + Brutality.MOD_ID + "." + identifier + ".maximum").withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.BOLD), true);
                }
            }

            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 10, false, false));
        }
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (!pIsSelected) {
            CameraHelper.unlockCamera();
        }

        if (pLevel instanceof ServerLevel serverLevel && pEntity instanceof LivingEntity livingEntity) {
            if (!pStack.getOrCreateTag().getBoolean(SUCCESSFUL))
                triggerAnim(livingEntity, GeoItem.getOrAssignId(pStack, serverLevel), "controller", "release");

        }
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if (pLivingEntity instanceof Player pPlayer) {
            if (pLevel.isClientSide()) {
                CameraHelper.unlockCamera();
            }
            if (!pLevel.isClientSide()) {
                triggerAnim(pPlayer, GeoItem.getOrAssignId(pStack, ((ServerLevel) pLevel)), "controller", "release");
                if (pStack.getOrCreateTag().getBoolean(SUCCESSFUL)) {
                    int explosionSize = circleCount * 3;
                    int fatigueTime = 200 * explosionSize;

//                pPlayer.getCooldowns().addCooldown(pStack.getItem(), fatigueTime);
                    pPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, fatigueTime, 1));
                    pPlayer.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, fatigueTime, 1));
                    pPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, fatigueTime, 1));

                    PacketHandler.sendToAllClients(new s2cEnhancedExactParticlePacket(
                            targetBlockPos.getCenter().x(),
                            targetBlockPos.getCenter().y(),
                            targetBlockPos.getCenter().z(),
                            0, 0, 0,
                            new ExplosionAmbientParticle.ParticleData(
                                    pPlayer.getId(),
                                    explosionSize * 2F
                            )
                    ));


                    ExplosionRay explosionRay = new ExplosionRay(ModEntities.EXPLOSION_RAY.get(), pLevel);
                    explosionRay.setOwner(pPlayer.getUUID());
                    explosionRay.setPitch(randPitch);
                    explosionRay.setYaw(randYaw);
                    explosionRay.setCircleCount(circleCount);
                    explosionRay.setPos(targetBlockPos.getCenter().x, targetBlockPos.getY(), targetBlockPos.getCenter().z);
                    pLevel.addFreshEntity(explosionRay);
                    ModExplosionHelper.ExplosionManager.addExplosion(pLevel, pPlayer, explosionRay.blockPosition(), explosionSize, 3);

                } else {
                    pLivingEntity.hurt(pLivingEntity.damageSources().indirectMagic(pLivingEntity, pLivingEntity), circleCount);
                }
                circleCount = 0;
            }
        }
    }


    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        if (player.level() instanceof ServerLevel serverLevel)
            triggerAnim(player, GeoItem.getOrAssignId(item, serverLevel), "controller", "idle");
        CameraHelper.unlockCamera();
        return super.onDroppedByPlayer(item, player);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", state -> state.setAndContinue(RawAnimation.begin().thenPlay("idle")))
                .triggerableAnim("use", RawAnimation.begin().thenPlay("use").thenPlayAndHold("using"))
                .triggerableAnim("release", RawAnimation.begin().thenPlayAndHold("release").thenPlayAndHold("idle"))
        );
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            handleUse(player);
        }
    }

    @SubscribeEvent
    public static void onOwnerDamaged(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            if (!player.level().isClientSide() && player.getUseItem().getItem() instanceof FirstExplosionStaff) {
                if (player.isUsingItem()) {
                    ItemStack pStack = player.getUseItem();
                    player.getCooldowns().addCooldown(pStack.getItem(), 200);
                    player.getUseItem().getOrCreateTag().putBoolean(SUCCESSFUL, false);
                    player.stopUsingItem();
                    CameraHelper.unlockCamera();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onChangeDimensions(PlayerEvent.PlayerChangedDimensionEvent event) {
        handleUse(event.getEntity());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        handleUse(event.getEntity());
    }

    public static void handleUse(Player player) {
        if (player.getUseItem().getItem() instanceof FirstExplosionStaff && player.isUsingItem()) {
            CameraHelper.unlockCamera();
        }
    }
}
