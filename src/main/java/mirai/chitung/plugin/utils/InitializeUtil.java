package mirai.chitung.plugin.utils;

import mirai.chitung.plugin.administration.config.ConfigHandler;
import mirai.chitung.plugin.core.groupconfig.GroupConfigManager;
import mirai.chitung.plugin.core.responder.Blacklist;
import mirai.chitung.plugin.core.responder.ResponderManager;
import mirai.chitung.plugin.core.responder.imageresponder.ImageResponder;
import mirai.chitung.plugin.core.responder.universalrespond.URManager;

public class InitializeUtil {

    static final String WELCOME_TEXT = "\n" +
            " ██████╗██╗  ██╗██╗████████╗██╗   ██╗███╗   ██╗ ██████╗ \n" +
            "██╔════╝██║  ██║██║╚══██╔══╝██║   ██║████╗  ██║██╔════╝ \n" +
            "██║     ███████║██║   ██║   ██║   ██║██╔██╗ ██║██║  ███╗\n" +
            "██║     ██╔══██║██║   ██║   ██║   ██║██║╚██╗██║██║   ██║\n" +
            "╚██████╗██║  ██║██║   ██║   ╚██████╔╝██║ ╚████║╚██████╔╝\n" +
            " ╚═════╝╚═╝  ╚═╝╚═╝   ╚═╝    ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝ \n" +
            "                                                        \n" +
            "██████╗ ██╗   ██╗██████╗ ██╗     ██╗ ██████╗            \n" +
            "██╔══██╗██║   ██║██╔══██╗██║     ██║██╔════╝            \n" +
            "██████╔╝██║   ██║██████╔╝██║     ██║██║                 \n" +
            "██╔═══╝ ██║   ██║██╔══██╗██║     ██║██║                 \n" +
            "██║     ╚██████╔╝██████╔╝███████╗██║╚██████╗            \n" +
            "╚═╝      ╚═════╝ ╚═════╝ ╚══════╝╚═╝ ╚═════╝            \n" +
            "                                                        \n";

    public static void initialize(){
        GroupPolice.getINSTANCE().ini();
        ConfigHandler.getINSTANCE().ini();
        GroupConfigManager.getINSTANCE().ini();
        URManager.getINSTANCE().ini();
        Blacklist.getINSTANCE().ini();
        ImageResponder.getINSTANCE().ini();
        ResponderManager.getINSTANCE().ini();
        System.out.println(WELCOME_TEXT);
    }
}
