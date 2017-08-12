package co.bugg.housingsaver;

import co.bugg.housingsaver.util.MessageBuilder;
import co.bugg.housingsaver.util.json.JsonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SaverEventHandler {

    // Regex pattern matching any messages from players that say "!test"
    private static final Pattern pattern = Pattern.compile("^<([A-Za-z0-9_]{1,16})> !save");

    @SubscribeEvent
    public void onChatReceivedEvent(ClientChatReceivedEvent event) {
        // This should only be run if currently logged onto the Hypixel Network.
        if(HousingSaver.onHypixel) {
            Matcher matcher = pattern.matcher(event.message.getUnformattedText());
            if (matcher.find()) {
                String playerName = matcher.group(1);
                System.out.println("Matches from " + playerName);

                EntityPlayer player = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(playerName);

                System.out.println("Player " + player.getName() + " triggered save command");

                IChatComponent component = MessageBuilder.buildSuccess("Writing save for " + player.getName() + "...");
                MessageBuilder.send(component);

                JsonUtil.write(EntityPlayer.getUUID(player.getGameProfile()).toString(), player.posX, player.posY, player.posZ);
            }
        }
    }

    @SubscribeEvent
    public void onLoggedIn(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        boolean singleplayer = Minecraft.getMinecraft().isSingleplayer();
        HousingSaver.onHypixel = true;
        if(!singleplayer) {
            String ip = Minecraft.getMinecraft().getCurrentServerData().serverIP;
            if (ip.contains(".hypixel.net")) {
                HousingSaver.onHypixel = true;
                System.out.println("Currently on Hypixel!");
            }
        }
    }

    @SubscribeEvent
    public void onLoggedOut(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        HousingSaver.onHypixel = false;
        HousingSaver.toggle = false;
        System.out.println("Currently logging off Hypixel.");
    }

    @SubscribeEvent
    public void onChatOpen(GuiScreenEvent.DrawScreenEvent.Post e) {
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
}