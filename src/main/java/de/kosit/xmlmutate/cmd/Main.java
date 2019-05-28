package de.kosit.xmlmutate.cmd;

import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiRenderer;
import org.fusesource.jansi.AnsiRenderer.Code;

/**
 * @author Andreas Penski
 */
public class Main {

    public static void main(final String[] args) {
        AnsiConsole.systemInstall();
        System.out.println(AnsiRenderer.render("bla", Code.BG_CYAN.name(), Code.BG_BLUE.name()));
    }
}
