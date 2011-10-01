package com.tort.mudai.event;

import com.tort.mudai.mapper.Directions;

class DirectionLister {
    public String listDirections() {
        StringBuilder builder = new StringBuilder();
        for (Directions direction : Directions.values()) {
            builder.append(direction.getName());
            builder.append("|");
        }
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

}
