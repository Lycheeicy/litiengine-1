package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.EventListener;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;

public class Spawnpoint extends Entity {
  private final Collection<EntitySpawnedListener> spawnedListeners = ConcurrentHashMap.newKeySet();

  @TmxProperty(name = MapObjectProperty.SPAWN_DIRECTION)
  private Direction direction;

  @TmxProperty(name = MapObjectProperty.SPAWN_TYPE)
  private String spawnType;

  public Spawnpoint() {
  }

  public Spawnpoint(double x, double y) {
    this(0, x, y);
  }

  public Spawnpoint(int mapId, double x, double y) {
    this(mapId, new Point2D.Double(x, y));
  }

  public Spawnpoint(int mapId, Point2D point) {
    super(mapId);
    this.setLocation(point);
  }

  public Spawnpoint(int mapId, double x, double y, Direction direction) {
    this(mapId, new Point2D.Double(x, y), direction);
  }

  public Spawnpoint(int mapId, Point2D point, Direction direction) {
    this(mapId, point);
    this.setDirection(direction);
  }

  public Spawnpoint(Direction direction) {
    this.setDirection(direction);
  }

  public Spawnpoint(Direction direction, String spawnType) {
    this(direction);
    this.setSpawnType(spawnType);
  }
  
  public void onSpawned(EntitySpawnedListener listener) {
    this.spawnedListeners.add(listener);
  }

  public void removeSpawnedListener(EntitySpawnedListener listener) {
    this.spawnedListeners.remove(listener);
  }

  public Direction getDirection() {
    return direction;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  public String getSpawnType() {
    return spawnType;
  }

  public void setSpawnType(String spawnType) {
    this.spawnType = spawnType;
  }

  /**
   * Spawns the specified entity to the <code>Environment</code> of the <code>Spawnpoint</code> or the currently active <code>Environment</code>.
   * 
   * <p>
   * Spawning will set the location of the entity to the location defined by the spawnpoint and optionally also set the angle of the entity,
   * if a spawn direction is defined.
   * </p>
   * 
   * @param entity
   *          The entity to spawn at the specified location.
   * @return True if the entity was spawned; otherwise false, which is typically the case if no environment is loaded.
   * 
   * @see GameWorld#environment()
   */
  public boolean spawn(IEntity entity) {
    Environment env = this.getEnvironment();
    if (env == null) {
      env = Game.world().environment();
    }

    if (env == null) {
      return false;
    }

    entity.setLocation(this.getLocation());

    if (this.getDirection() != null && this.getDirection() != Direction.UNDEFINED) {
      entity.setAngle(this.getDirection().toAngle());
    }

    if (env.get(entity.getMapId()) == null) {
      env.add(entity);
    }

    final EntitySpawnedEvent event = new EntitySpawnedEvent(this, entity);
    for (EntitySpawnedListener listener : this.spawnedListeners) {
      listener.spawned(event);
    }

    return true;
  }

  @FunctionalInterface
  public interface EntitySpawnedListener extends EventListener {
    void spawned(EntitySpawnedEvent event);
  }
}