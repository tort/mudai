package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.RoomSnapshot;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LookAroundTrigger implements EventTrigger {
    public static final Pattern PATTERN = PatternUtil.compile("^(?:Вы поплелись на (?:север|юг|запад|восток)\\.\n)?" +
            "\u001B\\[1\\;36m(.*)\u001B\\[0\\;37m$\\s\\s\\s.*\n\n\u001B\\[1\\;33m(.*)\n\u001B\\[0\\;37m\n\u001B\\[0\\;32m[^\n]*$");

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
        final String mobs = matcher.group(2);
        final RoomSnapshot roomSnapshot = new RoomSnapshot();
        roomSnapshot.setLocationTitle(locationTitle);
        roomSnapshot.setObjectsPresent(mobs);

        _eventDistributor.invoke(new Handler<LookAroundEvent>(){
            @Override
            public void handle(final AbstractTask task) {
                task.lookAround(roomSnapshot);
            }
        });
    }
}
