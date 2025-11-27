package com.arcane.onslaught.input;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.PlayerComponent;
import com.arcane.onslaught.entities.components.VelocityComponent;
import com.arcane.onslaught.events.EventManager;
import com.arcane.onslaught.events.PlayerInputEvent;
import com.arcane.onslaught.utils.Constants;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles keyboard input and publishes PlayerInputEvents
 */
public class GameInputProcessor extends InputAdapter {

    private Set<PlayerInputEvent.Direction> activeDirections = new HashSet<>();
    private Vector2 inputVector = new Vector2();

    @Override
    public boolean keyDown(int keycode) {
        boolean changed = false;

        switch (keycode) {
            case Input.Keys.W:
            case Input.Keys.UP:
                changed = activeDirections.add(PlayerInputEvent.Direction.UP);
                break;
            case Input.Keys.S:
            case Input.Keys.DOWN:
                changed = activeDirections.add(PlayerInputEvent.Direction.DOWN);
                break;
            case Input.Keys.A:
            case Input.Keys.LEFT:
                changed = activeDirections.add(PlayerInputEvent.Direction.LEFT);
                break;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                changed = activeDirections.add(PlayerInputEvent.Direction.RIGHT);
                break;
        }

        if (changed) {
            updatePlayerVelocity();
        }

        return changed;
    }

    @Override
    public boolean keyUp(int keycode) {
        boolean changed = false;

        switch (keycode) {
            case Input.Keys.W:
            case Input.Keys.UP:
                changed = activeDirections.remove(PlayerInputEvent.Direction.UP);
                break;
            case Input.Keys.S:
            case Input.Keys.DOWN:
                changed = activeDirections.remove(PlayerInputEvent.Direction.DOWN);
                break;
            case Input.Keys.A:
            case Input.Keys.LEFT:
                changed = activeDirections.remove(PlayerInputEvent.Direction.LEFT);
                break;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                changed = activeDirections.remove(PlayerInputEvent.Direction.RIGHT);
                break;
        }

        if (changed) {
            updatePlayerVelocity();
        }

        return changed;
    }

    /**
     * Updates player velocity based on active input directions.
     * This directly modifies the player's velocity component rather than
     * just publishing an event, for more responsive controls.
     */
    private void updatePlayerVelocity() {
        inputVector.set(0, 0);

        // Calculate input vector
        if (activeDirections.contains(PlayerInputEvent.Direction.UP)) {
            inputVector.y += 1;
        }
        if (activeDirections.contains(PlayerInputEvent.Direction.DOWN)) {
            inputVector.y -= 1;
        }
        if (activeDirections.contains(PlayerInputEvent.Direction.LEFT)) {
            inputVector.x -= 1;
        }
        if (activeDirections.contains(PlayerInputEvent.Direction.RIGHT)) {
            inputVector.x += 1;
        }

        // Normalize for diagonal movement (so diagonal isn't faster)
        if (inputVector.len() > 0) {
            inputVector.nor();
        }

        // Scale by player speed
        inputVector.scl(Constants.PLAYER_SPEED);

        // Publish event (for listeners that need to know about input)
        EventManager.getInstance().publish(new PlayerInputEvent(new HashSet<>(activeDirections)));
    }

    /**
     * Helper method to set player velocity directly.
     * Call this from systems that have access to the Ashley engine.
     */
    public static void setPlayerVelocity(Entity player, Vector2 velocity) {
        VelocityComponent vel = player.getComponent(VelocityComponent.class);
        if (vel != null) {
            vel.velocity.set(velocity);
        }
    }

    public Vector2 getInputVector() {
        return inputVector;
    }
}
