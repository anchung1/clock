package com.example.clock;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends SimpleBaseGameActivity implements IOnSceneTouchListener, IOnAreaTouchListener{

	static final int CAMERA_WIDTH = 600;
	static final int CAMERA_HEIGHT = 800;

	private static final String TAG = "game";
	private static final boolean D = true;

	private BuildableBitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mTextureRegionBigHand;
	private TiledTextureRegion mTextureRegionLittleHand;
	private TextureRegion mTextureRegionFace;
	private TextureRegion mTextureRegionTimePhase;
	private TiledTextureRegion mTextureRegionSun;
	private TiledTextureRegion mTextureRegionMoon;
	private TextureRegion mTextureRegionDream;
	
	
	private Background mBackground;
	
	static final int screenMidX = CAMERA_WIDTH/2;
	static final int screenMidY = CAMERA_HEIGHT/2;
	
	private Scene mScene;

	private Sprite mTimePhase;
	private TiledSprite bigHand;
	private TiledSprite littleHand;
	private TiledSprite touchedSprite=null;
	private TiledSprite sun;
	private TiledSprite moon;
	
	private float sunTimeElapsed=0;
	private float moonTimeElapsed=0;
	
	private Font mDigitalDreamFont;
	private static final float FONT_SIZE = 32;
	
	private float celestrialRadius = 0;
	
	Angle angObject;
	HandMovement handMovement;
	ReadHand readHand;
	Text mDigitalText;
	
	//minutehand1.png: 31x200
	//clockface.png: 341x341
	//hourhand.png: 15x100
	
	//littlehand: one tick is 6 degrees.
	//need to move 5 ticks in an hour.
	//60minutes/5 = 12.  move a tick every 12 minutes.
	//12 minutes is what percentage of 60?  12/60 = 0.2.  0.2*360degrees = 72 degrees.
	//every 72 degrees that a big hand moves, little hand moves 6 degrees.
	
	//big hand one minute = 1/60 * 360 or 6 degrees per minute
	
	//sprite sheet
	//hourhands: 30x100
	//minutehands: 62x200

	static final int HIGHLIGHT = 0;
	static final int NORMAL = 1;
	
	//TODO: 
	// 1) setup timer to adjust big hand after moving little hand (done)
	// 2) move digital clock to bottom center (done)
	// 3) enable digital clock (done)
	// 4) rotate sun/moon
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		// TODO Auto-generated method stub
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, 
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}


	@Override
	protected void onCreateResources() {
		// TODO Auto-generated method stub
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(getTextureManager(), 2048, 1024, TextureOptions.NEAREST);
		mTextureRegionBigHand = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, "minutehand-fuse.png", 2, 1);
		mTextureRegionFace = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mBitmapTextureAtlas, this, "clockface.png");
		mTextureRegionLittleHand = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, "hourhand-fuse.png", 2, 1);
		mTextureRegionTimePhase = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mBitmapTextureAtlas, this, "daynight.png");
		mTextureRegionSun = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, getAssets(), "suntile.png", 2, 1);
		mTextureRegionMoon = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, getAssets(), "moontile.png", 2, 1);
		mTextureRegionDream = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mBitmapTextureAtlas, getAssets(), "dreams.jpg");
		
				
		try {
			mBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
			mBitmapTextureAtlas.load();
		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}
		
		
		
		//final ITexture digitalDreamFontTexture = new BitmapTextureAtlas(getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		final ITexture digitalDreamFontTexture = new BitmapTextureAtlas(getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		FontFactory.setAssetBasePath("font/");
		mDigitalDreamFont = FontFactory.createFromAsset(getFontManager(), digitalDreamFontTexture, getAssets(), 
				"DigitalDream.ttf", FONT_SIZE, true, Color.BLUE_ARGB_PACKED_INT);
		mDigitalDreamFont.load();
	}


	@Override
	protected Scene onCreateScene() {

		printLog("onCreateScene");
		angObject = new Angle(CAMERA_WIDTH/2, CAMERA_HEIGHT/2, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		celestrialRadius = CAMERA_WIDTH/2;
		
		mScene = new Scene();
		
		//rgb yellow: ffff00
		//rgb purple: 800080
		//mBackground = new Background(Color.CYAN);
		//mScene.setBackground(mBackground);
		
		Sprite dreams = new Sprite(0, 0, mTextureRegionDream, getVertexBufferObjectManager());
		mScene.attachChild(dreams);
		
		mTimePhase = new Sprite((CAMERA_WIDTH-mTextureRegionTimePhase.getWidth())/2, (CAMERA_HEIGHT-mTextureRegionTimePhase.getHeight())/2,
				mTextureRegionTimePhase, getVertexBufferObjectManager());
		mTimePhase.setZIndex(0);
		mTimePhase.setRotationCenter(mTextureRegionTimePhase.getWidth()/2, mTextureRegionTimePhase.getHeight()/2);
		//mScene.attachChild(mTimePhase);
		
		
		Sprite face = new Sprite(CAMERA_WIDTH/2-mTextureRegionFace.getWidth()/2, CAMERA_HEIGHT/2-mTextureRegionFace.getHeight()/2, 
				mTextureRegionFace, getVertexBufferObjectManager());
		face.setZIndex(1);
		mScene.attachChild(face);

		littleHand = new TiledSprite(
				CAMERA_WIDTH/2-mTextureRegionLittleHand.getWidth()/2,
				CAMERA_HEIGHT/2-mTextureRegionLittleHand.getHeight()+5,
				mTextureRegionLittleHand, getVertexBufferObjectManager());
		littleHand.setRotationCenter(13,95);
		littleHand.setZIndex(2);
		littleHand.setCurrentTileIndex(NORMAL);
		mScene.attachChild(littleHand);
		mScene.registerTouchArea(littleHand);

		bigHand = new TiledSprite(CAMERA_WIDTH/2-mTextureRegionBigHand.getWidth()/2,
				CAMERA_HEIGHT/2-mTextureRegionBigHand.getHeight()+10,
				mTextureRegionBigHand, getVertexBufferObjectManager());
		bigHand.setRotationCenter(15, 190);
		bigHand.setZIndex(3);
		bigHand.setCurrentTileIndex(NORMAL);
		mScene.attachChild(bigHand);
		mScene.registerTouchArea(bigHand);
		
		sun = new TiledSprite((CAMERA_WIDTH-mTextureRegionSun.getWidth())/2, 
				CAMERA_HEIGHT/2 + celestrialRadius,
				mTextureRegionSun, getVertexBufferObjectManager());
		
	
		sun.setCurrentTileIndex(NORMAL);
		mScene.attachChild(sun);
		//mScene.setTouchAreaBindingOnActionDownEnabled(true);
		//mScene.registerTouchArea(sun);
		
		//WIDTH/2 - 
		moon = new TiledSprite( (CAMERA_WIDTH-mTextureRegionMoon.getWidth())/2,
				 CAMERA_HEIGHT/2 - celestrialRadius,
				mTextureRegionMoon, getVertexBufferObjectManager());


		moon.setCurrentTileIndex(NORMAL);
		mScene.attachChild(moon);
		//mScene.setTouchAreaBindingOnActionDownEnabled(true);
		//mScene.registerTouchArea(moon);
		
		mScene.setOnSceneTouchListener(this);
		mScene.setOnAreaTouchListener(this);
		
		handMovement = new HandMovement(littleHand, bigHand);
		readHand = new ReadHand(handMovement);
		mDigitalText = new Text(0, 0, mDigitalDreamFont, readHand.getTime(), getVertexBufferObjectManager());
		mDigitalText.setX(CAMERA_WIDTH/2 - mDigitalText.getWidth()/2);
		mDigitalText.setY(CAMERA_HEIGHT/2 + face.getHeight()/2 + 20);
		mScene.attachChild(mDigitalText);
		
		sunTimeElapsed = 0;
		moonTimeElapsed = 0;
		mScene.registerUpdateHandler(new IUpdateHandler() {
			
			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onUpdate(float pSecondsElapsed) {
				
				//printLog("Time elapsed: " + Float.valueOf(pSecondsElapsed).toString());
				// TODO Auto-generated method stub
				if (sun.getCurrentTileIndex()==HIGHLIGHT) {
					
					if (sunTimeElapsed == 0) {
						sunTimeElapsed = pSecondsElapsed;
					} else {
						sunTimeElapsed += pSecondsElapsed;
						if (sunTimeElapsed > 0.33f) {
							sun.setCurrentTileIndex(NORMAL);
							sunTimeElapsed = 0;
						}
					}
				} 
				
			
				if (moon.getCurrentTileIndex()==HIGHLIGHT) {
					
					if (moonTimeElapsed == 0) {
						moonTimeElapsed = pSecondsElapsed;
					} else {
						moonTimeElapsed += pSecondsElapsed;
						if (moonTimeElapsed > 0.33f) {
							moon.setCurrentTileIndex(NORMAL);
							moonTimeElapsed = 0;
						}
					}
				} 
			}
		});
		return mScene;
	}



	
	void printLog(String s) {
		if (D) Log.d(TAG, s);
	}


	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub

		float x, y;
		
		if (pSceneTouchEvent.isActionDown()) {
			
			printLog("Down");

		} else if (pSceneTouchEvent.isActionUp()) {

			if (touchedSprite==null) {
				return false;
			}
			touchedSprite.setCurrentTileIndex(NORMAL);
			if (touchedSprite==littleHand) {
				handMovement.relativeMinHandMovement();
				bigHand.setRotation((float)handMovement.minRotation());
				mDigitalText.setText(readHand.getTime());
			}
			
			touchedSprite = null;
			printLog("Up");
		} else if (pSceneTouchEvent.isActionMove()) {
			if (touchedSprite==null) {
				return false;
			}
			
			if (touchedSprite==bigHand) {
				x = pSceneTouchEvent.getX();
				y = pSceneTouchEvent.getY();
				
				double angle = angObject.getAngle(x, y);
				
				//without this, angles in 2nd and 3rd quadrants are expressed in negative values
				if (angle < 0) {
					angle = 360 + angle;
				}
				handMovement.minHandMove(angle);
				bigHand.setRotation((float)handMovement.minRotation());
				littleHand.setRotation((float)handMovement.hourRotation());
				//mTimePhase.setRotation(handMovement.hourRotation()/2);
				mDigitalText.setText(readHand.getTime());				
			}
			
			if (touchedSprite==littleHand) {
				x = pSceneTouchEvent.getX();
				y = pSceneTouchEvent.getY();
				
				double angle = angObject.getAngle(x, y);
				if (angle < 0) {
					angle = 360 + angle;
				}
				handMovement.hourHandMove(angle);
				littleHand.setRotation((float)handMovement.hourRotation());
				bigHand.setRotation((float)handMovement.minRotation());
				mDigitalText.setText(readHand.getTime());
			}

				
//			printLog("Move");
		} else if (pSceneTouchEvent.isActionCancel()) {
			printLog("Cancel");
		} else if (pSceneTouchEvent.isActionOutside()) {
			printLog("Outside");
		}

		return false;
	}


	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
			ITouchArea pTouchArea, float pTouchAreaLocalX,
			float pTouchAreaLocalY) {
		// TODO Auto-generated method stub

		
		if (pSceneTouchEvent.isActionDown()) {
			printLog("hand Down");
			touchedSprite = (TiledSprite)pTouchArea;
			if (touchedSprite==null) return false;
			touchedSprite.setCurrentTileIndex(HIGHLIGHT);
			
		} else if (pSceneTouchEvent.isActionUp()) {
			if (touchedSprite==null) return false;
			touchedSprite.setCurrentTileIndex(NORMAL);
			if (touchedSprite==littleHand) {
				handMovement.relativeMinHandMovement();
				bigHand.setRotation((float)handMovement.minRotation());
				mDigitalText.setText(readHand.getTime());
			}
			touchedSprite = null;
			printLog("hand Up");
		} 
		return true;
	}





}
