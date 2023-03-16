package io.github.simonbas.screwbox.core.graphics;

import io.github.simonbas.screwbox.core.window.Window;

/**
 * Some predefined mouse cursors that you can use.
 *
 * @see Window#setCursor(MouseCursor)
 * @see Window#setFullscreenCursor(MouseCursor)
 * @see Window#setWindowCursor(MouseCursor)
 */
public enum MouseCursor {

    /**
     * The default cursor of the current operation system.
     */
    DEFAULT,

    /**
     * Not visible cursor.
     */
    HIDDEN;
}
