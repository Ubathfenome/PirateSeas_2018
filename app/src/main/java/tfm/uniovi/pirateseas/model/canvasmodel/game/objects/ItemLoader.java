package tfm.uniovi.pirateseas.model.canvasmodel.game.objects;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tfm.uniovi.pirateseas.R;

/**
 * Class for the ItemLoader of the ShopActivity
 */
public class ItemLoader{

	private List<Item> itemList, defaultList;
	private List<Item> levelOne = new ArrayList<>();
	private List<Item> levelTwo = new ArrayList<>();
	private List<Item> levelThree = new ArrayList<>();
	
	private int[] levelProbabilities;

	private Item crew;
	private Item repairman;
	private Item nest;
	private Item materials;
	private Item mapPiece;
	private Item blackPowder;
	private Item valuable;

	private Item ammoSimple, ammoAimed, ammoDouble, ammoSweep;

	private static final float ITEM_TIER1_PERCENT = 65;
	private static final float ITEM_TIER2_PERCENT = 25;
	private static final float ITEM_TIER3_PERCENT = 10;
	
	private Context context;
	
	@SuppressWarnings("unchecked")
	/*
	 * Constructor
	 */
	public ItemLoader(Context c){
		itemList = new ArrayList<>();
		
		this.context = c;
		
		crew = new Item(R.string.shop_item_crew_name,
				getString(R.string.shop_item_crew_desc), 1, 5);

		repairman = new Item(R.string.shop_item_repairman_name,
				getString(R.string.shop_item_repairman_desc), 1, 15);
		ammoSimple = new Item(R.string.shop_item_ammo_simple_name, getString(R.string.shop_item_ammo_simple_desc), 1, 1);
		ammoAimed = new Item(R.string.shop_item_ammo_aimed_name, getString(R.string.shop_item_ammo_aimed_desc), 1, 10);
		ammoDouble = new Item(R.string.shop_item_ammo_double_name, getString(R.string.shop_item_ammo_double_desc), 2, 20);
		ammoSweep = new Item(R.string.shop_item_ammo_sweep_name, getString(R.string.shop_item_ammo_sweep_desc), 3, 30);
		nest = new Item(R.string.shop_item_nest_name,
				getString(R.string.shop_item_nest_desc), 3, 35);
		materials = new Item(R.string.shop_item_mats_name,
				getString(R.string.shop_item_mats_desc), 2, 40);
		mapPiece = new Item(R.string.shop_item_mpiece_name,
				getString(R.string.shop_item_mpiece_desc), 2, 65);
		blackPowder = new Item(R.string.shop_item_bpowder_name,
				getString(R.string.shop_item_bpowder_desc), 3, 85);
		valuable = new Item(R.string.shop_item_valuable_name,
				getString(R.string.shop_item_valuable_desc), 3, 101);
		
		defaultList = (ArrayList<Item>) ((ArrayList<Item>) loadAll()).clone();
		
		levelProbabilities = new int[100];
		for(int i = 0; i < 100; i++){
			if(i % ITEM_TIER1_PERCENT == 0)
				levelProbabilities[i] = 1;
			else if (i % ITEM_TIER2_PERCENT == 0)
				levelProbabilities[i] = 2;
			else if (i % ITEM_TIER3_PERCENT == 0)
				levelProbabilities[i] = 3;
		}
	}

	/**
	 * Get String of a resource
	 * @param id Resource index
	 * @return String
	 */
	private String getString(int id){
		return context.getResources().getString(id);
	}

	/**
	 * Method to load all the Items on the Shop
	 * @return List with all the loaded Items
	 */
	private List<Item> loadAll(){
		itemList.clear();
		
		itemList.add(crew);
		itemList.add(repairman);

		itemList.add(ammoSimple);
		itemList.add(ammoAimed);
		itemList.add(ammoDouble);
		itemList.add(ammoSweep);

		itemList.add(nest);
		itemList.add(materials);
		itemList.add(mapPiece);
		itemList.add(blackPowder);
		itemList.add(valuable);
		
		Arrays.sort(itemList.toArray());
		
		return itemList;
	}

	/**
	 * Method to load the default Items for the Shop based on the Player's level
	 * @param level Player's level
	 * @return List with the loaded Items
	 */
	public List<Item> loadDefault(int level){
		itemList.clear();
		for(int i = 0, all = defaultList.size(); i < all; i++){
			Item dummyItem = defaultList.get(i);
			if(dummyItem.getRecommendedLevel()<=level)
				itemList.add(dummyItem);
		}
		return itemList;
	}

	/**
	 * Methpd to load Random Items to the Shop
	 * @return List with the loaded Items
	 */
	public List<Item> loadRandom(){
		for(Item item : defaultList){
			if(item.getLevel() == 1)
				levelOne.add(item);
			else if (item.getLevel() == 2)
				levelTwo.add(item);
			else
				levelThree.add(item);
		}
		
		itemList.clear();
		
		itemList.add(getRandomItem());
		itemList.add(getRandomItem());
		itemList.add(getRandomItem());
		itemList.add(getRandomItem());
		
		Arrays.sort(itemList.toArray());
		
		return itemList;
	}

	/**
	 * Method to get a Random Item
	 * @return Random Item
	 */
	private Item getRandomItem(){
		int randomProbability = (int) (Math.random() * 100);
		Item item;
		double randomValue = Math.random();

		switch(levelProbabilities[randomProbability]){
			case 3:
				item = levelThree.get((int) (randomValue * (levelThree.size() - 1)));
				break;
			case 2:
				item = levelTwo.get((int) (randomValue * (levelTwo.size() - 1)));
				break;
			default:
				item = levelOne.get((int) (randomValue * (levelOne.size() - 1)));
				break;
		}
		
		item.setPrice(0);
		
		return item;
	}
}