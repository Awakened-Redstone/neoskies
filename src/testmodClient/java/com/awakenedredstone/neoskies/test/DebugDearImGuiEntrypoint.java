package com.awakenedredstone.neoskies.test;

import dev.deftu.imgui.DearImGuiEntrypoint;
import dev.deftu.imgui.ImGuiRenderer;

public class DebugDearImGuiEntrypoint implements DearImGuiEntrypoint {
    @Override
    public ImGuiRenderer createRenderer() {
        return new DebugImGuiRenderer();
    }

    @Override
    public void render() {
        // Render ImGui here
    }
}
