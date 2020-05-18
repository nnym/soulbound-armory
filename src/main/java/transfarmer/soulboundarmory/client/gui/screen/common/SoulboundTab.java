package transfarmer.soulboundarmory.client.gui.screen.common;

import com.mojang.blaze3d.systems.RenderSystem;
import nerdhub.cardinal.components.api.component.Component;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import transfarmer.soulboundarmory.MainClient;
import transfarmer.soulboundarmory.client.gui.ExtendedButtonWidget;
import transfarmer.soulboundarmory.client.gui.XPBarGUI;
import transfarmer.soulboundarmory.client.gui.XPBarGUI.Style;
import transfarmer.soulboundarmory.client.gui.RGBASlider;
import transfarmer.soulboundarmory.client.i18n.Mappings;
import transfarmer.soulboundarmory.component.soulbound.item.ISoulboundItemComponent;
import transfarmer.soulboundarmory.config.MainConfig;
import transfarmer.soulboundarmory.network.Packets;
import transfarmer.soulboundarmory.network.common.ExtendedPacketBuffer;
import transfarmer.soulboundarmory.statistics.Category;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static transfarmer.soulboundarmory.statistics.StatisticType.LEVEL;
import static transfarmer.soulboundarmory.statistics.StatisticType.XP;

@Environment(EnvType.CLIENT)
public abstract class SoulboundTab extends ScreenTab {
    protected static final NumberFormat FORMAT = DecimalFormat.getInstance();

    protected final PlayerEntity player;
    protected final List<AbstractButtonWidget> options;
    protected final List<RGBASlider> rgbaSliders;
    protected final ISoulboundItemComponent<? extends Component> component;

    protected XPBarGUI xpBar;
    protected ExtendedButtonWidget styleButton;
    protected int slot;

    public SoulboundTab(final Text title, final ISoulboundItemComponent<? extends Component> component,
                        final List<ScreenTab> tabs) {
        super(title, tabs);

        this.player = MainClient.PLAYER;
        this.component = component;
        this.options = new ArrayList<>(5);
        this.rgbaSliders = new ArrayList<>(4);
    }

    @Override
    public void init() {
        this.displayTabs = this.component != null;

        if (this.displayTabs) {
            this.initOptions();

            this.component.setCurrentTab(this.index);
            this.slot = this.player.inventory.getSlotWithStack(this.component.getValidEquippedStack());

            final Text text = this.slot != component.getBoundSlot()
                    ? Mappings.MENU_BUTTON_BIND
                    : Mappings.MENU_BUTTON_UNBIND;
            final int width = Math.max(this.button.getWidth(), TEXT_RENDERER.getStringWidth(text.toString()) + 8);
            final int x = this.button.endX - width;

            this.addButton(new ButtonWidget(x, this.height - this.height / 16 - 20, width, 20, text.toString(), this.bindSlotAction()));

            if (this.displayXPBar()) {
                this.xpBar = new XPBarGUI(this.component);
            }
        }

        super.init();
    }

    protected void initOptions() {
        if (MainConfig.instance().displayOptions && this.displayXPBar()) {
            this.options.add(this.addButton(this.colorSlider(0, MainConfig.instance().colors.red, Mappings.RED)));
            this.options.add(this.addButton(this.colorSlider(1, MainConfig.instance().colors.green, Mappings.GREEN)));
            this.options.add(this.addButton(this.colorSlider(2, MainConfig.instance().colors.blue, Mappings.BLUE)));
            this.options.add(this.addButton(this.colorSlider(3, MainConfig.instance().colors.alpha, Mappings.ALPHA)));
            this.options.add(this.addButton(this.styleButton = this.optionButton(4, String.format("%s: %s", Mappings.XP_BAR_STYLE, MainConfig.instance().style), this.cycleStyleAction(1), this.cycleStyleAction(-1))));
        }
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        this.addBlitOffset(-500);
        this.renderBackground();
        this.addBlitOffset(500);

        super.render(mouseX, mouseY, partialTicks);

        if (this.displayXPBar()) {
            this.drawXPBar(mouseX, mouseY);
        }
    }

    protected void drawXPBar(final int mouseX, final int mouseY) {
        final int xp = component.getDatum(XP);

        this.xpBar.drawXPBar(this.getXPBarX(), this.getXPBarY(), 182);

        if (this.isMouseOverLevel(mouseX, mouseY) && MainConfig.instance().maxLevel >= 0) {
            this.renderTooltip(String.format("%d/%d", this.component.getDatum(LEVEL), MainConfig.instance().maxLevel), mouseX, mouseY);
        } else if (this.isMouseOverXPBar(mouseX, mouseY)) {
            this.renderTooltip(this.component.canLevelUp()
                    ? String.format("%d/%d", xp, component.getNextLevelXP())
                    : String.format("%d", xp), mouseX, mouseY);
        }

        RenderSystem.disableLighting();
    }

    protected boolean displayXPBar() {
        return this.component != null;
    }

    protected boolean isMouseOverXPBar(final double mouseX, final double mouseY) {
        final double barX = this.getXPBarX();
        final double barY = this.getXPBarY();

        return this.displayXPBar() && mouseX >= barX && mouseX <= barX + 182 && mouseY >= barY && mouseY <= barY + 4;
    }

    protected int getXPBarY() {
        return this.height - 29;
    }

    protected int getXPBarX() {
        return (this.width - 182) / 2;
    }

    protected boolean isMouseOverLevel(final int mouseX, final int mouseY) {
        final String levelString = "" + this.component.getDatum(LEVEL);

        final int levelLeftX = (this.width - TEXT_RENDERER.getStringWidth(levelString)) / 2;
        final int levelTopY = height - 35;

        return mouseX >= levelLeftX && mouseX <= levelLeftX + TEXT_RENDERER.getStringWidth(levelString)
                && mouseY >= levelTopY && mouseY <= levelTopY + TEXT_RENDERER.fontHeight;
    }

    protected RGBASlider sliderMousedOver(final double mouseX, final double mouseY) {
        for (int slider = 0; slider < 4; slider++) {
            if (mouseX >= this.getOptionX() && mouseX <= this.getOptionX() + 100
                    && mouseY >= this.getOptionY(slider) && mouseY <= this.getOptionY(slider) + 20) {
                return this.rgbaSliders.get(0);
            }
        }

        return null;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        super.mouseClicked(mouseX, mouseY, button);

        if (this.isMouseOverXPBar(mouseX, mouseY)) {
            if (!MainConfig.instance().displayOptions) {
                this.buttons.addAll(this.options);
                MainConfig.instance().displayOptions = true;
            } else {
                this.buttons.removeAll(this.options);
                MainConfig.instance().displayOptions = false;
            }

//            ClientConfig.instance().save();
            this.refresh();

            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (keyCode == MainClient.GUI_KEY_BINDING.getBoundKey().getKeyCode()) {
            this.onClose();
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double dWheel) {
        final RGBASlider slider = this.sliderMousedOver(x, y);

        if (slider != null) {
            final TranslatableText text = slider.getText();
//            final int value = MathHelper.clamp(MainConfig.instance()., 0, 255);
//            MainConfig.instance().setColor(text, value);
//
//            slider.setValue(value);
        }

        return super.mouseScrolled(x, y, dWheel);
    }

    protected void cycleStyle(final int change) {
        int index = (Style.STYLES.indexOf(MainConfig.instance().style) + change) % Style.AMOUNT;

        if (index < 0) {
            this.cycleStyle(Style.AMOUNT + index);
        } else {
            MainConfig.instance().style = Style.STYLES.get(index);
            this.refresh();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    public ButtonWidget centeredButton(final int y, final int buttonWidth, final String text,
                                       final PressAction action) {
        return new ButtonWidget((this.width - buttonWidth) / 2, y, buttonWidth, 20, text, action);
    }

    public ButtonWidget squareButton(final int x, final int y, final String text, final PressAction action) {
        return new ButtonWidget(x - 10, y - 10, 20, 20, text, action);
    }

    public ButtonWidget resetButton(final PressAction action) {
        return new ButtonWidget(this.width - this.width / 24 - 112, this.height - this.height / 16 - 20, 112, 20, Mappings.MENU_BUTTON_RESET.toString(), action);
    }

    public ExtendedButtonWidget optionButton(final int row, final String text, final PressAction primaryAction,
                                             final PressAction secondaryAction) {
        return new ExtendedButtonWidget(this.getOptionX(), this.getOptionY(row), 100, 20, text, primaryAction, secondaryAction);
    }

    public RGBASlider colorSlider(final int row, final double currentValue, final TranslatableText text) {
        return new RGBASlider(this.getOptionX(), this.getOptionY(row), 100, 20, currentValue, text);
    }

    public ButtonWidget[] addPointButtons(final int rows, final int points, final PressAction action) {
        final ButtonWidget[] buttons = new ButtonWidget[rows];
        final int start = (this.height - (rows - 1) * this.height / 16) / 2;

        for (int row = 0; row < rows; row++) {
            buttons[row] = squareButton((this.width + 162) / 2, start + row * this.height / 16 + 4, "+", action);
            buttons[row].active = points > 0;
        }

        return buttons;
    }

    public ButtonWidget[] removePointButtons(final int rows, final PressAction action) {
        final ButtonWidget[] buttons = new ButtonWidget[rows];
        final int start = (this.height - (rows - 1) * this.height / 16) / 2;

        for (int row = 0; row < rows; row++) {
            buttons[row] = this.squareButton((this.width + 162) / 2 - 20, start + row * this.height / 16 + 4, "-", action);
        }

        return buttons;
    }

    public int getOptionX() {
        return Math.round(this.width * (1 - 1 / 24F)) - 100;
    }

    public int getOptionY(final int row) {
        return this.height / 16 + Math.max(this.height / 16 * row, 30 * row);
    }

    public void drawMiddleAttribute(final String format, final double value, final int row, final int rows) {
        TEXT_RENDERER.draw(String.format(format, FORMAT.format(value)), (this.width - 182) / 2F, this.getHeight(rows, row), 0xFFFFFF);
    }

    protected PressAction bindSlotAction() {
        return (final ButtonWidget button) ->
                MainClient.PACKET_REGISTRY.sendToServer(Packets.C2S_BIND_SLOT, new ExtendedPacketBuffer(this.component).writeInt(this.slot));
    }

    protected PressAction cycleStyleAction(final int change) {
        return (final ButtonWidget button) -> this.cycleStyle(change);
    }

    protected PressAction resetAction(final Category category) {
        return (final ButtonWidget button) ->
                MainClient.PACKET_REGISTRY.sendToServer(Packets.C2S_RESET, new ExtendedPacketBuffer(this.component).writeString(category.toString()));
    }
}