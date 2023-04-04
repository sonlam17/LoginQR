package com.secsign.java.rest;

/**
 * A class which represents a SecSign ID Server version.
 */
public class SecSignIDServerVersion {
    /**
     * Unkown server version (all versions before 10.7.5).
     */
    public final static SecSignIDServerVersion VERSION_UNKNOWN = new SecSignIDServerVersion(0, 0, 0);

    /**
     * Server version 10.7.5:
     * - FIDO
     * - Access Tokens
     * - Plugin Registration
     * - Server Info
     */
    public final static SecSignIDServerVersion VERSION_10_7_5 = new SecSignIDServerVersion(10, 7, 5);

    /**
     * The major version.
     */
    private final int major;

    /**
     * The minor version.
     */
    private final int minor;

    /**
     * The patch version.
     */
    private final int patch;

    /**
     * Constructor for building the version from the version parts.
     * @param major the major version
     * @param minor the minor version
     * @param patch the patch version
     */
    public SecSignIDServerVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * Get the major version.
     * @return the major version
     */
    public int getMajor() {
        return major;
    }

    /**
     * Get the minor version.
     * @return the minor version
     */
    public int getMinor() {
        return minor;
    }

    /**
     * Get the patch version.
     * @return the patch version.
     */
    public int getPatch() {
        return patch;
    }

    /**
     * Checks whether this {@link SecSignIDServerVersion} instance is greater or equals than {@code otherVersion},
     * @param otherVersion the other {@link SecSignIDServerVersion}
     * @return true if this instance is greater or equals than {@code otherVersion}, otherwise false
     */
    public boolean isGreaterOrEquals(SecSignIDServerVersion otherVersion) {
        if (otherVersion.equals(this)) {
            return true;
        }

        return isGreater(otherVersion);
    }

    /**
     * Checks whether this {@link SecSignIDServerVersion} instance is greater than {@code otherVersion},
     * @param otherVersion the other {@link SecSignIDServerVersion}
     * @return true if this instance is greater than {@code otherVersion}, otherwise false
     */
    public boolean isGreater(SecSignIDServerVersion otherVersion) {
        if (major > otherVersion.getMajor()) {
            return true;
        }

        if (major == otherVersion.getMajor() && minor > otherVersion.getMinor()) {
            return true;
        }

        if (major == otherVersion.getMajor() && minor == otherVersion.getMinor() && patch >= otherVersion.getPatch()) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether this {@link SecSignIDServerVersion} instance is lower or equals than {@code otherVersion},
     * @param otherVersion the other {@link SecSignIDServerVersion}
     * @return true if this instance is lower or equals than {@code otherVersion}, otherwise false
     */
    public boolean isLowerOrEquals(SecSignIDServerVersion otherVersion) {
        if (otherVersion.equals(this)) {
            return true;
        }

        return isLower(otherVersion);
    }

    /**
     * Checks whether this {@link SecSignIDServerVersion} instance is lower than {@code otherVersion},
     * @param otherVersion the other {@link SecSignIDServerVersion}
     * @return true if this instance is lower than {@code otherVersion}, otherwise false
     */
    public boolean isLower(SecSignIDServerVersion otherVersion) {
        if (major > otherVersion.getMajor()) {
            return false;
        }

        if (minor > otherVersion.getMinor() && major >= otherVersion.getMajor()) {
            return false;
        }

        if (patch >= otherVersion.getPatch() && minor >= otherVersion.getMinor() && major >= otherVersion.getMajor()) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return getMajor() + "." + getMinor() + "." + getPatch();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SecSignIDServerVersion that = (SecSignIDServerVersion) o;

        if (major != that.major) return false;
        if (minor != that.minor) return false;
        return patch == that.patch;
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + patch;
        return result;
    }
}
