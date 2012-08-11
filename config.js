target = '';
binds = [];
triggers = [];

function trigger(regex, command) {
    var res = {
        test: function (text) {
            if (regex.test(text)) return command;
        }
    };

    triggers.push(res);

    return res;
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

trigger(/^Введите имя персонажа/, "веретень");
trigger(/^Вы пропали в пустоте этого мира./, "зев")

bind(224, 'север')
bind(226, 'запад')
bind(227, 'восток')
bind(65368, 'юг')
bindFunction(116, killTargetCommand)
bind(112, 'зач возв')
bind(117, 'копать')
bind(118, 'поло все.кам сун')

function onMudEvent(text) {
    result = [];
    for(i in triggers) {
        var cmd = triggers[i].test(text)
        if(cmd != undefined) {
            commandExecutor.submit(new com.tort.mudai.command.RawWriteCommand(cmd));
        }
    }

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

bind(113, 'зач возв вивиана')
