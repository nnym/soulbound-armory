package user11681.soulboundarmory.client.gui;

import net.minecraft.text.Text;
import user11681.cell.client.gui.widget.Slider;
import user11681.soulboundarmory.config.Configuration;
import user11681.soulboundarmory.text.Translation;

public class RGBASlider extends Slider {
    protected static final Configuration.Client.Colors colors = Configuration.instance().client.colors;

    public final int id;

    protected final Text text;

    protected int componentValue;

    public RGBASlider(int id, Text text) {
        this.min(0).max(255);

        this.text = text;
        this.id = id;

        this.value(colors.get(id));
        this.applyValue();
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(new Translation("%s: %s", this.text, this.componentValue));
    }

    @Override
    public void applyValue() {
        this.componentValue = (int) (0xFF * this.value);

        colors.set(this.id, this.componentValue);
    }
}
