package soulboundarmory.module.gui.screen;

import soulboundarmory.module.gui.widget.Widget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public abstract class ScreenWidget<T extends ScreenWidget<T>> extends Widget<T> {
    public Text title = LiteralText.EMPTY;
    public ScreenDelegate screen;

    public ScreenWidget() {
        this.z(100);
    }

    public T title(Text title) {
        this.title = title;

        return (T) this;
    }

    public Screen asScreen() {
        return this.screen == null ? this.screen = new ScreenDelegate(this.title, this) : this.screen;
    }

    public void open() {
        client.setScreen(this.asScreen());
    }

    public boolean shouldPause() {
        return false;
    }

    public boolean shouldClose(int keyCode, int scanCode, int modifiers) {
        return keyCode == GLFW.GLFW_KEY_ESCAPE;
    }

    public void close() {
        client.setScreen(this.screen.parent);
    }

    @Override
    protected void render() {
        this.renderBackground(this.matrixes);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        if (this.shouldClose(keyCode, scanCode, modifiers)) {
            this.close();

            return true;
        }

        return false;
    }
}