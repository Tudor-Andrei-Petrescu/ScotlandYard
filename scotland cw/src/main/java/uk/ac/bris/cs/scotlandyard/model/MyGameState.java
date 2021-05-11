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

    /**
     * @param detectives
     * Checks that a detective doesn't have DOUBLE/SECRET tickets
     */

    private void rightTickets(List<Player> detectives){
        boolean rightTickets = true;
        for(Player p : detectives)
            if(p.has(ScotlandYard.Ticket.DOUBLE) || p.has(ScotlandYard.Ticket.SECRET) )
                rightTickets = false;

         if(!rightTickets)
             throw new IllegalArgumentException("Detectives cannot have MrX tickets!");

    }

    /**
     *
     * @param setup
     * Checks that there are rounds to play before the game starts, and that the graph exists
     */
    private void testSetupEmpty(GameSetup setup){
        if(setup.rounds.isEmpty())
            throw new IllegalArgumentException("Rounds is empty!");
        if(setup.graph.hashCode() == 0)
            throw new IllegalArgumentException("Graph empty");
    }

    /**
     *
     * @param remaining
     * Checks that there are players in a current round
     */

    private void testPlayersEmpty(final ImmutableSet<Piece> remaining){
        if(remaining.isEmpty()) throw new IllegalArgumentException("No players!");
    }

    /**
     *
     * @param mrX
     * Checks that mrX is not a detective
     */
    private void testMrxDetective(final Player mrX){
        if(mrX.isDetective()) throw new IllegalArgumentException("MrX is not a detective!");
    }

    /**
     *
     * @param detectives
     * Checks that there are no 2 identical detectives and that mrX is not among them
     */
    private void testImpostorAmongUs(final List<Player> detectives){
        for(Player p : detectives)
            if(!p.isDetective())
                throw new IllegalArgumentException("There cannot be more than 1 MrX!");
        int middle = detectives.size()/2, i=0;
        while(i<=middle){
            if(detectives.lastIndexOf(detectives.get(i)) != i)
                throw new IllegalArgumentException("No duplicates !");
            i++;
        }
    }

    /**
     *
     * @param detectives
     * Checks that there aren't 2 detectives on the same location, uses the Comparator interface for that
     */
    private void testDetectiveLocation(final List<Player> detectives){
        List<Player> temp = new ArrayList<>(detectives);
        temp.sort(new DetectiveLocation());
        for(int j = 0; j < temp.size() - 1 ; j++)
            if(temp.get(j).location() == temp.get(j+1).location())
                throw new IllegalArgumentException("2 Pieces on the same location!");

    }

    /**
     *
     * @param detectives
     * @param mrX
     * Helper function which returns an ImmutableList with every player in the game, mrX being the first one
     */
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

    /**
     *
     * @returns the Game setup
     */
    @Override @Nonnull
    public GameSetup getSetup(){
        return this.setup;
    }

    /**
     *
     * @returns an ImmutableSet with every piece in the game
     */
    @Override @Nonnull
    public ImmutableSet<Piece> getPlayers(){
        Set<Piece> players = new HashSet<>();
            players.add(mrX.piece());
        for(Player p : this.detectives)
            players.add(p.piece());
        return ImmutableSet.copyOf(players);
    }

    /**
     *
     * @param detective the detective
     * @returns the location of a detective if there is such player, otherwise empty
     */
    @Override @Nonnull
    public Optional<Integer> getDetectiveLocation(Piece.Detective detective){
       for(Player p : this.detectives)
           if(p.piece() == detective)
               return Optional.of(p.location());
           return Optional.empty();

    }

    /**
     *
     * @param piece the player piece
     * If the player exits it returns an anonymous class which can be used to get the number of tickets for a specific player, otherwise empty
     */
    @Override @Nonnull
    public Optional<TicketBoard> getPlayerTickets(Piece piece){
        for(Player p : everyone)
            if(p.piece() == piece) return Optional.of(new TicketBoard() {
                @Override
                public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
                    return p.tickets().get(ticket);
                }
            });
        return Optional.empty();

    }

    /**
     *
     * @param setup
     * @param detectives
     * @param player
     * @param source
     *@returns an ImmutableSet with all the possible moves for a given player, with respect to the locations of the detectives in the game and the number of tickets
     */
    private static ImmutableSet<Move.SingleMove> makeSingleMoves(
            GameSetup setup,
            List<Player> detectives,
            Player player,
            int source){
        final var singleMoves = new ArrayList<Move.SingleMove>();
        for(int destination : setup.graph.adjacentNodes(source)) {
            boolean locationEmpty = true;
            for(Player p : detectives) {
                if (p.location() == destination)
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

    /**
     *
     * @param setup
     * @param detectives
     * @param mrX
     * @param source
     * @returns an ImmutableSet with all the double moves mrX can make
     */
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
                for(Player p : detectives) {
                    if (p.location() == destination2) {
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

    /**
     *
     * @returns the travel log of mrX
     */
    @Override @Nonnull
    public ImmutableList<LogEntry> getMrXTravelLog(){
        return this.log;
    }

    /**
     *
     * @returns whether the detectives have won the game
     */
    private boolean detectivesWin(){
        boolean detectivesWin = false;
        for(Player p : detectives)
            if(p.location() == mrX.location())
                detectivesWin = true;
            if(makeSingleMoves(setup,detectives,mrX, mrX.location()).isEmpty() && this.remaining.contains(mrX.piece()))
                detectivesWin = true;
            return detectivesWin;

    }

    /**
     *
     * @returns whether mrX has won the game
     */
    private boolean mrxWins(){
        boolean mrxWins = false;
        if(this.log.size() == setup.rounds.size() && this.remaining.contains(mrX.piece()))
            mrxWins = true;
        int size = 0;
        for(Player p : detectives){
            if(makeSingleMoves(setup,detectives,p, p.location()).isEmpty())
                size ++;
        }
        if(size == detectives.size())
            mrxWins =  true;
        return mrxWins;
    }

    /**
     *
     * @returns the winner
     */
    @Override @Nonnull
    public ImmutableSet<Piece> getWinner(){
       return this.winner;
    }

    /**
     *
     * @param piece
     * @return the player object of a given piece
     */
    private Player getPlayer(Piece piece){
        Player tmp = null;
        for(Player p : everyone)
            if(p.piece().equals(piece))
               tmp = p;
            return tmp;
    }

    /**
     *
     * @returns an ImmutableSet with all the available moves that mrX or the detectives can make
     */
    @Override @Nonnull
    public ImmutableSet<Move> getAvailableMoves() {

        if(!getWinner().isEmpty())
            return ImmutableSet.copyOf(new HashSet<>());

        Set<Move.SingleMove> singleMoves = new HashSet<>();
        Player player;
        for (Piece p : this.remaining) {
            player = getPlayer(p);
            singleMoves.addAll(makeSingleMoves(setup, detectives, player, player.location()));
        }
        Set<Move> moves = new HashSet<>(singleMoves);

        //Checking that mrX can use the DOUBLE ticket, or if he has one
        if(this.remaining.contains(mrX.piece()) && mrX.has(ScotlandYard.Ticket.DOUBLE) && this.log.size() +2 <=setup.rounds.size()){
            Set<Move.DoubleMove> doubleMoves = new HashSet<>(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
            moves.addAll(doubleMoves);
        }
        this.moves = ImmutableSet.copyOf(moves);
        return this.moves;
    }

    /**
     *
     * @param move
     * @param location
     * @returns an updated log after mrX makes a move
     */
    private List<LogEntry> updateLog(Move move,int location){
        List<LogEntry> newLog = new ArrayList<>(this.log);
        for(ScotlandYard.Ticket t : move.tickets()){
            int size = newLog.size();
            if(t != ScotlandYard.Ticket.DOUBLE){
            if(setup.rounds.get(size) )
                newLog.add(LogEntry.reveal(t,location));
            else newLog.add(LogEntry.hidden(t));}
        }
        return newLog;
    }

    /**
     *
     * @param pieces
     * @param piece
     * Removes a given piece from the current Set
     */
   private void removePiece(Set<Piece> pieces, Piece piece){
       pieces.remove(piece);

    }

    /**
     *
     * @param everyone
     * @returns a Set with the pieces of every player
     */
    private Set<Piece> createSet(List<Player> everyone){
        Set<Piece> pieces = new HashSet<>();
        for(Player x : everyone)
         pieces.add(x.piece());

        return pieces;
    }

    /**
     *
     * @param move
     * Implements the visitor pattern, returns the destination of a move
     */
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

    /**
     *
     * @param move the move to make
     * If the move is allowed, then the player is updated to that location and a new GameState is returned
     */
    @Override @Nonnull
    public GameState advance(Move move){
        getAvailableMoves();
        if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

        List<LogEntry> newLogEntry = new ArrayList<>(this.log);
        List<Player> detectiveList = new ArrayList<>(); //list with all detectives; at first it also contains mrX to ease the work
        Set<Piece> remainingAfter =new HashSet<>(this.remaining);

        for(Player p : everyone){

            Player tmp = p;
            if(p.piece() == move.commencedBy()){

                tmp=tmp.use(move.tickets());

                int location = visitMe(move);
                tmp=tmp.at(location);

                if(move.commencedBy().isMrX())
                    newLogEntry = updateLog(move,location);


            }
            detectiveList.add(tmp);
        }

        for(Player p : detectives)
            if(makeSingleMoves(setup,detectives,p,p.location()).isEmpty())
                removePiece(remainingAfter,p.piece());

        if(remainingAfter.size() == 1 ){
            if (remainingAfter.contains(this.mrX.piece())){
                remainingAfter = createSet(detectiveList);
                removePiece(remainingAfter,move.commencedBy());
            }
             else{
                remainingAfter = createSet(detectiveList);
                 for(Player p : detectives)
                     removePiece(remainingAfter,p.piece());
            }

        }
        else removePiece(remainingAfter, move.commencedBy());

        Player mrx = detectiveList.get(0);

        if(move.commencedBy().isDetective()){

            for(ScotlandYard.Ticket t : move.tickets())
                mrx = mrx.give(t);

        }


        detectiveList.remove(0);
        return new MyGameState(setup,ImmutableSet.copyOf(remainingAfter),ImmutableList.copyOf(newLogEntry),mrx,ImmutableList.copyOf(detectiveList));

    }


}
