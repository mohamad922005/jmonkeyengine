package com.jme3.app.state;

import com.jme3.app.Application;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.RenderManager;
import org.junit.Test;
import org.mockito.InOrder;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Demonstrates Mockito-based mocking for AppStateManager interaction testing.
 * These tests focus on behavior-checking: verifying specific calls happen
 * (and in which order), which is difficult to do with plain assertions alone.
 */
public class AppStateManagerMockitoTest {

    @Test
    public void attachAndUpdate_shouldCallLifecycleMethods_inCorrectOrder() {
        Application app = mock(Application.class);
        when(app.getAppProfiler()).thenReturn((AppProfiler) null);

        AppStateManager manager = new AppStateManager(app);

        AppState state = mock(AppState.class);
        when(state.isEnabled()).thenReturn(true); // so update() is executed

        assertTrue(manager.attach(state));

        // stateAttached happens immediately during attach()
        verify(state, times(1)).stateAttached(manager);

        // initialize/update happen during manager.update()
        manager.update(0.1f);

        InOrder inOrder = inOrder(state);
        inOrder.verify(state).stateAttached(manager);
        inOrder.verify(state).initialize(manager, app);
        inOrder.verify(state).update(0.1f);

    }

    @Test
    public void detach_thenNextUpdate_shouldCallStateDetached_thenCleanup() {
        Application app = mock(Application.class);
        when(app.getAppProfiler()).thenReturn((AppProfiler) null);

        AppStateManager manager = new AppStateManager(app);

        AppState state = mock(AppState.class);
        when(state.isEnabled()).thenReturn(true);

        manager.attach(state);
        manager.update(0.016f); // initialize + update

        assertTrue(manager.detach(state));

        // stateDetached synchronous in detach()
        verify(state, atLeastOnce()).stateDetached(manager);

        // cleanup happens on next update()
        manager.update(0.016f);
        verify(state, atLeastOnce()).cleanup();
    }

    @Test
    public void render_shouldCallRenderOnEnabledStates_onlyAfterInitialization() {
        Application app = mock(Application.class);
        when(app.getAppProfiler()).thenReturn((AppProfiler) null);

        AppStateManager manager = new AppStateManager(app);
        RenderManager rm = mock(RenderManager.class);

        AppState state = mock(AppState.class);
        when(state.isEnabled()).thenReturn(true);

        manager.attach(state);

        // Not initialized until update() is called => render() should do nothing for it yet.
        manager.render(rm);
        verify(state, never()).render(rm);

        // After update(), state is initialized and running.
        manager.update(0.016f);
        manager.render(rm);
        verify(state, times(1)).render(rm);
    }
}
