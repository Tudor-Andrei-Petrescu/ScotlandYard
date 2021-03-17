package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.*;

final class MyGameState implements GameState {
    private final GameSetup setup;
    private ImmutableSet<Piece> remaining;
    private ImmutableList<LogEntry> log;
    private final Player mrX;
    private final List<Player> detectives;
    private ImmutableList<Player> everyone;
    private ImmutableSet<Move> moves;
    private ImmutableSet<Piece> winner;

    void rightTickets(List<Player> detectives){
        boolean rightTickets = true;
        for(Player x : detectives)
            if(x.has(ScotlandYard.Ticket.DOUBLE) || x.has(ScotlandYard.Ticket.SECRET) )
                rightTickets = false;

         if(rightTickets == false)
             throw new IllegalArgumentException("Detectives cannot have MrX tickets!");

    }

    void testSetupEmpty(GameSetup setup){
        if(setup.rounds.isEmpty())
            throw new IllegalArgumentException("Rounds is empty!");
        if(setup.graph.hashCode() == 0)
            throw new IllegalArgumentException("Graph empty");
    }

    void testPlayersEmpty(final ImmutableSet<Piece> remaining){
        if(remaining.isEmpty()) throw new IllegalArgumentException("No players!");
    }

    void testMrxDetective(final Player mrX){
        if(mrX.isDetective() == true) throw new IllegalArgumentException("MrX is not a detective!");
    }

    void testImpostorAmongUs(final List<Player> detectives){
        for(Player x : detectives)
            if(x.isDetective() == false)
                throw new IllegalArgumentException("There cannot be more than 1 MrX!");
        int middle = detectives.size()/2, i=0;
        while(i<=middle){
            if(detectives.lastIndexOf(detectives.get(i)) != i)
                throw new IllegalArgumentException("No duplicates !");
            i++;
        }
    }

    void testDetectiveLocation(final List<Player> detectives){
        List<Player> temp = new ArrayList<Player>();
        temp.addAll(detectives);
        temp.sort(new DetectiveLocation());
        for(int j = 0; j < temp.size() - 1 ; j++)
            if(temp.get(j).location() == temp.get(j+1).location())
                throw new IllegalArgumentException("2 Pieces on the same location!");

    }

    ImmutableList<Player> initEveryone(List<Player> detectives, Player mrX){
        List<Player> list = new ArrayList<>();
        list.add(mrX);
        list.addAll(detectives);
        ImmutableList<Player> all = ImmutableList.copyOf(list);
        return all;


    }
    MyGameState(final GameSetup setup,
                final ImmutableSet<Piece> remaining,
                final ImmutableList<LogEntry> log,
                final Player mrX,
                final List<Player> detectives){


         testSetupEmpty(setup);
         this.setup = setup;
         testPlayersEmpty(remaining);
         this.remaining = remaining;
         this.log = log;
         testMrxDetective(mrX);
         this.mrX = mrX;
         testImpostorAmongUs(detectives);
         testDetectiveLocation(detectives);
         rightTickets(detectives);
         this.detectives = detectives;
         this.everyone = initEveryone(detectives,mrX);
         Set<Piece> win = new HashSet<>();
         this.winner = ImmutableSet.copyOf(win);
    }
    @Override
    public GameSetup getSetup(){
        return this.setup;
    }

    @Override
    public ImmutableSet<Piece> getPlayers(){
        Set<Piece> players = new HashSet<>();
            players.add(mrX.piece());
        for(Player x : this.detectives)
            players.add(x.piece());
       ImmutableSet<Piece> immPlayers = ImmutableSet.copyOf(players);
       return immPlayers;
    }
    @Override
    public Optional<Integer> getDetectiveLocation(Piece.Detective detective){
       for(Player x : this.detectives)
           if(x.piece().equals(detective))
               return Optional.of(x.location());
           return Optional.empty();

    }

    @Override
    public Optional<TicketBoard> getPlayerTickets(Piece piece){
        for(Player x : everyone)
            if(x.piece() == piece) return Optional.of(new TicketBoard() {
                @Override
                public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
                    return x.tickets().get(ticket);
                }
            });
        return Optional.empty();

    }
    private static ImmutableSet<Move.SingleMove> makeSingleMoves(
            GameSetup setup,
            List<Player> detectives,
            Player player,
            int source){
        final var singleMoves = new ArrayList<Move.SingleMove>();
        for(int destination : setup.graph.adjacentNodes(source)) {
            boolean locationEmpty = true;
            for(Player x : detectives) {
                if(x.location() == destination)
                    locationEmpty = false;
                    break;
            }
            if(locationEmpty){
                    for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
                        if (player.has(t.requiredTicket()))
                            singleMoves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
                        if(player.isMrX() && player.has(ScotlandYard.Ticket.SECRET))
                            singleMoves.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination));
                    }
            }
        }
        return ImmutableSet.copyOf(singleMoves);
    }

    @Override
    public ImmutableList<LogEntry> getMrXTravelLog(){
        return this.log;
    }
    @Override
    public ImmutableSet<Piece> getWinner(){
        return this.winner;
    }

    @Override
    public ImmutableSet<Move> getAvailableMoves(){
        Set<Move> moves = new HashSet<>();
        for(Player x : everyone)
            moves.add((Move) makeSingleMoves(setup, detectives, x, x.location()));
        this.moves= ImmutableSet.copyOf(moves);
        return this.moves;
    }

    @Override
    public GameState advance(Move move){
        return null;
    }

}
