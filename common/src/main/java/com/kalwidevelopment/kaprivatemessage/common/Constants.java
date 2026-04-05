package com.kalwidevelopment.kaprivatemessage.common;

public final class Constants {
    private Constants() {}

    public static final String PLUGIN_CHANNEL = "kaprivatemessage:main";
    public static final int MIN_SEARCH_LENGTH = 3;

    // Packet types Velocity → Paper
    public static final String PACKET_PLAY_SOUND = "PLAY_SOUND";
    public static final String PACKET_REQUEST_SOUND_GUI = "REQUEST_SOUND_GUI";

    // Packet types Paper → Velocity
    public static final String PACKET_NICKNAME_UPDATE = "NICKNAME_UPDATE";
    public static final String PACKET_SOUND_SELECTED = "SOUND_SELECTED";
    public static final String PACKET_PM_SEND_REQUEST = "PM_SEND_REQUEST";
    public static final String PACKET_PM_SERVERS_REQUEST = "PM_SERVERS_REQUEST";

    // Packet types Velocity → Paper
    public static final String PACKET_PM_SERVERS_RESPONSE = "PM_SERVERS_RESPONSE";

    // Default sounds
    public static final String DEFAULT_SOUND_SEND = "block.note_block.hat";
    public static final String DEFAULT_SOUND_RECEIVE = "entity.experience_orb.pickup";
    public static final double DEFAULT_VOLUME = 1.0;
    public static final double DEFAULT_PITCH = 1.0;

    // Permissions
    public static final String PERM_ADMIN = "kaprivatemessage.admin";
    public static final String PERM_STAFF = "kaprivatemessage.pm.staff";
    public static final String PERM_DONATOR = "kaprivatemessage.pm.donator";
    public static final String PERM_SOCIALSPY = "kaprivatemessage.socialspy";
    public static final String PERM_PM_COLOR = "kaprivatemessage.pm.color";
}
