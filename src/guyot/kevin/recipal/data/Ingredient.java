package guyot.kevin.recipal.data;

import java.io.Serializable;

public class Ingredient implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3100614603846155367L;
	private int id;
	private int recipeId;
	private int sortOrder;
	private String description;
	
	public Ingredient(int recipeId, int sortOrder, String description)
	{
		this.setId(-1);
		this.setRecipeId(recipeId);
		this.setSortOrder(sortOrder);
		this.setDescription(description);
	}

	public Ingredient(int id, int recipeId, int sortOrder, String description)
	{
		this.setId(id);
		this.setRecipeId(recipeId);
		this.setSortOrder(sortOrder);
		this.setDescription(description);
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setRecipeId(int recipeId) {
		this.recipeId = recipeId;
	}

	public int getRecipeId() {
		return recipeId;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}	
}
