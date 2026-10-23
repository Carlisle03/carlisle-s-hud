package carlisle.carlisleshud.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import carlisle.carlisleshud.client.config.ConfigScreen;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::create;
    }
}
