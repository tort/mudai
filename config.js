function onMudEvent(text) {
    if ((/^Введите имя персонажа/).test(text)) commandExecutor.submit(new com.tort.mudai.command.RawWriteCommand("ладень"));

    return text.replace("\u001B[0;32m", "\u001B[0;35m");
}
