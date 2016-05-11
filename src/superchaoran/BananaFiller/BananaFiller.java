package superchaoran.BananaFiller;

import org.powerbot.script.*;
import org.powerbot.script.rt6.*;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.GeItem;

import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by chaoran on 5/10/16.
 */
@Script.Manifest(
        name = "Banana Filler", properties = "author=superchaoran; topic=998877; client=6;",
        description = "Filling basket with bananas and make huge profit off it! 5*banana + basket -> banana(5)"
)
public class BananaFiller extends PollingScript<ClientContext> implements PaintListener {

    /*500g, 2kg and 5kg*/
    private static final long startTime = System.currentTimeMillis();
    private static String status ="Starting";
    private static int totalFills = 0;
    private static int bananaID = 1963;
    private static int banana5ID = 5416;
    private static int basketID = 5376;
    private static GeItem banana = new GeItem(bananaID);
    private static GeItem banana5 = new GeItem(banana5ID);
    private static GeItem basket = new GeItem(basketID);
    private static int fillCount = 0;

    private Npc banker;

    @Override
    public void start() {
//        for (final Item i : ctx.backpack.select()) {
//            log.info(""+ i.id());
//
//        }
//
//        ctx.backpack.select().id(graniteIDs[0]).each(new Filter<Item>() {
//            @Override
//            public boolean accept(Item item) {
//                return item.interact("Craft");
//            }
//        });
        log.info("Find nearest Banker");
        status = "Find nearest Banker";
        banker = ctx.npcs.select().name("Banker").nearest().poll();
        if (!banker.inViewport()) {
            log.info("Banker not in viewport");
            status = "Banker not in viewport";
            ctx.movement.step(banker.tile());
            log.info("Walking to banker");
            status = "Walking to banker";
            if (banker.valid()) {
                log.info("Turn to banker");
                status = "Turn to banker";
                ctx.camera.turnTo(banker);
            } else {
                log.info("Banker not valid");
                status = "Banker not valid";
            }
        }
    }

    @Override
    public void poll() {

        switch (state()) {
            case Bank:

                log.info("Wait for Bank to open");
                status = "Wait for Bank to open";
                Condition.wait(new Condition.Check() {
                    @Override
                    public boolean poll() {
                        return ctx.bank.open();
                    }
                }, 20, 50 * 3);

                log.info("Wait for DepositInventory");
                status = "Wait for DepositInventory";
                Condition.wait(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        return ctx.bank.depositInventory();
                    }
                });

                log.info("Withdrawing...");
                status = "Withdrawing...";
                ctx.bank.withdraw(basketID, 5);
                ctx.bank.withdraw(bananaID, 25);


                log.info("Wait for bank close");
                status = "Wait for bank close...";
                Condition.wait(new Condition.Check() {
                    @Override
                    public boolean poll() {
                        return ctx.bank.close();
                    }
                }, 20, 50 * 3);

                break;

            case Fill:

                log.info("Open Backpack");
                status = "Open Backpack";
                if(!ctx.hud.opened(Hud.Window.BACKPACK)){
                    ctx.hud.open(Hud.Window.BACKPACK);
                }

                //craft 5kg granite
                log.info("Fill Basket..");
                status = "Fill Basket..";
                ctx.backpack.select().id(basketID).each(new Filter<Item>() {
                    @Override
                    public boolean accept(Item item) {
                        if(BananaFiller.fillCount++ <4)
                            return item.interact("Fill");
                        return false;
                    }
                });
                BananaFiller.fillCount = 0;

                //wait for completion
                status = "Wait for filling...";
                Condition.wait(new Condition.Check() {
                    @Override
                    public boolean poll() {
                        return ctx.backpack.select().id(banana5ID).count() == 4;
                    }
                }, 20, 50 * 10);



                log.info("completed");
                status = "Completed a backpack cycle";
                totalFills += 4;
                break;
        }
    }

    private BananaFiller.State state() {
        int count = ctx.backpack.select().count();
        if(ctx.backpack.select().id(basketID).count()!=5 || ctx.backpack.select().id(bananaID).count() != 23){
            return State.Bank;
        } else {
            return State.Fill;
        }
    }

    private enum State {
        Fill, Bank
    }

    Font font = new Font("Arial", Font.PLAIN, 10);
    Color background = new Color(0, 0, 0, 150);
    public void repaint(Graphics graphics) {
        int unitProfit = banana5.price - banana.price *5 + basket.price;
        graphics.setFont(font);
        graphics.setColor(background);
        graphics.drawRect(0, 0, 200, 100);
        graphics.fillRect(0, 0, 200, 100);
        graphics.setColor(Color.WHITE);
        graphics.getFont();
        graphics.drawString("Banana Basket Filler", 5, 15);
        int runtime = Integer.parseInt("" + (System.currentTimeMillis()- startTime));
        graphics.drawString("Run time: " + timeFormat(runtime), 105, 15);
        graphics.drawString("Status: " + status, 5, 40);
        graphics.drawString("Total Craft: " + totalFills, 5, 53);
        graphics.drawString("Fills/h: " + (int)((3600000D*totalFills) / (System.currentTimeMillis() - startTime)), 5, 68);
        graphics.drawString("Profit/Basket(5kg): " + unitProfit, 105, 53);
        graphics.drawString("Total profit: " + unitProfit*totalFills, 105, 68);
        graphics.drawString("Profit/h: " + (int)((3600000D*(unitProfit*totalFills)) / (System.currentTimeMillis() - startTime)), 105, 83);
    }

    private String timeFormat(long duration) {
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
        if (days == 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
    }


}
