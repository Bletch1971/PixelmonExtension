package bletch.pixelmoninformation.jei.shopkeeper;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.pixelmonmod.pixelmon.entities.npcs.registry.BaseShopItem;
import com.pixelmonmod.pixelmon.entities.npcs.registry.ShopItem;
import bletch.pixelmoninformation.core.ModDetails;
import bletch.pixelmoninformation.jei.PixelmonJei;
import bletch.pixelmoninformation.utils.Font;
import bletch.pixelmoninformation.utils.TextUtils;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;

@ParametersAreNonnullByDefault
public class ShopKeeperWrapper implements IRecipeWrapper, ITooltipCallback<ItemStack> {

	private static final int buttonWidth = 13;
	private static final int buttonHeight = 13;
	
	private final ShopKeeperEntry entry;
	private IFocus<ItemStack> focus;
    private GuiButton buttonPrev;
    private GuiButton buttonNext;
    private int pageIndex;
    
	public ShopKeeperWrapper(ShopKeeperEntry entry) {
        this.entry = entry;
        this.focus = null;
        this.pageIndex = 1;
        
		GuiHelper guiHelper = (GuiHelper) PixelmonJei.getGuiHelper();
		if (guiHelper != null) {
			IDrawableStatic arrowPrevious = guiHelper.getArrowPrevious();
			IDrawableStatic arrowNext = guiHelper.getArrowNext();
			
			if (arrowPrevious != null && arrowNext != null) {
				this.buttonPrev = new GuiIconButtonSmall(0, 3, 104, buttonWidth, buttonHeight, arrowPrevious);
	        	this.buttonNext = new GuiIconButtonSmall(1, 153, 104, buttonWidth, buttonHeight, arrowNext);
	        	updateButtonsState();
			}
        }
	}

	public ShopKeeperEntry getEntry() {
		return this.entry;
	}

    public IFocus<ItemStack> getFocus() {
    	return this.focus;
    }
    
    public int getPageIndex() {
    	return this.pageIndex;
    }
    
    public IFocus<ItemStack> setFocus(IFocus<ItemStack> focus) {
    	// check if the focus is changing
    	if (!(this.focus == null && focus == null || this.focus != null && focus != null && this.focus.equals(focus))) {
    		// focus has changed, reset the page index
    		this.pageIndex = 1;
    	}

    	return this.focus = focus;
    }
    
    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setOutputs(VanillaTypes.ITEM, this.entry.getItemStacks(null));
    }

    public int amountOfItems() {
    	return amountOfItems(this.focus);
    }
    
    public int amountOfItems(@Nullable IFocus<ItemStack> focus) {
    	return getItems(focus).size();
    }

    public List<ItemStack> getItems() {
        return getItems(this.focus);
    }
    
    public List<ItemStack> getItems(@Nullable IFocus<ItemStack> focus) {
        List<ItemStack> stacks = this.entry.getItemStacks(focus);
        stacks.removeIf(Objects::isNull);
        return stacks;
    }
    
    public List<ItemStack> getItems(int startIndex, int count) {
    	return getItems(this.focus, startIndex, count);
    }
    
    public List<ItemStack> getItems(@Nullable IFocus<ItemStack> focus, int startIndex, int count) {
        List<ItemStack> stacks = getItems(focus);
        
        if (stacks.size() <= startIndex) {
        	return new ArrayList<ItemStack>(0);
        }
        if (stacks.size() <= startIndex + count) {
        	count = stacks.size() - startIndex;
        }

    	return stacks.subList(startIndex, startIndex + count);
    }
    
    public void updateButtonsState() {
    	double temp = (double)amountOfItems() / (double)ShopKeeperCategory.ITEMS_PER_PAGE;
    	int pages = (int)Math.ceil(temp);
    	
    	if (pages > 1) {
    		this.buttonPrev.visible = true;
    		this.buttonNext.visible = true;
    	} else {
    		this.buttonPrev.visible = false;
    		this.buttonNext.visible = false;
    	}
    }
    
    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        Font.normal.printLeft(this.entry.getLocalisedName(), 2, 2);
        
    	double temp = (double)amountOfItems() / (double)ShopKeeperCategory.ITEMS_PER_PAGE;
    	int pages = (int)Math.ceil(temp);
    	
    	if (pages > 1) {
    		// if there is 2 or more pages, then draw the darker background with the page details
    		Gui.drawRect(3, 104, 166, 117, 0x40FFFFFF);
    		Font.normal.printCentered(this.pageIndex + "/" + pages, 84, 107, Color.WHITE.getRGB(), true);
    	}
    	
        this.buttonPrev.drawButton(minecraft, mouseX, mouseY, 1f);
        this.buttonNext.drawButton(minecraft, mouseX, mouseY, 1f);
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
    	
    	double temp = (double)amountOfItems() / (double)ShopKeeperCategory.ITEMS_PER_PAGE;
    	int pages = (int)Math.ceil(temp);
    	
    	if (this.buttonPrev.mousePressed(minecraft, mouseX, mouseY)) {
    		this.buttonPrev.playPressSound(minecraft.getSoundHandler());
    		
    		try {
	    		RecipesGui recipesGui = (RecipesGui)PixelmonJei.getJeiRuntime().getRecipesGui();
	    		if (recipesGui != null) {
	        		this.pageIndex--;
	        		if (this.pageIndex <= 0) {
	        			this.pageIndex = pages;
	        		}
	        		
	        		recipesGui.onStateChange();
	        		updateButtonsState();
	        		
	        		return true;
	    		}
    		}
    		catch (Exception ex) {
    			ModDetails.MOD_LOGGER.error(ex.getMessage());
    		}	    		
    	}

    	if (this.buttonNext.mousePressed(minecraft, mouseX, mouseY)) {
    		this.buttonNext.playPressSound(minecraft.getSoundHandler());

    		try {
        		RecipesGui recipesGui = (RecipesGui)PixelmonJei.getJeiRuntime().getRecipesGui();
        		if (recipesGui != null) {
            		this.pageIndex++;
            		if (this.pageIndex > pages) {
            			this.pageIndex = 1;
            		}
            		
            		recipesGui.onStateChange();
            		updateButtonsState();
            		
            		return true;
        		}    		
    		}
    		catch (Exception ex) {
    			ModDetails.MOD_LOGGER.error(ex.getMessage());
    		}
    	}
    	
        return false;
    }
    
    @Override
    public void onTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
    	
    	ShopItem item = this.entry.getShopItem(ingredient);

    	if (item != null) {
    		try {
    			Field buyField = BaseShopItem.class.getDeclaredField("buy");
    			if (!buyField.isAccessible()) {
    				buyField.setAccessible(true);
    			}    			
    			
    			int buyValue = buyField.getInt(item.getBaseItem());
				if (buyValue != -1) {
					tooltip.add(String.format(TextUtils.translate("gui.shopitem.buy"), buyValue));
				}
    		}
    		catch (Exception ex) {
    			ModDetails.MOD_LOGGER.error(ex.getMessage());
    		}
		
    		try {
    			Field sellField = BaseShopItem.class.getDeclaredField("sell");
    			if (!sellField.isAccessible()) {
    				sellField.setAccessible(true);    				
    			}    			
    			
    			int sellValue = sellField.getInt(item.getBaseItem());
				if (sellValue != -1) {
					tooltip.add(String.format(TextUtils.translate("gui.shopitem.sell"), sellValue));
				}
    		}
    		catch (Exception ex) {
    			ModDetails.MOD_LOGGER.error(ex.getMessage());
    		}

    		tooltip.add(TextUtils.translate("gui.shopitem.canvary") + " " + (item.canPriceVary() ? TextUtils.SYMBOL_GREENTICK : TextUtils.SYMBOL_REDCROSS));
			if (item.getRarity() != 1) {
				tooltip.add(String.format(TextUtils.translate("gui.shopitem.rarity"), (int)(item.getRarity() * 100)));
			}
    	}
    }
    
}
