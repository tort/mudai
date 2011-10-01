package com.tort.mudai

import command.RawWriteCommand
import java.nio.CharBuffer
import mapper.{Persister, Mapper}
import java.io.{IOException, InputStreamReader}
import task.{StatedTask, Person}
import com.google.inject.{Guice, Inject}

class SimpleMudClient @Inject()(val person: Person,
                                val commandExecutor: CommandExecutor,
                                val persister: Persister,
                                val mapper: Mapper) {
    val FIND_PATH_COMMAND  = "/путь"
    val LIST_LOCATIONS_COMMAND = "/лист"
    val TRAVEL_COMMAND = "/го"
    val ENLIST_MOBS_COMMAND = "/моб"
    val MARK_WATER_SOURCE_COMMAND = "/вода"
    val ROAM_COMMAND = "/зонинг"
    val PROVISION_COMMAND = "/затариться"
    val MOB_ALIAS_COMMAND = "/обозвать"
    val MAP_ZONE_COMMAND = "/замапить"

    val MARK_SHOP_COMMAND = "/магазин"
    val MARK_TAVERN_COMMAND = "/таверна"

    def start() {
        person.subscribe(new SimpleEventListener())
        person.start()

        val reader = new InputStreamReader(System.in)
        val charBuffer = CharBuffer.allocate(AdapterImpl.OUT_BUF_SIZE)
        try {
            while (reader.read(charBuffer) != -1) {
                charBuffer.flip()
                val command = charBuffer.toString
                if (command.startsWith(FIND_PATH_COMMAND)) {
                    handleFindPathCommand(command)
                } else if (command.startsWith(LIST_LOCATIONS_COMMAND)) {
                    for (location <- persister.enlistLocations) {
                        System.out.println("LOCATION: " + location.title)
                    }
                } else if (command.startsWith(ENLIST_MOBS_COMMAND)) {
                    val mobs = persister.enlistMobs
                    for (mob <- mobs) {
                        System.out.println("MOB: " + mob.name)
                    }
                } else if (command.startsWith(TRAVEL_COMMAND)) {
                    handleTravelCommand(command)
                } else if (command.startsWith(MAP_ZONE_COMMAND)) {
                    person.mapZone(command.substring(MAP_ZONE_COMMAND.length() + 1, command.length() - 1))
                } else if (command.startsWith(ROAM_COMMAND)) {
                    person.roam()
                } else if (command.startsWith(MOB_ALIAS_COMMAND)) {
                    val args = command.substring(MOB_ALIAS_COMMAND.length() + 1, command.length() - 1).split("!")
                    val name = args(0)
                    val longName = args(1)
                    val mob = persister.findMob(name)
                    mob.descName(longName)
                    persister.persistMob(mob)
                } else if (command.startsWith(PROVISION_COMMAND)) {
                    person.provision()
                } else if (command.startsWith(MARK_WATER_SOURCE_COMMAND)) {
                    mapper.markWaterSource(command.substring(MARK_WATER_SOURCE_COMMAND.length() + 1, command.length() - 1))
                } else if (command.startsWith(MARK_SHOP_COMMAND)) {
                    mapper.currentLocation.markShop()
                    persister.persistLocation(mapper.currentLocation)
                } else if (command.startsWith(MARK_TAVERN_COMMAND)) {
                    mapper.currentLocation.markTavern()
                    persister.persistLocation(mapper.currentLocation)
                } else {
                    commandExecutor.submit(new RawWriteCommand(command))
                }

                charBuffer.clear()
            }
        } catch {
          case e:IOException =>
            System.out.println("read keyboard input error")
        }
    }

    private def handleFindPathCommand(command: String) {
        val locationTitle = command.substring(FIND_PATH_COMMAND.length() + 1, command.length() - 1)
        val locations = persister.loadLocations(locationTitle)
        if (locations.isEmpty) {
            System.out.println("NO LOCATION FOUND: " + locationTitle)
            return;
        }
        if (locations.size > 1) {
            System.out.println(locations.size + " locations found, titled " + locationTitle)
            for (location <- locations) {
                System.out.println("DISTANCE: " + mapper.pathTo(location).size)
            }
        } else {
            val path = mapper.pathTo(locations.head)
            if(path == null){
                System.out.println("NO PATH FOUND");
                return;
            }

            val result = new StringBuilder()
            for (direction <- path) {
                result.append(direction.getName + " ")
            }
            System.out.println("PATH: " + result)
        }
    }

    private def handleTravelCommand(command: String) {
        val locationTitle = command.substring(TRAVEL_COMMAND.length() + 1, command.length() - 1)
        val locations = persister.loadLocations(locationTitle)
        if(locations.isEmpty){
            System.out.println("ROOM UNKNOWN");
            return;
        }

        if (locations.size > 1) {
            System.out.println(locations.size + " locations found, titled " + locationTitle)
            for (location <- locations) {
                System.out.println("DISTANCE: " + mapper.pathTo(location).size)
            }
        } else {
            person.travel(locations.head)
        }
    }

    private class SimpleEventListener extends StatedTask {
        def SimpleEventListener() {
            run()
        }

        private def print(message: String) {
            System.out.println(message)
        }

        override def adapterException(e: Exception) {
            print("network error: " + e)
        }

        override def connectionClosed() {
            print("connection closed");
            System.exit(0)
        }

        override def rawRead(buffer: String) {
            System.out.print(buffer)
        }

        override def programmerError(exception: Throwable) {
            exception.printStackTrace();
        }
    }
}

object SimpleMudClient {
  val injector = Guice.createInjector(new MudaiModule())

  def main(args: Array[String]) {
    val simpleMudClient = injector.getInstance(classOf[SimpleMudClient])

    simpleMudClient.start();
  }
}
