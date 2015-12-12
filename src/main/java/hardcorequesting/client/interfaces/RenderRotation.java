package hardcorequesting.client.interfaces;

public enum RenderRotation {
    NORMAL,
    ROTATE_90,
    ROTATE_180,
    ROTATE_270,

    FLIP_HORIZONTAL,
    ROTATE_90_FLIP,
    FLIP_VERTICAL,
    ROTATE_270_FLIP;

    public RenderRotation getNextRotation() {
        switch (this) {
            default:
            case NORMAL:
                return ROTATE_90;
            case ROTATE_90:
                return ROTATE_180;
            case ROTATE_180:
                return ROTATE_270;
            case ROTATE_270:
                return NORMAL;

            case FLIP_HORIZONTAL:
                return ROTATE_90_FLIP;
            case ROTATE_90_FLIP:
                return FLIP_VERTICAL;
            case FLIP_VERTICAL:
                return ROTATE_270_FLIP;
            case ROTATE_270_FLIP:
                return FLIP_HORIZONTAL;
        }
    }

    public RenderRotation getFlippedRotation() {
        switch (this) {
            default:
            case NORMAL:
                return FLIP_HORIZONTAL;
            case ROTATE_90:
                return ROTATE_90_FLIP;
            case ROTATE_180:
                return FLIP_VERTICAL;
            case ROTATE_270:
                return ROTATE_270_FLIP;

            case FLIP_HORIZONTAL:
                return NORMAL;
            case ROTATE_90_FLIP:
                return ROTATE_90;
            case FLIP_VERTICAL:
                return ROTATE_180;
            case ROTATE_270_FLIP:
                return ROTATE_270;
        }
    }

}
