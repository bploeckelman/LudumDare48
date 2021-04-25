package lando.systems.ld48.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthoCachedTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import lando.systems.ld48.Assets;
import lando.systems.ld48.physics.Segment2D;
import lando.systems.ld48.screens.GameScreen;
import lando.systems.ld48.utils.Utils;

public class Level {

    public static final float TILE_SIZE = 32f;

    public enum LayerType { background, collision, foreground }
    public static class Layer {
        public final int[] index;
        public final TiledMapTileLayer tileLayer;
        public Layer(int index, TiledMapTileLayer tileLayer) {
            this.index = new int[]{ index };
            this.tileLayer = tileLayer;
        }
    }

    private Assets assets;

    private String name;
    private TiledMap map;
    private TiledMapRenderer renderer;
    private MapLayer objectsLayer;
    private ObjectMap<LayerType, Layer> layers;

    private Pool<Rectangle> rectPool = Pools.get(Rectangle.class);
    private Array<Rectangle> tileRects = new Array<>();
    private Rectangle tempRect = new Rectangle();

    private boolean collisionDirty;
    private Array<Segment2D> collisionSegments;

    private LevelDescriptor currentLevel;
    private LevelDescriptor nextLevel = null;

    private Exit exit;
    private SpawnPlayer playerSpawn;
    private Array<SpawnEnemy> enemySpawns;
    private Array<SpawnPickup> pickupSpawns;

    public Level(LevelDescriptor levelDescriptor, GameScreen gameScreen) {
        Gdx.app.log("Level", "Loading: " + levelDescriptor);

        this.assets = gameScreen.game.assets;
        this.currentLevel = levelDescriptor;

        // load map
        this.map = (new TmxMapLoader()).load(levelDescriptor.mapFileName);
        // note: gwt doesn't like the mipmaps
//        this.map = (new TmxMapLoader()).load(levelDescriptor.mapFileName, new TmxMapLoader.Parameters() {{
//            generateMipMaps = true;
//            textureMinFilter = Texture.TextureFilter.MipMap;
//            textureMagFilter = Texture.TextureFilter.MipMap;
//        }});
        this.renderer = new OrthoCachedTiledMapRenderer(map);
        ((OrthoCachedTiledMapRenderer) renderer).setBlending(true);

        // load map properties
        this.name = map.getProperties().get("name", "[UNNAMED]", String.class);
        String nextLevelName = map.getProperties().get("next-level", null, String.class);
        if (nextLevelName != null) {
            this.nextLevel = LevelDescriptor.valueOf(nextLevelName);
        }

        // load and validate map layers
        MapLayers mapLayers = map.getLayers();
        this.layers = new ObjectMap<>();
        for (int i = 0; i < mapLayers.size(); i++) {
            MapLayer mapLayer = mapLayers.get(i);
            if (mapLayer.getName().equalsIgnoreCase("objects")) {
                this.objectsLayer = mapLayer;
            } else if (mapLayer instanceof TiledMapTileLayer) {
                Layer layer = new Layer(i, (TiledMapTileLayer) mapLayer);
                if      (mapLayer.getName().equalsIgnoreCase("background")) this.layers.put(LayerType.background, layer);
                else if (mapLayer.getName().equalsIgnoreCase("collision"))  this.layers.put(LayerType.collision,  layer);
                else if (mapLayer.getName().equalsIgnoreCase("foreground")) this.layers.put(LayerType.foreground, layer);
            } else {
                Gdx.app.log("Level", "Tilemap has a weird layer that is neight 'objects' nor one of the TiledMapTileLayer types: '" + mapLayer.getName() + "'");
            }
        }
        if (this.layers.get(LayerType.background) == null) {
            throw new GdxRuntimeException("Tilemap missing required layer: 'background'. (required layers: 'background', 'collision', 'foreground', 'objects')");
        }
        if (this.layers.get(LayerType.collision) == null) {
            throw new GdxRuntimeException("Tilemap missing required layer: 'collision'. (required layers: 'background', 'collision', 'foreground', 'objects')");
        }
        if (this.layers.get(LayerType.foreground) == null) {
            throw new GdxRuntimeException("Tilemap missing required layer: 'foreground'. (required layers: 'background', 'collision', 'foreground', 'objects')");
        }
        if (this.objectsLayer == null) {
            throw new GdxRuntimeException("Tilemap missing required layer: 'objects'. (required layers: 'background', 'collision', 'foreground', 'objects')");
        }

        // load map objects
        exit = null;
        playerSpawn = null;
        enemySpawns = new Array<>();
        pickupSpawns = new Array<>();

        MapObjects objects = objectsLayer.getObjects();
        for (MapObject object : objects) {
            MapProperties props = object.getProperties();
            String type = (String) props.get("type");
            if (type == null) {
                Gdx.app.log("Map", "Map object missing 'type' property");
                continue;
            }
            float x = props.get("x", Float.class);
            float y = props.get("y", Float.class);

            if ("spawn-player".equalsIgnoreCase(type)) {
                playerSpawn = new SpawnPlayer(x, y, assets);
            }
            else if ("spawn-enemy".equalsIgnoreCase(type)) {
                SpawnEnemy.Type enemyType = SpawnEnemy.Type.valueOf((String) props.get("enemy-type"));
                SpawnEnemy spawn = new SpawnEnemy(enemyType, x, y, gameScreen.game.assets);
                enemySpawns.add(spawn);
            }
            else if ("spawn-pickup".equalsIgnoreCase(type)) {
                SpawnPickup.Type pickupType = SpawnPickup.Type.valueOf((String) props.get("pickup-type"));
                SpawnPickup spawn = new SpawnPickup(pickupType, x, y, gameScreen.game.assets);
                pickupSpawns.add(spawn);
            }
            else if ("exit".equalsIgnoreCase(type)) {
                LevelTransition.Type transitionType = LevelTransition.Type.valueOf((String) props.get("transition-type"));
                LevelDescriptor targetLevel = LevelDescriptor.valueOf((String) props.get("target-level"));
                exit = new Exit(x, y, transitionType, targetLevel, assets);
            }
        }

        // Validate that we have required entities
        if (playerSpawn == null) {
            throw new GdxRuntimeException("Map missing required object: 'spawn-player'");
        }
        if (exit == null) {
            throw new GdxRuntimeException("Map missing required object: 'exit'");
        }

        buildCollisionBounds();
        collisionDirty = true;
    }

    public void update(float dt) {

    }

    public Exit getExit() {
        return exit;
    }

    public SpawnPlayer getPlayerSpawn() {
        return playerSpawn;
    }

    public Array<SpawnEnemy> getEnemySpawns() {
        return enemySpawns;
    }

    public Array<SpawnPickup> getPickupSpawns() {
        return pickupSpawns;
    }

    public Layer getLayer(LayerType layerType) {
        return layers.get(layerType);
    }

    public Array<Segment2D> getCollisionSegments() {
        return collisionSegments;
    }

    public boolean isCollisionDirty() {
        return collisionDirty;
    }

    public void setCollisionDirty(boolean dirty) {
        collisionDirty = dirty;
    }

    // ------------------------------------------------------------------------
    // Render methods
    // ------------------------------------------------------------------------

    public void render(LayerType layerType, OrthographicCamera camera) {
        Layer layer = layers.get(layerType);
        if (layer == null || layer.tileLayer == null || layer.index.length != 1) {
            return;
        }

        renderer.setView(camera);
        renderer.render(layer.index);
    }

    public void renderObjects(SpriteBatch batch) {
        // ...
    }

    private Color segmentColor = new Color();
    public void renderDebug(SpriteBatch batch) {
        float width = 2;
        float hue = 0;
        for (Segment2D segment : collisionSegments) {
            hue += .17;
            batch.setColor(Utils.hsvToRgb(hue, 1f, 1f, segmentColor));
            batch.draw(assets.whitePixel, segment.start.x, segment.start.y - width / 2f, 0, width / 2f, segment.delta.len(), width, 1, 1, segment.getRotation());
            batch.draw(assets.whitePixel, segment.start.x + segment.delta.x / 2, segment.start.y + segment.delta.y / 2, 0,0, 10, 1, 1, 1, segment.normal.angle());
        }
        exit.render(batch);
        playerSpawn.render(batch);
    }

    // ------------------------------------------------------------------------
    // Collision methods
    // ------------------------------------------------------------------------

    public void addCollisionRectangle(Rectangle rect) {
        collisionSegments.add(new Segment2D(new Vector2(rect.x, rect.y), new Vector2(rect.x + rect.width, rect.y)));
        collisionSegments.add(new Segment2D(new Vector2(rect.x+rect.width, rect.y), new Vector2(rect.x + rect.width, rect.y + rect.height)));
        collisionSegments.add(new Segment2D(new Vector2(rect.x + rect.width, rect.y + rect.height), new Vector2(rect.x, rect.y + rect.height)));
        collisionSegments.add(new Segment2D(new Vector2(rect.x, rect.y + rect.height), new Vector2(rect.x, rect.y)));
        collisionDirty = true;
    }

    public void removeCollisionRectangle(Rectangle rect) {
        Segment2D bottom = new Segment2D(new Vector2(rect.x, rect.y), new Vector2(rect.x + rect.width, rect.y));
        Segment2D right = new Segment2D(new Vector2(rect.x+rect.width, rect.y), new Vector2(rect.x + rect.width, rect.y + rect.height));
        Segment2D top = new Segment2D(new Vector2(rect.x + rect.width, rect.y + rect.height), new Vector2(rect.x, rect.y + rect.height));
        Segment2D left = new Segment2D(new Vector2(rect.x, rect.y + rect.height), new Vector2(rect.x, rect.y));

        removeSegment(bottom);
        removeSegment(right);
        removeSegment(top);
        removeSegment(left);
        collisionDirty = true;
    }

    private void removeSegment(Segment2D segment){
        if (!collisionSegments.removeValue(segment, false)){
            Gdx.app.log("Collision", "need to write this to handle consolidated physics.");
        }
    }

    private void buildCollisionBounds() {
        collisionSegments = new Array<>();
        TiledMapTileLayer collisionLayer = layers.get(LayerType.collision).tileLayer;
        float tileWidth = collisionLayer.getTileWidth();

        // toggle this to allow for 45 degree ramps;
        // but if it's on the parser gets confused about stylistic tiles with transparency that aren't actual ramps
        boolean pixelPrecision = true;
        Pixmap pixmap = null;

        // Build Edges
        for (int x = 0; x < collisionLayer.getWidth(); x++) {
            for (int y =0; y < collisionLayer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);
                if (cell == null) continue;

                TiledMapTileLayer.Cell cellRight  = collisionLayer.getCell(x + 1, y);
                TiledMapTileLayer.Cell cellLeft   = collisionLayer.getCell(x - 1, y);
                TiledMapTileLayer.Cell cellTop    = collisionLayer.getCell(x, y + 1);
                TiledMapTileLayer.Cell cellBottom = collisionLayer.getCell(x, y - 1);

                Segment2D topSegment    = null;
                Segment2D leftSegment   = null;
                Segment2D bottomSegment = null;
                Segment2D rightSegment  = null;
                if (cellRight  == null) rightSegment  = new Segment2D((x+1) * tileWidth, y     * tileWidth, (x+1) * tileWidth, (y+1) * tileWidth);
                if (cellLeft   == null) leftSegment   = new Segment2D((x)   * tileWidth, (y+1) * tileWidth, (x)   * tileWidth, (y)   * tileWidth);
                if (cellTop    == null) topSegment    = new Segment2D((x+1) * tileWidth, (y+1) * tileWidth, (x)   * tileWidth, (y+1) * tileWidth);
                if (cellBottom == null) bottomSegment = new Segment2D((x)   * tileWidth, (y)   * tileWidth, (x+1) * tileWidth, (y)   * tileWidth);
                if (!pixelPrecision) {
                    if (topSegment    != null) collisionSegments.add(topSegment);
                    if (rightSegment  != null) collisionSegments.add(rightSegment);
                    if (leftSegment   != null) collisionSegments.add(leftSegment);
                    if (bottomSegment != null) collisionSegments.add(bottomSegment);
                }

                // NOTE - needed for ramps, but we don't care at the moment and it misreads the prototype tileset
                if (pixelPrecision) {
                    if (pixmap == null) {
                        Texture texture = cell.getTile().getTextureRegion().getTexture();
                        if (!texture.getTextureData().isPrepared()) {
                            texture.getTextureData().prepare();
                        }
                        pixmap = texture.getTextureData().consumePixmap();
                    }

                    // look for transparent pixels on the edges of a tile to indicate a ramp
                    TextureRegion region = cell.getTile().getTextureRegion();
                    int valueL = pixmap.getPixel(region.getRegionX(), region.getRegionY() + region.getRegionHeight() / 2);
                    int valueR = pixmap.getPixel(region.getRegionX() + region.getRegionWidth() - 1, region.getRegionY() + region.getRegionHeight() / 2);
                    int valueU = pixmap.getPixel(region.getRegionX() + region.getRegionWidth() / 2, region.getRegionY());
                    int valueD = pixmap.getPixel(region.getRegionX() + region.getRegionWidth() / 2, region.getRegionY() + region.getRegionHeight() - 1);
                    Color colorL = new Color(valueL);
                    Color colorR = new Color(valueR);
                    Color colorU = new Color(valueU);
                    Color colorD = new Color(valueD);

                    if (colorL.a == 0f && colorU.a == 0f) {
                        if (rightSegment  != null) collisionSegments.add(rightSegment);
                        if (bottomSegment != null) collisionSegments.add(bottomSegment);
                        collisionSegments.add(new Segment2D((x + 1) * tileWidth, (y + 1) * tileWidth, (x) * tileWidth, (y) * tileWidth));
                    } else if (colorR.a == 0f && colorU.a == 0f) {
                        if (leftSegment   != null) collisionSegments.add(leftSegment);
                        if (bottomSegment != null) collisionSegments.add(bottomSegment);
                        collisionSegments.add(new Segment2D((x + 1) * tileWidth, (y) * tileWidth, (x) * tileWidth, (y + 1) * tileWidth));
                    } else if (colorL.a == 0f && colorD.a == 0f) {
                        if (rightSegment != null) collisionSegments.add(rightSegment);
                        if (topSegment   != null) collisionSegments.add(topSegment);
                        collisionSegments.add(new Segment2D((x) * tileWidth, (y + 1) * tileWidth, (x + 1) * tileWidth, (y) * tileWidth));
                    } else if (colorR.a == 0f && colorD.a == 0f) {
                        if (leftSegment != null) collisionSegments.add(leftSegment);
                        if (topSegment  != null) collisionSegments.add(topSegment);
                        collisionSegments.add(new Segment2D((x) * tileWidth, (y) * tileWidth, (x + 1) * tileWidth, (y + 1) * tileWidth));
                    } else {
                        if (topSegment    != null) collisionSegments.add(topSegment);
                        if (rightSegment  != null) collisionSegments.add(rightSegment);
                        if (leftSegment   != null) collisionSegments.add(leftSegment);
                        if (bottomSegment != null) collisionSegments.add(bottomSegment);
                    }
                }
            }
        }

        consolidateSegments();
    }

    private void consolidateSegments(){
        // consolidate segments
        boolean fixed = true;
        while (fixed){
            fixed = false;
            for (int i = 0; i < collisionSegments.size; i++) {
                Segment2D seg = collisionSegments.get(i);
                for (int j = collisionSegments.size - 1; j > i; j--) {
                    Segment2D next = collisionSegments.get(j);
                    if (seg.getRotation() != next.getRotation()) continue;
                    if (seg.end.epsilonEquals(next.start)) {
                        seg.setEnd(next.end);
                        collisionSegments.removeIndex(j);
                        fixed = true;
                    } else if (seg.start.epsilonEquals(next.end)){
                        seg.setStart(next.start);
                        collisionSegments.removeIndex(j);
                        fixed = true;
                    }
                }
            }
        }
    }

    public void getTiles(float startX, float startY, float endX, float endY, Array<Rectangle> tiles) {
        if (startX > endX) {
            float t = startX;
            startX = endX;
            endX = t;
        }
        if (startY > endY) {
            float t = startY;
            startY = endY;
            endY = t;
        }

        rectPool.freeAll(tiles);
        tiles.clear();

        TiledMapTileLayer collisionLayer = layers.get(LayerType.collision).tileLayer;
        int iStartX = (int) (startX / collisionLayer.getTileWidth());
        int iStartY = (int) (startY / collisionLayer.getTileHeight());
        int iEndX   = (int) (endX   / collisionLayer.getTileWidth());
        int iEndY   = (int) (endY   / collisionLayer.getTileHeight());
        for (int y = iStartY; y <= iEndY; y++) {
            for (int x = iStartX; x <= iEndX; x++) {
                TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);
                if (cell != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x * collisionLayer.getTileWidth(),
                            y * collisionLayer.getTileHeight(),
                            collisionLayer.getTileWidth(),
                            collisionLayer.getTileHeight());
                    tiles.add(rect);
                }
            }
        }
    }

    public boolean checkCollision(Vector2 oldPosition, Vector2 newPosition, float radius, Vector2 collisionPoint) {
        // insert Doug here
//
//        boolean unresolvedCollisions = false;
//        CollisionType collisionType = CollisionType.None;
//        collisionPoint.set(newPosition);
//        int fuckThisShit = 0;
//        do {
//            fuckThisShit++;
//            unresolvedCollisions = false;
//            tempCollisionPoint.set(collisionPoint);
//
//            // Check for collisions against boundaries
//            for (int j = 0; j < boundaries.size; j++) {
//                Polyline boundary = boundaries.get(j).getPolyline();
//                boolean collided = false;
//                float nearestCollision = Float.MAX_VALUE;
//                int vertLength = boundary.getVertices().length;
//                for (int i = 0; i < vertLength; i += 2) {
//                    tempVector.set(boundary.getTransformedVertices()[i], boundary.getTransformedVertices()[i + 1]);
//                    tempVector2.set(boundary.getTransformedVertices()[(i + 2) % vertLength], boundary.getTransformedVertices()[(i + 3) % vertLength]);
//
//                    // Check if the traveling path intersects the segment
//                    if (Intersector.intersectSegments(oldPosition, tempCollisionPoint, tempVector, tempVector2, collisionPoint)) {
//                        normal.set(-1 * (tempVector2.y - tempVector.y), (tempVector2.x - tempVector.x));
//                        if (Intersector.pointLineSide(tempVector, tempVector2, oldPosition) != Intersector.pointLineSide(tempVector.x, tempVector.y, tempVector2.x, tempVector2.y, collisionPoint.x + normal.x, collisionPoint.y + normal.y)) {
//                            normal.set((tempVector2.y - tempVector.y), -1 * (tempVector2.x - tempVector.x));
//                        }
//                        normal.nor();
//                        collisionPoint.add(normal.scl(radius+1f));
//
//                        normal.nor();
//
//                        collided = true;
//                        nearestCollision = 0;
//                    }
//
//                    // Check if the segments are within the radius of the object
//                    float dist = Intersector.distanceSegmentPoint(tempVector, tempVector2, tempCollisionPoint);
//                    if (dist < nearestCollision && dist < radius) {
//                        Intersector.nearestSegmentPoint(tempVector, tempVector2, tempCollisionPoint, collisionPoint);
//                        // If it is on an end point bounce back towards where you came from
//                        if (collisionPoint.epsilonEquals(tempVector) || collisionPoint.epsilonEquals(tempVector2)) {
//                            normal.set(tempCollisionPoint.x - collisionPoint.x, tempCollisionPoint.y - collisionPoint.y);
//                        } else {
//                            // bounce away from the normal of the segment
//                            normal.set(-1 * (tempVector2.y - tempVector.y), (tempVector2.x - tempVector.x));
//                            if (Intersector.pointLineSide(tempVector, tempVector2, oldPosition) != Intersector.pointLineSide(tempVector.x, tempVector.y, tempVector2.x, tempVector2.y, collisionPoint.x + normal.x, collisionPoint.y + normal.y)) {
//                                // Normal is facing the wrong way, flip it
//                                normal.set((tempVector2.y - tempVector.y), -1 * (tempVector2.x - tempVector.x));
//                            }
//                        }
//                        normal.nor();
//                        collisionPoint.add(normal.scl(radius+1f));
//
//                        normal.nor();
//
//                        collided = true;
//                        nearestCollision = dist;
//                    }
//
//                }
//                if (collided) {
//                    unresolvedCollisions = true;
//                    if (collisionType != CollisionType.Bumper){
//                        collisionType = CollisionType.Wall;
//                    }
//                }
//            }
//
//            // Check for collision against water regions
//            for (int j = 0; j < waterRegions.size; j++) {
//                Polygon boundary = waterRegions.get(j);
//                float nearestCollision = Float.MAX_VALUE;
//                int vertLength = boundary.getVertices().length;
//                for (int i = 0; i < vertLength; i += 2) {
//                    tempVector.set(boundary.getTransformedVertices()[i], boundary.getTransformedVertices()[i + 1]);
//                    tempVector2.set(boundary.getTransformedVertices()[(i + 2) % vertLength], boundary.getTransformedVertices()[(i + 3) % vertLength]);
//
//                    // Check if the segments are within the radius of the object
//                    float dist = Intersector.distanceSegmentPoint(tempVector, tempVector2, tempCollisionPoint);
//                    if (dist < nearestCollision && dist < radius) {
//                        Intersector.nearestSegmentPoint(tempVector, tempVector2, tempCollisionPoint, collisionPoint);
//                        // If it is on an end point bounce back towards where you came from
//                        if (collisionPoint.epsilonEquals(tempVector) || collisionPoint.epsilonEquals(tempVector2)) {
//                            normal.set(tempCollisionPoint.x - collisionPoint.x, tempCollisionPoint.y - collisionPoint.y);
//                        } else {
//                            // bounce away from the normal of the segment
//                            normal.set(-1 * (tempVector2.y - tempVector.y), (tempVector2.x - tempVector.x));
//                            if (Intersector.pointLineSide(tempVector, tempVector2, oldPosition) != Intersector.pointLineSide(tempVector.x, tempVector.y, tempVector2.x, tempVector2.y, collisionPoint.x + normal.x, collisionPoint.y + normal.y)) {
//                                // Normal is facing the wrong way, flip it
//                                normal.set((tempVector2.y - tempVector.y), -1 * (tempVector2.x - tempVector.x));
//                            }
//                        }
//                        normal.nor();
//                        collisionPoint.add(normal.scl(radius+1f));
//
//                        normal.nor();
//
//                        nearestCollision = dist;
//                        unresolvedCollisions = true;
//                        if (collisionType != CollisionType.Bumper){
//                            collisionType = CollisionType.Water;
//                        }
//                    }
//
//                }
//            }
//
//            for (int i = 0; i < circles.size; i++) {
//                Ellipse circle = circles.get(i).getEllipse();
//                if (tempCollisionPoint.dst(circle.x + circle.height / 2f, circle.y + circle.height / 2f) < radius + circle.height / 2f) {
//                    normal.set(tempCollisionPoint);
//                    normal.sub(circle.x + circle.height / 2f, circle.y + circle.height / 2f);
//                    normal.nor();
//                    collisionPoint.set(circle.x + circle.height / 2f, circle.y + circle.height / 2f);
//                    normal.scl(radius + circle.height / 2f);
//                    collisionPoint.add(normal);
//                    normal.nor();
//                    unresolvedCollisions = true;
//                    if (collisionType != CollisionType.Bumper){
//                        collisionType = CollisionType.Wall;
//                    }
//                }
//            }
//
//            for (PinballBumper bumper : pinballBumpers) {
//                if (tempCollisionPoint.dst(bumper.position) < radius + bumper.radius) {
//                    bumper.isOn = true;
//                    normal.set(tempCollisionPoint);
//                    normal.sub(bumper.position);
//                    normal.nor();
//                    collisionPoint.set(bumper.position);
//                    normal.scl(bumper.radius + radius + 10);
//                    collisionPoint.add(normal);
//                    normal.nor();
//                    unresolvedCollisions = true;
//                    collisionType = CollisionType.Bumper;
//                }
//            }
//        }while(unresolvedCollisions && fuckThisShit < 10);

        // easy case is just within width of gameboard
        return false;

    }

}
