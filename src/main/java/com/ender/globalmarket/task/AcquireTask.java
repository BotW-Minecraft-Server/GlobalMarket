package com.ender.globalmarket.task;

import com.ender.globalmarket.Main;
import com.ender.globalmarket.common.BukkitUtil;
import com.ender.globalmarket.data.MarketItem;
import com.ender.globalmarket.economy.MarketData;
import com.ender.globalmarket.economy.MarketTrade;
import com.ender.globalmarket.storage.ConfigReader;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import com.tripleying.qwq.LocaleLanguageAPI.LocaleLanguageAPI;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * 随机收购事件
 */
public class AcquireTask  implements Runnable{
    Logger log = Logger.getLogger("测试");
    private final Main plugin;

    public AcquireTask(Main plugin) {
        this.plugin = plugin;
    }

    public static String op = ConfigReader.config.getString("OP");

    /**
     * 收购最低数量
     */
    public static int ACQUIRE_BASE = ConfigReader.config.getInt("ACQUIRE_BASE");

    /**
     * 收购数量随机增长额度
     */
    public static int ACQUIRE_GAP = ConfigReader.config.getInt("ACQUIRE_GAP");

    /**
     * 收购物品的价格上限
     */
    public static int ACQUIRE_MAX_PRICE = ConfigReader.config.getInt("ACQUIRE_MAX_PRICE");


    /**
     * 进行随机收购事件
     */
    @Override
    public void run(){
        MarketTrade.randomAcquire = getRandomItem();
        MarketTrade.randomRestCount = new AtomicInteger(getRandomCount());
        Material item = MarketTrade.randomAcquire.item;
        ItemStack is = new ItemStack(item);
        Logger.getLogger("s").info(LocaleLanguageAPI.getItemName(is));
        //广播全服
//        TranslatableComponent name = Component.translatable(Material.STICK.name());
//        StringBuilder stringBuilder = new StringBuilder();
//        BukkitAudiences bukkitAudiences = BukkitAudiences.create(plugin);
//        List<Player> players = BukkitUtil.getAllOnlineUser();
//        for(Player p : players){
//            bukkitAudiences.player(p).sendMessage(name);
//        }
        //Bukkit.broadcast(ChatColor.DARK_GREEN + "[Market]","globalmarket.broadcast");
    }

    /**
     * 获取一个随机出售物品
     * @return
     */
    public MarketItem getRandomItem(){
        List<MarketItem> list = MarketData.getMarketItem();
        //取出剩下的
        list = list.stream().filter(e -> e.x <= ACQUIRE_MAX_PRICE).collect(Collectors.toList());

        Random r = new Random();
        int index = r.nextInt(list.size());

        return list.get(index);
    }

    /**
     * 获取一个随机存量
     * @return 随机存量
     */
    public int getRandomCount(){
        Random r = new Random();
        return ACQUIRE_BASE + r.nextInt(ACQUIRE_GAP);
    }
}
