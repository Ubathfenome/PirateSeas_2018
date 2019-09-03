package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Map;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.exceptions.NotEnoughGoldException;
import tfm.uniovi.pirateseas.exceptions.SaveGameException;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ammunitions;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ship;
import tfm.uniovi.pirateseas.model.canvasmodel.game.objects.Item;
import tfm.uniovi.pirateseas.model.canvasmodel.game.objects.ItemLoader;
import tfm.uniovi.pirateseas.model.canvasmodel.ui.UIDisplayElement;
import tfm.uniovi.pirateseas.utils.persistence.GameHelper;

/**
 * Activity to buy better mods for the ship
 * @see: http://developer.android.com/guide/topics/ui/layout/listview.html
 * @see: http://developer.android.com/reference/android/app/ListActivity.html
 *
 */
public class ShopActivity extends ListActivity{
	private static final String TAG = "ShopActivity";
	
	static String descriptionTip;
	
	String mNature = "";
	private int[] sensorTypes;
	private boolean loadGame;
	private int mapHeight;
	private int mapWidth;

	Player dummyPlayer;
	Ship dummyShip;
	Map dummyMap;
	
	List<Item> itemList;
	ListView listView;
	ListAdapter mAdapter;
	
	TextView txtDescription, lblShopTitle;
	UIDisplayElement txtAvailableGold;

	Button btnAcceptAll;
	Button btnClose;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_shop);

		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		Intent data = getIntent();
		mNature = data.getExtras().getString(Constants.ITEMLIST_NATURE, Constants.EMPTY_STRING);
		sensorTypes = data.getIntArrayExtra(Constants.TAG_SENSOR_LIST);
		loadGame = data.getBooleanExtra(Constants.TAG_LOAD_GAME, true);
		mapHeight = data.getIntExtra(Constants.TAG_SCREEN_SELECTION_MAP_HEIGHT, Constants.MAP_MIN_HEIGHT);
		mapWidth = data.getIntExtra(Constants.TAG_SCREEN_SELECTION_MAP_WIDTH, Constants.MAP_MIN_WIDTH);
		
		ItemLoader loader = new ItemLoader(this);
		
		dummyPlayer = new Player();
		dummyShip = new Ship();
		dummyMap = new Map(new Date(),Constants.MAP_MIN_HEIGHT,Constants.MAP_MIN_WIDTH);
		
		GameHelper.loadGameAtPreferences(this, dummyPlayer, dummyShip, dummyMap);
		dummyPlayer = GameHelper.helperPlayer;
		dummyShip = GameHelper.helperShip;
		
		if(mNature.equals(Constants.NATURE_SHOP)){
			// An ordered list of items with their price
			itemList = loader.loadDefault(dummyPlayer.getLevel());
		} else if (mNature.equals(Constants.NATURE_TREASURE)){
			// A random list of free items
			itemList = loader.loadRandom();
		}

		lblShopTitle = findViewById(R.id.lblShopTitle);
		lblShopTitle.setTypeface(customFont);
		
		listView = findViewById(android.R.id.list);
		
		// Assign loaded itemList to ListView Adapter
		mAdapter = new ItemAdapter(this, R.layout.list_item_layout, itemList);
		listView.setAdapter(mAdapter);
		
		txtDescription = findViewById(R.id.txtItemDescription);
		txtDescription.setTypeface(customFont);
		descriptionTip = this.getResources().getString(R.string.shop_purchase_hint);
		txtAvailableGold = findViewById(R.id.playerGold);
		txtAvailableGold.setElementValue(dummyPlayer.getGold());
		
		// Accept all
		btnAcceptAll = findViewById(R.id.btnReceiveAll);
		if(mNature.equals(Constants.NATURE_SHOP))
			btnAcceptAll.setVisibility(View.GONE);
		btnAcceptAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for(int i = 0, size = itemList.size(); i < size; i++){
					purchaseItem(itemList.get(i));
				}
				int size = itemList.size();
				while(!itemList.isEmpty()){
					itemList.remove(--size);
				}
				
				listView.setAdapter(mAdapter);
				v.setVisibility(View.INVISIBLE);
			}
		});
		
		// Cancel
		btnClose = findViewById(R.id.btnClose);
		btnClose.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					LeaveActivityDialogFragment exitShopDialog = new LeaveActivityDialogFragment();
					exitShopDialog.show(getFragmentManager(), "ExitShopDialog");

					Log.d(TAG,"Finish Shop Activity");
			}
		});
	}

	/**
	 * Method to buy the selected item
	 * @param itemPurchased Selected Item
	 * @return true if the item was bought, false otherwise
	 */
	public boolean purchaseItem(Item itemPurchased){
		
		boolean purchased = false;
		
		// Take item price from player's gold stash
		try {
			dummyPlayer.useGold(this, itemPurchased.getPrice());
			txtAvailableGold.setElementValue(dummyPlayer.getGold());
			txtAvailableGold.invalidate();
			purchased = true;
			
			// Add item effects
			switch(itemPurchased.getName()){
				case Constants.ITEM_KEY_CREW:
					dummyShip.gainHealth(5);
					break;
				case Constants.ITEM_KEY_REPAIRMAN:
					dummyShip.gainHealth(15);
					break;
				case Constants.ITEM_KEY_AMMO_SIMPLE:
					dummyShip.gainAmmo(10, Ammunitions.DEFAULT);
					break;
				case Constants.ITEM_KEY_AMMO_AIMED:
					dummyShip.gainAmmo(5, Ammunitions.AIMED);
					break;
				case Constants.ITEM_KEY_AMMO_DOUBLE:
					dummyShip.gainAmmo(5, Ammunitions.DOUBLE);
					break;
				case Constants.ITEM_KEY_AMMO_SWEEP:
					dummyShip.gainAmmo(2, Ammunitions.SWEEP);
					break;
				case Constants.ITEM_KEY_NEST:
					dummyShip.addRange(1.15f);
					break;
				case Constants.ITEM_KEY_MATERIALS:
					dummyShip.setMaxHealth(dummyShip.getMaxHealth() + 10);
					break;
				case Constants.ITEM_KEY_MAPPIECE:
					dummyPlayer.addMapPiece();
					break;
				case Constants.ITEM_KEY_MAP:
					dummyPlayer.giveCompleteMap(true);
					break;
				case Constants.ITEM_KEY_BLACKPOWDER:
					dummyShip.addPower(0.5f);
					break;
				case Constants.ITEM_KEY_VALUABLE:
					dummyPlayer.addGold(100);
					break;
			}
		} catch (NotEnoughGoldException e) {
			Log.e(TAG, e.getMessage());
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
		return purchased;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
        // Are you sure you want to buy this?
        // Affirmative: Purchase Item
        // Negative: Nothing
        PurchaseItemDialogFragment purchaseDialog = new PurchaseItemDialogFragment(itemList.get(position));
        purchaseDialog.show(getFragmentManager(), "ConfirmItemBuyDialog");
	}
	
	@SuppressLint("ValidFragment")
	/*
	 * Class to show a dialog that asks the user if he/she wants to leave the activity
	 */
	public static class LeaveActivityDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();

			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity);
			LayoutInflater inflater = dummyActivity.getLayoutInflater();
			View view = inflater.inflate(R.layout.custom_dialog_layout, null);
			TextView txtTitle = view.findViewById(R.id.txtTitle);
			TextView txtMessage = view.findViewById(R.id.txtMessage);
			Button btnPositive = view.findViewById(R.id.btnPositive);
			Button btnNegative = view.findViewById(R.id.btnNegative);
			txtTitle.setText(getResources().getString(R.string.exit_shop_dialog_title));
			txtMessage.setText(getResources().getString(R.string.exit_shop_dialog_message));
			btnPositive.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// ISSUE #8 (Test pending)
					((ShopActivity)getActivity()).dummyMap.clearActiveMapCell();

					if (GameHelper.saveGameAtPreferences(dummyActivity, ((ShopActivity)getActivity()).dummyPlayer, ((ShopActivity)getActivity()).dummyShip, ((ShopActivity)getActivity()).dummyMap))
						Log.v(TAG, "Game saved");
					else {
						try {
							throw new SaveGameException(getResources().getString(
									R.string.exception_save));
						} catch (NotFoundException e) {
							Log.e(TAG, e.getMessage());
						} catch (SaveGameException e) {
							Log.e(TAG, e.getMessage());
							Toast.makeText(dummyActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
					// Create ScreenSelection Intent, populate extras + flags & start Activity
					Intent screenSelectionIntent = new Intent(dummyActivity, ScreenSelectionActivity.class);
					screenSelectionIntent.putExtra(Constants.TAG_SENSOR_LIST, ((ShopActivity)getActivity()).sensorTypes);
					screenSelectionIntent.putExtra(Constants.TAG_LOAD_GAME, ((ShopActivity)getActivity()).loadGame);
					screenSelectionIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_HEIGHT, ((ShopActivity)getActivity()).mapHeight);
					screenSelectionIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_WIDTH, ((ShopActivity)getActivity()).mapWidth);
					screenSelectionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(screenSelectionIntent);
					try {
						MusicManager.getInstance().stopBackgroundMusic();
					} catch(IllegalStateException e){
						MusicManager.getInstance().resetPlayer();
					}
					MusicManager.getInstance(dummyActivity, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
					dummyActivity.finish();
				}
			});
			btnNegative.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					dismiss();
				}
			});
			builder.setView(view);

			// Create the AlertDialog object and return it
			return builder.create();
		}
	}
	
	@SuppressLint("ValidFragment")
	/*
	 * Class to show a Dialog that asks the user if he/she really want to buy an Item
	 */
	public static class PurchaseItemDialogFragment extends DialogFragment {
		
		Item item;
		
		public PurchaseItemDialogFragment(Item pressedItem) {
			this.item = pressedItem;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity);
			LayoutInflater inflater = dummyActivity.getLayoutInflater();
			View view = inflater.inflate(R.layout.custom_dialog_layout, null);
			TextView txtTitle = view.findViewById(R.id.txtTitle);
			TextView txtMessage = view.findViewById(R.id.txtMessage);
			Button btnPositive = view.findViewById(R.id.btnPositive);
			Button btnNegative = view.findViewById(R.id.btnNegative);
			txtTitle.setText(getResources().getString(R.string.shop_purchase_dialog_title));
			txtMessage.setText(getResources().getString(R.string.shop_purchase_dialog_message));
			btnPositive.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if(item != null){
						if(((ShopActivity)getActivity()).purchaseItem(item)){
							((ShopActivity)getActivity()).itemList.remove(item);
							((ShopActivity)getActivity()).listView.setAdapter(((ShopActivity)getActivity()).mAdapter);
						}
					}
					dismiss();
				}
			});
			btnNegative.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					dismiss();
				}
			});
			builder.setView(view);

			// Create the AlertDialog object and return it
			return builder.create();
		}
	}
	
	/**
	 * Class that holds all available Items on the Shop
	 * @see: http://stackoverflow.com/questions/2265661/how-to-use-arrayadaptermyclass
	 *
	 */
	private class ItemAdapter extends ArrayAdapter<Item>{
		
		private class ViewHolder{
			private ImageView itemHelpIcon;
			private ImageView itemIconView;
			private TextView itemNameView;
			private TextView itemPriceView;
			private ImageView itemPriceIconView;
		}
		
		ViewHolder vHolder;
		
		ItemAdapter(Context context, int resource, List<Item> objects) {
			super(context, resource, objects);
		}

		@NonNull
		@Override
		public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
			if(convertView == null){
				convertView = LayoutInflater.from(this.getContext())
						.inflate(R.layout.list_item_layout, parent, false);
				
				vHolder = new ViewHolder();
				vHolder.itemHelpIcon = convertView.findViewById(R.id.imgHelpIcon);
				vHolder.itemHelpIcon.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
                        txtDescription.setText(itemList.get(position).getDescription() + descriptionTip);
					}
				});
				vHolder.itemIconView = convertView.findViewById(R.id.imgItemIcon);
				vHolder.itemNameView = convertView.findViewById(R.id.txtItemName);
				vHolder.itemPriceView = convertView.findViewById(R.id.txtItemPrice);
				vHolder.itemPriceIconView = convertView.findViewById(R.id.imgGoldIcon);
				
				convertView.setTag(vHolder);
				
			} else {
				vHolder = (ViewHolder) convertView.getTag();
			}

			Item item = getItem(position);
			if(item != null) {
				if(getString(R.string.shop_item_crew_name).equals(item.getName())){
				    vHolder.itemIconView.setImageResource(R.mipmap.icon_buff_crew); }
				else if(getString(R.string.shop_item_repairman_name).equals(item.getName())){
				    vHolder.itemIconView.setImageResource(R.mipmap.icon_buff_repa); }
				else if(getString(R.string.shop_item_ammo_simple_name).equals(item.getName())){
				    vHolder.itemIconView.setImageResource(R.mipmap.txtr_ammo_default); }
				else if(getString(R.string.shop_item_ammo_aimed_name).equals(item.getName())){
				    vHolder.itemIconView.setImageResource(R.mipmap.txtr_ammo_aimed); }
				else if(getString(R.string.shop_item_ammo_double_name).equals(item.getName())){
				    vHolder.itemIconView.setImageResource(R.mipmap.txtr_ammo_double); }
				else if(getString(R.string.shop_item_ammo_sweep_name).equals(item.getName())){
				    vHolder.itemIconView.setImageResource(R.mipmap.txtr_ammo_sweep); }
				else if(getString(R.string.shop_item_nest_name).equals(item.getName())){
				    vHolder.itemIconView.setImageResource(R.mipmap.icon_buff_occu); }
				else if(getString(R.string.shop_item_mats_name).equals(item.getName())){
				    vHolder.itemIconView.setImageResource(R.mipmap.icon_buff_wood); }
				else if(getString(R.string.shop_item_mpiece_name).equals(item.getName())){
				    vHolder.itemIconView.setImageResource(R.mipmap.icon_buff_mapp); }
				else if(getString(R.string.shop_item_map_name).equals(item.getName())){
				    vHolder.itemIconView.setImageResource(R.mipmap.icon_buff_mapp); }
				else if(getString(R.string.shop_item_bpowder_name).equals(item.getName())){
				    vHolder.itemIconView.setImageResource(R.mipmap.icon_buff_bpow); }
				else if(getString(R.string.shop_item_valuable_name).equals(item.getName())){
				    vHolder.itemIconView.setImageResource(R.mipmap.ico_gold); }

				vHolder.itemNameView.setText(item.getName());
				vHolder.itemPriceView.setText(String.valueOf(item.getPrice()));
				vHolder.itemPriceIconView.setBackgroundResource(R.mipmap.ico_gold);
			}

			return convertView;
		}
	}
	
}