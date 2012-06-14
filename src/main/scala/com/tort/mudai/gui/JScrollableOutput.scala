package com.tort.mudai.gui

import javax.swing._
import text.{StyleConstants, StyleContext, DefaultCaret}
import java.awt.Color

class JScrollableOutput extends JScrollPane {
  val textColor = ANSI.DARK_WHITE
  val textPane = new JTextPane()
  val caret: DefaultCaret = textPane.getCaret.asInstanceOf[DefaultCaret]
  caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE)

  textPane.setEditable(false)
  textPane.setBackground(Color.BLACK)
  setViewportView(textPane)

  val styleContext = new StyleContext()
  val defaultTextStyle = styleContext.addStyle("main text", null)
  defaultTextStyle.addAttribute(StyleConstants.Foreground, Color.LIGHT_GRAY)
  defaultTextStyle.addAttribute(StyleConstants.Background, Color.BLACK)

  val coloredTextStyle = styleContext.addStyle("colored text", defaultTextStyle)
  coloredTextStyle.addAttribute(StyleConstants.Foreground, Color.GREEN)

  def print(text: String) {
    val greenPresent = text.indexOf(ANSI.GREEN)
    val style = greenPresent match {
      case -1 => defaultTextStyle
      case _ => coloredTextStyle
    }

    textPane.getStyledDocument.insertString(textPane.getCaretPosition, text, style)
  }
}

object ANSI {
  val SANE = "\u001B[0m";

  val BLACK = "\u001B[0;30m";
  val RED = "\u001B[0;31m";
  val GREEN = "\u001B[0;32m";
  val YELLOW = "\u001B[0;33m";
  val BLUE = "\u001B[0;34m";
  val MAGENTA = "\u001B[0;35m";
  val CYAN = "\u001B[0;36m";
  val WHITE = "\u001B[0;37m";

  val DARK_BLACK = "\u001B[1;30m";
  val DARK_RED = "\u001B[1;31m";
  val DARK_GREEN = "\u001B[1;32m";
  val DARK_YELLOW = "\u001B[1;33m";
  val DARK_BLUE = "\u001B[1;34m";
  val DARK_MAGENTA = "\u001B[1;35m";
  val DARK_CYAN = "\u001B[1;36m";
  val DARK_WHITE = "\u001B[1;37m";

  val BACKGROUND_BLACK = "\u001B[40m";
  val BACKGROUND_RED = "\u001B[41m";
  val BACKGROUND_GREEN = "\u001B[42m";
  val BACKGROUND_YELLOW = "\u001B[43m";
  val BACKGROUND_BLUE = "\u001B[44m";
  val BACKGROUND_MAGENTA = "\u001B[45m";
  val BACKGROUND_CYAN = "\u001B[46m";
  val BACKGROUND_WHITE = "\u001B[47m";
}