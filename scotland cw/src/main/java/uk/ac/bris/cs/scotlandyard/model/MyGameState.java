package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;

import javax.annotation.Nonnull;
import java.util.*;

final class MyGameState implements GameState {
    private final GameSetup setup;
    private final ImmutableSet<Piece> remaining;
    private final ImmutableList<LogEntry> log;
    private final Player mrX;
    private final List<Player> detectives;
    private final ImmutableList<Player> everyone;
    private ImmutableSet<Move> moves;
    private final ImmutableSet<Piece> winner;

    private void rightTickets(List<Player> detectives){
        boolean rightTickets = true;
        for(Player x : detectives)
            if(x.has(ScotlandYard.Ticket.DOUBLE) || x.has(ScotlandYard.Ticket.SECRET) )
                rightTickets = false;

         if(!rightTickets)
             throw new IllegalArgumentException("Detectives cannot have MrX tickets!");

    }

    private void testSetupEmpty(GameSetup setup){
        if(setup.rounds.isEmpty())
            throw new IllegalArgumentException("Rounds is empty!");
        if(setup.graph.hashCode() == 0)
            throw new IllegalArgumentException("Graph empty");
    }

    private void testPlayersEmpty(final ImmutableSet<Piece> remaining){
        if(remaining.isEmpty()) throw new IllegalArgumentException("No players!");
    }

    private void testMrxDetective(final Player mrX){
        if(mrX.isDetective()) throw new IllegalArgumentException("MrX is not a detective!");
    }
    private void testImpostorAmongUs(final List<Player> detectives){
        for(Player x : detectives)
            if(!x.isDetective())
                throw new IllegalArgumentException("There cannot be more than 1 MrX!");
        int middle = detectives.size()/2, i=0;
        while(i<=middle){
            if(detectives.lastIndexOf(detectives.get(i)) != i)
                throw new IllegalArgumentException("No duplicates !");
            i++;
        }
    }

    private void testDetectiveLocation(final List<Player> detectives){
        List<Player> temp = new ArrayList<>(detectives);
        temp.sort(new DetectiveLocation());
        for(int j = 0; j < temp.size() - 1 ; j++)
            if(temp.get(j).location() == temp.get(j+1).location())
                throw new IllegalArgumentException("2 Pieces on the same location!");

    }

   private ImmutableList<Player> initEveryone(List<Player> detectives, Player mrX){
        List<Player> list = new ArrayList<>();
        list.add(mrX);
        list.addAll(detectives);
       return ImmutableList.copyOf(list);


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
        if(detectivesWin()){
            for(Player x : detectives)
                win.add(x.piece());
        }
        if(mrxWins())
            win.add(mrX.piece());
         this.winner = ImmutableSet.copyOf(win);




    }

    @Override @Nonnull
    public GameSetup getSetup(){
        return this.setup;
    }

    @Override @Nonnull
    public ImmutableSet<Piece> getPlayers(){
        Set<Piece> players = new HashSet<>();
            players.add(mrX.piece());
        for(Player x : this.detectives)
            players.add(x.piece());
        return ImmutableSet.copyOf(players);
    }
    @Override @Nonnull
    public Optional<Integer> getDetectiveLocation(Piece.Detective detective){
       for(Player x : this.detectives)
           if(x.piece() == detective)
               return Optional.of(x.location());
           return Optional.empty();

    }

    @Override @Nonnull
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
                if (x.location() == destination)
                    locationEmpty = false;
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
    private static ImmutableSet<Move.DoubleMove> makeDoubleMoves(
            GameSetup setup,
            List<Player> detectives,
            Player mrX,
            int source
    ){
        final var doubleMoves = new ArrayList<Move.DoubleMove>();
        Set<Move.SingleMove> singleMoves = new HashSet<>(makeSingleMoves(setup, detectives, mrX, source));
        for(Move.SingleMove move1  : singleMoves){
            int destination1 = move1.destination;
            Player tmp = mrX.use(move1.tickets());
            for(int destination2 : setup.graph.adjacentNodes(destination1)){
                boolean locationEmpty = true;
                for(Player x : detectives) {
                    if (x.location() == destination2) {
                        locationEmpty = false;

                    }
                }
                if(locationEmpty){
                    for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(destination1, destination2, ImmutableSet.of())) {
                        if (tmp.has(t.requiredTicket()))
                            doubleMoves.add(new Move.DoubleMove(mrX.piece(),source,move1.ticket,destination1,t.requiredTicket(),destination2));
                        if(mrX.has(ScotlandYard.Ticket.SECRET))
                            doubleMoves.add(new Move.DoubleMove(mrX.piece(),source,move1.ticket,destination1, ScotlandYard.Ticket.SECRET,destination2));

                    }
                }

            }
        }
      return ImmutableSet.copyOf(doubleMoves);
    }
    @Override @Nonnull
    public ImmutableList<LogEntry> getMrXTravelLog(){
        return this.log;
    }

    private boolean detectivesWin(){
        boolean detectiveswin = false;
        for(Player x : detectives)
            if(x.location() == mrX.location())
                detectiveswin = true;
            if(makeSingleMoves(setup,detectives,mrX, mrX.location()).isEmpty() && this.remaining.contains(mrX.piece()))
                detectiveswin = true;
            return detectiveswin;

    }
    private boolean mrxWins(){
        boolean mrxWins = false;
        if(this.log.size() == setup.rounds.size() && this.remaining.contains(mrX.piece()))
            mrxWins = true;
        int size = 0;
        for(Player x : detectives){
            if(makeSingleMoves(setup,detectives,x, x.location()).isEmpty())
                size ++;
        }
        if(size == detectives.size())
            mrxWins =  true;
        return mrxWins;
    }

    @Override @Nonnull
    public ImmutableSet<Piece> getWinner(){
       return this.winner;
    }
    private Player getPlayer(Piece piece){
        Player tmp = null;
        for(Player x : everyone)
            if(x.piece().equals(piece))
               tmp = x;
            return tmp;
    }
    @Override @Nonnull
    public ImmutableSet<Move> getAvailableMoves() {

        if(!getWinner().isEmpty())
            return ImmutableSet.copyOf(new HashSet<>());

        Set<Move.SingleMove> singleMoves = new HashSet<>();
        Player player;
        for (Piece x : this.remaining) {
            player = getPlayer(x);
            singleMoves.addAll(makeSingleMoves(setup, detectives, player, player.location()));
        }
        Set<Move> moves = new HashSet<>(singleMoves);
        if(this.remaining.contains(mrX.piece()) && mrX.has(ScotlandYard.Ticket.DOUBLE) && this.log.size() +2 <=setup.rounds.size()){
            Set<Move.DoubleMove> doubleMoves = new HashSet<>(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
            moves.addAll(doubleMoves);
        }
        this.moves = ImmutableSet.copyOf(moves);
        return this.moves;
    }

    private List<LogEntry> updateLog(Move move,int location){
        List<LogEntry> newlog = new ArrayList<>(this.log);
        for(ScotlandYard.Ticket t : move.tickets()){
            int size = newlog.size();
            if(t != ScotlandYard.Ticket.DOUBLE){
            if(setup.rounds.get(size) )
                newlog.add(LogEntry.reveal(t,location));
            else newlog.add(LogEntry.hidden(t));}
        }
        return newlog;
    }

   private void removePiece(Set<Piece> pieces, Piece piece){
       pieces.remove(piece);

    }
    private Set<Piece> createSet(List<Player> everyone){
        Set<Piece> pieces = new HashSet<>();
        for(Player x : everyone)
         pieces.add(x.piece());

        return pieces;
    }
    private int visitMe(Move move){

        return move.visit(new Move.Visitor<>(){

            @Override
            public Integer visit(Move.SingleMove move1) {
                return move1.destination;
            }

            @Override
            public Integer visit(Move.DoubleMove move1) {
                return move1.destination2;
            }
        });
    }
    @Override @Nonnull
    public GameState advance(Move move){
        getAvailableMoves();
        if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
        List<LogEntry> newLogEntry = new ArrayList<>(this.log);
        List<Player> evr = new ArrayList<>();
        Set<Piece> remainingafter =new HashSet<>(this.remaining);

        for(Player x : everyone){

            Player tmp = x;
            if(x.piece() == move.commencedBy()){

                tmp=tmp.use(move.tickets());

                int location = visitMe(move);
                tmp=tmp.at(location);

                if(move.commencedBy().isMrX())
                    newLogEntry = updateLog(move,location);


            }
            evr.add(tmp);
        }

        for(Player p : detectives)
            if(makeSingleMoves(setup,detectives,p,p.location()).isEmpty())
                removePiece(remainingafter,p.piece());

        if(remainingafter.size() == 1 ){
            if (remainingafter.contains(this.mrX.piece())){
                remainingafter = createSet(evr);
                removePiece(remainingafter,move.commencedBy());
            }
             else{
                remainingafter = createSet(evr);
                 for(Player x : detectives)
                     removePiece(remainingafter,x.piece());
            }

        }
        else removePiece(remainingafter, move.commencedBy());

        Player mrx = evr.get(0);

        if(move.commencedBy().isDetective()){

            for(ScotlandYard.Ticket t : move.tickets())
                mrx = mrx.give(t);
            evr.remove(0);
            evr.add(0,mrx);
        }


        evr.remove(0);
        return new MyGameState(setup,ImmutableSet.copyOf(remainingafter),ImmutableList.copyOf(newLogEntry),mrx,ImmutableList.copyOf(evr));

    }


}
