package me.cockx.learn;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.codeandweb.physicseditor.PhysicsShapeCache;

import java.util.HashMap;

public class GdxGame extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture img;
	private TextureAtlas textureAtlas;
	private ExtendViewport extendViewport;
	private OrthographicCamera camera;
	private final HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();
	private World world;
    private PhysicsShapeCache physicsBodies;
	private Box2DDebugRenderer renderer;
	private Body crate;
	private Body ground;

	private static final float STEP_TIME = 1f / 60f;
	private static final int VELOCITY_ITERATIONS = 6;
	private static final int POSITION_ITERATIONS = 2;
	private static final float SCALE = 0.05f;

	private float accumulator = 0;
	private Body crate2;

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		textureAtlas = new TextureAtlas("sprites.txt");
		camera = new OrthographicCamera();
		extendViewport = new ExtendViewport(50,50,camera);
		addSprites();
		Box2D.init();
		world = new World(new Vector2(0, -10), true);
		renderer = new Box2DDebugRenderer();
		physicsBodies = new PhysicsShapeCache("physics.xml");
		crate = createBody("crate",10,10,0);
		crate2 = createBody("crate",12,15,0);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.57f, 0.77f, 0.85f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		drawSprite("crate",0,0,0 );
		Vector2 position = crate.getPosition();
		float degrees = (float)Math.toDegrees(crate.getAngle());
		drawSprite("crate",position.x,position.y,degrees );
		Vector2 position2 = crate2.getPosition();
		float degrees2 = (float)Math.toDegrees(crate2.getAngle());
		drawSprite("crate",position2.x,position2.y,degrees2 );
		drawSprite("cherries",30,0,0 );
		batch.end();
		stepWorld();
		renderer.render(world,camera.combined);
	}

	private void stepWorld() {
		float delta = Gdx.graphics.getDeltaTime();

		accumulator += Math.min(delta, 0.25f);

		if (accumulator >= STEP_TIME) {
			accumulator -= STEP_TIME;

			world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
		}
	}

	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
		textureAtlas.dispose();
		sprites.clear();
		world.dispose();
		renderer.dispose();
	}

	@Override
	public void resize(int width, int height) {
		extendViewport.update(width,height,true);
		batch.setProjectionMatrix(camera.combined);
		createGround();
	}

	private void drawSprite(String name, float x, float y, float degrees) {
		Sprite sprite = sprites.get(name);
		sprite.setPosition(x, y);
		sprite.setRotation(degrees);
		sprite.setOrigin(0f,0f);
		sprite.draw(batch);
	}

	private void addSprites() {
		Array<TextureAtlas.AtlasRegion> regions = textureAtlas.getRegions();
		for (TextureAtlas.AtlasRegion region : regions) {
			Sprite sprite = textureAtlas.createSprite(region.name);
			float width = sprite.getWidth()*SCALE;
			float height = sprite.getHeight()*SCALE;

			sprite.setSize(width,height);
			sprite.setOrigin(0,0);

			sprites.put(region.name, sprite);

		}
	}

	private Body createBody(String name, float x, float y, float rotation) {
		Body body = physicsBodies.createBody(name, world, SCALE, SCALE);
		body.setTransform(x, y, rotation);

		return body;
	}

	private void createGround() {
		if (ground != null) world.destroyBody(ground);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction=1;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(camera.viewportWidth, 0);

		fixtureDef.shape = shape;

		ground = world.createBody(bodyDef);
		ground.createFixture(fixtureDef);
		ground.setTransform(0, 0, 0);

		shape.dispose();
	}
}
