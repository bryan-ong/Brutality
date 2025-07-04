package net.goo.brutality.registry;

import net.goo.brutality.Brutality;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Brutality.MOD_ID);

    public static final RegistryObject<SoundEvent> LEAF_BLOWER_ON = registerSoundEvents("leaf_blower_on");
    public static final RegistryObject<SoundEvent> LEAF_BLOWER_ACTIVE = registerSoundEvents("leaf_blower_active");
    public static final RegistryObject<SoundEvent> LEAF_BLOWER_OFF = registerSoundEvents("leaf_blower_off");

    public static final List<RegistryObject<SoundEvent>> JACKPOT_SOUNDS = List.of(
            registerSoundEvents("jackpot_sound_1"),
            registerSoundEvents("jackpot_sound_2"),
            registerSoundEvents("jackpot_sound_3"),
            registerSoundEvents("jackpot_sound_4")
    );

    public static final List<RegistryObject<SoundEvent>> MURASAMA = List.of(
            registerSoundEvents("murasama_1"),
            registerSoundEvents("murasama_2"),
            registerSoundEvents("murasama_3")
    );

    public static final List<RegistryObject<SoundEvent>> SUPERNOVA = List.of(
            registerSoundEvents("supernova_1"),
            registerSoundEvents("supernova_2"),
            registerSoundEvents("supernova_3")
    );

    public static final List<RegistryObject<SoundEvent>> SPATULA = List.of(
            registerSoundEvents("spatula_flip_1"),
            registerSoundEvents("spatula_flip_2"),
            registerSoundEvents("spatula_flip_3"),
            registerSoundEvents("spatula_flip_4"),
            registerSoundEvents("spatula_flip_5")
    );

    public static final List<RegistryObject<SoundEvent>> PUNCHES = List.of(
            registerSoundEvents("epic_punch_1"),
            registerSoundEvents("epic_punch_2"),
            registerSoundEvents("epic_punch_3")
    );

    public static final List<RegistryObject<SoundEvent>> UMBRAL_DASH = List.of(
            registerSoundEvents("umbral_dash_1"),
            registerSoundEvents("umbral_dash_2")
    );

    public static final List<RegistryObject<SoundEvent>> DARKIN_BLADE = List.of(
            registerSoundEvents("darkin_blade_1"),
            registerSoundEvents("darkin_blade_2"),
            registerSoundEvents("darkin_blade_3")
    );

    public static final RegistryObject<SoundEvent> DEATHBRINGER_STANCE_READY = registerSoundEvents("deathbringer_stance_ready");
    public static final RegistryObject<SoundEvent> DEATHBRINGER_STANCE_HIT = registerSoundEvents("deathbringer_stance_hit");


    public static final RegistryObject<SoundEvent> EVENT_HORIZON_DISLODGE = registerSoundEvents("event_horizon_dislodge");
    public static final RegistryObject<SoundEvent> EVENT_HORIZON_RETURN = registerSoundEvents("event_horizon_return");


    public static final RegistryObject<SoundEvent> BASS_BOOM = registerSoundEvents("bass_boom");

    public static final RegistryObject<SoundEvent> KNIFE_BLOCK = registerSoundEvents("knife_block");
    public static final RegistryObject<SoundEvent> SQUELCH = registerSoundEvents("squelch");
    public static final RegistryObject<SoundEvent> FRYING_PAN_HIT = registerSoundEvents("frying_pan_hit");

    public static final RegistryObject<SoundEvent> DULL_KNIFE_SWING = registerSoundEvents("dull_knife_swing");
    public static final RegistryObject<SoundEvent> DULL_KNIFE_STAB = registerSoundEvents("dull_knife_stab");
    public static final RegistryObject<SoundEvent> DULL_KNIFE_CRIT = registerSoundEvents("dull_knife_crit");
    public static final RegistryObject<SoundEvent> DULL_KNIFE_ABILITY = registerSoundEvents("dull_knife_ability");


    public static final RegistryObject<SoundEvent> TERRA_BLADE_USE = registerSoundEvents("terra_blade_use");
    public static final RegistryObject<SoundEvent> ICE_WAVE = registerSoundEvents("ice_wave");
    public static final RegistryObject<SoundEvent> BIG_EXPLOSION = registerSoundEvents("big_explosion");
    public static final RegistryObject<SoundEvent> BIGGER_EXPLOSION = registerSoundEvents("bigger_explosion");
    public static final RegistryObject<SoundEvent> WINGS_FLAP = registerSoundEvents("wings_flap");

    private static RegistryObject<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Brutality.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
