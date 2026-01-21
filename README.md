# 🪙 Dynamic EcoVault v2.0

### For Minecraft 1.21.x

## 💼 Server Economy Overview

Dynamic EcoVault transforms your Minecraft world into a living, breathing economy where supply, demand, banking, taxation, and dynamic trading shape how players earn, spend, and grow.

It’s not just another economy plugin — it’s a complete economic simulation with an in-built dynamic shop, flexible Vault integration, and optimized performance.

⚙️ Core Systems
💹 Dynamic Economy

Prices automatically rise and fall based on player activity, supply, and demand.

Create a realistic marketplace where scarcity and trading truly matter.

Encourage players to think strategically — buy low, sell high, and adapt to changing prices.

Works with EcoVault’s in-built Dynamic Shop GUI — no external shop plugin required!

## 🏪 Dynamic Shop System (NEW)

Fully in-built, lightweight, and configurable shop system.

No dependency on EconomyShopGUI or EssentialsX.

Async-powered for lag-free performance.

Define base, min, and max prices per item in simple YAML files.

Prices automatically adjust based on player sales and purchases.

/shop command opens the interactive Dynamic Shop GUI.

## 💰 Taxation System

Automatically applies taxes on income, trades, payments, and shop transactions.

Taxes are funneled directly into the server bank vault.

Fully configurable tax percentages and collection intervals for balanced gameplay.

Designed to simulate real economic regulation in-game.

## 🏦 Bank System

Integrated directly with Vault — no EssentialsX or fake accounts needed.

The Bank Vault (default name: bankvault, configurable) acts as the global treasury.

Supports loans, interest rates, and repayment periods (default: 7 in-game days).

All deposits, withdrawals, and repayments are safely handled via Vault.

Encourages teamwork, saving, and smarter money management.

## 🔗 Plugin Integration

Dynamic EcoVault works seamlessly with:

🏦 Vault – Core economy handler & global treasury.

⚙️ Dynamic CORE+ – Recommended for best performance & compatibility.

🔒 LuckPerms – Role and permission control.

🪙 PlaceholderAPI – Real-time economy placeholders.

💬 DiscordSRV – Sends tax and loan updates directly to your Discord server.

**(EssentialsX and external shop plugins are no longer required!)**

## 💬 Commands
### 👤 Player Commands

/bank balance — Check your bank balance
/bank deposit <amount> — Deposit money into the bank
/bank withdraw <amount> — Withdraw funds from the bank
/bank loan <amount> — Request a loan
/bank repay — Repay your current loan
/tax info — View current tax rates
/shop — Open the in-built Dynamic Shop GUI

### 🛠️ Admin Commands

/eco set <player> <amount> — Set a player’s balance
/eco give <player> <amount> — Give money to a player
/eco take <player> <amount> — Remove money from a player
/eco reload — Reload configuration files
/eco setprice <item> <price> — Manually set item price
/eco resetmarket — Reset all market data
/tax set <rate> — Adjust tax rate
/tax toggle — Enable or disable taxation
/bankvault balance — Check total server bank balance
/bankvault deposit <amount> — Deposit funds into server vault
/bankvault withdraw <amount> — Withdraw funds from server vault
/loan setrate <percent> — Change loan interest rate
/loan forcerepay <player> — Force a player’s loan repayment

## 🧩 Update Highlights

✅ Added in-built Dynamic Shop System (no external plugins needed)
✅ Removed EssentialsX dependency — integrates with any Vault-based economy
✅ Optimized Vault integration for Dynamic CORE+
✅ Fixed multiple bugs and glitches for smoother transactions
✅ Improved asynchronous performance and data handling

---

### 🌍 **Why Choose Dynamic EcoVault**

Because Minecraft deserves an **economy that evolves**.
From shifting prices to loans and taxes, every financial move influences the world — creating **emergent gameplay, competition, and strategic depth** that no static economy can match.
