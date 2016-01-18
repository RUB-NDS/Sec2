/**
 * 
 */

package de.adesso.mobile.android.sec2.model;

/**
 * @author hoppe
 *
 */
public enum EventType {
    TODAY(0), EVENT(1), INVALID(2);

    private Integer mType;

    private EventType(final int type) {
        mType = type;
    }

    public Integer getType() {
        return this.mType;
    }

    public static EventType getEnum(final Integer type) {
        for (EventType et : EventType.values()) {
            if (et.getType().equals(type)) {
                return et;
            }
        }
        return EventType.INVALID;
    }

    public static EventType fromInt(final int i) {
        for (EventType eventType : EventType.values()) {
            if (eventType.mType == i) {
                return eventType;
            }
        }
        return EventType.INVALID;
    }

}
