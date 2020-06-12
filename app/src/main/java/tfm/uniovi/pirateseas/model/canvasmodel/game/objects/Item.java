package tfm.uniovi.pirateseas.model.canvasmodel.game.objects;

import android.support.annotation.NonNull;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Class for the in-game things to buy
 */
public class Item implements Comparable<Item> {

	private int name;
	private String description;
	private final int recommendedLevel;
	private int level;
	private int price;

	private String relatedEntity;
	private String relatedStat;

	/**
	 * Constructor
	 * @param name Item name
	 * @param description Item description
	 * @param level Item level
	 * @param price Item price
	 */
	Item(int name, String description, int level, int price){
		this.name = name;
		this.description = description;
		this.level = level;
		this.recommendedLevel = level - 1;
		this.price = price;
		// Set related entity & stat from the received name
		switch(name){
			//  Reemplazar Constants.NOMBRE_VARIABLE por R.string.id_recurso
			case R.string.shop_item_crew_name:
			case R.string.shop_item_repairman_name:
				relatedEntity = Constants.SHIP_ENTITY;
				relatedStat = Constants.SHIP_HEALTH;
				break;
			case R.string.shop_item_ammo_simple_name:
			case R.string.shop_item_ammo_aimed_name:
			case R.string.shop_item_ammo_double_name:
			case R.string.shop_item_ammo_sweep_name:
				relatedEntity = Constants.SHIP_ENTITY;
				relatedStat = Constants.SHIP_AMMO;
				break;
			case R.string.shop_item_nest_name:
				relatedEntity = Constants.SHIP_ENTITY;
				relatedStat = Constants.SHIP_RANGE;
				break;
			case R.string.shop_item_mats_name:
				relatedEntity = Constants.SHIP_ENTITY;
				relatedStat = Constants.SHIP_MAX_HEALTH;
				break;
			case R.string.shop_item_mpiece_name:
			case R.string.shop_item_map_name:
				relatedEntity = Constants.PLAYER_ENTITY;
				relatedStat = Constants.PLAYER_MAP_PIECE;
				break;
			case R.string.shop_item_bpowder_name:
				relatedEntity = Constants.SHIP_ENTITY;
				relatedStat = Constants.SHIP_POWER;
				break;
			case R.string.shop_item_valuable_name:
				relatedEntity = Constants.PLAYER_ENTITY;
				relatedStat = Constants.PLAYER_GOLD;
				break;
		}
	}

	/**
	 * Compare this Item to another
	 * @param other Other Item
	 * @return 1 if this Item has higher level than the other, 0 if their level is equal, -1 if the other Item has higher level
	 */
	public int compareTo(Item other){
		return Integer.compare(level, other.level);
	}

	/**
	 * @return the name
	 */
	public int getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(int name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the level
	 */
	int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the price
	 */
	public int getPrice() {
		return price;
	}

	/**
	 * @param price the price to set
	 */
	void setPrice(int price) {
		this.price = price;
	}

	/**
	 * @return the recommendedLevel
	 */
	int getRecommendedLevel() {
		return recommendedLevel;
	}

	@NonNull
	@Override
	/*
	 * toString
	 */
	public String toString() {
		return "Item [name=" + name + ", price=" + price + "]";
	}

	public String getRelatedEntity() {
		return relatedEntity;
	}

	public String getRelatedStat() {
		return relatedStat;
	}
}