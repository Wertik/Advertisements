package space.devport.wertik.advertisements;

import space.devport.utils.text.language.LanguageDefaults;

public class AdvertLanguage extends LanguageDefaults {

    @Override
    public void setDefaults() {
        addDefault("Advert-Message", "&8&m                &r", "  &7Player &f%owner% &7is selling some fine weed,", "  you should come and get some!", "&8&m                &r");
        addDefault("Advert-Expired", "&7Your advertisement &f%name% &7has expired.");

        addDefault("Commands.Invalid-Player", "&cPlayer &f%param% &cdoes not exist.");
        addDefault("Commands.ARM-Not-Hooked", "&cARM is not installed, but 'require-arm-market' is set to true, notify the admins.");
        addDefault("Commands.None", "&cYou have no advertisements.");
        addDefault("Commands.None-Others", "&f%player% &chas no advertisements.");
        addDefault("Commands.You", "&6you");

        addDefault("Commands.Buy.No-Market", "&cYou have to own a Market in order to buy ads.");
        addDefault("Commands.Buy.Limit-Reached", "&cYou own the max amount of adverts. &7(&f%amount%&7/&f%max%&7)");
        addDefault("Commands.Buy.Duplicate-Name", "&cYou already own an ad with the name &f%name%");
        addDefault("Commands.Buy.Could-Not-Create", "&cCould not create your advertisement.");
        addDefault("Commands.Buy.Done", "&7Created your advertisement &f%name% &7and scheduled it.");
        addDefault("Commands.Buy.Done-Others", "&7Created advertisement for &f%player% &7with the name &f%name%");

        addDefault("Commands.Info.Header", "&8&m    &7 Adverts of &f%player%");
        addDefault("Commands.Info.Advert-Line", "&8 - &f%name% &7(exp.: &f%expire% &8- &6T&7-&f%until%&7)");
        addDefault("Commands.Info.Footer", "&8&m        &r");

        addDefault("Commands.Cancel.Done", "&7Cancelled advert &f%name% &7owned by &f%player%");

        addDefault("Placeholders.true", "true");
        addDefault("Placeholders.false", "false");
    }
}