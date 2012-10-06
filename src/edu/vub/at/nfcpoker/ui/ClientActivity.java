package edu.vub.at.nfcpoker.ui;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.google.common.base.CaseFormat;

import edu.vub.at.commlib.CommLib;
import edu.vub.at.commlib.CommLibConnectionInfo;
import edu.vub.at.nfcpoker.Card;
import edu.vub.at.nfcpoker.ConcretePokerServer.GameState;
import edu.vub.at.nfcpoker.R;
import edu.vub.at.nfcpoker.TableThing;
import edu.vub.at.nfcpoker.comm.Message.ReceiveHoleCardsMessage;
import edu.vub.at.nfcpoker.comm.Message.ReceivePublicCards;
import edu.vub.at.nfcpoker.comm.PokerServer;
import edu.vub.at.nfcpoker.comm.Message.ClientAction;
import edu.vub.at.nfcpoker.comm.Message.ClientActionType;
import edu.vub.at.nfcpoker.comm.Message.FutureMessage;
import edu.vub.at.nfcpoker.comm.Message.ReceiveHoleCardsMessage;
import edu.vub.at.nfcpoker.comm.Message.ReceivePublicCards;
import edu.vub.at.nfcpoker.comm.Message.RequestClientActionFutureMessage;
import edu.vub.at.nfcpoker.ui.tools.PageProvider;
import edu.vub.nfc.thing.EmptyRecord;
import edu.vub.nfc.thing.Thing;
import fi.harism.curl.CurlView;

public class ClientActivity extends Activity implements OnClickListener {

	// Game state
	//public static GameState GAME_STATE = GameState.INIT;
	private static int currentBet = 0;
	private static int currentMoney = 0;
	private int currentChipSwiped = 0;
	
	// UI
	private CurlView mCardView1;
	private CurlView mCardView2;
	
	//private int POKER_GREEN = Color.rgb(44, 103, 46);
	public static final int POKER_GREEN = 0xFF2C672E;
	private static final int[] DEFAULT_CARDS = new int[] { R.drawable.backside, R.drawable.backside };
	
    // Interactivity
    public static boolean incognitoMode;
    private static final boolean useIncognitoMode = true;
    private static final boolean useIncognitoLight = false;
    private static final boolean useIncognitoProxmity = true;
    private long incognitoLight;
    private long incognitoProximity;
    private Timer incognitoDelay;
    private SensorManager sensorManager;
	
    // egb: added for gestures.
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
	private Button bet;
	private Button check;
	private Button fold;
    
    
	// Enums
//	public enum GameState {
//	    INIT, NFCPAIRING, HOLE, HOLE_NEXT, FLOP, FLOP_NEXT, TURN, TURN_NEXT, RIVER, RIVER_NEXT
//	}
	
    @Override
    @SuppressWarnings("unused")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);
        
        // Settings
        
        // Game state
        
        // Interactivity
        incognitoMode = false;
        incognitoLight = -1;
        incognitoProximity = -1;
        incognitoDelay = new Timer();
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        
        
        // UI
        /*final Intent intent = new Intent(NfcAdapter.ACTION_TAG_DISCOVERED);
        intent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, AsciiNdefMessage.CreateNdefMessage(UUID));
        startActivity(intent);*/
        
        // Gesture detection
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
        	@Override
        	public boolean onTouch(View arg0, MotionEvent arg1) {
        		int viewSwiped = arg0.getId();
        		switch(viewSwiped) {
        			case R.id.whitechip: currentChipSwiped = 5; break;
        				case R.id.redchip: currentChipSwiped = 10; break;
        				case R.id.greenchip: currentChipSwiped = 20; break;
        				case R.id.bluechip: currentChipSwiped = 50; break;
        				case R.id.blackchip: currentChipSwiped = 100; break;
        				default: Log.v("AMBIENTPOKER", "wrong view swipped" + viewSwiped);
        		}
        		return gestureDetector.onTouchEvent(arg1);
        	}
        };
        
        final ImageView whitechip = (ImageView) findViewById(R.id.whitechip);
        whitechip.setOnClickListener(ClientActivity.this); 
        whitechip.setOnTouchListener(gestureListener);

        final ImageView redchip = (ImageView) findViewById(R.id.redchip);
        redchip.setOnClickListener(ClientActivity.this); 
        redchip.setOnTouchListener(gestureListener);

        final ImageView greenchip = (ImageView) findViewById(R.id.greenchip);
        greenchip.setOnClickListener(ClientActivity.this); 
        greenchip.setOnTouchListener(gestureListener);

        final ImageView bluechip = (ImageView) findViewById(R.id.bluechip);
        bluechip.setOnClickListener(ClientActivity.this); 
        bluechip.setOnTouchListener(gestureListener);

        final ImageView blackchip = (ImageView) findViewById(R.id.blackchip);
        blackchip.setOnClickListener(ClientActivity.this); 
        blackchip.setOnTouchListener(gestureListener);
        
        /*
        ArrayList<Bitmap> mPages1 = new ArrayList<Bitmap>();
		mPages1.add(BitmapFactory.decodeResource(getResources(), R.drawable.backside));
		mPages1.add(BitmapFactory.decodeResource(getResources(), R.drawable.clubs_10c));

        final PageCurlView card1 = (PageCurlView) findViewById(R.id.Card1);
        card1.setPages(mPages1);

        ArrayList<Bitmap> mPages2 = new ArrayList<Bitmap>();
        mPages2.add(BitmapFactory.decodeResource(getResources(), R.drawable.backside));
        mPages2.add(BitmapFactory.decodeResource(getResources(), R.drawable.diamonds_10d));
		
        final PageCurlView card2 = (PageCurlView) findViewById(R.id.Card2);
        card2.setPages(mPages2);
        */
        
        mCardView1 = (CurlView) findViewById(R.id.Card1);
        mCardView1.setPageProvider(new PageProvider(this, DEFAULT_CARDS));
        mCardView1.setCurrentIndex(0);
        //mCardView1.setBackgroundColor(POKER_GREEN);
        
        mCardView2 = (CurlView) findViewById(R.id.Card2);
        mCardView2.setPageProvider(new PageProvider(this, DEFAULT_CARDS));
        mCardView2.setCurrentIndex(0);
        //mCardView2.setBackgroundColor(POKER_GREEN);        
		bet = (Button) findViewById(R.id.Bet);
		check = (Button) findViewById(R.id.Check);
		fold = (Button) findViewById(R.id.Fold);
       
        
        incrementBetAmount(0);
        listenToGameServer();
    }
    
    private void listenToGameServer() {
    	final ClientActivity theActivity = this;
    	final String ip = getIntent().getStringExtra("ip");
    	final int port = getIntent().getIntExtra("port", 0);
    	final Thread tt = new Thread() {
    		public void run() {
    			try {
    				Log.v("AMBIENTPOKER", "Discovered server at " + ip);
    				Client c = CommLibConnectionInfo.connect(ip, port, new Listener() {
    					@Override
    					public void connected(Connection arg0) {
    						super.connected(arg0);
    						Log.d("AMBIENTPOKER","Connected to server!");
    					}
    					
    					@Override
    					public void received(Connection c, Object m) {
    						super.received(c, m);
					
    						Log.v("AMBIENTPOKER", "Received message " + m.toString());
					
    						if (m instanceof GameState) {
    							GameState newGameState = (GameState) m;
    							switch (newGameState) {
    							case STOPPED:
    								Log.v("AMBIENTPOKER", "Game state changed to STOPPED");
    								break;
    							case WAITING_FOR_PLAYERS:
    								Log.v("AMBIENTPOKER", "Game state changed to WAITING_FOR_PLAYERS");
    								Toast.makeText(theActivity, "Waiting for players", Toast.LENGTH_SHORT).show();
    								disableActions();
    								hideCards();
    								break;
    							case PREFLOP:
    								Log.v("AMBIENTPOKER", "Game state changed to PREFLOP");
    								enableActions();
    								showCards();
    								break;
    							case FLOP:
    								Log.v("AMBIENTPOKER", "Game state changed to FLOP");
    								break;
    							case TURN:
    								Log.v("AMBIENTPOKER", "Game state changed to TURN");
    								break;
    							case RIVER:
    								Log.v("AMBIENTPOKER", "Game state changed to RIVER");
    								break;
    							case END_OF_ROUND:
    								Log.v("AMBIENTPOKER", "Game state changed to END_OF_ROUND");
    								break;
    							}
    						}
    						
    						if (m instanceof ReceivePublicCards) {
    							ReceivePublicCards newPublicCards = (ReceivePublicCards) m;
    							Log.v("AMBIENTPOKER", "Received public cards: ");
    							Card[] cards = newPublicCards.cards;
    							for (int i = 0; i < cards.length; i++) {
    								Log.v("AMBIENTPOKER", cards[i].toString() + ", ");
    							}
    							
    						}
					
    						if (m instanceof ReceiveHoleCardsMessage) {
    							final ReceiveHoleCardsMessage newHoleCards = (ReceiveHoleCardsMessage) m;
    							Log.v("AMBIENTPOKER", "Received hand cards: " + newHoleCards.toString());
    							theActivity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										updateHandGui(newHoleCards);
									}
								});
    						}
    					}

						private void enableActions() {
							theActivity.runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									bet.setEnabled(true);
									check.setEnabled(true);
									fold.setEnabled(true);
								}
							});						
						}

						private void disableActions() {
							theActivity.runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									bet.setEnabled(false);
									check.setEnabled(false);
									fold.setEnabled(false);
								}
							});
						}
    				});
    			} catch (IOException e) {
    				Log.e("AMBIENTPOKER", "Could not discover server: ");
    				e.printStackTrace();
    			}
    		}
    	};
    	
    	tt.start();
    		
    }
    
    private void updateHandGui(ReceiveHoleCardsMessage cards) {
 
        int id1 = getResources().getIdentifier("edu.vub.at.nfcpoker:drawable/" + cards.card1.toString(), null, null);
        int[] bitmapIds1 = new int[] { R.drawable.backside, id1 };
        mCardView1.setPageProvider(new PageProvider(this, bitmapIds1));
        
        int id2 = getResources().getIdentifier("edu.vub.at.nfcpoker:drawable/" + cards.card2.toString(), null, null);
        int[] bitmapIds2 = new int[] { R.drawable.backside, id2 };
        mCardView2.setPageProvider(new PageProvider(this, bitmapIds2));
        
    }
    
    @Override
    protected void onResume()
    {
        if (useIncognitoMode) {
	        incognitoMode = false;
	        incognitoLight = -1;
	        incognitoProximity = -1;
	        incognitoDelay = new Timer();

	        if (useIncognitoLight) {
		        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		        if (lightSensor != null) {
			        sensorManager.registerListener(incognitoSensorEventListener, 
			        		lightSensor, 
			        		SensorManager.SENSOR_DELAY_NORMAL);
			        incognitoLight = 0;
		        }
	        }
	        if (useIncognitoProxmity) {
	        	Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		        if (proximitySensor != null) {
			        sensorManager.registerListener(incognitoSensorEventListener, 
			        		proximitySensor, 
			        		SensorManager.SENSOR_DELAY_NORMAL);
			        incognitoProximity = 0;
		        }
	        }
        }
        mCardView1.onResume();
        mCardView2.onResume();
        super.onResume();
    }
    
    @Override
    protected void onPause()
    {
    	sensorManager.unregisterListener(incognitoSensorEventListener);
        mCardView1.onPause();
        mCardView2.onPause();
        super.onPause();
    }

    /*
    // UI
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        int myWidth = (int) (parentHeight * 0.5);
        super.onMeasure(MeasureSpec.makeMeasureSpec(myWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
    }*/
    
    // Game
    private void incrementBetAmount(int value) {
    	currentBet += value;
        final TextView textCurrentBet = (TextView) findViewById(R.id.currentBet);
        textCurrentBet.setText(" " + currentBet);
    }
    
    private boolean canViewCards() {
//    	return ((GAME_STATE == GameState.HOLE) ||
//    			(GAME_STATE == GameState.HOLE_NEXT) ||
//    			(GAME_STATE == GameState.FLOP) ||
//    			(GAME_STATE == GameState.FLOP_NEXT) ||
//    			(GAME_STATE == GameState.TURN) ||
//    			(GAME_STATE == GameState.TURN_NEXT) ||
//    			(GAME_STATE == GameState.RIVER) ||
//    	    	(GAME_STATE == GameState.RIVER_NEXT));
    	return true;
    }
    
    private boolean canBet() {
//    	return ((GAME_STATE == GameState.HOLE) ||
//    			(GAME_STATE == GameState.FLOP) ||
//    			(GAME_STATE == GameState.TURN) ||
//    			(GAME_STATE == GameState.RIVER));
    	return true;
    }
    
    // Interactivity
    SensorEventListener incognitoSensorEventListener = new SensorEventListener() {
    	@Override
    	public void onAccuracyChanged(Sensor sensor, int accuracy) {
    		
    	}

    	@Override
    	public void onSensorChanged(SensorEvent event) {
    		if (event.sensor.getType()==Sensor.TYPE_LIGHT){
    			float currentReading = event.values[0];
    			if (currentReading < 10) {
    				if (incognitoLight == 0) incognitoLight = System.currentTimeMillis();
        			Log.d("Light SENSOR", "It's dark!" + currentReading);
    			} else {
    				incognitoLight = 0;
        			Log.d("Light SENSOR", "It's bright!" + currentReading);
    			}
    		}
    		if (event.sensor.getType()==Sensor.TYPE_PROXIMITY){
    			float currentReading = event.values[0];
    			if (currentReading < 1) {
    				if (incognitoProximity == 0) incognitoProximity = System.currentTimeMillis();
        			Log.d("Proximity SENSOR", "I found a hand!" + currentReading);
    			} else {
    				incognitoProximity = 0;
        			Log.d("Proximity SENSOR", "All clear!" + currentReading);
    			}
    		}
    		if ((incognitoLight != 0) && (incognitoProximity != 0)) {
    			if (!incognitoMode) {
    				incognitoMode = true;
    				incognitoDelay = new Timer();
    				incognitoDelay.schedule(new TimerTask() {
    					public void run() {
    						runOnUiThread(new Runnable() {
    							@Override
    							public void run() {
    								showCards();
    							}
    						});
    					}}, 750);
    			}
    		} else {
    			if (incognitoDelay != null) {
    				incognitoDelay.cancel();
    				incognitoDelay = null;
    			}
				if (incognitoMode) {
					incognitoMode = false;
	    			runOnUiThread(new Runnable() {
	    	            @Override
	    	            public void run() {
	    	            	hideCards();
	    	            }
	    	        });
				}
    		}
    	}
    };
    
    // UI
    private void showCards() {
    	if (canViewCards()) {
            mCardView1.setCurrentIndex(1);
            mCardView2.setCurrentIndex(1);
    	}
    }
    
    private void hideCards() {
    	if (canViewCards()) {
            mCardView1.setCurrentIndex(0);
            mCardView2.setCurrentIndex(0);
    	}
    }


	@Override
	public void onClick(View v) {
	//	Filter f = (Filter) v.getTag();
      //  FilterFullscreenActivity.show(this, input, f);
		
	}
		
	 class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
         @Override
         public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
             try {
                 if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
                     return false;
                 // right to left swipe
                 if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                	 if (canBet()) { 
                		 Toast.makeText(ClientActivity.this, "Adding to the bet " + currentChipSwiped, Toast.LENGTH_SHORT).show();
                		 incrementBetAmount(currentChipSwiped);
                	 }
                 }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                	 if (canBet()) { 
                	 Toast.makeText(ClientActivity.this, "Removing to the bet " + currentChipSwiped, Toast.LENGTH_SHORT).show();
                	 incrementBetAmount(-currentChipSwiped);
                	 }
                 }
             } catch (Exception e) {
                 // nothing
             }
             return false;
         }

     }
}

	
