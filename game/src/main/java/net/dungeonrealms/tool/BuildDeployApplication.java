package net.dungeonrealms.tool;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/7/2016
 */

public class BuildDeployApplication {

    static String TOOL_PATH = "\"C:\\Users\\XenoJava\\Desktop\\DR\\DungeonRealms\\tools\"";

    public static void main(String[] args) {

        if (args.length > 0) switch (args[0]) {
            case "-updateBungee":
                executeCommand("cd " + TOOL_PATH + " && pushBungee.bat");
                break;
            case "-updateDeploymentServer":
                executeCommand("cd " + TOOL_PATH + " && pushDeploymentServer.bat");
                break;
            case "-updateServer":
                executeCommand("cd " + TOOL_PATH + " && pushDev.bat " + args[1].substring(1) + " " + args[2].substring(1));
                break;
            case "-updateAll":
                executeCommand("cd " + TOOL_PATH + " && pushBungee.bat");
                executeCommand("cd " + TOOL_PATH + " && pushDeploymentServer.bat");
                break;
        }
        else System.out.println("Program arguments are invalid!");
    }

    private static void executeCommand(String command) {
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", command);
        builder.redirectErrorStream(true);
        Process p;

        try {
            p = builder.start();
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) break;
                System.out.println(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
