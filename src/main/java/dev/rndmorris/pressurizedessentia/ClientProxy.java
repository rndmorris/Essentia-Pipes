package dev.rndmorris.pressurizedessentia;

import dev.rndmorris.pressurizedessentia.client.CreativeTab;

public class ClientProxy extends CommonProxy {

    private final CreativeTab creativeTab;

    public ClientProxy() {
        this.creativeTab = new CreativeTab();
    }

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

    @Override
    public CreativeTab getCreativeTab() {
        return creativeTab;
    }
}
