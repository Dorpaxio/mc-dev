package net.minecraft.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;

public class NetServerHandler extends NetHandler {

    public static Logger logger = Logger.getLogger("Minecraft");
    public INetworkManager networkManager;
    public boolean disconnected = false;
    private MinecraftServer minecraftServer;
    private EntityPlayer player;
    private int f;
    private int g;
    private boolean h;
    private int i;
    private long j;
    private static Random k = new Random();
    private long l;
    private int m = 0;
    private int x = 0;
    private double y;
    private double z;
    private double q;
    private boolean checkMovement = true;
    private IntHashMap s = new IntHashMap();

    public NetServerHandler(MinecraftServer minecraftserver, INetworkManager inetworkmanager, EntityPlayer entityplayer) {
        this.minecraftServer = minecraftserver;
        this.networkManager = inetworkmanager;
        inetworkmanager.a(this);
        this.player = entityplayer;
        entityplayer.netServerHandler = this;
    }

    public void d() {
        this.h = false;
        ++this.f;
        this.minecraftServer.methodProfiler.a("packetflow");
        this.networkManager.b();
        this.minecraftServer.methodProfiler.c("keepAlive");
        if ((long) this.f - this.l > 20L) {
            this.l = (long) this.f;
            this.j = System.nanoTime() / 1000000L;
            this.i = k.nextInt();
            this.sendPacket(new Packet0KeepAlive(this.i));
        }

        if (this.m > 0) {
            --this.m;
        }

        if (this.x > 0) {
            --this.x;
        }

        this.minecraftServer.methodProfiler.c("playerTick");
        if (!this.h && !this.player.viewingCredits) {
            this.player.g();
            if (this.player.vehicle == null) {
                this.player.setPositionRotation(this.y, this.z, this.q, this.player.yaw, this.player.pitch);
            }
        }

        this.minecraftServer.methodProfiler.b();
    }

    public void disconnect(String s) {
        if (!this.disconnected) {
            this.player.m();
            this.sendPacket(new Packet255KickDisconnect(s));
            this.networkManager.d();
            this.minecraftServer.getServerConfigurationManager().sendAll(new Packet3Chat("\u00A7e" + this.player.name + " left the game."));
            this.minecraftServer.getServerConfigurationManager().disconnect(this.player);
            this.disconnected = true;
        }
    }

    public void a(Packet10Flying packet10flying) {
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

        this.h = true;
        if (!this.player.viewingCredits) {
            double d0;

            if (!this.checkMovement) {
                d0 = packet10flying.y - this.z;
                if (packet10flying.x == this.y && d0 * d0 < 0.01D && packet10flying.z == this.q) {
                    this.checkMovement = true;
                }
            }

            if (this.checkMovement) {
                double d1;
                double d2;
                double d3;
                double d4;

                if (this.player.vehicle != null) {
                    float f = this.player.yaw;
                    float f1 = this.player.pitch;

                    this.player.vehicle.V();
                    d1 = this.player.locX;
                    d2 = this.player.locY;
                    d3 = this.player.locZ;
                    double d5 = 0.0D;

                    d4 = 0.0D;
                    if (packet10flying.hasLook) {
                        f = packet10flying.yaw;
                        f1 = packet10flying.pitch;
                    }

                    if (packet10flying.hasPos && packet10flying.y == -999.0D && packet10flying.stance == -999.0D) {
                        if (Math.abs(packet10flying.x) > 1.0D || Math.abs(packet10flying.z) > 1.0D) {
                            System.err.println(this.player.name + " was caught trying to crash the server with an invalid position.");
                            this.disconnect("Nope!");
                            return;
                        }

                        d5 = packet10flying.x;
                        d4 = packet10flying.z;
                    }

                    this.player.onGround = packet10flying.g;
                    this.player.g();
                    this.player.move(d5, 0.0D, d4);
                    this.player.setLocation(d1, d2, d3, f, f1);
                    this.player.motX = d5;
                    this.player.motZ = d4;
                    if (this.player.vehicle != null) {
                        worldserver.vehicleEnteredWorld(this.player.vehicle, true);
                    }

                    if (this.player.vehicle != null) {
                        this.player.vehicle.V();
                    }

                    this.minecraftServer.getServerConfigurationManager().d(this.player);
                    this.y = this.player.locX;
                    this.z = this.player.locY;
                    this.q = this.player.locZ;
                    worldserver.playerJoinedWorld(this.player);
                    return;
                }

                if (this.player.isSleeping()) {
                    this.player.g();
                    this.player.setLocation(this.y, this.z, this.q, this.player.yaw, this.player.pitch);
                    worldserver.playerJoinedWorld(this.player);
                    return;
                }

                d0 = this.player.locY;
                this.y = this.player.locX;
                this.z = this.player.locY;
                this.q = this.player.locZ;
                d1 = this.player.locX;
                d2 = this.player.locY;
                d3 = this.player.locZ;
                float f2 = this.player.yaw;
                float f3 = this.player.pitch;

                if (packet10flying.hasPos && packet10flying.y == -999.0D && packet10flying.stance == -999.0D) {
                    packet10flying.hasPos = false;
                }

                if (packet10flying.hasPos) {
                    d1 = packet10flying.x;
                    d2 = packet10flying.y;
                    d3 = packet10flying.z;
                    d4 = packet10flying.stance - packet10flying.y;
                    if (!this.player.isSleeping() && (d4 > 1.65D || d4 < 0.1D)) {
                        this.disconnect("Illegal stance");
                        logger.warning(this.player.name + " had an illegal stance: " + d4);
                        return;
                    }

                    if (Math.abs(packet10flying.x) > 3.2E7D || Math.abs(packet10flying.z) > 3.2E7D) {
                        this.disconnect("Illegal position");
                        return;
                    }
                }

                if (packet10flying.hasLook) {
                    f2 = packet10flying.yaw;
                    f3 = packet10flying.pitch;
                }

                this.player.g();
                this.player.V = 0.0F;
                this.player.setLocation(this.y, this.z, this.q, f2, f3);
                if (!this.checkMovement) {
                    return;
                }

                d4 = d1 - this.player.locX;
                double d6 = d2 - this.player.locY;
                double d7 = d3 - this.player.locZ;
                double d8 = Math.min(Math.abs(d4), Math.abs(this.player.motX));
                double d9 = Math.min(Math.abs(d6), Math.abs(this.player.motY));
                double d10 = Math.min(Math.abs(d7), Math.abs(this.player.motZ));
                double d11 = d8 * d8 + d9 * d9 + d10 * d10;

                if (d11 > 100.0D && (!this.minecraftServer.H() || !this.minecraftServer.G().equals(this.player.name))) {
                    logger.warning(this.player.name + " moved too quickly! " + d4 + "," + d6 + "," + d7 + " (" + d8 + ", " + d9 + ", " + d10 + ")");
                    this.a(this.y, this.z, this.q, this.player.yaw, this.player.pitch);
                    return;
                }

                float f4 = 0.0625F;
                boolean flag = worldserver.getCubes(this.player, this.player.boundingBox.clone().shrink((double) f4, (double) f4, (double) f4)).isEmpty();

                if (this.player.onGround && !packet10flying.g && d6 > 0.0D) {
                    this.player.j(0.2F);
                }

                this.player.move(d4, d6, d7);
                this.player.onGround = packet10flying.g;
                this.player.checkMovement(d4, d6, d7);
                double d12 = d6;

                d4 = d1 - this.player.locX;
                d6 = d2 - this.player.locY;
                if (d6 > -0.5D || d6 < 0.5D) {
                    d6 = 0.0D;
                }

                d7 = d3 - this.player.locZ;
                d11 = d4 * d4 + d6 * d6 + d7 * d7;
                boolean flag1 = false;

                if (d11 > 0.0625D && !this.player.isSleeping() && !this.player.itemInWorldManager.isCreative()) {
                    flag1 = true;
                    logger.warning(this.player.name + " moved wrongly!");
                }

                this.player.setLocation(d1, d2, d3, f2, f3);
                boolean flag2 = worldserver.getCubes(this.player, this.player.boundingBox.clone().shrink((double) f4, (double) f4, (double) f4)).isEmpty();

                if (flag && (flag1 || !flag2) && !this.player.isSleeping()) {
                    this.a(this.y, this.z, this.q, f2, f3);
                    return;
                }

                AxisAlignedBB axisalignedbb = this.player.boundingBox.clone().grow((double) f4, (double) f4, (double) f4).a(0.0D, -0.55D, 0.0D);

                if (!this.minecraftServer.getAllowFlight() && !this.player.itemInWorldManager.isCreative() && !worldserver.c(axisalignedbb)) {
                    if (d12 >= -0.03125D) {
                        ++this.g;
                        if (this.g > 80) {
                            logger.warning(this.player.name + " was kicked for floating too long!");
                            this.disconnect("Flying is not enabled on this server");
                            return;
                        }
                    }
                } else {
                    this.g = 0;
                }

                this.player.onGround = packet10flying.g;
                this.minecraftServer.getServerConfigurationManager().d(this.player);
                this.player.b(this.player.locY - d0, packet10flying.g);
            }
        }
    }

    public void a(double d0, double d1, double d2, float f, float f1) {
        this.checkMovement = false;
        this.y = d0;
        this.z = d1;
        this.q = d2;
        this.player.setLocation(d0, d1, d2, f, f1);
        this.player.netServerHandler.sendPacket(new Packet13PlayerLookMove(d0, d1 + 1.6200000047683716D, d1, d2, f, f1, false));
    }

    public void a(Packet14BlockDig packet14blockdig) {
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

        if (packet14blockdig.e == 4) {
            this.player.bB();
        } else if (packet14blockdig.e == 5) {
            this.player.by();
        } else {
            boolean flag = worldserver.weirdIsOpCache = worldserver.worldProvider.dimension != 0 || this.minecraftServer.getServerConfigurationManager().isOp(this.player.name) || this.minecraftServer.H();
            boolean flag1 = false;

            if (packet14blockdig.e == 0) {
                flag1 = true;
            }

            if (packet14blockdig.e == 2) {
                flag1 = true;
            }

            int i = packet14blockdig.a;
            int j = packet14blockdig.b;
            int k = packet14blockdig.c;

            if (flag1) {
                double d0 = this.player.locX - ((double) i + 0.5D);
                double d1 = this.player.locY - ((double) j + 0.5D) + 1.5D;
                double d2 = this.player.locZ - ((double) k + 0.5D);
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (d3 > 36.0D) {
                    return;
                }

                if (j >= this.minecraftServer.getMaxBuildHeight()) {
                    return;
                }
            }

            ChunkCoordinates chunkcoordinates = worldserver.getSpawn();
            int l = MathHelper.a(i - chunkcoordinates.x);
            int i1 = MathHelper.a(k - chunkcoordinates.z);

            if (l > i1) {
                i1 = l;
            }

            if (packet14blockdig.e == 0) {
                if (i1 <= 16 && !flag) {
                    this.player.netServerHandler.sendPacket(new Packet53BlockChange(i, j, k, worldserver));
                } else {
                    this.player.itemInWorldManager.dig(i, j, k, packet14blockdig.face);
                }
            } else if (packet14blockdig.e == 2) {
                this.player.itemInWorldManager.a(i, j, k);
                if (worldserver.getTypeId(i, j, k) != 0) {
                    this.player.netServerHandler.sendPacket(new Packet53BlockChange(i, j, k, worldserver));
                }
            } else if (packet14blockdig.e == 1) {
                this.player.itemInWorldManager.c(i, j, k);
                if (worldserver.getTypeId(i, j, k) != 0) {
                    this.player.netServerHandler.sendPacket(new Packet53BlockChange(i, j, k, worldserver));
                }
            } else if (packet14blockdig.e == 3) {
                double d4 = this.player.locX - ((double) i + 0.5D);
                double d5 = this.player.locY - ((double) j + 0.5D);
                double d6 = this.player.locZ - ((double) k + 0.5D);
                double d7 = d4 * d4 + d5 * d5 + d6 * d6;

                if (d7 < 256.0D) {
                    this.player.netServerHandler.sendPacket(new Packet53BlockChange(i, j, k, worldserver));
                }
            }

            worldserver.weirdIsOpCache = false;
        }
    }

    public void a(Packet15Place packet15place) {
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
        ItemStack itemstack = this.player.inventory.getItemInHand();
        boolean flag = false;
        int i = packet15place.d();
        int j = packet15place.f();
        int k = packet15place.g();
        int l = packet15place.getFace();
        boolean flag1 = worldserver.weirdIsOpCache = worldserver.worldProvider.dimension != 0 || this.minecraftServer.getServerConfigurationManager().isOp(this.player.name) || this.minecraftServer.H();

        if (packet15place.getFace() == 255) {
            if (itemstack == null) {
                return;
            }

            this.player.itemInWorldManager.useItem(this.player, worldserver, itemstack);
        } else if (packet15place.f() >= this.minecraftServer.getMaxBuildHeight() - 1 && (packet15place.getFace() == 1 || packet15place.f() >= this.minecraftServer.getMaxBuildHeight())) {
            this.player.netServerHandler.sendPacket(new Packet3Chat("\u00A77Height limit for building is " + this.minecraftServer.getMaxBuildHeight()));
            flag = true;
        } else {
            ChunkCoordinates chunkcoordinates = worldserver.getSpawn();
            int i1 = MathHelper.a(i - chunkcoordinates.x);
            int j1 = MathHelper.a(k - chunkcoordinates.z);

            if (i1 > j1) {
                j1 = i1;
            }

            if (this.checkMovement && this.player.e((double) i + 0.5D, (double) j + 0.5D, (double) k + 0.5D) < 64.0D && (j1 > 16 || flag1)) {
                this.player.itemInWorldManager.interact(this.player, worldserver, itemstack, i, j, k, l, packet15place.j(), packet15place.l(), packet15place.m());
            }

            flag = true;
        }

        if (flag) {
            this.player.netServerHandler.sendPacket(new Packet53BlockChange(i, j, k, worldserver));
            if (l == 0) {
                --j;
            }

            if (l == 1) {
                ++j;
            }

            if (l == 2) {
                --k;
            }

            if (l == 3) {
                ++k;
            }

            if (l == 4) {
                --i;
            }

            if (l == 5) {
                ++i;
            }

            this.player.netServerHandler.sendPacket(new Packet53BlockChange(i, j, k, worldserver));
        }

        itemstack = this.player.inventory.getItemInHand();
        if (itemstack != null && itemstack.count == 0) {
            this.player.inventory.items[this.player.inventory.itemInHandIndex] = null;
            itemstack = null;
        }

        if (itemstack == null || itemstack.m() == 0) {
            this.player.h = true;
            this.player.inventory.items[this.player.inventory.itemInHandIndex] = ItemStack.b(this.player.inventory.items[this.player.inventory.itemInHandIndex]);
            Slot slot = this.player.activeContainer.a((IInventory) this.player.inventory, this.player.inventory.itemInHandIndex);

            this.player.activeContainer.b();
            this.player.h = false;
            if (!ItemStack.matches(this.player.inventory.getItemInHand(), packet15place.getItemStack())) {
                this.sendPacket(new Packet103SetSlot(this.player.activeContainer.windowId, slot.d, this.player.inventory.getItemInHand()));
            }
        }

        worldserver.weirdIsOpCache = false;
    }

    public void a(String s, Object[] aobject) {
        logger.info(this.player.name + " lost connection: " + s);
        this.minecraftServer.getServerConfigurationManager().sendAll(new Packet3Chat("\u00A7e" + this.player.name + " left the game."));
        this.minecraftServer.getServerConfigurationManager().disconnect(this.player);
        this.disconnected = true;
        if (this.minecraftServer.H() && this.player.name.equals(this.minecraftServer.G())) {
            logger.info("Stopping singleplayer server as player logged out");
            this.minecraftServer.safeShutdown();
        }
    }

    public void onUnhandledPacket(Packet packet) {
        logger.warning(this.getClass() + " wasn\'t prepared to deal with a " + packet.getClass());
        this.disconnect("Protocol error, unexpected packet");
    }

    public void sendPacket(Packet packet) {
        if (packet instanceof Packet3Chat) {
            Packet3Chat packet3chat = (Packet3Chat) packet;
            int i = this.player.getChatFlags();

            if (i == 2) {
                return;
            }

            if (i == 1 && !packet3chat.isServer()) {
                return;
            }
        }

        this.networkManager.queue(packet);
    }

    public void a(Packet16BlockItemSwitch packet16blockitemswitch) {
        if (packet16blockitemswitch.itemInHandIndex >= 0 && packet16blockitemswitch.itemInHandIndex < PlayerInventory.getHotbarSize()) {
            this.player.inventory.itemInHandIndex = packet16blockitemswitch.itemInHandIndex;
        } else {
            logger.warning(this.player.name + " tried to set an invalid carried item");
        }
    }

    public void a(Packet3Chat packet3chat) {
        if (this.player.getChatFlags() == 2) {
            this.sendPacket(new Packet3Chat("Cannot send chat message."));
        } else {
            String s = packet3chat.message;

            if (s.length() > 100) {
                this.disconnect("Chat message too long");
            } else {
                s = s.trim();

                for (int i = 0; i < s.length(); ++i) {
                    if (!SharedConstants.isAllowedChatCharacter(s.charAt(i))) {
                        this.disconnect("Illegal characters in chat");
                        return;
                    }
                }

                if (s.startsWith("/")) {
                    this.handleCommand(s);
                } else {
                    if (this.player.getChatFlags() == 1) {
                        this.sendPacket(new Packet3Chat("Cannot send chat message."));
                        return;
                    }

                    s = "<" + this.player.name + "> " + s;
                    logger.info(s);
                    this.minecraftServer.getServerConfigurationManager().sendAll(new Packet3Chat(s, false));
                }

                this.m += 20;
                if (this.m > 200 && !this.minecraftServer.getServerConfigurationManager().isOp(this.player.name)) {
                    this.disconnect("disconnect.spam");
                }
            }
        }
    }

    private void handleCommand(String s) {
        this.minecraftServer.getCommandHandler().a(this.player, s);
    }

    public void a(Packet18ArmAnimation packet18armanimation) {
        if (packet18armanimation.b == 1) {
            this.player.i();
        }
    }

    public void a(Packet19EntityAction packet19entityaction) {
        if (packet19entityaction.animation == 1) {
            this.player.setSneaking(true);
        } else if (packet19entityaction.animation == 2) {
            this.player.setSneaking(false);
        } else if (packet19entityaction.animation == 4) {
            this.player.setSprinting(true);
        } else if (packet19entityaction.animation == 5) {
            this.player.setSprinting(false);
        } else if (packet19entityaction.animation == 3) {
            this.player.a(false, true, true);
            this.checkMovement = false;
        }
    }

    public void a(Packet255KickDisconnect packet255kickdisconnect) {
        this.networkManager.a("disconnect.quitting", new Object[0]);
    }

    public int lowPriorityCount() {
        return this.networkManager.e();
    }

    public void a(Packet7UseEntity packet7useentity) {
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
        Entity entity = worldserver.getEntity(packet7useentity.target);

        if (entity != null) {
            boolean flag = this.player.l(entity);
            double d0 = 36.0D;

            if (!flag) {
                d0 = 9.0D;
            }

            if (this.player.e(entity) < d0) {
                if (packet7useentity.action == 0) {
                    this.player.m(entity);
                } else if (packet7useentity.action == 1) {
                    this.player.attack(entity);
                }
            }
        }
    }

    public void a(Packet205ClientCommand packet205clientcommand) {
        if (packet205clientcommand.a == 1) {
            if (this.player.viewingCredits) {
                this.player = this.minecraftServer.getServerConfigurationManager().moveToWorld(this.player, 0, true);
            } else if (this.player.q().getWorldData().isHardcore()) {
                if (this.minecraftServer.H() && this.player.name.equals(this.minecraftServer.G())) {
                    this.player.netServerHandler.disconnect("You have died. Game over, man, it\'s game over!");
                    this.minecraftServer.O();
                } else {
                    BanEntry banentry = new BanEntry(this.player.name);

                    banentry.setReason("Death in Hardcore");
                    this.minecraftServer.getServerConfigurationManager().getNameBans().add(banentry);
                    this.player.netServerHandler.disconnect("You have died. Game over, man, it\'s game over!");
                }
            } else {
                if (this.player.getHealth() > 0) {
                    return;
                }

                this.player = this.minecraftServer.getServerConfigurationManager().moveToWorld(this.player, 0, false);
            }
        }
    }

    public boolean b() {
        return true;
    }

    public void a(Packet9Respawn packet9respawn) {}

    public void handleContainerClose(Packet101CloseWindow packet101closewindow) {
        this.player.l();
    }

    public void a(Packet102WindowClick packet102windowclick) {
        if (this.player.activeContainer.windowId == packet102windowclick.a && this.player.activeContainer.b(this.player)) {
            ItemStack itemstack = this.player.activeContainer.clickItem(packet102windowclick.slot, packet102windowclick.button, packet102windowclick.shift, this.player);

            if (ItemStack.matches(packet102windowclick.item, itemstack)) {
                this.player.netServerHandler.sendPacket(new Packet106Transaction(packet102windowclick.a, packet102windowclick.d, true));
                this.player.h = true;
                this.player.activeContainer.b();
                this.player.broadcastCarriedItem();
                this.player.h = false;
            } else {
                this.s.a(this.player.activeContainer.windowId, Short.valueOf(packet102windowclick.d));
                this.player.netServerHandler.sendPacket(new Packet106Transaction(packet102windowclick.a, packet102windowclick.d, false));
                this.player.activeContainer.a(this.player, false);
                ArrayList arraylist = new ArrayList();

                for (int i = 0; i < this.player.activeContainer.b.size(); ++i) {
                    arraylist.add(((Slot) this.player.activeContainer.b.get(i)).getItem());
                }

                this.player.a(this.player.activeContainer, arraylist);
            }
        }
    }

    public void a(Packet108ButtonClick packet108buttonclick) {
        if (this.player.activeContainer.windowId == packet108buttonclick.a && this.player.activeContainer.b(this.player)) {
            this.player.activeContainer.a((EntityHuman) this.player, packet108buttonclick.b);
            this.player.activeContainer.b();
        }
    }

    public void a(Packet107SetCreativeSlot packet107setcreativeslot) {
        if (this.player.itemInWorldManager.isCreative()) {
            boolean flag = packet107setcreativeslot.slot < 0;
            ItemStack itemstack = packet107setcreativeslot.b;
            boolean flag1 = packet107setcreativeslot.slot >= 1 && packet107setcreativeslot.slot < 36 + PlayerInventory.getHotbarSize();
            boolean flag2 = itemstack == null || itemstack.id < Item.byId.length && itemstack.id >= 0 && Item.byId[itemstack.id] != null;
            boolean flag3 = itemstack == null || itemstack.getData() >= 0 && itemstack.getData() >= 0 && itemstack.count <= 64 && itemstack.count > 0;

            if (flag1 && flag2 && flag3) {
                if (itemstack == null) {
                    this.player.defaultContainer.setItem(packet107setcreativeslot.slot, (ItemStack) null);
                } else {
                    this.player.defaultContainer.setItem(packet107setcreativeslot.slot, itemstack);
                }

                this.player.defaultContainer.a(this.player, true);
            } else if (flag && flag2 && flag3 && this.x < 200) {
                this.x += 20;
                EntityItem entityitem = this.player.drop(itemstack);

                if (entityitem != null) {
                    entityitem.d();
                }
            }
        }
    }

    public void a(Packet106Transaction packet106transaction) {
        Short oshort = (Short) this.s.get(this.player.activeContainer.windowId);

        if (oshort != null && packet106transaction.b == oshort.shortValue() && this.player.activeContainer.windowId == packet106transaction.a && !this.player.activeContainer.b(this.player)) {
            this.player.activeContainer.a(this.player, true);
        }
    }

    public void a(Packet130UpdateSign packet130updatesign) {
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

        if (worldserver.isLoaded(packet130updatesign.x, packet130updatesign.y, packet130updatesign.z)) {
            TileEntity tileentity = worldserver.getTileEntity(packet130updatesign.x, packet130updatesign.y, packet130updatesign.z);

            if (tileentity instanceof TileEntitySign) {
                TileEntitySign tileentitysign = (TileEntitySign) tileentity;

                if (!tileentitysign.a()) {
                    this.minecraftServer.warning("Player " + this.player.name + " just tried to change non-editable sign");
                    return;
                }
            }

            int i;
            int j;

            for (j = 0; j < 4; ++j) {
                boolean flag = true;

                if (packet130updatesign.lines[j].length() > 15) {
                    flag = false;
                } else {
                    for (i = 0; i < packet130updatesign.lines[j].length(); ++i) {
                        if (SharedConstants.allowedCharacters.indexOf(packet130updatesign.lines[j].charAt(i)) < 0) {
                            flag = false;
                        }
                    }
                }

                if (!flag) {
                    packet130updatesign.lines[j] = "!?";
                }
            }

            if (tileentity instanceof TileEntitySign) {
                j = packet130updatesign.x;
                int k = packet130updatesign.y;

                i = packet130updatesign.z;
                TileEntitySign tileentitysign1 = (TileEntitySign) tileentity;

                System.arraycopy(packet130updatesign.lines, 0, tileentitysign1.lines, 0, 4);
                tileentitysign1.update();
                worldserver.notify(j, k, i);
            }
        }
    }

    public void a(Packet0KeepAlive packet0keepalive) {
        if (packet0keepalive.a == this.i) {
            int i = (int) (System.nanoTime() / 1000000L - this.j);

            this.player.ping = (this.player.ping * 3 + i) / 4;
        }
    }

    public boolean a() {
        return true;
    }

    public void a(Packet202Abilities packet202abilities) {
        this.player.abilities.isFlying = packet202abilities.f() && this.player.abilities.canFly;
    }

    public void a(Packet203TabComplete packet203tabcomplete) {
        StringBuilder stringbuilder = new StringBuilder();

        String s;

        for (Iterator iterator = this.minecraftServer.a((ICommandListener) this.player, packet203tabcomplete.d()).iterator(); iterator.hasNext(); stringbuilder.append(s)) {
            s = (String) iterator.next();
            if (stringbuilder.length() > 0) {
                stringbuilder.append(");
            }
        }

        this.player.netServerHandler.sendPacket(new Packet203TabComplete(stringbuilder.toString()));
    }

    public void a(Packet204LocaleAndViewDistance packet204localeandviewdistance) {
        this.player.a(packet204localeandviewdistance);
    }

    public void a(Packet250CustomPayload packet250custompayload) {
        DataInputStream datainputstream;
        ItemStack itemstack;
        ItemStack itemstack1;

        if ("MC|BEdit".equals(packet250custompayload.tag)) {
            try {
                datainputstream = new DataInputStream(new ByteArrayInputStream(packet250custompayload.data));
                itemstack = Packet.c(datainputstream);
                if (!ItemBookAndQuill.a(itemstack.getTag())) {
                    throw new IOException("Invalid book tag!");
                }

                itemstack1 = this.player.inventory.getItemInHand();
                if (itemstack != null && itemstack.id == Item.BOOK_AND_QUILL.id && itemstack.id == itemstack1.id) {
                    itemstack1.setTag(itemstack.getTag());
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else if ("MC|BSign".equals(packet250custompayload.tag)) {
            try {
                datainputstream = new DataInputStream(new ByteArrayInputStream(packet250custompayload.data));
                itemstack = Packet.c(datainputstream);
                if (!ItemWrittenBook.a(itemstack.getTag())) {
                    throw new IOException("Invalid book tag!");
                }

                itemstack1 = this.player.inventory.getItemInHand();
                if (itemstack != null && itemstack.id == Item.WRITTEN_BOOK.id && itemstack1.id == Item.BOOK_AND_QUILL.id) {
                    itemstack1.setTag(itemstack.getTag());
                    itemstack1.id = Item.WRITTEN_BOOK.id;
                }
            } catch (Exception exception1) {
                exception1.printStackTrace();
            }
        } else if ("MC|TrSel".equals(packet250custompayload.tag)) {
            try {
                datainputstream = new DataInputStream(new ByteArrayInputStream(packet250custompayload.data));
                int i = datainputstream.readInt();
                Container container = this.player.activeContainer;

                if (container instanceof ContainerMerchant) {
                    ((ContainerMerchant) container).c(i);
                }
            } catch (Exception exception2) {
                exception2.printStackTrace();
            }
        }
    }
}
