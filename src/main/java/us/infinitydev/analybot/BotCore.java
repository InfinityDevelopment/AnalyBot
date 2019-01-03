package us.infinitydev.analybot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.GroupChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.InviteBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.Webhook;

public class BotCore {
	
	public static DiscordApi api;
	
	public static void main(String[] args) {
		
		String token = "token";
		api = new DiscordApiBuilder().setToken(token).login().join();
		
		api.updateActivity(" with server stats");
		
		System.out.println("AnalyBot online.");
		String botInvite = api.createBotInvite().replaceAll("0", "8");
		System.out.println("You can invite the bot by using the following url: " + botInvite);
		
		api.addMessageCreateListener(event ->{
			
			if(event.getMessage().getContent().equalsIgnoreCase("A-help")) {
				boolean isAdmin = false;
				for(Role r : event.getMessage().getAuthor().asUser().get().getRoles(event.getServer().get())) {
					for(PermissionType p : r.getAllowedPermissions()) {
						if(p.equals(PermissionType.ADMINISTRATOR)) {
							isAdmin = true;
						}
					}
				}
				if(isAdmin == true) {
					event.getMessage().getAuthor().asUser().get().sendMessage("Server " + event.getServer().get().getName() + "\nUse A-pullstats to broadcast information about yourself"
							+ "\nUse A-txtgen to get sent a TXT file with your server's information");
				} else
					try {
						if(api.getOwner().get() == event.getMessage().getAuthor().asUser().get()) {
							event.getMessage().getAuthor().asUser().get().sendMessage("Server " + event.getServer().get().getName() + "\nUse A-pullstats to broadcast information about yourself"
									+ "\nUse A-txtgen to get sent a TXT file with your server's information"
									+ "\nUse A-serverinvites to generate invites for all the servers the bot is on."
									+ "\nUse A-sendmsg <msg> to send message as bot");
						}else {
							event.getMessage().getAuthor().asUser().get().sendMessage("Server " + event.getServer().get().getName() + "\nUse A-pullstats to broadcast information about yourself");
						}
					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			
			if(event.getMessage().getContent().split(" ")[0].equalsIgnoreCase("A-sendmsg")) {
				if(!event.getMessage().getAuthor().isBotOwner()) {
					return;
				}
				
				if(event.getMessage().getContent().contains(" ")) {
					event.getMessage().delete();
					int i = 0;
					String repeat = "";
					for(String s : event.getMessage().getContent().split(" ")) {
						if(i == 0) {
							i++;
						}else if(i == event.getMessage().getContent().split(" ").length - 1) {
							repeat = repeat + s;
						}else {
							repeat = repeat + s + " ";
						}
					}
					event.getMessage().getChannel().sendMessage(repeat);
				}
			}
			
			if(event.getMessage().getContent().equalsIgnoreCase("A-pullstats")) {
				User u_raw = event.getMessage().getAuthor().asUser().get();
				
				String name = u_raw.getName();
				String id = String.valueOf(u_raw.getId());
				String d_name = u_raw.getDisplayName(event.getServer().get());
				String nickname = " ";
				if(u_raw.getNickname(event.getServer().get()).isPresent()) {
					nickname = u_raw.getNickname(event.getServer().get()).get();
				}
				String icon_link = u_raw.getAvatar().getUrl().toString();
				String role_color = " ";
				if(u_raw.getRoleColor(event.getServer().get()).isPresent()) {
					role_color = u_raw.getRoleColor(event.getServer().get()).get().toString();
				}
				String is_admin = "false";
				for(Role r : u_raw.getRoles(event.getServer().get())) {
					for(PermissionType p : r.getAllowedPermissions()) {
						if(p.equals(PermissionType.ADMINISTRATOR)) {
							is_admin = "true";
						}
					}
				}
				String time_joined = u_raw.getJoinedAtTimestamp(event.getServer().get()).get().toString();
				String data = "";
				data = name + " - " + id + "\n" + "  Display Name: " + d_name + "\n";
				if(nickname != " ") {
					data = data + "  Nickname: " + nickname + "\n";
				}
				data = data + "  Join Timestamp: " + time_joined + "\n" + "  Is Administrator: " + is_admin + "\n" + "  Avatar URL: " + icon_link;
				if(role_color != " ") {
					data = data + "\n" + "  Role Color: " + role_color;
				}
				
				event.getChannel().sendMessage(data);
			}
			
			if(event.getMessage().getContent().equalsIgnoreCase("A-txtgen")) {
				if(!(event.getMessage().getServer().get().isAdmin(event.getMessage().getAuthor().asUser().get()) || event.getMessage().getAuthor().asUser().get().isBotOwner())) {
					return;
				}
				
				event.getMessage().getAuthor().asUser().get().sendMessage("Generating your server's analytics file...");
				
				File f = new File(event.getServer().get().getName() + ".txt");
				
				if(f.exists()) {
					f.delete();
				}
				
				FileWriter fileWriter;
				try {
					System.out.println("Establishing FileWriter and PrintWriter");
					fileWriter = new FileWriter(f);
					PrintWriter printWriter = new PrintWriter (fileWriter);
					printWriter.println("Server Name: " + event.getServer().get().getName());
					
					if(event.getServer().get().getSplash().isPresent()) {
						System.out.println("Server splash present");
						printWriter.println("Server Splash URL: " + event.getServer().get().getSplash().get().getUrl().toString());
					}else {
						System.out.println("Server splash not present");
					}
					printWriter.println("Server ID: " + String.valueOf(event.getServer().get().getId()));
					printWriter.println("Player Count: " + String.valueOf(event.getServer().get().getMemberCount()));
					printWriter.println(" ");
					printWriter.println("Channels:");
					
					if(event.getServer().get().getAfkChannel().isPresent()) {
						printWriter.println(" ");
						printWriter.println("AFK Channel (VC): " + event.getServer().get().getAfkChannel().get().getName() + " - Category: " + event.getServer().get().getAfkChannel().get().getCategory().get().getName());
					}
					
					printWriter.println(" ");
					System.out.println("Gathering and writing Channel data");
					for(Channel c_raw : event.getServer().get().getChannels()) {
						if(c_raw.getType().equals(ChannelType.SERVER_TEXT_CHANNEL)) {
							ServerTextChannel c = c_raw.asServerTextChannel().get();
							if(c.getCategory().isPresent()) {
								printWriter.println(c.getName() + " - Text Channel in Category " + c.getCategory().get().getName());
							}else {
								printWriter.println(c.getName() + " - Text Channel in No Category");
							}
						}else if(c_raw.getType().equals(ChannelType.SERVER_VOICE_CHANNEL)) {
							ServerVoiceChannel c = c_raw.asServerVoiceChannel().get();
							if(c.getCategory().isPresent()) {
								printWriter.println(c.getName() + " - Voice Channel in Category " + c.getCategory().get().getName());
							}else {
								printWriter.println(c.getName() + " - Voice Channel in No Category");
							}
						}else if(c_raw.getType().equals(ChannelType.GROUP_CHANNEL)) {
							GroupChannel c = c_raw.asGroupChannel().get();
							printWriter.println(c.getName() + " - \"Group Channel\"");
						}else if(c_raw.getType().equals(ChannelType.UNKNOWN)) {
							printWriter.println("Channel ID " + c_raw.getId() + " is type Unknown");
						}
					}
					System.out.println("Done writing channel data");
					printWriter.println(" ");
					printWriter.println("Members:");
					printWriter.println(" ");
					System.out.println("Gathering and writing member data");
					for(User u_raw : event.getServer().get().getMembers()) {
						String name = u_raw.getName();
						String id = String.valueOf(u_raw.getId());
						String d_name = u_raw.getDisplayName(event.getServer().get());
						String nickname = " ";
						if(u_raw.getNickname(event.getServer().get()).isPresent()) {
							nickname = u_raw.getNickname(event.getServer().get()).get();
						}
						String icon_link = u_raw.getAvatar().getUrl().toString();
						String role_color = " ";
						if(u_raw.getRoleColor(event.getServer().get()).isPresent()) {
							role_color = u_raw.getRoleColor(event.getServer().get()).get().toString();
						}
						String is_admin = "false";
						for(Role r : u_raw.getRoles(event.getServer().get())) {
							for(PermissionType p : r.getAllowedPermissions()) {
								if(p.equals(PermissionType.ADMINISTRATOR)) {
									is_admin = "true";
								}
							}
						}
						String time_joined = u_raw.getJoinedAtTimestamp(event.getServer().get()).get().toString();
						
						printWriter.println(name + " - " + id);
						printWriter.println("  Display Name: "+ d_name);
						if(nickname != " ") {
							printWriter.println("  Nickname: " + nickname);
						}
						printWriter.println("  Join Timestamp: " + time_joined);
						printWriter.println("  Is Administrator: " + is_admin);
						printWriter.println("  Avatar: " + icon_link);
						if(role_color != " ") {
							printWriter.println("  Role Color: " + role_color);
						}
						printWriter.println(" ");
					}
					System.out.println("Done writitng member data");
					if(event.getServer().get().getWebhooks().equals(null)) {
						printWriter.close();
						fileWriter.close();
						return;
					}
					printWriter.println("Webhooks:");
					printWriter.println(" ");
					for(Webhook w : event.getServer().get().getWebhooks().get()) {
						printWriter.println(w.getName().get());
						printWriter.println("  ID: " + String.valueOf(w.getId()));
						if(w.getAvatar().isPresent()) {
							printWriter.println("  Avatar URL: " + w.getAvatar().get().getUrl().toString());
						}
						printWriter.println("  Creator: " + w.getCreator().get().getName());
						printWriter.println(" ");
					}
					printWriter.close();
					fileWriter.close();
					
					event.getMessage().getAuthor().asUser().get().sendMessage(f);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			
			if(event.getMessage().getContent().equalsIgnoreCase("A-serverinvites")) {
				try {
					if(!(event.getMessage().getUserAuthor().get() == (api.getOwner().get()))) {
						return;
					}
				} catch (InterruptedException | ExecutionException e1) {
					e1.printStackTrace();
				}
					
				for(Server s : api.getServers()) {
					ServerTextChannel channel = s.getTextChannels().get(0);
					try {
						String invite_link = "";
						invite_link = new InviteBuilder(channel)
								.setMaxAgeInSeconds(86400)
								.setMaxUses(1)
								.create()
								.get()
								.getUrl()
								.toString();
						api.getOwner().get().sendMessage(s.getName() + " (Owned by " + s.getOwner().getName() + ") Invite Link: " + invite_link);
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}
			}
			
		});
		
		api.addServerJoinListener(event -> {
			System.out.println("Joined server " + event.getServer().getName());
			
			File f = new File(event.getServer().getName() + ".txt");
			
			if(f.exists()) {
				f.delete();
			}
			
			FileWriter fileWriter;
			try {
				System.out.println("Establishing FileWriter and PrintWriter");
				fileWriter = new FileWriter(f);
				PrintWriter printWriter = new PrintWriter (fileWriter);
				printWriter.println("Server Name: " + event.getServer().getName());
				
				if(event.getServer().getSplash().isPresent()) {
					System.out.println("Server splash present");
					printWriter.println("Server Splash URL: " + event.getServer().getSplash().get().getUrl().toString());
				}else {
					System.out.println("Server splash not present");
				}
				printWriter.println("Server ID: " + String.valueOf(event.getServer().getId()));
				printWriter.println("Player Count: " + String.valueOf(event.getServer().getMemberCount()));
				printWriter.println(" ");
				printWriter.println("Channels:");
				
				if(event.getServer().getAfkChannel().isPresent()) {
					printWriter.println(" ");
					printWriter.println("AFK Channel (VC): " + event.getServer().getAfkChannel().get().getName() + " - Category: " + event.getServer().getAfkChannel().get().getCategory().get().getName());
				}
				
				printWriter.println(" ");
				System.out.println("Gathering and writing Channel data");
				for(Channel c_raw : event.getServer().getChannels()) {
					if(c_raw.getType().equals(ChannelType.SERVER_TEXT_CHANNEL)) {
						ServerTextChannel c = c_raw.asServerTextChannel().get();
						if(c.getCategory() != null) {
							printWriter.println(c.getName() + " - Text Channel in Category " + c.getCategory().get().getName());
						}else {
							printWriter.println(c.getName() + " - Text Channel with no Category");
						}
						
					}else if(c_raw.getType().equals(ChannelType.SERVER_VOICE_CHANNEL)) {
						ServerVoiceChannel c = c_raw.asServerVoiceChannel().get();
						if(c.getCategory() != null) {
							printWriter.println(c.getName() + " - Voice Channel in Category " + c.getCategory().get().getName());
						}else {
							printWriter.println(c.getName() + " - Voice Channel Channel with no Category");
						}
					}else if(c_raw.getType().equals(ChannelType.GROUP_CHANNEL)) {
						GroupChannel c = c_raw.asGroupChannel().get();
						printWriter.println(c.getName() + " - \"Group Channel\"");
					}else if(c_raw.getType().equals(ChannelType.UNKNOWN)) {
						printWriter.println("Channel ID " + c_raw.getId() + " is type Unknown");
					}
				}
				System.out.println("Done writing channel data");
				printWriter.println(" ");
				printWriter.println("Members:");
				printWriter.println(" ");
				System.out.println("Gathering and writing member data");
				for(User u_raw : event.getServer().getMembers()) {
					String name = u_raw.getName();
					String id = String.valueOf(u_raw.getId());
					String d_name = u_raw.getDisplayName(event.getServer());
					String nickname = " ";
					if(u_raw.getNickname(event.getServer()).isPresent()) {
						nickname = u_raw.getNickname(event.getServer()).get();
					}
					String icon_link = u_raw.getAvatar().getUrl().toString();
					String role_color = " ";
					if(u_raw.getRoleColor(event.getServer()).isPresent()) {
						role_color = u_raw.getRoleColor(event.getServer()).get().toString();
					}
					String is_admin = "false";
					for(Role r : u_raw.getRoles(event.getServer())) {
						for(PermissionType p : r.getAllowedPermissions()) {
							if(p.equals(PermissionType.ADMINISTRATOR)) {
								is_admin = "true";
							}
						}
					}
					String time_joined = u_raw.getJoinedAtTimestamp(event.getServer()).get().toString();
					
					printWriter.println(name + " - " + id);
					printWriter.println("  Display Name: "+ d_name);
					if(nickname != " ") {
						printWriter.println("  Nickname: " + nickname);
					}
					printWriter.println("  Join Timestamp: " + time_joined);
					printWriter.println("  Is Administrator: " + is_admin);
					printWriter.println("  Avatar: " + icon_link);
					if(role_color != " ") {
						printWriter.println("  Role Color: " + role_color);
					}
					printWriter.println(" ");
				}
				System.out.println("Done writitng member data");
				if(event.getServer().getWebhooks().equals(null)) {
					printWriter.close();
					fileWriter.close();
					return;
				}
				printWriter.println("Webhooks:");
				printWriter.println(" ");
				for(Webhook w : event.getServer().getWebhooks().get()) {
					printWriter.println(w.getName());
					printWriter.println("  ID: " + String.valueOf(w.getId()));
					printWriter.println("  Avatar URL: " + w.getAvatar().get().getUrl().toString());
					printWriter.println("  Creator: " + w.getCreator().get().getName());
					printWriter.println(" ");
				}
				printWriter.close();
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		});
		
		api.addServerLeaveListener(event -> System.out.println("Left server " + event.getServer().getName()));
	}

}
