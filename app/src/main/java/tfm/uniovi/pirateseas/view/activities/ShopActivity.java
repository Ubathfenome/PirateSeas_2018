package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
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
 * 
 * @author p7166421
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

	boolean mDebug = false;
	
	Player dummyPlayer;
	Ship dummyShip;
	Map dummyMap;
	
	List<Item> itemList;
	ListView listView;
	ListAdapter mAdapter;
	
	TextView txtDescription;
	UIDisplayElement txtAvailableGold;

	Button btnAcceptAll;
	Button btnClose;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_shop);
		
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
		
		listView = (ListView) findViewById(android.R.id.list);
		
		// Assign loaded itemList to ListView Adapter
		mAdapter = new ItemAdapter(this, R.layout.list_item_layout, itemList);
		listView.setAdapter(mAdapter);
				
		this.getListView().setLongClickable(true);
		this.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
		    public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
				// Are you sure you want to buy this?
				// Affirmative: Purchase Item
				// Negative: Nothing
				PurchaseItemDialogFragment purchaseDialog = new PurchaseItemDialogFragment(itemList.get(position));
				purchaseDialog.show(getFragmentManager(), "ConfirmItemBuyDialog");
				
		        return true;
		    }
		});
		
		txtDescription = (TextView) findViewById(R.id.txtItemDescription);
		descriptionTip = this.getResources().getString(R.string.shop_purchase_hint);
		txtAvailableGold = (UIDisplayElement) findViewById(R.id.playerGold);
		txtAvailableGold.setElementValue(dummyPlayer.getGold());
		
		// Accept all
		btnAcceptAll = (Button) findViewById(R.id.btnReceiveAll);
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
		btnClose = (Button) findViewById(R.id.btnClose);
		btnClose.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					LeaveActivityDialogFragment exitShopDialog = new LeaveActivityDialogFragment();
					exitShopDialog.show(getFragmentManager(), "ExitShopDialog");

					Log.d(TAG,"Finish Shop Activity");
			}
		});
	}
	
	public boolean purchaseItem(Item itemPurchased){
		
		boolean purchased = false;
		
		// Take item price from player's gold stash
		try {
			dummyPlayer.useGold(this, itemPurchased.getPrice());
			txtAvailableGold.setElementValue(dummyPlayer.getGold());
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
		// Update item description on TextView
		txtDescription.setText(itemList.get(position).getDescription() + descriptionTip);
	}
	
	@SuppressLint("ValidFragment")
	public class LeaveActivityDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity);
			builder.setTitle(
					getResources().getString(R.string.exit_shop_dialog_title))
					.setMessage(R.string.exit_shop_dialog_message)
					.setPositiveButton(R.string.exit_shop_dialog_positive,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									if (GameHelper.saveGameAtPreferences(dummyActivity, dummyPlayer, dummyShip, dummyMap))
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
									screenSelectionIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
									screenSelectionIntent.putExtra(Constants.TAG_LOAD_GAME, loadGame);
									screenSelectionIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_HEIGHT, mapHeight);
									screenSelectionIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_WIDTH, mapWidth);
									screenSelectionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(screenSelectionIntent);
									MusicManager.getInstance().stopBackgroundMusic();
									MusicManager.getInstance(dummyActivity, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
									dummyActivity.finish();
								}
							})
					.setNegativeButton(R.string.exit_dialog_negative,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancels the dialog
								}
							});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}
	
	@SuppressLint("ValidFragment")
	public class PurchaseItemDialogFragment extends DialogFragment {
		
		Item item = null;
		
		public PurchaseItemDialogFragment(Item pressedItem) {
			this.item = pressedItem;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity);
			builder.setTitle(
					getResources().getString(R.string.shop_purchase_dialog_title))
					.setMessage(R.string.shop_purchase_dialog_message)
					.setPositiveButton(R.string.shop_purchase_dialog_positive,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									if(item != null){
										if(purchaseItem(item)){
											itemList.remove(item);
											listView.setAdapter(mAdapter);
										}
									}
								}
							})
					.setNegativeButton(R.string.exit_dialog_negative,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancels the dialog
								}
							});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}
	
	/**
	 * 
	 * @author p7166421
	 * @see: http://stackoverflow.com/questions/2265661/how-to-use-arrayadaptermyclass
	 *
	 */
	private class ItemAdapter extends ArrayAdapter<Item>{
		
		private class ViewHolder{
			private ImageView itemIconView;
			private TextView itemNameView;
			private TextView itemPriceView;
			private ImageView itemPriceIconView;
		}
		
		ViewHolder vHolder;
		
		public ItemAdapter(Context context, int resource, List<Item> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = LayoutInflater.from(this.getContext())
						.inflate(R.layout.list_item_layout, parent, false);
				
				vHolder = new ViewHolder();
				vHolder.itemIconView = (ImageView) convertView.findViewById(R.id.imgItemIcon);
				vHolder.itemNameView = (TextView) convertView.findViewById(R.id.txtItemName);
				vHolder.itemPriceView = (TextView) convertView.findViewById(R.id.txtItemPrice);
				vHolder.itemPriceIconView = (ImageView) convertView.findViewById(R.id.imgGoldIcon);
				
				convertView.setTag(vHolder);
				
			} else {
				vHolder = (ViewHolder) convertView.getTag();
			}
			
			Item item = getItem(position);
			if(item != null) {
				if (item.getName().equals(Constants.ITEM_KEY_CREW)) {
					vHolder.itemIconView.setImageDrawable(getResources().getDrawable(R.mipmap.icon_buff_crew));
				} else if (item.getName().equals(Constants.ITEM_KEY_REPAIRMAN)) {
					vHolder.itemIconView.setImageDrawable(getResources().getDrawable(R.mipmap.icon_buff_repa));
				} else if (item.getName().equals(Constants.ITEM_KEY_AMMO_SIMPLE)) {
					vHolder.itemIconView.setImageDrawable(getResources().getDrawable(R.mipmap.txtr_ammo_default));
				} else if (item.getName().equals(Constants.ITEM_KEY_AMMO_AIMED)) {
					vHolder.itemIconView.setImageDrawable(getResources().getDrawable(R.mipmap.txtr_ammo_aimed));
				} else if (item.getName().equals(Constants.ITEM_KEY_AMMO_DOUBLE)) {
					vHolder.itemIconView.setImageDrawable(getResources().getDrawable(R.mipmap.txtr_ammo_double));
				} else if (item.getName().equals(Constants.ITEM_KEY_AMMO_SWEEP)) {
					vHolder.itemIconView.setImageDrawable(getResources().getDrawable(R.mipmap.txtr_ammo_sweep));
				} else if (item.getName().equals(Constants.ITEM_KEY_NEST)){
					vHolder.itemIconView.setImageDrawable(getResources().getDrawable(R.mipmap.icon_buff_occu));
				}else if(item.getName().equals(Constants.ITEM_KEY_MATERIALS)) {
					vHolder.itemIconView.setImageDrawable(getResources().getDrawable(R.mipmap.icon_buff_wood));
				}else if(item.getName().equals(Constants.ITEM_KEY_MAPPIECE)) {
					vHolder.itemIconView.setImageDrawable(getResources().getDrawable(R.mipmap.icon_buff_mapp));
				}else if(item.getName().equals(Constants.ITEM_KEY_MAP)) {
					vHolder.itemIconView.setImageDrawable(getResources().getDrawable(R.mipmap.icon_buff_mapp));
				}else if(item.getName().equals(Constants.ITEM_KEY_BLACKPOWDER)) {
					vHolder.itemIconView.setImageDrawable(getResources().getDrawable(R.mipmap.icon_buff_bpow));
				}else if(item.getName().equals(Constants.ITEM_KEY_VALUABLE)) {
					vHolder.itemIconView.setImageDrawable(getResources().getDrawable(R.mipmap.ico_gold));
				}

				vHolder.itemNameView.setText(item.getName());
				vHolder.itemPriceView.setText("" + item.getPrice());
				vHolder.itemPriceIconView.setBackgroundResource(R.mipmap.ico_gold);
			}

			return convertView;
		}
	}
	
}