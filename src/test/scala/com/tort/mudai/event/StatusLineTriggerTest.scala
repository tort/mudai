package com.tort.mudai.event

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class StatusLineTriggerTest extends FunSuite with ShouldMatchers {
val sample = "\u001B[1;36mГостиный двор\u001B[0;37m" +
    "\n   Тощий, долгорукий и по-особому ловкий в своем деле, хозяин спешит" +
    "\nублаготворить гостей. Он улыбается, кланяется, заискивает, называя каждого" +
    "\nдобрейшим, любезнейшим, умнейшим, прекраснейшим, щедрейшим... Наилучшие" +
    "\nкачества и в превосходной степени сыплются с его языка, как отборный горох" +
    "\nс лотка. Конечно же, он возьмет на сохранение все - это великолепные вещи," +
    "\nбольшого веса, большой цены. Христос свидетель, он все сбережет, он будет" +
    "\nбезо всякого обмана, троица святая видит, да, да, он будет давать кров" +
    "\nблагороднейшим людям, он запомнил каждого, достаточно войти, чтобы получить" +
    "\nвсе желаемое... Напрасно, право же, напрасно великодушный путник упоминает" +
    "\nо расправе за неверность. Пусть спросят весь город, здесь никогда никого " +
    "\nне обманули." +
    "\n" +
    "\n\u001B[0;36m[ Exits: n w ]\u001B[0;37m" +
    "\n\u001B[1;33mДоска для различных заметок и объявлений прибита тут ..блестит!" +
    "\n\u001B[1;31mВелянин Ярофей (инок ОС) стоит здесь. " +
    "\nВелянин Парацельс (упырь БУ) летает здесь. " +
    "\nХозяин двора услужливо улыбается Вам." +
    "\n\u001B[0;0m\u001B[0;32m760H\u001B[0;37m \u001B[1;32m208M\u001B[0;37m 6248967о Зауч:0 30L 300G Вых:СЗ> "

  val sample2 = "\u001B[1;36mХарчевня\u001B[0;37m\n   Красна изба углами, а хозяйка - пирогами. Правду молвит народ - хороша" +
    "\n\nхозяйка, красна да ласкоречива, а от пирожного духа ажно слюнки текут." +
    "\n\nА окромя пирогов, на огромной, дубовой лавке стоят клети с караваями" +
    "\n\nрумяными, да с шанежками и кренделями пышными. А у стены - бочки многоведерные" +
    "\n\nс медом хмельным да винами зелеными. Веселись, душа русская, веселись, да" +
    "\n\nмеру знай.\n\n" +
    "\n\u001B[0;36m[ Exits: s ]\u001B[0;37m" +
    "\n\u001B[1;33m\u001B[1;31mПьяный мужчина бредет потихоньку домой." +
    "\nОгромный, с сизым носом, трактирщик отдыхает за стойкой." +
    "\n\u001B[0;0m" +
    "\n\u001B[0;32m170H\u001B[0;37m \u001B[0;32m135M\u001B[0;37m 86955о Зауч:0 10L 95G Вых:Ю> "

  test("status line matching") {
    new StatusLineTrigger().matches(sample) should be(true)
  }
}
