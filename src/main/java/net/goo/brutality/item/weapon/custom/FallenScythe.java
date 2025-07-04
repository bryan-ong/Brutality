package net.goo.brutality.item.weapon.custom;

import net.goo.brutality.Brutality;
import net.goo.brutality.client.renderers.item.BrutalityAutoFullbrightItemNoDepthRenderer;
import net.goo.brutality.item.base.BrutalityScytheItem;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static net.goo.brutality.util.helpers.AttributeHelper.replaceOrAddModifier;
import static net.goo.brutality.util.helpers.EnchantmentHelper.restrictEnchants;

@Mod.EventBusSubscriber(modid = Brutality.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FallenScythe extends BrutalityScytheItem {
    public static String SOULS_HARVESTED = "souls_harvested";
    private final Style STYLE_DARK_GREEN = Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);
    private static final int baseAttackDamage = 3, entityReachBonus = 2;

    private static final Set<Enchantment> ALLOWED_ENCHANTMENTS = Set.of(
            Enchantments.MOB_LOOTING,
            Enchantments.MENDING,
            Enchantments.UNBREAKING
    );

    public FallenScythe(Tier pTier, float pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties, String identifier, Rarity rarity, List<BrutalityTooltipHelper.DescriptionComponent> descriptionComponents) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties, identifier, rarity, descriptionComponents);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
        return 2;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        int soulsHarvested = pStack.getOrCreateTag().getInt(SOULS_HARVESTED);

        int[][] colors = BrutalityTooltipHelper.getLoreColors(this.getClass());
        pTooltipComponents.add(Component.translatable("item.armament.fallen_scythe.souls_harvested").withStyle(Style.EMPTY.withColor(BrutalityTooltipHelper.rgbToInt(colors[0]))).append(BrutalityTooltipHelper.tooltipHelper(String.valueOf(soulsHarvested), true, null, colors[1])));
        pTooltipComponents.add(Component.literal(""));
        pTooltipComponents.add(Component.translatable("item.modifiers.mainhand").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));

        pTooltipComponents.add(Component.literal(" " + baseAttackDamage + " + ").withStyle(ChatFormatting.DARK_GREEN)
                .append(BrutalityTooltipHelper.tooltipHelper(String.format("%.2f", Math.min(calculateAttackDamageBonus(soulsHarvested), 18 - baseAttackDamage)) + " ", true, null, colors[1])
                        .append(Component.translatable("attribute.name.generic.attack_damage").withStyle(STYLE_DARK_GREEN.withBold(false)))));

        pTooltipComponents.add(
                Component.literal(" 1 + ")
                        .withStyle(ChatFormatting.DARK_GREEN)
                        .append(BrutalityTooltipHelper.tooltipHelper(String.format("%.2f", Math.min(calculateAttackSpeedBonus(soulsHarvested), 0.6)) + " ", true, null, colors[1]))
                        .append(Component.translatable("attribute.name.generic.attack_speed").withStyle(STYLE_DARK_GREEN.withBold(false)))
        );

        if (!BETTER_COMBAT_LOADED) {
            pTooltipComponents.add(Component.literal("+" + entityReachBonus + " ").withStyle(ChatFormatting.BLUE).append(Component.translatable("forge.entity_reach")));
        }
            pTooltipComponents.add(Component.literal(""));

    }

    @Override
    public <R extends BlockEntityWithoutLevelRenderer> void initGeo(Consumer<IClientItemExtensions> consumer, Class<R> rendererClass) {
        super.initGeo(consumer, BrutalityAutoFullbrightItemNoDepthRenderer.class);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            if (mainHandItem.getItem() instanceof FallenScythe) {
                int soulsHarvested = mainHandItem.getOrCreateTag().getInt(SOULS_HARVESTED);
                mainHandItem.getOrCreateTag().putInt(SOULS_HARVESTED, soulsHarvested + 1);

                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SOUL, event.getEntity().getX(), event.getEntity().getY() + event.getEntity().getBbHeight() / 2, event.getEntity().getZ(), 2, 0.25, 0.25, 0.25, 0);
                    serverLevel.playSound(null, event.getEntity().getOnPos(), SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 4F, 1F);
                }

                float baseAttackDamage = 3;
                float attackDamageBonus = calculateAttackDamageBonus(soulsHarvested);
                float newAttackDamage = baseAttackDamage + attackDamageBonus;
                newAttackDamage = Math.min(newAttackDamage, 18 - 1);

                float attackSpeedModifier = -3; // Base attack speed modifier
                float attackSpeedBonus = calculateAttackSpeedBonus(soulsHarvested);
                float newAttackSpeed = attackSpeedModifier + attackSpeedBonus;
                newAttackSpeed = Math.min(newAttackSpeed, -2.4F);

                replaceOrAddModifier(mainHandItem, Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE_UUID, newAttackDamage, EquipmentSlot.MAINHAND, AttributeModifier.Operation.ADDITION);
                replaceOrAddModifier(mainHandItem, Attributes.ATTACK_SPEED, BASE_ATTACK_SPEED_UUID, newAttackSpeed, EquipmentSlot.MAINHAND, AttributeModifier.Operation.ADDITION);
                replaceOrAddModifier(mainHandItem, ForgeMod.ENTITY_REACH.get(), BASE_ENTITY_INTERACTION_RANGE_UUID, entityReachBonus, EquipmentSlot.MAINHAND, AttributeModifier.Operation.ADDITION);

                // -2.5F = 0.6s = 1.66 attack speed
                // -2F = 0.45s = 2.22 attack speed
                // -3F = 0.9s = 1.11 attack speed

                // Capping at 500 souls in terms of functionality

                // This means that 0.5F = 0.15s
            }
        }
    }

    public static float calculateAttackDamageBonus(int soulsHarvested) {
        return ((float) soulsHarvested / 500 * 15);
    }

    public static float calculateAttackSpeedBonus(int soulsHarvested) {
        return (float) ((soulsHarvested / 500.0) * 0.6);
    }

    @Override
    public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return restrictEnchants(book, ALLOWED_ENCHANTMENTS);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return ALLOWED_ENCHANTMENTS.contains(enchantment);
    }

}

