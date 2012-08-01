target = ''

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
    return {
        test: function (keyPressed) {
            if(keyPressed == keyCode) {
                 return command;
            }
        }
    }
}

function bindFunction(keyCode, commandFunction) {
    return {
        test: function (keyPressed) {
            if(keyPressed == keyCode) {
                return commandFunction(target);
            }
        }
    }
}

function pushIfDefined(arr, str) {
    if(str != undefined) {
        arr.push(str)
    }
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
killTargetBind = bindFunction('116', killTargetCommand)

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
    pushIfDefined(result, northBind.test(keyCode));
    pushIfDefined(result, westBind.test(keyCode));
    pushIfDefined(result, eastBind.test(keyCode));
    pushIfDefined(result, southBind.test(keyCode));
    pushIfDefined(result, killTargetBind.test(keyCode));

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
