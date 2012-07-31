function submitCommand(command) {
        commandExecutor.submit(new com.tort.mudai.command.RawWriteCommand(command));
}

function trigger(regex, command) {
    return {
        reply: function (text) {
            if (regex.test(text)) submitCommand(command)
        }
    }
}

function bind(keyCode, command) {
    return {
        test: function (keyPressed) {
            if(keyPressed == keyCode) submitCommand(command)
        }
    }
}

personNameTrigger = trigger(/^Введите имя персонажа/, "веретень");
northBind = bind(224, 'север')
westBind = bind(226, 'запад')
eastBind = bind(227, 'восток')
southBind = bind(65368, 'юг')
killTargetBind = bind('116', 'уб пчел')

function onMudEvent(text) {
    personNameTrigger.reply(text);

    if ((/^Персонаж с таким именем уже существует. Введите пароль/).test(text))
        commandExecutor.submit(new com.tort.mudai.command.RawWriteCommand("таганьйорк"));

    return text.replace("\u001B[0;32m", "\u001B[0;35m");
}

function onKeyEvent(keyCode) {
    if(keyCode == 123) {
        commandExecutor.submit(new com.tort.mudai.command.StartSessionCommand("bylins.su", 4000));
    }

    northBind.test(keyCode)
    westBind.test(keyCode)
    eastBind.test(keyCode)
    southBind.test(keyCode)
    killTargetBind.test(keyCode)

    out.println(keyCode)
}

