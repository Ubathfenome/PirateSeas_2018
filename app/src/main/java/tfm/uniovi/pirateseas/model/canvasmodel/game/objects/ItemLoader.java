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
	
	List<Item> itemList, defaultList;
	List<Item> levelOne = new ArrayList<Item>();
	List<Item> levelTwo = new ArrayList<Item>();
	List<Item> levelThree = new ArrayList<Item>();
	
	private int[] levelProbabilities;
	
	Item crew;
	Item repairman;
	Item ammoSimple;
	Item ammoAimed;
	Item ammoDouble;
	Item ammoSweep;
	Item nest;
	Item materials;
	Item mapPiece;
	Item map;
	Item blackPowder;
	Item valuable;
	
	public static int CREW_ID;
	public static int REPAIRMAN_ID;
	public static int AMMO_SIMPLE_ID;
	public static int AMMO_AIMED_ID;
	public static int AMMO_DOUBLE_ID;
	public static int AMMO_SWEEP_ID;
	public static int NEST_ID;
	public static int MATERIALS_ID;
	public static int MAPPIECE_ID;
	public static int MAP_ID;
	public static int BLACKPOWDER_ID;
	public static int VALUABLE_ID;
	
	
	private static final float ITEM_TIER1_PERCENT = 80;
	private static final float ITEM_TIER2_PERCENT = 15;
	private static final float ITEM_TIER3_PERCENT = 5;
	
	private Context context;
	
	@SuppressWarnings("unchecked")
	/**
	 * Constructor
	 */
	public ItemLoader(Context c){
		itemList = new ArrayList<Item>();
		
		this.context = c;
		
		crew = new Item(getString(R.string.shop_item_crew_name),
				getString(R.string.shop_item_crew_desc), 1, 5);
		CREW_ID = crew.getId();
		repairman = new Item(getString(R.string.shop_item_repairman_name),
				getString(R.string.shop_item_repairman_desc), 1, 15);
		REPAIRMAN_ID = repairman.getId();
		ammoSimple = new Item(getString(R.string.shop_item_ammo_simple_name), getString(R.string.shop_item_ammo_simple_desc),1,1);
		AMMO_SIMPLE_ID = ammoSimple.getId();
		ammoAimed = new Item(getString(R.string.shop_item_ammo_aimed_name), getString(R.string.shop_item_ammo_aimed_desc),1,10);
		AMMO_AIMED_ID = ammoAimed.getId();
		ammoDouble = new Item(getString(R.string.shop_item_ammo_double_name), getString(R.string.shop_item_ammo_double_desc), 2, 20);
		AMMO_DOUBLE_ID = ammoDouble.getId();
		ammoSweep = new Item(getString(R.string.shop_item_ammo_sweep_name), getString(R.string.shop_item_ammo_sweep_desc), 3, 30);
		AMMO_SWEEP_ID = ammoSweep.getId();
		nest = new Item(getString(R.string.shop_item_nest_name),
				getString(R.string.shop_item_nest_desc), 3, 35);
		NEST_ID = nest.getId();
		materials = new Item(getString(R.string.shop_item_mats_name),
				getString(R.string.shop_item_mats_desc), 2, 40);
		MATERIALS_ID = materials.getId();
		mapPiece = new Item(getString(R.string.shop_item_mpiece_name),
				getString(R.string.shop_item_mpiece_desc), 2, 65);
		MAPPIECE_ID = mapPiece.getId();
		map = new Item(getString(R.string.shop_item_map_name),
				getString(R.string.shop_item_map_desc), 3, 140);
		MAP_ID = map.getId();
		blackPowder = new Item(getString(R.string.shop_item_bpowder_name),
				getString(R.string.shop_item_bpowder_desc), 3, 85);
		BLACKPOWDER_ID = blackPowder.getId();
		valuable = new Item(getString(R.string.shop_item_valuable_name),
				getString(R.string.shop_item_valuable_desc), 3, 101);
		VALUABLE_ID = valuable.getId();
		
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
	public List<Item> loadAll(){
		itemList.clear();
		
		itemList.add(crew);
		itemList.add(repairman);

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
		
		Arrays.sort(itemList.toArray());
		
		return itemList;
	}

	/**
	 * Method to get a Random Item
	 * @return Random Item
	 */
	private Item getRandomItem(){
		int randomProbability = (int) Math.random() * 100;
		Item item = null;
		
		switch(levelProbabilities[randomProbability]){
			case 1:
				item = levelOne.get((int) Math.random() * (levelOne.size() - 1));
				break;
			case 2:
				item = levelTwo.get((int) Math.random() * (levelTwo.size() - 1));
				break;
			case 3:
				item = levelThree.get((int) Math.random() * (levelThree.size() - 1));
				break;
		}
		
		item.setPrice(0);
		
		return item;
	}
}