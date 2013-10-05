package net.gameon365.mc.plugins.bungee.switchlimit;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;

import net.craftminecraft.bungee.bungeeyaml.pluginapi.ConfigurablePlugin;

import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.Packet3Chat;

public class SwitchLimit extends ConfigurablePlugin implements Listener {
    protected String regex;
    protected String message;
    protected Long limit;
    protected Map<InetSocketAddress, Long> timer;
    
    @Override
    public void onEnable()
    {
        this.regex = this.getConfig().getString( "command-regex" );
        this.message = this.getConfig().getString( "message" );
        this.limit = this.getConfig().getLong( "limit-seconds" ) * 1000;
        this.timer = new HashMap<>();
        this.getProxy().getPluginManager().registerListener( this, this );
    }
    
    @Override
    public void onDisable()
    {
        this.limit = null;
        this.timer = null;
    }
    
    @EventHandler
    public void onChatEvent( ChatEvent e )
    {
        if( ! e.getMessage().matches( this.regex ) )
        {
            return;
        }
        
        if( this.timer.get( e.getSender().getAddress() ) > ( System.currentTimeMillis() - this.limit ) )
        {
            e.setCancelled( true );
            e.getSender().unsafe().sendPacket( new Packet3Chat( this.message ) );
        }
        
        this.timer.put( e.getSender().getAddress(), System.currentTimeMillis() );
    }
    
    @EventHandler
    public void onPlayerDisconnectEvent( PlayerDisconnectEvent e )
    {
        this.timer.remove( e.getPlayer().getAddress() );
    }
    
    @EventHandler
    public void onServerConnectedEvent( ServerConnectedEvent e )
    {
        this.timer.put( e.getPlayer().getAddress(), new Long( 0 ) );
    }
    
    @EventHandler
    public void onServerKickEvent( ServerKickEvent e )
    {
        this.timer.remove( e.getPlayer().getAddress() );
    }
}
