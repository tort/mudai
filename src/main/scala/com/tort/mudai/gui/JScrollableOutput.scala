package com.tort.mudai.gui

import javax.swing._
import text.{Style, StyleConstants, StyleContext, DefaultCaret}
import java.awt.Color

class JScrollableOutput extends JScrollPane with OutputPrinter {
  var stringsToDropPattern: Option[String] = None

  val textPane = new JTextPane()
  val caret: DefaultCaret = textPane.getCaret.asInstanceOf[DefaultCaret]
  caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE)

  textPane.setEditable(false)
  textPane.setFocusable(false)
  textPane.setBackground(Color.BLACK)
  setViewportView(textPane)

  val styleContext = new StyleContext()
  val defaultTextStyle = styleContext.addStyle("main text", null)
  defaultTextStyle.addAttribute(StyleConstants.Foreground, Color.WHITE.darker())
  defaultTextStyle.addAttribute(StyleConstants.Background, Color.BLACK)

  val ANSIColorToStyle = Map(
    ANSI.SANE -> defaultTextStyle,
    ANSI.GRAY -> foregroundStyle(Color.GRAY),
    ANSI.RED -> foregroundStyle(Color.RED.darker),
    ANSI.GREEN -> foregroundStyle(Color.GREEN.darker),
    ANSI.YELLOW -> foregroundStyle(Color.YELLOW.darker),
    ANSI.BLUE -> foregroundStyle(Color.BLUE.darker),
    ANSI.MAGENTA -> foregroundStyle(Color.MAGENTA.darker),
    ANSI.CYAN -> foregroundStyle(Color.CYAN.darker),
    ANSI.WHITE -> foregroundStyle(Color.WHITE.darker),
    ANSI.LIGHT_GRAY -> foregroundStyle(Color.LIGHT_GRAY),
    ANSI.LIGHT_RED -> foregroundStyle(Color.RED.brighter),
    ANSI.LIGHT_GREEN -> foregroundStyle(Color.GREEN.brighter),
    ANSI.LIGHT_BLUE -> foregroundStyle(Color.BLUE.brighter),
    ANSI.LIGHT_YELLOW -> foregroundStyle(Color.YELLOW.brighter),
    ANSI.LIGHT_MAGENTA -> foregroundStyle(Color.MAGENTA.brighter),
    ANSI.LIGHT_CYAN -> foregroundStyle(Color.CYAN.brighter),
    ANSI.LIGHT_WHITE -> foregroundStyle(Color.WHITE.brighter)
  )

  private def foregroundStyle(color: Color): Style = {
    val style = styleContext.addStyle(color.toString + " foreground", defaultTextStyle)
    style.addAttribute(StyleConstants.Foreground, color)
    style
  }


  var currentStyle = defaultTextStyle

  def clear() {
    textPane.setText("")
  }

  def dropStringsWith(pattern: String) {
    stringsToDropPattern = Option(pattern)
  }

  def print(input: String) {
    val text = stringsToDropPattern.map(x => input.replace(x, "")).getOrElse(input)//hack, generalize

    val escapeIndex = text.indexOf('\u001B')
    escapeIndex match {
      case -1 =>
        textPane.getStyledDocument.insertString(textPane.getCaretPosition, text, currentStyle)
      case 0 =>
        val colorCodeEnd = text.indexOf('m', escapeIndex) + 1
        val colorCode = text.substring(escapeIndex, colorCodeEnd)
        val ansiColor = ANSI.colors.find(color => color == colorCode).getOrElse {
          println("color not found: " + colorCode); ANSI.LIGHT_WHITE
        }
        currentStyle = ANSIColorToStyle.get(ansiColor).getOrElse({
          println("style not found for " + ansiColor.drop(1)); defaultTextStyle
        })
        print(text.drop(colorCodeEnd))
      case x =>
        textPane.getStyledDocument.insertString(textPane.getCaretPosition, text.substring(0, x), currentStyle)
        print(text.drop(x))
    }
  }
}

object ANSI {
  val SANE = "\u001B[0;0m"

  val GRAY = "\u001B[0;30m"
  val RED = "\u001B[0;31m"
  val GREEN = "\u001B[0;32m"
  val YELLOW = "\u001B[0;33m"
  val BLUE = "\u001B[0;34m"
  val MAGENTA = "\u001B[0;35m"
  val CYAN = "\u001B[0;36m"
  val WHITE = "\u001B[0;37m"

  val LIGHT_GRAY = "\u001B[1;30m"
  val LIGHT_RED = "\u001B[1;31m"
  val LIGHT_GREEN = "\u001B[1;32m"
  val LIGHT_YELLOW = "\u001B[1;33m"
  val LIGHT_BLUE = "\u001B[1;34m"
  val LIGHT_MAGENTA = "\u001B[1;35m"
  val LIGHT_CYAN = "\u001B[1;36m"
  val LIGHT_WHITE = "\u001B[1;37m"

  val BACKGROUND_BLACK = "\u001B[40m"
  val BACKGROUND_RED = "\u001B[41m"
  val BACKGROUND_GREEN = "\u001B[42m"
  val BACKGROUND_YELLOW = "\u001B[43m"
  val BACKGROUND_BLUE = "\u001B[44m"
  val BACKGROUND_MAGENTA = "\u001B[45m"
  val BACKGROUND_CYAN = "\u001B[46m"
  val BACKGROUND_WHITE = "\u001B[47m"

  val colors = Set(SANE, GRAY, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE,
    LIGHT_GRAY, LIGHT_RED, LIGHT_GREEN, LIGHT_YELLOW, LIGHT_BLUE, LIGHT_MAGENTA, LIGHT_CYAN, LIGHT_WHITE)
}

trait OutputPrinter {
  def print(text: String)
}