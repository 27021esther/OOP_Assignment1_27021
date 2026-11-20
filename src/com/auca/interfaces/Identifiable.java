package com.auca.interfaces;

/** Anything that has an ID, a display name and an active flag. */
public interface Identifiable {
    String getId();
    String getDisplayName();
    boolean isActive();
}
