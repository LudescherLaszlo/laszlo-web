package com.mrwhite.game;

import com.mrwhite.dto.*;
import com.mrwhite.words.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class GameService {
    private final WordPairService words;
    private final SimpMessagingTemplate messaging;
    private final Map<String, Player> bySession = new LinkedHashMap<>();
    private final Map<String, String> votes = new HashMap<>();
    private final Set<String> ready = new HashSet<>();
    private final Set<String> judges = new LinkedHashSet<>();
    private final Map<String, Boolean> verdicts = new HashMap<>();
    private final List<String> speakingOrder = new ArrayList<>();
    private Phase phase = Phase.LOBBY;
    private long version, noticeId;
    private int round;
    private String currentSpeaker, civilianWord, undercoverWord, executedId, guesserId;
    private WinType victoryType;
    private String abortedBy;
    private long lastAction = System.currentTimeMillis();
    private final int minPlayers, maxPlayers, nameMaxLength, recentPairsExcluded;

    public GameService(WordPairService words, SimpMessagingTemplate messaging,
                       @Value("${mrwhite.minPlayers:3}") int minPlayers,
                       @Value("${mrwhite.maxPlayers:30}") int maxPlayers,
                       @Value("${mrwhite.nameMaxLength:20}") int nameMaxLength,
                       @Value("${mrwhite.recentPairsExcluded:10}") int recentPairsExcluded) {
        this.words=words; this.messaging=messaging; this.minPlayers=minPlayers; this.maxPlayers=maxPlayers;
        this.nameMaxLength=nameMaxLength; this.recentPairsExcluded=recentPairsExcluded;
    }
    public synchronized String[] join(String rawName) {
        String name = rawName == null ? "" : rawName.trim().replaceAll("\\s+", " ");
        if (name.isBlank() || name.length() > nameMaxLength || !name.matches("[\\p{L}\\p{N} .,'!?-]+"))
            throw new IllegalArgumentException("Please enter a valid name (1-20 characters).");
        if (bySession.values().stream().anyMatch(p -> p.getName().equalsIgnoreCase(name)))
            throw new IllegalStateException("That name is already taken. Choose another one.");
        long active = bySession.values().stream().filter(p -> p.getStatus() != PlayerStatus.POSTGAME).count();
        if (active >= maxPlayers) throw new IllegalStateException("Lobby full (" + maxPlayers + "/" + maxPlayers + ")");
        String session = UUID.randomUUID().toString();
        Player p = new Player(session, name, phase == Phase.LOBBY ? PlayerStatus.LOBBY : PlayerStatus.WAITING);
        bySession.put(session,p); changed(); return new String[]{session,p.getId()};
    }
    public synchronized Player player(String session) { return bySession.get(session); }
    public synchronized void resetForInactivity() {
        if (System.currentTimeMillis() - lastAction >= 30 * 60_000L) {
            bySession.clear(); votes.clear(); ready.clear(); phase=Phase.LOBBY; version++; broadcast();
        }
    }
    public synchronized void connected(String session) { Player p=bySession.get(session); if(p!=null){p.setConnected(true); changed();} }
    public synchronized void disconnected(String session) { Player p=bySession.get(session); if(p!=null){p.setConnected(false);p.setDisconnectedSince(Instant.now()); changed();} }
    public synchronized PublicState publicState() { return snapshot(); }
    public synchronized PrivateState privateState(String session) {
        Player p=bySession.get(session); if(p==null) return null;
        boolean living=p.getStatus()==PlayerStatus.ALIVE;
        Role role=p.getRole(); String word=living ? p.getWord() : null;
        if (p.getStatus()==PlayerStatus.SPECTATOR && role!=Role.MR_WHITE) { word=null; }
        return new PrivateState(new PrivateState.Me(p.getId(),p.getName(),p.getStatus()), role, word,
                p.getStatus()==PlayerStatus.SPECTATOR&&role!=Role.MR_WHITE?civilianWord:null,
                p.getStatus()==PlayerStatus.SPECTATOR&&role!=Role.MR_WHITE?undercoverWord:null,
                votes.get(p.getId()), null, judges.contains(p.getId()) ? new PrivateState.Judge(guesserId,civilianWord,verdicts.containsKey(p.getId())):null,
                bySession.values().stream().filter(x->x.getStatus()==PlayerStatus.ALIVE&&!x.isConnected()).map(Player::getId).toList(),
                permissions(p));
    }
    public synchronized void command(String session, String command, Object payload) {
        Player p=bySession.get(session); if(p==null){ error(session,"INVALID_SESSION"); return; }
        lastAction=System.currentTimeMillis();
        try {
            switch(command) {
                case "sync" -> sync(session);
                case "leave" -> remove(p);
                case "start" -> start(p, ((CommandPayloads.Start)payload).expectedCount());
                case "assignment-ready" -> assignmentReady(p);
                case "next-speaker" -> nextSpeaker(p);
                case "skip-speaker" -> skip(p);
                case "end-round" -> endRound(p);
                case "vote" -> vote(p, ((CommandPayloads.Vote)payload).targetId());
                case "ready" -> executionReady(p);
                case "declare-guess" -> declareGuess(p);
                case "guess-verdict" -> verdict(p, ((CommandPayloads.Verdict)payload).correct());
                case "remove-player" -> removeAbsent(p, ((CommandPayloads.Remove)payload).playerId());
                case "end-game" -> abort(p);
                case "return-to-lobby" -> returnLobby(p);
                default -> error(session,"That action is no longer available.");
            }
        } catch (RuntimeException ex) { error(session, ex.getMessage()); }
    }
    private void start(Player actor,int expected) {
        if(phase!=Phase.LOBBY||actor.getStatus()!=PlayerStatus.LOBBY) throw invalid();
        List<Player> list=active(PlayerStatus.LOBBY); if(list.size()<minPlayers||expected!=list.size()) throw new IllegalStateException("That action is no longer available.");
        WordPair pair=words.randomPair(recentPairsExcluded); String[] pairWords=words.randomAssignment(pair); civilianWord=pairWords[0]; undercoverWord=pairWords[1];
        RoleDistribution d=RoleDistributor.distribution(list.size()); List<Role> roles=new ArrayList<>();
        for(int i=0;i<d.undercovers();i++)roles.add(Role.UNDERCOVER); for(int i=0;i<d.mrWhites();i++)roles.add(Role.MR_WHITE);
        while(roles.size()<list.size())roles.add(Role.CIVILIAN); Collections.shuffle(roles);
        for(int i=0;i<list.size();i++){Player p=list.get(i);p.setStatus(PlayerStatus.ALIVE);p.setRole(roles.get(i));p.setWord(p.getRole()==Role.CIVILIAN?civilianWord:p.getRole()==Role.UNDERCOVER?undercoverWord:null);}
        phase=Phase.ASSIGNMENT; round=1; ready.clear(); changed();
    }
    private void assignmentReady(Player p){ require(phase==Phase.ASSIGNMENT&&p.getStatus()==PlayerStatus.ALIVE); if(!ready.add(p.getId()))throw invalid(); p.setAssignmentReady(true); if(ready.size()==active(PlayerStatus.ALIVE).size()){phase=Phase.DISCUSSION; reshuffle();} changed(); }
    private void nextSpeaker(Player p){ require(phase==Phase.DISCUSSION&&p.getStatus()==PlayerStatus.ALIVE&&p.getId().equals(currentSpeaker)); advanceSpeaker(); changed(); }
    private void skip(Player p){ require(phase==Phase.DISCUSSION&&p.getStatus()==PlayerStatus.ALIVE); advanceSpeaker(); changed(); }
    private void endRound(Player p){ require(phase==Phase.DISCUSSION&&p.getStatus()==PlayerStatus.ALIVE); phase=Phase.VOTING; votes.clear(); changed(); }
    private void vote(Player p,String target){ require((phase==Phase.VOTING||phase==Phase.TIEBREAK)&&p.getStatus()==PlayerStatus.ALIVE); if(votes.containsKey(p.getId())||target.equals(p.getId()))throw invalid(); if(!active(PlayerStatus.ALIVE).stream().anyMatch(x->x.getId().equals(target)))throw invalid(); votes.put(p.getId(),target); if(votes.size()==active(PlayerStatus.ALIVE).size()) resolveVote(); changed(); }
    private void resolveVote(){ VoteResolution r=VoteResolver.resolve(votes,phase==Phase.TIEBREAK); if(r.kind()==VoteResolution.Kind.TIEBREAK){phase=Phase.TIEBREAK;votes.clear();}else{executedId=r.winnerId(); if(r.kind()==VoteResolution.Kind.WHEEL){phase=Phase.WHEEL;executedId=r.tiedIds().get(ThreadLocalRandom.current().nextInt(r.tiedIds().size()));}else phase=Phase.EXECUTION; ready.clear();} }
    private void executionReady(Player p){require(phase==Phase.EXECUTION&&(p.getStatus()==PlayerStatus.ALIVE||p.getId().equals(executedId))); if(!ready.add(p.getId()))throw invalid(); p.setExecutionReady(true); if(ready.size()==active(PlayerStatus.ALIVE).size()+(executedId==null?0:1)){Player victim=byId(executedId); if(victim!=null){victim.setStatus(PlayerStatus.SPECTATOR); phase=Phase.REVEAL; if(victim.getRole()==Role.MR_WHITE&&!victim.isGuessUsed()) {guesserId=victim.getId();} else afterElimination();}} changed();}
    private void declareGuess(Player p){require((phase==Phase.DISCUSSION||phase==Phase.VOTING)&&p.getStatus()==PlayerStatus.ALIVE&&p.getRole()==Role.MR_WHITE&&!p.isGuessUsed()); p.setGuessUsed(true); guesserId=p.getId(); judges.clear(); bySession.values().stream().filter(x->(x.getRole()==Role.CIVILIAN)||(x.getRole()==Role.UNDERCOVER&&x.getStatus()==PlayerStatus.SPECTATOR)).forEach(x->judges.add(x.getId())); verdicts.clear();phase=Phase.MR_WHITE_GUESS;changed();}
    private void verdict(Player p,boolean correct){require(phase==Phase.MR_WHITE_GUESS&&judges.contains(p.getId())&&!verdicts.containsKey(p.getId())); verdicts.put(p.getId(),correct); int yes=(int)verdicts.values().stream().filter(Boolean::booleanValue).count(), answered=verdicts.size(), total=judges.size(); if(yes>total/2){victoryType=WinType.MR_WHITE;phase=Phase.VICTORY;}else if(yes+total-answered<=total/2||answered==total){Player g=byId(guesserId);if(g!=null)g.setStatus(PlayerStatus.SPECTATOR);if(g!=null)afterElimination();}changed();}
    private void afterElimination(){WinResult result=WinEvaluator.evaluate(bySession.values(),false);if(result!=null){victoryType=result.type();phase=Phase.VICTORY;}else{round++;phase=Phase.DISCUSSION;reshuffle();}}
    private void removeAbsent(Player actor,String id){require(actor.getStatus()==PlayerStatus.ALIVE||actor.getStatus()==PlayerStatus.SPECTATOR);Player target=byId(id);if(target==null||target.isConnected()||target.getDisconnectedSince()==null||target.getDisconnectedSince().plusSeconds(60).isAfter(Instant.now()))throw invalid(); remove(target);}
    private void remove(Player p){if(p.getStatus()==PlayerStatus.LOBBY||p.getStatus()==PlayerStatus.WAITING||p.getStatus()==PlayerStatus.POSTGAME){bySession.remove(p.getSessionId());}else{p.setStatus(PlayerStatus.SPECTATOR); if(phase!=Phase.VICTORY)afterElimination();}changed();}
    private void abort(Player p){require(p.getStatus()==PlayerStatus.ALIVE||p.getStatus()==PlayerStatus.SPECTATOR);abortedBy=p.getName();victoryType=WinType.ABORTED;phase=Phase.VICTORY;changed();}
    private void returnLobby(Player p){require(phase==Phase.VICTORY&&(p.getStatus()==PlayerStatus.POSTGAME||p.getStatus()==PlayerStatus.SPECTATOR));p.setStatus(PlayerStatus.LOBBY);if(bySession.values().stream().noneMatch(x->x.getStatus()==PlayerStatus.ALIVE))phase=Phase.LOBBY;changed();}
    private void reshuffle(){speakingOrder.clear();active(PlayerStatus.ALIVE).forEach(x->speakingOrder.add(x.getId()));Collections.shuffle(speakingOrder);currentSpeaker=speakingOrder.isEmpty()?null:speakingOrder.get(0);}
    private void advanceSpeaker(){int i=speakingOrder.indexOf(currentSpeaker);currentSpeaker=speakingOrder.get((i+1)%speakingOrder.size());}
    private List<Player> active(PlayerStatus status){return bySession.values().stream().filter(p->p.getStatus()==status&&p.isConnected()).toList();}
    private Player byId(String id){return bySession.values().stream().filter(p->p.getId().equals(id)).findFirst().orElse(null);}
    private void require(boolean ok){if(!ok)throw invalid();} private IllegalStateException invalid(){return new IllegalStateException("That action is no longer available.");}
    private void changed(){version++;broadcast();}
    private void broadcast(){messaging.convertAndSend("/topic/game",snapshot());for(Player p:bySession.values())send(p.getSessionId());}
    private void sync(String session){messaging.convertAndSend("/topic/game",snapshot());send(session);}
    private void send(String session){PrivateState state=privateState(session);if(state!=null)messaging.convertAndSendToUser(session,"/queue/private",state);}
    private void error(String session,String text){messaging.convertAndSendToUser(session,"/queue/errors",Map.of("message",text==null?"That action is no longer available.":text));}
    private PublicState snapshot(){List<PublicState.PlayerView> ps=bySession.values().stream().map(p->new PublicState.PlayerView(p.getId(),p.getName(),p.getStatus(),p.isConnected(),p.getDisconnectedSince()==null?null:p.getDisconnectedSince().toEpochMilli(),p.getStatus()==PlayerStatus.ALIVE?null:p.getRole())).toList();return new PublicState(version,System.currentTimeMillis(),phase,round,minPlayers,maxPlayers,ps,List.copyOf(speakingOrder),currentSpeaker,null,null,null,null,null,null,victoryType==null?null:new PublicState.VictoryView(victoryType,abortedBy,civilianWord,undercoverWord,bySession.values().stream().map(p->new PublicState.VictoryPlayer(p.getId(),p.getName(),p.getRole(),p.getStatus()==PlayerStatus.ALIVE)).toList()),null);}
    private PrivateState.Permissions permissions(Player p){boolean living=p.getStatus()==PlayerStatus.ALIVE;return new PrivateState.Permissions(phase==Phase.LOBBY&&p.getStatus()==PlayerStatus.LOBBY,phase==Phase.ASSIGNMENT&&living&&!p.isAssignmentReady(),phase==Phase.DISCUSSION&&living&&p.getId().equals(currentSpeaker),phase==Phase.DISCUSSION&&living,phase==Phase.DISCUSSION&&living,(phase==Phase.VOTING||phase==Phase.TIEBREAK)&&living,phase==Phase.EXECUTION&&(living||p.getId().equals(executedId)),(phase==Phase.DISCUSSION||phase==Phase.VOTING)&&living&&p.getRole()==Role.MR_WHITE&&!p.isGuessUsed(),phase==Phase.MR_WHITE_GUESS&&judges.contains(p.getId()));}
}
