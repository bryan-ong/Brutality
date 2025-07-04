package net.goo.brutality.client.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public interface ArmaGeoEntity extends GeoEntity {

    String geoIdentifier();

    default String model() {
        return geoIdentifier();
    }

    default String texture() {
        return model();
    }

    @Override
    default AnimatableInstanceCache getAnimatableInstanceCache() {
        return GeckoLibUtil.createInstanceCache(this, true);
    }

    GeoAnimatable cacheItem();

    @OnlyIn(Dist.CLIENT)
    default <T extends Entity & ArmaGeoEntity, R extends GeoEntityRenderer<T>> void initGeo(Consumer<EntityRendererProvider<T>> consumer, Class<R> rendererClass) {
        consumer.accept(context -> {
            try {
                return rendererClass.getDeclaredConstructor(EntityRendererProvider.Context.class).newInstance(context);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate renderer: " + rendererClass.getSimpleName(), e);
            }
        });
    }

    @Override
    default void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, (state) ->
                state.setAndContinue(RawAnimation.begin().thenLoop("idle")))
        );
    }


}
