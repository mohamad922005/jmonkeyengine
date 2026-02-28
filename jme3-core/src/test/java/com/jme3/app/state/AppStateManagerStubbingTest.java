package com.jme3.app.state;

import com.jme3.app.Application;
import com.jme3.app.LostFocusBehavior;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.Listener;
import com.jme3.input.InputManager;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.Timer;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * Demonstrates stubbing (hand-written test doubles) for AppStateManager.
 * No mocking framework is used here; instead we stub Application and AppState
 * to make lifecycle behavior observable and deterministic.
 */
public class AppStateManagerStubbingTest {

    /**
     * A minimal Application stub. Only getAppProfiler() is used by AppStateManager.update/render.
     * Everything else is irrelevant for these unit tests and throws UnsupportedOperationException
     * to catch accidental coupling.
     */
    private static final class StubApplication implements Application {
        @Override public AppProfiler getAppProfiler() { return null; }

        // --- The remainder of Application is not needed for these unit tests ---
        @Override public LostFocusBehavior getLostFocusBehavior() { throw new UnsupportedOperationException(); }
        @Override public void setLostFocusBehavior(LostFocusBehavior lostFocusBehavior) { throw new UnsupportedOperationException(); }
        @Override public boolean isPauseOnLostFocus() { throw new UnsupportedOperationException(); }
        @Override public void setPauseOnLostFocus(boolean pauseOnLostFocus) { throw new UnsupportedOperationException(); }
        @Override public void setSettings(AppSettings settings) { throw new UnsupportedOperationException(); }
        @Override public void setTimer(Timer timer) { throw new UnsupportedOperationException(); }
        @Override public Timer getTimer() { throw new UnsupportedOperationException(); }
        @Override public AssetManager getAssetManager() { throw new UnsupportedOperationException(); }
        @Override public InputManager getInputManager() { throw new UnsupportedOperationException(); }
        @Override public AppStateManager getStateManager() { throw new UnsupportedOperationException(); }
        @Override public RenderManager getRenderManager() { throw new UnsupportedOperationException(); }
        @Override public Renderer getRenderer() { throw new UnsupportedOperationException(); }
        @Override public AudioRenderer getAudioRenderer() { throw new UnsupportedOperationException(); }
        @Override public Listener getListener() { throw new UnsupportedOperationException(); }
        @Override public JmeContext getContext() { throw new UnsupportedOperationException(); }
        @Override public Camera getCamera() { throw new UnsupportedOperationException(); }
        @Override public void start() { throw new UnsupportedOperationException(); }
        @Override public void start(boolean waitFor) { throw new UnsupportedOperationException(); }
        @Override public void setAppProfiler(AppProfiler prof) { throw new UnsupportedOperationException(); }
        @Override public void restart() { throw new UnsupportedOperationException(); }
        @Override public void stop() { throw new UnsupportedOperationException(); }
        @Override public void stop(boolean waitFor) { throw new UnsupportedOperationException(); }
        @Override public <V> Future<V> enqueue(Callable<V> callable) { throw new UnsupportedOperationException(); }
        @Override public void enqueue(Runnable runnable) { throw new UnsupportedOperationException(); }
        @Override public ViewPort getGuiViewPort() { throw new UnsupportedOperationException(); }
        @Override public ViewPort getViewPort() { throw new UnsupportedOperationException(); }
    }

    /**
     * A hand-written AppState stub that records which lifecycle methods were invoked.
     */
    private static final class RecordingAppState implements AppState {
        int stateAttachedCalls = 0;
        int initializeCalls = 0;
        int updateCalls = 0;
        int stateDetachedCalls = 0;
        int cleanupCalls = 0;

        boolean enabled = true;
        boolean initialized = false;
        final String id;

        RecordingAppState(String id) {
            this.id = id;
        }

        @Override
        public void initialize(AppStateManager stateManager, Application app) {
            initializeCalls++;
            initialized = true;
        }

        @Override
        public boolean isInitialized() {
            return initialized;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setEnabled(boolean active) {
            enabled = active;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void stateAttached(AppStateManager stateManager) {
            stateAttachedCalls++;
        }

        @Override
        public void stateDetached(AppStateManager stateManager) {
            stateDetachedCalls++;
        }

        @Override
        public void update(float tpf) {
            updateCalls++;
        }

        @Override
        public void render(RenderManager rm) {
            // not used here
        }

        @Override
        public void postRender() {
            // not used here
        }

        @Override
        public void cleanup() {
            cleanupCalls++;
            initialized = false;
        }
    }

    @Test
    public void attachThenUpdate_shouldCallStateAttachedImmediately_thenInitializeOnce_thenUpdate() {
        Application app = new StubApplication();
        AppStateManager manager = new AppStateManager(app);

        RecordingAppState state = new RecordingAppState("S1");

        assertTrue(manager.attach(state));

        // stateAttached is synchronous during attach()
        assertEquals(1, state.stateAttachedCalls);
        assertEquals(0, state.initializeCalls);
        assertEquals(0, state.updateCalls);

        // initializePending + update happen in update()
        manager.update(0.016f);

        assertEquals(1, state.initializeCalls);
        assertEquals(1, state.updateCalls);
        assertTrue(state.isInitialized());

        // a second update should not re-initialize
        manager.update(0.016f);
        assertEquals(1, state.initializeCalls);
        assertEquals(2, state.updateCalls);
    }

    @Test
    public void detach_shouldCallStateDetachedImmediately_thenCleanupOnNextUpdate() {
        Application app = new StubApplication();
        AppStateManager manager = new AppStateManager(app);

        RecordingAppState state = new RecordingAppState("S2");
        manager.attach(state);
        manager.update(0.016f); // initializes it
        assertTrue(state.isInitialized());

        assertTrue(manager.detach(state));

        // stateDetached is synchronous during detach()
        assertEquals(1, state.stateDetachedCalls);
        assertEquals(0, state.cleanupCalls);

        // cleanup happens at the start of the next update()
        manager.update(0.016f);
        assertEquals(1, state.cleanupCalls);
        assertFalse(state.isInitialized());
    }
}
