package com.ender.globalmarket.command;

import com.ender.globalmarket.Main;
import com.ender.globalmarket.data.MarketItem;
import com.ender.globalmarket.economy.MarketData;
import com.ender.globalmarket.economy.MarketEconomy;
import com.ender.globalmarket.money.Vault;
import com.ender.globalmarket.player.Inventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

//import javax.annotation.ParametersAreNonnullByDefault;

public class UserCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        if (args.length == 0) {
            // 参数数量太少，拒绝处理
            return false;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        switch (args[0]) {
            case "buy": {
                //检测参数数量
                if (args.length != 3) {
                    sender.sendMessage(ChatColor.RED + "[GlobalMarket]输入的参数太多或者太少，此处应为3个");
                    return true;
                }
                //检测物品是否存在
                Material itemToBuy = Material.matchMaterial(args[1]);
                if (itemToBuy == null) {
                    sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]你输入的物品不存在，请在物品名称前加命名空间。如原版物品minecraft:..., 或者机械动力物品create:...");
                    return true;
                }
                //检测物品是否在数据库中
                MarketItem marketItem = MarketData.getMarketItem(itemToBuy);
                if (marketItem == null) {
                    sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]你输入的物品当前不可交易，请使用命令/globalmarket list查询可交易物品列表");
                    return true;
                }
                //检测物品数量合法性
                if (Integer.parseInt(args[2]) <= 0) {
                    sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]你输入的数量不合法，应大于0");
                    return true;
                }
                //检测市场存量是否满足
                if (Integer.parseInt(args[2]) > marketItem.x) {
                    sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]你输入的数量超过了当前市场的存量");
                    return true;
                }
                //检测玩家背包是否满足
                if (Inventory.calcEmpty(player) < (double) Integer.parseInt(args[2]) / (double) itemToBuy.getMaxStackSize()) {
                    sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]你的背包剩余空格数不足以容纳你要购买的物品");
                    return true;
                }

                double costs = MarketEconomy.getBuyingPrice(marketItem, Integer.parseInt(args[2]));
                double tax = MarketEconomy.getTax(costs);

                //检测玩家余额是否满足
                if (Vault.checkCurrency(uuid) < costs + tax) {
                    sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]你的账户余额不足以进行本次购买， 本次购买需要税后：$" + (costs + tax));
                    return true;
                } else {
                    Vault.subtractCurrency(uuid, costs + tax);

                    Inventory.addInventory(player, itemToBuy, Integer.parseInt(args[2]));
                    MarketData.updateMarketItemStorage(marketItem);
                    sender.sendMessage(ChatColor.GREEN + "[GlobalMarket]交易成功，你购买了" + args[2] + "个" + itemToBuy.name() + "，税后花费:" + (costs + tax));

                }
                break;
            }
            case "sell": {
//                if (player.getInventory().getItemInMainHand().getType() != null) {
//                        MarketItem marketItem = null;
//                        marketItem = MarketData.getMarketItem(player.getInventory().getItemInMainHand().getType());
//
//                } else {
//                    sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]你需要将需要出售的物品拿在主手中");
//                }
                if (args.length != 3) return false;

                Material itemToSell = Material.matchMaterial(args[1]);
                if (itemToSell == null) {
                    sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]你输入的物品不存在，请在物品名称前加命名空间。如原版物品minecraft:..., 或者机械动力物品create:...");
                    return true;
                }
                //检测此种物品是否可交易
                MarketItem marketItem = MarketData.getMarketItem(itemToSell);
                if (marketItem == null) {
                    sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]你输入的物品当前不可交易，请使用命令/globalmarket list查询可交易物品列表");
                    return true;
                }
                //检测玩家是否输入all，卖出所有此种物品
                if (!args[2].equals("all")) {
                    if (Integer.parseInt(args[2]) <= 0) {
                        sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]你输入的数量不合法，应大于0");
                        return true;
                    }
                }

                //计算背包中的所求物品数
                int amountInInventory = Inventory.calcInventory(player, itemToSell);

                int sellAmount = 0;

                if (amountInInventory == 0) {
                    sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]你的背包空空如也，没有你欲出售的物品！");
                    return true;
                }

                if (Objects.equals(args[2], "all")) {
                    sellAmount = amountInInventory;
                } else {
                    sellAmount = Integer.parseInt(args[2]);
                }

                if (amountInInventory < sellAmount) {
                    sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]你背包中的物品不足！");
                    return true;
                }

                int sellAmountCopy = sellAmount;

                double price = MarketEconomy.getSellingPrice(marketItem, sellAmount);
                double tax = MarketEconomy.getTax(price);

                Vault.addVaultCurrency(uuid, price - tax);

                MarketData.updateMarketItemStorage(marketItem);

                Inventory.subtractInventory(player, itemToSell, sellAmount);

                sender.sendMessage(ChatColor.GREEN + "[GlobalMarket]交易成功，你出售了" + sellAmountCopy + "个" + itemToSell.name() + "，税后所得:" + (price - tax));
                break;
            }
            case "money": {
                sender.sendMessage(String.format("你的账户余额为 $%s", Vault.checkCurrency(uuid)));
                break;
            }
            case "calc": {
                if (args.length != 4) return false;
                Material item = Material.matchMaterial(args[2]);
                if (item == null) {
                    sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]未找到查询的物品，请检查拼写格式是否为例如minecraft:diamond的样式");
                    return true;
                }
                MarketItem marketItem = MarketData.getMarketItem(item);
                if (marketItem == null) {
                    sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]未找到查询的物品，请检查该物品是否允许交易");
                    return true;
                }

                if (Integer.parseInt(args[3]) <= 0) {
                    sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]你输入的数量不合法，应大于0");
                    return true;
                }

                switch (args[1]) {
                    case "buy": {
                        if (marketItem.x < Integer.parseInt(args[3])) {
                            sender.sendMessage(ChatColor.YELLOW + "[GlobalMarket]输入的数量超过当前市场存量，无法计算");
                            return true;
                        }

                        double price = MarketEconomy.getBuyingPrice(marketItem, Integer.parseInt(args[3]));
                        double tax = MarketEconomy.getTax(price);

                        sender.sendMessage(ChatColor.AQUA + "[GlobalMarket]您查询的物品" + args[2] + "购买" + args[3] + "个的价格是$" + price + ", 同时收取贸易税$" + tax);
                        break;
                    }
                    case "sell": {
                        double price = MarketEconomy.getSellingPrice(marketItem, Integer.parseInt(args[3]));
                        double tax = MarketEconomy.getTax(price);

                        sender.sendMessage(ChatColor.AQUA + "[GlobalMarket]您查询的物品" + args[2] + "出售" + args[3] + "个的价格是$" + price + ", 同时收取贸易税$" + tax);
                        break;
                    }
                    default: {
                        return false;
                    }
                }
                break;
            }
            case "list": {
                sender.sendMessage(ChatColor.AQUA + "[GlobalMarket]可交易物品列表查询：");

                MarketItem[] marketItems = MarketData.getAllMarketItems();
                for (int i = 0; marketItems[i] != null; i++) {
                    sender.sendMessage("名称:" + marketItems[i].item.getKey() + " // 库存:" + marketItems[i].x + " // 单价:$" + MarketEconomy.formatMoney(MarketEconomy.calculate(marketItems[i])));

                }
                break;
            }
            case "help":
            default: {
                return false;
            }
        }
        return true;
    }
}