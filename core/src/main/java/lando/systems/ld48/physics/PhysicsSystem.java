package lando.systems.ld48.physics;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import lando.systems.ld48.levels.Level;
import lando.systems.ld48.particles.Particle;
import lando.systems.ld48.screens.GameScreen;
import lando.systems.ld48.utils.PointComparator;
import lando.systems.ld48.utils.QuadTree;
import lando.systems.ld48.utils.QuadTreeable;
import lando.systems.ld48.utils.Utils;

public class PhysicsSystem {

    private final Pool<Collision> collisionPool = Pools.get(Collision.class, 100);

    static final float GRAVITY = -800;

    private GameScreen screen;
    private Vector2 normal;
    Vector2 tempStart1 = new Vector2();
    Vector2 tempEnd1 = new Vector2();
    Vector2 frameEndPos = new Vector2();
    Vector2 tempStart2 = new Vector2();
    Vector2 tempEnd2 = new Vector2();
    Vector2 nearest1 = new Vector2();
    Vector2 nearest2 = new Vector2();
    Vector2 incomingVector = new Vector2();
    Vector2 nearestRight1 = new Vector2();
    Vector2 nearestRight2 = new Vector2();
    Vector2 collisionResult = new Vector2();
    Vector2 moveVector = new Vector2();
    Vector2 oldPos = new Vector2();
    Array<Vector2> projPoints = new Array<>();

    Array<Collision> collisions = new Array<>();
    public QuadTree collisionTree;
    Array<QuadTreeable> quadEntities = new Array<>();
    PointComparator sorter;
    float storeDT;



    public PhysicsSystem(GameScreen screen) {
        this.screen = screen;
        this.normal = new Vector2();
        for (int i = 0; i < 4; i++){
            projPoints.add(new Vector2());
        }
        TiledMapTileLayer collisionLayer = screen.level.getLayer(Level.LayerType.collision).tileLayer;
        float width = collisionLayer.getWidth() * collisionLayer.getTileWidth();
        float height = collisionLayer.getHeight() * collisionLayer.getTileHeight();
        collisionTree = new QuadTree(screen.game.assets, 0, new Rectangle(0,0, width, height));
        rebuildTree();
        sorter = new PointComparator();
        storeDT = 0;
    }

    public void update(float dt) {
        if (screen.level.isCollisionDirty()) {
            rebuildTree();
        }
        //update particles
        updateParticles(dt);
        updateGameEntities(dt);
    }

    public void renderDebug(SpriteBatch batch) {
        collisionTree.renderDebug(batch);
    }

    private void rebuildTree() {
        collisionTree.clear();
        for (Segment2D segment : screen.level.getCollisionSegments()){
            collisionTree.insert(segment);
        }
        screen.level.setCollisionDirty(false);
    }

    public boolean isPositionAboveGround(Vector2 pos) {
        return isPositionAboveGround(pos, 10);
    }
    public boolean isPositionAboveGround(Vector2 pos, float distanceToCheck) { return isPositionAboveGround(pos.x, pos.y, distanceToCheck);}
    public boolean isPositionAboveGround(float x, float y, float distanceToCheck) {
        tempStart1.set(x, y);
        tempEnd1.set(x, y - distanceToCheck);
//        collisionResult = checkSegmentCollision(tempStart1, tempEnd1, segment.start, segment.end, nearest1, nearest2, collisionResult);
        for (Segment2D segment : screen.level.getCollisionSegments()) {
            if (intersectSegments(tempStart1, tempEnd1, segment.start, segment.end, collisionResult)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPositionAboveGround(Segment2D segment) {
        for (Segment2D colSegment : screen.level.getCollisionSegments()) {
            if (intersectSegments(segment.start, segment.end, colSegment.start, colSegment.end, collisionResult)){
                return true;
            }
        }
        return false;
    }

    private void updateGameEntities(float dt) {
        Array<PhysicsComponent> entities = screen.physicsEntities;
        for (PhysicsComponent obj : entities) {
            Vector2 accel = obj.getAcceleration();
            Vector2 vel = obj.getVelocity();
            Vector2 pos = obj.getPosition();
            Vector3 impulse = obj.getImpulse();
            Rectangle bounds = (Rectangle) obj.getCollisionBounds();
            oldPos.set(pos);
            vel.x *= (float)Math.pow(.02f, dt);

            vel.x += accel.x * dt;
            float gravity = obj.isGrounded() ? 0 : (GRAVITY * obj.getGravityModifier());
            vel.y += (accel.y + gravity) * dt;
//            if (Math.abs(vel.y) < 4) vel.y = 0;

            if (impulse.z > 0){
                vel.x += impulse.x;
                vel.y += impulse.y;
                impulse.z -= dt;
            }

            float dtLeft = dt;
            moveVector.set(vel.x, vel.y);

            boolean hadCollision = true;
            int i = 0;
            while (hadCollision && i < 100 && dtLeft > 0){
                if (moveVector.len2() < .01) break;
                hadCollision = false;
                i++;
                moveVector.scl(dtLeft);
                checkCollisions(obj);
                float dtUsed = 0;
                for(Collision c : collisions) {
//                    if (c.distance.normal.dot(c.segment.normal) == 0) continue;
                    if (c.segment.normal.dot(moveVector) > 0) continue;

                    bounds = (Rectangle) obj.getCollisionBounds();
                    if (sweepRectSegment(bounds, c.segment, moveVector, c)) {
                        float s = Math.signum(c.distance.normal.dot(c.segment.normal));
                        pos.add(moveVector);
                        pos.add((c.distance.depth+.01f) * c.distance.normal.x * s, (c.distance.depth+.01f) * c.distance.normal.y * s);
                        tempStart1.set(c.segment.end).sub(c.segment.start);
                        float dot = tempEnd1.set(moveVector).dot(tempStart1);
                        moveVector.set(tempStart1.scl(dot / tempStart1.len2()));

                        dtUsed = dtLeft * MathUtils.clamp(1f - c.t, 0, 1f);
//                        moveVector.scl(MathUtils.clamp(1f - c.t, 0, 1f));
                        hadCollision = true;
                        obj.onCollision();
                        break;
                    }
                }
                moveVector.scl(1/dtLeft);
                dtLeft -= dtUsed;
                if (hadCollision) checkCollisions(obj);
//                handleCollision(c.startPos, c.segment, moveVector, tempEnd2);
            }

            boolean groundThisFrame = false;
            for (Segment2D segment : screen.level.getCollisionSegments()){
                for (int x = 0; x < bounds.width; x+= 16) {
                    tempStart1.set(bounds.x + x, bounds.y);
                    if (testGround(tempStart1, segment)) groundThisFrame = true;
                }
                tempStart1.set(bounds.x + bounds.width, bounds.y);
                if (testGround(tempStart1, segment)) groundThisFrame = true;
            }

            obj.setGrounded(groundThisFrame);
//            if (moveVector.len2() < .01f) moveVector.set(0,0);
            pos.add(moveVector.x * dtLeft, moveVector.y * dtLeft);
            float origLength = vel.len();
//            vel.set(moveVector).nor().scl(origLength);
            vel.set(moveVector);
        }
    }

    private void checkCollisions(PhysicsComponent obj){
        Rectangle bounds = (Rectangle) obj.getCollisionBounds();
        collisionPool.freeAll(collisions);
        collisions.clear();
        for (Segment2D segment : screen.level.getCollisionSegments()) {
            Collision c = collisionPool.obtain();
            try {
                if (sweepRectSegment(bounds, segment, moveVector, c)) {
                    collisions.add(c);
                } else {
                    collisionPool.free(c);
                }
            } catch (Exception e){
                // Had a polygon error here once, so if they go one frame inside so be it.
            }
        }
        collisions.sort();
    }

    private float dtAmount = .03f;
    private void updateParticles(float dt){
        storeDT += dt;
        if (storeDT > dtAmount){
            storeDT -= dtAmount;
            dt = dtAmount;
        } else {
            return;
        }
        Array<PhysicsComponent> particles = screen.particles.getPhysicalParticles();
        for(PhysicsComponent obj : particles){
            Vector2 accel = obj.getAcceleration();
            Vector2 vel = obj.getVelocity();
            Vector2 pos = obj.getPosition();
            float scale = obj.getBounceScale();
            Circle bounds = (Circle) obj.getCollisionBounds();

            vel.scl((float)Math.pow(.4f, dt));

            vel.x += accel.x * dt;
            vel.y += (accel.y + GRAVITY) * dt;


            float nextX = pos.x + vel.x * dt;
            float nextY = pos.y + vel.y * dt;
            tempStart1.set(pos);
            tempEnd1.set(nextX, nextY);
            frameEndPos.set(tempEnd1);

            quadEntities.clear();
            collisionTree.retrieve(quadEntities, (Particle) obj);

            for (QuadTreeable entity : quadEntities) {
                Segment2D segment = (Segment2D)entity;
                collisionResult = checkSegmentCollision(tempStart1, tempEnd1, segment.start, segment.end, nearest1, nearest2, collisionResult);
                if (collisionResult.x != Float.MAX_VALUE && nearest1.dst2(nearest2) < (bounds.radius + 1f) * (bounds.radius + 1f)){
                    frameEndPos.set(nearest1);
                    float backupDist = (bounds.radius + 1.1f) - nearest1.dst(nearest2);
                    float x = frameEndPos.x + backupDist * (segment.normal.x);
                    float y = frameEndPos.y + backupDist * (segment.normal.y);
                    frameEndPos.set(x, y);

                    vel.scl(scale);
                    vel.set(Utils.reflectVector(incomingVector.set(vel), segment.normal));

//                    if (vel.epsilonEquals(0, 0, 6f)) {
//                        ((Particle)obj).setPhysics(false);
//                        vel.set(0,0);
//                    }
                }
            }
            pos.set(frameEndPos);
        }
    }

//    private void handleCollision(Vector2 startPos, Segment2D segment, Vector2 movement, Vector2 end) {
//        tempStart1.set(startPos.x, startPos.y);
//        tempEnd1.set(startPos.x + movement.x, startPos.y + movement.y);
////        collisionResult = checkSegmentCollision(tempStart1, tempEnd1, segment.start, segment.end, nearest1, nearest2, collisionResult);
//        if (intersectSegments(tempStart1, tempEnd1, segment.start, segment.end, collisionResult)){
//            tempStart1.set(segment.end).sub(segment.start);
//            float dot = tempEnd1.set(movement).dot(tempStart1);
//            movement.set(tempStart1.scl(dot/ tempStart1.len2()));
//        }
//    }

    private Array<Vector2> touchPoints = new Array<>();
    private Vector2 intersectionPoint = new Vector2();
    private float[] rectVerts = new float[8];
    private float[] velVerts = new float[8];
    private Polygon rectPoly = new Polygon();
    private Polygon velPoly = new Polygon();
    private Polygon overlapPoly = new Polygon();
    private Intersector.MinimumTranslationVector transVector = new Intersector.MinimumTranslationVector();
    private boolean sweepRectSegment(Rectangle rect, Segment2D segment, Vector2 v, Collision collision) {
        rectVerts[0] = rect.x; rectVerts[1] = rect.y;
        rectVerts[2] = rect.x; rectVerts[3] = rect.y + rect.height;
        rectVerts[4] = rect.x + rect.width; rectVerts[5] = rect.y + rect.height;
        rectVerts[6] = rect.x + rect.width; rectVerts[7] = rect.y;


        projPoints.get(0).set(segment.start);
        projPoints.get(1).set(segment.start.x - v.x, segment.start.y - v.y);
        projPoints.get(2).set(segment.end);
        projPoints.get(3).set(segment.end.x - v.x, segment.end.y - v.y);

        sorter.center = findCentroid(projPoints);
        projPoints.sort(sorter);
        for (int i = 0; i < 4; i ++){
            velVerts[i*2] = projPoints.get(i).x;
            velVerts[i*2 +1] = projPoints.get(i).y;
        }
        if (Intersector.overlapConvexPolygons(rectVerts, velVerts, transVector)) {
            rectPoly.setVertices(rectVerts);
            velPoly.setVertices(velVerts);
            collision.init(segment, transVector);
            collision.rect.set(rect);
            collision.t = transVector.depth / v.len();
            collision.velocity.set(v);
            if (Intersector.intersectPolygons(rectPoly, velPoly, overlapPoly)) {
                collision.polygon.setVertices(overlapPoly.getVertices());
                collision.polygon.setOrigin(overlapPoly.getOriginX(), overlapPoly.getOriginY());
                collision.polygon.setPosition(overlapPoly.getX(), overlapPoly.getY());
                collision.polygon.setRotation(overlapPoly.getRotation());
                collision.polygon.setScale(overlapPoly.getScaleX(), overlapPoly.getScaleY());
            } else {
                velVerts[0] = 0;
                velVerts[1] = 0;
                velVerts[2] = transVector.depth;
                velVerts[3] = 0;
                velVerts[4] = transVector.depth;
                velVerts[5] = transVector.depth;

                collision.polygon.setVertices(velVerts);
            }
            return true;

        }
        return false;
    }

    private boolean testGround(Vector2 startPos, Segment2D segment){
        tempStart1.set(startPos.x, startPos.y + 2);
        tempEnd1.set(startPos.x, startPos.y - 8);
//        collisionResult = checkSegmentCollision(tempStart1, tempEnd1, segment.start, segment.end, nearest1, nearest2, collisionResult);
        if (intersectSegments(tempStart1, tempEnd1, segment.start, segment.end, collisionResult)){
            return true;
        }
        return false;
    }

    private Vector2 findCentroid(Array<Vector2> points) {
        float x = 0;
        float y = 0;
        for (Vector2 v : points) {
            x += v.x;
            y += v.y;
        }
        tempStart1.set(x / points.size, y / points.size);
        return tempStart1;
    }
//    private void testDownRay(Vector2 start, )

    private Vector2 d1 = new Vector2();
    private Vector2 d2 = new Vector2();
    private Vector2 r = new Vector2();
    private Vector2 checkSegmentCollision(Vector2 seg1Start, Vector2 seg1End, Vector2 seg2Start, Vector2 seg2End, Vector2 nearestSeg1, Vector2 nearestSeg2, Vector2 results){
        d1.set(seg1End).sub(seg1Start);
        d2.set(seg2End).sub(seg2Start);
        r.set(seg1Start).sub(seg2Start);

        float a = d1.dot(d1);
        float e = d2.dot(d2);
        float f = d2.dot(r);

        float b = d1.dot(d2);
        float c = d1.dot(r);

        float s = 0;
        float t = 0;

        float denom = a*e-b*b;
        if (denom != 0){
            s = MathUtils.clamp((b*f - c*e)/denom, 0f, 1f);
        } else {
            // Parallel
            return results.set(Float.MAX_VALUE, Float.MAX_VALUE);
        }

        t = (b*s + f) /e;
        if (t < 0) {
            t = 0;
            s = MathUtils.clamp(-c /a, 0, 1);
        } else if (t > 1) {
            t = 1;
            s = MathUtils.clamp((b-c)/a, 0, 1);
        }

        nearestSeg1.set(seg1Start).add(d1.scl(s));
        nearestSeg2.set(seg2Start).add(d2.scl(t));
        return results.set(s, t);
    }

    private Vector2 b = new Vector2();
    private Vector2 d = new Vector2();
    private Vector2 c = new Vector2();
    private boolean intersectSegments(Vector2 start1, Vector2 end1, Vector2 start2, Vector2 end2, Vector2 intersection) {
        intersection.set(0,0);
        b.set(end1).sub(start1);
        d.set(end2).sub(start2);

        float dot = b.x * d.y - b.y * d.x;
        if (dot == 0) return false;

        c.set(start2).sub(start1);
        float t = (c.x * d.y - c.y * d.x) / dot;
        float u = (c.x * b.y - c.y * b.x) / dot;
        if (t < 0 || t > 1 || u < 0 || u > 1) return false;

        intersection.set(start1).add(b.scl(t));
        return true;
    }

}
