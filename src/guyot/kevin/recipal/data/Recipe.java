package guyot.kevin.recipal.data;

import java.io.Serializable;
import java.util.ArrayList;

public class Recipe implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7256897916849652804L;
	private int id;
	private String name;
	private String preperation;
	private String image;
	private float rating;
	private ArrayList<Ingredient> ingredients;
	
	public Recipe(String name, String preperation, String image, float rating)
	{
		this.setId(-1);
		this.setName(name);
		this.setPreperation(preperation);
		this.setImage(image);
		this.setRating(rating);
		this.setIngredients(new ArrayList<Ingredient>());
	}

	Recipe(int id, String name, String preperation, String image, float rating)
	{
		this.setId(id);
		this.setName(name);
		this.setPreperation(preperation);
		this.setImage(image);
		this.setRating(rating);
		this.setIngredients(new ArrayList<Ingredient>());
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public void setName(String name) {
		if(name == null)
			name = "";
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setPreperation(String preperation) {
		if(preperation == null)
			preperation = "";
		this.preperation = preperation;
	}

	public String getPreperation() {
		return this.preperation;
	}

	public void setImage(String image) {
		if(image == null)
			image = "";
		this.image = image;
	}

	public String getImage() {
		return this.image;
	}

	public void setRating(float rating) {
		this.rating = rating;
	}

	public float getRating() {
		return this.rating;
	}

	public void setIngredients(ArrayList<Ingredient> ingredients) {
		if(ingredients == null)
			ingredients = new ArrayList<Ingredient>();
		this.ingredients = ingredients;
	}

	public ArrayList<Ingredient> getIngredients() {
		return this.ingredients;
	}
	
	public void addIngredient(Ingredient ingredient) {
		if(this.ingredients == null)
			this.ingredients = new ArrayList<Ingredient>();
		this.ingredients.add(ingredient);
	}

	public void removeIngredient(Ingredient i) {
		// TODO Auto-generated method stub
		if(this.ingredients != null)
			this.ingredients.remove(i);

	}

}
