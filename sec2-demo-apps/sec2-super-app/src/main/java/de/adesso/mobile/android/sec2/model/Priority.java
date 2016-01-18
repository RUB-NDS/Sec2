/**
 * 
 */
package de.adesso.mobile.android.sec2.model;

/**
 * @author benner
 *
 */
public enum Priority {
	HIGH(0), MIDDLE(1), LOW(2);
	
	private final Integer type;

    // Constructor
	Priority(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return this.type;
    }

    public static Priority getEnum(Integer type) {
        for (Priority ct : Priority.values()) {
            if (ct.getType().equals(type)) {
                return ct;
            }
        }
        return Priority.MIDDLE;
    }

}
