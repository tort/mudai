target = ''
binds = [];

function submitCommand(command) {
    commandExecutor.submit(new com.tort.mudai.command.RawWriteCommand(command));
}

function trigger(regex, command) {
    return {
        test: function (text) {
            if (regex.test(text)) submitCommand(command)
        }
    }
}

function bind(keyCode, command) {
    var res = {
        test: function (keyPressed) {
            if(keyPressed == keyCode) {
                 return command;
            }
        }
    };

    binds.push(res);

    return res;
}

function bindFunction(keyCode, commandFunction) {
    var res = {
        test: function (keyPressed) {
            if(keyPressed == keyCode) {
                 return commandFunction(command);
            }
        }
    };

    binds.push(res);

    return res;
}

function killTargetCommand(target) {
    return 'уб ' + target
}

personNameTrigger = trigger(/^Введите имя персонажа/, "ладень");
disappearTrigger = trigger(/^Вы пропали в пустоте этого мира./, "зев")

northBind = bind(224, 'север')
westBind = bind(226, 'запад')
eastBind = bind(227, 'восток')
southBind = bind(65368, 'юг')
killTargetBind = bindFunction(116, killTargetCommand)

function onMudEvent(text) {
    personNameTrigger.test(text);
    disappearTrigger.test(text);

    if ((/^Персонаж с таким именем уже существует. Введите пароль/).test(text))
        commandExecutor.submit(new com.tort.mudai.command.RawWriteCommand("таганьйорк"));

    return text.replace("\u001B[0;32m", "\u001B[0;35m");
}

function onKeyEvent(keyCode) {
    if(keyCode == 123) {
        commandExecutor.submit(new com.tort.mudai.command.StartSessionCommand("bylins.su", 4000));
    }

    result = [];
    for(i in binds) {
        var cmd = binds[i].test(keyCode)
        if(cmd != undefined) {
            result.push(cmd)
        }
    }

    out.println(keyCode)
    return result;
}

function onInputEvent(text) {
    //set target
    if((/^т .*/).test(text)) {
        res = (/^т (.*)/).exec(text);
        target = res[1];
        return [];
    }
}
