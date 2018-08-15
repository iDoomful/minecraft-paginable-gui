// // // // // // // // // // // // // // // // // // // // //
//                                                          //
//  !! There will be better documentation in the future  !! //
//                                                          //
// // // // // // // // // // // // // // // // // // // // //

import java.util.List;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class Paginable {
	private int page = 1;
	private int currentIndex;
	
  // This method must return the list of ItemStack that will compose your pages
	protected abstract List<ItemStack> bodyList();
  
  // If you want your items to be generated in the middle of the inventory for example,
  // specify the slots the iteration will ignore while creating the items for the page
	protected abstract int[] skippingPoints();
  
  // This method must return the maximum of items that can be generated per page
	protected abstract int itemsPerPage();
  
  // This method must return the ItemStack that represents your next page button
	protected abstract ItemStack createNextButton();
  
  // This method must return the ItemStack that represents your previous page button
	protected abstract ItemStack createPreviousButton();
  
  // This method must return the ItemStack that was in place before the next or previous button are generated
  // Method used inside the updateButtons() method
	protected abstract ItemStack createItemBeforeReplacement();
  
  // This method must return the slot(s) the next page buttons will be placed at
	protected abstract int[] nextButtonSlots();
  
  // This method must return the slot(s) the previous page buttons will be placed at
	protected abstract int[] previousButtonSlots();
  
  // This method must return the inventory where the pagination takes place
	protected abstract Inventory getInventory();
  
  // Gets the current page the inventory is at
	public int getPage() {
		return page;
	}
	
  // The page index will advance
	public void nextPage() { 
    isNextPage(true); 
  }
  
  // The page index will descend
	public void previousPage() {
		isNextPage(false);
	}
	
  // The first page will get created inside the inventory
  // This method must be called before you open the inventory
	protected final void createFirstPage() {
		int atSlot = 0;
		
		listIteration: for (int index = 0; index < itemsPerPage(); index++) {
			inventoryIteration: for (int slot = atSlot; slot < getInventory().getSize(); slot++) {
				for(int point : skippingPoints()) {
					if(point == slot) {
						atSlot += 1;
						continue inventoryIteration;
					}
				}
				
				if(index > bodyList().size() - 1) break listIteration;
				currentIndex += 1;
				
				getInventory().setItem(slot, bodyList().get(index));
				atSlot += 1;
				
				continue listIteration;
			}
		}
		updateButtons();
	}
	
  // The core method responsible for the page turning mechanic
  // It comes with 3 modes:
  // - true: the page number increases, turning to the next page
  // - false: the page number decreases, turning to the previous page
  // - null: the page number will remain still, allowing for a page refresh
	private void isNextPage(Boolean mode) {
	    if(mode != null) {
            if(mode) {
                page += 1;
            } else {
                page -= 1;
                currentIndex -= itemsPerPage() * 2;
            }
        }

		final int listStartPoint = getPage() * itemsPerPage() - itemsPerPage();
		final int listEndPoint = getPage() * itemsPerPage() - 1;
		
		final int inventoryStartPoint = getInventoryStartingPoint(skippingPoints());
		final int inventoryEndPoint = skippingPoints()[skippingPoints().length - 1] - 1;
		
		int inventoryIndex = -1;
		
		listIteration: for (int index = listStartPoint; index < listEndPoint + 1; index++) {
			if(inventoryIndex == -1) inventoryIndex = inventoryStartPoint;

			if(inventoryIndex < inventoryEndPoint + 1) {
				for (int point : skippingPoints()) if (point == inventoryIndex) {
					inventoryIndex += 1;
					index -= 1;
					continue listIteration;
				}

				if (index > bodyList().size() - 1) {
					getInventory().setItem(inventoryIndex, null);
					inventoryIndex += 1;
					continue;
				}
				
				getInventory().setItem(inventoryIndex, bodyList().get(index));
				
                if (mode != null) currentIndex += 1;
				inventoryIndex += 1;
			}
		}
		if(mode != null) updateButtons();
	}
  
  //  The next page and previous page buttons will update their location or existence inside
  //  the inventory depending on the page number the inventory is at.
	private void updateButtons() {
		// PREVIOUS PAGE CODE
		if(getPage() > 1) {
			for(int i = 0; i < previousButtonSlots().length; i++) {
				getInventory().setItem(previousButtonSlots()[i], createPreviousButton());
			}
		} else {
			for(int i = 0; i < previousButtonSlots().length; i++) {
				getInventory().setItem(previousButtonSlots()[i], createItemBeforeReplacement());
			}
		}
		
		// NEXT PAGE CODE
		if(currentIndex % itemsPerPage() == 0) {
			if(currentIndex >= bodyList().size()) {
				for(int i = 0; i < nextButtonSlots().length; i++) {
					getInventory().setItem(nextButtonSlots()[i], createItemBeforeReplacement());
				}
				return;
			}
			for(int i = 0; i < nextButtonSlots().length; i++) {
				getInventory().setItem(nextButtonSlots()[i], createNextButton());
			}
			
		} else {
			for(int i = 0; i < nextButtonSlots().length; i++) {
				getInventory().setItem(nextButtonSlots()[i], createItemBeforeReplacement());
			}
			currentIndex = getPage() * itemsPerPage();
		}
	}
  
  // The current page of the inventory will get recreated entirely by making
  // use of the isNextPage() method and the bodyList() method
  
  // In order for this method to take effect, the returning List<ItemStack> object
  // of bodyList() must be updated with the new list before calling, inside your GUI class
	protected void refreshPage() {
	    isNextPage(null);
    }
	
  // Just a utility method used by isNextPage()
	private int getInventoryStartingPoint(int[] input) {
		int output = 0;
		for(int index = input[0]; index < 56; index++) {
			if(index != input[index]) {
				output = input[index - 1] + 1;
				return output;
			}
		}
		return output;
	}
}
