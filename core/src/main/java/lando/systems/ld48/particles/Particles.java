package lando.systems.ld48.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import lando.systems.ld48.Assets;
import lando.systems.ld48.levels.SpawnPickup;
import lando.systems.ld48.physics.PhysicsComponent;
import lando.systems.ld48.utils.Utils;

public class Particles implements Disposable {

    public enum Layer { background, middle, foreground }

    private static final int MAX_PARTICLES = 4000;

    private final Assets assets;
    private final ObjectMap<Layer, Array<Particle>> activeParticles;
    private final Pool<Particle> particlePool = Pools.get(Particle.class, MAX_PARTICLES);
    private Array<PhysicsComponent> physicsParticles;

    public Particles(Assets assets) {
        this.assets = assets;
        physicsParticles = new Array<>();
        this.activeParticles = new ObjectMap<>();
        int particlesPerLayer = MAX_PARTICLES / Layer.values().length;
        this.activeParticles.put(Layer.background, new Array<>(false, particlesPerLayer));
        this.activeParticles.put(Layer.middle,     new Array<>(false, particlesPerLayer));
        this.activeParticles.put(Layer.foreground, new Array<>(false, particlesPerLayer));
    }

    public void clear() {
        for (Layer layer : Layer.values()) {
            particlePool.freeAll(activeParticles.get(layer));
            activeParticles.get(layer).clear();
        }
    }

    public void update(float dt) {
        for (Layer layer : Layer.values()) {
            for (int i = activeParticles.get(layer).size - 1; i >= 0; --i) {
                Particle particle = activeParticles.get(layer).get(i);
                particle.update(dt);
                if (particle.isDead()) {
                    activeParticles.get(layer).removeIndex(i);
                    particlePool.free(particle);
                }
            }
        }
    }

    public Array<PhysicsComponent> getPhysicalParticles() {
        physicsParticles.clear();
        for (Layer layer : Layer.values()) {
            for (int i = activeParticles.get(layer).size -1; i >= 0; i--) {
                Particle particle = activeParticles.get(layer).get(i);
                if (particle.hasPhysics()) physicsParticles.add(particle);
            }
        }
        return physicsParticles;
    }

    public void draw(SpriteBatch batch, Layer layer) {
        activeParticles.get(layer).forEach(particle -> particle.render(batch));
    }

    @Override
    public void dispose() {
        clear();
    }

    // ------------------------------------------------------------------------
    // Helper fields for particle spawner methods
    // ------------------------------------------------------------------------
    private final Color tempColor = new Color();
    private final Vector2 tempVec2 = new Vector2();
    // ------------------------------------------------------------------------
    // Spawners for different particle effects
    // ------------------------------------------------------------------------

    public void pickup(float x, float y, SpawnPickup.Type pickupType) {
        Animation<TextureRegion> animation;
        switch (pickupType) {
            default:
            case dogecoin: animation = assets.dogeCoinAnimation; break;
            case bitcoin:  animation = assets.bitCoinAnimation;  break;
        }
        int numParticles = 20;
        float angle = 0;
        float speed = 20f;
        float increment = 36f;
        float ttl = 1f;
        for (int i = 0; i < numParticles; ++i) {
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .animation(animation)
                    .startPos(x, y)
                    .velocityDirection(angle, speed)
                    .startSize(16f)
                    .endSize(1f)
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .timeToLive(ttl)
                    .init());
            angle += increment;
            speed += 5f;
//            ttl += 0.1f;
            increment -= 20f / numParticles;
        }
    }

    private Color testColor = new Color();
    public void physics(float x, float y) {
        TextureRegion keyframe = assets.particles.circle;

        int numParticles = 200;
        for (int i = 0; i < numParticles; ++i) {
            Utils.hsvToRgb(MathUtils.random(0.0f, 0.5f), 1f, .9f, testColor);
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(x, y)
                    .velocityDirection(MathUtils.random(360f), MathUtils.random(30f, 1000f))
                    .startSize(5f)
                    .endSize(.1f)
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .timeToLive(3f)
                    .startColor(testColor)
                    .makePhysics()
                    .interpolation(Interpolation.fastSlow)
                    .init());
        }
    }

    public void smoke(float x, float y) {
        int numParticles = 100;
        for (int i = 0; i < numParticles; i++){
            float g = MathUtils.random(.7f) + .3f;
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(assets.particles.smoke)
                    .startPos(x + MathUtils.random(-30f, 30f), y + MathUtils.random(-30f, 30f))
                    .velocityDirection(MathUtils.random(360f), MathUtils.random(10f))
                    .startSize(MathUtils.random(20f, 40f))
                    .endSize(MathUtils.random(.1f, 10f))
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .startRotation(MathUtils.random(40))
                    .endRotation(MathUtils.random(-40, 80))
                    .timeToLive(MathUtils.random(1f, 3f))
                    .startColor(g, g, g, 1)
                    .init());

        }
    }

}
