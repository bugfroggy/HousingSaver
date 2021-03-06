package co.bugg.housingsaver;

import co.bugg.housingsaver.config.SaverConfig;
import co.bugg.housingsaver.util.MessageBuilder;
import co.bugg.housingsaver.util.json.JsonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The mod's main event handler
 */
public class SaverEventHandler {

    // Regex pattern matching any messages from players that say "!save"
    private static final Pattern pattern = Pattern.compile(SaverConfig.saveRegex);

    @SubscribeEvent
    public void onChatReceivedEvent(ClientChatReceivedEvent event) {

        // This should only be run if currently logged onto the Hypixel Network & the mod is enabled.
        if(HousingSaver.onHypixel && HousingSaver.toggle) {
            Matcher matcher = pattern.matcher(event.message.getUnformattedText());
            if (matcher.find()) {
                // Send the chat message before other chat messages are sent (this event fires before the message is posted)
                event.setCanceled(true);
                MessageBuilder.send(event.message);

                String playerName = matcher.group(1);
                System.out.println("Matches from " + playerName);

                EntityPlayer player = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(playerName);

                System.out.println("Player " + player.getName() + " triggered save command");

                IChatComponent component = MessageBuilder.buildSuccess("Writing save for " + player.getName() + "...");
                MessageBuilder.send(component);
                HousingSaver.buffer.sendPM(player.getName(), "Saving your location, " + player.getName() + "...");

                boolean writeStatus = JsonUtil.write(EntityPlayer.getUUID(player.getGameProfile()).toString(), player.posX, player.posY, player.posZ);
                if(writeStatus) {
                    HousingSaver.buffer.sendPM(player.getName(), "Location saved, " + player.getName() + "! Let "
                            + HousingSaver.master + " know when you need your location loaded back.");
                } else {
                    HousingSaver.buffer.sendPM(player.getName(), "There was an error saving your location!");
                }
            }
        }
    }

    @SubscribeEvent
    public void onLoggedIn(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        // Check if the recent connection is to the Hypixel network.
        boolean singleplayer = Minecraft.getMinecraft().isSingleplayer();
        if(!singleplayer) {
            String ip = Minecraft.getMinecraft().getCurrentServerData().serverIP;
            if (ip.contains(SaverConfig.ipMatch)) {
                HousingSaver.onHypixel = true;
                System.out.println("Currently on Hypixel!");
            }
        }
    }

    @SubscribeEvent
    public void onLoggedOut(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        // Turn off the mod and flag the client as not online hypixel
        if(HousingSaver.onHypixel) System.out.println("Currently logging off Hypixel.");

        HousingSaver.onHypixel = false;
        HousingSaver.toggle = false;
    }

    @SubscribeEvent
    public void onGuiOpen(GuiScreenEvent.DrawScreenEvent.Post e) {
        // When the chat is opened, if the mod is enabled, add text to
        // the screen that says the mod is enabled.
        if(HousingSaver.toggle && HousingSaver.onHypixel && e.gui instanceof GuiChat) {

            // Draw text onto the chat window saying that Housing Saver is enabled.
            e.gui.drawCenteredString(
                    Minecraft.getMinecraft().fontRendererObj,
                    "Housing Saver is enabled.",
                    85,
                    e.gui.height - 25,
                    0xA4F442);


        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.modID.equals(Reference.MOD_ID)) {
            SaverConfig.sync();
        }
    }
}
