package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.RoomSnapshot;
import com.tort.mudai.mapper.Directions;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LookAroundTrigger implements EventTrigger {
    public static final DirectionLister lister = new DirectionLister();
    public static final Pattern PATTERN = PatternUtil.compile("^(?:Вы поплелись на (?:" + lister.listDirections() + ")\\.\r?\n)?" +
            "\u001B\\[1\\;36m(.*)\u001B\\[0\\;37m$\\s\\s\\s.*\r?\n\r?\n" +
            "\u001B\\[1\\;33m(?:(.*)\r?\n)?" +
            "\u001B\\[1\\;31m(?:(.*)\r?\n)?" +
            "\u001B\\[0\\;37m\r?\n?\u001B\\[0\\;32m[^\n] Вых:(С|\\(С\\))?(В|\\(В\\))?(Ю|\\(Ю\\))?(З|\\(З\\))?(\\^|\\(^\\))?(v|\\(v\\))?>$");

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
        final String objectsGroup = matcher.group(2);
        String[] objects = new String[]{};
        if(objectsGroup != null){
            objects = objectsGroup.split("\n");
        }

        String mobsGroup = matcher.group(3);
        String[] mobs = new String[]{};
        if (mobsGroup != null) {
            mobs = mobsGroup.split("\n");
        }

        final RoomSnapshot roomSnapshot = new RoomSnapshot();
        roomSnapshot.setLocationTitle(locationTitle);
        roomSnapshot.setObjectsPresent(objects);
        roomSnapshot.setMobs(mobs);

        final String northExitGroup = matcher.group(4);
        if("\\(С\\)".equals(northExitGroup))
            roomSnapshot.addDoor(Directions.NORTH);

        final String eastExitGroup = matcher.group(5);
        if("\\(В\\)".equals(eastExitGroup))
            roomSnapshot.addDoor(Directions.EAST);

        final String westExitGroup = matcher.group(6);
        if("\\(З\\)".equals(westExitGroup))
            roomSnapshot.addDoor(Directions.WEST);

        final String southExitGroup = matcher.group(7);
        if("\\(Ю\\)".equals(southExitGroup))
            roomSnapshot.addDoor(Directions.SOUTH);

        final String upExitGroup = matcher.group(8);
        if("\\(\\^\\)".equals(upExitGroup))
            roomSnapshot.addDoor(Directions.UP);

        final String downExitGroup = matcher.group(9);
        if("\\(v\\)".equals(downExitGroup))
            roomSnapshot.addDoor(Directions.DOWN);

        _eventDistributor.invoke(new Handler<LookAroundEvent>() {
            @Override
            public void handle(final AbstractTask task) {
                task.lookAround(roomSnapshot);
            }
        });

        return null;
    }

}
