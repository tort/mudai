package com.tort.mudai

import command.{MultiCommand, RenderableCommand, StartSessionCommand, RawWriteCommand}
import gui.{OutputPrinter, JScrollableOutput}
import mapper.{Persister, Mapper}
import persistance.MudaiDb
import task.{StatedTask, Person}
import com.google.inject.{Guice, Inject}
import util.parsing.combinator.RegexParsers
import java.awt.event._
import javax.swing._
import java.awt.{BorderLayout, Frame, Dimension, TextField}
import com.google.inject.assistedinject.Assisted
import org.mozilla.javascript.{ScriptableObject, Context}
import java.io.{File, FileInputStream, InputStreamReader}
import org.mozilla.javascript.{Function => JsFunction}

class SimpleMudClient @Inject()(val person: Person,
                                commandExecutor: CommandExecutor
                                 ) extends RegexParsers {

  def start(console: OutputPrinter) {
    person.subscribe(new SimpleEventListener(console))
    person.start()
  }

  private class SimpleEventListener(console: OutputPrinter) extends StatedTask {
    def SimpleEventListener() {
      run()
    }

    override def adapterException(e: Exception) {
      print("network error: " + e)
    }

    override def connectionClosed() {
      print("connection closed");
      System.exit(0)
    }

    override def rawRead(buffer: String) {
      console.print(applyJsTransformation(buffer))
    }

    override def programmerError(exception: Throwable) {
      exception.printStackTrace();
    }

    private def applyJsTransformation(text: String): String = {
      val jsContext = Context.enter
      val scope = Scope.get(jsContext, commandExecutor)
      val strings = text.split("\r?\r?\n")
      val functionObject = scope.get("onMudEvent", scope);
      val jsFunction: JsFunction = functionObject.asInstanceOf[JsFunction]

      val transformedByJsScript = strings
        .map(str => jsFunction.call(jsContext, scope, scope, Array(str)).toString)
        .reduceLeft((a, s) => a + "\n" + s)

      Context.exit()
      transformedByJsScript
    }
  }

}

object Scope {
  @volatile
  private var scope: Option[ScriptableObject] = None

  def reset() { scope = None }

  def get(jsContext: Context, executor: CommandExecutor): ScriptableObject = {
    scope = scope.orElse(Option(init(jsContext, executor)))

    scope.get
  }

  private def init(jsContext: Context, executor: CommandExecutor): ScriptableObject = {
    val scope = jsContext.initStandardObjects
    val jsOut = Context.javaToJS(System.out, scope)
    ScriptableObject.putProperty(scope, "out", jsOut)
    val jsCommandExecutor = Context.javaToJS(executor, scope)
    ScriptableObject.putProperty(scope, "commandExecutor", jsCommandExecutor)
    val jsFiles = new File(".").listFiles.filter(_.getName.endsWith(".js")).map(_.getName).map(name => name.split("\\.")(0) -> name).sortBy(_._1)
    jsFiles.foreach {
      case file =>
        jsContext.evaluateReader(scope, new InputStreamReader(new FileInputStream(file._2)), "err.log", 1, null)
    }

    scope
  }
}

object SimpleMudClient {
  val injector = Guice.createInjector(new MudaiModule())

  def main(args: Array[String]) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      def run() {
        val frame = new JFrame()
        val pane = frame.getContentPane
        frame.setExtendedState(Frame.MAXIMIZED_BOTH)
        val console = injector.getInstance(classOf[JScrollableOutput])
        console.setAutoscrolls(true)
        val input = new TextField()
        input.setMinimumSize(new Dimension(500, 20))
        input.setMaximumSize(new Dimension(1200, 20))
        val factory = injector.getInstance(classOf[InputKeyListenerFactory])
        input.addKeyListener(factory.create(input))
        pane.add(console, BorderLayout.CENTER)
        pane.add(input, BorderLayout.PAGE_END)
        input.setFocusable(true)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        frame.setVisible(true)
        frame.addWindowFocusListener(new WindowAdapter {
          override def windowGainedFocus(e: WindowEvent) {
            input.requestFocusInWindow()
          }
        })
        frame.addComponentListener(new ComponentListener {
          def componentResized(p1: ComponentEvent) {
            input.setSize(frame.getWidth - 7, 20)
          }

          def componentShown(p1: ComponentEvent) {}

          def componentMoved(p1: ComponentEvent) {}

          def componentHidden(p1: ComponentEvent) {}
        })

        val simpleMudClient = injector.getInstance(classOf[SimpleMudClient])
        simpleMudClient.start(console)
      }
    })
  }
}


class InputKeyListener @Inject()(@Assisted input: TextField,
                                 val commandExecutor: CommandExecutor,
                                 val persister: Persister,
                                 val person: Person,
                                 val mapper: Mapper,
                                 val output: JScrollableOutput
                                  ) extends KeyListener {
  val FIND_PATH_COMMAND = "/путь"
  val LIST_LOCATIONS_COMMAND = "/лист"
  val TRAVEL_COMMAND = "/го"
  val MARK_WATER_SOURCE_COMMAND = "/вода"
  val MOB_ALIAS_COMMAND = "/обозвать"
  val MAP_ZONE_COMMAND = "/замапить"

  val StartSessionCommand = "#connect"
  val StartSessionPattern = ("^" + StartSessionCommand + """\s*([^\s]*)\s*(\d*).*$""").r

  def keyTyped(e: KeyEvent) {}

  def keyPressed(e: KeyEvent) {
    val ctx = Context.enter()

    if (e.getKeyCode == KeyEvent.VK_ENTER) {
      handleAliases(ctx)
    } else if (e.getKeyCode == KeyEvent.VK_EQUALS) {
      output.clear()
      output.dropStringsWith("Вивиан")
      callJsOnKeyEvent(ctx, e)
    } else {
        callJsOnKeyEvent(ctx, e)
    }
    Context.exit()
  }


  def callJsOnKeyEvent(ctx: Context, e: KeyEvent) {
    callSingleArgumentJsFunction(ctx, "onKeyEvent", e.getKeyCode.toString) match {
      case None =>
      case Some(Array()) =>
      case Some(x) => commandExecutor.submit(new MultiCommand(x.map(c => new RawWriteCommand(c))))
    }
  }

  private def like(substr: String): String = {
    "%" + substr.trim.toLowerCase + "%"
  }

  private def handleAliases(ctx: Context) {
    val command = input.getText
    val splitted = command.split(" ").toList
    splitted match {
      case FIND_PATH_COMMAND :: x => handleFindPathCommand(x.mkString(" "))
      case List(LIST_LOCATIONS_COMMAND) =>
        for (location <- persister.enlistLocations) {
          System.out.println("LOCATION: " + location.title)
        }
      case List("/моб") =>
        val mobs = persister.enlistMobs
        for (mob <- mobs) {
          System.out.println("MOB: " + mob.name)
        }
      case TRAVEL_COMMAND :: x =>
        handleTravelCommand(x.mkString(" "))
      case MAP_ZONE_COMMAND :: rest =>
        val x = rest.mkString(" ")
        person.mapZone(x.substring(MAP_ZONE_COMMAND.length() + 1, x.length() - 1))
      case List("/зонинг") =>
        person.roam()
      case MOB_ALIAS_COMMAND :: rest =>
        val x = rest.mkString(" ")
        val args = x.substring(MOB_ALIAS_COMMAND.length() + 1, x.length() - 1).split("!")
        val name = args(0)
        val longName = args(1)
        val mob = persister.findMob(name)
        mob.descName(longName)
        persister.persistMob(mob)
      case List("/затариться") =>
        person.provision()
      case MARK_WATER_SOURCE_COMMAND :: x =>
        mapper.markWaterSource(x.mkString(" "))
      case List("/магазин") =>
        mapper.currentLocation.markShop()
        persister.persistLocation(mapper.currentLocation)
      case List("/таверна") =>
        mapper.currentLocation.markTavern()
        persister.persistLocation(mapper.currentLocation)
      case List(StartSessionCommand) =>
        val StartSessionPattern(host, port) = command
        commandExecutor.submit(new StartSessionCommand(host, port.toInt))
      case List("#zap") =>
        commandExecutor.submit(new CloseSessionCommand())
      case List("#reload") =>
        Scope.reset
      case "/стат" :: rest if(rest.nonEmpty) =>
        val stats = MudaiDb.statsLike(like(rest.mkString(" ").trim))
        stats.foreach(s => output.print("\n" + s.desc))
      case _ =>
        callSingleArgumentJsFunction(ctx, "onInputEvent", input.getText) match {
          case None => commandExecutor.submit(new RawWriteCommand(command))
          case Some(Array()) =>
          case Some(x) => commandExecutor.submit(new MultiCommand(x.map(c => new RawWriteCommand(c))))
        }
    }
    input.setText("")
  }

  private def callSingleArgumentJsFunction(ctx: Context, name: String, arg: String): Option[Array[String]] = {
    val scope = Scope.get(ctx, commandExecutor)
    val functionObject = scope.get(name, scope);
    val jsFunction: JsFunction = functionObject.asInstanceOf[JsFunction]
    jsFunction.call(ctx, scope, scope, Array(arg)) match {
      case _: org.mozilla.javascript.Undefined => None
      case result: org.mozilla.javascript.NativeArray => Some(result.toArray.map(_.toString))
    }
  }

  def keyReleased(e: KeyEvent) {}

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
      if (path == null) {
        System.out.println("NO PATH FOUND");
        return;
      }

      val result = new StringBuilder()
      for (direction <- path) {
        result.append(direction.id + " ")
      }
      System.out.println("PATH: " + result)
    }
  }

  private def handleTravelCommand(command: String) {
    val locationTitle = command.substring(TRAVEL_COMMAND.length() + 1, command.length() - 1)
    val locations = persister.loadLocations(locationTitle)
    if (locations.isEmpty) {
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
}

trait InputKeyListenerFactory {
  def create(input: TextField): InputKeyListener
}

class CloseSessionCommand extends RenderableCommand {
  //get rid of stubbed renders
  def render = null
}

trait RawTextEventDispatcher {
  def event(text: String): String
}

