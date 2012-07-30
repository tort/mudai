function trigger(regex, command) {
    return {
        reply: function (text) {
            if (regex.test(text)) commandExecutor.submit(new com.tort.mudai.command.RawWriteCommand(command));
        }
    }
}

personNameTrigger = trigger(/^Введите имя персонажа/, "ладень");

function onMudEvent(text) {
    personNameTrigger.reply(text);

    if ((/^Персонаж с таким именем уже существует. Введите пароль/).test(text))
        commandExecutor.submit(new com.tort.mudai.command.RawWriteCommand("таганьйорк"));

    return text.replace("\u001B[0;32m", "\u001B[0;35m");
}

