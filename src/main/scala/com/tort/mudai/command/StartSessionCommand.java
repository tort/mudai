package com.tort.mudai.command;

public class StartSessionCommand implements RenderableCommand {
    public String host;
    public Integer port;

    public StartSessionCommand(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String render() {
        return null;
    }
}
