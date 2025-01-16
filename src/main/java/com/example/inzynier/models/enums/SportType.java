package com.example.inzynier.models.enums;

public enum SportType {
    GYM, BOX, KICKBOXING, MMA, MARTIAL_ARTS, ALL;

    public static SportType get(final Integer value) {
        switch (value) {
            case 0:
                return GYM;
            case 1:
                return BOX;
            case 2:
                return KICKBOXING;
            case 3:
                return MMA;
            case 4:
                return MARTIAL_ARTS;
            case 5:
                return ALL;
            default:
                throw new IllegalArgumentException("Unknown sport type id: " + value);
        }
    }

}
