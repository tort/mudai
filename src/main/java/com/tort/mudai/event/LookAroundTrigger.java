package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.RoomSnapshot;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LookAroundTrigger implements EventTrigger {
    public static final Pattern PATTERN = PatternUtil.compile("^(?:Вы поплелись на (?:север|юг|запад|восток)\\.\n)?" +
            "\u001B\\[1\\;36m(.*)\u001B\\[0\\;37m$\\s\\s\\s.*\r{0,}\n\r{0,}\n" +
            "\u001B\\[1\\;33m(?:(.*)\r{0,}\n)?" +
            "\u001B\\[1\\;31m(?:(.*)\r{0,}\n)?" +
            "\u001B\\[0\\;37m\r{0,}\n\u001B\\[0\\;32m[^\n]*$");

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
    public void fireEvent(final String text) {
        final Matcher matcher = PATTERN.matcher(text);
        matcher.find();

        final String locationTitle = matcher.group(1);
        final String objectsGroup = matcher.group(2);
        String[] objects = objectsGroup.split("\n");

        String mobsGroup = matcher.group(3);
        String[] mobs = mobsGroup.split("\n");

        final RoomSnapshot roomSnapshot = new RoomSnapshot();
        roomSnapshot.setLocationTitle(locationTitle);
        roomSnapshot.setObjectsPresent(objects);
        roomSnapshot.setMobs(mobs);

        _eventDistributor.invoke(new Handler<LookAroundEvent>(){
            @Override
            public void handle(final AbstractTask task) {
                task.lookAround(roomSnapshot);
            }
        });
    }

    private class MobsHelper {
        public void mobs(String objects) {

        }
    }
}
