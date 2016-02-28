package com.alexvasilkov.gestures;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;

import com.alexvasilkov.gestures.internal.MovementBounds;

/**
 * Helper class that holds reference to {@link Settings} object and controls some aspects of view
 * {@link State}, such as movement bounds restrictions
 * (see {@link #getEffectiveMovementArea(RectF, State)}) and dynamic min / max zoom levels
 * (see {@link #getEffectiveMinZoom()} and {@link #getEffectiveMaxZoom()}).
 * <p/>
 * It also provides few static methods that can be useful:
 * <ul>
 * <li>{@link #restrict(float, float, float) restrict()} float value,</li>
 * <li>{@link #interpolate(float, float, float) interpolate()} float value</li>
 * <li>{@link #interpolate(State, State, State, float) interpolate()} {@link State} object</li>
 * <li>{@link #interpolate(State, State, float, float, State, float, float, float) interpolate()}
 * {@link State} object with specified pivot point</li>
 * </ul>
 */
public class StateController {

    // Temporary objects
    private static final State TMP_STATE = new State();
    private static final Matrix TMP_MATRIX = new Matrix();
    private static final RectF TMP_RECT_F = new RectF();
    private static final MovementBounds TMP_MOV_BOUNDS = new MovementBounds();

    private final Settings mSettings;

    private boolean mIsResetRequired = true;

    /**
     * Values to store calculated values for min / max zoom levels
     */
    private float mMinZoom, mMaxZoom;

    StateController(Settings settings) {
        mSettings = settings;
    }

    /**
     * Resets to initial state (min zoom, position according to gravity). Reset will only occur
     * when image and viewport sizes are known, otherwise reset will occur sometime in the future
     * when {@link #updateState(State)} method will be called.
     *
     * @return {@code true} if reset was completed or {@code false} if reset is scheduled for future
     */
    boolean resetState(State state) {
        mIsResetRequired = true;
        return updateState(state);
    }

    /**
     * Updates state (or resets state if reset was scheduled, see {@link #resetState(State)}).
     *
     * @return {@code true} if state was reset to initial state or {@code false} if state was
     * updated.
     */
    boolean updateState(State state) {
        if (mIsResetRequired) {
            // We can correctly reset state only when we have both image size and viewport size
            // but there can be a delay before we have all values properly set
            // (waiting for layout or waiting for image to be loaded)
            state.set(0f, 0f, 1f, 0f);
            boolean updated = adjustZoomLevels(state);
            state.set(0f, 0f, mMinZoom, 0f);
            MovementBounds.setupInitialMovement(state, mSettings);

            mIsResetRequired = !updated;
            return !mIsResetRequired;
        } else {
            restrictStateBounds(state);
            return false;
        }
    }

    /**
     * @return Min zoom level as it's used by state controller.
     */
    public float getEffectiveMinZoom() {
        return mMinZoom;
    }

    /**
     * @return Max zoom level as it's used by state controller.
     * Note, that it may be different than {@link Settings#getMaxZoom()}.
     */
    @SuppressWarnings("unused") // Public API
    public float getEffectiveMaxZoom() {
        return mMaxZoom;
    }

    /**
     * Maximizes zoom if it closer to min zoom or minimizes it if it closer to max zoom
     *
     * @return End state for toggle animation
     */
    State toggleMinMaxZoom(State state, float pivotX, float pivotY) {
        adjustZoomLevels(state); // Calculating zoom levels

        final float middleZoom = (mMinZoom + mMaxZoom) / 2f;
        final float targetZoom = state.getZoom() < middleZoom ? mMaxZoom : mMinZoom;

        State end = state.copy();
        end.zoomTo(targetZoom, pivotX, pivotY);
        return end;
    }

    /**
     * Restricts state's translation and zoom bounds, disallowing overscroll / overzoom.
     */
    boolean restrictStateBounds(State state) {
        return restrictStateBounds(state, null, Float.NaN, Float.NaN, false, false, true);
    }

    /**
     * Restricts state's translation and zoom bounds.
     *
     * @return End state to animate changes or null if no changes are required
     */
    @Nullable
    State restrictStateBoundsCopy(State state, State prevState, float pivotX, float pivotY,
            boolean allowOverscroll, boolean allowOverzoom, boolean restrictRotation) {
        TMP_STATE.set(state);
        boolean changed = restrictStateBounds(TMP_STATE, prevState, pivotX, pivotY,
                allowOverscroll, allowOverzoom, restrictRotation);
        return changed ? TMP_STATE.copy() : null;
    }

    /**
     * Restricts state's translation and zoom bounds. If {@code prevState} is not null and
     * {@code allowOverscroll (allowOverzoom)} parameter is true than resilience
     * will be applied to translation (zoom) changes if they are out of bounds.
     *
     * @return true if state was changed, false otherwise
     */
    boolean restrictStateBounds(State state, State prevState, float pivotX, float pivotY,
            boolean allowOverscroll, boolean allowOverzoom, boolean restrictRotation) {

        if (!mSettings.isRestrictBounds()) {
            return false;
        }

        // Calculating default pivot point, if not provided
        if (Float.isNaN(pivotX) || Float.isNaN(pivotY)) {
            Point pivot = MovementBounds.getDefaultPivot(mSettings);
            pivotX = pivot.x;
            pivotY = pivot.y;
        }

        boolean isStateChanged = false;

        if (restrictRotation && mSettings.isRestrictRotation()) {
            float rotation = Math.round(state.getRotation() / 90f) * 90f;
            if (!State.equals(rotation, state.getRotation())) {
                state.rotateTo(rotation, pivotX, pivotY);
                isStateChanged = true;
            }
        }

        adjustZoomLevels(state); // Calculating zoom levels

        float overzoom = allowOverzoom ? mSettings.getOverzoomFactor() : 1f;

        float zoom = restrict(state.getZoom(), mMinZoom / overzoom, mMaxZoom * overzoom);

        // Applying elastic overzoom
        if (prevState != null) {
            zoom = applyZoomResilience(zoom, prevState.getZoom(), overzoom);
        }

        if (!State.equals(zoom, state.getZoom())) {
            state.zoomTo(zoom, pivotX, pivotY);
            isStateChanged = true;
        }

        MovementBounds bounds = getMovementBounds(state);
        float overscrollX = allowOverscroll ? mSettings.getOverscrollDistanceX() : 0f;
        float overscrollY = allowOverscroll ? mSettings.getOverscrollDistanceY() : 0f;

        PointF tmpPos = bounds.restrict(state.getX(), state.getY(), overscrollX, overscrollY);
        float x = tmpPos.x;
        float y = tmpPos.y;

        if (zoom < mMinZoom) {
            // Decreasing overscroll if zooming less than minimum zoom
            float minZoom = mMinZoom / overzoom;
            float factor = (zoom - minZoom) / (mMinZoom - minZoom);
            factor = (float) Math.sqrt(factor);

            tmpPos = bounds.restrict(x, y);
            float strictX = tmpPos.x;
            float strictY = tmpPos.y;

            x = strictX + factor * (x - strictX);
            y = strictY + factor * (y - strictY);
        }

        if (prevState != null) {
            RectF extBounds = bounds.getExternalBounds();
            x = applyTranslationResilience(x, prevState.getX(),
                    extBounds.left, extBounds.right, overscrollX);
            y = applyTranslationResilience(y, prevState.getY(),
                    extBounds.top, extBounds.bottom, overscrollY);
        }

        if (!State.equals(x, state.getX()) || !State.equals(y, state.getY())) {
            state.translateTo(x, y);
            isStateChanged = true;
        }

        return isStateChanged;
    }

    private float applyZoomResilience(float zoom, float prevZoom, float overzoom) {
        if (overzoom == 1f) {
            return zoom;
        }

        float minZoom = mMinZoom / overzoom;
        float maxZoom = mMaxZoom * overzoom;

        float resilience = 0f;

        if (zoom < mMinZoom && zoom < prevZoom) {
            resilience = (mMinZoom - zoom) / (mMinZoom - minZoom);
        } else if (zoom > mMaxZoom && zoom > prevZoom) {
            resilience = (zoom - mMaxZoom) / (maxZoom - mMaxZoom);
        }

        if (resilience == 0f) {
            return zoom;
        } else {
            float factor = zoom / prevZoom;
            factor += (float) Math.sqrt(resilience) * (1f - factor);
            return prevZoom * factor;
        }
    }

    private float applyTranslationResilience(float value, float prevValue,
            float boundsMin, float boundsMax, float overscroll) {
        if (overscroll == 0f) {
            return value;
        }

        float resilience = 0f;

        float avg = (value + prevValue) * 0.5f;

        if (avg < boundsMin && value < prevValue) {
            resilience = (boundsMin - avg) / overscroll;
        } else if (avg > boundsMax && value > prevValue) {
            resilience = (avg - boundsMax) / overscroll;
        }

        if (resilience == 0f) {
            return value;
        } else {
            if (resilience > 1f) {
                resilience = 1f;
            }
            float delta = value - prevValue;
            delta *= (1f - (float) Math.sqrt(resilience));
            return prevValue + delta;
        }
    }


    /**
     * Do note store returned object, since it will be reused next time this method is called.
     */
    MovementBounds getMovementBounds(State state) {
        TMP_MOV_BOUNDS.setup(state, mSettings);
        return TMP_MOV_BOUNDS;
    }


    /**
     * Returns area in which {@link State#getX()} & {@link State#getY()} values can change.
     * Note, that this is different than {@link Settings#setMovementArea(int, int)} which defines
     * part of the viewport in which image can move.
     *
     * @param out Result will be stored in this rect.
     * @param state State for which to calculate bounds.
     */
    @SuppressWarnings("unused") // Public API
    public void getEffectiveMovementArea(RectF out, State state) {
        out.set(getMovementBounds(state).getExternalBounds());
    }

    /**
     * Adjusting min and max zoom levels.
     *
     * @return true if zoom levels was correctly updated (image and viewport sizes are known),
     * false otherwise
     */
    private boolean adjustZoomLevels(State state) {
        mMaxZoom = mSettings.getMaxZoom();

        float fittingZoom = 1f;

        boolean isCorrectSize = mSettings.hasImageSize() && mSettings.hasViewportSize();

        if (isCorrectSize) {
            float w = mSettings.getImageW(), h = mSettings.getImageH();

            float areaW = mSettings.getMovementAreaW();
            float areaH = mSettings.getMovementAreaH();

            if (mSettings.getFitMethod() == Settings.Fit.OUTSIDE) {
                // Computing movement area size taking rotation into account. We need to inverse
                // rotation, since it will be applied to the area, not to the image itself.
                TMP_MATRIX.setRotate(-state.getRotation());
                TMP_RECT_F.set(0, 0, areaW, areaH);
                TMP_MATRIX.mapRect(TMP_RECT_F);
                areaW = TMP_RECT_F.width();
                areaH = TMP_RECT_F.height();
            } else {
                // Computing image bounding size taking rotation into account.
                TMP_MATRIX.setRotate(state.getRotation());
                TMP_RECT_F.set(0, 0, w, h);
                TMP_MATRIX.mapRect(TMP_RECT_F);
                w = TMP_RECT_F.width();
                h = TMP_RECT_F.height();
            }

            switch (mSettings.getFitMethod()) {
                case HORIZONTAL:
                    fittingZoom = areaW / w;
                    break;
                case VERTICAL:
                    fittingZoom = areaH / h;
                    break;
                case OUTSIDE:
                    fittingZoom = Math.max(areaW / w, areaH / h);
                    break;
                case INSIDE:
                default:
                    fittingZoom = Math.min(areaW / w, areaH / h);
                    break;
            }
        }

        if (fittingZoom > mMaxZoom) {
            if (mSettings.isFillViewport()) {
                // zooming to fill entire viewport
                mMinZoom = mMaxZoom = fittingZoom;
            } else {
                // restricting min zoom
                mMinZoom = mMaxZoom;
            }
        } else {
            mMinZoom = fittingZoom;
            if (!mSettings.isZoomEnabled()) {
                mMaxZoom = mMinZoom;
            }
        }

        return isCorrectSize;
    }

    public static float restrict(float value, float minValue, float maxValue) {
        return Math.max(minValue, Math.min(value, maxValue));
    }

    /**
     * Interpolates from start state to end state by given factor (from 0 to 1),
     * storing result into out state.
     */
    public static void interpolate(State out, State start, State end, float factor) {
        interpolate(out, start, start.getX(), start.getY(), end, end.getX(), end.getY(), factor);
    }

    /**
     * Interpolates from start state to end state by given factor (from 0 to 1),
     * storing result into out state. All operations (translation, zoom, rotation) will be
     * performed within specified pivot points, assuming start and end pivot points represent
     * same physical point on the image.
     */
    public static void interpolate(State out, State start, float startPivotX, float startPivotY,
            State end, float endPivotX, float endPivotY, float factor) {
        out.set(start);

        if (!State.equals(start.getZoom(), end.getZoom())) {
            float zoom = interpolate(start.getZoom(), end.getZoom(), factor);
            out.zoomTo(zoom, startPivotX, startPivotY);
        }

        // Getting rotations
        float startRotation = start.getRotation();
        float endRotation = end.getRotation();

        float rotation = Float.NaN;

        // Choosing shortest path to interpolate
        if (Math.abs(startRotation - endRotation) <= 180f) {
            if (!State.equals(startRotation, endRotation)) {
                rotation = interpolate(startRotation, endRotation, factor);
            }
        } else {
            // Keeping rotation positive
            float startRotationPositive = startRotation < 0f ? startRotation + 360f : startRotation;
            float endRotationPositive = endRotation < 0f ? endRotation + 360f : endRotation;

            if (!State.equals(startRotationPositive, endRotationPositive)) {
                rotation = interpolate(startRotationPositive, endRotationPositive, factor);
            }
        }

        if (!Float.isNaN(rotation)) {
            out.rotateTo(rotation, startPivotX, startPivotY);
        }

        float dx = interpolate(0, endPivotX - startPivotX, factor);
        float dy = interpolate(0, endPivotY - startPivotY, factor);
        out.translateBy(dx, dy);
    }

    public static float interpolate(float start, float end, float factor) {
        return start + (end - start) * factor;
    }

}
