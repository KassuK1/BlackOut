package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;

/**
 * @author OLEPOSSU
 */

public class AntiCrawl extends BlackOutModule {
    public AntiCrawl() {
        super(BlackOut.BLACKOUT, "Anti Crawl", "Doesn't crawl or sneak when in low space (should be used on 1.12.2).");
    }
}
