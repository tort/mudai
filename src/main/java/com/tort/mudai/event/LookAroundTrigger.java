package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.RoomSnapshot;
import com.tort.mudai.mapper.Directions;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LookAroundTrigger implements EventTrigger {
    public static final DirectionLister lister = new DirectionLister();
    public static final Pattern PATTERN = PatternUtil.compile("^(?:Вы поплелись (?:на )?(?:" + lister.listDirections() + ")\\.\r?\n)?" +
            "\u001B\\[1\\;36m(.*)\u001B\\[0\\;37m$\\s\\s\\s(.*)\r?\n\r?\n" +
            "(?:\u001B\\[1\\;37mСнежный ковер лежит у Вас под ногами.\u001B\\[0\\;37m\r?\n)?" +
            "\u001B\\[1\\;33m(?:(.*)\r?\n)?" +
            "\u001B\\[1\\;31m(?:(.*)\r?\n)?" +
            "\u001B\\[0\\;37m\r?\n?\u001B\\[0\\;32m[^\n]*Вых\\:([^\n]*)\\> $");

    private final EventDistributor _eventDistributor;

    public LookAroundTrigger(final EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public boolean matches(final String text) {
        final Matcher matcher = PATTERN.matcher(text);

        return matcher.matches();
    }

    @Override
    public Event fireEvent(final String text) {
        final Matcher matcher = PATTERN.matcher(text);
        matcher.find();

        final String locationTitle = matcher.group(1);
        final String locationDesc = matcher.group(2);
        final String objectsGroup = matcher.group(3);
        final String availableExits = matcher.group(5);
        Set<Directions> exits = new HashSet<Directions>();
        for (Directions exit : Directions.values()) {
            if (availableExits.contains(exit.getAlias())) {
                exits.add(exit);
            }
        }
        String[] objects = new String[]{};
        if (objectsGroup != null) {
            objects = objectsGroup.split("\n");
        }

        String mobsGroup = matcher.group(4);
        String[] mobs = new String[]{};
        if (mobsGroup != null) {
            mobs = mobsGroup.split("\n");
        }

        final RoomSnapshot roomSnapshot = new RoomSnapshot();
        roomSnapshot.setLocationTitle(locationTitle);
        roomSnapshot.setObjectsPresent(objects);
        roomSnapshot.setMobs(mobs);
        roomSnapshot.setLocationDesc(locationDesc);
        roomSnapshot.setExits(exits);

        _eventDistributor.invoke(new Handler<LookAroundEvent>() {
            @Override
            public void handle(final AbstractTask task) {
                task.lookAround(roomSnapshot);
            }
        });

        return null;
    }

}
