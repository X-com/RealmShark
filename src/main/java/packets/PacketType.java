package packets;

import packets.Packet.IPacket;
import packets.incoming.*;
import packets.incoming.ip.IpAddress;
import packets.incoming.pets.*;
import packets.incoming.arena.*;
import packets.outgoing.*;
import packets.outgoing.pets.*;
import packets.outgoing.arena.*;

import java.util.HashMap;

import static packets.PacketType.Direction.Incoming;
import static packets.PacketType.Direction.Outgoing;

/**
 * Packet are matched with the packet index sent as a header of packets and returned.
 */
public enum PacketType { //ChristmasTree™   ⛧   <-crown
                           FAILURE(  0, Incoming, FailurePacket::new),
                             HELLO(  1, Outgoing, HelloPacket::new),
                        // Missing
            CLAIM_LOGIN_REWARD_MSG(  3, Outgoing, ClaimDailyRewardMessage::new),
                        DELETE_PET(  4, Incoming, DeletePetMessage::new),
                      REQUESTTRADE(  5, Outgoing, RequestTradePacket::new),
              QUEST_FETCH_RESPONSE(  6, Incoming, QuestFetchResponsePacket::new),
                         JOINGUILD(  7, Outgoing, JoinGuildPacket::new),
                              PING(  8, Incoming, PingPacket::new),
                           NEWTICK(  9, Incoming, NewTickPacket::new),
                        PLAYERTEXT( 10, Outgoing, PlayerTextPacket::new),
                           USEITEM( 11, Outgoing, UseItemPacket::new),
                 SERVERPLAYERSHOOT( 12, Incoming, ServerPlayerShootPacket::new),
                        SHOWEFFECT( 13, Incoming, ShowEffectPacket::new),
                     TRADEACCEPTED( 14, Incoming, TradeAcceptedPacket::new),
                       GUILDREMOVE( 15, Outgoing, GuildRemovePacket::new),
                 PETUPGRADEREQUEST( 16, Outgoing, PetUpgradeRequestPacket::new),
                       ENTER_ARENA( 17, Outgoing, EnterArenaPacket::new),
                              GOTO( 18, Incoming, GotoPacket::new),
                           INVSWAP( 19, Outgoing, InvSwapPacket::new),
                          OTHERHIT( 20, Outgoing, OtherHitPacket::new),
                        NAMERESULT( 21, Incoming, NameResultPacket::new),
                         BUYRESULT( 22, Incoming, BuyResultPacket::new),
                         HATCH_PET( 23, Incoming, HatchPetMessage::new),
         ACTIVE_PET_UPDATE_REQUEST( 24, Outgoing, ActivePetUpdateRequestPacket::new),
                          ENEMYHIT( 25, Outgoing, EnemyHitPacket::new),
                       GUILDRESULT( 26, Incoming, GuildResultPacket::new),
                   EDITACCOUNTLIST( 27, Outgoing, EditAccountListPacket::new),
                      TRADECHANGED( 28, Incoming, TradeChangedPacket::new),
                         // Missing
                       PLAYERSHOOT( 30, Outgoing, PlayerShootPacket::new),
                              PONG( 31, Outgoing, PongPacket::new),
                         // Missing
               PET_CHANGE_SKIN_MSG( 33, Outgoing, ChangePetSkinPacket::new),
                         TRADEDONE( 34, Incoming, TradeDonePacket::new),
                        ENEMYSHOOT( 35, Incoming, EnemyShootPacket::new),
                       ACCEPTTRADE( 36, Outgoing, AcceptTradePacket::new),
                   CHANGEGUILDRANK( 37, Outgoing, ChangeGuildRankPacket::new),
                         PLAYSOUND( 38, Incoming, PlaySoundPacket::new),
                      VERIFY_EMAIL( 39, Incoming, VerifyEmailPacket::new),
                         SQUAREHIT( 40, Outgoing, SquareHitPacket::new),
                       NEW_ABILITY( 41, Incoming, NewAbilityMessage::new),
                              MOVE( 42, Outgoing, MovePacket::new),
                         // Missing
                              TEXT( 44, Incoming, TextPacket::new),
                         RECONNECT( 45, Incoming, ReconnectPacket::new),
                             DEATH( 46, Incoming, DeathPacket::new),
                         USEPORTAL( 47, Outgoing, UsePortalPacket::new),
                    QUEST_ROOM_MSG( 48, Outgoing, GoToQuestRoomPacket::new),
                         ALLYSHOOT( 49, Incoming, AllyShootPacket::new),
               IMMINENT_ARENA_WAVE( 50, Incoming, ImminentArenaWavePacket::new),
                            RESKIN( 51, Outgoing, ReskinPacket::new),
                RESET_DAILY_QUESTS( 52, Outgoing, ResetDailyQuestsPacket::new),
               PET_CHANGE_FORM_MSG( 53, Outgoing, ReskinPetPacket::new),
                        // Missing
                           INVDROP( 55, Outgoing, InvDropPacket::new),
                       CHANGETRADE( 56, Outgoing, ChangeTradePacket::new),
                              LOAD( 57, Outgoing, LoadPacket::new),
                      QUEST_REDEEM( 58, Outgoing, QuestRedeemPacket::new),
                       CREATEGUILD( 59, Outgoing, CreateGuildPacket::new),
                      SETCONDITION( 60, Outgoing, SetConditionPacket::new),
                            CREATE( 61, Outgoing, CreatePacket::new),
                            UPDATE( 62, Incoming, UpdatePacket::new),
                 KEY_INFO_RESPONSE( 63, Incoming, KeyInfoResponsePacket::new),
                               AOE( 64, Incoming, AoePacket::new),
                           GOTOACK( 65, Outgoing, GotoAckPacket::new),
               GLOBAL_NOTIFICATION( 66, Incoming, GlobalNotificationPacket::new),
                      NOTIFICATION( 67, Incoming, NotificationPacket::new),
                       ARENA_DEATH( 68, Incoming, ArenaDeathPacket::new),
                        CLIENTSTAT( 69, Incoming, ClientStatPacket::new),
                        // Missing
                        // Missing
                        // Missing
                        // Missing
                          TELEPORT( 74, Outgoing, TeleportPacket::new),
                            DAMAGE( 75, Incoming, DamagePacket::new),
                   ACTIVEPETUPDATE( 76, Incoming, ActivePetPacket::new),
                    INVITEDTOGUILD( 77, Incoming, InvitedToGuildPacket::new),
                     PETYARDUPDATE( 78, Incoming, PetYardUpdate::new),
                   PASSWORD_PROMPT( 79, Incoming, PasswordPromptPacket::new),
                ACCEPT_ARENA_DEATH( 80, Outgoing, AcceptArenaDeathPacket::new),
                         UPDATEACK( 81, Outgoing, UpdateAckPacket::new),
                        QUESTOBJID( 82, Incoming, QuestObjectIdPacket::new),
                               PIC( 83, Incoming, PicPacket::new),
               REALM_HERO_LEFT_MSG( 84, Incoming, RealmHeroesLeftPacket::new),
                               BUY( 85, Outgoing, BuyPacket::new),
                        TRADESTART( 86, Incoming, TradeStartPacket::new),
                        EVOLVE_PET( 87, Incoming, EvolvedPetMessage::new),
                    TRADEREQUESTED( 88, Incoming, TradeRequestedPacket::new),
                            AOEACK( 89, Outgoing, AoeAckPacket::new),
                         PLAYERHIT( 90, Outgoing, PlayerHitPacket::new),
                       CANCELTRADE( 91, Outgoing, CancelTradePacket::new),
                           MAPINFO( 92, Incoming, MapInfoPacket::new),
                  LOGIN_REWARD_MSG( 93, Incoming, ClaimDailyRewardResponse::new),
                  KEY_INFO_REQUEST( 94, Outgoing, KeyInfoRequestPacket::new),
                         INVRESULT( 95, Incoming, InvResultPacket::new),
             QUEST_REDEEM_RESPONSE( 96, Incoming, QuestRedeemResponsePacket::new),
                        CHOOSENAME( 97, Outgoing, ChooseNamePacket::new),
                   QUEST_FETCH_ASK( 98, Outgoing, QuestFetchAskPacket::new),
                       ACCOUNTLIST( 99, Incoming, AccountListPacket::new),
                          SHOOTACK(100, Outgoing, ShootAckPacket::new),
                    CREATE_SUCCESS(101, Incoming, CreateSuccessPacket::new),
                      CHECKCREDITS(102, Outgoing, CheckCreditsPacket::new),
                      GROUNDDAMAGE(103, Outgoing, GroundDamagePacket::new),
                       GUILDINVITE(104, Outgoing, GuildInvitePacket::new),
                            ESCAPE(105, Outgoing, EscapePacket::new),
                              FILE(106, Incoming, FilePacket::new),
                     RESKIN_UNLOCK(107, Incoming, ReskinUnlockPacket::new),
         NEW_CHARACTER_INFORMATION(108, Incoming, NewCharacterInfoPacket::new),
//                UNLOCK_INFORMATION(109, null, null),
                        // Missing
                        // Missing
                 QUEUE_INFORMATION(112, Incoming, QueueInfoPacket::new),
                      QUEUE_CANCEL(113, Outgoing, QueueCancelPacket::new),
          EXALTATION_BONUS_CHANGED(114, Incoming, ExaltationUpdatePacket::new),
//          REDEEM_EXALTATION_REWARD(115, null, null),
                        // Missing
                      VAULT_UPDATE(117, Incoming, VaultContentPacket::new),
                     FORGE_REQUEST(118, Outgoing, ForgeRequestPacket::new),
                      FORGE_RESULT(119, Incoming, ForgeResultPacket::new),
         FORGE_UNLOCKED_BLUEPRINTS(120, Incoming, ForgeUnlockedBlueprints::new),
                  CHANGE_ALLYSHOOT(121, Outgoing, ChangeAllyShootPacket::new),
                 SHOOT_ACK_COUNTER(122, Outgoing, ShootAckCounterPacket::new),
          GET_PLAYERS_LIST_MESSAGE(123, Outgoing, GetPlayersListPacket::new),
          MODERATOR_ACTION_MESSAGE(124, Outgoing, ModeratorActionMessagePacket::new),
                        // Missing
                CREEP_MOVE_MESSAGE(126, Outgoing, CreepMoveMessagePacket::new),
                        // Missing
                       UNKNOWN134(-122, Outgoing, UnknownPacket134::new),
                        // Missing
                             DASH(-119, Outgoing, DashPacket::new),
                         DASH_ACK(-118, Outgoing, DashAckPacket::new),
                       UNKNOWN139(-117, Outgoing, UnknownPacket139::new),
                        // Missing
                       UNKNOWN145(-111, Outgoing, UnknownPacket145::new),
                       UNKNOWN146(-110, Outgoing, UnknownPacket146::new),
                       UNKNOWN147(-109, Outgoing, UnknownPacket147::new),
                       IP_ADDRESS(1000, Incoming, IpAddress::new);


    private static final HashMap<Integer, PacketType> PACKET_TYPE = new HashMap<>();
    private static final HashMap<Integer, IPacket> PACKET_TYPE_FACTORY = new HashMap<>();
    private static final HashMap<Class, PacketType> PACKET_CLASS = new HashMap<>();

    static {
        try {
            for (PacketType o : PacketType.values()) {
                PACKET_TYPE.put(o.index, o);
                PACKET_TYPE_FACTORY.put(o.index, o.packet);
                PACKET_CLASS.put(o.packet.factory().getClass(), o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final int index;
    private final Direction dir;
    private final IPacket packet;

    PacketType(int i, Direction d, IPacket p) {
        index = i;
        dir = d;
        packet = p;
    }

    /**
     * Get the index of the packet
     *
     * @return Index of the enum.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the enum by index.
     *
     * @param index The index.
     * @return Enum by index.
     */
    public static PacketType byOrdinal(int index) {
        return PACKET_TYPE.get(index);
    }

    /**
     * Get the enum type by class.
     *
     * @param packet The packet to be returned the type of.
     * @return Enum type.
     */
    public static PacketType byClass(Packet packet) {
        return PACKET_CLASS.get(packet.getClass());
    }

    /**
     * Retrieves the packet type from the PACKET_TYPE list.
     *
     * @param type Index of the packet needing to be retrieved.
     * @return Interface IPacket of the class being retrieved.
     */
    public static IPacket getPacket(int type) {
        return PACKET_TYPE_FACTORY.get(type);
    }

    /**
     * Checks if packet type exists in the PACKET_TYPE list.
     *
     * @param type Index of the packet.
     * @return True if the packet exists in the list of packets in PACKET_TYPE.
     */
    public static boolean containsKey(int type) {
        return PACKET_TYPE_FACTORY.containsKey(type);
    }

    /**
     * Returns the class of the enum.
     *
     * @return Class of the enum.
     */
    public Class<? extends Packet> getPacketClass() {
        return packet.factory().getClass();
    }

    public enum Direction {
        Incoming, Outgoing
    }
}
