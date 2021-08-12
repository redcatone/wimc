package com.redcatone.wimc;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "wimc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CrateTooltip {
    public static void init() {
        MinecraftForge.EVENT_BUS.register(new CrateTooltip());
    }

    public static final ResourceLocation WIDGET_RESOURCE = new ResourceLocation("wimc", "textures/shulker_widget.png");

    @SubscribeEvent
    public static void renderTooltip(RenderTooltipEvent.PostText event) {
        if((Objects.requireNonNull(event.getStack().getItem().getRegistryName()).toString().equals("immersiveengineering:crate")
                || Objects.requireNonNull(event.getStack().getItem().getRegistryName()).toString().equals("immersiveengineering:reinforced_crate"))
                && event.getStack().hasTag()) {
            Minecraft mc = Minecraft.getInstance();
            MatrixStack matrix = event.getMatrixStack();

            CompoundNBT cmp = ItemNBTHelper.getCompound(event.getStack(), "inventory", true);
            if (cmp != null) {
                if(cmp.contains("LootTable"))
                    return;

                if (!cmp.contains("id", Constants.NBT.TAG_STRING)) {
                    cmp = cmp.copy();
                    cmp.putString("id", "minecraft:shulker_box");
                }

                TileEntity te = TileEntity.loadStatic(((BlockItem) event.getStack().getItem()).getBlock().defaultBlockState(), cmp);
                if (te != null) {
                    if(te instanceof LockableLootTileEntity)
                        ((LockableLootTileEntity) te).setLootTable(null, 0);

                    LazyOptional<IItemHandler> handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    handler.ifPresent((capability) -> {
                        int currentX = event.getX() - 5;
                        int currentY = event.getY() - 70;

                        int size = capability.getSlots();
                        int[] dims = { Math.min(size, 9), Math.max(size / 9, 1) };
                        for (int[] testAgainst : TARGET_RATIOS) {
                            if (testAgainst[0] * testAgainst[1] == size) {
                                dims = testAgainst;
                                break;
                            }
                        }

                        int texWidth = CORNER * 2 + EDGE * dims[0];

                        if (currentY < 0)
                            currentY = event.getY() + event.getLines().size() * 10 + 5;

                        int right = currentX + texWidth;
                        MainWindow window = mc.getWindow();

                        if (right > window.getGuiScaledWidth())
                            currentX -= (right - window.getGuiScaledWidth());

                        RenderSystem.pushMatrix();
                        RenderSystem.translatef(0, 0, 700);

                        int color = -1;

                        /*if (ImprovedTooltipsModule.shulkerBoxUseColors && ((BlockItem) currentBox.getItem()).getBlock() instanceof ShulkerBoxBlock) {
                            DyeColor dye = ((ShulkerBoxBlock) ((BlockItem) currentBox.getItem()).getBlock()).getColor();
                            if (dye != null) {
                                float[] colorComponents = dye.getColorComponentValues();
                                color = ((int) (colorComponents[0] * 255) << 16) |
                                        ((int) (colorComponents[1] * 255) << 8) |
                                        (int) (colorComponents[2] * 255);
                            }
                        }*/

                        renderTooltipBackground(mc, matrix, currentX, currentY, dims[0], dims[1], color);

                        ItemRenderer render = mc.getItemRenderer();

                        ListNBT itemList = Objects.requireNonNull(event.getStack().getTag()).getList("inventory", 10);
                        for (int i = 0; i < itemList.size(); i++) {
                            CompoundNBT itemTag = itemList.getCompound(i);
                            ItemStack itemstack = new ItemStack(null);
                            int slotNum = 0;

                            if (itemTag.contains("Slot", 1)) {
                                slotNum = itemTag.getByte("Slot");
                                itemstack = ItemStack.of(itemTag);
                            }

                            int xp = currentX + 6 + (slotNum % 9) * 18;
                            int yp = currentY + 6 + (slotNum / 9) * 18;

                            if (!itemstack.isEmpty()) {
                                render.renderGuiItem(itemstack, xp, yp);
                                render.renderGuiItemDecorations(mc.font, itemstack, xp, yp);
                            }
                        }

                        RenderSystem.popMatrix();
                    });

                }
            }
        }
    }

    private static final int[][] TARGET_RATIOS = new int[][] {
            { 1, 1 },
            { 9, 3 },
            { 9, 5 },
            { 9, 6 },
            { 9, 8 },
            { 9, 9 },
            { 12, 9 }
    };

    private static final int CORNER = 5;
    private static final int BUFFER = 1;
    private static final int EDGE = 18;

    public static void renderTooltipBackground(Minecraft mc, MatrixStack matrix, int x, int y, int width, int height, int color) {
        mc.getTextureManager().bind(WIDGET_RESOURCE);
        RenderSystem.color3f(((color & 0xFF0000) >> 16) / 255f,
                ((color & 0x00FF00) >> 8) / 255f,
                (color & 0x0000FF) / 255f);

        AbstractGui.blit(matrix, x, y,
                0, 0,
                CORNER, CORNER, 256, 256);
        AbstractGui.blit(matrix, x + CORNER + EDGE * width, y + CORNER + EDGE * height,
                CORNER + BUFFER + EDGE + BUFFER, CORNER + BUFFER + EDGE + BUFFER,
                CORNER, CORNER, 256, 256);
        AbstractGui.blit(matrix, x + CORNER + EDGE * width, y,
                CORNER + BUFFER + EDGE + BUFFER, 0,
                CORNER, CORNER, 256, 256);
        AbstractGui.blit(matrix, x, y + CORNER + EDGE * height,
                0, CORNER + BUFFER + EDGE + BUFFER,
                CORNER, CORNER, 256, 256);
        for (int row = 0; row < height; row++) {
            AbstractGui.blit(matrix, x, y + CORNER + EDGE * row,
                    0, CORNER + BUFFER,
                    CORNER, EDGE, 256, 256);
            AbstractGui.blit(matrix, x + CORNER + EDGE * width, y + CORNER + EDGE * row,
                    CORNER + BUFFER + EDGE + BUFFER, CORNER + BUFFER,
                    CORNER, EDGE, 256, 256);
            for (int col = 0; col < width; col++) {
                if (row == 0) {
                    AbstractGui.blit(matrix, x + CORNER + EDGE * col, y,
                            CORNER + BUFFER, 0,
                            EDGE, CORNER, 256, 256);
                    AbstractGui.blit(matrix, x + CORNER + EDGE * col, y + CORNER + EDGE * height,
                            CORNER + BUFFER, CORNER + BUFFER + EDGE + BUFFER,
                            EDGE, CORNER, 256, 256);
                }

                AbstractGui.blit(matrix, x + CORNER + EDGE * col, y + CORNER + EDGE * row,
                        CORNER + BUFFER, CORNER + BUFFER,
                        EDGE, EDGE, 256, 256);
            }
        }

        RenderSystem.color3f(1F, 1F, 1F);
    }
}
