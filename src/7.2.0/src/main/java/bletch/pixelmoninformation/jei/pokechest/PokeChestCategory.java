package bletch.pixelmoninformation.jei.pokechest;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import bletch.pixelmoninformation.core.ModDetails;
import bletch.pixelmoninformation.jei.BlankRecipeCategory;
import bletch.pixelmoninformation.jei.PixelmonJei;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
public class PokeChestCategory extends BlankRecipeCategory<PokeChestWrapper> {

	public static final String CATEGORY = "pokechest";
	
	private static final ResourceLocation TEXTURE_LOCOCATION_ICON = new ResourceLocation(ModDetails.MOD_ID, "textures/gui/jeicategorytabs.png");
	private static final ResourceLocation TEXTURE_LOCOCATION_GUI = new ResourceLocation(ModDetails.MOD_ID, "textures/gui/gui_" + CATEGORY + ".png");

    private static final int LEFT = 3;
    private static final int TOP = 31;
    private static final int COLUMNS = 9;
    private static final int ROWS = 5;
    
    public static final int ITEMS_PER_PAGE = ROWS * COLUMNS;
    private static final int ITEM_HEIGHT = 18;
    private static final int ITEM_WIDTH = 18;

    public PokeChestCategory() {
    	super(CATEGORY, 
    			PixelmonJei.getGuiHelper().createDrawable(TEXTURE_LOCOCATION_ICON, 48, 0, 16, 16),
    			PixelmonJei.getGuiHelper().createDrawable(TEXTURE_LOCOCATION_GUI, 0, 0, 169, 138));
    }
    
    @Override
    public void drawExtras(Minecraft minecraft) {
    }    
    
	@SuppressWarnings("unchecked")
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, PokeChestWrapper recipeWrapper, IIngredients ingredients) {

		IGuiItemStackGroup guiStacks = recipeLayout.getItemStacks();
        int x = LEFT;
        int y = TOP;	
        int slotIndex = 0;
        
		// Initialize the input slots
		guiStacks.init(slotIndex++, true, 6, 6);
        
        // Initialize the output slots
        for (int rowIndex = 0; rowIndex < ROWS; rowIndex++) {
        	for (int columnIndex = 0; columnIndex < COLUMNS; columnIndex++) {
        		guiStacks.init(slotIndex++, false, x, y);

        		x += ITEM_WIDTH;
        	}
        	
            x = LEFT;
            y += ITEM_HEIGHT;
        }
                
        recipeWrapper.setFocus((IFocus<ItemStack>)recipeLayout.getFocus());
		slotIndex = 0;

		// populate the input slots
		guiStacks.set(slotIndex++, ingredients.getInputs(VanillaTypes.ITEM).get(0));
		
		// populate the output slots
		int startIndex = (recipeWrapper.getPageIndex() - 1) * ITEMS_PER_PAGE;
		List<ItemStack> items = recipeWrapper.getItems(startIndex, ITEMS_PER_PAGE);
		
		for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
			guiStacks.set(slotIndex++, items.get(itemIndex));
		}
        
		recipeWrapper.updateButtonsState();
		
        guiStacks.addTooltipCallback(recipeWrapper);
    }
	
}
