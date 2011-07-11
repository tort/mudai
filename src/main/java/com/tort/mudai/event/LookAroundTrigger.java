package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.RoomSnapshot;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LookAroundTrigger implements EventTrigger {
    public static final Pattern PATTERN = PatternUtil.compile("^(?:Вы поплелись на (?:север|юг|запад|восток)\\.\r?\n)?" +
            "\u001B\\[1\\;36m(.*)\u001B\\[0\\;37m$\\s\\s\\s.*\r?\n\r?\n" +
            "\u001B\\[1\\;33m(?:(.*)\r?\n)?" +
            "\u001B\\[1\\;31m(?:(.*)\r?\n)?" +
            "\u001B\\[0\\;37m\r?\n?\u001B\\[0\\;32m[^\n]*$");

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

        _eventDistributor.invoke(new Handler<LookAroundEvent>() {
            @Override
            public void handle(final AbstractTask task) {
                task.lookAround(roomSnapshot);
            }
        });

        return null;
    }
}
