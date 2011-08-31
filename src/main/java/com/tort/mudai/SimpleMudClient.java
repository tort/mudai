package com.tort.mudai;

import com.db4o.ObjectSet;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.RawWriteCommand;
import com.tort.mudai.mapper.*;
import com.tort.mudai.task.Person;
import com.tort.mudai.task.StatedTask;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.List;

public class SimpleMudClient {
    private static final String FIND_PATH_COMMAND = "/путь";
    private static final String LIST_LOCATIONS_COMMAND = "/лист";
    private static final String TRAVEL_COMMAND = "/го";
    private static final String ENLIST_MOBS_COMMAND = "/моб";
    private static final String MARK_WATER_SOURCE_COMMAND = "/вода";

    private Person _person;
    private CommandExecutor _commandExecutor;
    private Persister _persister;
    private final Mapper _mapper;
    private static final String MARK_SHOP_COMMAND = "/магазин";
    private static final String MARK_TAVERN_COMMAND = "/таверна";

    @Inject
    protected SimpleMudClient(final Person person, final CommandExecutor commandExecutor, Persister persister, Mapper mapper) {
        _person = person;
        _commandExecutor = commandExecutor;
        _persister = persister;
        _mapper = mapper;
    }

    public void start() {
        _person.subscribe(new SimpleEventListener());
        _person.start();

        final InputStreamReader reader = new InputStreamReader(System.in);
        final CharBuffer charBuffer = CharBuffer.allocate(AdapterImpl.OUT_BUF_SIZE);
        try {
            while (reader.read(charBuffer) != -1) {
                charBuffer.flip();
                final String command = charBuffer.toString();
                if (command.startsWith(FIND_PATH_COMMAND)) {
                    handleFindPathCommand(command);
                } else if (command.startsWith(LIST_LOCATIONS_COMMAND)) {
                    for (Location location : _persister.enlistLocations()) {
                        System.out.println("LOCATION: " + location.getTitle());
                    }
                } else if (command.startsWith(ENLIST_MOBS_COMMAND)) {
                    ObjectSet<Mob> mobs = _persister.enlistMobs();
                    for (Mob mob : mobs) {
                        System.out.println("MOB: " + mob.getName());
                    }
                } else if (command.startsWith(TRAVEL_COMMAND)) {
                    handleTravelCommand(command);
                } else if (command.startsWith(MARK_WATER_SOURCE_COMMAND)) {
                    _mapper.markWaterSource(command.substring(MARK_WATER_SOURCE_COMMAND.length() + 1, command.length() - 1));
                } else if (command.startsWith(MARK_SHOP_COMMAND)) {
                    _mapper.currentLocation().markShop();
                    _persister.persistLocation(_mapper.currentLocation());
                } else if (command.startsWith(MARK_TAVERN_COMMAND)) {
                    _mapper.currentLocation().markTavern();
                    _persister.persistLocation(_mapper.currentLocation());
                } else {
                    _commandExecutor.submit(new RawWriteCommand(command));
                }

                charBuffer.clear();
            }
        } catch (IOException e) {
            System.out.println("read keyboard input error");
        }
    }

    private void handleFindPathCommand(String command) {
        String to = command.substring(FIND_PATH_COMMAND.length() + 1, command.length() - 1);
        Location prototype = new Location();
        prototype.setTitle(to);
        final List<Location> locations = _persister.loadLocation(prototype);
        if (locations.isEmpty()) {
            System.out.println("NO LOCATION FOUND: " + to);
            return;
        }
        if (locations.size() > 1) {
            System.out.println(locations.size() + " locations found, titled " + prototype.getTitle());
            for (Location location : locations) {
                System.out.println("DISTANCE: " + _mapper.pathTo(location).size());
            }
        } else {
            final List<Direction> path = _mapper.pathTo(locations.get(0));
            if(path == null){
                System.out.println("NO PATH FOUND");
                return;
            }

            StringBuilder result = new StringBuilder();
            for (Direction direction : path) {
                result.append(direction.getName() + " ");
            }
            System.out.println("PATH: " + result);
        }
    }

    private void handleTravelCommand(String command) {
        final String to = command.substring(TRAVEL_COMMAND.length() + 1, command.length() - 1);
        Location prototype = new Location();
        prototype.setTitle(to);
        final List<Location> locations = _persister.loadLocation(prototype);
        if (locations.size() > 1) {
            System.out.println(locations.size() + " locations found, titled " + prototype.getTitle());
            for (Location location : locations) {
                System.out.println("DISTANCE: " + _mapper.pathTo(location).size());
            }
        } else {
            _person.travel(locations.get(0));
        }
    }

    private static class SimpleEventListener extends StatedTask {
        public SimpleEventListener() {
            run();
        }

        private void print(final String message) {
            System.out.println(message);
        }

        @Override
        public void adapterException(Exception e) {
            print("network error: " + e);
        }

        @Override
        public void connectionClosed() {
            print("connection closed");
            System.exit(0);
        }

        @Override
        public void rawRead(String buffer) {
            System.out.print(buffer);
        }

        @Override
        public void programmerError(Throwable exception) {
            exception.printStackTrace();
        }

        @Override
        public Command pulse() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new MudaiModule());
        final SimpleMudClient simpleMudClient = injector.getInstance(SimpleMudClient.class);

        simpleMudClient.start();
    }
}
