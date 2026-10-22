package com.lostandfound.security;

import com.lostandfound.entity.PoliceStation;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.Role;
import com.lostandfound.exception.ForbiddenException;

import java.util.UUID;

/** Central rule: police staff may only touch records of their own station. SYSTEM_ADMIN is exempt. */
public final class StationAccess {

    private StationAccess() {}

    public static boolean isSystemAdmin(UserPrincipal p) {
        return "SYSTEM_ADMIN".equals(p.getRole());
    }

    public static boolean isPoliceStaff(UserPrincipal p) {
        return "POLICE_OFFICER".equals(p.getRole()) || "POLICE_ADMIN".equals(p.getRole());
    }

    /** null = all stations (SYSTEM_ADMIN); otherwise the caller's own station id. */
    public static UUID scopeFor(UserPrincipal p) {
        if (isSystemAdmin(p)) return null;
        if (p.getStationId() == null) {
            throw new ForbiddenException("Your police account is not assigned to a station");
        }
        return p.getStationId();
    }

    public static void assertOwnStation(UserPrincipal p, UUID recordStationId) {
        if (isSystemAdmin(p)) return;
        check(p.getStationId(), recordStationId);
    }

    public static void assertOwnStation(UserPrincipal p, PoliceStation recordStation) {
        assertOwnStation(p, recordStation == null ? null : recordStation.getStationId());
    }

    public static void assertOwnStation(User user, UUID recordStationId) {
        if (user.getRole() == Role.SYSTEM_ADMIN) return;
        check(user.getStation() == null ? null : user.getStation().getStationId(), recordStationId);
    }

    public static void assertOwnStation(User user, PoliceStation recordStation) {
        assertOwnStation(user, recordStation == null ? null : recordStation.getStationId());
    }

    private static void check(UUID own, UUID record) {
        if (own == null) {
            throw new ForbiddenException("Your police account is not assigned to a station");
        }
        if (record == null || !own.equals(record)) {
            throw new ForbiddenException("This record belongs to another police station");
        }
    }
}