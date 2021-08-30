package hardcorequesting.fabric.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import hardcorequesting.common.HardcoreQuestingCore;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.locale.Language;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

@Mixin(Language.class)
public class MixinLanguage {
    @Shadow @Final private static Logger LOGGER;
    
    @Inject(method = "loadDefault",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void loadDefault(CallbackInfoReturnable<Language> cir, ImmutableMap.Builder<String, String> builder, BiConsumer<String, String> biConsumer) {
        String s = HardcoreQuestingCore.ID;
        FabricLoader.getInstance().getModContainer(s).ifPresent(modContainer -> {
            try {
                Path path = modContainer.getPath("assets/" + HardcoreQuestingCore.ID + "/lang/en_us.json");
                try (InputStream inputStream = Files.newInputStream(path)) {
                    Language.loadFromJson(inputStream, biConsumer);
                }
            } catch (JsonParseException | IOException var15) {
                LOGGER.error("Couldn't read strings from /assets/" + HardcoreQuestingCore.ID + "/lang/en_us.json", var15);
            }
        });
        
    }
}
